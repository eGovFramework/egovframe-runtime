package org.egovframe.rte.ptl.mvc.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.egovframe.rte.ptl.mvc.tags.ui.SubmitTokenTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockServletContext;

/**
 * {@link EgovSubmitTokens} 의 <b>예측 불가성·원자성·세션 바인딩·비반사</b> 회귀 검증.
 *
 * <p>기능 테스트({@link EgovSubmitTokensTest})가 흐름을 다루고, 이 클래스는 공격 관점의 성질을
 * 고정한다 — 토큰을 추측할 수 없고, 동시에 두 번 쓸 수 없고, 다른 세션에서 쓸 수 없으며,
 * 실패 응답이 요청 값을 되비추지 않는다.</p>
 */
class EgovSubmitTokensSecurityTest {

	private static final String UUID_V4 = "[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";

	@Test
	@DisplayName("같은 토큰으로 64건이 동시에 들어와도 정확히 1건만 통과한다 — 확인과 소비가 원자적이다")
	void 동시_소비는_정확히_1건() throws Exception {
		int threads = 64;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		try {
			for (int round = 0; round < 5; round++) {
				MockHttpSession session = new MockHttpSession();
				MockHttpServletRequest issuing = new MockHttpServletRequest();
				issuing.setSession(session);
				String token = EgovSubmitTokens.issue(issuing, "order");

				CountDownLatch ready = new CountDownLatch(threads);
				CountDownLatch start = new CountDownLatch(1);
				List<Future<Boolean>> results = new ArrayList<>();
				for (int i = 0; i < threads; i++) {
					results.add(pool.submit(() -> {
						MockHttpServletRequest request = new MockHttpServletRequest();
						request.setSession(session);
						request.setParameter(EgovSubmitTokens.PARAMETER_NAME, token);
						ready.countDown();
						start.await();
						return EgovSubmitTokens.validate(request, "order");
					}));
				}
				ready.await();
				start.countDown();

				int passed = 0;
				for (Future<Boolean> result : results) {
					if (result.get()) {
						passed++;
					}
				}
				assertEquals(1, passed, "round " + round);
			}
		} finally {
			pool.shutdownNow();
		}
	}

	@Test
	@DisplayName("토큰은 UUID v4 형식(122비트 무작위)이라 추측할 수 없고, 문자 집합이 HTML 속성·URL 에 안전하다")
	void 토큰_형식과_유일성() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		Set<String> issued = new HashSet<>();
		for (int i = 0; i < 2000; i++) {
			String token = EgovSubmitTokens.issue(request, "k" + (i % 8));
			assertTrue(token.matches(UUID_V4), token);
			issued.add(token);
		}
		assertEquals(2000, issued.size(), "발급값이 겹치면 안 된다");
	}

	@Test
	@DisplayName("토큰은 발급 세션에 묶인다 — 다른 세션에서 제시하면 거부되고, 그 시도가 원래 토큰을 소비하지도 않는다")
	void 세션_바인딩() {
		MockHttpSession victim = new MockHttpSession();
		MockHttpServletRequest issuing = new MockHttpServletRequest();
		issuing.setSession(victim);
		String token = EgovSubmitTokens.issue(issuing, "pay");

		MockHttpServletRequest attacker = new MockHttpServletRequest();
		attacker.setSession(new MockHttpSession());
		attacker.setParameter(EgovSubmitTokens.PARAMETER_NAME, token);
		assertFalse(EgovSubmitTokens.validate(attacker, "pay"));

		MockHttpServletRequest owner = new MockHttpServletRequest();
		owner.setSession(victim);
		owner.setParameter(EgovSubmitTokens.PARAMETER_NAME, token);
		assertTrue(EgovSubmitTokens.validate(owner, "pay"), "다른 세션의 시도가 원래 토큰을 소비하면 안 된다");
	}

	@Test
	@DisplayName("검증 실패 메시지는 요청이 제시한 값을 되비추지 않는다 — 화면에 그대로 찍혀도 주입이 되지 않는다")
	void 실패_메시지_비반사() {
		MockHttpSession session = new MockHttpSession();
		MockHttpServletRequest issuing = new MockHttpServletRequest();
		issuing.setSession(session);
		EgovSubmitTokens.issue(issuing, "form");

		String hostile = "x\" onfocus=\"alert(1)\" <script>";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(session);
		request.setParameter(EgovSubmitTokens.PARAMETER_NAME, hostile);
		EgovSubmitTokenException failure = org.junit.jupiter.api.Assertions.assertThrows(
				EgovSubmitTokenException.class, () -> EgovSubmitTokens.validateOrThrow(request, "form"));

		assertFalse(failure.getMessage().contains("alert"), failure.getMessage());
		assertFalse(failure.getMessage().contains("<"), failure.getMessage());
		assertEquals(EgovSubmitTokenException.Reason.TOKEN_MISMATCH, failure.getReason());
	}

	@Test
	@DisplayName("태그 출력은 고정 name 과 UUID value 뿐이다 — tokenKey 속성값은 출력에 실리지 않는다")
	void 태그_출력_고정_형식() throws Exception {
		MockServletContext context = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(context);
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockPageContext pageContext = new MockPageContext(context, request, response);

		SubmitTokenTag tag = new SubmitTokenTag();
		tag.setPageContext(pageContext);
		tag.setTokenKey("form\" onfocus=\"alert(1)");
		tag.doStartTag();

		String html = response.getContentAsString();
		assertTrue(html.matches("<input type=\"hidden\" name=\"" + EgovSubmitTokens.PARAMETER_NAME
				+ "\" value=\"" + UUID_V4 + "\"/>"), html);
		assertFalse(html.contains("onfocus"), html);
	}

}
