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
 * 전자정부 연계 서비스 호출을 위한 Service 인터페이스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 호출을 위한 Service Interface이다. </p>
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
public interface EgovIntegrationService {

	/**
	 * 연계 id를 읽어온다.
	 * 
	 * @return 연계 id
	 */
	public String getId();

	/**
	 * 연계 요청 메시지를 생성한다.
	 * 
	 * @return 연계 요청 메시지
	 */
	public EgovIntegrationMessage createRequestMessage();

	/**
	 * 연계 서비스의 default timeout(millisecond)를 읽어온다.
	 * 
	 * @return default timeout
	 */
	public long getDefaultTimeout();

	/**
	 * 동기 연계 서비스를 요청한다. timeout 값은 default 값을 사용한다.
	 * 
	 * @param requestMessage
	 *            연계 요청 메시지
	 * @return 연계 응답 메시지
	 * @throws IllegalArgumentException
	 *             Argument <code>requestMessage</code> 값이 <code>null</code>인 경우
	 */
	public EgovIntegrationMessage sendSync(EgovIntegrationMessage requestMessage);

	/**
	 * 동기 연계 서비스를 요청한다.
	 * 
	 * @param requestMessage
	 *            연계 요청 메시지
	 * @param timeout
	 *            동기 연계 수행을 기다릴 시간(millisecond)
	 * @return 연계 응답 메시지
	 * @throws IllegalArgumentException
	 *             Argument <code>requestMessage</code> 값이 <code>null</code>인 경우
	 */
	public EgovIntegrationMessage sendSync(
			EgovIntegrationMessage requestMessage, long timeout);

	/**
	 * 비동기 연계 서비스를 요청한다.
	 * 
	 * @param requestMessage
	 *            연계 요청 메시지
	 * @return 비동기 연계 서비스 Response 객체
	 * @throws IllegalArgumentException
	 *             Argument <code>requestMessage</code> 값이 <code>null</code>인 경우
	 */
	public EgovIntegrationServiceResponse sendAsync(
			EgovIntegrationMessage requestMessage);

	/**
	 * 비동기 연계 서비스를 요청한다.
	 * 
	 * @param requestMessage
	 *            연계 요청 메시지
	 * @param callback
	 *            비동기 연계 서비스 Callback 객체
	 * @throws IllegalArgumentException
	 *             Argument <code>requestMessage</code>, <code>callback</code>
	 *             값이 <code>null</code>인 경우
	 */
	public void sendAsync(EgovIntegrationMessage requestMessage,
			EgovIntegrationServiceCallback callback);
}
