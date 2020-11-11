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
package egovframework.rte.fdl.cmmn.trace.manager;

import egovframework.rte.fdl.cmmn.trace.handler.TraceHandler;

import org.springframework.util.PathMatcher;

/**
 * TraceHandlerService 인터페이스는 Handler Service 메소드를 정의하고 있다.
 * 
 * @author Judd Cho (horanghi@gmail.com)
 * @since 2009.06.01
 * @version 1.0
 * @see
 *
 * <pre>
 * == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.05.30  Judd Cho        최초 생성
 *   2015.01.31 Vincent Han		코드 품질 개선 
 *
 * </pre>
 */
public interface TraceHandlerService {

	/**
	 * 패키지, 클래스 이름으로 패턴등록(Ant형식의 매칭).
	 * 
	 * @param patterns 패턴리스트
	 */

	public void setPatterns(String[] patterns);

	/**
	 * ExceptionHandler 리스트 등록.
	 * 
	 * @param handlers handler리스트
	 */
	public void setHandlers(TraceHandler[] handlers);

	/**
	 * 비교할 클래스 정보.
	 * 
	 * @param canonicalName 비교할 클래스명
	 */
	public void setPackageName(String canonicalName);

	/**
	 * setReqExpMatcher 메소드.
	 * 
	 * @param pm 사용자에 의해 사용하고자하는 PathMatcher 
	 */
	public void setReqExpMatcher(PathMatcher pm);

	/**
	 * PathMatcher 가 있는지 여부 반환.
	 * 
	 * @return boolean true|false
	 */
	public boolean hasReqExpMatcher();

	/**
	 * trace 메소드.
	 * 
	 * @param clazz 클래스정보
	 * @param message 보여주고자하는 메세지
	 * @return boolean true|false
	 */
	public boolean trace(Class<?> clazz, String message);

}
