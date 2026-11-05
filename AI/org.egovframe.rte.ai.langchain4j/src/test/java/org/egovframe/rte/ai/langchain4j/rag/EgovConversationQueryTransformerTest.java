package org.egovframe.rte.ai.langchain4j.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.egovframe.rte.ai.langchain4j.support.StubChatModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.query.Metadata;
import dev.langchain4j.rag.query.Query;

/**
 * 대화 맥락 질의 변환기 계약 테스트 — 이력 없으면 생략·추론 태그 제거·답변형 거부·실패 전파/대체·상태 노출
 * (스펙 ai/langchain4j-hybrid-retrieval 3항).
 */
class EgovConversationQueryTransformerTest {

	private static final String FOLLOW_UP = "그 설정 방법은?";

	private static Query withHistory(String text) {
		return new Query(text, Metadata.from(UserMessage.from(text), "conv-1", List.of(
				UserMessage.from("전자정부 표준프레임워크의 페이징이 뭐야?"),
				AiMessage.from("목록 조회 결과를 페이지 단위로 나누는 기능이다."))));
	}

	private static EgovCompressedQuery transform(StubChatModel model, Query query) {
		return (EgovCompressedQuery) EgovConversationQueryTransformer.builder(model).build().transform(query).iterator().next();
	}

	@Test
	@DisplayName("이력이 없으면 모델을 부르지 않고 원 질의를 돌려주며 상태는 skipped 다")
	void skipsWithoutHistory() {
		StubChatModel model = new StubChatModel().reply("호출되면 안 됨");

		EgovCompressedQuery result = transform(model, new Query("페이징 설정 방법은?"));

		assertEquals("페이징 설정 방법은?", result.text());
		assertEquals(EgovConversationQueryTransformer.STATUS_SKIPPED, result.status());
		assertEquals(0, model.calls());
	}

	@Test
	@DisplayName("추론 태그가 섞인 압축 결과는 태그를 뗀 질의로 채택되고 상태는 applied, 원 질의를 함께 노출한다")
	void appliesStrippedCompression() {
		StubChatModel model = new StubChatModel().reply("<think>맥락: 페이징</think>\n전자정부 표준프레임워크의 페이징 설정 방법");

		EgovCompressedQuery result = transform(model, withHistory(FOLLOW_UP));

		assertEquals("전자정부 표준프레임워크의 페이징 설정 방법", result.text());
		assertEquals(EgovConversationQueryTransformer.STATUS_APPLIED, result.status());
		assertEquals(FOLLOW_UP, result.originalText());
		assertTrue(result.changed());
		assertEquals("conv-1", result.metadata().chatMemoryId(), "대화 id 는 메타데이터로 명시 전달");
		assertEquals(1, model.calls());
		String rendered = ((UserMessage) model.requests().get(0).messages().get(model.requests().get(0).messages().size() - 1)).singleText();
		assertTrue(rendered.contains("페이징이 뭐야?"), "이력이 템플릿에 들어간다: " + rendered);
		assertTrue(rendered.contains(FOLLOW_UP), rendered);
		assertTrue(rendered.contains("독립 질문"), "한국어 기본 템플릿 사용");
	}

	@Test
	@DisplayName("답변형(여러 줄 설명) 출력은 채택하지 않고 원 질의를 돌려주며 상태는 fallback 이다")
	void rejectsAnswerLikeOutput() {
		StubChatModel model = new StubChatModel().reply(
				"전자정부 표준프레임워크의 페이징은 목록을 나누는 기능입니다.\n\nPaginationInfo 에 현재 페이지와 크기를 설정합니다.\n\n자세한 내용은 가이드를 참고하세요.");

		EgovCompressedQuery result = transform(model, withHistory(FOLLOW_UP));

		assertEquals(FOLLOW_UP, result.text());
		assertEquals(EgovConversationQueryTransformer.STATUS_FALLBACK, result.status());
	}

	@Test
	@DisplayName("빈 결과·과도한 길이도 fallback 이다")
	void rejectsEmptyAndOverlong() {
		assertEquals(EgovConversationQueryTransformer.STATUS_FALLBACK, transform(new StubChatModel().reply("<think>만</think>"), withHistory(FOLLOW_UP)).status());
		EgovCompressedQuery overlong = transform(new StubChatModel().reply("가".repeat(300)), withHistory(FOLLOW_UP));
		assertEquals(FOLLOW_UP, overlong.text());
		assertEquals(EgovConversationQueryTransformer.STATUS_FALLBACK, overlong.status());
	}

	@Test
	@DisplayName("모델 호출 실패는 기본적으로 전파되고, 옵션을 켜면 원 질의 + fallback 으로 계속한다")
	void failurePropagatesUnlessOptedIn() {
		StubChatModel failing = new StubChatModel().failWith(new IllegalStateException("모델 다운"));

		assertEquals("모델 다운", assertThrows(IllegalStateException.class, () -> transform(failing, withHistory(FOLLOW_UP))).getMessage());

		EgovCompressedQuery result = (EgovCompressedQuery) EgovConversationQueryTransformer.builder(failing).fallbackOnFailure(true).build()
				.transform(withHistory(FOLLOW_UP)).iterator().next();
		assertEquals(FOLLOW_UP, result.text());
		assertEquals(EgovConversationQueryTransformer.STATUS_FALLBACK, result.status());
	}

	@Test
	@DisplayName("기본 템플릿은 UTF-8 리소스에서 읽히고 두 변수를 갖는다")
	void defaultTemplateLoads() {
		String template = EgovConversationQueryTransformer.defaultPromptTemplate().template();

		assertTrue(template.contains("{{chatMemory}}") && template.contains("{{query}}"), template);
		assertTrue(template.contains("독립된 질문"), "한글이 깨지지 않는다: " + template);
	}
}
