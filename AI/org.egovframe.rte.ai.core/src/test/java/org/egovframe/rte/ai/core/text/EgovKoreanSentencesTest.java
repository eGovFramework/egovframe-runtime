package org.egovframe.rte.ai.core.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.egovframe.rte.ai.core.text.EgovKoreanSentences.Span;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 한국어 문장 경계 규칙 계약 테스트 — 참고 샘플(egovframe-ai-rag)의 검증 케이스를 이식해 같은 입력에 같은 경계가 나오는지
 * 실측한다(스펙 ai/korean-sentence-chunking 1항).
 */
class EgovKoreanSentencesTest {

	@Test
	@DisplayName("종결부호로 한국어 문장을 분리한다")
	void splitsByTerminators() {
		assertEquals(List.of("안녕하세요.", "반갑습니다!", "무엇일까요?"),
				EgovKoreanSentences.split("안녕하세요. 반갑습니다! 무엇일까요?"));
		assertEquals(List.of("회의는 3시에 시작한다.", "장소는 2층이다."),
				EgovKoreanSentences.split("회의는 3시에 시작한다. 장소는 2층이다."));
	}

	@Test
	@DisplayName("개행 없는 긴 줄에 종결부호가 수만 개 있어도 선형 시간에 끝난다 — 줄 시작부터 재스캔하던 O(n²) 결함(2026-09-07 4차 점검)")
	void longSingleLineIsLinear() {
		String hostile = "a! ".repeat(100_000) + "[열린 링크 (열린 괄호 b? " + "c! ".repeat(50_000);
		org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(java.time.Duration.ofSeconds(2),
				() -> assertTrue(EgovKoreanSentences.spans(hostile).size() > 1000));
	}

	@Test
	@DisplayName("날짜 표기·라틴 이니셜·소수점·약어의 마침표는 경계가 아니다")
	void keepsNonSentenceDots() {
		assertEquals(List.of("회의는 2026. 7. 30. 개최한다.", "참석자는 다음과 같다."),
				EgovKoreanSentences.split("회의는 2026. 7. 30. 개최한다. 참석자는 다음과 같다."));
		assertEquals(List.of("담당자는 A. B. 홍길동입니다.", "확인 바랍니다."),
				EgovKoreanSentences.split("담당자는 A. B. 홍길동입니다. 확인 바랍니다."));
		assertEquals(List.of("값은 3.14입니다.", "버전은 1.0.1입니다."),
				EgovKoreanSentences.split("값은 3.14입니다. 버전은 1.0.1입니다."));
		assertEquals(List.of("번호는 No. 1입니다.", "기타 etc. 항목입니다.", "예: e.g. 형식입니다.", "지역은 U.S. 기준입니다."),
				EgovKoreanSentences.split("번호는 No. 1입니다. 기타 etc. 항목입니다. 예: e.g. 형식입니다. 지역은 U.S. 기준입니다."));
		assertEquals(List.of("2026.9.3. 기준 3.5% 상승, Dr. Kim 참석"),
				EgovKoreanSentences.split("2026.9.3. 기준 3.5% 상승, Dr. Kim 참석"));
	}

	@Test
	@DisplayName("종결어미로 끝난 문장은 다음 문장이 같은 글자로 시작해도 분리한다")
	void splitsBeforeWordStartingWithEndingSyllable() {
		assertEquals(List.of("보고서를 제출한다.", "하고자 하는 업무는 별도로 정한다."),
				EgovKoreanSentences.split("보고서를 제출한다. 하고자 하는 업무는 별도로 정한다."));
	}

	@Test
	@DisplayName("목록 번호·법령 조항의 마침표는 경계로 보지 않는다")
	void keepsNumberedListMarkers() {
		String text = "1. 항목입니다.\n  - 2. 하위 항목입니다.\n제1. 서수 항목입니다.\n"
				+ "이 규정은 제3조제1항. 다음 조항을 따른다. 적용 범위는 제2조. 세부는 별표 참조. 총 금액은 100. 이는 부가세 별도다. 제2항. 설명입니다.";
		assertEquals(List.of(
				"1. 항목입니다.",
				"- 2. 하위 항목입니다.",
				"제1. 서수 항목입니다.",
				"이 규정은 제3조제1항. 다음 조항을 따른다.",
				"적용 범위는 제2조. 세부는 별표 참조. 총 금액은 100. 이는 부가세 별도다.",
				"제2항. 설명입니다."), EgovKoreanSentences.split(text));
	}

	@Test
	@DisplayName("연속 마침표는 하나의 경계만 만들고 빈 문장을 만들지 않는다")
	void terminatorRunMakesOneBoundary() {
		List<String> sentences = EgovKoreanSentences.split("끝났다... 다음");
		assertEquals(List.of("끝났다...", "다음"), sentences);
	}

	@Test
	@DisplayName("공백 없는 한국어 종결어미 뒤 문장을 분리한다")
	void splitsNoSpaceAfterKoreanEnding() {
		assertEquals(List.of("처리를 완료합니다.", "다음 문장입니다."),
				EgovKoreanSentences.split("처리를 완료합니다.다음 문장입니다."));
	}

