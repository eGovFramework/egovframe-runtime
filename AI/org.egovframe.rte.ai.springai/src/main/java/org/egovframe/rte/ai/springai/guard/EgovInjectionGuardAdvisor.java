/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.ai.springai.guard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.egovframe.rte.ai.core.guard.EgovPromptInjectionGuard;
import org.egovframe.rte.ai.core.guard.GuardDecision;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import reactor.core.publisher.Flux;

/**
 * 프롬프트 인젝션 가드 어드바이저 — 요청의 마지막 사용자 메시지를 기반 모듈 {@link EgovPromptInjectionGuard} 로 판정한다.
 *
 * <ul>
 *   <li>차단 정책에서 탐지되면 체인(모델)을 부르지 않고 안내 문구 응답을 돌려주며, 응답 컨텍스트에 {@link #BLOCKED}{@code =true}
 *       를 넣는다. 매칭 패턴은 응답에 넣지 않는다(감사 채널에만 남는다 — 기반 모듈이 발신). 스트림도 안내 응답 1건으로 끝난다.</li>
 *   <li>기록 정책에서는 감사만 남고 체인이 정상 호출된다.</li>
 *   <li>기본 순서는 Spring AI 채팅 메모리 어드바이저({@link Advisor#DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER})보다 앞이라 차단된 질의는
 *       대화 메모리에 저장되지 않는다.</li>
 * </ul>
 *
 * <p>{@code BaseAdvisor} 의 before/after 는 체인 호출을 건너뛸 수 없어 {@link CallAdvisor}·{@link StreamAdvisor} 를 직접 구현한다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai — 샘플은 서비스 계층 직접 호출, 확장점 미사용)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovInjectionGuardAdvisor implements CallAdvisor, StreamAdvisor {

	/** 차단 시 응답 컨텍스트에 넣는 표식 키(값 {@code true}). */
	public static final String BLOCKED = "egov_guard_blocked";

	/** 기본 안내 문구. */
	public static final String DEFAULT_BLOCKED_MESSAGE = "요청을 처리할 수 없습니다. 질의에 허용되지 않는 지시가 포함되어 있습니다.";

	/** 기본 순서 — 채팅 메모리 어드바이저보다 앞. */
	public static final int DEFAULT_ORDER = Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER - 100;

	private final EgovPromptInjectionGuard guard;
	private final String blockedMessage;
	private final int order;

	private EgovInjectionGuardAdvisor(Builder builder) {
		this.guard = builder.guard;
		this.blockedMessage = builder.blockedMessage;
		this.order = builder.order;
	}

	/**
	 * 빌더.
	 *
	 * @param guard 기반 모듈 가드(정책·렉시콘 포함)
	 * @return 빌더
	 */
	public static Builder builder(EgovPromptInjectionGuard guard) {
		return new Builder(guard);
	}

	/**
	 * 기본 옵션의 어드바이저.
	 *
	 * @param guard 기반 모듈 가드
	 * @return 어드바이저
	 */
	public static EgovInjectionGuardAdvisor of(EgovPromptInjectionGuard guard) {
		return builder(guard).build();
	}

	/** 위임 가드. */
	public EgovPromptInjectionGuard guard() {
		return guard;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
		GuardDecision decision = inspect(request);
		if (!decision.allowed()) {
			return blockedResponse(request);
		}
		return chain.nextCall(request);
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
		GuardDecision decision = inspect(request);
		if (!decision.allowed()) {
			return Flux.just(blockedResponse(request));
		}
		return chain.nextStream(request);
	}

	/**
	 * 요청의 마지막 사용자 메시지를 판정한다(사용자 메시지가 없으면 허용).
	 *
	 * @param request 요청
	 * @return 판정
	 */
	public GuardDecision inspect(ChatClientRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		UserMessage userMessage = request.prompt() == null ? null : request.prompt().getUserMessage();
		String text = userMessage == null ? null : userMessage.getText();
		return guard.inspect(text);
	}

	private ChatClientResponse blockedResponse(ChatClientRequest request) {
		ChatResponse chatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage(blockedMessage))));
		Map<String, Object> context = new HashMap<>(request.context() == null ? Map.of() : request.context());
		context.put(BLOCKED, Boolean.TRUE);
		return ChatClientResponse.builder().chatResponse(chatResponse).context(context).build();
	}

	/** {@link EgovInjectionGuardAdvisor} 빌더. */
	public static final class Builder {
		private final EgovPromptInjectionGuard guard;
		private String blockedMessage = DEFAULT_BLOCKED_MESSAGE;
		private int order = DEFAULT_ORDER;

		private Builder(EgovPromptInjectionGuard guard) {
			this.guard = Objects.requireNonNull(guard, "guard must not be null");
		}

		/** 차단 시 안내 문구(비어 있지 않아야 함). */
		public Builder blockedMessage(String blockedMessage) {
			if (blockedMessage == null || blockedMessage.isBlank()) {
				throw new IllegalArgumentException("blockedMessage 는 비어 있을 수 없습니다");
			}
			this.blockedMessage = blockedMessage;
			return this;
		}

		/** 어드바이저 순서(작을수록 앞, 기본 {@link #DEFAULT_ORDER}). */
		public Builder order(int order) {
			this.order = order;
			return this;
		}

		public EgovInjectionGuardAdvisor build() {
			return new EgovInjectionGuardAdvisor(this);
		}
	}
}
