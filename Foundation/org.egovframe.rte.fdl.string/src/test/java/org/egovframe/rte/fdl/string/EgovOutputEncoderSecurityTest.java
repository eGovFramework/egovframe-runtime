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

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.UnaryOperator;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.Test;

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
@RunWith(Enclosed.class)
public class EgovOutputEncoderSecurityTest {

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

		public static class BmpSweep {

		@Test
		public void forHtml_전수_왕복() {
			for (int c = 0; c <= 0xFFFF; c++) {
				String in = String.valueOf((char) c);
				String out = EgovOutputEncoder.forHtml(in);
				assertNoRaw(out, "<>\"'", c);
				assertEquals("왕복 실패 U+" + hex(c), in, decodeEntities(out));
			}
		}

		@Test
		public void forHtmlAttribute_전수_왕복() {
			for (int c = 0; c <= 0xFFFF; c++) {
				String in = String.valueOf((char) c);
				String out = EgovOutputEncoder.forHtmlAttribute(in);
				for (int k = 0; k < out.length(); k++) {
					char oc = out.charAt(k);
					if (oc < 0x80 && !isAlnum(oc) && oc != '&' && oc != '#' && oc != ';') {
						fail("따옴표 없는 속성에 구분자 후보가 남았다 U+" + hex(c) + " -> " + out);
					}
				}
				assertEquals("왕복 실패 U+" + hex(c), in, decodeEntities(out));
			}
		}

		@Test
		public void forJavaScript_전수_왕복() {
			for (int c = 0; c <= 0xFFFF; c++) {
				String in = String.valueOf((char) c);
				String out = EgovOutputEncoder.forJavaScript(in);
				assertNoRaw(out, "<>&\r\n" + LINE_SEPARATOR + PARAGRAPH_SEPARATOR, c);
				assertNoUnescapedQuote(out, c);
				for (int k = 0; k < out.length(); k++) {
					assertFalse("제어문자가 원시 형태로 남았다 U+" + hex(c), out.charAt(k) < 0x20);
				}
				assertEquals("왕복 실패 U+" + hex(c), in, decodeJavaScript(out));
			}
		}

		@Test
		public void forUrl_전수_왕복() {
			for (int c = 0; c <= 0xFFFF; c++) {
				if (Character.isSurrogate((char) c)) {
					continue; // 단독 서로게이트는 문자열로서 유효하지 않다 — 쌍은 아래에서 따로 본다
				}
				String in = String.valueOf((char) c);
				String out = EgovOutputEncoder.forUrl(in);
				for (int k = 0; k < out.length(); k++) {
					char oc = out.charAt(k);
					assertTrue("URL 구성요소 밖 문자 U+" + hex(c) + " -> " + out, isAlnum(oc) || "-._~%".indexOf(oc) >= 0);
				}
				assertEquals("왕복 실패 U+" + hex(c), in, decodePercent(out));
			}
			assertEquals("보충 평면 문자는 4바이트 UTF-8", "%F0%9F%98%80", EgovOutputEncoder.forUrl(SURROGATE_PAIR));
		}

		@Test
		public void forXml_전수_왕복() {
			for (int c = 0; c <= 0xFFFF; c++) {
				String in = String.valueOf((char) c);
				String out = EgovOutputEncoder.forXml(in);
				boolean forbidden = (c < 0x20 && c != '\t' && c != '\n' && c != '\r') || c == 0xFFFE || c == 0xFFFF;
				if (forbidden) {
					assertEquals("금지 문자가 남았다 U+" + hex(c), "", out);
				} else {
					assertNoRaw(out, "<>\"'", c);
					assertEquals("왕복 실패 U+" + hex(c), in, decodeEntities(out));
				}
			}
		}
	}

	// ------------------------------------------------------------ 이탈 페이로드

	/** 컨텍스트를 가리지 않고 던져 보는 페이로드 — 새 우회 기법이 알려지면 여기에 더한다. */
	private static final List<String> PAYLOADS = Arrays.asList(
			"</script>", "<!--", "-->", "]]>", "\"><script>alert(1)</script>", "' onmouseover='alert(1)",
			" onmouseover=alert(1)", "'-alert(1)-'", "\"-alert(1)-\"", BS + "'-alert(1)//", "</textarea><script>alert(1)</script>",
			LINE_SEPARATOR + "alert(1)", PARAGRAPH_SEPARATOR + "alert(1)", "\r\nalert(1)", "&#106;avascript:alert(1)",
			"%3Cscript%3E", "a&b=c&d", NUL + "<script>", "　<img src=x onerror=alert(1)>");

		public static class Breakouts {

		@Test
		public void html_컨텍스트() {
			for (String p : PAYLOADS) {
				String out = EgovOutputEncoder.forHtml(p);
				assertNoRaw(out, "<>\"'", -1);
				assertTrue("왕복 실패: " + p, decodeEntities(out).equals(p));
			}
		}

		@Test
		public void 따옴표없는_속성_컨텍스트() {
			for (String p : PAYLOADS) {
				String out = EgovOutputEncoder.forHtmlAttribute(p);
				for (int k = 0; k < out.length(); k++) {
					char oc = out.charAt(k);
					assertFalse("구분자 잔존: " + p + " -> " + out, oc < 0x80 && !isAlnum(oc) && oc != '&' && oc != '#' && oc != ';');
				}
			}
		}

