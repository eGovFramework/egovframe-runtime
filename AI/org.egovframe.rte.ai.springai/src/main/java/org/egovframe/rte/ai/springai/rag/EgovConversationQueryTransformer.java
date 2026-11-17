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
package org.egovframe.rte.ai.springai.rag;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.egovframe.rte.ai.core.response.EgovModelOutputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;

/**
 * 대화 맥락 질의 변환기 — Spring AI {@link CompressionQueryTransformer} 를 한국어 템플릿으로 감싸고 결과를 검증한다.
 *
 * <ul>
 *   <li>{@link Query#history()} 가 비어 있거나 <b>이전 턴이 없으면</b>(어시스턴트 메시지도, 현재 질의와 다른 사용자 메시지도 없음 —
 *       {@code RetrievalAugmentationAdvisor} 는 현재 사용자 메시지까지 이력에 넣으므로 첫 턴도 이력이 비어 있지 않다) 모델을 부르지 않고
 *       원 질의를 돌려준다(상태 {@code skipped}).</li>
 *   <li>압축 결과에서 추론 태그를 제거({@link EgovModelOutputs#stripReasoning})한 뒤, 비어 있거나 답변형(여러 줄·여러 문장)이거나
 *       원 질의 대비 과도하게 길면 원 질의를 돌려주고 경고를 남긴다(상태 {@code fallback}).</li>
 *   <li>채택하면 상태 {@code applied}. 상태는 {@link Query#context()} 의 {@link #STATUS} 키에 실린다.</li>
 *   <li>모델 호출 예외는 기본적으로 전파하고, {@link Builder#fallbackOnFailure(boolean)} 를 켠 경우에만 원 질의 + 경고로 대체한다.</li>
 * </ul>
 *
 * <p>대화 id 와 이력은 {@code RetrievalAugmentationAdvisor} 가 프롬프트 메시지에서 채운 {@code Query.history()} 로 명시 전달된다 —
 * 메모리 어드바이저가 앱이 넘긴 {@code ChatMemory.CONVERSATION_ID} 파라미터로 이력을 붙이므로 여기에는 세션 상태(ThreadLocal)가
 * 없다(검토 4장 #3 교정).</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai — 샘플 EgovCompressionQueryTransformer 재구성)
 *  2026.09.03  실행환경팀     첫 턴(이전 턴 없음)은 압축 생략 — RetrievalAugmentationAdvisor 가 현재 질의를 이력에 넣어도 모델을 부르지 않음
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovConversationQueryTransformer implements QueryTransformer {

	private static final Logger LOG = LoggerFactory.getLogger(EgovConversationQueryTransformer.class);

	/** {@link Query#context()} 상태 키. */
	public static final String STATUS = "egov_query_compression";

	/** 압축 결과를 채택함. */
	public static final String STATUS_APPLIED = "applied";

	/** 이력이 없어 압축을 생략함. */
	public static final String STATUS_SKIPPED = "skipped";

	/** 결과를 채택하지 않거나(빈 값·답변형·길이 초과) 호출이 실패해 원 질의를 유지함. */
	public static final String STATUS_FALLBACK = "fallback";

	/** 기본 한국어 템플릿 리소스(변수 {@code {history}}·{@code {query}}). */
	public static final String DEFAULT_TEMPLATE_RESOURCE = "egov/ai/springai/query-compression.st";

	/** 원 질의 대비 허용 길이 배율 기본값. */
	public static final double DEFAULT_MAX_LENGTH_RATIO = 3.0;

	/** 배율과 무관하게 허용하는 최소 길이(짧은 후속 질문이 길어지는 정상 경우). */
	static final int MIN_LENGTH_ALLOWANCE = 200;

	private static final Pattern SENTENCE_END = Pattern.compile("[.!?。](?=\\s|$)");

	private final QueryTransformer delegate;
	private final double maxLengthRatio;
	private final boolean fallbackOnFailure;

	private EgovConversationQueryTransformer(QueryTransformer delegate, double maxLengthRatio, boolean fallbackOnFailure) {
		this.delegate = delegate;
		this.maxLengthRatio = maxLengthRatio;
		this.fallbackOnFailure = fallbackOnFailure;
	}

	/**
	 * 빌더.
	 *
	 * @param chatClientBuilder 압축에 쓸 ChatClient 빌더(호출마다 {@code build()})
	 * @return 빌더
	 */
	public static Builder builder(ChatClient.Builder chatClientBuilder) {
		return new Builder(chatClientBuilder);
	}

	/**
	 * 기본 한국어 템플릿 — 클래스패스 리소스를 UTF-8 로 읽는다({@code PromptTemplate(Resource)} 는 플랫폼 기본 문자셋을 쓰므로 직접 읽는다).
	 *
	 * @return 템플릿
	 */
	public static PromptTemplate defaultPromptTemplate() {
		ClassLoader loader = EgovConversationQueryTransformer.class.getClassLoader();
		try (InputStream in = loader.getResourceAsStream(DEFAULT_TEMPLATE_RESOURCE)) {
			if (in == null) {
				throw new IllegalStateException("기본 질의 압축 템플릿 리소스가 없습니다: " + DEFAULT_TEMPLATE_RESOURCE);
			}
			return new PromptTemplate(new String(in.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new UncheckedIOException("기본 질의 압축 템플릿을 읽지 못했습니다: " + DEFAULT_TEMPLATE_RESOURCE, e);
		}
	}

	@Override
	public Query transform(Query query) {
		Objects.requireNonNull(query, "query must not be null");
		String original = query.text();
		if (!hasPriorTurn(query.history(), original)) {
			return withStatus(query, original, STATUS_SKIPPED);
		}
		String compressed;
		try {
			compressed = delegate.transform(query).text();
		} catch (RuntimeException e) {
			if (!fallbackOnFailure) {
				throw e;
			}
			LOG.warn("질의 압축 모델 호출 실패 — 원 질의로 계속합니다: {}", e.toString());
			return withStatus(query, original, STATUS_FALLBACK);
		}
		String cleaned = EgovModelOutputs.stripReasoning(compressed);
		cleaned = cleaned == null ? "" : cleaned.trim();
		String rejection = rejectionReason(cleaned, original);
		if (rejection != null) {
			LOG.warn("질의 압축 결과를 채택하지 않습니다({}) — 원 질의를 유지합니다", rejection);
			return withStatus(query, original, STATUS_FALLBACK);
		}
		return withStatus(query, cleaned, STATUS_APPLIED);
	}

	/**
	 * 채택 거부 사유(없으면 null) — 빈 결과, 길이 초과, 답변형(비어 있지 않은 줄 3개 이상 또는 문장 종결 3개 이상).
	 */
	String rejectionReason(String candidate, String original) {
		if (candidate.isEmpty()) {
			return "빈 결과";
		}
		int originalLength = original == null ? 0 : original.length();
		int limit = (int) Math.max(Math.ceil(originalLength * maxLengthRatio), MIN_LENGTH_ALLOWANCE);
		if (candidate.length() > limit) {
			return "길이 초과(" + candidate.length() + " > " + limit + ")";
		}
		long lines = candidate.lines().filter(line -> !line.isBlank()).count();
		if (lines >= 3) {
			return "답변형(줄 " + lines + "개)";
		}
		int sentences = 0;
		var matcher = SENTENCE_END.matcher(candidate);
		while (matcher.find()) {
			sentences++;
		}
		if (sentences >= 3) {
			return "답변형(문장 " + sentences + "개)";
		}
		return null;
	}

	/**
	 * 압축할 이전 턴이 있는지 — 어시스턴트 메시지가 하나라도 있거나, 현재 질의와 다른 사용자 메시지가 있으면 참. 첫 턴(시스템 메시지 +
	 * 현재 사용자 메시지뿐)은 거짓이라 모델 호출을 아낀다(4/4 예제 실측: 첫 질의에 모델 2회 호출).
	 */
	static boolean hasPriorTurn(List<Message> history, String currentQuery) {
		if (history == null || history.isEmpty()) {
			return false;
		}
		for (Message message : history) {
			if (message == null) {
				continue;
			}
			if (message.getMessageType() == MessageType.ASSISTANT) {
				return true;
			}
			if (message.getMessageType() == MessageType.USER && !Objects.equals(message.getText(), currentQuery)) {
				return true;
			}
		}
		return false;
	}

	private static Query withStatus(Query query, String text, String status) {
		Map<String, Object> context = new LinkedHashMap<>(query.context() == null ? Map.of() : query.context());
		context.put(STATUS, status);
		return query.mutate().text(text).context(context).build();
	}

	/** {@link EgovConversationQueryTransformer} 빌더. */
	public static final class Builder {
		private final ChatClient.Builder chatClientBuilder;
		private PromptTemplate promptTemplate;
		private double maxLengthRatio = DEFAULT_MAX_LENGTH_RATIO;
		private boolean fallbackOnFailure = false;

		private Builder(ChatClient.Builder chatClientBuilder) {
			this.chatClientBuilder = Objects.requireNonNull(chatClientBuilder, "chatClientBuilder must not be null");
		}

		/** 프롬프트 템플릿({@code {history}}·{@code {query}} 변수 필수, 기본은 한국어 리소스). */
		public Builder promptTemplate(PromptTemplate promptTemplate) {
			this.promptTemplate = promptTemplate;
			return this;
		}

		/** 원 질의 대비 허용 길이 배율(1 이상, 기본 {@value #DEFAULT_MAX_LENGTH_RATIO}). */
		public Builder maxLengthRatio(double maxLengthRatio) {
			this.maxLengthRatio = maxLengthRatio;
			return this;
		}

		/** 모델 호출 실패 시 원 질의로 계속할지(기본 false — 예외 전파). */
		public Builder fallbackOnFailure(boolean fallbackOnFailure) {
			this.fallbackOnFailure = fallbackOnFailure;
			return this;
		}

		public EgovConversationQueryTransformer build() {
			if (maxLengthRatio < 1.0) {
				throw new IllegalArgumentException("maxLengthRatio 는 1 이상이어야 합니다: " + maxLengthRatio);
			}
			QueryTransformer delegate = CompressionQueryTransformer.builder()
					.chatClientBuilder(chatClientBuilder)
					.promptTemplate(promptTemplate == null ? defaultPromptTemplate() : promptTemplate)
					.build();
			return new EgovConversationQueryTransformer(delegate, maxLengthRatio, fallbackOnFailure);
		}
	}
}
