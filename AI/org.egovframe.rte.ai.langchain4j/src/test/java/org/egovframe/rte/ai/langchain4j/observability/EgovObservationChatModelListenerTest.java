package org.egovframe.rte.ai.langchain4j.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.egovframe.rte.ai.langchain4j.support.StubChatModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

/**
 * 관측 브리지 계약 테스트 — 이름·키·본문 미기록·오류·무요청 무시·부재 안내(스펙 ai/langchain4j-observability-bridge).
 */
class EgovObservationChatModelListenerTest {

	/** 시작·중지·오류를 기록하는 핸들러. */
	private static final class Capturing implements ObservationHandler<Observation.Context> {
		final List<Observation.Context> started = new ArrayList<>();
		final List<Observation.Context> stopped = new ArrayList<>();
		final List<Observation.Context> errored = new ArrayList<>();

		@Override
		public boolean supportsContext(Observation.Context context) {
			return true;
		}

		@Override
		public void onStart(Observation.Context context) {
			started.add(context);
		}

		@Override
		public void onStop(Observation.Context context) {
			stopped.add(context);
		}

		@Override
		public void onError(Observation.Context context) {
			errored.add(context);
		}
	}

	private static String low(Observation.Context context, String key) {
		KeyValue value = context.getLowCardinalityKeyValue(key);
		return value == null ? null : value.getValue();
	}

	private static String high(Observation.Context context, String key) {
		for (KeyValue value : context.getHighCardinalityKeyValues()) {
			if (value.getKey().equals(key)) {
				return value.getValue();
			}
		}
		return null;
	}

	private static ChatRequest request(String text) {
		return ChatRequest.builder().messages(UserMessage.from(text))
				.parameters(ChatRequestParameters.builder().modelName("stub-model").build()).build();
	}

	@Test
	@DisplayName("요청·응답 — gen_ai.client.operation 1건, 저·고카디널리티 키, 멈춤")
	void requestAndResponse() {
		ObservationRegistry registry = ObservationRegistry.create();
		Capturing handler = new Capturing();
		registry.observationConfig().observationHandler(handler);
		StubChatModel model = new StubChatModel().reply("응답").listeners(EgovObservationChatModelListener.of(registry));

		ChatResponse response = model.chat(request("페이징 설정 방법"));

		assertEquals("응답", response.aiMessage().text());
		assertEquals(1, handler.started.size());
		assertEquals(1, handler.stopped.size());
		Observation.Context context = handler.stopped.get(0);
		assertEquals(EgovObservationChatModelListener.OBSERVATION_NAME, context.getName());
		assertEquals("chat stub-model", context.getContextualName());
		assertEquals("chat", low(context, EgovObservationChatModelListener.KEY_OPERATION_NAME));
		assertEquals("other", low(context, EgovObservationChatModelListener.KEY_SYSTEM));
		assertEquals("stub-model", low(context, EgovObservationChatModelListener.KEY_REQUEST_MODEL));
		assertEquals("stub-model", low(context, EgovObservationChatModelListener.KEY_RESPONSE_MODEL));
		assertEquals("12", high(context, EgovObservationChatModelListener.KEY_USAGE_INPUT_TOKENS));
		assertEquals("7", high(context, EgovObservationChatModelListener.KEY_USAGE_OUTPUT_TOKENS));
		assertEquals("[\"STOP\"]", high(context, EgovObservationChatModelListener.KEY_FINISH_REASONS));
	}

	@Test
	@DisplayName("본문 미기록 — 어떤 키 값에도 프롬프트·응답 텍스트가 없다")
	void noContentInKeys() {
		ObservationRegistry registry = ObservationRegistry.create();
		Capturing handler = new Capturing();
		registry.observationConfig().observationHandler(handler);
		StubChatModel model = new StubChatModel().reply("비밀 응답 본문").listeners(EgovObservationChatModelListener.of(registry));

		model.chat(request("비밀 질문 본문"));

		Observation.Context context = handler.stopped.get(0);
		List<String> values = new ArrayList<>();
		context.getLowCardinalityKeyValues().forEach(kv -> values.add(kv.getValue()));
		context.getHighCardinalityKeyValues().forEach(kv -> values.add(kv.getValue()));
		values.add(context.getContextualName());
		for (String value : values) {
			assertFalse(value.contains("비밀"), value);
		}
	}

	@Test
	@DisplayName("모델 실패 — 오류가 기록되고 멈추며 예외는 전파된다")
	void modelFailure() {
		ObservationRegistry registry = ObservationRegistry.create();
		Capturing handler = new Capturing();
		registry.observationConfig().observationHandler(handler);
		StubChatModel model = new StubChatModel().failWith(new IllegalStateException("모델 다운")).listeners(EgovObservationChatModelListener.of(registry));

		assertThrows(IllegalStateException.class, () -> model.chat(request("질문")));

		assertEquals(1, handler.errored.size());
		assertEquals(1, handler.stopped.size());
		assertNotNull(handler.stopped.get(0).getError());
		assertEquals("모델 다운", handler.stopped.get(0).getError().getMessage());
	}

	@Test
	@DisplayName("요청 없이 온 응답·오류는 무시되고, 등록소 타입·제공자 표기·micrometer 확인이 맞다")
	void ignoresContextsWithoutObservationAndMisc() {
		EgovObservationChatModelListener listener = EgovObservationChatModelListener.of(ObservationRegistry.create());
		ChatRequest request = request("q");

		listener.onResponse(new ChatModelResponseContext(new StubChatModel().doChat(request), request, ModelProvider.OTHER, new HashMap<>()));
		listener.onError(new ChatModelErrorContext(new RuntimeException("x"), request, ModelProvider.OTHER, new HashMap<>()));

		assertThrows(IllegalArgumentException.class, () -> EgovObservationChatModelListener.of(new Object()));
		assertEquals("ollama", EgovObservationChatModelListener.providerName(ModelProvider.OLLAMA));
		assertEquals("openai", EgovObservationChatModelListener.providerName(ModelProvider.OPEN_AI));
		assertEquals("azure", EgovObservationChatModelListener.providerName(ModelProvider.AZURE_OPEN_AI));
		assertEquals("watsonx", EgovObservationChatModelListener.providerName(ModelProvider.WATSONX));
		assertEquals(EgovObservationChatModelListener.UNKNOWN, EgovObservationChatModelListener.providerName(null));
		EgovObservationChatModelListener.requireMicrometer();
		assertTrue(EgovObservationChatModelListener.MICROMETER_COORDINATES.startsWith("io.micrometer"));
	}
}
