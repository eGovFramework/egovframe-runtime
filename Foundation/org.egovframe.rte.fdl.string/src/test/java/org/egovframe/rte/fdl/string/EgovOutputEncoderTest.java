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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovOutputEncoder} 단위 테스트.
 *
 * <p>일반 동작 검증과 함께 <b>공통컴포넌트 원본(EgovWebUtil)의 결함을 회귀로 고정</b>한다 —
 * 마침표 훼손, 공백 문자열 소실, {@code &apos;} 의 HTML4 미지원, 따옴표 없는 속성 무방비.
 * 원본 구현이라면 실패할 단언들이다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
class EgovOutputEncoderTest {

	/**
	 * 제어문자·특수문자는 <b>소스에 리터럴로 두지 않고</b> 코드포인트로 선언한다 —
	 * 편집기·도구가 실제 줄바꿈으로 취급하거나 파일이 바이너리로 인식되는 것을 막는다.
	 */
	private static final String JS_LINE_SEPARATOR = String.valueOf((char) 0x2028);

	/** 수직 탭(U+000B) — XML 1.0 이 금지하는 제어문자다. */
	private static final String VERTICAL_TAB = String.valueOf((char) 0x0B);

	/** SOH(U+0001) — 제어문자 이스케이프 검증용. */
	private static final String CONTROL_SOH = String.valueOf((char) 0x01);

	@Nested
	@DisplayName("원본 결함 회귀 — 공통컴포넌트 EgovWebUtil 이라면 실패한다")
	class LegacyDefectRegression {

		@Test
		@DisplayName("마침표를 훼손하지 않는다 (원본 clearXSSMinimum 은 &#46; 로 치환)")
		void 마침표_보존() {
			assertEquals("3.14", EgovOutputEncoder.forHtml("3.14"));
			assertEquals("report.v2.txt", EgovOutputEncoder.forHtml("report.v2.txt"));
			assertEquals("오늘은 맑음. 내일은 비.", EgovOutputEncoder.forHtml("오늘은 맑음. 내일은 비."));
		}

		@Test
		@DisplayName("공백만으로 이루어진 문자열이 사라지지 않는다 (원본은 trim 판정으로 \"\" 반환)")
		void 공백_문자열_보존() {
			assertEquals("   ", EgovOutputEncoder.forHtml("   "));
			assertEquals("\t", EgovOutputEncoder.forXml("\t"));
			assertEquals("\\t", EgovOutputEncoder.forJavaScript("\t"));
		}

		@Test
		@DisplayName("HTML 은 &#39; 를 쓴다 — &apos; 는 HTML4 에 없는 실체다")
		void html_작은따옴표는_숫자참조() {
			assertEquals("&#39;", EgovOutputEncoder.forHtml("'"));
			assertFalse(EgovOutputEncoder.forHtml("'").contains("&apos;"));
		}

		@Test
		@DisplayName("XML 은 &apos; 를 쓴다 — 컨텍스트가 다르면 결과도 달라야 한다")
		void xml_작은따옴표는_사전정의실체() {
			assertEquals("&apos;", EgovOutputEncoder.forXml("'"));
		}

		@Test
		@DisplayName("따옴표 없는 속성값의 주입을 막는다 (원본·Spring HtmlUtils 는 통과시킨다)")
		void 따옴표없는_속성_주입_차단() {
			String injected = "a onmouseover=alert(1)";
			String encoded = EgovOutputEncoder.forHtmlAttribute(injected);

			assertFalse(encoded.contains(" "), "공백이 남으면 새 속성을 주입할 수 있다");
			assertFalse(encoded.contains("="), "등호가 남으면 속성값을 지정할 수 있다");
			assertTrue(encoded.startsWith("a&#x20;onmouseover&#x3D;alert"));
		}

		@Test
		@DisplayName("백틱을 인코딩한다 — 일부 브라우저가 속성 구분자로 해석한다")
		void 백틱_인코딩() {
			assertEquals("a&#x60;b", EgovOutputEncoder.forHtmlAttribute("a`b"));
		}

		@Test
		@DisplayName("null 에 예외를 던지지 않는다 (Spring 은 클래스마다 다른 예외를 던진다)")
		void null_은_빈문자열() {
			assertDoesNotThrow(() -> EgovOutputEncoder.forHtml(null));
			assertEquals("", EgovOutputEncoder.forHtml(null));
			assertEquals("", EgovOutputEncoder.forHtmlAttribute(null));
			assertEquals("", EgovOutputEncoder.forXml(null));
			assertEquals("", EgovOutputEncoder.forJavaScript(null));
			assertEquals("", EgovOutputEncoder.forUrl(null));
		}
	}

	@Nested
	@DisplayName("forHtml — HTML 본문·따옴표로 감싼 속성")
	class ForHtml {

		@Test
		@DisplayName("다섯 문자만 문자 참조로 바꾼다")
		void 기본_이스케이프() {
			assertEquals("&amp;&lt;&gt;&quot;&#39;", EgovOutputEncoder.forHtml("&<>\"'"));
		}

		@Test
		@DisplayName("스크립트 태그가 무력화된다")
		void 스크립트_무력화() {
			assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;",
					EgovOutputEncoder.forHtml("<script>alert(1)</script>"));
		}

		@Test
		@DisplayName("한글과 일반 문장은 그대로 둔다")
		void 한글_무훼손() {
			String text = "행정안전부 전자정부표준프레임워크 5.1, 실행환경!";
			assertEquals(text, EgovOutputEncoder.forHtml(text));
		}

