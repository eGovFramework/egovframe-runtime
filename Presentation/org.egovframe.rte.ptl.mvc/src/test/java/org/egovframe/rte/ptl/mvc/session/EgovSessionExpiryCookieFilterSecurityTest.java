package org.egovframe.rte.ptl.mvc.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.http.Cookie;

/**
 * {@link EgovSessionExpiryCookieFilter} 쿠키가 시각 값만 노출하고 잘못된 설정은 거부하는지 검증하는 회귀 테스트.
 */
public class EgovSessionExpiryCookieFilterSecurityTest {

	@Test
	public void 세션_식별자_미노출() throws Exception {
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
			assertTrue(cookie.getName() + "=" + cookie.getValue(), cookie.getValue().matches("-?[0-9]+"));
		}
		for (String header : response.getHeaders("Set-Cookie")) {
			assertFalse(header, header.contains(session.getId()));
			assertFalse(header, header.contains("\r") || header.contains("\n"));
		}
	}

	@Test
	public void 잘못된_설정은_실패로_닫힌다() {
		List<String[]> badParams = Arrays.asList(
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

			assertThrows(param[0] + "=" + param[1], IllegalArgumentException.class, () -> {
				filter.init(config);
				filter.doFilter(request, response, new MockFilterChain());
			});
			assertEquals(param[0] + "=" + param[1], 0, response.getHeaders("Set-Cookie").size());
		}
	}

}
