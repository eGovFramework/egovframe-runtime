package org.egovframe.rte.ai.springai.support;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import reactor.core.publisher.Flux;

/**
 * 대본형 스텁 채팅 모델 — 예약한 응답을 순서대로 돌려주고 받은 프롬프트와 호출 횟수를 기록한다. 예외를 예약하면 호출 시 던진다.
 */
public final class StubChatModel implements ChatModel {

	public static final String DEFAULT_REPLY = "스텁 응답";

	private final Deque<String> replies = new ArrayDeque<>();
	private final List<Prompt> prompts = new ArrayList<>();
	private RuntimeException failure;

	public StubChatModel reply(String... texts) {
		replies.addAll(List.of(texts));
		return this;
	}

	public StubChatModel failWith(RuntimeException exception) {
		this.failure = exception;
		return this;
	}

	public List<Prompt> prompts() {
		return prompts;
	}

	public int calls() {
		return prompts.size();
	}

	@Override
	public ChatResponse call(Prompt prompt) {
		prompts.add(prompt);
		if (failure != null) {
			throw failure;
		}
		String text = replies.isEmpty() ? DEFAULT_REPLY : replies.poll();
		return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {
		return Flux.just(call(prompt));
	}
}
