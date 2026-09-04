package org.egovframe.rte.ptl.mvc.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovHtmlSanitizer} 의 <b>XSS 공격 벡터</b> 회귀 검증(CWE-79).
 *
 * <p>기능 테스트({@link EgovHtmlSanitizerTest})가 대표 사례를 다루고, 이 클래스는 우회에 실제로
 * 쓰이는 입력 변형을 한 표로 모아 <b>하나라도 실행 가능한 구조가 남으면 실패</b>하게 한다.
 * 새 우회 기법이 알려지면 이 목록에 한 줄을 더한다.</p>
 *
 * <p>판정은 정제 결과를 다시 파싱해 검사한다 — {@code <script>}·이벤트 핸들러 속성·위험 프로토콜
 * URL·raw-text 실행 요소가 없어야 하고, 재파싱해도 같은 결과여야 한다(mutation XSS 신호 차단).</p>
 */
class EgovHtmlSanitizerSecurityTest {

	/** 소스에 리터럴로 두지 않는 제어문자. */
	private static final String NUL = String.valueOf((char) 0x00);
	private static final String TAB = String.valueOf((char) 0x09);
	private static final String LF = String.valueOf((char) 0x0A);
	private static final String CR = String.valueOf((char) 0x0D);
	private static final String SOH = String.valueOf((char) 0x01);

	private static final Pattern CONTROL = Pattern.compile("[\\x00-\\x20]");

	/**
	 * 실행 가능한 구조가 남았으면 그 목록을, 없으면 빈 목록을 돌려준다. 정제 결과를 <b>다시 파싱해</b>
	 * 실제 요소·속성 단위로 판정한다 — 이스케이프된 텍스트에 나타난 {@code onerror=} 문자열 같은
	 * 무해한 표기를 위험으로 오인하지 않기 위해서다.
	 */
	private static List<String> executableRemains(String out) {
		java.util.List<String> found = new java.util.ArrayList<>();
		if (out.toLowerCase(Locale.ROOT).contains("<script")) {
			found.add("raw<script");
		}
		java.util.Set<String> danger = java.util.Set.of("script", "iframe", "object", "embed", "base", "meta",
				"form", "style", "link", "svg", "math", "applet", "frame", "frameset", "noscript", "template",
				"noframes", "noembed", "xmp", "title", "textarea", "input", "button");
		org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(out);
		for (org.jsoup.nodes.Element el : doc.getAllElements()) {
			String name = el.tagName().toLowerCase(Locale.ROOT);
			if (danger.contains(name)) {
				found.add("tag:" + name);
			}
			for (org.jsoup.nodes.Attribute a : el.attributes()) {
				if (a.getKey().toLowerCase(Locale.ROOT).startsWith("on")) {
					found.add("handler:" + a.getKey());
				}
				String v = CONTROL.matcher(a.getValue()).replaceAll("").toLowerCase(Locale.ROOT);
				if (v.startsWith("javascript:") || v.startsWith("vbscript:") || v.startsWith("livescript:")
						|| v.startsWith("data:text/html")) {
					found.add("url:" + a.getKey() + "=" + a.getValue());
				}
			}
		}
		return found;
	}

	private static String reparse(String html) {
		org.jsoup.nodes.Document d = Jsoup.parseBodyFragment(html);
		d.outputSettings().prettyPrint(false);
		return d.body().html();
	}

