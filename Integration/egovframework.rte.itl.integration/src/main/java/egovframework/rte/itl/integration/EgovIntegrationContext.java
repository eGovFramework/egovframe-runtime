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

import egovframework.rte.itl.integration.monitor.EgovIntegrationServiceMonitor;

/**
 * 전자정부 연계 서비스 Context 인터페이스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 Context Interface이다.
 * </p>
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
public interface EgovIntegrationContext {
	/**
	 * 연계 Service 객체를 가져온다.
	 * 
	 * @param id
	 *            연계 Id
	 * @return 연계 Service
	 * @throws EgovIntegrationNoSuchServiceException
	 *             id 에 해당하는 Service가 없을 경우
	 */
	public EgovIntegrationService getService(final String id)
			throws EgovIntegrationNoSuchServiceException;

	/**
	 * 연계 수행 시의 default timeout 값(millisecond)를 읽어온다.
	 * 
	 * @return default timeout
	 */
	public long getDefaultTimeout();

	/**
	 * Monitor를 추가한다.
	 * 
	 * @param monitor
	 * @throws IllegalArgumentException
	 *             Argument <code>monitor</code> 값이 <code>null</code>인 경우
	 */
	public void attachMonitor(EgovIntegrationServiceMonitor monitor);

	/**
	 * Monitor를 제거한다.
	 * 
	 * @param monitor
	 */
	public void detachMonitor(EgovIntegrationServiceMonitor monitor);
}
