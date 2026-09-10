package org.egovframe.rte.ptl.mvc.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

/**
 * {@link EgovRequestContext} 단위 테스트 — A-05 요청 컨텍스트 파사드.
 *
 * <p>기본 조회 계약과 함께, 원본(공통컴포넌트 EgovHttpRequestHelper)의
 * <b>예외 기반 존재 판정</b>·<b>세션 강제 생성</b>을 회귀 검증한다.</p>
 */
class EgovRequestContextTest {

	@AfterEach
	void clearContext() {
		RequestContextHolder.resetRequestAttributes();
	}

	private MockHttpServletRequest bind() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		return request;
	}

	private MockHttpServletRequest bind(MockHttpServletResponse response) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
		return request;
	}

	// ─────────────── 원본 결함 ① 예외 기반 존재 판정 ───────────────

	@Test
	@DisplayName("요청 컨텍스트가 없으면 isPresent 는 예외 없이 false 를 반환한다")
	void 컨텍스트_없음_예외_없이_판정() {
		assertFalse(EgovRequestContext.isPresent(),
				"배치·비동기 스레드에서의 호출은 정상 사용이므로 예외가 나면 안 된다");
	}

	@Test
	@DisplayName("컨텍스트가 없으면 조회 메서드는 모두 빈 Optional 이다")
	void 컨텍스트_없음_조회() {
		assertTrue(EgovRequestContext.getRequest().isEmpty());
		assertTrue(EgovRequestContext.getResponse().isEmpty());
		assertTrue(EgovRequestContext.getSession().isEmpty());
		assertTrue(EgovRequestContext.getRequestUri().isEmpty());
		assertTrue(EgovRequestContext.getMethod().isEmpty());
		assertTrue(EgovRequestContext.getRemoteAddress().isEmpty());
		assertTrue(EgovRequestContext.getHeader("X-Test").isEmpty());
		assertTrue(EgovRequestContext.getParameter("q").isEmpty());
	}

	@Test
	@DisplayName("반드시 필요한 자리에서는 getRequiredRequest 가 사유를 담아 예외를 던진다")
	void 필수_요청_예외() {
		IllegalStateException e = assertThrows(IllegalStateException.class,
				EgovRequestContext::getRequiredRequest);

		assertTrue(e.getMessage().contains("요청 컨텍스트"), "원인을 알 수 있는 메시지여야 한다");
	}

	// ─────────────── 원본 결함 ② 세션 강제 생성 ───────────────

	@Test
	@DisplayName("getSession 은 세션이 없으면 만들지 않는다")
	void 세션_생성하지_않음() {
		MockHttpServletRequest request = bind();

		assertTrue(EgovRequestContext.getSession().isEmpty());
		assertNull(request.getSession(false),
				"단순 조회로 세션이 생기면 비로그인 방문자에게도 세션이 할당된다");
	}

	@Test
	@DisplayName("세션이 이미 있으면 그 세션을 그대로 반환한다")
	void 기존_세션_반환() {
		MockHttpServletRequest request = bind();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);

		assertSame(session, EgovRequestContext.getSession().orElseThrow());
	}

	@Test
	@DisplayName("getOrCreateSession 은 명시적으로 세션을 만든다")
	void 세션_명시_생성() {
		MockHttpServletRequest request = bind();

		HttpSession created = EgovRequestContext.getOrCreateSession();

		assertNotNull(created);
		assertNotNull(request.getSession(false), "명시 호출에서는 생성돼야 한다");
	}

	@Test
	@DisplayName("컨텍스트가 없으면 getOrCreateSession 은 예외")
	void 세션_생성_컨텍스트_없음() {
		assertThrows(IllegalStateException.class, EgovRequestContext::getOrCreateSession);
	}

	// ─────────────────────────── 기본 조회 ───────────────────────────

	@Test
	@DisplayName("바인딩된 요청을 그대로 반환한다")
	void 요청_반환() {
		MockHttpServletRequest request = bind();

		assertTrue(EgovRequestContext.isPresent());
		assertSame(request, EgovRequestContext.getRequest().orElseThrow());
		assertSame(request, EgovRequestContext.getRequiredRequest());
	}

	@Test
	@DisplayName("요청 URI·메서드·원격 주소를 조회한다")
	void 요청_정보() {
		MockHttpServletRequest request = bind();
		request.setRequestURI("/board/list.do");
		request.setMethod("POST");
		request.setRemoteAddr("10.1.2.3");

		assertEquals("/board/list.do", EgovRequestContext.getRequestUri().orElseThrow());
		assertEquals("POST", EgovRequestContext.getMethod().orElseThrow());
		assertEquals("10.1.2.3", EgovRequestContext.getRemoteAddress().orElseThrow());
	}

	@Test
	@DisplayName("헤더·파라미터를 조회하고, 없으면 빈 Optional 이다")
	void 헤더_파라미터() {
		MockHttpServletRequest request = bind();
		request.addHeader("X-Request-Id", "abc-123");
		request.setParameter("pageIndex", "2");

		assertEquals("abc-123", EgovRequestContext.getHeader("X-Request-Id").orElseThrow());
		assertEquals("2", EgovRequestContext.getParameter("pageIndex").orElseThrow());
		assertTrue(EgovRequestContext.getHeader("X-Absent").isEmpty());
		assertTrue(EgovRequestContext.getParameter("absent").isEmpty());
	}

	@Test
	@DisplayName("헤더·파라미터 이름이 null 이면 빈 Optional (예외 아님)")
	void 이름_null() {
		bind();

		assertTrue(EgovRequestContext.getHeader(null).isEmpty());
		assertTrue(EgovRequestContext.getParameter(null).isEmpty());
	}

	@Test
	@DisplayName("응답이 함께 바인딩되면 응답도 반환한다")
	void 응답_반환() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		bind(response);

		assertSame(response, EgovRequestContext.getResponse().orElseThrow());
	}

	@Test
	@DisplayName("응답이 바인딩되지 않았으면 응답은 빈 Optional")
	void 응답_없음() {
		bind();

		assertTrue(EgovRequestContext.getResponse().isEmpty());
	}

	// ─────────────────────────── 정리 계약 ───────────────────────────

	@Test
	@DisplayName("컨텍스트를 비우면 다시 없음으로 돌아간다(스레드 재사용 대비)")
	void 컨텍스트_정리() {
		bind();
		assertTrue(EgovRequestContext.isPresent());

		RequestContextHolder.resetRequestAttributes();

		assertFalse(EgovRequestContext.isPresent(),
				"스레드풀 재사용 시 이전 요청 정보가 남으면 안 된다");
	}

}
