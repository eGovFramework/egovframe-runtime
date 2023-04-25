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
package org.egovframe.rte.fdl.cmmn.trace.manager;

import org.egovframe.rte.fdl.cmmn.trace.handler.TraceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.util.PathMatcher;

import javax.annotation.Resource;

/**
 * AbsTraceHandleManager 클래스는 DefaultTraceHandleManager 클래스의 부모 추상 클래스 이다.
 * TraceHandlerService 인터페이스의 메소드를 거의 구현하여져 있어 별도의 사용자의 목적에 따라
 * 상속 받아 trace 메소드만 재정하여 사용할 수 있다.
 *
 * <p><b>NOTE:</b> 사용자가 TraceHandleManager 를 구현시 상속받을 추상클래스 이다.</p>
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
public abstract class AbstractTraceHandleManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTraceHandleManager.class);

	@Resource(name = "messageSource")
	protected MessageSource messageSource;

	protected String packageName;
	protected String[] patterns;
	protected TraceHandler[] handlers;
	protected PathMatcher pm;

	/**
	 * 패키지, 클래스 이름으로 패턴등록(Ant형식의 매칭). 
	 * @param patterns 패턴리스트
	 */
	public void setPatterns(String[] patterns) {
		this.patterns = patterns;
	}

	/**
	 * ExceptionHandler 리스트 등록.
	 * @param handlers handler리스트
	 */
	public void setHandlers(TraceHandler[] handlers) {
		this.handlers = handlers;
	}

	/**
	 * 비교할 클래스 정보.
	 * @param canonicalName 비교할 클래스명
	 */
	public void setPackageName(String canonicalName) {
		this.packageName = canonicalName;
	}

	/**
	 * getPackageName 메소드.
	 */
	public String getPackageName() {
		return this.packageName;
	}

	/**
	 * setReqExpMatcher 메소드.
	 * @param pm 사용자에 의해 사용하고자하는 PathMatcher
	 */
	public void setReqExpMatcher(PathMatcher pm) {
		this.pm = pm;
	}

	/**
	 * PathMatcher 가 있는지 여부 반환.
	 */
	public boolean hasReqExpMatcher() {
		return this.enableMatcher();
	}

	/**
	 * PathMatcher 가 있는지 여부 반환.
	 */
	public boolean enableMatcher() {
		return (this.pm == null) ? false : true;
	}

	/**
	 * 상속받아 구현해야할 메스드 하지만 미리구현은 먼저 해둠. 실 구현체에서 override 하여 구현해야 함.
	 * @param clazz 클래스정보
	 * @param message 보여주고자하는 메세지
	 * @return boolean true|false
	 */
	public boolean trace(Class<?> clazz, String message) {
		LOGGER.debug(" DefaultExceptionHandleManager.run() ");
		// 매칭조건이 false 인 경우
		if (!enableMatcher()) {
			return false;
		}
		for (String pattern : patterns) {
			LOGGER.debug("pattern = {}, thisPackageName = {}", pattern, packageName);
			LOGGER.debug("pm.match(pattern, thisPackageName) = {}", pm.match(pattern, packageName));
			if (pm.match(pattern, packageName)) {
				for (TraceHandler eh : handlers) {
					eh.todo(clazz, message);
				}
				break;
			}
		}
		return true;
	}

}
