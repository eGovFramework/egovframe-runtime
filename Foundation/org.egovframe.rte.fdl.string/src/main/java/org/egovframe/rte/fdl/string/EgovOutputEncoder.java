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
package org.egovframe.rte.fdl.string;

import java.nio.charset.StandardCharsets;

/**
 * 값이 출력되는 컨텍스트(HTML 본문·속성, JavaScript, URL, XML)별 인코딩을 제공하는 유틸 클래스.
 * 인코딩은 출력 시점에 한 번만 적용하며, 모든 메서드는 {@code null}을 빈 문자열로 반환한다.
 * URL 속성의 스킴 검사, CSS 컨텍스트, JSON 직렬화는 다루지 않는다.
 *
 * <pre>
 * &lt;td&gt;{@code forHtml(v)}&lt;/td&gt;                    HTML 본문
 * &lt;div class={@code forHtmlAttribute(v)}&gt;         따옴표 없는 속성
 * &lt;script&gt;var s = '{@code forJavaScript(v)}';&lt;/script&gt;
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.10.25  실행환경팀     최초 생성 (5.1.0 기능 이식)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 4.3.1
 */
public final class EgovOutputEncoder {

	/** RFC 3986 unreserved 문자 (퍼센트 인코딩 제외) */
	private static final String URL_UNRESERVED = "-._~";

	private static final char[] HEX = "0123456789ABCDEF".toCharArray();

	/** JavaScript가 줄바꿈으로 해석하는 U+2028·U+2029 (소스 손상 방지를 위해 코드포인트로 선언) */
	private static final char JS_LINE_SEPARATOR = 0x2028;

	private static final char JS_PARAGRAPH_SEPARATOR = 0x2029;

	private EgovOutputEncoder() {
	}

	/**
	 * HTML 본문과 <b>따옴표로 감싼</b> 속성값에 넣을 값을 인코딩한다.
	 * {@code & < > " '} 다섯 문자만 문자 참조로 바꾼다(따옴표 없는 속성은 {@link #forHtmlAttribute(String)}).
	 *
	 * @param value 인코딩할 값({@code null} 허용)
	 * @return 인코딩된 문자열({@code null} 이면 빈 문자열)
	 */
	public static String forHtml(String value) {
		if (value == null || value.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder(value.length() + 16);
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			switch (ch) {
				case '&':
					sb.append("&amp;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '"':
					sb.append("&quot;");
					break;
				case '\'':
					sb.append("&#39;");
					break;
				default:
					sb.append(ch);
			}
		}
		return sb.toString();
	}

	/**
	 * <b>따옴표 없는</b> HTML 속성값에 넣을 값을 인코딩한다.
	 * ASCII 영숫자와 {@code U+0080} 이상을 제외한 ASCII는 16진 문자 참조로 바꾼다.
	 *
	 * @param value 인코딩할 값({@code null} 허용)
	 * @return 인코딩된 문자열({@code null} 이면 빈 문자열)
	 */
	public static String forHtmlAttribute(String value) {
		if (value == null || value.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder(value.length() + 32);
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if (isAsciiAlphaNumeric(ch) || ch >= 0x80) {
				sb.append(ch);
			} else {
				sb.append("&#x").append(Integer.toHexString(ch).toUpperCase()).append(';');
			}
		}
		return sb.toString();
	}

	/**
	 * XML 텍스트 노드·속성값에 넣을 값을 인코딩한다.
	 * XML 1.0이 허용하지 않는 제어문자는 제거한다(HTML에는 {@link #forHtml(String)} 사용).
	 *
	 * @param value 인코딩할 값({@code null} 허용)
	 * @return 인코딩된 문자열({@code null} 이면 빈 문자열)
	 */
	public static String forXml(String value) {
		if (value == null || value.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder(value.length() + 16);
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if (!isValidXmlChar(ch)) {
				continue;
			}
			switch (ch) {
				case '&':
					sb.append("&amp;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '"':
					sb.append("&quot;");
					break;
				case '\'':
					sb.append("&apos;");
					break;
				default:
					sb.append(ch);
			}
		}
		return sb.toString();
	}

	/**
	 * JavaScript 따옴표 문자열 리터럴 안에 넣을 값을 인코딩한다.
	 * 이벤트 핸들러 속성 안이라면 결과에 {@link #forHtml(String)}를 추가 적용한다.
	 *
	 * @param value 인코딩할 값({@code null} 허용)
	 * @return 인코딩된 문자열({@code null} 이면 빈 문자열)
	 */
	public static String forJavaScript(String value) {
		if (value == null || value.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder(value.length() + 16);
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			switch (ch) {
				case '\\':
					sb.append("\\\\");
					break;
				case '\'':
					sb.append("\\'");
					break;
				case '"':
					sb.append("\\\"");
					break;
				case '/':
					sb.append("\\/");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '<':
				case '>':
				case '&':
					appendUnicodeEscape(sb, ch);
					break;
				default:
					if (ch < 0x20 || ch == JS_LINE_SEPARATOR || ch == JS_PARAGRAPH_SEPARATOR) {
						appendUnicodeEscape(sb, ch);
					} else {
						sb.append(ch);
					}
			}
		}
		return sb.toString();
	}

	/**
	 * URL 구성요소(경로 조각·쿼리 파라미터 값) 하나에 넣을 값을 인코딩한다.
	 * RFC 3986 unreserved 문자 외에는 UTF-8 퍼센트 인코딩한다(공백은 {@code %20}).
	 *
	 * @param value 인코딩할 값({@code null} 허용)
	 * @return 인코딩된 문자열({@code null} 이면 빈 문자열)
	 */
	public static String forUrl(String value) {
		if (value == null || value.isEmpty()) {
			return "";
		}
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		StringBuilder sb = new StringBuilder(bytes.length + 32);
		for (byte b : bytes) {
			int ch = b & 0xFF;
			if (isAsciiAlphaNumeric((char) ch) || URL_UNRESERVED.indexOf(ch) >= 0) {
				sb.append((char) ch);
			} else {
				sb.append('%').append(HEX[ch >> 4]).append(HEX[ch & 0x0F]);
			}
		}
		return sb.toString();
	}

	private static boolean isAsciiAlphaNumeric(char ch) {
		return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9');
	}

	/**
	 * XML 1.0 허용 문자 여부를 반환한다(서로게이트 범위는 통과).
	 */
	private static boolean isValidXmlChar(char ch) {
		return ch == '\t' || ch == '\n' || ch == '\r' || (ch >= 0x20 && ch <= 0xFFFD);
	}

	private static void appendUnicodeEscape(StringBuilder sb, char ch) {
		sb.append("\\u");
		sb.append(HEX[(ch >> 12) & 0x0F]);
		sb.append(HEX[(ch >> 8) & 0x0F]);
		sb.append(HEX[(ch >> 4) & 0x0F]);
		sb.append(HEX[ch & 0x0F]);
	}
}
