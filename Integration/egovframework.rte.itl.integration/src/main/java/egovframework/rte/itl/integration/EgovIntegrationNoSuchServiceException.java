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
 * 전자정부 연계 서비스 Exception 구현 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 Exception Class이다. </p>
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
public class EgovIntegrationNoSuchServiceException extends
		EgovIntegrationException {

	/**
	 * serialVersion UID
	 */
	private static final long serialVersionUID = 901444084642853859L;

	public EgovIntegrationNoSuchServiceException() {
		super();
	}

	public EgovIntegrationNoSuchServiceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public EgovIntegrationNoSuchServiceException(String arg0) {
		super(arg0);
	}

	public EgovIntegrationNoSuchServiceException(Throwable arg0) {
		super(arg0);
	}

}
