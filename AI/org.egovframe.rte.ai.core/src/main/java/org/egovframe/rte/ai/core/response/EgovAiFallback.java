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
package org.egovframe.rte.ai.core.response;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.egovframe.rte.ai.core.audit.EgovAiAudit;

/**
 * 모델 호출 실패의 분류·폴백 문구 정책.
 *
 * <p>예외의 cause 체인 전체를 순회(자기 참조·깊이 한도 방어)하며 각 단에서 ① 등록된 {@link EgovAiErrorClassifier}(등록 순서)
 * ② JDK 네트워크 예외 ③ 잘 알려진 클라이언트 예외 FQN(netty) ④ 메시지 키워드 순으로 판정하고, 첫 판정을 채택한다. 어떤 단에서도
 * 판정되지 않으면 {@link EgovAiErrorType#GENERAL}. 유형별 문구는 기본(한국어)을 두고 교체할 수 있다.</p>
 *
 * <pre>
 * EgovAiFallback fallback = EgovAiFallback.builder().classifier(springAiClassifier).build();
 * String message = fallback.message(exception);   // 감사 채널에 유형·예외 클래스 기록
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 두 샘플의 EgovAiFallbackHandler 공통부를 SPI 로 재구성)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovAiFallback {

	/** cause 체인 순회 최대 깊이. */
	public static final int DEFAULT_MAX_DEPTH = 20;

	private static final String[] TIMEOUT_KEYWORDS = { "timeout", "timed out" };
	private static final String[] TOKEN_KEYWORDS = { "context length", "maximum context", "too many tokens",
			"prompt is too long", "input too long", "exceeds the limit", "token limit" };
	private static final String[] CONNECTION_KEYWORDS = { "connection refused", "connection reset", "no route to host",
			"connect exception", "network is unreachable", "failed to connect", "premature close", "unreachable" };
	private static final String[] TRANSIENT_KEYWORDS = { "rate limit", "too many requests", "service unavailable",
			"internal server error", "bad gateway", "temporarily unavailable" };

	private static final Map<String, EgovAiErrorType> KNOWN_CLASS_NAMES = Map.of(
			"io.netty.channel.ConnectTimeoutException", EgovAiErrorType.TIMEOUT,
			"reactor.netty.http.client.PrematureCloseException", EgovAiErrorType.CONNECTION);

	private final List<EgovAiErrorClassifier> classifiers;
	private final Map<EgovAiErrorType, String> messages;
	private final int maxDepth;

	private EgovAiFallback(Builder builder) {
		this.classifiers = List.copyOf(builder.classifiers);
		Map<EgovAiErrorType, String> merged = new EnumMap<>(EgovAiErrorType.class);
		for (EgovAiErrorType type : EgovAiErrorType.values()) {
			merged.put(type, builder.messages.getOrDefault(type, type.defaultMessage()));
		}
		this.messages = Collections.unmodifiableMap(merged);
		this.maxDepth = builder.maxDepth;
	}

	public static Builder builder() {
		return new Builder();
	}

	/** 기본 분류·기본 문구. */
	public static EgovAiFallback defaults() {
		return builder().build();
	}

	/**
	 * 예외를 분류한다(cause 체인 전체).
	 *
	 * @param throwable 예외(null 이면 GENERAL)
	 */
	public EgovAiErrorType classify(Throwable throwable) {
		Throwable current = throwable;
		int depth = 0;
		Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		while (current != null && depth < maxDepth && visited.add(current)) {
			EgovAiErrorType type = classifyLevel(current);
			if (type != EgovAiErrorType.GENERAL) {
				return type;
			}
			current = current.getCause();
			depth++;
		}
		return EgovAiErrorType.GENERAL;
	}

	/**
	 * 폴백 문구 — 분류 뒤 감사 채널에 유형·예외 클래스를 기록한다(메시지·값 미기록).
	 */
	public String message(Throwable throwable) {
		EgovAiErrorType type = classify(throwable);
		EgovAiAudit.callFailure(type.name(), throwable);
		return message(type);
	}

	/** 유형의 폴백 문구. */
	public String message(EgovAiErrorType type) {
		return messages.get(Objects.requireNonNull(type, "type must not be null"));
	}

	private EgovAiErrorType classifyLevel(Throwable t) {
		for (EgovAiErrorClassifier classifier : classifiers) {
			Optional<EgovAiErrorType> classified = classifier.classify(t);
			if (classified != null && classified.isPresent()) {
				return classified.get();
			}
		}
		if (t instanceof TimeoutException || t instanceof SocketTimeoutException) {
			return EgovAiErrorType.TIMEOUT;
		}
		if (t instanceof ConnectException || t instanceof UnknownHostException || t instanceof NoRouteToHostException) {
			return EgovAiErrorType.CONNECTION;
		}
		EgovAiErrorType known = KNOWN_CLASS_NAMES.get(t.getClass().getName());
		if (known != null) {
			return known;
		}
		return classifyByMessage(t.getMessage());
	}

	private static EgovAiErrorType classifyByMessage(String message) {
		if (message == null) {
			return EgovAiErrorType.GENERAL;
		}
		String lower = message.toLowerCase(Locale.ROOT);
		if (containsAny(lower, TIMEOUT_KEYWORDS)) {
			return EgovAiErrorType.TIMEOUT;
		}
		if (containsAny(lower, TOKEN_KEYWORDS)) {
			return EgovAiErrorType.TOKEN_LIMIT;
		}
		if (containsAny(lower, CONNECTION_KEYWORDS)) {
			return EgovAiErrorType.CONNECTION;
		}
		if (containsAny(lower, TRANSIENT_KEYWORDS)) {
			return EgovAiErrorType.TRANSIENT;
		}
		return EgovAiErrorType.GENERAL;
	}

	private static boolean containsAny(String text, String[] keywords) {
		for (String keyword : keywords) {
			if (text.contains(keyword)) {
				return true;
			}
		}
		return false;
	}

	/** {@link EgovAiFallback} 빌더. */
	public static final class Builder {
		private final List<EgovAiErrorClassifier> classifiers = new ArrayList<>();
		private final Map<EgovAiErrorType, String> messages = new EnumMap<>(EgovAiErrorType.class);
		private int maxDepth = DEFAULT_MAX_DEPTH;

		private Builder() {
		}

		/** 프레임워크 예외 분류기를 등록한다(등록 순서로 우선). */
		public Builder classifier(EgovAiErrorClassifier classifier) {
			classifiers.add(Objects.requireNonNull(classifier, "classifier must not be null"));
			return this;
		}

		/** 유형별 폴백 문구를 교체한다. */
		public Builder message(EgovAiErrorType type, String message) {
			Objects.requireNonNull(type, "type must not be null");
			if (message == null || message.isBlank()) {
				throw new IllegalArgumentException("폴백 문구는 비어 있을 수 없습니다: " + type);
			}
			messages.put(type, message);
			return this;
		}

		/** cause 체인 순회 최대 깊이(기본 20). */
		public Builder maxDepth(int maxDepth) {
			if (maxDepth <= 0) {
				throw new IllegalArgumentException("maxDepth 는 1 이상이어야 합니다: " + maxDepth);
			}
			this.maxDepth = maxDepth;
			return this;
		}

		public EgovAiFallback build() {
			return new EgovAiFallback(this);
		}
	}
}
