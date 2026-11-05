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

import java.util.regex.Pattern;

/**
 * 색인 전 텍스트 정규화기 — <b>글자를 지우지 않는다.</b>
 *
 * <p>기본 동작은 제어 문자(탭·개행 제외)와 유니코드 서식 문자(제로폭 공백·BOM 등)만 제거한다. 한자·가운뎃점·물결·낫표·
 * 전각 종결부호·특수기호를 포함한 어떤 글자·문장부호도 지우지 않는다 — 참고 샘플의 "특수문자 정리"(허용 목록 밖 전부 삭제)는
 * <code>행정·재정 지원 ※ 「전자정부법」 제2조(定義)</code> 를 <code>행정재정 지원 전자정부법 제2조()</code> 로 만들고
 * 문장 분리기가 경계로 쓰는 문장부호까지 지우던 것이 실측되어 옵션으로도 제공하지 않는다. 정말 지워야 할 표기는
 * {@link Builder#removePattern(Pattern)} 으로 명시한다.</p>
 *
 * <p>선택 규칙: HTML 태그 제거 · 연속 공백 축약(개행 보존) · 줄바꿈 정규화(CRLF→LF, <b>빈 줄 하나는 보존</b>하고 3개 이상의
 * 연속 개행은 2개로) · 마크다운 코드 블록 제거. 줄바꿈 정규화가 문단 경계를 없애지 않으므로 문장 분리기의 빈 줄 규칙이
 * 그대로 동작한다(샘플은 빈 줄을 단일 개행으로 축약해 자기 분리기의 문단 경계를 무력화했다).</p>
 *
 * <pre>
 * EgovTextNormalizer normalizer = EgovTextNormalizer.builder().stripHtmlTags(true).collapseWhitespace(true).build();
 * EgovNormalized result = normalizer.normalize(text);
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 샘플 정규화기의 글자 삭제 기본값 교정)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovTextNormalizer {

	/** 기본 정규화기 — 제어·서식 문자 제거만. */
	public static final EgovTextNormalizer DEFAULT = builder().build();

	private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");
	private static final Pattern CODE_BLOCK = Pattern.compile("```[\\s\\S]*?```");
	private static final Pattern HORIZONTAL_WHITESPACE = Pattern.compile("[^\\S\\r\\n]+");
	private static final Pattern EXCESS_BLANK_LINES = Pattern.compile("\\n{3,}");

	private final boolean stripHtmlTags;
	private final boolean collapseWhitespace;
	private final boolean normalizeNewlines;
	private final boolean removeCodeBlocks;
	private final Pattern removePattern;

	private EgovTextNormalizer(Builder builder) {
		this.stripHtmlTags = builder.stripHtmlTags;
		this.collapseWhitespace = builder.collapseWhitespace;
		this.normalizeNewlines = builder.normalizeNewlines;
		this.removeCodeBlocks = builder.removeCodeBlocks;
		this.removePattern = builder.removePattern;
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * 정규화한다.
	 *
	 * @param text 원문(null 이면 변경 없음·길이 0 결과)
	 * @return 결과(변경 없으면 같은 객체)
	 */
	public EgovNormalized normalize(String text) {
		if (text == null || text.isEmpty()) {
			return new EgovNormalized(text, false, 0);
		}
		String result = text;
		if (normalizeNewlines) {
			result = result.replace("\r\n", "\n").replace('\r', '\n');
		}
		if (stripHtmlTags) {
			result = HTML_TAG.matcher(result).replaceAll("");
		}
		if (removeCodeBlocks) {
			result = CODE_BLOCK.matcher(result).replaceAll("");
		}
		result = stripControlAndFormat(result);
		if (collapseWhitespace) {
			result = HORIZONTAL_WHITESPACE.matcher(result).replaceAll(" ");
		}
		if (normalizeNewlines) {
			result = EXCESS_BLANK_LINES.matcher(result).replaceAll("\n\n");
		}
		if (removePattern != null) {
			result = removePattern.matcher(result).replaceAll("");
		}
		boolean changed = !result.equals(text);
		return new EgovNormalized(changed ? result : text, changed, text.length());
	}

	/** 제어 문자(탭·개행·캐리지 리턴 제외)와 유니코드 서식 문자(Cf)만 제거한다 — 글자·문장부호는 건드리지 않는다. */
	static String stripControlAndFormat(String text) {
		StringBuilder out = null;
		int length = text.length();
		for (int i = 0; i < length; ) {
			int codePoint = text.codePointAt(i);
			int width = Character.charCount(codePoint);
			boolean remove = isRemovable(codePoint);
			if (remove && out == null) {
				out = new StringBuilder(length);
				out.append(text, 0, i);
			} else if (!remove && out != null) {
				out.appendCodePoint(codePoint);
			}
			i += width;
		}
		return out == null ? text : out.toString();
	}

	private static boolean isRemovable(int codePoint) {
		if (codePoint == '\t' || codePoint == '\n' || codePoint == '\r') {
			return false;
		}
		return Character.isISOControl(codePoint) || Character.getType(codePoint) == Character.FORMAT;
	}

	/** {@link EgovTextNormalizer} 빌더 — 모든 선택 규칙의 기본은 꺼짐. */
	public static final class Builder {
		private boolean stripHtmlTags;
		private boolean collapseWhitespace;
		private boolean normalizeNewlines;
		private boolean removeCodeBlocks;
		private Pattern removePattern;

		private Builder() {
		}

		/** HTML 태그(<code>&lt;…&gt;</code>)를 제거한다. 부등호를 그대로 쓴 산문에는 오탐이 있을 수 있다. */
		public Builder stripHtmlTags(boolean enabled) {
			this.stripHtmlTags = enabled;
			return this;
		}

		/** 연속 공백·탭을 공백 하나로 축약한다(개행은 보존). */
		public Builder collapseWhitespace(boolean enabled) {
			this.collapseWhitespace = enabled;
			return this;
		}

		/** CRLF→LF 로 통일하고 3개 이상의 연속 개행을 2개로 줄인다(빈 줄 하나 보존). */
		public Builder normalizeNewlines(boolean enabled) {
			this.normalizeNewlines = enabled;
			return this;
		}

		/** 마크다운 코드 블록(<code>```…```</code>)을 제거한다. */
		public Builder removeCodeBlocks(boolean enabled) {
			this.removeCodeBlocks = enabled;
			return this;
		}

		/** 호출 측이 명시한 추가 제거 정규식(null 이면 없음). */
		public Builder removePattern(Pattern pattern) {
			this.removePattern = pattern;
			return this;
		}

		public EgovTextNormalizer build() {
			return new EgovTextNormalizer(this);
		}
	}
}
