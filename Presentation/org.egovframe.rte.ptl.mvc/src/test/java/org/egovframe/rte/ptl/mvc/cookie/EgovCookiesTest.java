package org.egovframe.rte.ptl.mvc.cookie;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovCookies(안전 쿠키 빌더) 검증.
 */
class EgovCookiesTest {

	private static MockHttpServletRequest request(boolean secure, String contextPath) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSecure(secure);
		request.setContextPath(contextPath);
		return request;
	}

	@Test
	@DisplayName("이름과 값만 주면 HttpOnly·Lax·Secure(보안 채널)·Path=컨텍스트 루트가 붙는다")
	void 기본값() {
		MockHttpServletResponse response = new MockHttpServletResponse();

		Cookie cookie = EgovCookies.builder("theme", "dark").addTo(request(true, "/app"), response);

		assertTrue(cookie.isHttpOnly());
		assertTrue(cookie.getSecure());
		assertEquals("Lax", cookie.getAttribute("SameSite"));
		assertEquals("/app", cookie.getPath());
		assertEquals(-1, cookie.getMaxAge(), "기본은 세션 쿠키");
		assertNotNull(response.getCookie("theme"), "응답에 실린다");
	}

	@Test
	@DisplayName("평문 채널이면 Secure 가 자동으로 꺼져 브라우저가 저장한다 — 원본 Secure 하드코딩의 침묵 미동작 교정")
	void 평문_채널_Secure_자동() {
		Cookie cookie = EgovCookies.builder("theme", "dark").build(request(false, ""));

		assertFalse(cookie.getSecure());
		assertEquals("/", cookie.getPath(), "컨텍스트 루트가 비면 /");
	}

	@Test
	@DisplayName("secure(true) 명시는 채널 자동 판정보다 우선한다(프록시 뒤 HTTPS 종단)")
	void Secure_명시_우선() {
		Cookie cookie = EgovCookies.builder("theme", "dark").secure(true).build(request(false, ""));

		assertTrue(cookie.getSecure());
	}

	@Test
	@DisplayName("SameSite=None 인데 Secure 가 아니면 생성 시점에 거부된다 — 종전 구현이면 브라우저가 폐기하는 쿠키를 발급하던 입력")
	void None_비Secure_거부() {
		EgovCookies.Builder builder = EgovCookies.builder("sso", "x").sameSite("None");

		assertThrows(IllegalArgumentException.class, () -> builder.build(request(false, "")));
		assertEquals("None", builder.build(request(true, "")).getAttribute("SameSite"), "Secure 면 허용");
		assertThrows(IllegalArgumentException.class,
				() -> EgovCookies.builder("p", "x").partitioned().build(request(false, "")), "Partitioned 도 Secure 전제");
	}

	@Test
	@DisplayName("CRLF·세미콜론이 섞인 값과 token 이 아닌 이름은 거부된다 — 정제가 아니라 실패")
	void 주입_문자_거부() {
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("a", "x\r\nSet-Cookie: evil=1"));
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("a", "x; Path=/"));
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("bad name", "x"));
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("a=b", "x"));
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("", "x"));
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("a", "x").path("/p;q"));
	}

	@Test
	@DisplayName("한글·공백 값은 기본 거부, encodeValue() 를 선택하면 URL 인코딩 발급 + decodedValue 대칭")
	void 인코딩_옵션() {
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("name", "홍길동").build(request(true, "")));
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("name", "a b").build(request(true, "")));

		Cookie cookie = EgovCookies.builder("name", "홍길동 님").encodeValue().build(request(true, ""));
		assertFalse(cookie.getValue().contains("홍"), "인코딩된 값: " + cookie.getValue());

		MockHttpServletRequest next = request(true, "");
		next.setCookies(cookie);
		assertEquals(Optional.of("홍길동 님"), EgovCookies.decodedValue(next, "name"));
		assertEquals(Optional.of(cookie.getValue()), EgovCookies.value(next, "name"), "value() 는 원시 값");
	}

	@Test
	@DisplayName("삭제는 발급과 같은 Path·Domain 으로 Max-Age=0 을 낸다 — 원본은 path 미설정이라 다른 경로에서 안 지워졌다")
	void 삭제_정합() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		EgovCookies.builder("theme", "dark").path("/app").domain("example.go.kr").addTo(request(true, "/app"), response);

		Cookie expired = EgovCookies.expire("theme").path("/app").domain("example.go.kr").addTo(request(true, "/app"), response);

		assertEquals(0, expired.getMaxAge());
		assertEquals("", expired.getValue());
		assertEquals("/app", expired.getPath());
		assertEquals("example.go.kr", expired.getDomain());
		assertThrows(IllegalStateException.class, () -> EgovCookies.expire("theme").maxAge(10), "삭제 빌더의 Max-Age 는 0 고정");
		// 기본값끼리는 자동 정합 — 발급 Path(컨텍스트 루트) == 삭제 Path
		assertEquals(EgovCookies.builder("t", "v").build(request(true, "/app")).getPath(),
				EgovCookies.expire("t").build(request(true, "/app")).getPath());
	}

	@Test
	@DisplayName("읽기 부재는 쿠키 배열이 null 이든 이름이 없든 빈 Optional 하나다 — 원본의 \"\"/null 이원화 교정")
	void 읽기_부재_단일_표현() {
		MockHttpServletRequest none = request(true, "");
		assertNull(none.getCookies(), "전제: 쿠키 배열 null");
		assertEquals(Optional.empty(), EgovCookies.value(none, "theme"));

		MockHttpServletRequest other = request(true, "");
		other.setCookies(new Cookie("other", "1"));
		assertEquals(Optional.empty(), EgovCookies.value(other, "theme"));

		other.setCookies(new Cookie("theme", "dark"));
		assertEquals(Optional.of("dark"), EgovCookies.value(other, "theme"));
	}

	@Test
	@DisplayName("속성 지정 — maxAge(Duration)·domain·partitioned·sameSite 정규화·명시 해제")
	void 속성_반영() {
		Cookie cookie = EgovCookies.builder("theme", "dark")
				.maxAge(Duration.ofDays(30)).domain("example.go.kr").partitioned().sameSite("strict")
				.build(request(true, "/app"));

		assertEquals(30 * 24 * 3600, cookie.getMaxAge());
		assertEquals("example.go.kr", cookie.getDomain());
		assertEquals("Strict", cookie.getAttribute("SameSite"), "대소문자 무시 후 표준 표기로 정규화");
		assertNotNull(cookie.getAttribute("Partitioned"));

		Cookie unset = EgovCookies.builder("theme", "dark").sameSite("").httpOnly(false).build(request(true, ""));
		assertNull(unset.getAttribute("SameSite"), "빈 값이면 속성을 붙이지 않는다(만료시각 필터의 의미)");
		assertFalse(unset.isHttpOnly());
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("t", "v").sameSite("Foo"));
		assertThrows(IllegalArgumentException.class, () -> EgovCookies.builder("t", "v").maxAge(-2));
	}

	@Test
	@DisplayName("request 없이 build 하면 Secure=false·Path=/ 로 판정한다(비 웹 컨텍스트)")
	void request_null() {
		Cookie cookie = EgovCookies.builder("theme", "dark").build(null);

		assertFalse(cookie.getSecure());
		assertEquals("/", cookie.getPath());
	}

}
