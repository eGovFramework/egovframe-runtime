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
package org.egovframe.rte.ai.core.document;

/**
 * 문서 텍스트 추출 실패 — 파서 라이브러리 부재·손상 파일·읽기 실패(unchecked, 원인 포함).
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public class EgovExtractionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EgovExtractionException(String message) {
		super(message);
	}

	public EgovExtractionException(String message, Throwable cause) {
		super(message, cause);
	}

	/** optional 라이브러리가 클래스패스에 없을 때. */
	static EgovExtractionException missingLibrary(String feature, String coordinates, Throwable cause) {
		return new EgovExtractionException(feature + "에는 " + coordinates
				+ " 가 클래스패스에 있어야 합니다 — optional 의존이므로 소비 모듈 pom 에 직접 선언하십시오", cause);
	}
}
