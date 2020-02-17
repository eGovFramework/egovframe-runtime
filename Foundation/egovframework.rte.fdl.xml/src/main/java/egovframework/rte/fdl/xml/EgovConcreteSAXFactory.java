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
package egovframework.rte.fdl.xml;

import egovframework.rte.fdl.xml.exception.UnsupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SAXValidator 생성 Factory Class
 * 
 * @deprecated use EgovSAXFactoryService class
 * @author 개발프레임웍크 실행환경 개발팀 김종호
 * @since 2009.03.18
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자              수정내용
 *  ---------   ---------   -------------------------------
 * 2009.03.18    김종호        최초생성
 *
 * </pre>
 */
// CHECKSTYLE:OFF
@Deprecated
public class EgovConcreteSAXFactory extends abstractXMLFactoryService {
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovConcreteSAXFactory.class);
	/** SAXValidator **/
	private EgovSAXValidatorService saxValidator = null;

	/**
	 * DOMValidatorService 생성자
	 * @return EgovDOMValidatorService - DOMValidator
	 * @exception UnsupportedException
	 */
	@Override
	public EgovDOMValidatorService CreateDOMValidator() {
		try {
			throw new UnsupportedException("지원되지 않는 방식입니다.");
		} catch (UnsupportedException e) {
			LOGGER.debug(e.getMessage());
		}
		return null;
	}

	/**
	 * EgovSAXValidatorService 생성자
	 * @return EgovSAXValidatorService - SAXValidator
	 */
	@Override
	public EgovSAXValidatorService CreateSAXValidator() {
		saxValidator = new EgovSAXValidatorService();
		return saxValidator;
	}

}
