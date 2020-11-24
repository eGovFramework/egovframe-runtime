/*
 * Copyright 2009-2014 MOSPA(Ministry of Security and Public Administration).

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
package org.egovframe.rte.fdl.xml;

/**
 * Factory Class
 * 
 * @deprecated use EgovAbstractXMLFactoryService (명명 규칙 수정)
 * @author 개발프레임웍크 실행환경 개발팀 김종호
 * @since 2009.03.10
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.03.10	김종호				최초생성
 * </pre>
 */
@Deprecated
public abstract class abstractXMLFactoryService {

	/**
	 * DOM 파서를 사용할 수 있도록 Service 생성
	 * @return EgovDOMValidatorService
	 */
	public abstract EgovDOMValidatorService CreateDOMValidator();

	/**
	* SAX 파서를 사용할 수 있도록 Service 생성
	* @return EgovSAXValidatorService
	*/
	public abstract EgovSAXValidatorService CreateSAXValidator();

}
