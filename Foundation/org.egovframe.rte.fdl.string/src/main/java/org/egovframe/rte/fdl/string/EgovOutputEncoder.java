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
 * <p><b>NOTE:</b> XSS 방어는 "위험한 문자를 지우는" 것이 아니라 <b>값이 놓이는 자리의 문법에 맞게
 * 인코딩</b>하는 것이다. 같은 문자라도 HTML 본문·속성·JavaScript·URL 에서 위험한 지점이 다르므로
 * 하나의 함수로 모두 처리할 수 없다. 이 클래스는 자리마다 다른 메서드를 제공한다.</p>
 *
 * <pre>
 * &lt;td&gt;{@code forHtml(v)}&lt;/td&gt;                    HTML 본문
 * &lt;input value="{@code forHtml(v)}"&gt;              따옴표로 감싼 속성
 * &lt;div class={@code forHtmlAttribute(v)}&gt;         따옴표 없는 속성
 * &lt;script&gt;var s = '{@code forJavaScript(v)}';&lt;/script&gt;
 * &lt;a href="/view?id={@code forUrl(v)}"&gt;
 * </pre>
 *
 * <p><b>인코딩은 출력 시점에 한다.</b> 입력 시점에 인코딩해 저장하면 DB 에 {@code &amp;lt;} 가
 * 남아 엑셀·API 응답·문자메시지 등 HTML 이 아닌 경로에서 그대로 노출된다. 또한 이미 인코딩한 값을
 * JSP {@code <c:out>}(기본 {@code escapeXml="true"})으로 다시 내면 이중 인코딩이 되므로
 * <b>한 번만</b> 적용한다.</p>
 *
 * <p><b>정제(sanitize)와 다르다.</b> 사용자가 입력한 태그를 <b>살려서</b> 보여줘야 하는
 * 리치 텍스트 본문은 인코딩이 아니라 화이트리스트 정제 대상이다
 * ({@code ptl.mvc} 의 {@code EgovHtmlSanitizer}).</p>
 *
 * <p><b>인코딩이 해결하지 않는 것</b> — ① URL 속성({@code href}·{@code src})에 값 <b>전체</b>를 넣을 때
 * {@code javascript:} 같은 스킴은 어떤 인코딩으로도 바뀌지 않는다(문자 참조는 브라우저가 속성값
 * 단계에서 도로 푼다). 스킴 화이트리스트(http·https·상대 경로) 검사를 따로 한 뒤 {@link #forHtml(String)}
 * 를 쓴다. ② CSS({@code style} 속성·스타일시트) 컨텍스트는 제공하지 않는다 — 사용자 값을 CSS 에 넣지
 * 않는다. ③ {@link #forJavaScript(String)} 는 따옴표 리터럴 전용이다. 백틱 템플릿 리터럴의
 * <code>${...}</code> 와 JSON(<code>\'</code> 는 JSON 이스케이프가 아니다)은 다루지 않는다 —
 * JSON 은 직렬화기에 맡긴다.</p>
 *
 * <p><b>계약</b> — 전 메서드가 {@code null} 을 빈 문자열로 돌려주며 예외를 던지지 않는다.
 * 공백만으로 이루어진 문자열은 <b>그대로 보존</b>한다(들여쓰기·정렬이 사라지지 않는다).</p>
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
	 * JavaScript 가 줄바꿈으로 해석하는 두 문자(U+2028 LINE SEPARATOR · U+2029 PARAGRAPH
	 * SEPARATOR). 소스에 리터럴로 두지 않고 코드포인트로 선언한다 — 편집기·도구에 따라
	 * 실제 줄바꿈으로 취급되어 소스가 손상될 수 있기 때문이다.
	 */
	private static final char JS_LINE_SEPARATOR = 0x2028;

	private static final char JS_PARAGRAPH_SEPARATOR = 0x2029;

	private EgovOutputEncoder() {
	}

	/**
	 * HTML 본문과 <b>따옴표로 감싼</b> 속성값에 넣을 값을 인코딩한다.
	 *
	 * <p>{@code & < > " '} 다섯 문자만 문자 참조로 바꾸고 나머지는 <b>그대로 둔다</b> —
	 * 한글·마침표·쉼표는 훼손되지 않는다. 작은따옴표는 HTML4 에 존재하지 않는
	 * {@code &amp;apos;} 대신 숫자 참조 {@code &amp;#39;} 를 쓰므로 HTML·XHTML 양쪽에서 안전하다.</p>
	 *
	 * <p><b>따옴표 없는 속성값에는 충분하지 않다.</b> {@code <div class=값>} 처럼 쓰는 자리는
	 * 공백 하나로 새 속성을 주입할 수 있으므로 {@link #forHtmlAttribute(String)} 를 쓰거나
	 * 속성값을 따옴표로 감싼다.</p>
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
	 * <p>ASCII 영숫자와 {@code U+0080} 이상(한글 등)만 남기고 <b>그 밖의 ASCII 는 전부</b>
	 * 16진 문자 참조({@code &amp;#xHH;})로 바꾼다. 공백·{@code =}·백틱처럼 속성 구분자로
	 * 쓰이는 문자가 모두 무력화되므로 {@code <div class=값>} 형태에서도 속성 주입이 되지 않는다.</p>
	 *
	 * <p>한글을 문자 참조로 바꾸지 않으므로 출력이 길어지지 않는다(UTF-8 문서 전제).
	 * <b>속성을 따옴표로 감쌀 수 있다면</b> {@link #forHtml(String)} 로 충분하며 출력도 짧다.</p>
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
	 * <b>XML 1.0 이 허용하지 않는 제어문자를 제거</b>한다(파서가 문서 전체를 거부하는 것을 막는다).
	 * 탭·개행·캐리지 리턴은 유효하므로 보존한다.</p>
	 *
	 * <p>결과를 HTML 에 그대로 쓰면 안 된다 — {@code &amp;apos;} 는 HTML4 에 정의되지 않은
	 * 실체다. HTML 은 {@link #forHtml(String)} 를 쓴다.</p>
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
	 * <p>따옴표·역슬래시를 이스케이프하고, {@code < > &} 와 {@code /} 를 함께 처리해
	 * 문자열 안에서 {@code </script>} 로 스크립트 블록을 끊는 공격을 막는다. 제어문자와
	 * JavaScript 가 줄바꿈으로 해석하는 {@code U+2028}·{@code U+2029} 도 유니코드
	 * 이스케이프로 바꾼다.</p>
	 *
	 * <p><b>반드시 따옴표 안에서 쓴다.</b> {@code var s = '값';} 처럼 리터럴 자리에만
	 * 넣어야 하며, 코드 조각을 조립하는 용도로 쓰면 안 된다. 이벤트 핸들러 속성
	 * ({@code onclick="..."})처럼 HTML 속성 안의 스크립트라면 이 결과에
	 * {@link #forHtml(String)} 를 한 번 더 적용해야 한다.</p>
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
	 * <p>RFC 3986 의 unreserved 문자({@code A-Z a-z 0-9 - . _ ~})만 남기고 나머지는
	 * UTF-8 바이트 단위 퍼센트 인코딩한다. {@code /}·{@code ?}·{@code &}·{@code =} 도
	 * 인코딩 대상이므로 <b>URL 전체가 아니라 값 하나</b>에만 적용한다.</p>
	 *
	 * <p>{@code java.net.URLEncoder} 와 다르다 — 그쪽은 폼 전송용(application/x-www-form-urlencoded)
	 * 이라 공백을 {@code +} 로 바꾸며, 경로 조각에 쓰면 잘못된 결과가 된다.</p>
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
	 * <p>서로게이트 범위(U+D800~U+DFFF)는 <b>통과시킨다</b> — 이모지 등 보충 평면 문자가
	 * 서로게이트 쌍으로 표현되므로, 여기서 걸러내면 정상 문자가 사라진다.</p>
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
