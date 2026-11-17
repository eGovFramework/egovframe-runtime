package org.egovframe.rte.ai.langchain4j.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.TokenCountEstimator;

/**
 * 메타데이터 변환 규칙·크기 계산기 테스트(스펙 ai/langchain4j-adapter 3·4항).
 */
class EgovLangChain4jMetadataTest {

	@Test
	@DisplayName("Boolean 은 문자열, Integer 는 int, String 은 그대로 — 키를 버리지 않는다")
	void convertsValueTypes() {
		Map<String, Object> source = new LinkedHashMap<>();
		source.put("has_headers", Boolean.TRUE);
		source.put("page_number", 3);
		source.put("file_name", "a.md");
		source.put("size", 12L);
		source.put("ratio", 0.5d);
		source.put("nothing", null);

		Metadata metadata = EgovLangChain4jMetadata.toMetadata(source);

		assertEquals("true", metadata.getString("has_headers"));
		assertEquals(3, metadata.getInteger("page_number"));
		assertEquals("a.md", metadata.getString("file_name"));
		assertEquals(12L, metadata.getLong("size"));
		assertEquals(0.5d, metadata.getDouble("ratio"));
		assertFalse(metadata.containsKey("nothing"));
		assertEquals(5, EgovLangChain4jMetadata.toMap(metadata).size());
		assertNull(EgovLangChain4jMetadata.toMetadata(null).getString("x"));
	}

	@Test
	@DisplayName("기본 계산기는 문자 수, LangChain4j 토큰 추정기는 감쌀 수 있다")
	void sizers() {
		assertEquals(4, EgovSegmentSizers.chars().applyAsInt("전자정부"));
		assertEquals(0, EgovSegmentSizers.chars().applyAsInt(null));

		ToIntFunction<String> half = EgovSegmentSizers.of(new TokenCountEstimator() {
			@Override
			public int estimateTokenCountInText(String text) {
				return text.length() / 2;
			}

			@Override
			public int estimateTokenCountInMessage(ChatMessage message) {
				return 0;
			}

			@Override
			public int estimateTokenCountInMessages(Iterable<ChatMessage> messages) {
				return 0;
			}
		});
		assertEquals(2, half.applyAsInt("전자정부"));
		assertEquals(0, half.applyAsInt(""));
	}
}
