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
package org.egovframe.rte.fdl.cmmn.trace;

import org.egovframe.rte.fdl.cmmn.trace.manager.TraceHandlerService;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * LeaveaTrace.
 *
 * <p><b>NOTE:</b> LeaveaTrace 클래스의 목적은 Exception 을 발생하지 않고 후처리로직만을 실행하고자 만들어졌다.
 * EgovAbstractServiceImpl 를 상속받은 경우 leavaTrace(메세지키) 형태로 실행을 하면 설정에 등록된 후처리 로직을 실행한다.
 * 그리고 나서 원래 로직으로 돌아와 나머지 수행을 한다. </p>
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
 * 2020.08.31	ESFC				시큐어코딩(ES)-Private 배열에 Public 데이터 할당[CWE-496]
 * </pre>
 */
public class LeaveaTrace {

	@Resource(name = "messageSource")
	private MessageSource messageSource;

	private TraceHandlerService[] traceHandlerServices;

	private PathMatcher pm = new AntPathMatcher();

	/**
	 * setTraceHandlerServices.
	 * @param traceHandlerServices 실행하고자 하는 Handler 를 가진 Manager서비스
	 */
	public void setTraceHandlerServices(TraceHandlerService[] traceHandlerServices) {
		// 2020.08.31 ESFC 시큐어코딩(ES)-Private 배열에 Public 데이터 할당[CWE-496]
		this.traceHandlerServices = new TraceHandlerService[traceHandlerServices.length];
		for (int i = 0; i < traceHandlerServices.length; i++) {
			this.traceHandlerServices[i] = traceHandlerServices[i];
		}
	}

	/**
	 * 등록된 TraceHandlerService 갯수를 리턴한다.
	 * @return TraceHandlerService 갯수
	 */
	public int countOfTheTraceHandlerService() {
		return (traceHandlerServices != null) ? traceHandlerServices.length : 0;
	}

	/**
	 * trace 메소드.
	 * @param msgKey 메세지를 가져오기 위한 메세지키값
	 * @param clazz leaveaTrace 실행 위치에 클래스 정보
	 */
	public void trace(String msgKey, Class<?> clazz) {
		this.trace(msgKey, new String[] {}, clazz);
	}

	/**
	 * trace 메소드.
	 * @param msgKey 메세지를 가져오기 위한 메세지키값
	 * @param msgArgs 메세지 값의 변수를 치환하기 위한 값리스트
	 * @param clazz leaveaTrace 실행 위치에 클래스 정보
	 */
	public void trace(String msgKey, String[] msgArgs, Class<?> clazz) {
		this.trace(msgKey, msgArgs, null, clazz);
	}

	/**
	 * trace 메소드.
	 * @param msgKey 메세지를 가져오기 위한 메세지키값
	 * @param msgArgs 메세지 값의 변수를 치환하기 위한 값리스트
	 * @param clazz leaveaTrace 실행 위치에 클래스 정보
	 */
	public void trace(String msgKey, String[] msgArgs, Locale locale, Class<?> clazz) {
		this.trace(clazz, messageSource, msgKey, msgArgs, locale, null);
	}

	/**
	 * trace 메소드.
	 * @param clazz leaveaTrace 실행 위치에 클래스 정보
	 * @param messageKey 메세지를 가져오기 위한 메세지키값
	 * @param messageParameters 메세지 값의 변수를 치환하기 위한 값리스트
	 * @param locale 국가/언어지정
	 */
	public void trace(Class<?> clazz, MessageSource messageSource, String messageKey, Object[] messageParameters, Locale locale) {
		this.trace(clazz, messageSource, messageKey, messageParameters, locale, null);
	}

	/**
	 * trace 메소드.
	 * @param clazz leaveaTrace 실행 위치에 클래스 정보
	 * @param messageKey 메세지를 가져오기 위한 메세지키값
	 * @param messageParameters 메세지 값의 변수를 치환하기 위한 값리스트
	 * @param locale 국가/언어지정
	 * @param log 로그객체지정
	 */
	public void trace(Class<?> clazz, MessageSource messageSource, String messageKey, Object[] messageParameters, Locale locale, Logger log) {
		String message = messageSource.getMessage(messageKey, messageParameters, null, locale);
		if (log != null) {
			log.info(" LeaveaTrace.trace() this.message =>" + message);
		}
		if (traceHandlerServices == null) {
			return;
		}
		for (TraceHandlerService traceHandlerService : traceHandlerServices) {
			if (traceHandlerService.hasReqExpMatcher()) {
				traceHandlerService.setReqExpMatcher(pm);
			}
			traceHandlerService.setPackageName(clazz.getCanonicalName());
			traceHandlerService.trace(clazz, message);
		}
	}

}
