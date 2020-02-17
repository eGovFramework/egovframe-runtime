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
 * 전자정부 연계 서비스의 비동기 방식 연계의 응답을 받기위한 callback 인터페이스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 비동기 방식 연계의 응답을 받기위한 callback Interface이다. </p>
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
public interface EgovIntegrationServiceCallback {
	
	public interface CallbackId {
	}

	/**
	 * 연계 서비스 시작 시, EgovIntegrationService에서 ID를 얻기 위해 호출한다.
	 * 
	 * @param service
	 *            연계 서비스
	 * @param requestMessage
	 *            요청 메시지
	 * @return Callback ID
	 * @throws IllegalArgumentException
	 *             Argument <code>service</code>,
	 *             <code>requestMessage</code> 값이 <code>null</code> 인 경우
	 */
	public CallbackId createId(EgovIntegrationService service,
			EgovIntegrationMessage requestMessage);

	/**
	 * 응답 메시지 도착시 호출된다.
	 * 
	 * @param callbackId
	 *            Callback ID
	 * @param responseMessage
	 *            응답 메시지
	 */
	public void onReceive(CallbackId callbackId,
			EgovIntegrationMessage responseMessage);

}
