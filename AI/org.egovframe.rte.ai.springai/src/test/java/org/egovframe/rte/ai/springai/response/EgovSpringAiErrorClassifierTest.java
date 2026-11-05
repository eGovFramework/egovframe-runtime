package org.egovframe.rte.ai.springai.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.SocketTimeoutException;
import java.util.Optional;

import org.egovframe.rte.ai.core.response.EgovAiErrorType;
import org.egovframe.rte.ai.core.response.EgovAiFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;

/**
 * Spring AI 예외 분류기 계약 테스트 — 일시 오류만 확정, 나머지는 기반 폴백의 체인·키워드 판정(스펙 ai/springai-response-handling 1항).
 */
class EgovSpringAiErrorClassifierTest {

	private final EgovSpringAiErrorClassifier classifier = new EgovSpringAiErrorClassifier();

	@Test
	@DisplayName("TransientAiException 은 TRANSIENT 로 확정한다")
	void transientIsConfirmed() {
		assertEquals(Optional.of(EgovAiErrorType.TRANSIENT), classifier.classify(new TransientAiException("rate limited")));
	}

	@Test
	@DisplayName("NonTransientAiException 과 그 밖의 예외는 확정하지 않는다")
	void othersAreLeftToChain() {
		assertTrue(classifier.classify(new NonTransientAiException("bad request")).isEmpty());
		assertTrue(classifier.classify(new IllegalStateException("x")).isEmpty());
	}

	@Test
	@DisplayName("기본 폴백에서 토큰 한도 메시지를 가진 비일시 오류는 TOKEN_LIMIT 이다")
	void tokenLimitViaKeyword() {
		EgovAiFallback fallback = EgovSpringAiFallbacks.defaults();
		assertEquals(EgovAiErrorType.TOKEN_LIMIT,
				fallback.classify(new NonTransientAiException("This model's maximum context length is 8192 tokens")));
	}

	@Test
	@DisplayName("기본 폴백에서 원인 체인 속 타임아웃은 TIMEOUT 이다")
	void timeoutViaCauseChain() {
		EgovAiFallback fallback = EgovSpringAiFallbacks.defaults();
		assertEquals(EgovAiErrorType.TIMEOUT,
				fallback.classify(new NonTransientAiException("call failed", new SocketTimeoutException("read timed out"))));
		assertEquals(EgovAiErrorType.TRANSIENT, fallback.classify(new TransientAiException("try again")));
	}
}
