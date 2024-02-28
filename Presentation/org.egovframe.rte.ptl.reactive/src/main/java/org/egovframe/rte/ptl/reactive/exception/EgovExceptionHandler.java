/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 발생한 오류를 처리하고 동일한 형식으로 응답을 보내기 위한 클래스
 *
 * <p>Desc.: 발생한 오류를 처리하고 동일한 형식으로 응답을 보내기 위한 클래스</p>
 *
 * @author ESFC
 * @since 2023.08.31
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   ESFC            최초 생성
 * </pre>
 */
@Order(-3)
public class EgovExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
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
        } else {
            EgovErrorCode egovErrorCode = EgovErrorCode.INTERNAL_SERVER_ERROR;
            String message = egovErrorCode.getMessage();
            return getExceptionResponse(response, egovErrorCode, message);
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
        DataBuffer dataBuffer = response.bufferFactory().wrap(JSONObject.toJSONString(jsonObject).getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }

}
