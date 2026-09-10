package org.egovframe.rte.ptl.mvc.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovHtmlSanitizer} 단위 테스트 — A-01 HTML 출력 정제.
 *
 * <p>공격 벡터 제거(스크립트·이벤트 핸들러·위험 프로토콜·변형 공격)와
 * 정상 서식 보존(정책별 허용 범위·상대경로·style 재필터)을 함께 검증한다.</p>
 */
class EgovHtmlSanitizerTest {

	// ─────────────────────────────── 공격 벡터 제거 ───────────────────────────────

	@Test
	@DisplayName("script 태그는 어떤 정책에서도 제거된다")
	void script_태그_제거() {
		String attack = "<p>본문</p><script>alert('xss')</script>";
		for (EgovHtmlSanitizePolicy policy : EgovHtmlSanitizePolicy.values()) {
			String result = EgovHtmlSanitizer.sanitize(attack, policy);
			assertFalse(result.contains("<script"), policy + " 정책에서 script 잔존: " + result);
			assertFalse(result.contains("alert"), policy + " 정책에서 script 본문 잔존: " + result);
		}
	}

	@Test
	@DisplayName("이벤트 핸들러 속성(onclick·onerror)은 제거된다")
	void 이벤트_핸들러_제거() {
		String result = EgovHtmlSanitizer.sanitize(
				"<p onclick=\"steal()\">글</p><img src=\"/img/a.png\" onerror=\"steal()\">");
		assertFalse(result.contains("onclick"));
		assertFalse(result.contains("onerror"));
		assertTrue(result.contains("<p>글</p>"), "정상 태그는 남아야 한다: " + result);
		assertTrue(result.contains("<img"), "정상 이미지는 남아야 한다: " + result);
	}

	@Test
	@DisplayName("javascript: 프로토콜 링크는 href 자체가 제거된다")
	void 위험_프로토콜_제거() {
		String result = EgovHtmlSanitizer.sanitize("<a href=\"javascript:alert(1)\">클릭</a>");
		assertFalse(result.contains("javascript"), "javascript: URL 잔존: " + result);
		assertTrue(result.contains("클릭"), "링크 텍스트는 남아야 한다");
	}

	@Test
	@DisplayName("변형 공격(<scr<script>ipt>)도 파서 기반 정제라 무력화된다")
	void 변형_공격_무력화() {
		// 문자열 치환식 필터가 뚫리는 고전 우회 — 파서 기반 화이트리스트는 뚫리지 않는다
		String result = EgovHtmlSanitizer.sanitize("<scr<script>ipt>alert('xss')</scr</script>ipt>");
		assertFalse(result.contains("<script"), "변형 공격 잔존: " + result);
	}

	@Test
	@DisplayName("svg/iframe 등 허용 목록 밖 태그는 제거된다")
	void 허용목록_밖_태그_제거() {
		String result = EgovHtmlSanitizer.sanitize(
				"<svg onload=\"alert(1)\"></svg><iframe src=\"https://evil.example\"></iframe><p>본문</p>");
		assertFalse(result.contains("<svg"));
		assertFalse(result.contains("<iframe"));
		assertTrue(result.contains("<p>본문</p>"));
	}

	// ─────────────────────────────── 정상 서식 보존 ───────────────────────────────

	@Test
	@DisplayName("기본 정책(RICH_TEXT)은 에디터 서식(표·제목·이미지·링크)을 보존한다")
	void 기본정책_서식_보존() {
		String richText = "<h2>제목</h2><table><tbody><tr><td>셀</td></tr></tbody></table>"
				+ "<p><strong>강조</strong>와 <a href=\"https://www.egovframe.go.kr\">링크</a></p>"
				+ "<img src=\"https://cdn.example/a.png\">";
		String result = EgovHtmlSanitizer.sanitize(richText);
		assertTrue(result.contains("<h2>제목</h2>"));
		assertTrue(result.contains("<td>셀</td>"));
		assertTrue(result.contains("<strong>강조</strong>"));
		assertTrue(result.contains("href=\"https://www.egovframe.go.kr\""));
		assertTrue(result.contains("<img src=\"https://cdn.example/a.png\""));
	}

	@Test
	@DisplayName("컨텍스트 상대경로 URL 은 절대경로로 바뀌지 않고 유지된다")
	void 상대경로_보존() {
		String result = EgovHtmlSanitizer.sanitize(
				"<img src=\"/utl/web/imageSrc.do?path=2026/a.png\">"
						+ "<a href=\"/cop/bbs/select.do?bbsId=1\">글</a>");
		assertTrue(result.contains("src=\"/utl/web/imageSrc.do?path=2026/a.png\""),
				"이미지 상대경로가 변형됨: " + result);
		assertTrue(result.contains("href=\"/cop/bbs/select.do?bbsId=1\""),
				"링크 상대경로가 변형됨: " + result);
		assertFalse(result.contains("http://localhost"), "더미 baseUri 가 출력에 노출됨: " + result);
	}

	@Test
	@DisplayName("한글 본문은 훼손 없이 그대로 보존된다")
	void 한글_본문_보존() {
		String html = "<p>안녕하세요. 표준프레임워크 실행환경입니다, 마침표와 쉼표.</p>";
		assertEquals(html, EgovHtmlSanitizer.sanitize(html));
	}

