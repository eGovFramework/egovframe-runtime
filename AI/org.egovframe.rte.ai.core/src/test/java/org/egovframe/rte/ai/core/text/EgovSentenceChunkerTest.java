package org.egovframe.rte.ai.core.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 문장 단위 청킹 계약 테스트 — 문자 계산기와 가짜 토큰 계산기(어절 수)로 같은 규칙이 동작하는지, 초과 문장 창 분할·서로게이트·
 * 극단 한도·오버랩·한도 도달 표지를 검증한다(스펙 ai/korean-sentence-chunking 2~4항).
 */
class EgovSentenceChunkerTest {

	private static final ToIntFunction<String> CHARS = String::length;
	/** 가짜 토큰 계산기 — 공백 어절 수. */
	private static final ToIntFunction<String> WORDS = s -> s.isBlank() ? 0 : s.trim().split("\\s+").length;
	private static final Pattern SENTENCE_ENDING = Pattern.compile(".*([.!?。！？][\"'」』)\\]》]?|다|요|니다|습니다|한다|된다)$");

	private static EgovSentenceChunker chunker(ToIntFunction<String> sizer, int maxSize) {
		return EgovSentenceChunker.builder(sizer).maxSize(maxSize).build();
	}

	@Test
	@DisplayName("문자 계산기 — 청크가 한도 이내이고 문장 경계에서 끝나며 원문 부분문자열이다")
	void charSizerRespectsLimitAndBoundaries() {
		String text = "첫 문장입니다. 둘째 문장입니다. 셋째 문장입니다. 넷째 문장입니다. 다섯째 문장입니다.";
		EgovChunks chunks = chunker(CHARS, 20).chunk(text);
		assertFalse(chunks.isEmpty());
		assertFalse(chunks.truncated());
		for (String chunk : chunks.texts()) {
			assertTrue(chunk.length() <= 20, chunk);
			assertTrue(SENTENCE_ENDING.matcher(chunk).matches(), chunk);
			assertTrue(text.contains(chunk));
		}
		assertEquals(chunks.texts(), chunker(CHARS, 20).chunk(text).texts(), "결정적");
	}

	@Test
	@DisplayName("토큰(어절) 계산기 — 같은 규칙으로 한도 이내 청크를 만든다")
	void tokenSizerRespectsLimit() {
		String text = "첫 번째 문장입니다. 두 번째 문장입니다! 세 번째 문장입니다? 마지막 문장입니다.";
		EgovChunks chunks = chunker(WORDS, 4).chunk(text);
		assertFalse(chunks.isEmpty());
		for (String chunk : chunks.texts()) {
			assertTrue(WORDS.applyAsInt(chunk) <= 4, chunk);
			assertTrue(SENTENCE_ENDING.matcher(chunk).matches(), chunk);
			assertTrue(text.contains(chunk));
		}
	}

	@Test
	@DisplayName("한도를 넘는 한 문장은 원문 창으로 잘려 각 청크가 한도 이내·부분문자열이고 이어 붙이면 원문이 된다")
	void oversizedSentenceIsWindowed() {
		String text = "전자정부표준프레임워크문서청킹검증".repeat(80);
		EgovChunks chunks = chunker(CHARS, 30).chunk(text);
		assertTrue(chunks.size() > 1);
		StringBuilder rebuilt = new StringBuilder();
		for (String chunk : chunks.texts()) {
			assertTrue(chunk.length() <= 30);
			assertTrue(text.contains(chunk));
			rebuilt.append(chunk);
		}
		assertEquals(text, rebuilt.toString());
	}

	@Test
	@DisplayName("긴 문장은 가능하면 공백에서 잘린다")
	void oversizedSentenceBacksOffToWhitespace() {
		String text = "가나다 라마바 사아자 차카타 파하 " .repeat(20).trim();
		EgovChunks chunks = chunker(CHARS, 12).chunk(text);
		for (String chunk : chunks.texts()) {
			assertTrue(chunk.length() <= 12, chunk);
			assertFalse(chunk.startsWith(" ") || chunk.endsWith(" "));
			assertTrue(text.contains(chunk));
		}
	}

	@Test
	@DisplayName("보충면 문자를 코드포인트 경계에서 잘라 짝 잃은 서로게이트를 남기지 않는다")
	void doesNotSplitSurrogatePairs() {
		String text = "𠀋𠮟".repeat(120);
		EgovChunks chunks = chunker(CHARS, 41).chunk(text);
		assertFalse(chunks.isEmpty());
		for (String chunk : chunks.texts()) {
			assertTrue(text.contains(chunk));
			assertFalse(hasUnpairedSurrogate(chunk), chunk);
			assertTrue(chunk.length() <= 41);
		}
	}

