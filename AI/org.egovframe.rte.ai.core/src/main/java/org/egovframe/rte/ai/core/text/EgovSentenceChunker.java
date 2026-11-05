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
package org.egovframe.rte.ai.core.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

import org.egovframe.rte.ai.core.text.EgovKoreanSentences.Span;

/**
 * 한국어 문장 경계를 보존하는 <b>프레임워크 중립 청킹기</b> — 크기 계산기(토큰 수·문자 수)를 주입받는다.
 *
 * <p>{@link EgovKoreanSentences} 로 문장 구간을 나눈 뒤 크기 한도 안에서 연속 문장을 이어 붙인다. 청크는 항상 원문의
 * 연속 부분문자열(개행·표 구조 보존)이고, 한 문장이 한도를 넘으면 한도에 맞는 가장 긴 창으로 잘라(공백 위치로 물러남·
 * 코드포인트 경계 유지) 진행을 보장한다. 최대 청크 수에 도달하면 중단하고 {@link EgovChunks#truncated()} 로 알린다.</p>
 *
 * <p>Spring AI 어댑터는 cl100k 토큰 계산기를, LangChain4j 어댑터는 문자 수({@code String::length})를 계산기로 넣는다 —
 * 참고 샘플이 두 변종(토큰/문자, 오버랩 유무)으로 갈라 두었던 것을 한 구현으로 합쳤다.</p>
 *
 * <pre>
 * EgovSentenceChunker chunker = EgovSentenceChunker.builder(String::length).maxSize(800).minLength(50).build();
 * EgovChunks chunks = chunker.chunk(text);
 * if (chunks.truncated()) { ... 문서를 나누거나 한도를 올린다 ... }
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 샘플의 토큰/문자 두 변종을 계산기 SPI 로 통합,
 *                            한도 도달을 결과 값으로 명시)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovSentenceChunker {

	private final ToIntFunction<String> sizer;
	private final int maxSize;
	private final int minLength;
	private final int maxChunks;
	private final int overlapSize;

	private EgovSentenceChunker(Builder builder) {
		this.sizer = builder.sizer;
		this.maxSize = builder.maxSize;
		this.minLength = builder.minLength;
		this.maxChunks = builder.maxChunks;
		this.overlapSize = builder.overlapSize;
	}

	/**
	 * 빌더를 만든다.
	 *
	 * @param sizer 크기 계산기(문자열 → 크기). 토큰 계산기 또는 {@code String::length}
	 */
	public static Builder builder(ToIntFunction<String> sizer) {
		return new Builder(sizer);
	}

	/** 크기 한도(계산기 단위). */
	public int maxSize() {
		return maxSize;
	}

	/**
	 * 텍스트를 청킹한다.
	 *
	 * @param text 원문(null·공백뿐이면 빈 결과)
	 * @return 구간 목록 + 한도 도달 여부
	 */
	public EgovChunks chunk(String text) {
		if (text == null || text.isBlank()) {
			return new EgovChunks(text == null ? "" : text, List.of(), false);
		}
		List<Span> spans = EgovKoreanSentences.spans(text);
		Collector out = new Collector(text);
		int bufferStart = -1;
		int bufferEnd = -1;
		int i = 0;
		for (; i < spans.size() && !out.limitReached; i++) {
			Span span = spans.get(i);
			if (sizer.applyAsInt(span.text(text)) > maxSize) {
				if (bufferStart >= 0) {
					out.add(spans.get(bufferStart).start(), spans.get(bufferEnd).end());
					bufferStart = -1;
					bufferEnd = -1;
				}
				if (!out.limitReached) {
					addOversized(out, text, span);
				}
				continue;
			}
			if (bufferStart < 0) {
				bufferStart = i;
				bufferEnd = i;
				continue;
			}
			String candidate = text.substring(spans.get(bufferStart).start(), span.end());
			if (sizer.applyAsInt(candidate) <= maxSize) {
				bufferEnd = i;
				continue;
			}
			out.add(spans.get(bufferStart).start(), spans.get(bufferEnd).end());
			if (out.limitReached) {
				bufferStart = -1;
				break;
			}
			// 오버랩: 앞 청크의 끝 문장부터 다시 시작하되 현재 문장까지 한도에 맞아야 한다
			int overlapStart = overlapStart(text, spans, bufferStart, bufferEnd);
			bufferStart = i;
			bufferEnd = i;
			if (overlapStart >= 0) {
				int candidateStart = overlapStart;
				while (candidateStart < i
						&& sizer.applyAsInt(text.substring(spans.get(candidateStart).start(), span.end())) > maxSize) {
					candidateStart++;
				}
				bufferStart = candidateStart;
			}
		}
		if (bufferStart >= 0 && !out.limitReached) {
			out.add(spans.get(bufferStart).start(), spans.get(bufferEnd).end());
			bufferStart = -1;
		}
		// 한도에 걸려 멈췄는데 처리하지 않은 문장(또는 비우지 못한 버퍼)이 남아 있으면 "도달"이 아니라 "잘림"이다
		boolean truncated = out.truncated || (out.limitReached && (i < spans.size() || bufferStart >= 0));
		return new EgovChunks(text, out.spans, truncated);
	}

	/** 오버랩 크기 안에 들어오는 가장 이른 문장 index(없으면 -1). 오버랩은 문장 단위·원문 연속 구간. */
	private int overlapStart(String text, List<Span> spans, int segmentStart, int segmentEnd) {
		if (overlapSize <= 0) {
			return -1;
		}
		int segmentTextEnd = spans.get(segmentEnd).end();
		int overlapStart = -1;
		for (int i = segmentEnd; i >= segmentStart; i--) {
			if (sizer.applyAsInt(text.substring(spans.get(i).start(), segmentTextEnd)) > overlapSize) {
				break;
			}
			overlapStart = i;
		}
		return overlapStart;
	}

	/** 한도를 넘는 한 문장을 원문 창으로 잘라 나눈다 — 가능하면 공백에서, 항상 코드포인트 경계에서. */
	private void addOversized(Collector out, String text, Span span) {
		int cursor = span.start();
		while (cursor < span.end() && !out.limitReached) {
			int cut = longestCutWithinLimit(text, cursor, span.end());
			if (cut < span.end()) {
				int candidate = cut;
				while (candidate > cursor && !Character.isWhitespace(text.charAt(candidate))) {
					candidate--;
				}
				if (candidate > cursor) {
					cut = candidate;
				}
			}
			cut = alignToCodePoint(text, cursor, cut);
			if (cut <= cursor) {
				// 한도에 맞는 창이 없으면(보충면 문자 하나가 한도 초과) 코드포인트 하나만큼 나아가 종료를 보장한다
				cut = cursor + Character.charCount(text.codePointAt(cursor));
			}
			out.add(cursor, cut);
			cursor = cut;
			while (cursor < span.end() && Character.isWhitespace(text.charAt(cursor))) {
				cursor++;
			}
		}
		if (out.limitReached && cursor < span.end()) {
			out.truncated = true;
		}
	}

	/** [start, end) 안에서 크기 한도에 맞는 가장 긴 창의 끝을 이진 탐색한다(없으면 start). */
	private int longestCutWithinLimit(String text, int start, int end) {
		int low = start + 1;
		int high = end;
		int best = start;
		while (low <= high) {
			int mid = low + (high - low) / 2;
			int candidate = alignToCodePoint(text, start, mid);
			if (candidate > start && sizer.applyAsInt(text.substring(start, candidate)) <= maxSize) {
				best = candidate;
				low = mid + 1;
			} else {
				high = mid - 1;
			}
		}
		return best;
	}

	/** 서로게이트 페어 중간이면 코드포인트 경계로 물러난다. */
	private static int alignToCodePoint(String text, int start, int cut) {
		if (cut > start && cut < text.length() && Character.isLowSurrogate(text.charAt(cut))
				&& Character.isHighSurrogate(text.charAt(cut - 1))) {
			return cut - 1;
		}
		return cut;
	}

	/** 청크 수집기 — 최소 길이 필터·최대 청크 수·앞뒤 공백 제외를 한곳에서 처리한다. */
	private final class Collector {
		private final String text;
		private final List<Span> spans = new ArrayList<>();
		private boolean limitReached;
		private boolean truncated;

		Collector(String text) {
			this.text = text;
		}

		void add(int start, int end) {
			int s = start;
			int e = end;
			while (s < e && Character.isWhitespace(text.charAt(s))) {
				s++;
			}
			while (e > s && Character.isWhitespace(text.charAt(e - 1))) {
				e--;
			}
			if (s >= e || (e - s) < minLength) {
				return;
			}
			if (spans.size() >= maxChunks) {
				limitReached = true;
				truncated = true;
				return;
			}
			spans.add(new Span(s, e));
			if (spans.size() >= maxChunks) {
				limitReached = true;
			}
		}
	}

	/** {@link EgovSentenceChunker} 빌더. */
	public static final class Builder {
		private final ToIntFunction<String> sizer;
		private int maxSize = -1;
		private int minLength = 0;
		private int maxChunks = Integer.MAX_VALUE;
		private int overlapSize = 0;

		private Builder(ToIntFunction<String> sizer) {
			this.sizer = Objects.requireNonNull(sizer, "sizer must not be null");
		}

		/** 청크 크기 한도(계산기 단위, 1 이상 — 필수). */
		public Builder maxSize(int maxSize) {
			this.maxSize = maxSize;
			return this;
		}

		/** 이 길이(문자 수) 미만 청크는 제외한다(기본 0). */
		public Builder minLength(int minLength) {
			this.minLength = minLength;
			return this;
		}

		/** 최대 청크 수(기본 무제한). 도달하면 중단하고 결과에 표시한다. */
		public Builder maxChunks(int maxChunks) {
			this.maxChunks = maxChunks;
			return this;
		}

		/** 문장 단위 오버랩 크기(계산기 단위, 기본 0). 한도의 절반을 넘을 수 없다. */
		public Builder overlap(int overlapSize) {
			this.overlapSize = overlapSize;
			return this;
		}

		public EgovSentenceChunker build() {
			if (maxSize <= 0) {
				throw new IllegalArgumentException("maxSize 는 1 이상이어야 합니다: " + maxSize);
			}
			if (minLength < 0) {
				throw new IllegalArgumentException("minLength 는 0 이상이어야 합니다: " + minLength);
			}
			if (maxChunks <= 0) {
				throw new IllegalArgumentException("maxChunks 는 1 이상이어야 합니다: " + maxChunks);
			}
			if (overlapSize < 0 || overlapSize > maxSize / 2) {
				// 오버랩이 절반을 넘으면 창이 조금씩만 나아가 청크 수가 문서 길이에 비례해 폭증한다
				throw new IllegalArgumentException(
						"overlap 은 0 이상, maxSize 의 절반(" + (maxSize / 2) + ") 이하여야 합니다: " + overlapSize);
			}
			return new EgovSentenceChunker(this);
		}
	}
}
