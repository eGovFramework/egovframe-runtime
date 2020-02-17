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
package egovframework.rte.itl.integration;

/**
 * 전자정부 연계 서비스 중 비동기 연계 서비스의 응답 메시지를 받기 위한 인터페이스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 중 비동기 연계 서비스의 응답 메시지를 받기 위한 Class이다. </p>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009.06.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.06.01  심상호           최초 생성
 * 
 * </pre>
 */
public interface EgovIntegrationServiceResponse {
	/**
	 * 비동기 연계의 응답 메시지를 읽어온다. timeout 값은 default 값을 사용한다.
	 * 
	 * @return 연계 응답 메시지
	 */
	public EgovIntegrationMessage receive();

	/**
	 * 비동기 연계의 응답 메시지를 읽어온다.
	 * 
	 * @param timeout
	 *            비동기 연계 수행을 기다릴 시간(millisecond)
	 * @return 연계 응답 메시지
	 */
	public EgovIntegrationMessage receive(long timeout);

}
