package org.egovframe.rte.ptl.mvc.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import jakarta.servlet.http.Cookie;

/**
 * {@link EgovSessionExpiryCookieFilter}·{@link EgovSessionExpiry} 단위 테스트.
 *
 * <p>기본 발행 계약과 함께, 원본(공통컴포넌트 SessionTimeoutCookieFilter)의
 * <b>세션 강제 생성</b>·<b>무제한 세션 음수 계산</b>·<b>httpOnly 목적 모순</b>을 회귀 검증한다.</p>
 */
class EgovSessionExpiryCookieFilterTest {

	private EgovSessionExpiryCookieFilter filter() throws Exception {
		EgovSessionExpiryCookieFilter filter = new EgovSessionExpiryCookieFilter();
		filter.init(new MockFilterConfig());
		return filter;
	}

	private MockHttpServletRequest requestWithSession(int maxInactiveSeconds) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession session = new MockHttpSession();
		session.setMaxInactiveInterval(maxInactiveSeconds);
		request.setSession(session);
		return request;
	}

	private Cookie cookie(MockHttpServletResponse response, String name) {
		return response.getCookie(name);
	}

	// ─────────────────────────────── 기본 발행 ───────────────────────────────

	@Test
	@DisplayName("세션이 있으면 서버 시각·만료 시각 쿠키를 발행한다")
	void 쿠키_발행() throws Exception {
		MockHttpServletRequest request = requestWithSession(1800);
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter().doFilter(request, response, new MockFilterChain());

		Cookie serverTime = cookie(response, EgovSessionExpiryCookieFilter.DEFAULT_SERVER_TIME_COOKIE);
		Cookie expiryTime = cookie(response, EgovSessionExpiryCookieFilter.DEFAULT_EXPIRY_TIME_COOKIE);
		assertNotNull(serverTime);
		assertNotNull(expiryTime);

		long server = Long.parseLong(serverTime.getValue());
		long expiry = Long.parseLong(expiryTime.getValue());
		assertEquals(1800L * 1000L, expiry - server, "만료 시각은 서버 시각 + 세션 유지 시간이어야 한다");
	}

	@Test
	@DisplayName("쿠키 경로는 / 이고 SameSite 는 Lax 가 기본이다")
	void 쿠키_속성_기본값() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter().doFilter(requestWithSession(600), response, new MockFilterChain());

		Cookie serverTime = cookie(response, EgovSessionExpiryCookieFilter.DEFAULT_SERVER_TIME_COOKIE);
		assertEquals("/", serverTime.getPath());
		assertEquals("Lax", serverTime.getAttribute("SameSite"));
	}

	@Test
	@DisplayName("필터는 항상 체인을 이어서 호출한다")
	void 체인_호출() throws Exception {
		MockFilterChain chain = new MockFilterChain();
		filter().doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

		assertNotNull(chain.getRequest(), "세션이 없어도 체인은 진행돼야 한다");
	}

	// ─────────────────────── 원본 결함 ① 세션 강제 생성 ───────────────────────

	@Test
	@DisplayName("세션이 없으면 쿠키를 발행하지 않고, 세션을 새로 만들지도 않는다")
	void 세션_생성하지_않음() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();   // 세션 없음
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter().doFilter(request, response, new MockFilterChain());

		assertEquals(0, response.getCookies().length, "세션이 없으면 쿠키를 내리지 않는다");
		assertNull(request.getSession(false),
				"필터가 세션을 생성하면 정적 자원 요청까지 세션이 만들어진다");
	}

	// ─────────────────── 원본 결함 ② 무제한 세션 음수 계산 ───────────────────

	@Test
	@DisplayName("무제한 세션(maxInactiveInterval<=0)은 만료 시각이 -1 이다 — 과거 시각이 되면 안 된다")
	void 무제한_세션() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter().doFilter(requestWithSession(-1), response, new MockFilterChain());

		Cookie expiryTime = cookie(response, EgovSessionExpiryCookieFilter.DEFAULT_EXPIRY_TIME_COOKIE);
		long expiry = Long.parseLong(expiryTime.getValue());

		assertEquals(EgovSessionExpiry.UNLIMITED, expiry);
		long server = Long.parseLong(
				cookie(response, EgovSessionExpiryCookieFilter.DEFAULT_SERVER_TIME_COOKIE).getValue());
		assertFalse(expiry > 0 && expiry < server,
				"만료 시각이 서버 시각보다 과거이면 화면이 즉시 만료로 오판한다");
	}

	@Test
	@DisplayName("큰 세션 유지 시간도 넘침 없이 계산된다(int 곱셈 한계 초과)")
	void 오버플로_방지() {
		// int 로 계산하면 2,147,484초를 넘길 때 넘침이 발생한다
		int hugeSeconds = 3_000_000;
		EgovSessionExpiry expiry = EgovSessionExpiry.of(sessionOf(hugeSeconds), 1_000_000L);

		assertEquals(1_000_000L + 3_000_000L * 1000L, expiry.getExpiryTime());
		assertTrue(expiry.getExpiryTime() > 0, "넘침이 나면 음수가 된다");
	}

	private MockHttpSession sessionOf(int seconds) {
		MockHttpSession session = new MockHttpSession();
		session.setMaxInactiveInterval(seconds);
		return session;
	}

	// ─────────────────── 원본 결함 ③ httpOnly 목적 모순 ───────────────────

	@Test
	@DisplayName("기본값은 httpOnly=false — 화면 스크립트가 읽어야 하는 쿠키다")
	void httpOnly_기본_false() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter().doFilter(requestWithSession(600), response, new MockFilterChain());

		assertFalse(cookie(response, EgovSessionExpiryCookieFilter.DEFAULT_SERVER_TIME_COOKIE).isHttpOnly());
		assertFalse(cookie(response, EgovSessionExpiryCookieFilter.DEFAULT_EXPIRY_TIME_COOKIE).isHttpOnly());
	}

	@Test
	@DisplayName("httpOnly 를 켤 수도 있다")
	void httpOnly_설정() throws Exception {
		MockFilterConfig config = new MockFilterConfig();
		config.addInitParameter("httpOnly", "true");
		EgovSessionExpiryCookieFilter filter = new EgovSessionExpiryCookieFilter();
		filter.init(config);

		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(requestWithSession(600), response, new MockFilterChain());

		assertTrue(cookie(response, EgovSessionExpiryCookieFilter.DEFAULT_SERVER_TIME_COOKIE).isHttpOnly());
	}

	// ─────────────────────────────── 설정 ───────────────────────────────

	@Test
	@DisplayName("HTTPS 요청이면 Secure 가 붙는다")
	void secure_요청() throws Exception {
		MockHttpServletRequest request = requestWithSession(600);
		request.setSecure(true);
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter().doFilter(request, response, new MockFilterChain());

		assertTrue(cookie(response, EgovSessionExpiryCookieFilter.DEFAULT_SERVER_TIME_COOKIE).getSecure());
	}

	@Test
	@DisplayName("쿠키 이름·경로·SameSite 를 초기화 파라미터로 바꿀 수 있다")
	void 설정_변경() throws Exception {
		MockFilterConfig config = new MockFilterConfig();
		config.addInitParameter("serverTimeCookieName", "svrTime");
		config.addInitParameter("expiryTimeCookieName", "expTime");
		config.addInitParameter("cookiePath", "/app");
		config.addInitParameter("sameSite", "Strict");
		EgovSessionExpiryCookieFilter filter = new EgovSessionExpiryCookieFilter();
		filter.init(config);

		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(requestWithSession(600), response, new MockFilterChain());

		Cookie custom = cookie(response, "svrTime");
		assertNotNull(custom);
		assertNotNull(cookie(response, "expTime"));
		assertEquals("/app", custom.getPath());
		assertEquals("Strict", custom.getAttribute("SameSite"));
	}

	@Test
	@DisplayName("제외 경로에는 쿠키를 발행하지 않는다(정적 자원)")
	void 제외_경로() throws Exception {
		MockFilterConfig config = new MockFilterConfig();
		config.addInitParameter("excludePathPatterns", "/css/**, /js/**");
		EgovSessionExpiryCookieFilter filter = new EgovSessionExpiryCookieFilter();
		filter.init(config);

		MockHttpServletRequest request = requestWithSession(600);
		request.setRequestURI("/css/egov.css");
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, new MockFilterChain());

		assertEquals(0, response.getCookies().length);
	}

	@Test
	@DisplayName("제외 경로 판정은 컨텍스트 경로를 제거하고 수행한다")
	void 제외_경로_컨텍스트() throws Exception {
		MockFilterConfig config = new MockFilterConfig();
		config.addInitParameter("excludePathPatterns", "/images/**");
		EgovSessionExpiryCookieFilter filter = new EgovSessionExpiryCookieFilter();
		filter.init(config);

		MockHttpServletRequest request = requestWithSession(600);
		request.setContextPath("/egov");
		request.setRequestURI("/egov/images/logo.png");
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, new MockFilterChain());

		assertEquals(0, response.getCookies().length);
	}

	@Test
	@DisplayName("제외 대상이 아닌 경로는 정상 발행된다")
	void 제외_비대상() throws Exception {
		MockFilterConfig config = new MockFilterConfig();
		config.addInitParameter("excludePathPatterns", "/css/**");
		EgovSessionExpiryCookieFilter filter = new EgovSessionExpiryCookieFilter();
		filter.init(config);

		MockHttpServletRequest request = requestWithSession(600);
		request.setRequestURI("/board/list.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, new MockFilterChain());

		assertEquals(2, response.getCookies().length);
	}

	// ─────────────────────────── EgovSessionExpiry ───────────────────────────

	@Test
	@DisplayName("세션이 없으면 빈 Optional 을 반환하고 세션을 만들지 않는다")
	void 값객체_세션_없음() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		assertTrue(EgovSessionExpiry.from(request).isEmpty());
		assertNull(request.getSession(false));
	}

	@Test
	@DisplayName("잔여 시간·만료 판정을 제공한다")
	void 값객체_계산() {
		EgovSessionExpiry expiry = EgovSessionExpiry.of(sessionOf(600), 1_000L);

		assertEquals(600_000L, expiry.getRemainingMillis());
		assertEquals(601_000L, expiry.getExpiryTime());
		assertEquals(600, expiry.getMaxInactiveIntervalSeconds());
		assertFalse(expiry.isUnlimited());
		assertFalse(expiry.isExpiredAt(600_999L));
		assertTrue(expiry.isExpiredAt(601_000L), "만료 시각에 도달하면 만료다");
	}

	@Test
	@DisplayName("무제한 세션은 잔여 시간 -1 이며 만료되지 않는다")
	void 값객체_무제한() {
		EgovSessionExpiry expiry = EgovSessionExpiry.of(sessionOf(0), 1_000L);

		assertTrue(expiry.isUnlimited());
		assertEquals(EgovSessionExpiry.UNLIMITED, expiry.getRemainingMillis());
		assertFalse(expiry.isExpiredAt(Long.MAX_VALUE));
	}

	@Test
	@DisplayName("null 인자는 명시 예외")
	void 값객체_null_예외() {
		assertThrows(IllegalArgumentException.class, () -> EgovSessionExpiry.from(null));
		assertThrows(IllegalArgumentException.class, () -> EgovSessionExpiry.of(null, 0L));
	}

	@Test
	@DisplayName("setter 로 등록해도 초기화 파라미터와 같게 동작한다(Java Config)")
	void 자바_설정() throws Exception {
		EgovSessionExpiryCookieFilter filter = new EgovSessionExpiryCookieFilter();
		filter.setServerTimeCookieName("st");
		filter.setExpiryTimeCookieName("et");
		filter.setExcludePathPatterns(Arrays.asList("/static/**"));

		MockHttpServletRequest excluded = requestWithSession(600);
		excluded.setRequestURI("/static/a.png");
		MockHttpServletResponse response1 = new MockHttpServletResponse();
		filter.doFilter(excluded, response1, new MockFilterChain());
		assertEquals(0, response1.getCookies().length);

		MockHttpServletResponse response2 = new MockHttpServletResponse();
		filter.doFilter(requestWithSession(600), response2, new MockFilterChain());
		assertNotNull(cookie(response2, "st"));
		assertNotNull(cookie(response2, "et"));
	}

}
