package org.egovframe.rte.ai.springai.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.ToIntFunction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;

/**
 * 크기 계산기 테스트 — 기본은 cl100k 토큰(스펙 ai/springai-adapter 3항).
 */
class EgovTokenSizersTest {

	@Test
	@DisplayName("기본 계산기는 Spring AI JTokkit 추정기(cl100k)와 같은 값을 돌려준다")
	void cl100kMatchesSpringAiEstimator() {
		String text = "전자정부 표준프레임워크 실행환경";
		ToIntFunction<String> sizer = EgovTokenSizers.cl100k();

		assertEquals(new JTokkitTokenCountEstimator().estimate(text), sizer.applyAsInt(text));
		assertTrue(sizer.applyAsInt(text) > 0);
		assertEquals(0, sizer.applyAsInt(null));
		assertEquals(0, sizer.applyAsInt(""));
	}

	@Test
	@DisplayName("추정기 래핑과 문자 수 계산기")
	void wrappersAndChars() {
		assertEquals(new JTokkitTokenCountEstimator().estimate("페이징"), EgovTokenSizers.of(new JTokkitTokenCountEstimator()).applyAsInt("페이징"));
		assertEquals(3, EgovTokenSizers.chars().applyAsInt("페이징"));
		assertEquals(0, EgovTokenSizers.chars().applyAsInt(null));
	}
}
