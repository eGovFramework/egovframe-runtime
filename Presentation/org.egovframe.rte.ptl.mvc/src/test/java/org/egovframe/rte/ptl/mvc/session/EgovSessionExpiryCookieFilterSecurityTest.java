package org.egovframe.rte.ptl.mvc.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import jakarta.servlet.http.Cookie;

/**
 * {@link EgovSessionExpiryCookieFilter} 의 <b>노출 범위·설정 오류</b> 회귀 검증.
 *
 * <p>이 필터의 쿠키는 스크립트가 읽어야 하므로 {@code HttpOnly} 가 아니다. 그래서 담기는 값이
 * <b>시각 두 개뿐</b>이라는 점(세션 식별자 미노출)이 안전성의 근거이며, 여기서 그것을 고정한다.
 * 초기화 파라미터에 주입 문자가 섞인 경우에는 쿠키를 내지 않고 실패해야 한다.</p>
 */
class EgovSessionExpiryCookieFilterSecurityTest {

	@Test
	@DisplayName("쿠키에는 시각 두 개(숫자)만 실리고 세션 식별자는 어디에도 실리지 않는다")
	void 세션_식별자_미노출() throws Exception {
		MockServletContext context = new MockServletContext();
		MockHttpSession session = new MockHttpSession(context, "SID-0123456789abcdef0123456789abcdef");
		session.setMaxInactiveInterval(1800);
		MockHttpServletRequest request = new MockHttpServletRequest(context, "GET", "/app/page");
		request.setContextPath("/app");
		request.setSession(session);
		MockHttpServletResponse response = new MockHttpServletResponse();

		EgovSessionExpiryCookieFilter filter = new EgovSessionExpiryCookieFilter();
		filter.init(new MockFilterConfig());
		filter.doFilter(request, response, new MockFilterChain());

		List<Cookie> cookies = Arrays.asList(response.getCookies());
		assertEquals(2, cookies.size());
		for (Cookie cookie : cookies) {
			assertTrue(cookie.getValue().matches("-?[0-9]+"), cookie.getName() + "=" + cookie.getValue());
		}
		for (String header : response.getHeaders("Set-Cookie")) {
			assertFalse(header.contains(session.getId()), header);
			assertFalse(header.contains("\r") || header.contains("\n"), header);
		}
	}

	@Test
	@DisplayName("주입 문자가 든 경로·미지원 SameSite·token 아닌 이름 설정은 쿠키를 내지 않고 실패로 닫힌다")
	void 잘못된_설정은_실패로_닫힌다() {
		List<String[]> badParams = List.of(
				new String[] {"cookiePath", "/app\r\nSet-Cookie: admin=true"},
				new String[] {"cookiePath", "/app; HttpOnly=false"},
				new String[] {"sameSite", "None; Secure"},
				new String[] {"serverTimeCookieName", "bad name"});
		for (String[] param : badParams) {
			MockFilterConfig config = new MockFilterConfig();
			config.addInitParameter(param[0], param[1]);
			MockHttpServletRequest request = new MockHttpServletRequest("GET", "/app/page");
			request.setContextPath("/app");
			request.getSession(true).setMaxInactiveInterval(1800);
			MockHttpServletResponse response = new MockHttpServletResponse();
			EgovSessionExpiryCookieFilter filter = new EgovSessionExpiryCookieFilter();

			assertThrows(IllegalArgumentException.class, () -> {
				filter.init(config);
				filter.doFilter(request, response, new MockFilterChain());
			}, param[0] + "=" + param[1]);
			assertEquals(0, response.getHeaders("Set-Cookie").size(), param[0] + "=" + param[1]);
		}
	}

}
