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
package org.egovframe.rte.fdl.cmmn.exception.handler;

/**
 * Exception 발생시 실행되는 Handler 인터페이스이다.
 * 구현체는 occur 메소드만 구현해주고 설정해주면 실행된다.
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
 *
 * </pre>
 */
public interface ExceptionHandler {

	/**
	 * occur 메소드.
	 * 
	 * @param exception 실제로 발생한 Exception 
	 * @param packageName Exception 발생한 클래스 패키지정보
	 */
	public void occur(Exception exception, String packageName);

}
