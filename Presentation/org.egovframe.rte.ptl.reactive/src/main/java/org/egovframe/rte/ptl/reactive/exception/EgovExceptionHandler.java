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

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 발생한 오류를 처리하고 동일한 형식으로 응답을 보내기 위한 클래스
 *
 * <p>Desc.: 발생한 오류를 처리하고 동일한 형식으로 응답을 보내기 위한 클래스</p>
 *
 * @author ESFC
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   ESFC            최초 생성
 * 2026.10.25   실행환경 개발팀      미분류 예외 원본은 서버 로그로만 기록, 표준 상태 예외(404·405 등)는 상태 보존 +
 *                              일반화 문구(요청 경로·입력값 미노출), 이미 전송이 시작된 응답은 건드리지 않음
 * </pre>
 * @since 2023.08.31
 */
@Order(-3)
public class EgovExceptionHandler implements WebExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            // 이미 전송이 시작된 응답은 바꿀 수 없으므로 오류를 그대로 전파한다
            return Mono.error(ex);
        }
        if (ex instanceof EgovException) {
            EgovException egovException = (EgovException) ex;
            EgovErrorCode egovErrorCode = egovException.getEgovErrorCode();
            String message = egovException.getMessage();
            return getExceptionResponse(response, egovErrorCode, message);
        } else if (ex instanceof EgovServiceException) {
            EgovServiceException egovServiceException = (EgovServiceException) ex;
            EgovErrorCode egovErrorCode = egovServiceException.getEgovErrorCode();
            String message = egovServiceException.getMessage();
            return getExceptionResponse(response, egovErrorCode, message);
        } else if (ex instanceof ResponseStatusException) {
            // Spring 표준 상태 예외(404·405 등)는 상태를 보존하되, 예외 메시지(요청 경로 등)는 싣지 않는다
            EgovErrorCode egovErrorCode = toErrorCode(((ResponseStatusException) ex).getRawStatusCode());
            LOGGER.debug("Standard exception mapped to status {}: {}", egovErrorCode.getStatus(), ex.getMessage());
            return getExceptionResponse(response, egovErrorCode, egovErrorCode.getMessage());
        } else {
            // 원본 메시지·스택은 서버 로그로만 남기고 응답은 일반화한다(CWE-209)
            LOGGER.error("Unhandled exception mapped to internal server error", ex);
            EgovErrorCode egovErrorCode = EgovErrorCode.INTERNAL_SERVER_ERROR;
            String message = egovErrorCode.getMessage();
            return getExceptionResponse(response, egovErrorCode, message);
        }
    }

    /**
     * HTTP 상태에 대응하는 오류 코드. 대응이 없으면 4xx 는 E001, 5xx 는 E021.
     */
    private static EgovErrorCode toErrorCode(int status) {
        switch (status) {
            case 401: return EgovErrorCode.UNAUTHORIZED;
            case 403: return EgovErrorCode.ACCESS_DENIED;
            case 404: return EgovErrorCode.NOT_FOUND;
            case 405: return EgovErrorCode.METHOD_NOT_ALLOWED;
            case 406: return EgovErrorCode.NOT_ACCEPTABLE;
            case 422: return EgovErrorCode.UNPROCESSABLE_ENTITY;
            case 503: return EgovErrorCode.SERVICE_UNAVAILABLE;
            default:
                return (status >= 400 && status < 500) ? EgovErrorCode.INVALID_INPUT_VALUE : EgovErrorCode.INTERNAL_SERVER_ERROR;
        }
    }

    private Mono<Void> getExceptionResponse(ServerHttpResponse response, EgovErrorCode egovErrorCode, String message) {
        EgovExceptionResponse egovResponse = EgovExceptionResponse.of(egovErrorCode, message);
        return handlerResponse(response,
                egovResponse.getTimestamp(),
                egovResponse.getStatus(),
                egovResponse.getCode(),
                egovResponse.getMessage());
    }

    private Mono<Void> handlerResponse(ServerHttpResponse response, String timestamp, int status, String code, String message) {
        response.setStatusCode(HttpStatus.valueOf(status));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", timestamp);
        map.put("status", HttpStatus.valueOf(status));
        map.put("code", code);
        map.put("message", message);
        JSONObject jsonObject = new JSONObject(map);
        DataBuffer dataBuffer = response.bufferFactory().wrap(JSONObject.toJSONString(jsonObject).getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(dataBuffer));
    }

}
