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
package org.egovframe.rte.fdl.cmmn;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizRuntimeException;
import org.egovframe.rte.fdl.cmmn.exception.EgovErrorMessage;
import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * 비즈니스 서비스 구현체가 상속받는 추상클래스
 * <p><b>NOTE:</b> 비즈니스 서비스 구현시 디폴드로 Exception 발생을 위한 processException 메소드와
 * leaveaTrace 메소드를 가지고 있다. processException / leaveaTrace 를 여러스타일의 파라미터를 취할 수 있도록 제공하고 있다.
 * 또한 EgovAbstractServiceImpl을 상속하는 클래스는 직접 Logger 생성없이
 * protected로 선언된 egovLogger를 사용할 수 있다.</p>
 *
 * <p>업무 예외를 던질 때는 {@link #throwBizException(String)} (checked) 또는
 * {@code throw} {@link #newBizRuntimeException(String)} (unchecked) 를 쓸 수 있다. 두 헬퍼는 메시지 키 규약
 * ({@code key}, {@code key.reason}, {@code key.solution})으로 구조화 오류 메시지({@link EgovErrorMessage})를
 * 해석해 예외에 담는다. {@code processException} 은 예외를 반환하므로 호출부에서 {@code throw} 를 빠뜨리면
 * 아무 일도 일어나지 않는데, {@code throwBizException} 은 그 자리에서 던진다.</p>
 *
 * @author Daniela Kwon
 * @version 3.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2014.06.01	Daniela Kwon		최초생성
 *   2026-09-05  이백행          [2026년 컨트리뷰션] 서비스 로거와 추적 로케일 처리 수정
 * 2026.09.10	실행환경 개발팀		구조화 오류 메시지를 담는 throwBizException·newBizRuntimeException 헬퍼 추가
 * </pre>
 * @since 2014.06.01
 */
public abstract class EgovAbstractServiceImpl {

    protected Logger egovLogger = LoggerFactory.getLogger(getClass());

    @Resource(name = "messageSource")
    private MessageSource messageSource;

    @Resource(name = "leaveaTrace")
    private LeaveaTrace traceObj;

    /**
     * EgovBizException 발생을 위한 메소드.
     *
     * @param msgKey 메세지리소스에서 제공되는 메세지의 키값
     * @return Exception EgovBizException 객체
     */
    protected Exception processException(final String msgKey) {
        return processException(msgKey, new String[]{});
    }

    /**
     * EgovBizException 발생을 위한 메소드.
     *
     * @param msgKey    메세지리소스에서 제공되는 메세지의 키값
     * @param exception 발생한 Exception(내부적으로 취하고 있다가 에러핸들링시 사용)
     * @return Exception EgovBizException 객체
     */
    protected Exception processException(final String msgKey, Exception exception) {
        return processException(msgKey, new String[]{}, exception);
    }

    /**
     * EgovBizException 발생을 위한 메소드.
     *
     * @param msgKey  메세지리소스에서 제공되는 메세지의 키값
     * @param msgArgs msgKey의 메세지에서 변수에 취환되는 값들
     * @return Exception EgovBizException 객체
     */
    protected Exception processException(final String msgKey, final String[] msgArgs) {
        return processException(msgKey, msgArgs, null);
    }

    /**
     * EgovBizException 발생을 위한 메소드.
     *
     * @param msgKey    메세지리소스에서 제공되는 메세지의 키값
     * @param msgArgs   msgKey의 메세지에서 변수에 취환되는 값들
     * @param exception 발생한 Exception(내부적으로 취하고 있다가 에러핸들링시 사용)
     * @return Exception EgovBizException 객체
     */
    protected Exception processException(final String msgKey, final String[] msgArgs, final Exception exception) {
        return processException(msgKey, msgArgs, exception, LocaleContextHolder.getLocale());
    }

    /**
     * EgovBizException 발생을 위한 메소드.
     *
     * @param msgKey    메세지리소스에서 제공되는 메세지의 키값
     * @param msgArgs   msgKey의 메세지에서 변수에 취환되는 값들
     * @param exception 발생한 Exception(내부적으로 취하고 있다가 에러핸들링시 사용)
     * @param locale    명시적 국가/언어지정
     * @return Exception EgovBizException 객체
     */
    protected Exception processException(final String msgKey, final String[] msgArgs, final Exception exception, Locale locale) {
        return processException(msgKey, msgArgs, exception, locale, null);
    }

    /**
     * EgovBizException 발생을 위한 메소드.
     *
     * @param msgKey           메세지리소스에서 제공되는 메세지의 키값
     * @param msgArgs          msgKey의 메세지에서 변수에 취환되는 값들
     * @param exception        발생한 Exception(내부적으로 취하고 있다가 에러핸들링시 사용)
     * @param locale           명시적 국가/언어지정
     * @param exceptionCreator 외부에서 별도의 Exception 생성기 지정
     * @return Exception EgovBizException 객체
     */
    protected Exception processException(final String msgKey, final String[] msgArgs, final Exception exception, final Locale locale, ExceptionCreator exceptionCreator) {
        ExceptionCreator eC = null;
        if (exceptionCreator == null) {
            eC = new ExceptionCreator() {
                public Exception createBizException(MessageSource messageSource) {
                    return new EgovBizException(messageSource, msgKey, msgArgs, locale, exception);
                }
            };
        } else {
            eC = exceptionCreator;
        }
        return eC.createBizException(messageSource);
    }

    /**
     * Exception 발생없이 후처리로직 실행을 위한 메소드.
     *
     * @param msgKey 메세지리소스에서 제공되는 메세지의 키값
     */
    protected void leaveaTrace(String msgKey) {
        leaveaTrace(msgKey, new String[]{});
    }

    /**
     * Exception 발생없이 후처리로직 실행을 위한 메소드.
     *
     * @param msgKey  메세지리소스에서 제공되는 메세지의 키값
     * @param msgArgs msgKey의 메세지에서 변수에 취환되는 값들
     */
    protected void leaveaTrace(String msgKey, String[] msgArgs) {
        leaveaTrace(msgKey, msgArgs, LocaleContextHolder.getLocale());
    }

    /**
     * Exception 발생없이 후처리로직 실행을 위한 메소드.
     *
     * @param msgKey  메세지리소스에서 제공되는 메세지의 키값
     * @param msgArgs msgKey의 메세지에서 변수에 취환되는 값들
     * @param locale  명시적 국가/언어지정
     */
    protected void leaveaTrace(String msgKey, String[] msgArgs, Locale locale) {
        traceObj.trace(this.getClass(), messageSource, msgKey, msgArgs, locale, egovLogger);
    }

    /**
     * 구조화 오류 메시지를 담은 {@link EgovBizException} 을 그 자리에서 던진다.
     *
     * <pre class="code">
     * if (stock &lt; quantity) {
     *     throwBizException("fail.biz.order.stock");
     * }
     * </pre>
     *
     * @param messageKey 메세지리소스 키. {@code key}/{@code key.reason}/{@code key.solution} 규약으로 구조화 해석
     * @throws EgovBizException 항상 발생
     */
    protected void throwBizException(String messageKey) throws EgovBizException {
        throwBizException(messageKey, null, null, LocaleContextHolder.getLocale());
    }

    /**
     * 구조화 오류 메시지를 담은 {@link EgovBizException} 을 그 자리에서 던진다.
     *
     * @param messageKey  메세지리소스 키
     * @param messageArgs 메시지 치환 인자
     * @throws EgovBizException 항상 발생
     */
    protected void throwBizException(String messageKey, Object[] messageArgs) throws EgovBizException {
        throwBizException(messageKey, messageArgs, null, LocaleContextHolder.getLocale());
    }

    /**
     * 구조화 오류 메시지를 담은 {@link EgovBizException} 을 그 자리에서 던진다.
     *
     * @param messageKey 메세지리소스 키
     * @param cause      원인 예외(cause 체인에 연결된다)
     * @throws EgovBizException 항상 발생
     */
    protected void throwBizException(String messageKey, Throwable cause) throws EgovBizException {
        throwBizException(messageKey, null, cause, LocaleContextHolder.getLocale());
    }

    /**
     * 구조화 오류 메시지를 담은 {@link EgovBizException} 을 그 자리에서 던진다.
     *
     * @param messageKey  메세지리소스 키
     * @param messageArgs 메시지 치환 인자
     * @param cause       원인 예외(cause 체인에 연결된다)
     * @throws EgovBizException 항상 발생
     */
    protected void throwBizException(String messageKey, Object[] messageArgs, Throwable cause) throws EgovBizException {
        throwBizException(messageKey, messageArgs, cause, LocaleContextHolder.getLocale());
    }

    /**
     * 구조화 오류 메시지를 담은 {@link EgovBizException} 을 그 자리에서 던진다.
     *
     * @param messageKey  메세지리소스 키
     * @param messageArgs 메시지 치환 인자
     * @param cause       원인 예외(cause 체인에 연결된다)
     * @param locale      명시적 국가/언어지정
     * @throws EgovBizException 항상 발생
     */
    protected void throwBizException(String messageKey, Object[] messageArgs, Throwable cause, Locale locale) throws EgovBizException {
        throw new EgovBizException(EgovErrorMessage.resolve(messageSource, messageKey, messageArgs, locale), cause);
    }

    /**
     * 구조화 오류 메시지를 담은 unchecked {@link EgovBizRuntimeException} 을 만든다. 반드시 {@code throw} 와 함께 쓴다.
     *
     * <pre class="code">
     * throw newBizRuntimeException("fail.biz.order.stock");
     * </pre>
     *
     * @param messageKey 메세지리소스 키. {@code key}/{@code key.reason}/{@code key.solution} 규약으로 구조화 해석
     * @return 만든 unchecked 예외. 호출부에서 바로 던진다
     */
    protected EgovBizRuntimeException newBizRuntimeException(String messageKey) {
        return newBizRuntimeException(messageKey, null, null);
    }

    /**
     * 구조화 오류 메시지를 담은 unchecked {@link EgovBizRuntimeException} 을 만든다.
     *
     * @param messageKey  메세지리소스 키
     * @param messageArgs 메시지 치환 인자
     * @return 만든 unchecked 예외. 호출부에서 바로 던진다
     */
    protected EgovBizRuntimeException newBizRuntimeException(String messageKey, Object[] messageArgs) {
        return newBizRuntimeException(messageKey, messageArgs, null);
    }

    /**
     * 구조화 오류 메시지를 담은 unchecked {@link EgovBizRuntimeException} 을 만든다.
     *
     * @param messageKey  메세지리소스 키
     * @param messageArgs 메시지 치환 인자
     * @param cause       원인 예외(cause 체인에 연결된다)
     * @return 만든 unchecked 예외. 호출부에서 바로 던진다
     */
    protected EgovBizRuntimeException newBizRuntimeException(String messageKey, Object[] messageArgs, Throwable cause) {
        return newBizRuntimeException(messageKey, messageArgs, cause, LocaleContextHolder.getLocale());
    }

    /**
     * 구조화 오류 메시지를 담은 unchecked {@link EgovBizRuntimeException} 을 만든다.
     *
     * @param messageKey  메세지리소스 키
     * @param messageArgs 메시지 치환 인자
     * @param cause       원인 예외(cause 체인에 연결된다)
     * @param locale      명시적 국가/언어지정
     * @return 만든 unchecked 예외. 호출부에서 바로 던진다
     */
    protected EgovBizRuntimeException newBizRuntimeException(String messageKey, Object[] messageArgs, Throwable cause, Locale locale) {
        return new EgovBizRuntimeException(EgovErrorMessage.resolve(messageSource, messageKey, messageArgs, locale), cause);
    }

    protected interface ExceptionCreator {
        Exception createBizException(MessageSource messageSource);
    }

}
