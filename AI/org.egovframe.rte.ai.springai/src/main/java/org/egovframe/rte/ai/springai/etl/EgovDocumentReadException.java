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

import java.util.List;
import java.util.Objects;

import org.egovframe.rte.ai.core.document.EgovExtractionResult;

/**
 * 리더의 엄격 읽기({@link EgovDocumentReader#get()})에서 한 파일이라도 실패했을 때 던지는 예외 — 실패한 파일과 사유를 모두
 * 담고, 첫 실패의 원인을 cause 로 잇는다(부분 성공을 조용히 돌려주지 않는다).
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai — 샘플 리더의 파일별 침묵 실패 교정)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public class EgovDocumentReadException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final transient List<EgovExtractionResult.Failure> failures;

	/**
	 * @param failures 실패 목록(1건 이상)
	 */
	public EgovDocumentReadException(List<EgovExtractionResult.Failure> failures) {
		super(describe(failures), firstCause(failures));
		this.failures = List.copyOf(failures);
	}

	/** 실패 목록(불변). */
	public List<EgovExtractionResult.Failure> failures() {
		return failures;
	}

	private static String describe(List<EgovExtractionResult.Failure> failures) {
		Objects.requireNonNull(failures, "failures must not be null");
		StringBuilder message = new StringBuilder("문서 읽기 실패 ").append(failures.size()).append("건: ");
		for (int i = 0; i < failures.size(); i++) {
			EgovExtractionResult.Failure failure = failures.get(i);
			if (i > 0) {
				message.append(", ");
			}
			message.append('[').append(failure.file() == null ? "?" : failure.file().getFileName())
					.append(": ").append(failure.reason()).append(']');
		}
		return message.toString();
	}

	private static Throwable firstCause(List<EgovExtractionResult.Failure> failures) {
		for (EgovExtractionResult.Failure failure : failures) {
			if (failure.cause() != null) {
				return failure.cause();
			}
		}
		return null;
	}
}
