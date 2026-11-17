package org.egovframe.rte.ai.springai.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.egovframe.rte.ai.core.response.EgovAiErrorType;
import org.egovframe.rte.ai.core.response.EgovAiFallback;
import org.egovframe.rte.ai.springai.support.LogCapture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.retry.TransientAiException;

/**
 * 기본 폴백 팩토리 테스트 — 기본 안내 문구·감사 발신(기반 모듈)·빌더 덧붙이기(스펙 ai/springai-response-handling 2항).
 */
class EgovSpringAiFallbacksTest {

	@Test
	@DisplayName("기본 폴백은 TRANSIENT 안내 문구를 돌려주고 감사에 call_failure 가 남는다")
	void defaultsClassifyAndAudit() {
		EgovAiFallback fallback = EgovSpringAiFallbacks.defaults();

		try (LogCapture audit = LogCapture.audit()) {
			String message = fallback.message(new TransientAiException("try again"));

			assertEquals(EgovAiErrorType.TRANSIENT.defaultMessage(), message);
			assertEquals(1, audit.lines().size(), audit.lines().toString());
			assertTrue(audit.lines().get(0).contains("event=call-failure"), audit.lines().get(0));
			assertTrue(audit.lines().get(0).contains("type=TRANSIENT"), audit.lines().get(0));
		}
	}

	@Test
	@DisplayName("빌더로 사용자 메시지를 덧붙일 수 있다")
	void builderCustomizesMessages() {
		EgovAiFallback fallback = EgovSpringAiFallbacks.builder()
				.message(EgovAiErrorType.TRANSIENT, "잠시 후 다시 시도해 주세요(사용자 문구)").build();

		assertEquals("잠시 후 다시 시도해 주세요(사용자 문구)", fallback.message(EgovAiErrorType.TRANSIENT));
		assertEquals(EgovAiErrorType.TRANSIENT, fallback.classify(new TransientAiException("x")));
	}
}
