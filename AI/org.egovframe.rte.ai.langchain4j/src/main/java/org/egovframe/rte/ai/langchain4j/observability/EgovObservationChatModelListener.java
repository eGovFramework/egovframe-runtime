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
package org.egovframe.rte.ai.langchain4j.observability;

import java.util.Objects;

import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.output.TokenUsage;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

/**
 * 관측 브리지 — LangChain4j {@link ChatModelListener} 를 Micrometer Observation 으로 잇는다.
 *
 * <p>Spring AI 가 내장 Observation 으로 내는 것과 같은 이름({@value #OBSERVATION_NAME})·키({@code gen_ai.*})를 쓰므로 두 어댑터의 모델 호출이
 * 실행환경 관측 층(C-10 핸들러·OTLP)에 같은 모양으로 합류한다. 요청에서 Observation 을 시작해 리스너 컨텍스트의 {@code attributes()} 에 두고,
 * 응답에서 토큰·모델·종료 사유를 붙여 멈추며, 오류는 기록 후 멈춘다. 프롬프트·응답 본문은 어떤 키에도 싣지 않는다. 요청 없이 온 응답·오류
 * (attributes 에 Observation 이 없음)는 무시한다.</p>
 *
 * <p>micrometer 는 optional 의존이다 — Observation 코드는 중첩 {@link MicrometerBridge} 에 격리하고 생성 시 클래스 존재를 확인해, 라이브러리가 없으면
 * 좌표를 담은 예외를 던지고 다른 어댑터 클래스에는 영향을 주지 않는다. 그래서 등록소는 {@code Object} 로 받는다({@link #of(Object)}).</p>
 *
 * <pre>
 *   ChatModel model = OllamaChatModel.builder()...listeners(List.of(EgovObservationChatModelListener.of(observationRegistry))).build();
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j — 검토 3-2 "ChatModelListener → Observation 브리지")
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovObservationChatModelListener implements ChatModelListener {

	/** Observation 이름(Spring AI 와 동일). */
	public static final String OBSERVATION_NAME = "gen_ai.client.operation";

	/** 저카디널리티 키 — 작업 이름(값 {@value #OPERATION_CHAT}). */
	public static final String KEY_OPERATION_NAME = "gen_ai.operation.name";

	/** 저카디널리티 키 — 모델 제공자. */
	public static final String KEY_SYSTEM = "gen_ai.system";

	/** 저카디널리티 키 — 요청 모델. */
	public static final String KEY_REQUEST_MODEL = "gen_ai.request.model";

	/** 저카디널리티 키 — 응답 모델. */
	public static final String KEY_RESPONSE_MODEL = "gen_ai.response.model";

	/** 고카디널리티 키 — 입력 토큰. */
	public static final String KEY_USAGE_INPUT_TOKENS = "gen_ai.usage.input_tokens";

	/** 고카디널리티 키 — 출력 토큰. */
	public static final String KEY_USAGE_OUTPUT_TOKENS = "gen_ai.usage.output_tokens";

	/** 고카디널리티 키 — 종료 사유. */
	public static final String KEY_FINISH_REASONS = "gen_ai.response.finish_reasons";

	/** 작업 이름 값. */
	public static final String OPERATION_CHAT = "chat";

	/** 값이 없을 때의 표기. */
	public static final String UNKNOWN = "unknown";

	/** micrometer 좌표(부재 안내). */
	public static final String MICROMETER_COORDINATES = "io.micrometer:micrometer-observation";

	/** 리스너 컨텍스트 attributes 의 Observation 키. */
	static final String ATTRIBUTE_KEY = EgovObservationChatModelListener.class.getName() + ".observation";

	private static final String PROBE_CLASS = "io.micrometer.observation.ObservationRegistry";

	private final MicrometerBridge bridge;

	private EgovObservationChatModelListener(Object registry) {
		this.bridge = new MicrometerBridge(registry);
	}

	/**
	 * 브리지를 만든다.
	 *
	 * @param observationRegistry {@code io.micrometer.observation.ObservationRegistry}(시그니처에 micrometer 타입을 노출하지 않으려 Object)
	 * @return 리스너
	 * @throws IllegalStateException micrometer-observation 이 클래스패스에 없을 때(좌표 안내)
	 * @throws IllegalArgumentException 등록소 타입이 아닐 때
	 */
	public static EgovObservationChatModelListener of(Object observationRegistry) {
		Objects.requireNonNull(observationRegistry, "observationRegistry must not be null");
		requireMicrometer();
		return new EgovObservationChatModelListener(observationRegistry);
	}

	/**
	 * micrometer-observation 존재 확인.
	 *
	 * @throws IllegalStateException 없을 때
	 */
	public static void requireMicrometer() {
		try {
			Class.forName(PROBE_CLASS, false, EgovObservationChatModelListener.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("관측 브리지에는 " + MICROMETER_COORDINATES
					+ " 이 필요합니다(optional 의존 — 앱 pom 에 직접 선언하세요)", e);
		}
	}

	@Override
	public void onRequest(ChatModelRequestContext context) {
		bridge.start(context);
	}

	@Override
	public void onResponse(ChatModelResponseContext context) {
		bridge.stop(context);
	}

	@Override
	public void onError(ChatModelErrorContext context) {
		bridge.error(context);
	}

	/**
	 * 모델 제공자 표기 — Spring AI {@code AiProvider} 값과 맞춘다(같은 대시보드에서 같은 축).
	 *
	 * @param provider LangChain4j 제공자(null 이면 {@value #UNKNOWN})
	 * @return 표기
	 */
	public static String providerName(ModelProvider provider) {
		if (provider == null) {
			return UNKNOWN;
		}
		switch (provider) {
			case OLLAMA:
				return "ollama";
			case OPEN_AI:
				return "openai";
			case ANTHROPIC:
				return "anthropic";
			case MISTRAL_AI:
				return "mistral_ai";
			case AZURE_OPEN_AI:
				return "azure";
			case AMAZON_BEDROCK:
				return "bedrock_converse";
			case GOOGLE_VERTEX_AI_GEMINI:
				return "vertex_ai";
			default:
				return provider.name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	/** Observation 코드 격리 — micrometer 타입은 이 클래스만 참조한다. */
	static final class MicrometerBridge {
		private final ObservationRegistry registry;

		MicrometerBridge(Object registry) {
			if (!(registry instanceof ObservationRegistry observationRegistry)) {
				throw new IllegalArgumentException("observationRegistry 는 io.micrometer.observation.ObservationRegistry 여야 합니다: "
						+ registry.getClass().getName());
			}
			this.registry = observationRegistry;
		}

		void start(ChatModelRequestContext context) {
			ChatRequest request = context.chatRequest();
			String model = request == null || request.modelName() == null ? UNKNOWN : request.modelName();
			Observation observation = Observation.createNotStarted(OBSERVATION_NAME, registry)
					.contextualName(OPERATION_CHAT + " " + model)
					.lowCardinalityKeyValue(KEY_OPERATION_NAME, OPERATION_CHAT)
					.lowCardinalityKeyValue(KEY_SYSTEM, providerName(context.modelProvider()))
					.lowCardinalityKeyValue(KEY_REQUEST_MODEL, model)
					.start();
			context.attributes().put(ATTRIBUTE_KEY, observation);
		}

		void stop(ChatModelResponseContext context) {
			Object attribute = context.attributes().get(ATTRIBUTE_KEY);
			if (!(attribute instanceof Observation observation)) {
				return;
			}
			ChatResponse response = context.chatResponse();
			ChatResponseMetadata metadata = response == null ? null : response.metadata();
			if (metadata != null) {
				if (metadata.modelName() != null) {
					observation.lowCardinalityKeyValue(KEY_RESPONSE_MODEL, metadata.modelName());
				}
				TokenUsage usage = metadata.tokenUsage();
				if (usage != null) {
					if (usage.inputTokenCount() != null) {
						observation.highCardinalityKeyValue(KEY_USAGE_INPUT_TOKENS, String.valueOf(usage.inputTokenCount()));
					}
					if (usage.outputTokenCount() != null) {
						observation.highCardinalityKeyValue(KEY_USAGE_OUTPUT_TOKENS, String.valueOf(usage.outputTokenCount()));
					}
				}
				if (metadata.finishReason() != null) {
					observation.highCardinalityKeyValue(KEY_FINISH_REASONS, "[\"" + metadata.finishReason().name() + "\"]");
				}
			}
			context.attributes().remove(ATTRIBUTE_KEY);
			observation.stop();
		}

		void error(ChatModelErrorContext context) {
			Object attribute = context.attributes().get(ATTRIBUTE_KEY);
			if (!(attribute instanceof Observation observation)) {
				return;
			}
			if (context.error() != null) {
				observation.error(context.error());
			}
			context.attributes().remove(ATTRIBUTE_KEY);
			observation.stop();
		}
	}
}
