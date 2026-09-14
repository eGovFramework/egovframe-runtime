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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovOutputEncoder} 의 <b>XSS 방어 완전성</b> 회귀 검증.
 *
 * <p>기능 테스트({@link EgovOutputEncoderTest})가 대표 사례를 다루고, 이 클래스는 두 가지를 맡는다.
 * 첫째, <b>BMP 65,536자 전수</b>에 대해 컨텍스트별 불변식(위험 문자가 원시 형태로 남지 않는다 ·
 * 이스케이프 형식이 유효하다 · 디코딩하면 원문으로 돌아온다)을 고정한다 — 빠진 문자가 하나라도
 * 생기면 실패한다. 둘째, 공격에 실제로 쓰이는 이탈 페이로드를 컨텍스트별로 모아 두고 새 우회
 * 기법이 알려지면 한 줄을 더한다.</p>
 *
 * <p>인코딩이 <b>해결하지 않는 것</b>(URL 스킴 · 템플릿 리터럴)도 계약으로 고정해, 호출부가
 * 인코딩만으로 충분하다고 오해하지 않도록 한다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.07  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
class EgovOutputEncoderSecurityTest {

	/** 역슬래시 — 소스 가독성을 위해 코드포인트로 선언한다. */
	private static final char BS = 0x5C;

	/** JavaScript 가 줄바꿈으로 해석하는 두 문자. 소스에 리터럴로 두지 않는다. */
	private static final char LINE_SEPARATOR = 0x2028;

	private static final char PARAGRAPH_SEPARATOR = 0x2029;

	private static final String NUL = String.valueOf((char) 0);

	private static final String SURROGATE_PAIR = new String(Character.toChars(0x1F600));

	private static final String LONE_SURROGATE = String.valueOf((char) 0xD83D);

	private static final String BOM = String.valueOf((char) 0xFEFF);

	// ------------------------------------------------------------ BMP 전수

	@Nested
	@DisplayName("BMP 전수 — 컨텍스트별 불변식과 왕복")
	class BmpSweep {

		@Test
		@DisplayName("forHtml — 원시 < > \" ' 가 남지 않고, 디코딩하면 65,536자 전부 원문이다")
		void forHtml_전수_왕복() {
			for (int c = 0; c <= 0xFFFF; c++) {
				String in = String.valueOf((char) c);
				String out = EgovOutputEncoder.forHtml(in);
				assertNoRaw(out, "<>\"'", c);
				assertEquals(in, decodeEntities(out), "왕복 실패 U+" + hex(c));
			}
		}

		@Test
		@DisplayName("forHtmlAttribute — 영숫자 외의 ASCII 는 참조 문법 문자(& # ;)뿐이고, 65,536자 전부 왕복한다")
		void forHtmlAttribute_전수_왕복() {
			for (int c = 0; c <= 0xFFFF; c++) {
				String in = String.valueOf((char) c);
				String out = EgovOutputEncoder.forHtmlAttribute(in);
				for (int k = 0; k < out.length(); k++) {
					char oc = out.charAt(k);
					if (oc < 0x80 && !isAlnum(oc) && oc != '&' && oc != '#' && oc != ';') {
						fail("따옴표 없는 속성에 구분자 후보가 남았다 U+" + hex(c) + " -> " + out);
					}
				}
				assertEquals(in, decodeEntities(out), "왕복 실패 U+" + hex(c));
			}
		}

		@Test
		@DisplayName("forJavaScript — 금지 문자가 원시 형태로 남지 않고, 이스케이프는 전부 유효하며, 65,536자 전부 왕복한다")
		void forJavaScript_전수_왕복() {
			for (int c = 0; c <= 0xFFFF; c++) {
				String in = String.valueOf((char) c);
				String out = EgovOutputEncoder.forJavaScript(in);
				assertNoRaw(out, "<>&\r\n" + LINE_SEPARATOR + PARAGRAPH_SEPARATOR, c);
				assertNoUnescapedQuote(out, c);
				for (int k = 0; k < out.length(); k++) {
					assertFalse(out.charAt(k) < 0x20, "제어문자가 원시 형태로 남았다 U+" + hex(c));
				}
				assertEquals(in, decodeJavaScript(out), "왕복 실패 U+" + hex(c));
			}
		}

