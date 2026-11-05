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
package org.egovframe.rte.ai.springai.rag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.egovframe.rte.ai.core.retrieval.EgovLexicalSearch;
import org.egovframe.rte.ai.core.retrieval.EgovRrfFusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

/**
 * 하이브리드 검색기 — 밀집 채널({@link DocumentRetriever})과 어휘 채널(기반 모듈 {@link EgovLexicalSearch})의 순위를
 * {@link EgovRrfFusion} 으로 융합해 상위 k 건을 돌려주는 Spring AI {@link DocumentRetriever}.
 *
 * <p>어휘 채널에만 잡힌 문서(밀집 결과에 없는 id)는 {@link EgovDocumentLookup} 으로 본문을 조회해 <b>포함</b>한다 — 두 샘플이
 * 갈라졌던 지점(검토 4장 #7)을 하나의 정책으로 고정한 것이다. 조회 SPI 없이 만들면 생성 시 예외이고, 어휘 채널을 재정렬로만
 * 쓰려면 {@link Builder#rerankOnly()} 를 명시해야 한다. 결과 문서의 score 는 RRF 점수다. 어휘 채널의 예외는 밀집 결과로 조용히
 * 대체하지 않고 그대로 전파한다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovHybridDocumentRetriever implements DocumentRetriever {

	private static final Logger LOG = LoggerFactory.getLogger(EgovHybridDocumentRetriever.class);

	/** 기본 반환 건수. */
	public static final int DEFAULT_TOP_K = 5;

	private final DocumentRetriever dense;
	private final EgovLexicalSearch lexical;
	private final EgovDocumentLookup documentLookup;
	private final double denseWeight;
	private final double lexicalWeight;
	private final int rrfK;
	private final int topK;
	private final int lexicalTopK;

	private EgovHybridDocumentRetriever(Builder builder) {
		this.dense = builder.dense;
		this.lexical = builder.lexical;
		this.documentLookup = builder.documentLookup;
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
		return documentLookup == null;
	}

	/**
	 * 두 채널을 융합해 검색한다.
	 *
	 * @param query 질의(텍스트 필수)
	 * @return 융합 순서의 문서(score = RRF 점수, 불변)
	 * @throws IllegalArgumentException 질의 텍스트가 비어 있을 때(채널을 부르지 않는다)
	 */
	@Override
	public List<Document> retrieve(Query query) {
		Objects.requireNonNull(query, "query must not be null");
		String text = query.text();
		if (text == null || text.isBlank()) {
			throw new IllegalArgumentException("질의 텍스트가 비어 있습니다");
		}
		Map<String, Document> byId = new LinkedHashMap<>();
		List<String> denseIds = new ArrayList<>();
		List<Document> denseDocuments = dense.retrieve(query);
		if (denseDocuments != null) {
			for (Document document : denseDocuments) {
				if (document != null && document.getId() != null && byId.putIfAbsent(document.getId(), document) == null) {
					denseIds.add(document.getId());
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
		if (!missing.isEmpty() && documentLookup != null) {
			List<Document> found = documentLookup.findByIds(List.copyOf(missing));
			if (found != null) {
				for (Document document : found) {
					if (document != null && document.getId() != null) {
						byId.putIfAbsent(document.getId(), document);
					}
				}
			}
		}
		List<Document> result = new ArrayList<>(fused.size());
		for (EgovRrfFusion.Scored<String> entry : fused) {
			Document document = byId.get(entry.key());
			if (document == null) {
				if (documentLookup != null) {
					LOG.warn("문서 조회 SPI 가 id {} 의 본문을 돌려주지 않아 결과에서 제외합니다", entry.key());
				} else {
					LOG.debug("재정렬 전용 — 어휘 채널에만 있는 id {} 는 제외합니다", entry.key());
				}
				continue;
			}
			result.add(document.mutate().score(entry.score()).build());
		}
		return List.copyOf(result);
	}

	/** {@link EgovHybridDocumentRetriever} 빌더. */
	public static final class Builder {
		private DocumentRetriever dense;
		private EgovLexicalSearch lexical;
		private EgovDocumentLookup documentLookup;
		private boolean rerankOnly;
		private double denseWeight = 1.0;
		private double lexicalWeight = 1.0;
		private int rrfK = EgovRrfFusion.DEFAULT_K;
		private int topK = DEFAULT_TOP_K;
		private int lexicalTopK = -1;

		private Builder() {
		}

		/** 밀집 채널(예: {@code VectorStoreDocumentRetriever}) — 필수. */
		public Builder dense(DocumentRetriever dense) {
			this.dense = dense;
			return this;
		}

		/** 어휘 채널(기반 모듈 RediSearch·pg_trgm 구현 등) — 필수. */
		public Builder lexical(EgovLexicalSearch lexical) {
			this.lexical = lexical;
			return this;
		}

		/** 어휘 단독 문서의 본문 조회 SPI — {@link #rerankOnly()} 와 둘 중 하나는 필수. */
		public Builder documentLookup(EgovDocumentLookup documentLookup) {
			this.documentLookup = documentLookup;
			return this;
		}

		/** 어휘 채널을 재정렬로만 쓴다(밀집 결과에 없는 문서는 제외) — 명시 선택. */
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

		public EgovHybridDocumentRetriever build() {
			Objects.requireNonNull(dense, "dense retriever must not be null");
			Objects.requireNonNull(lexical, "lexical search must not be null");
			if (documentLookup == null && !rerankOnly) {
				throw new IllegalArgumentException("어휘 채널에만 있는 문서의 본문을 조회할 EgovDocumentLookup 이 없습니다 — "
						+ "documentLookup(...) 을 지정하거나, 어휘 채널을 재정렬로만 쓰려면 rerankOnly() 를 명시하세요");
			}
			if (documentLookup != null && rerankOnly) {
				throw new IllegalArgumentException("documentLookup 과 rerankOnly 는 함께 지정할 수 없습니다");
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
			return new EgovHybridDocumentRetriever(this);
		}
	}
}
