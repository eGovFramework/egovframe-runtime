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
package org.egovframe.rte.ai.springai.etl;

import java.util.Objects;
import java.util.function.ToIntFunction;

import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;

/**
 * 기반 모듈 청킹기의 크기 계산기({@code ToIntFunction<String>})를 Spring AI 의 토큰 추정기로 만든다.
 *
 * <p>기본은 cl100k 토큰({@link JTokkitTokenCountEstimator}) — Spring AI 가 {@code TokenTextSplitter} 에 쓰는 것과 같은 단위라
 * 청크 크기를 Spring AI 사용자가 익숙한 토큰 수로 지정할 수 있다. jtokkit 은 Spring AI 추정기를 통해서만 쓰고 직접 의존하지
 * 않는다. 인코딩 로드 비용이 있어 cl100k 추정기는 첫 호출 때 만든다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai — 어댑터 규범 2: 크기 계산기 SPI 로 프레임워크 주입)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovTokenSizers {

	private EgovTokenSizers() {
	}

	/**
	 * cl100k 토큰 수 계산기(기본).
	 *
	 * @return null·빈 문자열은 0
	 */
	public static ToIntFunction<String> cl100k() {
		return text -> text == null || text.isEmpty() ? 0 : Cl100k.ESTIMATOR.estimate(text);
	}

	/**
	 * 임의의 Spring AI 토큰 추정기를 계산기로 감싼다.
	 *
	 * @param estimator 토큰 추정기
	 * @return 계산기(null·빈 문자열은 0)
	 */
	public static ToIntFunction<String> of(TokenCountEstimator estimator) {
		Objects.requireNonNull(estimator, "estimator must not be null");
		return text -> text == null || text.isEmpty() ? 0 : estimator.estimate(text);
	}

	/**
	 * 문자 수 계산기 — 토큰 추정이 필요 없는 경우(LangChain4j 어댑터와 같은 단위).
	 *
	 * @return 계산기(null 은 0)
	 */
	public static ToIntFunction<String> chars() {
		return text -> text == null ? 0 : text.length();
	}

	/** 지연 초기화 홀더 — 인코딩 로드는 첫 사용 시. */
	private static final class Cl100k {
		static final TokenCountEstimator ESTIMATOR = new JTokkitTokenCountEstimator();
	}
}
