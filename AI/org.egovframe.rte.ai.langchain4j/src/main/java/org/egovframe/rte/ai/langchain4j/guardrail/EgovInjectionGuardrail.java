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
package org.egovframe.rte.ai.langchain4j.guardrail;

import java.util.Objects;

import org.egovframe.rte.ai.core.guard.EgovPromptInjectionGuard;
import org.egovframe.rte.ai.core.guard.GuardDecision;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

/**
 * 프롬프트 인젝션 가드레일 — LangChain4j {@link InputGuardrail} 로 사용자 메시지를 기반 모듈 {@link EgovPromptInjectionGuard} 에 판정시킨다.
 *
 * <p>차단 정책에서 탐지되면 안내 문구를 담은 {@code fatal} 결과를 돌려준다 — {@code AiServices} 는 모델을 부르지 않고
 * {@code InputGuardrailException}(메시지에 안내 문구 포함)으로 호출을 중단하므로 앱은 그 예외를 잡아 안내 문구를 응답한다(Spring AI 어댑터의
 * 안내 응답과 달리 예외 경로). 기록 정책은 {@code success} 로 통과시키고 탐지 사실은 기반 모듈이 감사 채널에 남긴다. 매칭 패턴은 결과에 넣지
 * 않는다. {@code failure} 는 다른 가드레일까지 돌린 뒤 중단이라 쓰지 않는다.</p>
 *
 * <pre>
 *   AiServices.builder(Assistant.class).chatModel(model)
 *       .inputGuardrails(List.of(EgovInjectionGuardrail.of(EgovPromptInjectionGuard.of(GuardPolicy.BLOCK, EgovInjectionLexicon.seed()))))
 *       .build();
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j — 샘플은 서비스 계층 직접 호출, 확장점 미사용)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovInjectionGuardrail implements InputGuardrail {

	/** 기본 안내 문구. */
	public static final String DEFAULT_BLOCKED_MESSAGE = "요청을 처리할 수 없습니다. 질의에 허용되지 않는 지시가 포함되어 있습니다.";

	private final EgovPromptInjectionGuard guard;
	private final String blockedMessage;

	private EgovInjectionGuardrail(Builder builder) {
		this.guard = builder.guard;
		this.blockedMessage = builder.blockedMessage;
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
	 * 기본 안내 문구의 가드레일.
	 *
	 * @param guard 기반 모듈 가드
	 * @return 가드레일
	 */
	public static EgovInjectionGuardrail of(EgovPromptInjectionGuard guard) {
		return builder(guard).build();
	}

	/** 위임 가드. */
	public EgovPromptInjectionGuard guard() {
		return guard;
	}

	/** 차단 시 안내 문구. */
	public String blockedMessage() {
		return blockedMessage;
	}

	@Override
	public InputGuardrailResult validate(UserMessage userMessage) {
		GuardDecision decision = inspect(userMessage);
		if (!decision.allowed()) {
			return fatal(blockedMessage);
		}
		return success();
	}

	/**
	 * 사용자 메시지의 텍스트를 판정한다(텍스트가 없으면 허용).
	 *
	 * @param userMessage 사용자 메시지(null 허용)
	 * @return 판정
	 */
	public GuardDecision inspect(UserMessage userMessage) {
		return guard.inspect(textOf(userMessage));
	}

	/**
	 * 사용자 메시지의 텍스트 — 단일 텍스트면 그대로, 복수 콘텐츠면 텍스트 콘텐츠를 줄바꿈으로 이어 붙인다.
	 *
	 * @param userMessage 사용자 메시지(null 이면 null)
	 * @return 텍스트
	 */
	static String textOf(UserMessage userMessage) {
		if (userMessage == null) {
			return null;
		}
		if (userMessage.hasSingleText()) {
			return userMessage.singleText();
		}
		StringBuilder text = new StringBuilder();
		for (Content content : userMessage.contents()) {
			if (content instanceof TextContent textContent) {
				if (text.length() > 0) {
					text.append('\n');
				}
				text.append(textContent.text());
			}
		}
		return text.toString();
	}

	/** {@link EgovInjectionGuardrail} 빌더. */
	public static final class Builder {
		private final EgovPromptInjectionGuard guard;
		private String blockedMessage = DEFAULT_BLOCKED_MESSAGE;

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

		public EgovInjectionGuardrail build() {
			return new EgovInjectionGuardrail(this);
		}
	}
}
