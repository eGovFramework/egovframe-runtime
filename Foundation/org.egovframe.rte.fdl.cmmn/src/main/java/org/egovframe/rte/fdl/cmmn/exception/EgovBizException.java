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
package org.egovframe.rte.fdl.cmmn.exception;

import org.springframework.context.MessageSource;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * EgovBizException : 비즈니스 서비스 구현체에서 발생시키는 Biz Exception .
 *
 * @author Judd Cho (horanghi@gmail.com)
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.05.30	Judd Cho			최초 생성
 * 2015.01.31	Vincent Han			코드 품질 개선
 * 2026.09.10	실행환경 개발팀		구조화 오류 메시지(EgovErrorMessage) 생성자와 getErrorMessage() 추가
 * </pre>
 * @since 2009.06.01
 */
public class EgovBizException extends BaseException {

    private static final long serialVersionUID = 1L;

    /**
     * 구조화 오류 메시지. 구조화 생성자로 만든 경우에만 설정되며 그 외에는 null 이다.
     */
    private EgovErrorMessage errorMessage;

    /**
     * EgovBizException 생성자.
     */
    public EgovBizException() {
        this("EgovBizException without message", null, null);
    }

    /**
     * EgovBizException 생성자.
     *
     * @param defaultMessage 메세지 지정
     */
    public EgovBizException(String defaultMessage) {
        this(defaultMessage, null, null);
    }

    /**
     * EgovBizException 생성자.
     *
     * @param defaultMessage   메세지 지정
     * @param wrappedException 원인 Exception
     */
    public EgovBizException(String defaultMessage, Exception wrappedException) {
        this(defaultMessage, null, wrappedException);
    }

    /**
     * EgovBizException 생성자.
     *
     * @param defaultMessage    메세지 지정(변수지정)
     * @param messageParameters 치환될 메세지 리스트
     * @param wrappedException  원인 Exception
     */
    public EgovBizException(String defaultMessage, Object[] messageParameters, Exception wrappedException) {
        super(wrappedException);
        String userMessage = defaultMessage;
        if (messageParameters != null) {
            userMessage = MessageFormat.format(defaultMessage, messageParameters);
        }
        this.message = userMessage;
        this.wrappedException = wrappedException;
    }

    /**
     * EgovBizException 생성자.
     *
     * @param messageSource 메세지 리소스
     * @param messageKey    메세지키값
     */
    public EgovBizException(MessageSource messageSource, String messageKey) {
        this(messageSource, messageKey, null, null, Locale.getDefault(), null);
    }

    /**
     * EgovBizException 생성자.
     *
     * @param messageSource    메세지 리소스
     * @param messageKey       메세지키값
     * @param wrappedException 원인 Exception
     */
    public EgovBizException(MessageSource messageSource, String messageKey, Exception wrappedException) {
        this(messageSource, messageKey, null, null, Locale.getDefault(), wrappedException);
    }

    /**
     * EgovBizException 생성자.
     *
     * @param messageSource    메세지 리소스
     * @param messageKey       메세지키값
     * @param locale           국가/언어지정
     * @param wrappedException 원인 Exception
     */
    public EgovBizException(MessageSource messageSource, String messageKey, Locale locale, Exception wrappedException) {
        this(messageSource, messageKey, null, null, locale, wrappedException);
    }

    /**
     * EgovBizException 생성자.
     *
     * @param messageSource     메세지 리소스
     * @param messageKey        메세지키값
     * @param messageParameters 치환될 메세지 리스트
     * @param locale            국가/언어지정
     * @param wrappedException  원인 Exception
     */
    public EgovBizException(MessageSource messageSource, String messageKey, Object[] messageParameters, Locale locale, Exception wrappedException) {
        this(messageSource, messageKey, messageParameters, null, locale, wrappedException);
    }

    /**
     * EgovBizException 생성자.
     *
     * @param messageSource     메세지 리소스
     * @param messageKey        메세지키값
     * @param messageParameters 치환될 메세지 리스트
     * @param wrappedException  원인 Exception
     */
    public EgovBizException(MessageSource messageSource, String messageKey, Object[] messageParameters, Exception wrappedException) {
        this(messageSource, messageKey, messageParameters, null, Locale.getDefault(), wrappedException);
    }

    /**
     * EgovBizException 생성자.
     *
     * @param messageSource     메세지 리소스
     * @param messageKey        메세지키값
     * @param messageParameters 치환될 메세지 리스트
     * @param defaultMessage    기본 메시지
     * @param wrappedException  원인 Exception
     */
    public EgovBizException(MessageSource messageSource, String messageKey, Object[] messageParameters, String defaultMessage, Exception wrappedException) {
        this(messageSource, messageKey, messageParameters, defaultMessage, Locale.getDefault(), wrappedException);
    }

    /**
     * EgovBizException 생성자.
     *
     * @param messageSource     메세지 리소스
     * @param messageKey        메세지키값
     * @param messageParameters 치환될 메세지 리스트
     * @param defaultMessage    기본 메시지
     * @param locale            국가/언어지정
     * @param wrappedException  원인 Exception
     */
    public EgovBizException(MessageSource messageSource, String messageKey, Object[] messageParameters, String defaultMessage, Locale locale, Exception wrappedException) {
        super(wrappedException);
        this.messageKey = messageKey;
        this.messageParameters = messageParameters;
        this.message = messageSource.getMessage(messageKey, messageParameters, defaultMessage, locale);
        this.wrappedException = wrappedException;
    }

    /**
     * 구조화 오류 메시지로 생성한다. 예외 메시지는 사용자 메시지, 메시지 키는 오류 코드가 되며
     * 원인 예외는 cause 체인에 연결된다.
     *
     * @param errorMessage 구조화 오류 메시지(코드·사용자 메시지·원인·조치)
     * @param cause        원인 예외(null 가능)
     */
    public EgovBizException(EgovErrorMessage errorMessage, Throwable cause) {
        super(errorMessage.getUserMessage(), (Object[]) null, cause);
        this.messageKey = errorMessage.getCode();
        this.errorMessage = errorMessage;
    }

    /**
     * 구조화 오류 메시지. 구조화 생성자로 만든 경우에만 값이 있고 그 외에는 null 이다.
     *
     * @return 구조화 오류 메시지 또는 null
     */
    public EgovErrorMessage getErrorMessage() {
        return errorMessage;
    }

}
