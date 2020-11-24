/*
 * Copyright 2014 MOSPA(Ministry of Security and Public Administration).
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

import java.util.Locale;

import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 비즈니스 서비스 구현체가 상속받는 추상클래스
 * <p><b>NOTE:</b> 비즈니스 서비스 구현시 디폴드로 Exception 발생을 위한 processException 메소드와
 * leaveaTrace 메소드를 가지고 있다. processException / leaveaTrace 를 여러스타일의 파라미터를 취할 수 있도록 제공하고 있다.
 * 또한 EgovAbstractServiceImpl을 상속하는 클래스는 직접 Logger 생성없이
 * protected로 선언된 egovLogger를 사용할 수 있다.</p>
 * 
 * @author Daniela Kwon
 * @since 2014.06.01
 * @version 3.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2014.06.01	Daniela Kwon		최초생성
 * </pre>
 */
public abstract class EgovAbstractServiceImpl {

	protected Logger egovLogger = LoggerFactory.getLogger(EgovAbstractServiceImpl.class);

	@Resource(name = "messageSource")
	private MessageSource messageSource;

	@Resource(name = "leaveaTrace")
	private LeaveaTrace traceObj;

	/**
	 * EgovBizException 발생을 위한 메소드.
	 * @param msgKey 메세지리소스에서 제공되는 메세지의 키값
	 * @return Exception EgovBizException 객체
	 */
	protected Exception processException(final String msgKey) {
		return processException(msgKey, new String[] {});
	}

	/**
	 * EgovBizException 발생을 위한 메소드.
	 * @param msgKey 메세지리소스에서 제공되는 메세지의 키값
	 * @param exception 발생한 Exception(내부적으로 취하고 있다가 에러핸들링시 사용)
	 * @return Exception EgovBizException 객체
	 */
	protected Exception processException(final String msgKey, Exception exception) {
		return processException(msgKey, new String[] {}, exception);
	}

	/**
	 * EgovBizException 발생을 위한 메소드.
	 * @param msgKey 메세지리소스에서 제공되는 메세지의 키값
	 * @param msgArgs msgKey의 메세지에서 변수에 취환되는 값들
	 * @return Exception EgovBizException 객체
	 */
	protected Exception processException(final String msgKey, final String[] msgArgs) {
		return processException(msgKey, msgArgs, null);
	}

	/**
	 * EgovBizException 발생을 위한 메소드.
	 * @param msgKey 메세지리소스에서 제공되는 메세지의 키값
	 * @param msgArgs msgKey의 메세지에서 변수에 취환되는 값들
	 * @param exception 발생한 Exception(내부적으로 취하고 있다가 에러핸들링시 사용)
	 * @return Exception EgovBizException 객체
	 */
	protected Exception processException(final String msgKey, final String[] msgArgs, final Exception exception) {
		return processException(msgKey, msgArgs, exception, LocaleContextHolder.getLocale());
	}

	/**
	 * EgovBizException 발생을 위한 메소드.
	 * @param msgKey 메세지리소스에서 제공되는 메세지의 키값
	 * @param msgArgs msgKey의 메세지에서 변수에 취환되는 값들
	 * @param exception 발생한 Exception(내부적으로 취하고 있다가 에러핸들링시 사용)
	 * @param locale 명시적 국가/언어지정
	 * @return Exception EgovBizException 객체
	 */
	protected Exception processException(final String msgKey, final String[] msgArgs, final Exception exception, Locale locale) {
		return processException(msgKey, msgArgs, exception, locale, null);
	}

	/**
	 * EgovBizException 발생을 위한 메소드.
	 * @param msgKey 메세지리소스에서 제공되는 메세지의 키값
	 * @param msgArgs msgKey의 메세지에서 변수에 취환되는 값들
	 * @param exception 발생한 Exception(내부적으로 취하고 있다가 에러핸들링시 사용)
	 * @param locale 명시적 국가/언어지정
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

	protected interface ExceptionCreator {
		Exception createBizException(MessageSource messageSource);
	}

	/**
	 * Exception 발생없이 후처리로직 실행을 위한 메소드.
	 * @param msgKey 메세지리소스에서 제공되는 메세지의 키값
	 */
	protected void leaveaTrace(String msgKey) {
		leaveaTrace(msgKey, new String[] {});
	}

	/**
	 * Exception 발생없이 후처리로직 실행을 위한 메소드.
	 * @param msgKey 메세지리소스에서 제공되는 메세지의 키값
	 * @param msgArgs msgKey의 메세지에서 변수에 취환되는 값들
	 */
	protected void leaveaTrace(String msgKey, String[] msgArgs) {
		leaveaTrace(msgKey, msgArgs, null);
	}

	/**
	 * Exception 발생없이 후처리로직 실행을 위한 메소드.
	 * @param msgKey 메세지리소스에서 제공되는 메세지의 키값
	 * @param msgArgs msgKey의 메세지에서 변수에 취환되는 값들
	 * @param locale 명시적 국가/언어지정
	 */
	protected void leaveaTrace(String msgKey, String[] msgArgs, Locale locale) {
		traceObj.trace(this.getClass(), messageSource, msgKey, msgArgs, locale, egovLogger);
	}

}
