package org.egovframe.rte.ai.core.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 모델 출력 정리 계약 테스트 — 사고 태그 제거·JSON 추출(스펙 ai/llm-response-handling 3항). 참고 샘플 케이스 이식.
 */
class EgovModelOutputsTest {

	@Test
	@DisplayName("null·공백 입력은 그대로 돌려준다")
	void nullAndBlank() {
		assertNull(EgovModelOutputs.extractJsonObject(null));
		assertEquals("   ", EgovModelOutputs.extractJsonObject("   "));
		assertEquals("", EgovModelOutputs.extractJsonObject(""));
	}

	@Test
	@DisplayName("사고 태그(대소문자·여러 줄)를 제거하고 JSON 객체만 추출한다")
	void stripsThinkAndExtractsJson() {
		assertEquals("{\"answer\":\"42\"}", EgovModelOutputs.extractJsonObject("<think>모델의 추론 과정</think>{\"answer\":\"42\"}"));
		assertEquals("{\"k\":2}", EgovModelOutputs.extractJsonObject("<THINK>reasoning</THINK>{\"k\":2}"));
		assertEquals("{\"x\":1}", EgovModelOutputs.extractJsonObject("<think>\n여러\n줄\n추론\n</think>\n{\"x\":1}"));
		assertEquals("{\"answer\":\"ok\"}", EgovModelOutputs.extractJsonObject("<think>생각</think>\n{\"answer\":\"ok\"} 끝"));
	}

	@Test
	@DisplayName("앞뒤 텍스트가 있어도 첫 { 부터 마지막 } 까지, 중첩 객체는 바깥 범위를 유지한다")
	void extractsOuterObject() {
		assertEquals("{\"a\":1}", EgovModelOutputs.extractJsonObject("여기 결과입니다: {\"a\":1} 끝"));
		assertEquals("{\"a\":{\"b\":1}}", EgovModelOutputs.extractJsonObject("{\"a\":{\"b\":1}}"));
	}

	@Test
	@DisplayName("중괄호가 없으면 태그를 제거한 원문(트림)을 돌려준다")
	void noBracePassthrough() {
		assertEquals("순수 텍스트", EgovModelOutputs.extractJsonObject("<think>x</think>  순수 텍스트  "));
		assertEquals("답변입니다.", EgovModelOutputs.stripReasoning("<think>생각</think>답변입니다."));
	}
}