	@Test
	@DisplayName("우회 변형 배터리 — 어떤 정책에서도 실행 가능한 구조가 남지 않는다")
	void 우회_변형_전부_무력화() {
		List<String> hostile = List.of(
				// javascript: 스킴 — 대소문자·공백·제어문자·엔티티 인코딩
				"<a href=\"javascript:alert(1)\">x</a>",
				"<a href=\"JaVaScRiPt:alert(1)\">x</a>",
				"<a href=\"java" + TAB + "script:alert(1)\">x</a>",
				"<a href=\"java" + LF + "script:alert(1)\">x</a>",
				"<a href=\"java" + CR + "script:alert(1)\">x</a>",
				"<a href=\" javascript:alert(1)\">x</a>",
				"<a href=\"&#106;avascript:alert(1)\">x</a>",
				"<a href=\"&#x6A;avascript:alert(1)\">x</a>",
				"<a href=\"java&#9;script:alert(1)\">x</a>",
				"<a href=\"javascript&colon;alert(1)\">x</a>",
				"<a href=\"vbscript:msgbox(1)\">x</a>",
				"<a href=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==\">x</a>",
				"<img src=\"javascript:alert(1)\">",
				"<img src=\"java" + TAB + "script:alert(1)\">",
				"<blockquote cite=\"javascript:alert(1)\">x</blockquote>",
				// 이벤트 핸들러
				"<img src=x onerror=alert(1)>",
				"<img src=x ONERROR=alert(1)>",
				"<img src=x onerror =alert(1)>",
				"<img src=x on" + NUL + "error=alert(1)>",
				"<details open ontoggle=alert(1)>x</details>",
				"<marquee onstart=alert(1)>x</marquee>",
				"<input onfocus=alert(1) autofocus>",
				"<body onload=alert(1)>",
				// 허용 목록 밖 위험 태그
				"<svg onload=alert(1)>",
				"<svg><a xlink:href=\"javascript:alert(1)\"><text>x</text></a></svg>",
				"<math href=\"javascript:alert(1)\">x</math>",
				"<iframe srcdoc=\"&lt;script&gt;alert(1)&lt;/script&gt;\"></iframe>",
				"<object data=\"javascript:alert(1)\"></object>",
				"<embed src=\"javascript:alert(1)\">",
				"<base href=\"javascript://evil/\">",
				"<meta http-equiv=\"refresh\" content=\"0;url=javascript:alert(1)\">",
				"<form action=\"javascript:alert(1)\"><input type=submit></form>",
				"<link rel=\"import\" href=\"http://evil/x.html\">",
				// style 로 흘리는 스크립트/클릭재킹
				"<style>body{background:url(javascript:alert(1))}</style>",
				"<img src=\"/a.png\" style=\"background:url(javascript:alert(1))\">",
				"<img src=\"/a.png\" style=\"width:expression(alert(1))\">",
				"<img src=\"/a.png\" style=\"-moz-binding:url(http://evil/x.xml#xss)\">",
				"<img src=\"/a.png\" style=\"position:fixed;top:0;left:0;width:100%;height:100%\">",
				// mutation XSS 고전
				"<scr<script>ipt>alert('xss')</scr</script>ipt>",
				"<svg><style><img src=x onerror=alert(1)></style></svg>",
				"<noscript><p title=\"</noscript><img src=x onerror=alert(1)>\"></noscript>",
				"<template><img src=x onerror=alert(1)></template>",
				"<select><iframe></select><img src=x onerror=alert(1)>",
				"<math><mtext><table><mglyph><style><img src=x onerror=alert(1)>",
				"<p><a href=\"/x\" title=\"<img src=x onerror=alert(1)>\">x</a></p>",
				// 형식 파괴
				"<<script>alert(1)//<</script>",
				"<script>alert(1)</script/>",
				"<scr" + NUL + "ipt>alert(1)</scr" + NUL + "ipt>",
				"<IMG SRC=x ONERROR=alert(1)><SCRIPT>alert(1)</SCRIPT>",
				"<img src=x/onerror=alert(1)>",
				// 선인코딩 입력은 그대로(이중 디코딩 금지)
				"&lt;script&gt;alert(1)&lt;/script&gt;");

		for (EgovHtmlSanitizePolicy policy : EgovHtmlSanitizePolicy.values()) {
			for (String attack : hostile) {
				String out = EgovHtmlSanitizer.sanitize(attack, policy);
				List<String> remains = executableRemains(out);
				assertTrue(remains.isEmpty(),
						policy + " 정책에서 실행 가능한 구조 잔존 " + remains + " — 입력=" + visible(attack) + " 출력=" + visible(out));
				assertEquals(reparse(out), reparse(reparse(out)),
						policy + " 정책 결과가 재파싱에 안정적이지 않음(mutation XSS 신호): " + visible(out));
			}
		}
	}

	@Test
	@DisplayName("제어문자로 끝나는 raw-text 태그를 넣어도 내장 정책은 스크립트를 실행 형태로 남기지 않는다")
	void 제어문자_꼬리_raw_text_태그도_내장정책에서_무력화() {
		// 파서 불일치 계열 공격(태그 이름 뒤 제어문자) — 내장 Safelist 는 raw-text 태그를 허용하지 않으므로
		// style/title/iframe/textarea/script 를 어떤 제어문자로 감싸도 실행 형태가 남지 않아야 한다
		String[] rawTags = { "style", "title", "iframe", "textarea", "noframes", "noembed", "xmp", "script" };
		String[] tails = { SOH, TAB, LF, CR, NUL, "" };
		for (EgovHtmlSanitizePolicy policy : EgovHtmlSanitizePolicy.values()) {
			for (String tag : rawTags) {
				for (String tail : tails) {
					String attack = "<" + tag + tail + "><img src=x onerror=alert(1)></" + tag + ">";
					String out = EgovHtmlSanitizer.sanitize(attack, policy);
					assertTrue(executableRemains(out).isEmpty(),
							policy + "/" + tag + " 제어문자 꼬리에서 실행 구조 잔존: " + visible(out));
				}
			}
		}
	}

