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
 * 비즈니스 서비스 구현체에서 발생시키는 Biz Exception 처리를 담당하는 클래스
 *
 * <p>Desc.: 비즈니스 서비스 구현체에서 발생시키는 Biz Exception 처리를 담당하는 클래스</p>
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
public class EgovServiceException extends RuntimeException {

    protected EgovErrorCode egovErrorCode;

    protected String message;

    public EgovServiceException(String message) {
        this(EgovErrorCode.INTERNAL_SERVER_ERROR, message);
    }

    public EgovServiceException(EgovErrorCode egovErrorCode, String message) {
        this.egovErrorCode = egovErrorCode;
        this.message = message;
    }

    public EgovErrorCode getEgovErrorCode() {
        return egovErrorCode;
    }

    public String getMessage() {
        return message;
    }

}
