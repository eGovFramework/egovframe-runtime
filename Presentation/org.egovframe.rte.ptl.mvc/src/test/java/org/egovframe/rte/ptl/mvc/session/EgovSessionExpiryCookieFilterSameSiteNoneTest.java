package org.egovframe.rte.ptl.mvc.session;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * {@code sameSite=None} 은 init() 시점엔 유효한 값이라 통과하지만, 비보안(HTTP) 요청에서는
 * {@code EgovCookies} 빌더가 발급을 거부한다. 이 클래스는 "필터는 항상 체인을 이어서
 * 호출한다"(doFilter javadoc "다음 필터로 넘긴다", {@link EgovSessionExpiryCookieFilterTest#체인_호출()})는
 * 계약이 그 경우에도 지켜지는지 검증한다.
 */
class EgovSessionExpiryCookieFilterSameSiteNoneTest {

	@Test
	@DisplayName("sameSite=None 인데 요청이 비보안이어도 체인은 계속 호출된다(부가 쿠키 발급 실패가 요청을 막지 않는다)")
	void SameSite_None_비보안_요청에도_체인은_계속된다() throws Exception {
		MockFilterConfig config = new MockFilterConfig();
		config.addInitParameter("sameSite", "None"); // 유효한 값이라 init() 은 통과한다
		EgovSessionExpiryCookieFilter filter = new EgovSessionExpiryCookieFilter();
		filter.init(config);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSecure(false); // HTTP — 프록시 신뢰 헤더 설정 없음
		request.getSession(true).setMaxInactiveInterval(600);
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		assertDoesNotThrow(() -> filter.doFilter(request, response, chain),
				"SameSite=None + 비보안 요청 조합에서 예외가 나면 안 된다");
		assertNotNull(chain.getRequest(), "쿠키 발급이 실패해도 다음 필터(실제 업무 로직)는 반드시 호출돼야 한다");
	}

}
