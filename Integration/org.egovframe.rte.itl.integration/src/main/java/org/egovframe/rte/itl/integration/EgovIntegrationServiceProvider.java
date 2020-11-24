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
package org.egovframe.rte.itl.integration;

/**
 * 전자정부 연계 서비스를 제공하기 위한 인터페이스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스를 제공하기 위한 Interface이다. </p>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	심상호				최초 생성
 * </pre>
 */
public interface EgovIntegrationServiceProvider {

	/**
	 * 연계 서비스를 제공한다.
	 * 
	 * @param requestMessage
	 *            연계 요청 메시지
	 * @param responseMessage
	 *            연계 응답 메시지
	 */
	public void service(EgovIntegrationMessage requestMessage, EgovIntegrationMessage responseMessage);

}
