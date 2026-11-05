package org.egovframe.rte.ai.langchain4j.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.egovframe.rte.ai.core.response.EgovAiErrorType;
import org.egovframe.rte.ai.core.response.EgovAiFallback;
import org.egovframe.rte.ai.langchain4j.support.LogCapture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.langchain4j.exception.InternalServerException;
import dev.langchain4j.exception.InvalidRequestException;
import dev.langchain4j.exception.RateLimitException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.exception.UnresolvedModelServerException;

/**
 * LangChain4j 예외 분류기·폴백 팩토리 테스트(스펙 ai/langchain4j-response-handling).
 */
class EgovLangChain4jErrorClassifierTest {

	private final EgovLangChain4jErrorClassifier classifier = new EgovLangChain4jErrorClassifier();

	@Test
	@DisplayName("계층 확정 — Timeout→TIMEOUT, RateLimit·InternalServer→TRANSIENT, UnresolvedModelServer→CONNECTION")
	void hierarchy() {
		assertEquals(Optional.of(EgovAiErrorType.TIMEOUT), classifier.classify(new TimeoutException("timed out")));
		assertEquals(Optional.of(EgovAiErrorType.TRANSIENT), classifier.classify(new RateLimitException("429")));
		assertEquals(Optional.of(EgovAiErrorType.TRANSIENT), classifier.classify(new InternalServerException("500")));
		assertEquals(Optional.of(EgovAiErrorType.CONNECTION), classifier.classify(new UnresolvedModelServerException("no server")));
	}

	@Test
	@DisplayName("그 밖의 비재시도·비 LangChain4j 예외는 확정하지 않는다")
	void othersLeftToChain() {
		assertTrue(classifier.classify(new InvalidRequestException("bad request")).isEmpty());
		assertTrue(classifier.classify(new IllegalStateException("x")).isEmpty());
	}

	@Test
	@DisplayName("기본 폴백에서 토큰 한도 메시지를 가진 비재시도 오류는 TOKEN_LIMIT 이다")
	void tokenLimitViaKeyword() {
		assertEquals(EgovAiErrorType.TOKEN_LIMIT,
				EgovLangChain4jFallbacks.defaults().classify(new InvalidRequestException("This model's maximum context length is 8192 tokens")));
	}

	@Test
	@DisplayName("기본 폴백은 TRANSIENT 안내 문구를 돌려주고 감사에 call-failure 가 남으며 빌더로 문구를 바꿀 수 있다")
	void fallbackMessageAndAudit() {
		EgovAiFallback fallback = EgovLangChain4jFallbacks.defaults();

		try (LogCapture audit = LogCapture.audit()) {
			assertEquals(EgovAiErrorType.TRANSIENT.defaultMessage(), fallback.message(new RateLimitException("429")));
			assertEquals(1, audit.lines().size(), audit.lines().toString());
			assertTrue(audit.lines().get(0).contains("event=call-failure"), audit.lines().get(0));
			assertTrue(audit.lines().get(0).contains("type=TRANSIENT"), audit.lines().get(0));
		}

		EgovAiFallback custom = EgovLangChain4jFallbacks.builder().message(EgovAiErrorType.TIMEOUT, "지연 중입니다.").build();
		assertEquals("지연 중입니다.", custom.message(EgovAiErrorType.TIMEOUT));
	}
}
