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
package org.egovframe.rte.ai.langchain4j.etl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

import org.egovframe.rte.ai.core.document.EgovDocumentMetadata;
import org.egovframe.rte.ai.core.text.EgovChunks;
import org.egovframe.rte.ai.core.text.EgovSentenceChunker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.TokenCountEstimator;

/**
 * 한국어 문장 경계 청킹 분할기 — 기반 모듈 {@link EgovSentenceChunker} 를 LangChain4j {@link DocumentSplitter} 로 노출한다.
 *
 * <p>크기 단위는 기본 문자 수({@link EgovSegmentSizers#chars()})이며 LangChain4j {@link TokenCountEstimator} 로 바꿀 수 있다. 각 세그먼트는
 * 원본 메타데이터를 복사하고 {@code chunk_index}·{@code chunk_count} 를 덧붙인다({@code original_id} 가 함께 복사되므로 라이터의 stale 정리가
 * 동작한다). 청크 수 한도에 걸리면 정책({@link EgovTruncationPolicy}, 기본 FAIL)에 따라 예외를 던지거나 {@code truncated="true"} 표식을
 * 붙인다 — 잘림을 로그만 남기고 완료로 처리하지 않는다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j — 샘플 동명 클래스의 청킹 루프를 기반 모듈 호출로 대체)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovKoreanSentenceSplitter implements DocumentSplitter {

	private static final Logger LOG = LoggerFactory.getLogger(EgovKoreanSentenceSplitter.class);

	/** 기본 세그먼트 크기(계산기 단위 — 문자 수 기본에서는 문자 수). */
	public static final int DEFAULT_MAX_SIZE = 1000;

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

	/** 문자 수 계산기 빌더. */
	public static Builder builder() {
		return new Builder(EgovSegmentSizers.chars());
	}

	/**
	 * 계산기를 지정한 빌더.
	 *
	 * @param sizer 크기 계산기
	 * @return 빌더
	 */
	public static Builder builder(ToIntFunction<String> sizer) {
		return new Builder(sizer);
	}

	/**
	 * LangChain4j 토큰 추정기를 계산기로 쓰는 빌더.
	 *
	 * @param estimator 토큰 추정기
	 * @return 빌더
	 */
	public static Builder builder(TokenCountEstimator estimator) {
		return new Builder(EgovSegmentSizers.of(estimator));
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
	 * 문서를 세그먼트로 분할한다.
	 *
	 * @param document 문서(텍스트가 비어 있으면 빈 목록)
	 * @return 세그먼트(원본 메타데이터 + {@code chunk_index}·{@code chunk_count}[·{@code truncated}])
	 * @throws EgovChunkLimitException FAIL 정책에서 한도에 걸렸을 때
	 */
	@Override
	public List<TextSegment> split(Document document) {
		Objects.requireNonNull(document, "document must not be null");
		String text = document.text();
		if (text == null || text.isBlank()) {
			LOG.debug("텍스트가 없는 문서라 세그먼트를 만들지 않습니다: {}", originalId(document));
			return List.of();
		}
		EgovChunks chunks = chunker.chunk(text);
		boolean truncated = chunks.truncated();
		if (truncated) {
			if (truncationPolicy == EgovTruncationPolicy.FAIL) {
				throw new EgovChunkLimitException(originalId(document), maxChunks);
			}
			LOG.warn("문서 {} 의 세그먼트 수가 한도 {} 에 도달해 본문 일부가 분할되지 않았습니다 — 세그먼트에 {}={} 표식을 붙입니다",
					originalId(document), maxChunks, EgovLangChain4jMetadata.TRUNCATED, EgovLangChain4jMetadata.TRUE);
		}
		List<String> texts = chunks.texts();
		int count = texts.size();
		List<TextSegment> segments = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			Metadata metadata = document.metadata() == null ? new Metadata() : document.metadata().copy();
			metadata.put(EgovLangChain4jMetadata.CHUNK_INDEX, i);
			metadata.put(EgovLangChain4jMetadata.CHUNK_COUNT, count);
			if (truncated) {
				metadata.put(EgovLangChain4jMetadata.TRUNCATED, EgovLangChain4jMetadata.TRUE);
			}
			segments.add(new TextSegment(texts.get(i), metadata));
		}
		return segments;
	}

	private static String originalId(Document document) {
		Metadata metadata = document.metadata();
		return metadata == null ? null : metadata.getString(EgovDocumentMetadata.ORIGINAL_ID);
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

		/** 세그먼트 크기 한도(계산기 단위, 기본 {@value #DEFAULT_MAX_SIZE}). */
		public Builder maxSize(int maxSize) {
			this.maxSize = maxSize;
			return this;
		}

		/** 이 길이(문자 수) 미만 세그먼트는 제외(기본 0). */
		public Builder minLength(int minLength) {
			this.minLength = minLength;
			return this;
		}

		/** 문서당 최대 세그먼트 수(기본 무제한). */
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
