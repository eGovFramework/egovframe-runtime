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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * EgovExceptionHandler로 보내는 응답을 구성하는 클래스
 *
 * <p>Desc.: EgovExceptionHandler로 보내는 응답을 구성하는 클래스</p>
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

    public static EgovExceptionResponse of(EgovErrorCode egovErrorCode, String message) {
        return new EgovExceptionResponse(egovErrorCode, message);
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
