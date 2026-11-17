package org.egovframe.rte.ai.springai.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.egovframe.rte.ai.springai.support.StubChatModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.rag.Query;

/**
 * 대화 맥락 질의 변환기 계약 테스트 — 이력 없으면 생략·추론 태그 제거·답변형 거부·실패 전파/대체·상태 명시
 * (스펙 ai/springai-hybrid-retrieval 3항).
 */
class EgovConversationQueryTransformerTest {

	private static final String FOLLOW_UP = "그 설정 방법은?";

	private static Query withHistory(String text) {
		return Query.builder().text(text).history(List.of(
				new UserMessage("전자정부 표준프레임워크의 페이징이 뭐야?"),
				new AssistantMessage("목록 조회 결과를 페이지 단위로 나누는 기능이다."))).build();
	}

	private static EgovConversationQueryTransformer transformer(StubChatModel model) {
		return EgovConversationQueryTransformer.builder(ChatClient.builder(model)).build();
	}

	@Test
	@DisplayName("이력이 없으면 모델을 부르지 않고 원 질의를 돌려주며 상태는 skipped 다")
	void skipsWithoutHistory() {
		StubChatModel model = new StubChatModel().reply("호출되면 안 됨");

		Query result = transformer(model).transform(new Query("페이징 설정 방법은?"));

		assertEquals("페이징 설정 방법은?", result.text());
		assertEquals(EgovConversationQueryTransformer.STATUS_SKIPPED, result.context().get(EgovConversationQueryTransformer.STATUS));
		assertEquals(0, model.calls());
	}

	@Test
	@DisplayName("첫 턴 — 이력이 현재 질의(사용자 메시지)뿐이면 모델을 부르지 않고 skipped")
	void skipsWhenHistoryIsOnlyCurrentQuery() {
		StubChatModel model = new StubChatModel().reply("호출되면 안 됨");
		Query query = Query.builder().text("민원 처리 기한은?").history(List.of(new UserMessage("민원 처리 기한은?"))).build();
		Query result = transformer(model).transform(query);
		assertEquals("민원 처리 기한은?", result.text());
		assertEquals(EgovConversationQueryTransformer.STATUS_SKIPPED, result.context().get(EgovConversationQueryTransformer.STATUS));
		assertEquals(0, model.calls(), "모델 미호출");
		assertTrue(EgovConversationQueryTransformer.hasPriorTurn(List.of(new UserMessage("이전 질문"), new UserMessage("민원 처리 기한은?")), "민원 처리 기한은?"));
		assertTrue(EgovConversationQueryTransformer.hasPriorTurn(List.of(new UserMessage("q"), new AssistantMessage("a")), "q"));
	}

	@Test
	@DisplayName("추론 태그가 섞인 압축 결과는 태그를 뗀 질의로 채택되고 상태는 applied 다")
	void appliesStrippedCompression() {
		StubChatModel model = new StubChatModel().reply("<think>맥락: 페이징</think>\n전자정부 표준프레임워크의 페이징 설정 방법");

		Query result = transformer(model).transform(withHistory(FOLLOW_UP));

		assertEquals("전자정부 표준프레임워크의 페이징 설정 방법", result.text());
		assertEquals(EgovConversationQueryTransformer.STATUS_APPLIED, result.context().get(EgovConversationQueryTransformer.STATUS));
		assertEquals(1, model.calls());
		String rendered = model.prompts().get(0).getUserMessage().getText();
		assertTrue(rendered.contains("페이징이 뭐야?"), "이력이 템플릿에 들어간다: " + rendered);
		assertTrue(rendered.contains(FOLLOW_UP), rendered);
		assertTrue(rendered.contains("독립 질문"), "한국어 기본 템플릿 사용");
		assertEquals(2, result.history().size(), "이력은 유지");
	}

	@Test
	@DisplayName("답변형(여러 줄 설명) 출력은 채택하지 않고 원 질의를 돌려주며 상태는 fallback 이다")
	void rejectsAnswerLikeOutput() {
		StubChatModel model = new StubChatModel().reply(
				"전자정부 표준프레임워크의 페이징은 목록을 나누는 기능입니다.\n\n"
				+ "PaginationInfo 에 현재 페이지와 크기를 설정합니다.\n\n"
				+ "자세한 내용은 가이드를 참고하세요.");

		Query result = transformer(model).transform(withHistory(FOLLOW_UP));

		assertEquals(FOLLOW_UP, result.text());
		assertEquals(EgovConversationQueryTransformer.STATUS_FALLBACK, result.context().get(EgovConversationQueryTransformer.STATUS));
	}

	@Test
	@DisplayName("빈 결과·과도한 길이도 fallback 이다")
	void rejectsEmptyAndOverlong() {
		Query empty = transformer(new StubChatModel().reply("<think>만</think>")).transform(withHistory(FOLLOW_UP));
		assertEquals(EgovConversationQueryTransformer.STATUS_FALLBACK, empty.context().get(EgovConversationQueryTransformer.STATUS));

		Query overlong = transformer(new StubChatModel().reply("가".repeat(300))).transform(withHistory(FOLLOW_UP));
		assertEquals(FOLLOW_UP, overlong.text());
		assertEquals(EgovConversationQueryTransformer.STATUS_FALLBACK, overlong.context().get(EgovConversationQueryTransformer.STATUS));
	}

	@Test
	@DisplayName("모델 호출 실패는 기본적으로 전파되고, 옵션을 켜면 원 질의 + fallback 으로 계속한다")
	void failurePropagatesUnlessOptedIn() {
		StubChatModel failing = new StubChatModel().failWith(new IllegalStateException("모델 다운"));

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> transformer(failing).transform(withHistory(FOLLOW_UP)));
		assertEquals("모델 다운", e.getMessage());

		Query result = EgovConversationQueryTransformer.builder(ChatClient.builder(failing)).fallbackOnFailure(true).build()
				.transform(withHistory(FOLLOW_UP));
		assertEquals(FOLLOW_UP, result.text());
		assertEquals(EgovConversationQueryTransformer.STATUS_FALLBACK, result.context().get(EgovConversationQueryTransformer.STATUS));
	}

	@Test
	@DisplayName("기본 템플릿은 UTF-8 리소스에서 읽히고 두 변수를 갖는다")
	void defaultTemplateLoads() {
		String template = EgovConversationQueryTransformer.defaultPromptTemplate().getTemplate();
		assertTrue(template.contains("{history}") && template.contains("{query}"), template);
		assertTrue(template.contains("독립된 질문"), "한글이 깨지지 않는다: " + template);
	}
}