		@Test
		@DisplayName("forUrl — 출력 알파벳은 unreserved 와 % 뿐이고, UTF-8 퍼센트 디코딩하면 원문이다")
		void forUrl_전수_왕복() {
			for (int c = 0; c <= 0xFFFF; c++) {
				if (Character.isSurrogate((char) c)) {
					continue; // 단독 서로게이트는 문자열로서 유효하지 않다 — 쌍은 아래에서 따로 본다
				}
				String in = String.valueOf((char) c);
				String out = EgovOutputEncoder.forUrl(in);
				for (int k = 0; k < out.length(); k++) {
					char oc = out.charAt(k);
					assertTrue(isAlnum(oc) || "-._~%".indexOf(oc) >= 0, "URL 구성요소 밖 문자 U+" + hex(c) + " -> " + out);
				}
				assertEquals(in, decodePercent(out), "왕복 실패 U+" + hex(c));
			}
			assertEquals("%F0%9F%98%80", EgovOutputEncoder.forUrl(SURROGATE_PAIR), "보충 평면 문자는 4바이트 UTF-8");
		}

		@Test
		@DisplayName("forXml — XML 1.0 이 금지한 제어문자와 U+FFFE·U+FFFF 만 사라지고 나머지는 왕복한다")
		void forXml_전수_왕복() {
			for (int c = 0; c <= 0xFFFF; c++) {
				String in = String.valueOf((char) c);
				String out = EgovOutputEncoder.forXml(in);
				boolean forbidden = (c < 0x20 && c != '\t' && c != '\n' && c != '\r') || c == 0xFFFE || c == 0xFFFF;
				if (forbidden) {
					assertEquals("", out, "금지 문자가 남았다 U+" + hex(c));
				} else {
					assertNoRaw(out, "<>\"'", c);
					assertEquals(in, decodeEntities(out), "왕복 실패 U+" + hex(c));
				}
			}
		}
	}

	// ------------------------------------------------------------ 이탈 페이로드

	/** 컨텍스트를 가리지 않고 던져 보는 페이로드 — 새 우회 기법이 알려지면 여기에 더한다. */
	private static final List<String> PAYLOADS = List.of(
			"</script>", "<!--", "-->", "]]>", "\"><script>alert(1)</script>", "' onmouseover='alert(1)",
			" onmouseover=alert(1)", "'-alert(1)-'", "\"-alert(1)-\"", BS + "'-alert(1)//", "</textarea><script>alert(1)</script>",
			LINE_SEPARATOR + "alert(1)", PARAGRAPH_SEPARATOR + "alert(1)", "\r\nalert(1)", "&#106;avascript:alert(1)",
			"%3Cscript%3E", "a&b=c&d", NUL + "<script>", "　<img src=x onerror=alert(1)>");

	@Nested
	@DisplayName("이탈 페이로드 배터리")
	class Breakouts {

		@Test
		@DisplayName("HTML 본문·따옴표 속성 — 어떤 페이로드에도 원시 < > \" ' & 가 남지 않는다")
		void html_컨텍스트() {
			for (String p : PAYLOADS) {
				String out = EgovOutputEncoder.forHtml(p);
				assertNoRaw(out, "<>\"'", -1);
				assertTrue(decodeEntities(out).equals(p), "왕복 실패: " + p);
			}
		}

		@Test
		@DisplayName("따옴표 없는 속성 — 공백·등호·백틱·꺾쇠 등 구분자가 하나도 남지 않는다")
		void 따옴표없는_속성_컨텍스트() {
			for (String p : PAYLOADS) {
				String out = EgovOutputEncoder.forHtmlAttribute(p);
				for (int k = 0; k < out.length(); k++) {
					char oc = out.charAt(k);
					assertFalse(oc < 0x80 && !isAlnum(oc) && oc != '&' && oc != '#' && oc != ';', "구분자 잔존: " + p + " -> " + out);
				}
			}
		}

		@Test
		@DisplayName("스크립트 리터럴 — 블록 종료(</script> ]]>)와 리터럴 이탈(따옴표·역슬래시·줄바꿈)이 모두 불가능하다")
		void 스크립트_컨텍스트() {
			for (String p : PAYLOADS) {
				String out = EgovOutputEncoder.forJavaScript(p);
				assertFalse(out.contains("</"), "스크립트 블록 종료 가능: " + out);
				assertFalse(out.contains("]]>"), "CDATA 종료 가능: " + out);
				assertNoRaw(out, "<>&\r\n" + LINE_SEPARATOR + PARAGRAPH_SEPARATOR, -1);
				assertNoUnescapedQuote(out, -1);
				assertEquals(p, decodeJavaScript(out), "왕복 실패: " + p);
			}
			assertEquals(BS + "u003C" + BS + "/script" + BS + "u003E", EgovOutputEncoder.forJavaScript("</script>"));
		}

