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

/**
 * 요청에 대한 오류 반환값을 정의한 클래스
 *
 * <p>Desc.: 요청에 대한 오류 반환값을 정의한 클래스</p>
 *
 * @author 유지보수
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   유지보수            최초 생성
 * 2026.09.30   실행환경 개발팀      415(E012)·413(E013) 오류 코드 추가
 * </pre>
 * @since 2023.08.31
 */
public class EgovErrorCode {

    public static final EgovErrorCode INVALID_INPUT_VALUE =
            new EgovErrorCode(400, "E001", "Invalid input value.");
    public static final EgovErrorCode INVALID_TYPE_VALUE =
            new EgovErrorCode(400, "E002", "Invalid input type.");
    public static final EgovErrorCode ENTITY_NOT_FOUND =
            new EgovErrorCode(400, "E003", "No input values found.");
    public static final EgovErrorCode UNAUTHORIZED =
            new EgovErrorCode(401, "E004", "User authentication is required.");
    public static final EgovErrorCode JWT_EXPIRED =
            new EgovErrorCode(401, "E005", "User authentication is required.");
    public static final EgovErrorCode ACCESS_DENIED =
            new EgovErrorCode(403, "E006", "Access denied.");
    public static final EgovErrorCode NOT_FOUND =
            new EgovErrorCode(404, "E007", "Page not found.");
    public static final EgovErrorCode METHOD_NOT_ALLOWED =
            new EgovErrorCode(405, "E008", "The HTTP method associated with the request is not supported.");
    public static final EgovErrorCode NOT_ACCEPTABLE =
            new EgovErrorCode(406, "E009", "The server does not provide a default representation.");
    public static final EgovErrorCode REQUIRE_USER_JOIN =
            new EgovErrorCode(412, "E010", "There are no registered users.");
    public static final EgovErrorCode UNPROCESSABLE_ENTITY =
            new EgovErrorCode(422, "E011", "Unable to follow requested instructions.");
    /** 지원하지 않는 요청 미디어 타입(415) — 표준 예외 상태 보존용 */
    public static final EgovErrorCode UNSUPPORTED_MEDIA_TYPE =
            new EgovErrorCode(415, "E012", "The media type of the request is not supported.");
    /** 요청 본문 크기 초과(413) — 표준 예외 상태 보존용 */
    public static final EgovErrorCode PAYLOAD_TOO_LARGE =
            new EgovErrorCode(413, "E013", "The request payload is too large.");
    public static final EgovErrorCode INTERNAL_SERVER_ERROR =
            new EgovErrorCode(500, "E021", "Internal Server Error.");
    public static final EgovErrorCode SERVICE_UNAVAILABLE =
            new EgovErrorCode(503, "E022", "The service cannot be performed.");

    private final int status;
    private final String code;
    private final String message;

    private EgovErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
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
