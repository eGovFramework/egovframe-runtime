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
package egovframework.rte.itl.integration.type;

import egovframework.rte.itl.integration.EgovIntegrationException;

/**
 * 전자정부 연계 서비스 CircularInheritanceException 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스 CircularInheritanceException
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
public class CircularInheritanceException extends EgovIntegrationException {

	/**
	 * serialVersion UID
	 */
	private static final long serialVersionUID = -3310827268500546032L;

	public CircularInheritanceException() {
		super();
	}

	public CircularInheritanceException(String message, Throwable cause) {
		super(message, cause);
	}

	public CircularInheritanceException(String message) {
		super(message);
	}

	public CircularInheritanceException(Throwable cause) {
		super(cause);
	}

}