	// ─────────────────────────────── style 재필터 ───────────────────────────────

	@Test
	@DisplayName("img style 은 width/height 선언만 남고 그 외 CSS 는 제거된다")
	void style_재필터() {
		String result = EgovHtmlSanitizer.sanitize(
				"<img src=\"/a.png\" style=\"width:640px; position:fixed; height:50%; "
						+ "background:url(javascript:alert(1))\">");
		assertTrue(result.contains("width:640px"), "허용 선언 소실: " + result);
		assertTrue(result.contains("height:50%"), "허용 선언 소실: " + result);
		assertFalse(result.contains("position"), "위험 선언 잔존: " + result);
		assertFalse(result.contains("background"), "위험 선언 잔존: " + result);
	}

	@Test
	@DisplayName("허용 선언이 하나도 없는 style 속성은 통째로 제거된다")
	void style_전부_위험시_속성_제거() {
		String result = EgovHtmlSanitizer.sanitize(
				"<img src=\"/a.png\" style=\"position:absolute; z-index:9999\">");
		assertFalse(result.contains("style="), "빈 style 속성이 남음: " + result);
		assertTrue(result.contains("<img"), "이미지 자체는 남아야 한다");
	}

	// ─────────────────────────────── 정책별 허용 범위 ───────────────────────────────

	@Test
	@DisplayName("TEXT_ONLY/stripToText 는 태그를 전부 제거하고 텍스트만 남긴다")
	void 텍스트_전용_정책() {
		String result = EgovHtmlSanitizer.stripToText("<b>공지</b>: <a href=\"/a.do\">이동</a>");
		assertFalse(result.contains("<"), "태그 잔존: " + result);
		assertTrue(result.contains("공지"));
		assertTrue(result.contains("이동"));
	}

	@Test
	@DisplayName("SIMPLE_TEXT 는 강조 태그만 남기고 링크는 제거한다")
	void 단순_강조_정책() {
		String result = EgovHtmlSanitizer.sanitize(
				"<b>강조</b> <a href=\"/a.do\">링크</a>", EgovHtmlSanitizePolicy.SIMPLE_TEXT);
		assertTrue(result.contains("<b>강조</b>"));
		assertFalse(result.contains("<a "), "링크 태그 잔존: " + result);
		assertTrue(result.contains("링크"), "링크 텍스트는 남아야 한다");
	}

	@Test
	@DisplayName("BASIC 은 이미지를 제거하고 BASIC_WITH_IMAGES 는 남긴다")
	void 이미지_허용_정책_차이() {
		String html = "<p>본문</p><img src=\"https://cdn.example/a.png\">";
		String basic = EgovHtmlSanitizer.sanitize(html, EgovHtmlSanitizePolicy.BASIC);
		String withImages = EgovHtmlSanitizer.sanitize(html, EgovHtmlSanitizePolicy.BASIC_WITH_IMAGES);
		assertFalse(basic.contains("<img"), "BASIC 에서 이미지 잔존: " + basic);
		assertTrue(withImages.contains("<img"), "BASIC_WITH_IMAGES 에서 이미지 소실: " + withImages);
	}

	// ─────────────────────────────── 계약(경계값·확장점) ───────────────────────────────

	@Test
	@DisplayName("null·공백 입력은 빈 문자열을 반환한다(뷰에서 null 분기 불필요)")
	void null_공백_계약() {
		assertEquals("", EgovHtmlSanitizer.sanitize(null));
		assertEquals("", EgovHtmlSanitizer.sanitize("   "));
		assertEquals("", EgovHtmlSanitizer.stripToText(null));
		assertEquals("", EgovHtmlSanitizer.sanitize(null, EgovHtmlSanitizePolicy.BASIC));
	}

	@Test
	@DisplayName("정책 null 은 기본 정책(RICH_TEXT)으로 동작한다")
	void 정책_null_기본값() {
		String html = "<h2>제목</h2>";
		assertEquals(EgovHtmlSanitizer.sanitize(html),
				EgovHtmlSanitizer.sanitize(html, (EgovHtmlSanitizePolicy) null));
	}

	@Test
	@DisplayName("커스텀 Safelist 확장점 — 허용 목록을 직접 구성할 수 있고 style 재필터는 유지된다")
	void 커스텀_Safelist() {
		Safelist custom = Safelist.none().addTags("mark").addAttributes("mark", "style");
		String result = EgovHtmlSanitizer.sanitize(
				"<mark style=\"width:10px; color:red\">형광펜</mark><b>제거대상</b>", custom);
		assertTrue(result.contains("<mark"), "커스텀 허용 태그 소실: " + result);
		assertFalse(result.contains("<b>"), "커스텀 목록 밖 태그 잔존: " + result);
		assertTrue(result.contains("width:10px"), "허용 style 선언 소실: " + result);
		assertFalse(result.contains("color"), "커스텀 경로에서도 style 재필터가 적용돼야 한다: " + result);
	}

	@Test
	@DisplayName("커스텀 Safelist null 은 명시 예외")
	void 커스텀_Safelist_null_예외() {
		assertThrows(IllegalArgumentException.class,
				() -> EgovHtmlSanitizer.sanitize("<p>x</p>", (Safelist) null));
	}

}