	@Test
	@DisplayName("style 속성은 어떤 위험 CSS 를 넣어도 width/height 선언만 남는다")
	void style_속성_재필터_우회_불가() {
		String[] attacks = {
				"<img src=\"/a.png\" style=\"width:1px;background:url(javascript:alert(1))\">",
				"<img src=\"/a.png\" style=\"width:1px/**/;x:expression(alert(1))\">",
				"<img src=\"/a.png\" style=\"wid\\74h:1px;background:url(javascript:alert(1))\">",
				"<img src=\"/a.png\" style=\"behavior:url(x.htc)\">",
				"<img src=\"/a.png\" style=\"position:absolute;z-index:9999\">" };
		for (String attack : attacks) {
			String out = EgovHtmlSanitizer.sanitize(attack);
			String style = extractStyle(out);
			assertFalse(style.toLowerCase(Locale.ROOT).contains("url("), "url() 잔존: " + visible(out));
			assertFalse(style.toLowerCase(Locale.ROOT).contains("expression"), "expression 잔존: " + visible(out));
			assertFalse(style.toLowerCase(Locale.ROOT).contains("position"), "position 잔존: " + visible(out));
			assertFalse(style.toLowerCase(Locale.ROOT).contains("behavior"), "behavior 잔존: " + visible(out));
			assertTrue(style.isEmpty() || style.matches("(?i)\\s*(width|height)\\s*:\\s*\\d{1,4}(px|%)?\\s*;?\\s*"
					+ "((width|height)\\s*:\\s*\\d{1,4}(px|%)?\\s*;?\\s*)*"), "허용 밖 선언 잔존: " + visible(out));
		}
	}

	@Test
	@DisplayName("컨텍스트 상대경로는 보존하되 더미 baseUri 나 프로토콜 상대경로 스킴은 새지 않는다")
	void 상대경로_보존과_baseUri_비노출() {
		String out = EgovHtmlSanitizer.sanitize(
				"<a href=\"/cop/bbs/select.do?id=1\">x</a><img src=\"/utl/web/imageSrc.do?p=a.png\">");
		assertTrue(out.contains("href=\"/cop/bbs/select.do?id=1\""), "상대경로 링크 변형: " + out);
		assertTrue(out.contains("src=\"/utl/web/imageSrc.do?p=a.png\""), "상대경로 이미지 변형: " + out);
		assertFalse(out.contains("localhost"), "더미 baseUri 노출: " + out);
	}

	@Test
	@DisplayName("대용량·깊은 입력에도 실행 구조를 남기지 않는다")
	void 대용량_입력_견고성() {
		StringBuilder deep = new StringBuilder();
		for (int i = 0; i < 3000; i++) {
			deep.append("<div>");
		}
		deep.append("<img src=x onerror=alert(1)>");
		String outDeep = EgovHtmlSanitizer.sanitize(deep.toString());
		assertTrue(executableRemains(outDeep).isEmpty(), "깊은 입력에서 실행 구조 잔존");

		StringBuilder big = new StringBuilder();
		while (big.length() < 200_000) {
			big.append("<p>가나다 <b>x</b> <img src=x onerror=alert(1)> </p>");
		}
		String outBig = EgovHtmlSanitizer.sanitize(big.toString());
		assertTrue(executableRemains(outBig).isEmpty(), "대용량 입력에서 실행 구조 잔존");
	}

	private static String extractStyle(String html) {
		org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(html);
		org.jsoup.nodes.Element el = doc.selectFirst("[style]");
		return (el == null) ? "" : el.attr("style");
	}

	private static String visible(String s) {
		StringBuilder b = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (c < 0x20) {
				b.append("<").append((int) c).append(">");
			} else {
				b.append(c);
			}
		}
		return b.length() > 160 ? b.substring(0, 160) + "…" : b.toString();
	}

}
