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
 * DOMValidator 생성 Factory Class
 * 
 * @deprecated use EgovDOMFactoryServiceImpl class
 * @author 개발프레임웍크 실행환경 개발팀 김종호
 * @since 2009.03.18
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자              수정내용
 *  ---------   ---------   -------------------------------
 * 2009.03.18    김종호       최초생성
 * 2017.02.28 	 장동한		시큐어코딩(ES)-오류 메시지를 통한 정보노출[CWE-209]
 *
 * </pre>
 */
// CHECKSTYLE:OFF
@Deprecated
public class EgovConcreteDOMFactory extends abstractXMLFactoryService {
	/** DOMValidator  **/
	private EgovDOMValidatorService domvalidator = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovConcreteDOMFactory.class);
    
	/**
	 * DOMValidatorService 생성자
	 * @return EgovDOMValidatorService - DOMValidator
	 */
	@Override
	public EgovDOMValidatorService CreateDOMValidator() {
		domvalidator = new EgovDOMValidatorService();
		return domvalidator;
	}

	/**
	 * EgovSAXValidatorService 생성자
	 * @return EgovSAXValidatorService - SAXValidator
	 * @exception UnsupportedException
	 */
	@Override
	public EgovSAXValidatorService CreateSAXValidator() {
		try {
			throw new UnsupportedException("지원되지 않는 방식입니다.");
		} catch (UnsupportedException e) {
			//2017.02.28 장동한 시큐어코딩(ES)-오류 메시지를 통한 정보노출[CWE-209]
			//e.printStackTrace();
			LOGGER.error("["+e.getClass()+"] Try/Catch...CreateSAXValidator() Runing : " + e.getMessage());
		}
		return null;
	}

}
