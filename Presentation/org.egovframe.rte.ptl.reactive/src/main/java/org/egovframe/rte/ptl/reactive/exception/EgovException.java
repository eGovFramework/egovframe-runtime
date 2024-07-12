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

/**
 * 런타임 시 비즈니스 로직상 사용자에게 알려줄 오류 메시지를 만들어 던지는 처리를 담당하는 클래스
 *
 * <p>Desc.: 런타임 시 비즈니스 로직상 사용자에게 알려줄 오류 메시지를 만들어 던지는 처리를 담당하는 클래스</p>
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
public class EgovException extends RuntimeException {

    protected EgovErrorCode egovErrorCode;

    protected String message;

    public EgovException(EgovErrorCode egovErrorCode) {
        this(egovErrorCode, true, null);
    }

    public EgovException(String message) {
        this(EgovErrorCode.INVALID_INPUT_VALUE, message);
    }

    public EgovException(EgovErrorCode egovErrorCode, String message) {
        this(egovErrorCode, false, message);
    }

    public EgovException(EgovErrorCode egovErrorCode, boolean messageCode, String message) {
        this.egovErrorCode = egovErrorCode;
        if (messageCode) message = egovErrorCode.getMessage();
        this.message = message;
    }

    public EgovErrorCode getEgovErrorCode() {
        return egovErrorCode;
    }

    public String getMessage() {
        return message;
    }

}