		@Test
		public void 스크립트_컨텍스트() {
			for (String p : PAYLOADS) {
				String out = EgovOutputEncoder.forJavaScript(p);
				assertFalse("스크립트 블록 종료 가능: " + out, out.contains("</"));
				assertFalse("CDATA 종료 가능: " + out, out.contains("]]>"));
				assertNoRaw(out, "<>&\r\n" + LINE_SEPARATOR + PARAGRAPH_SEPARATOR, -1);
				assertNoUnescapedQuote(out, -1);
				assertEquals("왕복 실패: " + p, p, decodeJavaScript(out));
			}
			assertEquals(BS + "u003C" + BS + "/script" + BS + "u003E", EgovOutputEncoder.forJavaScript("</script>"));
		}

		@Test
		public void URL_컨텍스트() {
			for (String p : PAYLOADS) {
				String out = EgovOutputEncoder.forUrl(p);
				for (char forbidden : "/?&=#:<>\"' ".toCharArray()) {
					assertTrue("URL 구분자 잔존 [" + forbidden + "]: " + out, out.indexOf(forbidden) < 0);
				}
				assertEquals("왕복 실패: " + p, p, decodePercent(out));
			}
			assertEquals("javascript%3Aalert%281%29", EgovOutputEncoder.forUrl("javascript:alert(1)"));
			assertEquals("이미 인코딩된 % 도 다시 인코딩 — 이중 디코딩 우회 없음", "%253Cscript%253E", EgovOutputEncoder.forUrl("%3Cscript%3E"));
		}

		@Test
		public void 이벤트_핸들러_중첩_순서() {
			String value = "');alert(1);//";
			String attr = EgovOutputEncoder.forHtml(EgovOutputEncoder.forJavaScript(value));
			assertNoRaw(attr, "<>\"'", -1);
			// 브라우저는 속성값의 문자 참조를 먼저 푼 뒤 스크립트로 넘긴다
			String seenByScript = decodeEntities(attr);
			assertEquals(BS + "');alert(1);" + BS + "/" + BS + "/", seenByScript);
			assertEquals("스크립트 엔진이 리터럴 하나로 읽는다", value, decodeJavaScript(seenByScript));
		}
	}

	// ------------------------------------------------------------ 계약 고정 — 인코딩이 해결하지 않는 것

		public static class ContractLimits {

		@Test
		public void URL_스킴은_인코딩_대상이_아니다() {
			String url = "javascript:alert(1)";
			assertEquals("본문·따옴표 속성 인코딩은 문자를 바꾸지 않는다", url, EgovOutputEncoder.forHtml(url));
			assertEquals("문자 참조는 브라우저가 속성값 단계에서 도로 풀어 스킴이 살아난다", "javascript&#x3A;alert&#x28;1&#x29;", EgovOutputEncoder.forHtmlAttribute(url));
			assertEquals("구성요소 인코딩은 콜론을 퍼센트로 바꿔 스킴이 성립하지 않는다 — 값 하나로 쓸 때만 해당", "javascript%3Aalert%281%29", EgovOutputEncoder.forUrl(url));
		}

		@Test
		public void 템플릿_리터럴은_대상이_아니다() {
			assertEquals("${alert(1)}", EgovOutputEncoder.forJavaScript("${alert(1)}"));
			assertEquals("`+alert(1)+`", EgovOutputEncoder.forJavaScript("`+alert(1)+`"));
		}
	}

	// ------------------------------------------------------------ 견고성

		public static class Robustness {

		@Test
		public void 특수_입력() {
			for (UnaryOperator<String> f : encoders()) {
				for (String in : Arrays.asList(NUL, LONE_SURROGATE, BOM, SURROGATE_PAIR, NUL + SURROGATE_PAIR + LONE_SURROGATE)) {
					f.apply(in); // 예외가 나면 테스트 실패
				}
			}
			assertTrue(EgovOutputEncoder.forHtml(SURROGATE_PAIR).contains(SURROGATE_PAIR));
			assertTrue(EgovOutputEncoder.forJavaScript(SURROGATE_PAIR).contains(SURROGATE_PAIR));
			assertEquals("단독 서로게이트는 UTF-8 로 표현할 수 없어 ? 로 대체된다", "%3F", EgovOutputEncoder.forUrl(LONE_SURROGATE));
		}

		@Test
		public void 대용량_선형() {
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
				assertTrue("1 MB 인코딩에 " + millis + " ms — 선형이면 수십 ms 다", millis < 10_000);
			}
		}
	}

	// ------------------------------------------------------------ helpers

	private static List<UnaryOperator<String>> encoders() {
		return Arrays.asList(EgovOutputEncoder::forHtml, EgovOutputEncoder::forHtmlAttribute, EgovOutputEncoder::forJavaScript,
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
			assertTrue("닫히지 않은 문자 참조: " + s, semi > i);
			String entity = s.substring(i + 1, semi);
			switch (entity) {
				case "amp": out.append('&'); break;
				case "lt": out.append('<'); break;
				case "gt": out.append('>'); break;
				case "quot": out.append('"'); break;
				case "apos": out.append('\''); break;
				case "#39": out.append('\''); break;
				default:
					assertTrue("알 수 없는 문자 참조: &" + entity + ";", entity.startsWith("#x"));
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
			assertTrue("끝에 홀로 남은 역슬래시: " + s, i + 1 < s.length());
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
					assertEquals("알 수 없는 이스케이프: " + BS + n, BS, n);
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
