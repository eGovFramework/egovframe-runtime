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
 * @author 유지보수
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   유지보수            최초 생성
 * </pre>
 * @since 2023.08.31
 */
@Order(-3)
public class EgovExceptionHandler implements WebExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (ex instanceof EgovException) {
            EgovException egovException = (EgovException) ex;
            EgovErrorCode egovErrorCode = egovException.getEgovErrorCode();
            String message = resolveResponseMessage(egovErrorCode, egovException.getMessage(), ex);
            return getExceptionResponse(response, egovErrorCode, message);
        } else if (ex instanceof EgovServiceException) {
            EgovServiceException egovServiceException = (EgovServiceException) ex;
            EgovErrorCode egovErrorCode = egovServiceException.getEgovErrorCode();
            String message = resolveResponseMessage(egovErrorCode, egovServiceException.getMessage(), ex);
            return getExceptionResponse(response, egovErrorCode, message);
        } else {
            EgovErrorCode egovErrorCode = EgovErrorCode.INTERNAL_SERVER_ERROR;
            LOGGER.error("Unhandled exception while processing request", ex);
            return getExceptionResponse(response, egovErrorCode, egovErrorCode.getMessage());
        }
    }

    /**
     * INTERNAL_SERVER_ERROR로 분류된 예외는 하위 계층(DB 등) 원본 메시지를 그대로 감싼 것일 수 있어
     * (CWE-209 오류 메시지를 통한 정보노출) 클라이언트 응답에는 일반화된 메시지만 노출하고, 원본
     * 메시지·스택트레이스는 서버 로그로만 남긴다. 그 외 명시적 오류코드(입력값 검증 등 개발자가
     * 의도적으로 설정한 사용자 대상 메시지)는 기존처럼 그대로 전달한다.
     */
    private String resolveResponseMessage(EgovErrorCode egovErrorCode, String originalMessage, Throwable ex) {
        if (egovErrorCode == EgovErrorCode.INTERNAL_SERVER_ERROR) {
            LOGGER.error("Internal error while processing request: {}", originalMessage, ex);
            return egovErrorCode.getMessage();
        }
        return originalMessage;
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
        response.getHeaders().setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", timestamp);
        map.put("status", status);
        map.put("code", code);
        map.put("message", message);
        JSONObject jsonObject = new JSONObject(map);
        DataBuffer dataBuffer = response.bufferFactory().wrap(JSONObject.toJSONString(jsonObject).getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(dataBuffer));
    }

}
