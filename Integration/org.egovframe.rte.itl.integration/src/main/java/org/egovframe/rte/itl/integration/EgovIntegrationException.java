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
 * 전자정부 연계 서비스 Exception 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 Exception Class이다. <br>
 * <code>EgovIntegrationContext</code>의 <code>getService</code> 메소드 호출 시 해당하는
 * service가 존재하지 않을 경우 발생한다.
 * </p>
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
public class EgovIntegrationException extends RuntimeException {

	/**
	 * serialVersion UID
	 */
	private static final long serialVersionUID = 5787571086773572581L;

	public EgovIntegrationException() {
		super();
	}

	public EgovIntegrationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public EgovIntegrationException(String arg0) {
		super(arg0);
	}

	public EgovIntegrationException(Throwable arg0) {
		super(arg0);
	}

}
