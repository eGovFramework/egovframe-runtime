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
package org.egovframe.rte.ai.langchain4j.response;

import org.egovframe.rte.ai.core.response.EgovAiFallback;

/**
 * LangChain4j 용 폴백 팩토리 — 기반 모듈 {@link EgovAiFallback} 기본 구성에 {@link EgovLangChain4jErrorClassifier} 를 끼운다.
 *
 * <pre>
 *   EgovAiFallback fallback = EgovLangChain4jFallbacks.defaults();
 *   try { return assistant.chat(memoryId, question); }
 *   catch (InputGuardrailException e) { return e.getMessage(); }         // 가드레일 차단 안내
 *   catch (RuntimeException e) { return fallback.message(e); }          // 분류 + 감사(event=call-failure) 는 기반 모듈이
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovLangChain4jFallbacks {

	private EgovLangChain4jFallbacks() {
	}

	/** 기본 폴백(기본 메시지·깊이 + LangChain4j 분류기). */
	public static EgovAiFallback defaults() {
		return builder().build();
	}

	/** LangChain4j 분류기가 미리 끼워진 빌더 — 메시지·깊이를 덧붙인다. */
	public static EgovAiFallback.Builder builder() {
		return EgovAiFallback.builder().classifier(new EgovLangChain4jErrorClassifier());
	}
}
