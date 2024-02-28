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
package org.egovframe.rte.fdl.access.config;

import org.apache.commons.lang3.ObjectUtils;
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
 * egov-access schema namespace 'config' element 처리를 담당하는 bean definition parser 클래스
 *
 * <p>Desc.: 설정 간소화 처리에 사용되는 bean definition parser</p>
 *
 * @author ESFC
 * @since 2019.10.01
 * @version 3.9
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				    수정내용
 * ----------------------------------------------
 * 2019.10.01	ESFC                최초 생성
 * 2019.12.30	신용호                mappingPath 추가
 * </pre>
 */
public class EgovAccessConfigBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovAccessConfigBeanDefinitionParser.class);

    @Override
    protected Class<?> getBeanClass(Element element) {
        return EgovAccessConfig.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        LOGGER.debug("EgovAccessConfigBeanDefinitionParser doParse Execute !!!");

        String globalAuthen = element.getAttribute("globalAuthen");
        if (StringUtils.hasText(globalAuthen)) {
            builder.addPropertyValue("globalAuthen", globalAuthen);
        }

        String dataSource = element.getAttribute("dataSource");
        if (StringUtils.hasText(dataSource)) {
            builder.addPropertyReference("dataSource", dataSource);
        }

        String loginUrl = element.getAttribute("loginUrl");
        if (StringUtils.hasText(loginUrl)) {
            builder.addPropertyValue("loginUrl", loginUrl);
            EgovAccessConfigShare.DEF_LOGIN_URL = loginUrl;
        }

        String accessDeniedUrl = element.getAttribute("accessDeniedUrl");
        if (StringUtils.hasText(accessDeniedUrl)) {
            builder.addPropertyValue("accessDeniedUrl", accessDeniedUrl);
            EgovAccessConfigShare.DEF_ACCESS_DENIED_URL = accessDeniedUrl;
        }

        String sqlAuthorityUser = element.getAttribute("sqlAuthorityUser");
        if (StringUtils.hasText(sqlAuthorityUser)) {
            builder.addPropertyValue("sqlAuthorityUser", sqlAuthorityUser);
        }

        String sqlRoleAndUrl = element.getAttribute("sqlRoleAndUrl");
        if (StringUtils.hasText(sqlRoleAndUrl)) {
            builder.addPropertyValue("sqlRoleAndUrl", sqlRoleAndUrl);
        }

        String requestMatcherType = element.getAttribute("requestMatcherType");
        if (StringUtils.hasText(requestMatcherType)) {
            builder.addPropertyValue("requestMatcherType", requestMatcherType);
            EgovAccessConfigShare.DEF_REQUEST_MATCH_TYPE = requestMatcherType;
        }

        String excludeList = element.getAttribute("excludeList");
        if (StringUtils.hasText(excludeList)) {
            builder.addPropertyValue("excludeList", excludeList);
        }

        String mappingPath = element.getAttribute("mappingPath");

        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<beans xmlns=\"http://www.springframework.org/schema/beans\"\n");
        sb.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        sb.append("    xmlns:mvc=\"http://www.springframework.org/schema/mvc\"\n");
        sb.append("    xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.3.xsd\n");
        sb.append("        http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc-4.3.xsd\">\n");
        sb.append("    <beans>\n");
        sb.append("        <mvc:interceptors>\n");
        sb.append("            <mvc:interceptor>\n");
        sb.append("                <mvc:mapping path=\"").append(mappingPath).append("\"/>\n");

        String[] list = excludeList.split(",");
        for (String path : list) {
            sb.append("<mvc:exclude-mapping path=\"").append(path.trim()).append("\" />\n");
        }

        sb.append("                <bean class=\"org.egovframe.rte.fdl.access.interceptor.EgovAccessInterceptor\"/>\n");
        sb.append("            </mvc:interceptor>\n");
        sb.append("        </mvc:interceptors>\n");
        sb.append("    </beans>\n");
        sb.append("</beans>");

        String profile = System.getProperty("spring.profiles.active");

        LOGGER.debug("EgovAccessConfigBeanDefinitionParser profile >>> {}", profile);
        LOGGER.debug("EgovAccessConfigBeanDefinitionParser globalAuthen >>> {}", globalAuthen);

        if (((ObjectUtils.isEmpty(profile) && "session".equals(globalAuthen))) ||
                (ObjectUtils.isNotEmpty(profile) && profile.contains(globalAuthen)) ) {
            try {
                parserContext.getReaderContext().getReader().setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
                LOGGER.debug("EgovAccessConfigBeanDefinitionParser httpd load start...");
                parserContext.getReaderContext().getReader().loadBeanDefinitions(new InputStreamResource(new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8))));
                LOGGER.debug("EgovAccessConfigBeanDefinitionParser httpd load end...");
                parserContext.getReaderContext().getReader().setValidationMode(XmlBeanDefinitionReader.VALIDATION_AUTO);
            } catch (IllegalArgumentException iae) {
                LOGGER.error("[["+iae.getClass()+"/IllegalArgumentException] Try/Catch... Runing : "+ iae.getMessage());
                throw new RuntimeException("[["+iae.getClass()+"/IllegalArgumentException] Try/Catch... Runing : "+ iae.getMessage());
            } catch (Exception e) {
                LOGGER.error("["+e.getClass()+"] Exception Try/Catch... Runing : " + e.getMessage());
                throw new RuntimeException("["+e.getClass()+"] Exception Try/Catch... Runing : " + e.getMessage());
            }

            parserContext.getReaderContext().getReader().loadBeanDefinitions("classpath:/META-INF/spring/access/access-config.xml");
        }
    }

}