		@Test
		@DisplayName("URL 구성요소 — 구분자(/ ? & = #)와 스킴 구분자(:)가 전부 퍼센트 인코딩된다")
		void URL_컨텍스트() {
			for (String p : PAYLOADS) {
				String out = EgovOutputEncoder.forUrl(p);
				for (char forbidden : "/?&=#:<>\"' ".toCharArray()) {
					assertTrue(out.indexOf(forbidden) < 0, "URL 구분자 잔존 [" + forbidden + "]: " + out);
				}
				assertEquals(p, decodePercent(out), "왕복 실패: " + p);
			}
			assertEquals("javascript%3Aalert%281%29", EgovOutputEncoder.forUrl("javascript:alert(1)"));
			assertEquals("%253Cscript%253E", EgovOutputEncoder.forUrl("%3Cscript%3E"), "이미 인코딩된 % 도 다시 인코딩 — 이중 디코딩 우회 없음");
		}

		@Test
		@DisplayName("이벤트 핸들러 속성 — JS 인코딩 후 HTML 인코딩 순서면 브라우저가 디코딩해도 리터럴 안에 머문다")
		void 이벤트_핸들러_중첩_순서() {
			String value = "');alert(1);//";
			String attr = EgovOutputEncoder.forHtml(EgovOutputEncoder.forJavaScript(value));
			assertNoRaw(attr, "<>\"'", -1);
			// 브라우저는 속성값의 문자 참조를 먼저 푼 뒤 스크립트로 넘긴다
			String seenByScript = decodeEntities(attr);
			assertEquals(BS + "');alert(1);" + BS + "/" + BS + "/", seenByScript);
			assertEquals(value, decodeJavaScript(seenByScript), "스크립트 엔진이 리터럴 하나로 읽는다");
		}
	}

	// ------------------------------------------------------------ 계약 고정 — 인코딩이 해결하지 않는 것

	@Nested
	@DisplayName("계약 고정 — 인코딩이 해결하지 않는 것")
	class ContractLimits {

		@Test
		@DisplayName("인코딩은 URL 스킴을 바꾸지 않는다 — href/src 에 값 전체를 넣으려면 스킴 검사가 따로 필요하다")
		void URL_스킴은_인코딩_대상이_아니다() {
			String url = "javascript:alert(1)";
			assertEquals(url, EgovOutputEncoder.forHtml(url), "본문·따옴표 속성 인코딩은 문자를 바꾸지 않는다");
			assertEquals("javascript&#x3A;alert&#x28;1&#x29;", EgovOutputEncoder.forHtmlAttribute(url),
					"문자 참조는 브라우저가 속성값 단계에서 도로 풀어 스킴이 살아난다");
			assertEquals("javascript%3Aalert%281%29", EgovOutputEncoder.forUrl(url),
					"구성요소 인코딩은 콜론을 퍼센트로 바꿔 스킴이 성립하지 않는다 — 값 하나로 쓸 때만 해당");
		}

		@Test
		@DisplayName("forJavaScript 는 템플릿 리터럴(백틱) 문법을 다루지 않는다 — 따옴표 리터럴 안에서만 쓴다")
		void 템플릿_리터럴은_대상이_아니다() {
			assertEquals("${alert(1)}", EgovOutputEncoder.forJavaScript("${alert(1)}"));
			assertEquals("`+alert(1)+`", EgovOutputEncoder.forJavaScript("`+alert(1)+`"));
		}
	}

	// ------------------------------------------------------------ 견고성

	@Nested
	@DisplayName("견고성")
	class Robustness {

		@Test
		@DisplayName("널 바이트·단독 서로게이트·BOM·보충 평면 문자에 예외가 없고 쌍은 보존된다")
		void 특수_입력() {
			for (UnaryOperator<String> f : encoders()) {
				for (String in : List.of(NUL, LONE_SURROGATE, BOM, SURROGATE_PAIR, NUL + SURROGATE_PAIR + LONE_SURROGATE)) {
					f.apply(in); // 예외가 나면 테스트 실패
				}
			}
			assertTrue(EgovOutputEncoder.forHtml(SURROGATE_PAIR).contains(SURROGATE_PAIR));
			assertTrue(EgovOutputEncoder.forJavaScript(SURROGATE_PAIR).contains(SURROGATE_PAIR));
			assertEquals("%3F", EgovOutputEncoder.forUrl(LONE_SURROGATE), "단독 서로게이트는 UTF-8 로 표현할 수 없어 ? 로 대체된다");
		}

