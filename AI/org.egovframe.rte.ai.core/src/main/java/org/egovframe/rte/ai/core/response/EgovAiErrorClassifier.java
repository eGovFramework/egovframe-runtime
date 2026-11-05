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

import java.util.Optional;

/**
 * 실패 분류기 SPI — 단일 예외 레벨(cause 체인의 한 단)을 보고 유형을 판정한다.
 *
 * <p>기반 모듈은 AI 프레임워크의 예외 계층을 모른다. 어댑터가 Spring AI(<code>TransientAiException</code> 등)·LangChain4j
 * (<code>RetriableException</code> 등) 예외를 이 SPI 로 매핑해 {@link EgovAiFallback} 에 등록한다. 판정할 수 없으면
 * {@link Optional#empty()} 를 돌려 다음 분류기·기본 분류로 넘긴다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
@FunctionalInterface
public interface EgovAiErrorClassifier {

	/**
	 * @param throwable cause 체인의 한 예외(null 아님)
	 * @return 판정한 유형, 판정 불가면 empty
	 */
	Optional<EgovAiErrorType> classify(Throwable throwable);
}
