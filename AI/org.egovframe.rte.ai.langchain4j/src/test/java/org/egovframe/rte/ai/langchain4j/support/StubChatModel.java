package org.egovframe.rte.ai.langchain4j.support;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;

/**
 * 대본형 스텁 채팅 모델 — 예약한 응답을 순서대로 돌려주고 받은 요청·호출 횟수를 기록한다. 예외를 예약하면 호출 시 던지고, 리스너를
 * 등록하면 {@code ChatModel.chat} 기본 구현이 그 리스너를 부른다(관측 브리지 테스트).
 */
public final class StubChatModel implements ChatModel {

	public static final String DEFAULT_REPLY = "스텁 응답";
	public static final String MODEL_NAME = "stub-model";

	private final Deque<String> replies = new ArrayDeque<>();
	private final List<ChatRequest> requests = new ArrayList<>();
	private List<ChatModelListener> listeners = List.of();
	private RuntimeException failure;

	public StubChatModel reply(String... texts) {
		replies.addAll(List.of(texts));
		return this;
	}

	public StubChatModel failWith(RuntimeException exception) {
		this.failure = exception;
		return this;
	}

	public StubChatModel listeners(ChatModelListener... listeners) {
		this.listeners = List.of(listeners);
		return this;
	}

	public List<ChatRequest> requests() {
		return requests;
	}

	public int calls() {
		return requests.size();
	}

	@Override
	public ChatResponse doChat(ChatRequest request) {
		requests.add(request);
		if (failure != null) {
			throw failure;
		}
		String text = replies.isEmpty() ? DEFAULT_REPLY : replies.poll();
		return ChatResponse.builder()
				.aiMessage(AiMessage.from(text))
				.modelName(MODEL_NAME)
				.tokenUsage(new TokenUsage(12, 7))
				.finishReason(FinishReason.STOP)
				.build();
	}

	@Override
	public List<ChatModelListener> listeners() {
		return listeners;
	}

	@Override
	public ModelProvider provider() {
		return ModelProvider.OTHER;
	}
}
