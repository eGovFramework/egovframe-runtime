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

import java.util.Optional;

import org.egovframe.rte.ai.core.response.EgovAiErrorClassifier;
import org.egovframe.rte.ai.core.response.EgovAiErrorType;

import dev.langchain4j.exception.RetriableException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.exception.UnresolvedModelServerException;

/**
 * LangChain4j 예외 분류기 — 계층이 뜻하는 유형만 확정한다: {@link TimeoutException}→TIMEOUT, {@link RetriableException} 계열
 * (RateLimit·InternalServer 포함)→TRANSIENT, {@link UnresolvedModelServerException}→CONNECTION. 그 밖의 NonRetriable·Http·비 LangChain4j
 * 예외는 비워 두어 기반 폴백의 cause 체인·메시지 키워드 판정(토큰 한도·연결 실패)이 계속되게 한다.
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j — 샘플 EgovAiFallbackHandler 매핑을 SPI 로)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovLangChain4jErrorClassifier implements EgovAiErrorClassifier {

	@Override
	public Optional<EgovAiErrorType> classify(Throwable throwable) {
		if (throwable instanceof TimeoutException) {
			return Optional.of(EgovAiErrorType.TIMEOUT);
		}
		if (throwable instanceof RetriableException) {
			return Optional.of(EgovAiErrorType.TRANSIENT);
		}
		if (throwable instanceof UnresolvedModelServerException) {
			return Optional.of(EgovAiErrorType.CONNECTION);
		}
		return Optional.empty();
	}
}