	@Test
	@DisplayName("빈 줄은 경계이고 하드랩 단일 개행은 경계가 아니다")
	void blankLineIsBoundaryHardWrapIsNot() {
		assertEquals(List.of("첫 줄입니다\n이어지는 줄입니다.", "새 문단입니다."),
				EgovKoreanSentences.split("첫 줄입니다\n이어지는 줄입니다.\n\n새 문단입니다."));
	}

	@Test
	@DisplayName("마크다운 제목 줄은 본문과 분리한다")
	void markdownHeadingIsBoundary() {
		assertEquals(List.of("# 제목", "본문입니다."), EgovKoreanSentences.split("# 제목\n본문입니다."));
	}

	@Test
	@DisplayName("인용문 뒤 조사는 같은 문장으로 유지하고 실제 다음 문장은 분리한다")
	void keepsQuoteParticlesWithQuotedSentence() {
		assertEquals(List.of("담당자는 \"즉시 조치한다.\"라고 말했다.", "다음 담당자는 \"교체한다.\" 라며 보고했다."),
				EgovKoreanSentences.split("담당자는 \"즉시 조치한다.\"라고 말했다. 다음 담당자는 \"교체한다.\" 라며 보고했다."));
		assertEquals(List.of("\"곧 끝난다.\"라고 말했다."), EgovKoreanSentences.split("\"곧 끝난다.\"라고 말했다."));
	}

	@Test
	@DisplayName("코드펜스 내부 문장부호는 분리하지 않고 앞뒤 산문만 분리한다")
	void codeFenceIsSingleSpan() {
		String text = "앞 문장입니다.\n```java\n// 주석. 끝\nint value = 1;\n```\n뒤 문장입니다.";
		assertEquals(List.of("앞 문장입니다.", "```java\n// 주석. 끝\nint value = 1;\n```", "뒤 문장입니다."),
				EgovKoreanSentences.split(text));
	}

	@Test
	@DisplayName("영어 문장 끝 마침표는 경계가 아니고, 영어가 섞여도 빈 줄 경계는 적용된다")
	void englishSentenceDotIsNotBoundary() {
		String mixed = "Spring Boot provides auto-configuration. 이를 통해 개발자는 설정을 최소화할 수 있습니다.";
		assertEquals(List.of(mixed), EgovKoreanSentences.split(mixed));
		String english = "Spring Boot provides auto-configuration. It reduces boilerplate.";
		assertEquals(List.of(english), EgovKoreanSentences.split(english));
		assertEquals(List.of("Spring Boot provides auto-configuration.", "이를 통해 개발자는 설정을 최소화할 수 있습니다."),
				EgovKoreanSentences.split("Spring Boot provides auto-configuration.\n\n이를 통해 개발자는 설정을 최소화할 수 있습니다."));
	}

	@Test
	@DisplayName("구간은 오름차순·비중첩이며 구간 사이에는 공백만 남는다")
	void spansAreOrderedAndNonOverlapping() {
		String text = "  # 제목\n본문입니다.\n\n담당자는 \"즉시 조치한다.\"라고 말했다.\n```java\n// 주석. 끝\n```\n마지막입니다.  ";
		List<Span> spans = EgovKoreanSentences.spans(text);
		assertTrue(!spans.isEmpty());
		assertTrue(text.substring(0, spans.get(0).start()).isBlank());
		List<String> texts = new ArrayList<>();
		for (int i = 0; i < spans.size(); i++) {
			Span span = spans.get(i);
			assertTrue(span.start() < span.end());
			texts.add(span.text(text));
			if (i + 1 < spans.size()) {
				Span next = spans.get(i + 1);
				assertTrue(span.end() <= next.start());
				assertTrue(text.substring(span.end(), next.start()).isBlank());
			}
		}
		assertTrue(text.substring(spans.get(spans.size() - 1).end()).isBlank());
		assertEquals(texts, EgovKoreanSentences.split(text));
	}

	@Test
	@DisplayName("null 또는 공백 입력은 빈 목록이다")
	void blankInputIsEmpty() {
		assertEquals(List.of(), EgovKoreanSentences.split(null));
		assertEquals(List.of(), EgovKoreanSentences.split(" \n\t "));
		assertEquals(List.of(), EgovKoreanSentences.spans(null));
	}

	@Test
	@DisplayName("분리한 문장은 공백 제거 기준으로 원문을 무손실 재구성한다")
	void splitIsLosslessIgnoringWhitespace() {
		List<String> cases = List.of(
				"안녕하세요. 반갑습니다! 무엇일까요?",
				"값은 3.14입니다. 버전은 1.0.1입니다.",
				"1. 항목입니다.\n  - 2. 하위 항목입니다.\n제1. 서수 항목입니다.",
				"끝났다... 다음",
				"처리를 완료합니다.다음 문장입니다.",
				"첫 줄입니다\n이어지는 줄입니다.\n\n새 문단입니다.",
				"담당자는 \"즉시 조치한다.\"라고 말했다.",
				"앞 문장입니다.\n```java\n// 주석. 끝\nint value = 1;\n```\n뒤 문장입니다.",
				"행정·재정 지원 ※ 2026.9.3.~9.5. 「전자정부법」 제2조(定義) ○ 대상 △ 제외 ‘인용’ “인용”");
		for (String text : cases) {
			String rebuilt = String.join("", EgovKoreanSentences.split(text));
			assertEquals(text.replaceAll("\\s+", ""), rebuilt.replaceAll("\\s+", ""));
		}
	}
}
