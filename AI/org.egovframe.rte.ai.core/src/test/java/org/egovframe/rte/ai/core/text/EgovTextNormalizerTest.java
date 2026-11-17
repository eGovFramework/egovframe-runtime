package org.egovframe.rte.ai.core.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 정규화 계약 테스트 — 기본은 글자를 지우지 않는다(참고 샘플이 한자·공문서 표기를 지우던 결함의 회귀 고정),
 * 선택 규칙은 각각 켜고 끌 수 있으며 빈 줄 하나는 보존된다(스펙 ai/text-normalization).
 */
class EgovTextNormalizerTest {

	private static final String OFFICIAL = "행정·재정 지원 ※ 2026.9.3.~9.5. 「전자정부법」 제2조(定義) ○ 대상 △ 제외 ‘인용’ “인용” 第1條 — … ~";

	@Test
	@DisplayName("기본 정규화는 한자·가운뎃점·물결·낫표·특수기호를 보존한다(회귀 고정)")
	void defaultPreservesOfficialNotation() {
		EgovNormalized result = EgovTextNormalizer.DEFAULT.normalize(OFFICIAL);
		assertEquals(OFFICIAL, result.text());
		assertFalse(result.changed());
		assertSame(OFFICIAL, result.text());
		assertEquals(OFFICIAL.length(), result.originalLength());
	}

	@Test
	@DisplayName("제어 문자와 유니코드 서식 문자만 제거한다(탭·개행은 보존)")
	void removesControlAndFormatOnly() {
		EgovNormalized result = EgovTextNormalizer.DEFAULT.normalize("가​나다﻿\t라\n마");
		assertEquals("가나다\t라\n마", result.text());
		assertTrue(result.changed());
	}

	@Test
	@DisplayName("HTML 태그 제거는 선택이다")
	void stripsHtmlTagsWhenEnabled() {
		assertEquals("공고", EgovTextNormalizer.builder().stripHtmlTags(true).build().normalize("<p>공고</p>").text());
		assertEquals("<p>공고</p>", EgovTextNormalizer.DEFAULT.normalize("<p>공고</p>").text());
	}

	@Test
	@DisplayName("공백 축약은 연속 스페이스·탭만 줄이고 개행을 지우지 않는다")
	void collapsesWhitespaceButKeepsNewlines() {
		EgovTextNormalizer normalizer = EgovTextNormalizer.builder().collapseWhitespace(true).build();
		assertEquals("가 나\n다", normalizer.normalize("가    나\n다").text());
		assertEquals("가 나 다\n라 마 바", normalizer.normalize("가  나\t\t다\n라   마\t바").text());
	}

	@Test
	@DisplayName("줄바꿈 정규화는 CRLF→LF 로 통일하고 빈 줄 하나는 보존한다")
	void normalizesNewlinesPreservingOneBlankLine() {
		EgovTextNormalizer normalizer = EgovTextNormalizer.builder().normalizeNewlines(true).build();
		assertEquals("문단1\n\n문단2", normalizer.normalize("문단1\r\n\r\n\r\n\r\n문단2").text());
		EgovNormalized single = normalizer.normalize("가\n\n나");
		assertEquals("가\n\n나", single.text());
		assertFalse(single.changed());
	}

	@Test
	@DisplayName("호출 측 제거 패턴과 코드 블록 제거는 명시했을 때만 동작한다")
	void customRemovalAndCodeBlocks() {
		EgovTextNormalizer normalizer = EgovTextNormalizer.builder().removePattern(Pattern.compile("\\[광고\\]")).build();
		assertEquals("공지 본문", normalizer.normalize("[광고]공지 본문").text());
		EgovTextNormalizer code = EgovTextNormalizer.builder().removeCodeBlocks(true).build();
		assertEquals("앞\n\n뒤", code.normalize("앞\n```java\nint a;\n```\n뒤").text());
	}

	@Test
	@DisplayName("null·빈 입력은 변경 없음이다")
	void nullAndEmpty() {
		assertFalse(EgovTextNormalizer.DEFAULT.normalize(null).changed());
		assertFalse(EgovTextNormalizer.DEFAULT.normalize("").changed());
		assertEquals(0, EgovTextNormalizer.DEFAULT.normalize(null).length());
	}
}