	@Test
	@DisplayName("보충면 문자와 극단 한도(1·2·3) 조합에서도 유한 시간에 끝나고 최소 코드포인트 하나씩 진행한다")
	void terminatesWithTinyLimit() {
		String text = "𠀋𠮟".repeat(20);
		for (int limit : new int[] { 1, 2, 3 }) {
			assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
				EgovChunks chunks = chunker(CHARS, limit).chunk(text);
				assertFalse(chunks.isEmpty());
				for (String chunk : chunks.texts()) {
					assertTrue(text.contains(chunk));
					assertFalse(hasUnpairedSurrogate(chunk));
				}
			});
		}
		// 한도 1: 보충면 문자 하나(UTF-16 2자)가 한도를 넘지만 코드포인트 하나를 그대로 내보낸다
		EgovChunks one = chunker(CHARS, 1).chunk(text);
		for (String chunk : one.texts()) {
			assertEquals(1, chunk.codePointCount(0, chunk.length()));
		}
	}

	@Test
	@DisplayName("최대 청크 수에 도달하면 중단하고 한도 도달 표지가 참이다")
	void truncatedWhenLimitReached() {
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= 20; i++) {
			sb.append(i).append("번째 문장이다. ");
		}
		EgovChunks limited = EgovSentenceChunker.builder(CHARS).maxSize(12).maxChunks(5).build().chunk(sb.toString());
		assertEquals(5, limited.size());
		assertTrue(limited.truncated());
		EgovChunks all = EgovSentenceChunker.builder(CHARS).maxSize(12).maxChunks(100).build().chunk(sb.toString());
		assertEquals(20, all.size());
		assertFalse(all.truncated());
	}

	@Test
	@DisplayName("최소 길이 미만 청크는 제외된다")
	void filtersShortChunks() {
		EgovChunks chunks = EgovSentenceChunker.builder(CHARS).maxSize(30).minLength(50).build().chunk("짧습니다. 작습니다.");
		assertTrue(chunks.isEmpty());
		assertFalse(chunks.truncated());
	}

	@Test
	@DisplayName("오버랩을 지정하면 인접 청크가 앞 청크의 끝 문장을 공유하고 여전히 원문 부분문자열이다")
	void overlapSharesTrailingSentence() {
		String text = "하나입니다. 둘입니다. 셋입니다. 넷입니다. 다섯입니다. 여섯입니다.";
		EgovChunks chunks = EgovSentenceChunker.builder(CHARS).maxSize(14).overlap(7).build().chunk(text);
		List<String> texts = chunks.texts();
		assertTrue(texts.size() > 1);
		for (int i = 1; i < texts.size(); i++) {
			String previousLast = lastSentence(texts.get(i - 1));
			assertTrue(texts.get(i).startsWith(previousLast), texts.get(i - 1) + " -> " + texts.get(i));
			assertTrue(text.contains(texts.get(i)));
		}
	}

	@Test
	@DisplayName("오버랩이 한도의 절반을 넘거나 한도가 0 이하면 생성이 거부된다")
	void builderValidation() {
		assertThrows(IllegalArgumentException.class, () -> EgovSentenceChunker.builder(CHARS).maxSize(400).overlap(300).build());
		assertThrows(IllegalArgumentException.class, () -> EgovSentenceChunker.builder(CHARS).maxSize(0).build());
		assertThrows(IllegalArgumentException.class, () -> EgovSentenceChunker.builder(CHARS).maxSize(10).maxChunks(0).build());
	}

	@Test
	@DisplayName("빈 입력은 빈 결과이고 큰 한도에서는 마크다운 구조가 한 청크에 보존된다")
	void blankInputAndLargeLimit() {
		assertTrue(chunker(CHARS, 30).chunk(null).isEmpty());
		assertTrue(chunker(CHARS, 30).chunk(" \n\t ").isEmpty());
		String markdown = "# 처리 기준\n\n## 상세 기준\n\n담당자는 \"즉시 조치한다.\"라고 말했다.\n\n| 항목 | 기본값 |\n| --- | --- |\n| chunk | 500 |\n\n```java\n// 주석. 끝\nint value = 1;\n```\n\n마지막 문장입니다.";
		EgovChunks chunks = chunker(CHARS, 5000).chunk(markdown);
		assertEquals(1, chunks.size());
		String chunk = chunks.texts().get(0);
		assertTrue(chunk.contains("# 처리 기준"));
		assertTrue(chunk.contains("\n| 항목 | 기본값 |"));
		assertTrue(chunk.contains("```java\n// 주석. 끝\nint value = 1;\n```"));
		assertTrue(markdown.contains(chunk));
	}

	private static String lastSentence(String chunk) {
		List<String> sentences = EgovKoreanSentences.split(chunk);
		return sentences.get(sentences.size() - 1);
	}

	private static boolean hasUnpairedSurrogate(String text) {
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (Character.isHighSurrogate(ch)) {
				if (i + 1 >= text.length() || !Character.isLowSurrogate(text.charAt(i + 1))) {
					return true;
				}
				i++;
			} else if (Character.isLowSurrogate(ch)) {
				return true;
			}
		}
		return false;
	}
}
