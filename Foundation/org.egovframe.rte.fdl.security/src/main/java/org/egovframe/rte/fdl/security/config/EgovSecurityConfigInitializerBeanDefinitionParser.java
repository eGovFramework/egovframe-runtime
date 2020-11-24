/*
 * Copyright 2014 MOSPA(Ministry of Security and Public Administration).
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
package org.egovframe.rte.fdl.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.w3c.dom.Element;

/**
 * egov-security schema namespace 'initializer' element 처리를 담당하는 bean definition parser 클래스
 * 
 *<p>Desc.: 설정 간소화 처리에 사용되는 bean definition parser</p>
 *
 * @author Vincent Han
 * @since 2014.03.12
 * @version 3.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2014.03.12	한성곤				Spring Security 설정 간소화 기능 추가
 * 2017.07.03	장동한				Spring Security 4.x 업그레이드(보안설정기능) 추가
 * </pre>
 */
public class EgovSecurityConfigInitializerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovSecurityConfigInitializerBeanDefinitionParser.class);

	@Override
	protected Class<?> getBeanClass(Element element) {
		return SecurityConfigInitializer.class;
	}
	
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		
		LOGGER.debug("Load '/META-INF/spring/security/security-config.xml'");
		
		parserContext.getReaderContext().getReader().loadBeanDefinitions("classpath*:/META-INF/spring/security/security-config.xml");
		EgovSecurityHttp egovSecurityHttp = EgovSecurityHttp.getInstance();

		try {
			parserContext.getReaderContext().getReader().setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
			LOGGER.debug("EgovSecurityConfigInitializerBeanDefinitionParser httpd load start...");
		    /**
		     * EgovSecurityHttp.getHttp
		     * @param Sniff			 	: 응답에 대한 브라우저의 MIME 가로채기를 방지 옵션(활성:true, 비활성:false)
		     * @param XFrameOptions 	: 프레임셋 동작여부 옵션(DENY:거부, SAMEORIGIN:허용) 
		     * @param XssProtection 	: 브라우저가 XSS 공격에 사용될 수 있는 스크립트를 실행하지 않음 옵션(활성:true, 비활성:false)
		     * @param Csrf 				: HTTP 요청과 악의적 인 웹 사이트의 요청을 거부 옵션(활성:true, 비활성:false)
			 * @param CacheControl		: 브라우저 캐시를 수동으로 제어하기 위한 설정(캐시비활성:true, 캐시활성:false)
		     * @return InputStreamResource
		     */
			parserContext.getReaderContext().getReader().loadBeanDefinitions(egovSecurityHttp.getHttp(
					EgovSecurityConfigShare.sniff,
					EgovSecurityConfigShare.xFrameOptions,
					EgovSecurityConfigShare.xssProtection,
					EgovSecurityConfigShare.cacheControl,
					EgovSecurityConfigShare.csrf ));

			parserContext.getReaderContext().getReader().setValidationMode(XmlBeanDefinitionReader.VALIDATION_AUTO);
		} catch(IllegalArgumentException e) {
		    LOGGER.error("[["+e.getClass()+"/IllegalArgumentException] Try/Catch... Runing : "+ e.getMessage());
		    throw new RuntimeException("[["+e.getClass()+"/IllegalArgumentException] Try/Catch... Runing : "+ e.getMessage());
		} catch(Exception e){
		    LOGGER.error("["+e.getClass()+"] Exception Try/Catch... Runing : " + e.getMessage());
		    throw new RuntimeException("["+e.getClass()+"] Exception Try/Catch... Runing : " + e.getMessage());
		}

		LOGGER.debug("EgovSecurityConfigInitializerBeanDefinitionParser httpd load complete...");

		String supportPointcut = element.getAttribute("supportPointcut");
		if (supportPointcut.equalsIgnoreCase("true")) {
			LOGGER.debug("Load '/META-INF/spring/security/pointcut-config.xml'");
			parserContext.getReaderContext().getReader().loadBeanDefinitions("classpath*:/META-INF/spring/security/pointcut-config.xml");
		}
		
		String supportMethod = element.getAttribute("supportMethod");
		if (supportMethod.equalsIgnoreCase("true")) {
			LOGGER.debug("Load '/META-INF/spring/security/method-config.xml'");
			parserContext.getReaderContext().getReader().loadBeanDefinitions("classpath*:/META-INF/spring/security/method-config.xml");
		}
	}
	
}
