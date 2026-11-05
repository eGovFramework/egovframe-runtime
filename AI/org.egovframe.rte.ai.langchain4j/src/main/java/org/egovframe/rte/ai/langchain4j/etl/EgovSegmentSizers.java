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
package org.egovframe.rte.ai.langchain4j.etl;

import java.util.Objects;
import java.util.function.ToIntFunction;

import dev.langchain4j.model.TokenCountEstimator;

/**
 * 기반 모듈 청킹기의 크기 계산기({@code ToIntFunction<String>})를 LangChain4j 관례로 만든다 — 기본은 문자 수, 필요하면 LangChain4j
 * {@link TokenCountEstimator}(모델 구현체가 제공)를 감싼다. 어댑터는 특정 토크나이저에 의존하지 않는다.
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j — 어댑터 규범 2: 크기 계산기 SPI 로 프레임워크 주입)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovSegmentSizers {

	private EgovSegmentSizers() {
	}

	/** 문자 수 계산기(기본) — null 은 0. */
	public static ToIntFunction<String> chars() {
		return text -> text == null ? 0 : text.length();
	}

	/**
	 * LangChain4j 토큰 추정기를 계산기로 감싼다.
	 *
	 * @param estimator 토큰 추정기
	 * @return 계산기(null·빈 문자열은 0)
	 */
	public static ToIntFunction<String> of(TokenCountEstimator estimator) {
		Objects.requireNonNull(estimator, "estimator must not be null");
		return text -> text == null || text.isEmpty() ? 0 : estimator.estimateTokenCountInText(text);
	}
}
