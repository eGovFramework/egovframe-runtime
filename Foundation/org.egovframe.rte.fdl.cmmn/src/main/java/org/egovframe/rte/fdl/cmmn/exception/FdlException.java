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
 * 전자정부 표준프레임워크에서 사용하는 Exception.
 *
 * @author Judd Cho (horanghi@gmail.com)
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.05.30	Judd Cho			최초 생성
 * 2015.01.31	Vincent Han			코드 품질 개선
 * </pre>
 */
public class FdlException extends BaseException {

	private static final long serialVersionUID = 1L;

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

	/**
	 * FdlException 생성자.
	 */
	public FdlException() {
		this("FdlException without message", null, null);
	}

	/**
	 * FdlException 생성자.
	 * @param defaultMessage 기본 메시지
	 */
	public FdlException(String defaultMessage) {
		this(defaultMessage, null, null);
	}

	/**
	 * FdlException 생성자.
	 * @param defaultMessage 기본 메시지
	 * @param wrappedException 원인 Exception
	 */
	public FdlException(String defaultMessage, Throwable wrappedException) {
		this(defaultMessage, null, wrappedException);
	}

	/**
	 * FdlException 생성자.
	 * @param defaultMessage 기본 메시지
	 * @param messageParameters 치환될 메세지 리스트
	 * @param wrappedException 원인 Exception
	 */
	public FdlException(String defaultMessage, Object[] messageParameters, Throwable wrappedException) {
		super(wrappedException);
		String userMessage = defaultMessage;
		if (messageParameters != null) {
			userMessage = MessageFormat.format(defaultMessage, messageParameters);
		}
		this.message = userMessage;
	}

	/**
	 * FdlException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 */
	public FdlException(MessageSource messageSource, String messageKey) {
		this(messageSource, messageKey, null, null, Locale.getDefault(), null);
	}

	/**
	 * FdlException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param wrappedException 원인 Exception
	 */
	public FdlException(MessageSource messageSource, String messageKey, Throwable wrappedException) {
		this(messageSource, messageKey, null, null, Locale.getDefault(), wrappedException);
	}

	/**
	 * FdlException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param locale 국가/언어지정
	 * @param wrappedException 원인 Exception
	 */
	public FdlException(MessageSource messageSource, String messageKey, Locale locale, Throwable wrappedException) {
		this(messageSource, messageKey, null, null, locale, wrappedException);
	}

	/**
	 * FdlException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param messageParameters 치환될 메세지 리스트
	 * @param locale 국가/언어지정
	 * @param wrappedException 원인 Exception
	 */
	public FdlException(MessageSource messageSource, String messageKey, Object[] messageParameters, Locale locale, Throwable wrappedException) {
		this(messageSource, messageKey, messageParameters, null, locale, wrappedException);
	}

	/**
	 * FdlException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param messageParameters 치환될 메세지 리스트
	 * @param wrappedException 원인 Exception
	 */
	public FdlException(MessageSource messageSource, String messageKey, Object[] messageParameters, Throwable wrappedException) {
		this(messageSource, messageKey, messageParameters, null, Locale.getDefault(), wrappedException);
	}

	/**
	 * FdlException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param messageParameters 치환될 메세지 리스트
	 * @param defaultMessage 기본 메시지
	 * @param wrappedException 원인 Exception
	 */
	public FdlException(MessageSource messageSource, String messageKey, Object[] messageParameters, String defaultMessage, Throwable wrappedException) {
		this(messageSource, messageKey, messageParameters, defaultMessage, Locale.getDefault(), wrappedException);
	}

	/**
	 * FdlException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param messageParameters 치환될 메세지 리스트
	 * @param defaultMessage 기본 메시지
	 * @param locale 국가/언어지정
	 * @param wrappedException 원인 Exception
	 */
	public FdlException(MessageSource messageSource, String messageKey, Object[] messageParameters, String defaultMessage, Locale locale, Throwable wrappedException) {
		super(wrappedException);
		this.messageKey = messageKey;
		this.messageParameters = messageParameters;
		this.message = messageSource.getMessage(messageKey, messageParameters, defaultMessage, locale);
	}

}