		@Test
		@DisplayName("1 MB 입력이 선형 시간에 끝난다 — 이차 복잡도로 퇴행하면 실패한다")
		void 대용량_선형() {
			StringBuilder sb = new StringBuilder(1_100_000);
			while (sb.length() < 1_000_000) {
				sb.append("<script>alert('x&y')</script> 한글 ").append(LINE_SEPARATOR).append(" /path?q=1 ");
			}
			String big = sb.toString();
			for (UnaryOperator<String> f : encoders()) {
				long start = System.nanoTime();
				String out = f.apply(big);
				long millis = (System.nanoTime() - start) / 1_000_000;
				assertTrue(out.length() >= big.length());
				assertTrue(millis < 10_000, "1 MB 인코딩에 " + millis + " ms — 선형이면 수십 ms 다");
			}
		}
	}

	// ------------------------------------------------------------ helpers

	private static List<UnaryOperator<String>> encoders() {
		return List.of(EgovOutputEncoder::forHtml, EgovOutputEncoder::forHtmlAttribute, EgovOutputEncoder::forJavaScript,
				EgovOutputEncoder::forUrl, EgovOutputEncoder::forXml);
	}

	private static boolean isAlnum(char ch) {
		return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9');
	}

	private static String hex(int c) {
		return Integer.toHexString(c).toUpperCase();
	}

	private static void assertNoRaw(String out, String forbidden, int codePoint) {
		for (int k = 0; k < out.length(); k++) {
			if (forbidden.indexOf(out.charAt(k)) >= 0) {
				fail("원시 위험 문자 [U+" + hex(out.charAt(k)) + "] 잔존" + (codePoint >= 0 ? " (입력 U+" + hex(codePoint) + ")" : "") + ": " + out);
			}
		}
	}

	/** 역슬래시 뒤에 오지 않는 따옴표가 있으면 리터럴이 닫힌다. */
	private static void assertNoUnescapedQuote(String out, int codePoint) {
		for (int k = 0; k < out.length(); k++) {
			char ch = out.charAt(k);
			if (ch == BS) {
				k++;
			} else if (ch == '\'' || ch == '"') {
				fail("이스케이프되지 않은 따옴표" + (codePoint >= 0 ? " (입력 U+" + hex(codePoint) + ")" : "") + ": " + out);
			}
		}
	}

	/** 인코더가 낼 수 있는 문자 참조만 받아들이는 엄격한 디코더 — 다른 형태가 나오면 실패한다. */
	private static String decodeEntities(String s) {
		StringBuilder out = new StringBuilder();
		int i = 0;
		while (i < s.length()) {
			char c = s.charAt(i);
			if (c != '&') {
				out.append(c);
				i++;
				continue;
			}
			int semi = s.indexOf(';', i);
			assertTrue(semi > i, "닫히지 않은 문자 참조: " + s);
			String entity = s.substring(i + 1, semi);
			switch (entity) {
				case "amp": out.append('&'); break;
				case "lt": out.append('<'); break;
				case "gt": out.append('>'); break;
				case "quot": out.append('"'); break;
				case "apos": out.append('\''); break;
				case "#39": out.append('\''); break;
				default:
					assertTrue(entity.startsWith("#x"), "알 수 없는 문자 참조: &" + entity + ";");
					out.append((char) Integer.parseInt(entity.substring(2), 16));
			}
			i = semi + 1;
		}
		return out.toString();
	}

	/** 인코더가 낼 수 있는 이스케이프만 받아들이는 엄격한 JavaScript 문자열 디코더. */
	private static String decodeJavaScript(String s) {
		StringBuilder out = new StringBuilder();
		int i = 0;
		while (i < s.length()) {
			char c = s.charAt(i);
			if (c != BS) {
				out.append(c);
				i++;
				continue;
			}
			assertTrue(i + 1 < s.length(), "끝에 홀로 남은 역슬래시: " + s);
			char n = s.charAt(i + 1);
			switch (n) {
				case 'u':
					out.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
					i += 6;
					continue;
				case 'b': out.append('\b'); break;
				case 'f': out.append('\f'); break;
				case 'n': out.append('\n'); break;
				case 'r': out.append('\r'); break;
				case 't': out.append('\t'); break;
				case '\'': case '"': case '/': out.append(n); break;
				default:
					assertEquals(BS, n, "알 수 없는 이스케이프: " + BS + n);
					out.append(BS);
			}
			i += 2;
		}
		return out.toString();
	}

	private static String decodePercent(String s) {
		byte[] bytes = new byte[s.length()];
		int n = 0;
		int i = 0;
		while (i < s.length()) {
			char c = s.charAt(i);
			if (c == '%') {
				bytes[n++] = (byte) Integer.parseInt(s.substring(i + 1, i + 3), 16);
				i += 3;
			} else {
				bytes[n++] = (byte) c;
				i++;
			}
		}
		return new String(bytes, 0, n, StandardCharsets.UTF_8);
	}
}
