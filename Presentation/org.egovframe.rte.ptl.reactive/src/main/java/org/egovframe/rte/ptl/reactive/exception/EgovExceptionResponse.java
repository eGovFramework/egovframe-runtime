/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.ptl.reactive.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * EgovExceptionHandler로 보내는 응답을 구성하는 클래스
 *
 * <p>Desc.: EgovExceptionHandler로 보내는 응답을 구성하는 클래스</p>
 *
 * @author 유지보수
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   유지보수            최초 생성
 * 2026.09.30   실행환경 개발팀      상태 지정 팩터리 추가
 * </pre>
 * @since 2023.08.31
 */
public class EgovExceptionResponse {

    protected String timestamp;
    protected int status;
    protected String code;
    protected String message;

    private EgovExceptionResponse(EgovErrorCode egovErrorCode, String message) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = egovErrorCode.getStatus();
        this.code = egovErrorCode.getCode();
        this.message = message;
    }

    private EgovExceptionResponse(int status, EgovErrorCode egovErrorCode, String message) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = status;
        this.code = egovErrorCode.getCode();
        this.message = message;
    }

    public static EgovExceptionResponse of(EgovErrorCode egovErrorCode, String message) {
        return new EgovExceptionResponse(egovErrorCode, message);
    }

    /**
     * 상태를 지정해 응답을 만든다. 상태에 대응하는 오류 코드가 없어 계열 대표 코드(4xx 는 E001, 5xx 는 E021)를
     * 쓰면서 status 는 실제 값을 유지할 때 쓴다.
     *
     * @param status 실제 HTTP 상태 코드(세 자리)
     * @param egovErrorCode 응답 code 로 쓸 오류 코드
     * @param message 응답 메시지
     * @return 응답
     * @throws IllegalArgumentException 상태 코드가 세 자리(100~999)가 아닌 경우
     * @since 5.1
     */
    public static EgovExceptionResponse of(int status, EgovErrorCode egovErrorCode, String message) {
        if (status < 100 || status > 999) {
            throw new IllegalArgumentException("HTTP status code must be a 3-digit value: " + status);
        }
        return new EgovExceptionResponse(status, egovErrorCode, message);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
