/*
 * Copyright 2008-2019 MOIS(Ministry of the Interior and Safety).
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
package egovframework.rte.fdl.cryptography.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * EgovCryptoConfig 클래스  
 * <Notice>
 * 	    Crypto Namespace Handler 
 * <Disclaimer>
 *		N/A
 *
 * @author 장동한
 * @since 2018.08.09
 * @version 1.0
 * @see
 *
 * <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2018.08.09  장동한           최초 생성
 * </pre>
 */
public class EgovCryptoNameHandler extends NamespaceHandlerSupport {

	/**
	 * 초기화-Crypto 설정간소화 Definition Parser 등록
	 *
	 * @return void
	 */
    public void init() {
    	   registerBeanDefinitionParser("config", new EgovCryptoConfigBeanDefinitionParser());	
    }
}
