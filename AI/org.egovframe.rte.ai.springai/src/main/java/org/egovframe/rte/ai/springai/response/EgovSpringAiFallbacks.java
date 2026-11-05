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
package org.egovframe.rte.ai.springai.response;

import org.egovframe.rte.ai.core.response.EgovAiFallback;

/**
 * Spring AI 용 폴백 팩토리 — 기반 모듈 {@link EgovAiFallback} 의 기본 구성에 {@link EgovSpringAiErrorClassifier} 를 끼운다.
 *
 * <pre>
 *   EgovAiFallback fallback = EgovSpringAiFallbacks.defaults();
 *   try { ... chatClient.prompt().user(q).call().content() ... }
 *   catch (RuntimeException e) { return fallback.message(e); }   // 분류 + 감사(event=call_failure) 는 기반 모듈이
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovSpringAiFallbacks {

	private EgovSpringAiFallbacks() {
	}

	/** 기본 폴백(기본 메시지·깊이 + Spring AI 분류기). */
	public static EgovAiFallback defaults() {
		return builder().build();
	}

	/** Spring AI 분류기가 미리 끼워진 빌더 — 메시지·깊이를 덧붙인다. */
	public static EgovAiFallback.Builder builder() {
		return EgovAiFallback.builder().classifier(new EgovSpringAiErrorClassifier());
	}
}
