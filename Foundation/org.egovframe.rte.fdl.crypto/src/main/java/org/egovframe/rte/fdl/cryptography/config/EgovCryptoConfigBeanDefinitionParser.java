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
package org.egovframe.rte.fdl.cryptography.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;


/**
 * EgovCryptoConfig 클래스  
 * <Notice>
 * 	    Crypto Definition Parser
 * <Disclaimer>
 *		N/A
 *
 * @author 장동한
 * @since 2018.08.09
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2018.08.09	장동한				최초 생성
 * </pre>
 */
public class EgovCryptoConfigBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovCryptoConfigBeanDefinitionParser.class);

	/**
	 * DefinitionElement 설정 파일 setter
	 * 
	 * @param element DefinitionElement 
	 * @return Class
	 */
	@Override
	protected Class<?> getBeanClass(Element element) {
		return EgovCryptoConfig.class;
	}

	/**
	 * DefinitionElement 설정 파일 setter
	 * 
	 * @param element DefinitionElement 
	 * @param parserContext DefinitionParserContext
	 * @param bean BeanDefinitionBuilder 
	 * 
	 * @return void
	 */
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder bean) {

		LOGGER.debug("EgovCryptoConfigBeanDefinitionParser doParse Execute !!!");
		
		String initial = element.getAttribute("initial");
		if (StringUtils.hasText(initial)) {
			bean.addPropertyValue("initial", initial);
		}
		
		String crypto = element.getAttribute("crypto");
		if (StringUtils.hasText(crypto)) {
			bean.addPropertyValue("crypto", crypto);
		}

		String algorithm = element.getAttribute("algorithm");
		if (StringUtils.hasText(algorithm)) {
			bean.addPropertyValue("algorithm", algorithm);
		}
		
		String algorithmKey = element.getAttribute("algorithmKey");
		if(System.getProperty("egov.crypto.algorithmKey") != null){
			LOGGER.debug("egov.crypto.algorithmKey : system property setting!!");
			algorithmKey = System.getProperty("egov.crypto.algorithmKey");
			bean.addPropertyValue("algorithmKey", algorithmKey);
		} else {
			if (StringUtils.hasText(algorithmKey)) {
				LOGGER.debug("egov.crypto.algorithmKey : xml property setting!!");
				bean.addPropertyValue("algorithmKey", algorithmKey);
			}
		}
		if(algorithmKey == null || algorithmKey.trim().equals("")){
			LOGGER.error("Egovframe EnvCrypto algorithmKey is not value!!! ");
		}

		String algorithmKeyHash = element.getAttribute("algorithmKeyHash");
		if(System.getProperty("egov.crypto.algorithmKeyHash") != null){
			LOGGER.debug("egov.crypto.algorithmKeyHash : system property setting!!");
			algorithmKeyHash = System.getProperty("egov.crypto.algorithmKeyHash");
			bean.addPropertyValue("algorithmKeyHash", algorithmKeyHash);
		} else {
			if (StringUtils.hasText(algorithmKeyHash)) {
				LOGGER.debug("egov.crypto.algorithmKeyHash : xml property setting!!");
				bean.addPropertyValue("algorithmKeyHash", algorithmKeyHash);
			}
		}
		if(algorithmKeyHash == null || algorithmKeyHash.trim().equals("")){
			LOGGER.error("Egovframe EnvCrypto algorithmKeyHash is not value!!! ");
		}
		
		String cryptoBlockSize = element.getAttribute("cryptoBlockSize");
		if(StringUtils.hasText(cryptoBlockSize)) {
			bean.addPropertyValue("cryptoBlockSize", cryptoBlockSize);
		}
		
		String cryptoPropertyLocation = element.getAttribute("cryptoPropertyLocation");
		if(StringUtils.hasText(cryptoPropertyLocation)) {
			bean.addPropertyValue("cryptoPropertyLocation", cryptoPropertyLocation);
		}
	
		String sCryptoInit = "";
		if(initial.toLowerCase().equals("true")){
			sCryptoInit = "init-method=\"init\"";
		}
		
		String sCryptoBeanDefinition = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
		"<beans xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
		"    xmlns:p=\"http://www.springframework.org/schema/p\"\n" + 
		"    xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.3.xsd\">\n" +
		"\n" +
		"	<bean id=\"egovEnvPasswordEncoderService\" class=\"org.egovframe.rte.fdl.cryptography.EgovPasswordEncoder\">\n" +
		"		<property name=\"algorithm\" value=\""+ algorithm +"\"/>\n" + 
		"		<property name=\"hashedPassword\" value=\"" + algorithmKeyHash +"\"/>\n" + 
		"	</bean>\n" + 
		"\n" + 
		"	<bean id=\"egovEnvARIACryptoService\" class=\"org.egovframe.rte.fdl.cryptography.impl.EgovARIACryptoServiceImpl\">\n" +
		"		<property name=\"passwordEncoder\" ref=\"egovEnvPasswordEncoderService\"/>\n" + 
		"		<property name=\"blockSize\" value=\"" + cryptoBlockSize +"\"/>\n" + 
		"	</bean>\n" + 
		"\n" + 
		"<bean id=\"egovEnvCryptoConfigurerService\" class=\"org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl\">\n" + //destroy-method=\"destroy\"
		"<property name=\"extFileName\">\n" +
		"	<set>\n" +
		"		<map>\n" +
		"			<entry key=\"encoding\" value=\"UTF-8\"/>\n" +
		"			<entry key=\"filename\" value=\"" + cryptoPropertyLocation + "\"/>\n" +
		"		</map>\n" +
		"	</set>\n" +
		"</property>\n" +
		"</bean>\n" +
		"\n" + 
		"	<bean id=\"egovEnvCryptoService\" class=\"org.egovframe.rte.fdl.cryptography.impl.EgovEnvCryptoServiceImpl\" "+sCryptoInit+">\n" +
		"		<property name=\"passwordEncoder\" ref=\"egovEnvPasswordEncoderService\"/>\n" + 
		"		<property name=\"cryptoService\" ref=\"egovEnvARIACryptoService\"/>\n" + 
		"		<property name=\"cryptoConfigurer\" ref=\"egovEnvCryptoConfigurerService\"/>\n" + 
		"		<property name=\"crypto\" value=\""+ crypto +"\"/>\n" + 
		"		<property name=\"cryptoAlgorithm\" value=\""+ algorithm +"\"/>\n" + 
		"		<property name=\"cyptoAlgorithmKey\" value=\""+ algorithmKey +"\"/>\n" + 
		"		<property name=\"cyptoAlgorithmKeyHash\" value=\""+ algorithmKeyHash +"\"/>\n" + 
		"		<property name=\"cryptoBlockSize\" value=\""+ cryptoBlockSize +"\"/>\n" + 
		"	</bean>\n" + 
		"</beans>\n";

		try {
			parserContext.getReaderContext().getReader().setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
			LOGGER.debug("EgovCryptoConfigBeanDefinitionParser httpd load start...");
			parserContext.getReaderContext().getReader().loadBeanDefinitions(new InputStreamResource(new ByteArrayInputStream(sCryptoBeanDefinition.getBytes("UTF-8"))));
			LOGGER.debug("EgovCryptoConfigBeanDefinitionParser httpd load end...");
			parserContext.getReaderContext().getReader().setValidationMode(XmlBeanDefinitionReader.VALIDATION_AUTO);
		} catch(IllegalArgumentException e) {
		    LOGGER.error("[["+e.getClass()+"/IllegalArgumentException] Try/Catch... Runing : "+ e.getMessage());
		    throw new RuntimeException("[["+e.getClass()+"/IllegalArgumentException] Try/Catch... Runing : "+ e.getMessage());
		} catch(Exception e){
		    LOGGER.error("["+e.getClass()+"] Exception Try/Catch... Runing : " + e.getMessage());
		    throw new RuntimeException("["+e.getClass()+"] Exception Try/Catch... Runing : " + e.getMessage());
		}
	}

}
