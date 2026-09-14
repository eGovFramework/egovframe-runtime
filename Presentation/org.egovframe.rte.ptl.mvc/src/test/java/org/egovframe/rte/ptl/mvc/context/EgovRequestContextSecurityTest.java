package org.egovframe.rte.ptl.mvc.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.AbstractRequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * {@link EgovRequestContext} 의 <b>신뢰 경계</b> 회귀 검증.
 *
 * <p>이 파사드는 요청 정보를 <b>해석하지 않고 그대로 전달</b>한다. 그래서 고정할 성질은 두 가지다 —
 * 원격 주소가 클라이언트가 위조할 수 있는 전달 헤더에 영향받지 않을 것, 어떤 적대적 헤더 값에도
 * 예외 없이 값을 그대로 돌려줄 것(해석은 호출자의 신뢰 경계 설계에 맡긴다).</p>
 */
class EgovRequestContextSecurityTest {

	@AfterEach
	void clearContext() {
		RequestContextHolder.resetRequestAttributes();
	}

	private static MockHttpServletRequest bind() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/app/page");
		request.setRemoteAddr("10.0.0.1");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
		return request;
	}

	@Test
	@DisplayName("원격 주소는 TCP 상대 주소이며, 클라이언트가 위조할 수 있는 전달 헤더에 영향받지 않는다")
	void 원격_주소는_전달_헤더에_영향받지_않음() {
		MockHttpServletRequest request = bind();
		request.addHeader("X-Forwarded-For", "203.0.113.5, 198.51.100.7");
		request.addHeader("X-Real-IP", "203.0.113.5");
		request.addHeader("Proxy-Client-IP", "203.0.113.5");
		request.addHeader("WL-Proxy-Client-IP", "203.0.113.5");
		request.addHeader("Forwarded", "for=203.0.113.5");

		assertEquals(Optional.of("10.0.0.1"), EgovRequestContext.getRemoteAddress());
	}

	@Test
	@DisplayName("적대적 헤더 값(CR/LF·10KB·스크립트·비정상 언어 태그)은 해석 없이 그대로 돌아오고 예외를 내지 않는다")
	void 적대_헤더는_해석_없이_그대로() {
		MockHttpServletRequest request = bind();
		String crlf = "https\r\nX-Injected: 1";
		String huge = "x".repeat(10240);
		request.addHeader("X-Forwarded-Proto", crlf);
		request.addHeader("User-Agent", huge);
		request.addHeader("Referer", "javascript:alert(1)");
		request.addHeader("Accept-Language", "xx-YY-ZZ-" + "a".repeat(500));
		request.addHeader("X-Forwarded-Host", "evil.example:80@good.example");

		assertEquals(Optional.of(crlf), EgovRequestContext.getHeader("X-Forwarded-Proto"));
		assertEquals(Optional.of(huge), EgovRequestContext.getHeader("User-Agent"));
		assertEquals(Optional.of("javascript:alert(1)"), EgovRequestContext.getHeader("Referer"));
		assertTrue(EgovRequestContext.getHeader("Accept-Language").isPresent());
		assertEquals(Optional.of("evil.example:80@good.example"), EgovRequestContext.getHeader("X-Forwarded-Host"));
		assertEquals(Optional.empty(), EgovRequestContext.getHeader("X-Not-Sent"));
		assertEquals(Optional.of("/app/page"), EgovRequestContext.getRequestUri());
	}

	@Test
	@DisplayName("서블릿이 아닌 요청 속성 구현이 바인딩돼 있으면 '없음'으로 판정한다 — 형 변환 예외가 나지 않는다")
	void 비서블릿_요청_속성은_없음으로_판정() {
		RequestContextHolder.setRequestAttributes(new AbstractRequestAttributes() {
			@Override
			public Object getAttribute(String name, int scope) {
				return null;
			}

			@Override
			public void setAttribute(String name, Object value, int scope) {
			}

			@Override
			public void removeAttribute(String name, int scope) {
			}

			@Override
			public String[] getAttributeNames(int scope) {
				return new String[0];
			}

			@Override
			public void registerDestructionCallback(String name, Runnable callback, int scope) {
			}

			@Override
			public Object resolveReference(String key) {
				return null;
			}

			@Override
			public String getSessionId() {
				return "none";
			}

			@Override
			public Object getSessionMutex() {
				return this;
			}

			@Override
			protected void updateAccessedSessionAttributes() {
			}
		});

		assertFalse(EgovRequestContext.isPresent());
		assertEquals(Optional.empty(), EgovRequestContext.getRequest());
		assertEquals(Optional.empty(), EgovRequestContext.getRemoteAddress());
		assertEquals(Optional.empty(), EgovRequestContext.getSession());
	}

}
