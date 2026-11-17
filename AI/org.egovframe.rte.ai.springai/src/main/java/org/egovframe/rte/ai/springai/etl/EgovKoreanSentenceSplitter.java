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
package org.egovframe.rte.ai.springai.etl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;

import org.egovframe.rte.ai.core.text.EgovChunks;
import org.egovframe.rte.ai.core.text.EgovSentenceChunker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;

/**
 * 한국어 문장 경계 청킹 분할기 — 기반 모듈 {@link EgovSentenceChunker} 를 Spring AI {@link TextSplitter} 로 노출한다.
 *
 * <p>청크 크기 단위는 기본 cl100k 토큰({@link EgovTokenSizers#cl100k()})이며 다른 계산기로 바꿀 수 있다. 각 청크 문서는 원본
 * 메타데이터 전부를 복사하고 {@code chunk_index}·{@code chunk_count} 를 덧붙인다({@code original_id} 가 함께 복사되므로
 * 라이터의 stale 청크 정리가 동작한다). 청크 수 한도에 걸리면 정책({@link EgovTruncationPolicy}, 기본 FAIL)에 따라 예외를 던지거나
 * {@code truncated=true} 표식을 붙인다 — 잘림을 로그만 남기고 완료로 처리하지 않는다.</p>
 *
 * <p>{@code TokenTextSplitter} 를 상속하지 않는 이유: 메타데이터 부여는 {@code splitText(String)} 에서 할 수 없어
 * {@link #apply(List)} 를 재정의해야 하고, 그 생성자 인자(chunkSize·minChunkSizeChars…)는 이 청킹기와 의미가 달라 상속하면 혼동만
 * 남는다({@code split(Document)} 는 {@code apply} 로 위임되므로 같은 정책이 적용된다). {@link #splitText(String)} 도 구현해 두었지만
 * 텍스트 단독 경로는 메타데이터를 붙일 수 없어 잘림을 예외로만 알린다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai — 샘플 동명 클래스의 청킹 루프를 기반 모듈 호출로 대체)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovKoreanSentenceSplitter extends TextSplitter {

	private static final Logger LOG = LoggerFactory.getLogger(EgovKoreanSentenceSplitter.class);

	/** 기본 청크 크기(계산기 단위 — cl100k 기본에서는 토큰 수). */
	public static final int DEFAULT_MAX_SIZE = 500;

	private final EgovSentenceChunker chunker;
	private final int maxChunks;
	private final EgovTruncationPolicy truncationPolicy;

	private EgovKoreanSentenceSplitter(Builder builder) {
		this.chunker = EgovSentenceChunker.builder(builder.sizer)
				.maxSize(builder.maxSize)
				.minLength(builder.minLength)
				.maxChunks(builder.maxChunks)
				.overlap(builder.overlap)
				.build();
		this.maxChunks = builder.maxChunks;
		this.truncationPolicy = builder.truncationPolicy;
	}

	/** cl100k 토큰 계산기 빌더. */
	public static Builder builder() {
		return new Builder(EgovTokenSizers.cl100k());
	}

	/**
	 * 계산기를 지정한 빌더.
	 *
	 * @param sizer 크기 계산기(문자 수 등)
	 * @return 빌더
	 */
	public static Builder builder(ToIntFunction<String> sizer) {
		return new Builder(sizer);
	}

	/** 위임 청킹기. */
	public EgovSentenceChunker chunker() {
		return chunker;
	}

	/** 잘림 정책. */
	public EgovTruncationPolicy truncationPolicy() {
		return truncationPolicy;
	}

	/**
	 * 문서 목록을 청크 문서 목록으로 분할한다.
	 *
	 * @param documents 문서(텍스트가 없는 문서는 청크를 만들지 않는다)
	 * @return 청크 문서(원본 메타데이터 + {@code chunk_index}·{@code chunk_count}[·{@code truncated}])
	 * @throws EgovChunkLimitException FAIL 정책에서 한도에 걸렸을 때
	 */
	@Override
	public List<Document> apply(List<Document> documents) {
		Objects.requireNonNull(documents, "documents must not be null");
		List<Document> chunksOut = new ArrayList<>();
		for (Document document : documents) {
			String text = document.getText();
			if (text == null || text.isBlank()) {
				LOG.debug("문서 {} 에 텍스트가 없어 청크를 만들지 않습니다", document.getId());
				continue;
			}
			EgovChunks chunks = chunker.chunk(text);
			boolean truncated = chunks.truncated();
			if (truncated) {
				if (truncationPolicy == EgovTruncationPolicy.FAIL) {
					throw new EgovChunkLimitException(document.getId(), maxChunks);
				}
				LOG.warn("문서 {} 의 청크 수가 한도 {} 에 도달해 본문 일부가 분할되지 않았습니다 — 청크에 {}=true 표식을 붙입니다",
						document.getId(), maxChunks, EgovSpringAiMetadata.TRUNCATED);
			}
			List<String> texts = chunks.texts();
			int count = texts.size();
			for (int i = 0; i < count; i++) {
				Map<String, Object> metadata = new LinkedHashMap<>(document.getMetadata());
				metadata.put(EgovSpringAiMetadata.CHUNK_INDEX, i);
				metadata.put(EgovSpringAiMetadata.CHUNK_COUNT, count);
				if (truncated) {
					metadata.put(EgovSpringAiMetadata.TRUNCATED, Boolean.TRUE);
				}
				Document chunk = Document.builder().text(texts.get(i)).metadata(metadata).build();
				if (isCopyContentFormatter()) {
					chunk.setContentFormatter(document.getContentFormatter());
				}
				chunksOut.add(chunk);
			}
		}
		return chunksOut;
	}

	/**
	 * 텍스트 단독 분할 — 메타데이터를 붙일 수 없으므로 잘림은 정책과 무관하게 예외로 알린다.
	 *
	 * @param text 본문
	 * @return 청크 텍스트
	 * @throws EgovChunkLimitException 한도에 걸렸을 때
	 */
	@Override
	protected List<String> splitText(String text) {
		EgovChunks chunks = chunker.chunk(text == null ? "" : text);
		if (chunks.truncated()) {
			throw new EgovChunkLimitException(null, maxChunks);
		}
		return chunks.texts();
	}

	/** {@link EgovKoreanSentenceSplitter} 빌더. */
	public static final class Builder {
		private final ToIntFunction<String> sizer;
		private int maxSize = DEFAULT_MAX_SIZE;
		private int minLength = 0;
		private int maxChunks = Integer.MAX_VALUE;
		private int overlap = 0;
		private EgovTruncationPolicy truncationPolicy = EgovTruncationPolicy.FAIL;

		private Builder(ToIntFunction<String> sizer) {
			this.sizer = Objects.requireNonNull(sizer, "sizer must not be null");
		}

		/** 청크 크기 한도(계산기 단위, 기본 {@value #DEFAULT_MAX_SIZE}). */
		public Builder maxSize(int maxSize) {
			this.maxSize = maxSize;
			return this;
		}

		/** 이 길이(문자 수) 미만 청크는 제외(기본 0). */
		public Builder minLength(int minLength) {
			this.minLength = minLength;
			return this;
		}

		/** 문서당 최대 청크 수(기본 무제한). */
		public Builder maxChunks(int maxChunks) {
			this.maxChunks = maxChunks;
			return this;
		}

		/** 문장 단위 오버랩 크기(계산기 단위, 기본 0 — 한도의 절반 이하). */
		public Builder overlap(int overlap) {
			this.overlap = overlap;
			return this;
		}

		/** 한도 도달 정책(기본 {@link EgovTruncationPolicy#FAIL}). */
		public Builder onTruncation(EgovTruncationPolicy policy) {
			this.truncationPolicy = Objects.requireNonNull(policy, "policy must not be null");
			return this;
		}

		public EgovKoreanSentenceSplitter build() {
			return new EgovKoreanSentenceSplitter(this);
		}
	}
}
