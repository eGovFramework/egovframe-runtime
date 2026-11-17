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
 * 값이 출력되는 <b>컨텍스트별</b> 인코딩을 제공하는 유틸 클래스.
 *
 * <p><b>NOTE:</b> XSS 방어 = 문자 삭제가 아닌 <b>출력 자리 문법에 맞는 인코딩</b>
 * (HTML 본문·속성·JavaScript·URL 별 메서드 제공)</p>
 *
 * <pre>
 * &lt;td&gt;{@code forHtml(v)}&lt;/td&gt;                    HTML 본문
 * &lt;input value="{@code forHtml(v)}"&gt;              따옴표로 감싼 속성
 * &lt;div class={@code forHtmlAttribute(v)}&gt;         따옴표 없는 속성
 * &lt;script&gt;var s = '{@code forJavaScript(v)}';&lt;/script&gt;
 * &lt;a href="/view?id={@code forUrl(v)}"&gt;
 * </pre>
 *
 * <p><b>인코딩은 출력 시점에 한 번만</b> (입력 시 인코딩하면 DB 에 {@code &amp;lt;} 가 남아 비 HTML 경로에 노출,
 * JSP {@code <c:out>}(기본 {@code escapeXml="true"})과 겹치면 이중 인코딩)</p>
 *
 * <p><b>정제(sanitize)와 별개</b> — 태그를 살리는 리치 텍스트는 화이트리스트 정제 대상
 * ({@code ptl.mvc} 의 {@code EgovHtmlSanitizer})</p>
 *
 * <p><b>인코딩이 해결하지 않는 것</b> — ① URL 속성({@code href}·{@code src})에 값 <b>전체</b>를 넣을 때
 * {@code javascript:} 같은 스킴은 어떤 인코딩으로도 바뀌지 않는다(문자 참조는 브라우저가 속성값
 * 단계에서 도로 푼다). 스킴 화이트리스트(http·https·상대 경로) 검사를 따로 한 뒤 {@link #forHtml(String)}
 * 를 쓴다. ② CSS({@code style} 속성·스타일시트) 컨텍스트는 제공하지 않는다 — 사용자 값을 CSS 에 넣지
 * 않는다. ③ {@link #forJavaScript(String)} 는 따옴표 리터럴 전용이다. 백틱 템플릿 리터럴의
 * <code>${...}</code> 와 JSON(<code>\'</code> 는 JSON 이스케이프가 아니다)은 다루지 않는다 —
 * JSON 은 직렬화기에 맡긴다.</p>
 *
 * <p><b>계약</b> — 전 메서드 {@code null} 은 빈 문자열, 예외 없음. 공백만인 문자열은 <b>그대로 보존</b></p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (공통컴포넌트 EgovWebUtil 의 컨텍스트별 인코딩
 *                            개념만 차용하고 구현은 재작성 — 코드 이식 아님. 원본
 *                            clearXSSMinimum/Maximum/clearXSS 3종은 마침표를 &amp;#46; 로
 *                            치환해 한글 문장·소수점·확장자를 훼손하고 입력 정화와 출력
 *                            인코딩에 혼용되어 폐기, escapeXml/escapeJavaScript 는 공백
 *                            문자열 소실과 &amp;apos; 의 HTML4 미지원 문제를 바로잡아 재구성)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovOutputEncoder {

	/** RFC 3986 unreserved 문자 — 퍼센트 인코딩하지 않는다. */
	private static final String URL_UNRESERVED = "-._~";

	private static final char[] HEX = "0123456789ABCDEF".toCharArray();

	/**
	 * JavaScript 줄바꿈 문자 U+2028 LINE SEPARATOR · U+2029 PARAGRAPH SEPARATOR
	 * (리터럴은 편집기·도구가 줄바꿈으로 취급해 소스 손상 우려, 코드포인트로 선언)
	 */
	private static final char JS_LINE_SEPARATOR = 0x2028;

	private static final char JS_PARAGRAPH_SEPARATOR = 0x2029;

	private EgovOutputEncoder() {
	}

	/**
	 * HTML 본문과 <b>따옴표로 감싼</b> 속성값에 넣을 값을 인코딩한다.
	 *
	 * <p>{@code & < > " '} 다섯 문자만 문자 참조로 변환, 나머지(한글·마침표·쉼표)는 <b>유지</b>.
	 * 작은따옴표는 {@code &amp;#39;} (HTML4 에 없는 {@code &amp;apos;} 대신, HTML·XHTML 공용)</p>
	 *
	 * <p><b>따옴표 없는 속성값에는 부족</b> ({@code <div class=값>} 은 공백으로 속성 주입 가능)
	 * : {@link #forHtmlAttribute(String)} 사용 또는 따옴표로 감쌈</p>
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
	 *
	 * <p>ASCII 영숫자와 {@code U+0080} 이상(한글 등)만 유지, <b>그 밖의 ASCII 는 전부</b>
	 * 16진 문자 참조({@code &amp;#xHH;}) (공백·{@code =}·백틱 등 속성 구분자 무력화)</p>
	 *
	 * <p>한글은 그대로 (UTF-8 문서 전제). <b>따옴표로 감쌀 수 있다면</b> {@link #forHtml(String)} 로 충분</p>
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
	 * XML 문서의 텍스트 노드·속성값에 넣을 값을 인코딩한다.
	 *
	 * <p>{@code & < > " '} 를 XML 사전 정의 실체({@code &amp;apos;} 포함)로 바꾸고,
	 * <b>XML 1.0 비허용 제어문자 제거</b>(파서의 문서 거부 방지, 탭·개행·CR 은 보존).</p>
	 *
	 * <p>HTML 용 아님 ({@code &amp;apos;} 는 HTML4 미정의) : HTML 은 {@link #forHtml(String)}</p>
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
	 * JavaScript 문자열 리터럴 안에 넣을 값을 인코딩한다.
	 *
	 * <p>따옴표·역슬래시 이스케이프, {@code < > &}·{@code /} 처리({@code </script>} 블록 탈출 방지),
	 * 제어문자·{@code U+2028}·{@code U+2029} 는 유니코드 이스케이프.</p>
	 *
	 * <p><b>따옴표 리터럴 안에서만 사용</b>({@code var s = '값';}, 코드 조립 금지).
	 * 이벤트 핸들러 속성({@code onclick="..."})이면 결과에 {@link #forHtml(String)} 추가 적용</p>
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
	 * URL 의 한 구성요소(경로 조각·쿼리 파라미터 값)에 넣을 값을 인코딩한다.
	 *
	 * <p>RFC 3986 unreserved 문자({@code A-Z a-z 0-9 - . _ ~}) 외 UTF-8 바이트 단위 퍼센트 인코딩
	 * ({@code /}·{@code ?}·{@code &}·{@code =} 포함, <b>URL 전체가 아닌 값 하나</b>에만 적용).</p>
	 *
	 * <p>{@code java.net.URLEncoder}(폼 전송용, 공백 → {@code +})와 다름</p>
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
	 * XML 1.0 이 허용하는 문자인지 판정한다(제어문자 대부분 금지, 탭·개행·CR 은 허용).
	 *
	 * <p>서로게이트 범위(U+D800~U+DFFF)는 <b>통과</b> (이모지 등 보충 평면 문자 보존)</p>
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
