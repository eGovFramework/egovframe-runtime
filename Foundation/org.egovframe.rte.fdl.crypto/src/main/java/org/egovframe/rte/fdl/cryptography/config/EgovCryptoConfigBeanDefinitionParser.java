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
import java.nio.charset.StandardCharsets;


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
		if(algorithmKey == null || algorithmKey.trim().isEmpty()){
			LOGGER.error("Egovframe EnvCrypto algorithmKey is not value!!! ");
		} else {
			if ("egovframe".equals(algorithmKey)) {
				System.err.println("[EgovFramework Fatal ERROR] Since a fatal security threat may occur, " +
						"the Crypto service default algorithm Key=\"egovframe\" must be changed to another keyword. " +
						"For more details see https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.1:fdl:crypto");
			}
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
		if(algorithmKeyHash == null || algorithmKeyHash.trim().isEmpty()){
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
		if(initial.equalsIgnoreCase("true")){
			sCryptoInit = "init-method=\"init\"";
		}

		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<beans xmlns=\"http://www.springframework.org/schema/beans\"\n");
		sb.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		sb.append("    xmlns:p=\"http://www.springframework.org/schema/p\"\n");
		sb.append("    xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.3.xsd\">\n");
		sb.append("    <bean id=\"egovEnvPasswordEncoderService\" class=\"org.egovframe.rte.fdl.cryptography.EgovPasswordEncoder\">\n");
		sb.append("        <property name=\"algorithm\" value=\"").append(algorithm).append("\"/>\n");
		sb.append("        <property name=\"hashedPassword\" value=\"").append(algorithmKeyHash).append("\"/>\n");
		sb.append("    </bean>\n");
		sb.append("    <bean id=\"egovEnvARIACryptoService\" class=\"org.egovframe.rte.fdl.cryptography.impl.EgovARIACryptoServiceImpl\">\n");
		sb.append("        <property name=\"passwordEncoder\" ref=\"egovEnvPasswordEncoderService\"/>\n");
		sb.append("        <property name=\"blockSize\" value=\"").append(cryptoBlockSize).append("\"/>\n");
		sb.append("    </bean>\n");
		sb.append("    <bean id=\"egovEnvCryptoConfigurerService\" class=\"org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl\">\n");
		sb.append("        <property name=\"extFileName\">\n");
		sb.append("            <set>\n");
		sb.append("                <map>\n");
		sb.append("                    <entry key=\"encoding\" value=\"UTF-8\"/>\n");
		sb.append("                    <entry key=\"filename\" value=\"").append(cryptoPropertyLocation).append("\"/>\n");
		sb.append("                </map>\n");
		sb.append("            </set>\n");
		sb.append("        </property>\n");
		sb.append("    </bean>\n");
		sb.append("    <bean id=\"egovEnvCryptoService\" class=\"org.egovframe.rte.fdl.cryptography.impl.EgovEnvCryptoServiceImpl\" ").append(sCryptoInit).append(">\n");
		sb.append("        <property name=\"passwordEncoder\" ref=\"egovEnvPasswordEncoderService\"/>\n");
		sb.append("        <property name=\"cryptoService\" ref=\"egovEnvARIACryptoService\"/>\n");
		sb.append("        <property name=\"cryptoConfigurer\" ref=\"egovEnvCryptoConfigurerService\"/>\n");
		sb.append("        <property name=\"crypto\" value=\"").append(crypto).append("\"/>\n");
		sb.append("        <property name=\"cryptoAlgorithm\" value=\"").append(algorithm).append("\"/>\n");
		sb.append("        <property name=\"cyptoAlgorithmKey\" value=\"").append(algorithmKey).append("\"/>\n");
		sb.append("        <property name=\"cyptoAlgorithmKeyHash\" value=\"").append(algorithmKeyHash).append("\"/>\n");
		sb.append("        <property name=\"cryptoBlockSize\" value=\"").append(cryptoBlockSize).append("\"/>\n");
		sb.append("    </bean>\n");
		sb.append("</beans>\n");

		try {
			parserContext.getReaderContext().getReader().setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
			LOGGER.debug("EgovCryptoConfigBeanDefinitionParser httpd load start...");
			parserContext.getReaderContext().getReader().loadBeanDefinitions(new InputStreamResource(new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8))));
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
