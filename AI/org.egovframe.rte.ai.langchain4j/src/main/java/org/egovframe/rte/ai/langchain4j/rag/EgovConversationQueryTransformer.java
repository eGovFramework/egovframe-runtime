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
package org.egovframe.rte.ai.langchain4j.rag;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.egovframe.rte.ai.core.response.EgovModelOutputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;

/**
 * 대화 맥락 질의 변환기 — LangChain4j {@link CompressingQueryTransformer} 를 한국어 템플릿으로 감싸고 결과를 검증한다.
 *
 * <ul>
 *   <li>{@link Query#metadata()} 가 없거나 {@code chatMemory()} 가 비어 있으면 모델을 부르지 않고 원 질의를 돌려준다(상태 {@code skipped}).</li>
 *   <li>압축 결과에서 추론 태그를 제거({@link EgovModelOutputs#stripReasoning})한 뒤, 비어 있거나 답변형(여러 줄·여러 문장)이거나 원 질의
 *       대비 과도하게 길면 원 질의를 돌려주고 경고를 남긴다(상태 {@code fallback}).</li>
 *   <li>채택하면 상태 {@code applied}. 반환 질의는 {@link EgovCompressedQuery}(상태·원 질의 텍스트) 하나를 담은 목록이다.</li>
 *   <li>모델 호출 예외는 기본 전파, {@link Builder#fallbackOnFailure(boolean)} 를 켠 경우만 원 질의 + 경고로 대체한다.</li>
 * </ul>
 *
 * <p>이력과 대화 id 는 {@code AiServices} 가 {@code @MemoryId} 로 받은 메모리를 {@code Query.metadata()} 에 채워 넘기므로 어댑터에 세션
 * 상태(ThreadLocal)가 없다(검토 4장 #3 교정).</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j — 샘플 LangChain4j 열에 없던 부품)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovConversationQueryTransformer implements QueryTransformer {

	private static final Logger LOG = LoggerFactory.getLogger(EgovConversationQueryTransformer.class);

	/** 압축 결과를 채택함. */
	public static final String STATUS_APPLIED = "applied";

	/** 이력이 없어 압축을 생략함. */
	public static final String STATUS_SKIPPED = "skipped";

	/** 결과를 채택하지 않거나(빈 값·답변형·길이 초과) 호출이 실패해 원 질의를 유지함. */
	public static final String STATUS_FALLBACK = "fallback";

	/** 기본 한국어 템플릿 리소스(변수 {@code {{chatMemory}}}·{@code {{query}}}). */
	public static final String DEFAULT_TEMPLATE_RESOURCE = "egov/ai/langchain4j/query-compression.txt";

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
	 * @param chatModel 압축에 쓸 모델
	 * @return 빌더
	 */
	public static Builder builder(ChatModel chatModel) {
		return new Builder(chatModel);
	}

	/**
	 * 기본 한국어 템플릿 — 클래스패스 리소스를 UTF-8 로 읽는다.
	 *
	 * @return 템플릿
	 */
	public static PromptTemplate defaultPromptTemplate() {
		ClassLoader loader = EgovConversationQueryTransformer.class.getClassLoader();
		try (InputStream in = loader.getResourceAsStream(DEFAULT_TEMPLATE_RESOURCE)) {
			if (in == null) {
				throw new IllegalStateException("기본 질의 압축 템플릿 리소스가 없습니다: " + DEFAULT_TEMPLATE_RESOURCE);
			}
			return PromptTemplate.from(new String(in.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new UncheckedIOException("기본 질의 압축 템플릿을 읽지 못했습니다: " + DEFAULT_TEMPLATE_RESOURCE, e);
		}
	}

	@Override
	public Collection<Query> transform(Query query) {
		Objects.requireNonNull(query, "query must not be null");
		String original = query.text();
		if (query.metadata() == null || query.metadata().chatMemory() == null || query.metadata().chatMemory().isEmpty()) {
			return List.of(EgovCompressedQuery.of(original, query.metadata(), STATUS_SKIPPED, original));
		}
		String compressed;
		try {
			Collection<Query> transformed = delegate.transform(query);
			compressed = transformed == null || transformed.isEmpty() ? null : transformed.iterator().next().text();
		} catch (RuntimeException e) {
			if (!fallbackOnFailure) {
				throw e;
			}
			LOG.warn("질의 압축 모델 호출 실패 — 원 질의로 계속합니다: {}", e.toString());
			return List.of(EgovCompressedQuery.of(original, query.metadata(), STATUS_FALLBACK, original));
		}
		String cleaned = EgovModelOutputs.stripReasoning(compressed);
		cleaned = cleaned == null ? "" : cleaned.trim();
		String rejection = rejectionReason(cleaned, original);
		if (rejection != null) {
			LOG.warn("질의 압축 결과를 채택하지 않습니다({}) — 원 질의를 유지합니다", rejection);
			return List.of(EgovCompressedQuery.of(original, query.metadata(), STATUS_FALLBACK, original));
		}
		return List.of(EgovCompressedQuery.of(cleaned, query.metadata(), STATUS_APPLIED, original));
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

	/** {@link EgovConversationQueryTransformer} 빌더. */
	public static final class Builder {
		private final ChatModel chatModel;
		private PromptTemplate promptTemplate;
		private double maxLengthRatio = DEFAULT_MAX_LENGTH_RATIO;
		private boolean fallbackOnFailure = false;

		private Builder(ChatModel chatModel) {
			this.chatModel = Objects.requireNonNull(chatModel, "chatModel must not be null");
		}

		/** 프롬프트 템플릿({@code {{chatMemory}}}·{@code {{query}}} 변수 필수, 기본은 한국어 리소스). */
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
			QueryTransformer delegate = new CompressingQueryTransformer(chatModel,
					promptTemplate == null ? defaultPromptTemplate() : promptTemplate);
			return new EgovConversationQueryTransformer(delegate, maxLengthRatio, fallbackOnFailure);
		}
	}
}
