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
package org.egovframe.rte.fdl.cmmn.exception;

import org.springframework.context.MessageSource;

import java.util.Locale;

/**
 * unchecked 업무 예외.
 *
 * <p>checked 인 {@link EgovBizException} 과 달리 메소드 시그니처를 바꾸지 않고 전파되는 업무 예외이다.
 * {@link EgovErrorMessage} 로 코드·사용자 메시지·원인·조치를 구조화해 보유하며, 원인 예외는 cause 체인
 * ({@link #getCause()})에 연결된다. 서비스 구현체에서는 {@code EgovAbstractServiceImpl#newBizRuntimeException}
 * 헬퍼로 만드는 것을 권장한다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.09.10	실행환경 개발팀		최초 생성
 * </pre>
 */
public class EgovBizRuntimeException extends BaseRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 구조화 오류 메시지. 단순 메시지 생성자로 만든 경우 null 이다.
     */
    private EgovErrorMessage errorMessage;

    /**
     * 구조화 오류 메시지로 생성한다. 예외 메시지는 사용자 메시지, 메시지 키는 오류 코드가 된다.
     *
     * @param errorMessage 구조화 오류 메시지
     * @param cause        원인 예외(null 가능)
     */
    public EgovBizRuntimeException(EgovErrorMessage errorMessage, Throwable cause) {
        super(errorMessage.getUserMessage(), (Object[]) null, cause);
        this.messageKey = errorMessage.getCode();
        this.errorMessage = errorMessage;
    }

    /**
     * 메시지 리소스 키로 생성한다. 구조화 메시지는 키 규약({@code key}, {@code key.reason}, {@code key.solution})으로
     * 함께 해석한다.
     *
     * @param messageSource 메시지 리소스
     * @param messageKey    메시지 키
     * @param messageArgs   메시지 치환 인자(없으면 null)
     * @param locale        로케일(Locale)
     * @param cause         원인 예외(null 가능)
     */
    public EgovBizRuntimeException(MessageSource messageSource, String messageKey, Object[] messageArgs, Locale locale, Throwable cause) {
        this(EgovErrorMessage.resolve(messageSource, messageKey, messageArgs, locale), cause);
        this.messageParameters = messageArgs;
    }

    /**
     * 단순 메시지로 생성한다.
     *
     * @param message 메시지
     * @param cause   원인 예외(null 가능)
     */
    public EgovBizRuntimeException(String message, Throwable cause) {
        super(message, (Object[]) null, cause);
    }

    /**
     * 구조화 오류 메시지. 단순 메시지 생성자로 만든 경우 null 이다.
     *
     * @return 구조화 오류 메시지 또는 null
     */
    public EgovErrorMessage getErrorMessage() {
        return errorMessage;
    }

}
