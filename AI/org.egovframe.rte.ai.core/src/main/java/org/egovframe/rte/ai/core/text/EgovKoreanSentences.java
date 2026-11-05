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

/**
 * 한국어 문장 경계를 <b>보수적·결정론적</b>으로 판정하는 유틸 — 청킹이 문장 중간에서 끊기지 않게 하는 기초.
 *
 * <p>경계 규칙(스펙 {@code ai/korean-sentence-chunking}):</p>
 * <ul>
 *   <li>빈 줄(연속 개행 2개 이상)과 마크다운 블록 시작 줄(<code>#</code>·<code>-</code>·<code>*</code>·<code>&gt;</code>·
 *       <code>|</code>·번호 목록)은 경계, 코드펜스(백틱·물결 3개 이상) 블록은 통째로 한 구간</li>
 *   <li>마침표는 한국어 종결어미(다·요·까·죠·음·임·함·네·오) 뒤에 오고 다음이 공백·개행·문서 끝일 때만 경계 —
 *       날짜·소수점·라틴 약어·목록 번호·법령 조항의 마침표는 경계가 아니다</li>
 *   <li>물음표·느낌표·전각 종결부호는 공백·개행·문서 끝이 이어지면 경계(같은 줄의 닫히지 않은 마크다운 링크·이미지 안이면 제외)</li>
 *   <li>종결부호 뒤 닫는 인용부호·괄호는 앞 문장에 포함, 인용을 닫은 뒤 같은 줄에 한글이 이어지면 경계가 아니다
 *       ("…한다."라고 말했다)</li>
 *   <li>공백 없이 종결어미 뒤에 한글·여는 인용부호가 바로 이어지면 경계</li>
 * </ul>
 *
 * <p>경계를 놓치면 두 문장이 한 구간으로 남을 뿐이고 구간은 원문 offset 이라 내용이 훼손되지 않는다. 반대로 경계를
 * 잘못 만들면 문장이 중간에서 끊기므로 확신 없는 마침표는 자르지 않는다. 영어 문장 끝 마침표는 경계가 아니라서
 * 영어 비중이 높은 문서는 구간이 길어진다 — 의도된 제약(청킹기의 초과 문장 창 분할이 보완).</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 공식 egovframe-ai-rag 샘플의 검증된 경계 규칙을
 *                            스펙으로 옮기고 재구성. 두 샘플이 376줄 동일 사본으로 갖던 것의 단일 원천)
 *  2026.09.07  실행환경팀     보안 재점검(4차): 인라인 마크업 계수를 전진 스캐너로 교체(개행 없는 긴 줄 O(n²) → O(n))
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovKoreanSentences {

	/** 한국어 종결어미로 볼 마지막 음절 — 이 뒤의 마침표만 경계 후보다. */
	private static final String KOREAN_ENDINGS = "다요까죠음임함네오";

	/** 종결부호 후보(반각·전각). */
	private static final String TERMINATORS = ".!?。！？";

	/** 종결부호 뒤에 붙어 앞 문장에 속하는 닫는 인용부호·괄호. */
	private static final String CLOSING_MARKS = "\"'」』)]》";

	/** 공백 없이 다음 문장을 여는 인용부호·괄호. */
	private static final String OPENING_MARKS = "\"'「『([《";

	private EgovKoreanSentences() {
	}

	/**
	 * 원문 안의 문장 구간 — {@code start} 포함·{@code end} 제외, 앞뒤 공백 제외.
	 *
	 * @param start 시작 offset(포함)
	 * @param end 끝 offset(제외)
	 */
	public record Span(int start, int end) {

		public Span {
			if (start < 0 || end < start) {
				throw new IllegalArgumentException("잘못된 구간: [" + start + ", " + end + ")");
			}
		}

		/** 구간 길이(문자 수). */
		public int length() {
			return end - start;
		}

		/** 원문에서 이 구간의 텍스트를 잘라낸다. */
		public String text(String source) {
			return source.substring(start, end);
		}
	}

	/**
	 * 문장 구간을 원문 offset 기준으로 돌려준다(오름차순·비중첩·앞뒤 공백 제외).
	 *
	 * @param text 원문(null·공백뿐이면 빈 목록)
	 * @return 불변 구간 목록
	 */
	public static List<Span> spans(String text) {
		if (text == null || text.isBlank()) {
			return List.of();
		}
		List<Span> spans = new ArrayList<>();
		InlineMarkupScanner markup = new InlineMarkupScanner();
		int sentenceStart = 0;
		int index = 0;
		int length = text.length();
		while (index < length) {
			Fence fence = fenceAt(text, index);
			if (fence != null) {
				// 코드펜스 안의 문장부호는 코드 조각일 수 있으므로 블록 전체를 한 구간으로 고정한다
				add(spans, text, sentenceStart, index);
				int fenceEnd = consumeFence(text, index, fence);
				add(spans, text, index, fenceEnd);
				sentenceStart = fenceEnd;
				index = fenceEnd;
				continue;
			}
			char current = text.charAt(index);
			if (isLineBreak(current)) {
				int runCount = 0;
				int runEnd = index;
				while (runEnd < length && isLineBreak(text.charAt(runEnd))) {
					if (text.charAt(runEnd) == '\r' && runEnd + 1 < length && text.charAt(runEnd + 1) == '\n') {
						runEnd += 2;
					} else {
						runEnd++;
					}
					runCount++;
				}
				if (runCount >= 2) {
					// 빈 줄 = 문단 전환 — 종결부호가 없어도 경계
					add(spans, text, sentenceStart, index);
					sentenceStart = runEnd;
				} else if (isBlockStart(text, lineStart(text, index)) || isBlockStart(text, runEnd)) {
					// 단일 개행은 보통 하드랩이지만 앞뒤 줄이 마크다운 블록이면 문단 섞임을 막기 위해 경계
					add(spans, text, sentenceStart, index);
					sentenceStart = runEnd;
				}
				index = runEnd;
				continue;
			}
			if (TERMINATORS.indexOf(current) >= 0) {
				int[] boundary = evaluateTerminator(text, index, markup);
				if (boundary[0] == 1) {
					add(spans, text, sentenceStart, boundary[1]);
					sentenceStart = boundary[1];
				}
				index = boundary[1];
				continue;
			}
			index++;
		}
		add(spans, text, sentenceStart, length);
		return List.copyOf(spans);
	}

	/**
	 * 문장 텍스트 목록(앞뒤 공백 제거).
	 *
	 * @param text 원문
	 * @return 불변 문장 목록
	 */
	public static List<String> split(String text) {
		List<Span> spans = spans(text);
		if (spans.isEmpty()) {
			return List.of();
		}
		List<String> sentences = new ArrayList<>(spans.size());
		for (Span span : spans) {
			sentences.add(span.text(text));
		}
		return List.copyOf(sentences);
	}

	// ---------------------------------------------------------------- 종결부호 판정

	/** @return {경계 여부(1/0), 경계 끝 offset} */
	private static int[] evaluateTerminator(String text, int terminatorStart, InlineMarkupScanner markup) {
		int length = text.length();
		int runEnd = terminatorStart + 1;
		while (runEnd < length && TERMINATORS.indexOf(text.charAt(runEnd)) >= 0) {
			runEnd++;
		}
		// "..."·"?!" 같은 종결부호 런은 한 번에 소비해 빈 문장이 생기지 않게 한다. 닫는 인용·괄호는 앞 문장에 포함
		int boundaryEnd = runEnd;
		while (boundaryEnd < length && CLOSING_MARKS.indexOf(text.charAt(boundaryEnd)) >= 0) {
			boundaryEnd++;
		}
		boolean consumedClosing = boundaryEnd > runEnd;
		char previous = terminatorStart > 0 ? text.charAt(terminatorStart - 1) : '\0';
		char next = boundaryEnd < length ? text.charAt(boundaryEnd) : '\0';
		boolean followedByBreak = next == '\0' || Character.isWhitespace(next);
		boolean boundary;
		if (text.charAt(terminatorStart) == '.') {
			// 마침표는 날짜·소수점·약어·목록 번호와 구분되지 않으므로 한국어 종결어미 뒤에서만 경계
			boundary = isKoreanEnding(previous) && followedByBreak;
		} else {
			// 물음표·느낌표는 다른 표기로 쓰이지 않는다 — 단 같은 줄의 닫히지 않은 링크·이미지 안이면 제외
			boundary = followedByBreak && !markup.insideUnclosed(text, terminatorStart);
		}
		// 인용을 닫은 뒤 같은 줄에 한글이 이어지면 인용을 받는 문장 성분("…"라고 말했다)이므로 자르지 않는다
		if (boundary && consumedClosing && continuesWithHangulOnSameLine(text, boundaryEnd)) {
			boundary = false;
		}
		// 공백 없이 다음 문장이 붙는 표기도 종결어미 뒤라면 경계 — 닫는 인용을 소비한 경우는 위와 같은 이유로 제외
		if (!boundary && !consumedClosing && isKoreanEnding(previous) && isSafeNoSpaceNext(next)) {
			boundary = true;
		}
		return new int[] { boundary ? 1 : 0, boundaryEnd };
	}

	private static boolean isKoreanEnding(char ch) {
		return KOREAN_ENDINGS.indexOf(ch) >= 0;
	}

	private static boolean isSafeNoSpaceNext(char ch) {
		if (ch == '\0' || isLineBreak(ch) || OPENING_MARKS.indexOf(ch) >= 0) {
			return true;
		}
		return isHangulSyllable(ch);
	}

	private static boolean continuesWithHangulOnSameLine(String text, int start) {
		int index = skipHorizontalWhitespace(text, start);
		return index < text.length() && isHangulSyllable(text.charAt(index));
	}

	private static boolean isHangulSyllable(char ch) {
		return ch >= '가' && ch <= '힣';
	}

	/**
	 * 같은 줄 앞쪽에 닫히지 않은 [ 또는 ( 가 있으면 마크다운 링크·이미지 표기 안이다.
	 *
	 * <p>종결부호마다 줄 시작부터 다시 세면 개행 없는 긴 줄(종결부호 10만 개)에서 O(n²)가 되어 색인 스레드를 수십 초 잡아둔다
	 * (2026-09-07 보안 재점검). {@code spans()} 는 위치를 오름차순으로만 묻으므로 마지막으로 센 위치부터 앞으로만 이어 세고,
	 * 개행을 만나면 줄 단위 계수를 초기화한다 — 원래 규칙(같은 줄 안에서만 계수)과 같은 판정을 O(n) 에 낸다.</p>
	 */
	private static final class InlineMarkupScanner {

		private int scanned;
		private int bracket;
		private int paren;

		boolean insideUnclosed(String text, int index) {
			if (index < scanned) {
				scanned = 0;
				bracket = 0;
				paren = 0;
			}
			for (int i = scanned; i < index; i++) {
				char ch = text.charAt(i);
				if (isLineBreak(ch)) {
					bracket = 0;
					paren = 0;
				} else if (ch == '[') {
					bracket++;
				} else if (ch == ']' && bracket > 0) {
					bracket--;
				} else if (ch == '(') {
					paren++;
				} else if (ch == ')' && paren > 0) {
					paren--;
				}
			}
			scanned = index;
			return bracket > 0 || paren > 0;
		}
	}

	// ---------------------------------------------------------------- 마크다운 블록·코드펜스

	private static boolean isBlockStart(String text, int start) {
		int index = skipHorizontalWhitespace(text, start);
		if (index >= text.length()) {
			return false;
		}
		char ch = text.charAt(index);
		if (ch == '#' || ch == '-' || ch == '*' || ch == '>' || ch == '|') {
			return true;
		}
		if (!Character.isDigit(ch)) {
			return false;
		}
		int numberEnd = index + 1;
		while (numberEnd < text.length() && Character.isDigit(text.charAt(numberEnd))) {
			numberEnd++;
		}
		return numberEnd < text.length() && (text.charAt(numberEnd) == '.' || text.charAt(numberEnd) == ')');
	}

	private record Fence(char marker, int length) {
	}

	/** 줄 시작에서 여는 코드펜스(백틱·물결 3개 이상)면 마커와 길이를 돌려준다. */
	private static Fence fenceAt(String text, int start) {
		if (!(start == 0 || isLineBreak(text.charAt(start - 1)))) {
			return null;
		}
		int index = skipHorizontalWhitespace(text, start);
		if (index >= text.length()) {
			return null;
		}
		char marker = text.charAt(index);
		if (marker != '`' && marker != '~') {
			return null;
		}
		int length = 0;
		while (index + length < text.length() && text.charAt(index + length) == marker) {
			length++;
		}
		return length >= 3 ? new Fence(marker, length) : null;
	}

	/** 닫는 펜스(같은 마커·같거나 긴 길이·뒤에 공백만) 줄의 끝까지 소비한다. 없으면 문서 끝. */
	private static int consumeFence(String text, int fenceLineStart, Fence opening) {
		int search = nextLineStart(text, fenceLineStart);
		while (search < text.length()) {
			Fence closing = fenceAt(text, search);
			if (closing != null && closing.marker() == opening.marker() && closing.length() >= opening.length()
					&& blankAfterMarker(text, search, closing)) {
				return lineEnd(text, search);
			}
			search = nextLineStart(text, search);
		}
		return text.length();
	}

	private static boolean blankAfterMarker(String text, int lineStart, Fence fence) {
		int index = skipHorizontalWhitespace(text, lineStart) + fence.length();
		index = skipHorizontalWhitespace(text, index);
		return index >= text.length() || isLineBreak(text.charAt(index));
	}

	// ---------------------------------------------------------------- 줄·공백 보조

	private static void add(List<Span> spans, String text, int start, int end) {
		int trimmedStart = start;
		int trimmedEnd = end;
		while (trimmedStart < trimmedEnd && Character.isWhitespace(text.charAt(trimmedStart))) {
			trimmedStart++;
		}
		while (trimmedEnd > trimmedStart && Character.isWhitespace(text.charAt(trimmedEnd - 1))) {
			trimmedEnd--;
		}
		if (trimmedStart < trimmedEnd) {
			spans.add(new Span(trimmedStart, trimmedEnd));
		}
	}

	private static boolean isLineBreak(char ch) {
		return ch == '\n' || ch == '\r';
	}

	private static int lineStart(String text, int index) {
		int start = index;
		while (start > 0 && !isLineBreak(text.charAt(start - 1))) {
			start--;
		}
		return start;
	}

	private static int lineEnd(String text, int index) {
		int end = index;
		while (end < text.length() && !isLineBreak(text.charAt(end))) {
			end++;
		}
		return end;
	}

	private static int nextLineStart(String text, int index) {
		int end = lineEnd(text, index);
		if (end >= text.length()) {
			return text.length();
		}
		if (text.charAt(end) == '\r' && end + 1 < text.length() && text.charAt(end + 1) == '\n') {
			return end + 2;
		}
		return end + 1;
	}

	private static int skipHorizontalWhitespace(String text, int start) {
		int index = start;
		while (index < text.length() && (text.charAt(index) == ' ' || text.charAt(index) == '\t')) {
			index++;
		}
		return index;
	}
}
