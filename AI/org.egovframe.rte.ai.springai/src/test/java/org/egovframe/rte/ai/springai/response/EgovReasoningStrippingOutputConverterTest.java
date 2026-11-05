package org.egovframe.rte.ai.springai.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * 추론 태그 제거 출력 변환기 계약 테스트(스펙 ai/springai-response-handling 3항).
 */
class EgovReasoningStrippingOutputConverterTest {

	/** 변환 대상 빈. */
	public static class Person {
		private String name;
		private int age;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}

	@Test
	@DisplayName("추론 태그와 설명이 섞인 JSON 은 빈으로 변환된다")
	void convertsBeanAfterStripping() {
		Person person = EgovReasoningStrippingOutputConverter.forBean(Person.class)
				.convert("<think>이름과 나이를 정리한다</think>결과입니다: {\"name\":\"홍길동\",\"age\":30} 이상입니다.");

		assertEquals("홍길동", person.getName());
		assertEquals(30, person.getAge());
	}

	@Test
	@DisplayName("형식 지시문은 위임 변환기의 것과 같다")
	void delegatesFormat() {
		BeanOutputConverter<Person> delegate = new BeanOutputConverter<>(Person.class);

		assertEquals(delegate.getFormat(), EgovReasoningStrippingOutputConverter.wrap(delegate, true).getFormat());
	}

	@Test
	@DisplayName("JSON 이 없는 출력은 위임 변환기의 예외가 그대로 전파된다")
	void propagatesDelegateFailure() {
		EgovReasoningStrippingOutputConverter<Person> converter = EgovReasoningStrippingOutputConverter.forBean(Person.class);

		assertThrows(RuntimeException.class, () -> converter.convert("<think>x</think>JSON 없는 답변"));
	}

	@Test
	@DisplayName("JSON 추출을 끄면 정리한 텍스트 전체가 위임된다(목록 변환기)")
	void listConverterWithoutJsonExtraction() {
		List<String> items = EgovReasoningStrippingOutputConverter
				.wrap(new ListOutputConverter(new DefaultConversionService()), false)
				.convert("<think>세 항목</think>사과, 배, 감");

		assertEquals(List.of("사과", "배", "감"), items);
	}

	@Test
	@DisplayName("맵 변환기에도 JSON 추출이 적용된다")
	void mapConverterWithJsonExtraction() {
		Map<String, Object> map = EgovReasoningStrippingOutputConverter.wrap(new MapOutputConverter(), true)
				.convert("설명: {\"key\":\"값\"}");

		assertEquals("값", map.get("key"));
	}
}
