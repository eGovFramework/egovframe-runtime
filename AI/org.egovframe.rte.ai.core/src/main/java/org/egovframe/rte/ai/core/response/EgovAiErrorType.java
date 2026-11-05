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

/**
 * 모델 호출 실패 유형 — 두 샘플에서 상수명이 표류하던 것(<code>TOKEN_EXCEEDED</code>/<code>TOKEN_LIMIT</code> 등)을 하나로 통일.
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public enum EgovAiErrorType {

	/** 응답 대기 시간 초과. */
	TIMEOUT("AI 모델 응답 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요."),

	/** 입력이 모델 허용 토큰 한도를 초과. */
	TOKEN_LIMIT("입력 내용이 너무 길어 처리할 수 없습니다. 질문을 줄여서 다시 시도해 주세요."),

	/** 모델 서버 연결 실패. */
	CONNECTION("AI 모델 서버에 연결할 수 없습니다. 서버 상태를 확인한 후 다시 시도해 주세요."),

	/** rate limit·일시 서버 오류 등 재시도 가능한 일시적 오류. */
	TRANSIENT("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),

	/** 위 유형에 해당하지 않는 일반 오류(인증 실패·잘못된 모델명 등 영구 오류 포함). */
	GENERAL("AI 응답 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

	private final String defaultMessage;

	EgovAiErrorType(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}

	/** 기본 폴백 문구(한국어). */
	public String defaultMessage() {
		return defaultMessage;
	}
}
