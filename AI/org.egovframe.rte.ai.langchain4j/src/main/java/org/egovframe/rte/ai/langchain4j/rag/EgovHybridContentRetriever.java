/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.ai.langchain4j.rag;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.egovframe.rte.ai.core.ingest.EgovContentHash;
import org.egovframe.rte.ai.core.retrieval.EgovLexicalSearch;
import org.egovframe.rte.ai.core.retrieval.EgovRrfFusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

/**
 * 하이브리드 검색기 — 밀집 채널({@link ContentRetriever})과 어휘 채널(기반 모듈 {@link EgovLexicalSearch})의 순위를
 * {@link EgovRrfFusion} 으로 융합해 상위 k 건을 돌려주는 LangChain4j {@link ContentRetriever}.
 *
 * <p>밀집 결과의 식별자는 {@link ContentMetadata#EMBEDDING_ID}({@code EmbeddingStoreContentRetriever} 가 넣는다)이며, 없으면 본문 해시로
 * 대체하되 그때는 어휘 채널과 키가 맞지 않아 사실상 재정렬 전용이 된다(디버그 로그). 어휘 채널에만 잡힌 세그먼트는
 * {@link EgovSegmentLookup} 으로 본문을 가져와 <b>포함</b>한다 — 조회 SPI 없이 만들면 생성 시 예외이고, 재정렬 전용은
 * {@link Builder#rerankOnly()} 를 명시해야 한다. 결과 {@link Content} 의 {@link ContentMetadata#SCORE} 는 RRF 점수다. 어휘 채널의 예외는
 * 밀집 결과로 조용히 대체하지 않고 전파한다(샘플은 경고 후 밀집만 사용).</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovHybridContentRetriever implements ContentRetriever {

	private static final Logger LOG = LoggerFactory.getLogger(EgovHybridContentRetriever.class);

	/** 기본 반환 건수. */
	public static final int DEFAULT_TOP_K = 5;

	private final ContentRetriever dense;
	private final EgovLexicalSearch lexical;
	private final EgovSegmentLookup segmentLookup;
	private final double denseWeight;
	private final double lexicalWeight;
	private final int rrfK;
	private final int topK;
	private final int lexicalTopK;

	private EgovHybridContentRetriever(Builder builder) {
		this.dense = builder.dense;
		this.lexical = builder.lexical;
		this.segmentLookup = builder.segmentLookup;
		this.denseWeight = builder.denseWeight;
		this.lexicalWeight = builder.lexicalWeight;
		this.rrfK = builder.rrfK;
		this.topK = builder.topK;
		this.lexicalTopK = builder.lexicalTopK > 0 ? builder.lexicalTopK : builder.topK * 2;
	}

	/** 빌더. */
	public static Builder builder() {
		return new Builder();
	}

	/** 재정렬 전용인가(조회 SPI 없음). */
	public boolean rerankOnly() {
		return segmentLookup == null;
	}

	/**
	 * 두 채널을 융합해 검색한다.
	 *
	 * @param query 질의(텍스트 필수)
	 * @return 융합 순서의 콘텐츠(SCORE = RRF 점수, EMBEDDING_ID = 식별자)
	 * @throws IllegalArgumentException 질의 텍스트가 비어 있을 때(채널을 부르지 않는다)
	 */
	@Override
	public List<Content> retrieve(Query query) {
		Objects.requireNonNull(query, "query must not be null");
		String text = query.text();
		if (text == null || text.isBlank()) {
			throw new IllegalArgumentException("질의 텍스트가 비어 있습니다");
		}
		Map<String, TextSegment> byId = new LinkedHashMap<>();
		List<String> denseIds = new ArrayList<>();
		List<Content> denseContents = dense.retrieve(query);
		if (denseContents != null) {
			for (Content content : denseContents) {
				if (content == null || content.textSegment() == null) {
					continue;
				}
				String id = identifierOf(content);
				if (byId.putIfAbsent(id, content.textSegment()) == null) {
					denseIds.add(id);
				}
			}
		}
		List<String> lexicalIds = lexical.search(text, lexicalTopK);
		List<EgovRrfFusion.Scored<String>> fused = EgovRrfFusion.fuseScored(denseIds,
				lexicalIds == null ? List.of() : lexicalIds, denseWeight, lexicalWeight, rrfK, topK);

		List<String> missing = new ArrayList<>();
		for (EgovRrfFusion.Scored<String> entry : fused) {
			if (!byId.containsKey(entry.key())) {
				missing.add(entry.key());
			}
		}
		if (!missing.isEmpty() && segmentLookup != null) {
			Map<String, TextSegment> found = segmentLookup.findByIds(List.copyOf(missing));
			if (found != null) {
				for (Map.Entry<String, TextSegment> entry : found.entrySet()) {
					if (entry.getKey() != null && entry.getValue() != null) {
						byId.putIfAbsent(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		List<Content> result = new ArrayList<>(fused.size());
		for (EgovRrfFusion.Scored<String> entry : fused) {
			TextSegment segment = byId.get(entry.key());
			if (segment == null) {
				if (segmentLookup != null) {
					LOG.warn("세그먼트 조회 SPI 가 식별자 {} 의 본문을 돌려주지 않아 결과에서 제외합니다", entry.key());
				} else {
					LOG.debug("재정렬 전용 — 어휘 채널에만 있는 식별자 {} 는 제외합니다", entry.key());
				}
				continue;
			}
			Map<ContentMetadata, Object> metadata = new EnumMap<>(ContentMetadata.class);
			metadata.put(ContentMetadata.SCORE, entry.score());
			metadata.put(ContentMetadata.EMBEDDING_ID, entry.key());
			result.add(Content.from(segment, metadata));
		}
		return List.copyOf(result);
	}

	/**
	 * 밀집 콘텐츠의 식별자 — {@code EMBEDDING_ID}, 없으면 본문 SHA-256(디버그 로그).
	 *
	 * @param content 콘텐츠
	 * @return 식별자
	 */
	static String identifierOf(Content content) {
		Map<ContentMetadata, Object> metadata = content.metadata();
		Object id = metadata == null ? null : metadata.get(ContentMetadata.EMBEDDING_ID);
		if (id != null && !String.valueOf(id).isBlank()) {
			return String.valueOf(id);
		}
		LOG.debug("밀집 콘텐츠에 EMBEDDING_ID 가 없어 본문 해시를 식별자로 씁니다 — 어휘 채널과 키가 맞지 않으면 재정렬 전용이 됩니다");
		return EgovContentHash.sha256Hex(content.textSegment().text());
	}

	/** {@link EgovHybridContentRetriever} 빌더. */
	public static final class Builder {
		private ContentRetriever dense;
		private EgovLexicalSearch lexical;
		private EgovSegmentLookup segmentLookup;
		private boolean rerankOnly;
		private double denseWeight = 1.0;
		private double lexicalWeight = 1.0;
		private int rrfK = EgovRrfFusion.DEFAULT_K;
		private int topK = DEFAULT_TOP_K;
		private int lexicalTopK = -1;

		private Builder() {
		}

		/** 밀집 채널(예: {@code EmbeddingStoreContentRetriever}) — 필수. */
		public Builder dense(ContentRetriever dense) {
			this.dense = dense;
			return this;
		}

		/** 어휘 채널(기반 모듈 RediSearch·pg_trgm 구현 등) — 필수. */
		public Builder lexical(EgovLexicalSearch lexical) {
			this.lexical = lexical;
			return this;
		}

		/** 어휘 단독 세그먼트의 본문 조회 SPI — {@link #rerankOnly()} 와 둘 중 하나는 필수. */
		public Builder segmentLookup(EgovSegmentLookup segmentLookup) {
			this.segmentLookup = segmentLookup;
			return this;
		}

		/** 어휘 채널을 재정렬로만 쓴다(밀집 결과에 없는 세그먼트는 제외) — 명시 선택. */
		public Builder rerankOnly() {
			this.rerankOnly = true;
			return this;
		}

		/** 밀집 채널 가중치(0 이상, 기본 1.0). */
		public Builder denseWeight(double denseWeight) {
			this.denseWeight = denseWeight;
			return this;
		}

		/** 어휘 채널 가중치(0 이상, 기본 1.0). */
		public Builder lexicalWeight(double lexicalWeight) {
			this.lexicalWeight = lexicalWeight;
			return this;
		}

		/** RRF 상수 k(0 이상, 기본 {@value EgovRrfFusion#DEFAULT_K}). */
		public Builder rrfK(int rrfK) {
			this.rrfK = rrfK;
			return this;
		}

		/** 반환 건수(1 이상, 기본 {@value #DEFAULT_TOP_K}). */
		public Builder topK(int topK) {
			this.topK = topK;
			return this;
		}

		/** 어휘 채널 요청 건수(기본 topK × 2). */
		public Builder lexicalTopK(int lexicalTopK) {
			this.lexicalTopK = lexicalTopK;
			return this;
		}

		public EgovHybridContentRetriever build() {
			Objects.requireNonNull(dense, "dense retriever must not be null");
			Objects.requireNonNull(lexical, "lexical search must not be null");
			if (segmentLookup == null && !rerankOnly) {
				throw new IllegalArgumentException("어휘 채널에만 있는 세그먼트의 본문을 조회할 EgovSegmentLookup 이 없습니다 — "
						+ "segmentLookup(...) 을 지정하거나, 어휘 채널을 재정렬로만 쓰려면 rerankOnly() 를 명시하세요");
			}
			if (segmentLookup != null && rerankOnly) {
				throw new IllegalArgumentException("segmentLookup 과 rerankOnly 는 함께 지정할 수 없습니다");
			}
			if (denseWeight < 0 || lexicalWeight < 0) {
				throw new IllegalArgumentException("가중치는 0 이상이어야 합니다: dense=" + denseWeight + ", lexical=" + lexicalWeight);
			}
			if (rrfK < 0) {
				throw new IllegalArgumentException("rrfK 는 0 이상이어야 합니다: " + rrfK);
			}
			if (topK < 1) {
				throw new IllegalArgumentException("topK 는 1 이상이어야 합니다: " + topK);
			}
			return new EgovHybridContentRetriever(this);
		}
	}
}
