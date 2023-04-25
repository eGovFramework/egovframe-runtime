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
package org.egovframe.rte.fdl.cmmn.exception;

import org.springframework.context.MessageSource;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * BaseRuntimeException 은 EgovBizException의 상위클래스이다.
 *
 * <p><b>NOTE:</b> Exception Handling 상의 BaseRuntimeException 은
 * 상속받은 자식 Exception에 대한 throw시 service > controller 전달 할수 있도록 구성 되어 있다.
 *
 * @author 장동한
 * @since 2017.07.25
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2017.07.25	장동한				최초생성
 * </pre>
 */
public class BaseRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	protected String message = null;
	protected String messageKey = null;
	protected Object[] messageParameters = null;
	protected Exception wrappedException = null;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	public Object[] getMessageParameters() {
		return messageParameters;
	}

	public void setMessageParameters(Object[] messageParameters) {
		this.messageParameters = messageParameters;
	}

	public Throwable getWrappedException() {
		return wrappedException;
	}

	public void setWrappedException(Exception wrappedException) {
		this.wrappedException = wrappedException;
	}

	/**
	 * BaseRuntimeException 기본 생성자.
	 */
	public BaseRuntimeException() {
		this("BaseRuntimeException without message", null, null);
	}

	/**
	 * BaseRuntimeException 생성자.
	 * @param defaultMessage 메세지 지정
	 */
	public BaseRuntimeException(String defaultMessage) {
		this(defaultMessage, null, null);
	}

	/**
	 * BaseRuntimeException 생성자.
	 * @param wrappedException  원인 Exception
	 */
	public BaseRuntimeException(Throwable wrappedException) {
		this("BaseRuntimeException without message", null, wrappedException);
	}

	/**
	 * BaseRuntimeException 생성자.
	 * @param defaultMessage 메세지 지정
	 * @param wrappedException 원인 Exception
	 */
	public BaseRuntimeException(String defaultMessage, Throwable wrappedException) {
		this(defaultMessage, null, wrappedException);
	}

	/**
	 * BaseRuntimeException 생성자.
	 * @param defaultMessage 메세지 지정(변수지정)
	 * @param messageParameters 치환될 메세지 리스트
	 * @param wrappedException 원인 Exception
	 */
	public BaseRuntimeException(String defaultMessage, Object[] messageParameters, Throwable wrappedException) {
		super(wrappedException);
		String userMessage = defaultMessage;
		if (messageParameters != null) {
			userMessage = MessageFormat.format(defaultMessage, messageParameters);
		}
		this.message = userMessage;
	}

	/**
	 * BaseRuntimeException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 */
	public BaseRuntimeException(MessageSource messageSource, String messageKey) {
		this(messageSource, messageKey, null, null, Locale.getDefault(), null);
	}

	/**
	 * BaseRuntimeException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 */
	public BaseRuntimeException(MessageSource messageSource, String messageKey, Throwable wrappedException) {
		this(messageSource, messageKey, null, null, Locale.getDefault(), wrappedException);
	}

	/**
	 * BaseRuntimeException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param locale 국가/언어지정
	 * @param wrappedException 원인 Exception
	 */
	public BaseRuntimeException(MessageSource messageSource, String messageKey, Locale locale, Throwable wrappedException) {
		this(messageSource, messageKey, null, null, locale, wrappedException);
	}

	/**
	 * BaseRuntimeException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param messageParameters 치환될 메세지 리스트
	 * @param locale 국가/언어지정
	 * @param wrappedException 원인 Exception
	 */
	public BaseRuntimeException(MessageSource messageSource, String messageKey, Object[] messageParameters, Locale locale, Throwable wrappedException) {
		this(messageSource, messageKey, messageParameters, null, locale, wrappedException);
	}

	/**
	 * BaseRuntimeException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param messageParameters 치환될 메세지 리스트
	 * @param wrappedException 원인 Exception
	 */
	public BaseRuntimeException(MessageSource messageSource, String messageKey, Object[] messageParameters, Throwable wrappedException) {
		this(messageSource, messageKey, messageParameters, null, Locale.getDefault(), wrappedException);
	}

	/**
	 * BaseRuntimeException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param messageParameters 치환될 메세지 리스트
	 * @param defaultMessage 메세지 지정(변수지정)
	 * @param wrappedException 원인 Exception
	 */
	public BaseRuntimeException(MessageSource messageSource, String messageKey, Object[] messageParameters, String defaultMessage, Throwable wrappedException) {
		this(messageSource, messageKey, messageParameters, defaultMessage, Locale.getDefault(), wrappedException);
	}

	/**
	 * BaseRuntimeException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param messageParameters 치환될 메세지 리스트
	 * @param defaultMessage 메세지 지정(변수지정)
	 * @param locale 국가/언어지정
	 * @param wrappedException 원인 Exception
	 */
	public BaseRuntimeException(MessageSource messageSource, String messageKey, Object[] messageParameters, String defaultMessage, Locale locale, Throwable wrappedException) {
		super(wrappedException);
		this.messageKey = messageKey;
		this.messageParameters = messageParameters;
		this.message = messageSource.getMessage(messageKey, messageParameters, defaultMessage, locale);
	}

}
