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
 * EgovBizException : 비즈니스 서비스 구현체에서 발생시키는 Biz Exception .
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
public class EgovBizException extends BaseException {

	private static final long serialVersionUID = 1L;

	/**
	 * EgovBizException 생성자.
	 */
	public EgovBizException() {
		this("BaseException without message", null, null);
	}

	/**
	 * EgovBizException 생성자.
	 * @param defaultMessage 메세지 지정
	 */
	public EgovBizException(String defaultMessage) {
		this(defaultMessage, null, null);
	}

	/**
	 * EgovBizException 생성자.
	 * @param defaultMessage 메세지 지정
	 * @param wrappedException 원인 Exception
	 */
	public EgovBizException(String defaultMessage, Exception wrappedException) {
		this(defaultMessage, null, wrappedException);
	}

	/**
	 * EgovBizException 생성자.
	 * @param defaultMessage 메세지 지정(변수지정)
	 * @param messageParameters 치환될 메세지 리스트
	 * @param wrappedException 원인 Exception
	 */
	public EgovBizException(String defaultMessage, Object[] messageParameters, Exception wrappedException) {
		String userMessage = defaultMessage;
		if (messageParameters != null) {
			userMessage = MessageFormat.format(defaultMessage, messageParameters);
		}
		this.message = userMessage;
		this.wrappedException = wrappedException;
	}

	/**
	 * EgovBizException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 */
	public EgovBizException(MessageSource messageSource, String messageKey) {
		this(messageSource, messageKey, null, null, Locale.getDefault(), null);
	}

	/**
	 * EgovBizException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param wrappedException 원인 Exception
	 */
	public EgovBizException(MessageSource messageSource, String messageKey, Exception wrappedException) {
		this(messageSource, messageKey, null, null, Locale.getDefault(), wrappedException);
	}

	/**
	 * EgovBizException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param locale 국가/언어지정
	 * @param wrappedException 원인 Exception
	 */
	public EgovBizException(MessageSource messageSource, String messageKey, Locale locale, Exception wrappedException) {
		this(messageSource, messageKey, null, null, locale, wrappedException);
	}

	/**
	 * EgovBizException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param messageParameters 치환될 메세지 리스트
	 * @param locale 국가/언어지정
	 * @param wrappedException 원인 Exception
	 */
	public EgovBizException(MessageSource messageSource, String messageKey, Object[] messageParameters, Locale locale, Exception wrappedException) {
		this(messageSource, messageKey, messageParameters, null, locale, wrappedException);
	}

	/**
	 * EgovBizException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param messageParameters 치환될 메세지 리스트
	 * @param wrappedException 원인 Exception
	 */
	public EgovBizException(MessageSource messageSource, String messageKey, Object[] messageParameters, Exception wrappedException) {
		this(messageSource, messageKey, messageParameters, null, Locale.getDefault(), wrappedException);
	}

	/**
	 * EgovBizException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param messageParameters 치환될 메세지 리스트
	 * @param defaultMessage 기본 메시지
	 * @param wrappedException 원인 Exception
	 */
	public EgovBizException(MessageSource messageSource, String messageKey, Object[] messageParameters, String defaultMessage, Exception wrappedException) {
		this(messageSource, messageKey, messageParameters, defaultMessage, Locale.getDefault(), wrappedException);
	}

	/**
	 *  EgovBizException 생성자.
	 * @param messageSource 메세지 리소스
	 * @param messageKey 메세지키값
	 * @param messageParameters 치환될 메세지 리스트
	 * @param defaultMessage 기본 메시지
	 * @param locale 국가/언어지정
	 * @param wrappedException 원인 Exception
	 */
	public EgovBizException(MessageSource messageSource, String messageKey, Object[] messageParameters, String defaultMessage, Locale locale, Exception wrappedException) {
		this.messageKey = messageKey;
		this.messageParameters = messageParameters;
		this.message = messageSource.getMessage(messageKey, messageParameters, defaultMessage, locale);
		this.wrappedException = wrappedException;
	}

}
