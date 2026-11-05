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

import java.util.Optional;

import org.egovframe.rte.ai.core.response.EgovAiErrorClassifier;
import org.egovframe.rte.ai.core.response.EgovAiErrorType;
import org.springframework.ai.retry.TransientAiException;

/**
 * Spring AI 예외 분류기 — {@link TransientAiException} 만 일시 오류(TRANSIENT)로 확정한다.
 *
 * <p>{@code NonTransientAiException} 은 여기서 GENERAL 로 확정하지 않고 비워 둔다. 확정하면 기반 모듈 폴백이 그 단계의 cause 체인·
 * 메시지 키워드 판정(토큰 한도·연결 실패)을 건너뛰어 더 구체적인 유형을 놓치기 때문이다. Spring AI 예외가 아니어도 비어 있다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai — 어댑터 규범 2: 예외 분류기 SPI)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovSpringAiErrorClassifier implements EgovAiErrorClassifier {

	@Override
	public Optional<EgovAiErrorType> classify(Throwable throwable) {
		if (throwable instanceof TransientAiException) {
			return Optional.of(EgovAiErrorType.TRANSIENT);
		}
		return Optional.empty();
	}
}