		@Test
		@DisplayName("빈 문자열은 빈 문자열")
		void 빈문자열() {
			assertEquals("", EgovOutputEncoder.forHtml(""));
		}

		@Test
		@DisplayName("이미 인코딩된 값을 다시 넣으면 이중 인코딩된다 — 한 번만 적용해야 한다")
		void 이중_인코딩_확인() {
			String once = EgovOutputEncoder.forHtml("<b>");
			assertEquals("&lt;b&gt;", once);
			assertEquals("&amp;lt;b&amp;gt;", EgovOutputEncoder.forHtml(once));
		}
	}

	@Nested
	@DisplayName("forHtmlAttribute — 따옴표 없는 속성")
	class ForHtmlAttribute {

		@Test
		@DisplayName("영숫자는 그대로 둔다")
		void 영숫자_보존() {
			assertEquals("abcXYZ012", EgovOutputEncoder.forHtmlAttribute("abcXYZ012"));
		}

		@Test
		@DisplayName("한글은 문자 참조로 바꾸지 않는다 — 출력이 불필요하게 길어지지 않는다")
		void 한글_보존() {
			assertEquals("게시판", EgovOutputEncoder.forHtmlAttribute("게시판"));
		}

		@Test
		@DisplayName("영숫자가 아닌 ASCII 는 전부 16진 참조가 된다")
		void ascii_기호_전부_인코딩() {
			assertEquals("&#x2D;&#x2E;&#x5F;", EgovOutputEncoder.forHtmlAttribute("-._"));
		}
	}

	@Nested
	@DisplayName("forXml — XML 텍스트·속성")
	class ForXml {

		@Test
		@DisplayName("사전 정의 실체 다섯 개")
		void 기본_이스케이프() {
			assertEquals("&amp;&lt;&gt;&quot;&apos;", EgovOutputEncoder.forXml("&<>\"'"));
		}

		@Test
		@DisplayName("XML 1.0 이 금지한 제어문자를 제거한다 — 파서가 문서를 거부하지 않도록")
		void 금지_제어문자_제거() {
			assertEquals("AB", EgovOutputEncoder.forXml("A" + VERTICAL_TAB + "B"));
		}

		@Test
		@DisplayName("탭·개행·캐리지 리턴은 유효하므로 보존한다")
		void 허용_공백문자_보존() {
			assertEquals("A\t\n\rB", EgovOutputEncoder.forXml("A\t\n\rB"));
		}

		@Test
		@DisplayName("보충 평면 문자(서로게이트 쌍)가 사라지지 않는다")
		void 보충평면_문자_보존() {
			String emoji = "😀";
			assertEquals("A" + emoji + "B", EgovOutputEncoder.forXml("A" + emoji + "B"));
		}
	}

	@Nested
	@DisplayName("forJavaScript — 스크립트 문자열 리터럴")
	class ForJavaScript {

		@Test
		@DisplayName("따옴표와 역슬래시를 이스케이프한다")
		void 따옴표_역슬래시() {
			assertEquals("\\'\\\"\\\\", EgovOutputEncoder.forJavaScript("'\"\\"));
		}

		@Test
		@DisplayName("문자열 안에서 </script> 로 블록을 끊을 수 없다")
		void 스크립트_종료_차단() {
			String encoded = EgovOutputEncoder.forJavaScript("</script>");
			assertFalse(encoded.contains("</"), "슬래시가 이스케이프되어야 한다");
			assertFalse(encoded.contains("<"), "꺾쇠가 남으면 안 된다");
			assertEquals("\\u003C\\/script\\u003E", encoded);
		}

		@Test
		@DisplayName("JavaScript 가 줄바꿈으로 해석하는 U+2028 을 이스케이프한다")
		void 줄종결자_이스케이프() {
			assertEquals("\\u2028", EgovOutputEncoder.forJavaScript(JS_LINE_SEPARATOR));
		}

		@Test
		@DisplayName("제어문자는 유니코드 이스케이프로 바꾼다")
		void 제어문자_이스케이프() {
			String expected = String.valueOf((char) 0x5C) + "u0001";
			assertEquals(expected, EgovOutputEncoder.forJavaScript(CONTROL_SOH));
		}

		@Test
		@DisplayName("한글은 그대로 둔다")
		void 한글_보존() {
			assertEquals("안녕하세요", EgovOutputEncoder.forJavaScript("안녕하세요"));
		}
	}

	@Nested
	@DisplayName("forUrl — URL 구성요소")
	class ForUrl {

		@Test
		@DisplayName("unreserved 문자는 그대로 둔다")
		void unreserved_보존() {
			assertEquals("aZ0-._~", EgovOutputEncoder.forUrl("aZ0-._~"));
		}

		@Test
		@DisplayName("한글은 UTF-8 퍼센트 인코딩")
		void 한글_퍼센트_인코딩() {
			assertEquals("%EA%B0%80", EgovOutputEncoder.forUrl("가"));
		}

		@Test
		@DisplayName("공백은 %20 이다 — URLEncoder 의 + 와 다르다")
		void 공백은_퍼센트20() {
			assertEquals("a%20b", EgovOutputEncoder.forUrl("a b"));
		}

		@Test
		@DisplayName("구분자를 인코딩해 파라미터 주입을 막는다")
		void 구분자_인코딩() {
			assertEquals("a%26b%3Dc", EgovOutputEncoder.forUrl("a&b=c"));
			assertEquals("..%2F..%2Fetc", EgovOutputEncoder.forUrl("../../etc"));
		}
	}
}
