package org.egovframe.rte.ptl.mvc.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.egovframe.rte.ptl.mvc.token.EgovSubmitTokenException.Reason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

/**
 * {@link EgovSubmitTokens} 단위 테스트 — 중복 제출 방지 토큰.
 *
 * <p>발급·검증의 기본 계약과 함께, 원본(공통컴포넌트 EgovDoubleSubmitHelper)이 막지 못하던
 * <b>동시 요청 경합</b>을 중점 검증한다.</p>
 */
class EgovSubmitTokensTest {

	private MockHttpServletRequest requestWith(MockHttpSession session, String token) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(session);
		if (token != null) {
			request.setParameter(EgovSubmitTokens.PARAMETER_NAME, token);
		}
		return request;
	}

	// ─────────────────────────────── 발급 ───────────────────────────────

	@Test
	@DisplayName("발급하면 토큰이 생성되고 세션에 저장소가 만들어진다")
	void 발급_기본() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		String token = EgovSubmitTokens.issue(request);

		assertNotNull(token);
		assertFalse(token.isEmpty());
		assertNotNull(request.getSession(false), "발급 시 세션이 생성돼야 한다");
		assertNotNull(request.getSession().getAttribute(EgovSubmitTokens.SESSION_ATTRIBUTE_NAME));
	}

	@Test
	@DisplayName("발급할 때마다 값이 다르다")
	void 발급_유일성() {
		MockHttpSession session = new MockHttpSession();
		String first = EgovSubmitTokens.issue(requestWith(session, null));
		String second = EgovSubmitTokens.issue(requestWith(session, null));

		assertNotEquals(first, second);
	}

	@Test
	@DisplayName("같은 키로 재발급하면 이전 토큰은 무효가 된다(화면을 다시 연 경우)")
	void 재발급하면_이전_토큰_무효() {
		MockHttpSession session = new MockHttpSession();
		String old = EgovSubmitTokens.issue(requestWith(session, null));
		EgovSubmitTokens.issue(requestWith(session, null));

		assertFalse(EgovSubmitTokens.validate(requestWith(session, old)));
	}

	// ─────────────────────────────── 검증·소비 ───────────────────────────────

	@Test
	@DisplayName("발급한 토큰은 한 번만 통과한다 — 두 번째는 중복 제출로 거부")
	void 일회성_소비() {
		MockHttpSession session = new MockHttpSession();
		String token = EgovSubmitTokens.issue(requestWith(session, null));

		assertTrue(EgovSubmitTokens.validate(requestWith(session, token)), "첫 제출은 통과");
		assertFalse(EgovSubmitTokens.validate(requestWith(session, token)), "재전송은 거부");
	}

	@Test
	@DisplayName("발급받지 않은 토큰은 거부된다")
	void 위조_토큰_거부() {
		MockHttpSession session = new MockHttpSession();
		EgovSubmitTokens.issue(requestWith(session, null));

		assertFalse(EgovSubmitTokens.validate(requestWith(session, "forged-token")));
	}

	@Test
	@DisplayName("토큰 파라미터가 없으면 거부된다")
	void 파라미터_누락_거부() {
		MockHttpSession session = new MockHttpSession();
		EgovSubmitTokens.issue(requestWith(session, null));

		assertFalse(EgovSubmitTokens.validate(requestWith(session, null)));
	}

	@Test
	@DisplayName("세션이 없으면 거부되고, 검증이 세션을 새로 만들지 않는다")
	void 세션_없음_거부() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(EgovSubmitTokens.PARAMETER_NAME, "any");

		assertFalse(EgovSubmitTokens.validate(request));
		assertEquals(null, request.getSession(false), "검증이 세션을 생성하면 안 된다");
	}

	// ─────────────────────────────── 동시 요청 (원본 결함) ───────────────────────────────

	@Test
	@DisplayName("같은 토큰으로 동시에 20건이 들어와도 정확히 1건만 통과한다")
	void 동시_요청에서_하나만_통과() throws InterruptedException {
		final int threads = 20;
		MockHttpSession session = new MockHttpSession();
		final String token = EgovSubmitTokens.issue(requestWith(session, null));

		ExecutorService pool = Executors.newFixedThreadPool(threads);
		CountDownLatch ready = new CountDownLatch(threads);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(threads);
		AtomicInteger passed = new AtomicInteger();

		try {
			for (int i = 0; i < threads; i++) {
				pool.execute(() -> {
					ready.countDown();
					try {
						start.await();
						if (EgovSubmitTokens.validate(requestWith(session, token))) {
							passed.incrementAndGet();
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					} finally {
						done.countDown();
					}
				});
			}
			assertTrue(ready.await(5, TimeUnit.SECONDS), "스레드 준비 실패");
			start.countDown();   // 동시 출발
			assertTrue(done.await(10, TimeUnit.SECONDS), "동시 검증이 끝나지 않았다");
		} finally {
			pool.shutdownNow();
		}

		assertEquals(1, passed.get(),
				"확인·소비가 원자적이어야 한다 — 통과 건수: " + passed.get());
	}

	// ─────────────────────────────── 토큰 키 ───────────────────────────────

	@Test
	@DisplayName("토큰 키가 다르면 서로 간섭하지 않는다(폼 여러 개 동시 사용)")
	void 토큰_키_분리() {
		MockHttpSession session = new MockHttpSession();
		String orderToken = EgovSubmitTokens.issue(requestWith(session, null), "orderForm");
		String memberToken = EgovSubmitTokens.issue(requestWith(session, null), "memberForm");

		assertTrue(EgovSubmitTokens.validate(requestWith(session, orderToken), "orderForm"));
		assertTrue(EgovSubmitTokens.validate(requestWith(session, memberToken), "memberForm"),
				"다른 폼의 토큰은 소비되지 않아야 한다");
	}

	@Test
	@DisplayName("다른 키의 토큰으로는 통과할 수 없다")
	void 키_교차_거부() {
		MockHttpSession session = new MockHttpSession();
		String orderToken = EgovSubmitTokens.issue(requestWith(session, null), "orderForm");
		EgovSubmitTokens.issue(requestWith(session, null), "memberForm");

		assertFalse(EgovSubmitTokens.validate(requestWith(session, orderToken), "memberForm"));
	}

	@Test
	@DisplayName("키가 null·공백이면 기본 키로 동작한다")
	void 키_기본값() {
		MockHttpSession session = new MockHttpSession();
		String token = EgovSubmitTokens.issue(requestWith(session, null), null);

		assertTrue(EgovSubmitTokens.validate(requestWith(session, token), "  "));
	}

	// ─────────────────────────────── 예외 계약 ───────────────────────────────

	@Test
	@DisplayName("validateOrThrow 는 중복 제출을 TOKEN_NOT_ISSUED 로 알린다")
	void 예외_중복제출() {
		MockHttpSession session = new MockHttpSession();
		String token = EgovSubmitTokens.issue(requestWith(session, null));
		EgovSubmitTokens.validateOrThrow(requestWith(session, token));   // 첫 제출

		EgovSubmitTokenException e = assertThrows(EgovSubmitTokenException.class,
				() -> EgovSubmitTokens.validateOrThrow(requestWith(session, token)));

		assertEquals(Reason.TOKEN_NOT_ISSUED, e.getReason());
		assertTrue(e.isDuplicateSubmit(), "중복 제출로 분류돼야 한다");
		assertEquals(EgovSubmitTokens.DEFAULT_TOKEN_KEY, e.getTokenKey());
	}

	@Test
	@DisplayName("사유별로 예외가 구분된다 — 세션 없음·파라미터 없음·불일치")
	void 예외_사유_구분() {
		MockHttpServletRequest noSession = new MockHttpServletRequest();
		noSession.setParameter(EgovSubmitTokens.PARAMETER_NAME, "x");
		assertEquals(Reason.NO_SESSION,
				assertThrows(EgovSubmitTokenException.class,
						() -> EgovSubmitTokens.validateOrThrow(noSession)).getReason());

		MockHttpSession session = new MockHttpSession();
		EgovSubmitTokens.issue(requestWith(session, null));
		assertEquals(Reason.NO_PARAMETER,
				assertThrows(EgovSubmitTokenException.class,
						() -> EgovSubmitTokens.validateOrThrow(requestWith(session, null))).getReason());

		assertEquals(Reason.TOKEN_MISMATCH,
				assertThrows(EgovSubmitTokenException.class,
						() -> EgovSubmitTokens.validateOrThrow(requestWith(session, "wrong"))).getReason());
	}

	@Test
	@DisplayName("세션 만료·화면 구성 누락은 중복 제출로 분류하지 않는다")
	void 예외_비중복_분류() {
		MockHttpServletRequest noSession = new MockHttpServletRequest();
		noSession.setParameter(EgovSubmitTokens.PARAMETER_NAME, "x");
		EgovSubmitTokenException e = assertThrows(EgovSubmitTokenException.class,
				() -> EgovSubmitTokens.validateOrThrow(noSession));

		assertFalse(e.isDuplicateSubmit());
	}

	@Test
	@DisplayName("request 가 null 이면 명시 예외")
	void request_null_예외() {
		assertThrows(IllegalArgumentException.class, () -> EgovSubmitTokens.issue(null));
		assertThrows(IllegalArgumentException.class, () -> EgovSubmitTokens.validate(null));
		assertThrows(IllegalArgumentException.class, () -> EgovSubmitTokens.reset(null));
	}

	// ─────────────────────────────── 저장소 상한·초기화 ───────────────────────────────

	@Test
	@DisplayName("세션당 토큰 수 상한을 넘으면 가장 오래된 토큰부터 제거된다")
	void 토큰_상한() {
		MockHttpSession session = new MockHttpSession();
		String oldest = EgovSubmitTokens.issue(requestWith(session, null), "key-0");
		for (int i = 1; i <= EgovSubmitTokens.MAX_TOKENS_PER_SESSION; i++) {
			EgovSubmitTokens.issue(requestWith(session, null), "key-" + i);
		}

		EgovSubmitTokenStore store = (EgovSubmitTokenStore)
				session.getAttribute(EgovSubmitTokens.SESSION_ATTRIBUTE_NAME);
		assertEquals(EgovSubmitTokens.MAX_TOKENS_PER_SESSION, store.size(), "상한을 넘지 않아야 한다");
		assertFalse(EgovSubmitTokens.validate(requestWith(session, oldest), "key-0"),
				"가장 오래된 토큰이 제거돼야 한다");
	}

	@Test
	@DisplayName("reset 은 세션의 토큰을 모두 제거한다")
	void 초기화() {
		MockHttpSession session = new MockHttpSession();
		String token = EgovSubmitTokens.issue(requestWith(session, null));

		EgovSubmitTokens.reset(requestWith(session, null));

		assertFalse(EgovSubmitTokens.validate(requestWith(session, token)));
	}

	@Test
	@DisplayName("세션이 없어도 reset 은 예외 없이 통과한다")
	void 초기화_세션_없음() {
		EgovSubmitTokens.reset(new MockHttpServletRequest());
	}

	@Test
	@DisplayName("저장소 상한은 1 미만일 수 없다")
	void 저장소_상한_검증() {
		assertThrows(IllegalArgumentException.class, () -> new EgovSubmitTokenStore(0));
	}

}
