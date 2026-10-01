package org.egovframe.rte.fdl.reactive.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 리액티브 사용자 ID 필터 테스트 — Principal → Context·교환 속성, 익명 대체값, MDC 복사 훅 연동, 접근로그 필터와의 조합, 순서.
 */
class EgovReactiveUserIdWebFilterTest {

	private final EgovReactiveUserIdWebFilter filter = new EgovReactiveUserIdWebFilter();

	@AfterEach
	void clearMdc() {
		MDC.clear();
	}

	private static ServerWebExchange exchangeWithPrincipal(String name) {
		Principal principal = () -> name;
		return MockServerWebExchange.from(MockServerHttpRequest.get("/api/me")).mutate().principal(Mono.just(principal)).build();
	}

	private static WebFilterChain capturingChain(AtomicReference<String> userId) {
		return exchange -> Mono.deferContextual(context -> {
			userId.set(context.getOrDefault(EgovReactiveUserIdWebFilter.USER_ID_KEY, "(missing)"));
			return Mono.empty();
		});
	}

	@Test
	void 인증된_요청은_Context_와_교환_속성에_사용자ID_를_싣는다() {
		ServerWebExchange exchange = exchangeWithPrincipal("hong");
		AtomicReference<String> seen = new AtomicReference<>();

		filter.filter(exchange, capturingChain(seen)).block();

		assertEquals("hong", seen.get());
		assertEquals("hong", exchange.getAttributes().get(EgovReactiveWebAccessLogFilter.ATTR_USER_ID));
	}

	@Test
	void Principal_이_없으면_anonymous_를_싣는다() {
		ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/me"));
		AtomicReference<String> seen = new AtomicReference<>();

		filter.filter(exchange, capturingChain(seen)).block();

		assertEquals(EgovReactiveUserIdWebFilter.ANONYMOUS_USER, seen.get());
		assertEquals("anonymous", exchange.getAttributes().get(EgovReactiveWebAccessLogFilter.ATTR_USER_ID));
	}

	@Test
	void Context_MDC_복사_훅이_켜져_있으면_로그_MDC_에도_사용자ID_가_실린다() {
		EgovMdcContextConfig hook = new EgovMdcContextConfig();
		hook.contextOperatorHook();
		try {
			ServerWebExchange exchange = exchangeWithPrincipal("hong");
			AtomicReference<String> mdcSeen = new AtomicReference<>();
			WebFilterChain chain = ex -> Mono.just("handled").map(value -> {
				mdcSeen.set(MDC.get(EgovReactiveUserIdWebFilter.USER_ID_KEY));   // 훅이 onNext 마다 Context 를 MDC 로 복사한다
				return value;
			}).then();

			filter.filter(exchange, chain).block();

			assertEquals("hong", mdcSeen.get());
		} finally {
			hook.cleanupHook();
		}
	}

	@Test
	void 접근로그_필터와_함께_쓰면_접근로그의_userId_가_채워진다() {
		List<String> lines = new ArrayList<>();
		EgovReactiveWebAccessLogFilter accessLogFilter = new EgovReactiveWebAccessLogFilter() {
			@Override
			protected void emit(boolean failed, String line) {
				lines.add(line);
			}
		};
		ServerWebExchange exchange = exchangeWithPrincipal("hong");
		WebFilterChain handler = ex -> {
			ex.getResponse().setStatusCode(HttpStatus.OK);
			return Mono.empty();
		};
		// 실제 체인 순서: 접근로그 필터(HIGHEST+10) → [Security -100] → 사용자 ID 필터(0) → 핸들러
		accessLogFilter.filter(exchange, ex -> filter.filter(ex, handler)).block();

		assertEquals(1, lines.size());
		assertTrue(lines.get(0).endsWith(" userId=hong"), lines.get(0));
	}

	@Test
	void 기본_순서는_Security_필터체인_뒤_0_이고_조정할_수_있다() {
		assertEquals(0, filter.getOrder());
		EgovReactiveUserIdWebFilter adjusted = new EgovReactiveUserIdWebFilter();
		adjusted.setOrder(50);
		assertEquals(50, adjusted.getOrder());
	}
}
