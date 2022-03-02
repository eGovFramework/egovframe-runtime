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

import java.io.ByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * egov-access schema namespace 'config' element 처리를 담당하는 bean definition parser 클래스
 *
 * <p>Desc.: 설정 간소화 처리에 사용되는 bean definition parser</p>
 *
 * @author Egovframework Center
 * @since 2019.10.01
 * @version 3.9
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				    수정내용
 * ----------------------------------------------
 * 2019.10.01	Egovframework Center	최초 생성
 * 2019.12.30	신용호					mappingPath 추가
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

        LOGGER.debug("##### EgovAccessConfigBeanDefinitionParser doParse Execute #####");

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
        LOGGER.debug("##### EgovAccessConfigBeanDefinitionParser mappingPath >>> {}", mappingPath);

        String beanDefinitionBuilderString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:mvc=\"http://www.springframework.org/schema/mvc\"\n" +
                "    xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.3.xsd\n" +
                "        http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc-4.3.xsd\">\n";

        if ("dummy".equals(globalAuthen.toLowerCase()) || "security".equals(globalAuthen.toLowerCase())) {
            beanDefinitionBuilderString += "    <beans profile=\"session\">\n";
        } else {
            beanDefinitionBuilderString += "    <beans profile=\"" + globalAuthen + "\">\n";
        }

        beanDefinitionBuilderString +=
                "       <mvc:interceptors>\n" +
                "           <mvc:interceptor>\n" +
                "               <mvc:mapping path=\""+mappingPath+"\"/>\n";

        String[] list = excludeList.split("\\,");
        for (String path : list) {
            beanDefinitionBuilderString += "<mvc:exclude-mapping path=\"" + path.trim() + "\" />\n";
        }

        beanDefinitionBuilderString +=
                "               <bean class=\"org.egovframe.rte.fdl.access.interceptor.EgovAccessInterceptor\"/>\n" +
                "           </mvc:interceptor>\n" +
                "       </mvc:interceptors>\n" +
                "   </beans>\n" +
                "</beans>";

        try {
            parserContext.getReaderContext().getReader().setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
            LOGGER.debug("##### EgovAccessConfigBeanDefinitionParser interceptor httpd load Start #####");
            parserContext.getReaderContext().getReader().loadBeanDefinitions(
                    new InputStreamResource(
                            new ByteArrayInputStream(beanDefinitionBuilderString.getBytes("UTF-8"))
                    )
            );
            LOGGER.debug("##### EgovAccessConfigBeanDefinitionParser interceptor httpd xml >>> {} #####", beanDefinitionBuilderString);
            LOGGER.debug("##### EgovAccessConfigBeanDefinitionParser interceptor httpd load End #####");
            parserContext.getReaderContext().getReader().setValidationMode(XmlBeanDefinitionReader.VALIDATION_AUTO);
        } catch (IllegalArgumentException iae) {
            LOGGER.error("##### EgovAccessConfigBeanDefinitionParser IllegalArgumentException >>> " + iae.getMessage());
            throw new RuntimeException("##### EgovAccessConfigBeanDefinitionParser IllegalArgumentException >>> " + iae.getMessage());
        } catch (Exception e) {
            LOGGER.error("##### EgovAccessConfigBeanDefinitionParser Exception >>> " + e.getMessage());
            throw new RuntimeException("##### EgovAccessConfigBeanDefinitionParser Exception >>> " + e.getMessage());
        }

        LOGGER.debug("##### EgovAccessConfigBeanDefinitionParser access-config.xml load Start #####");
        parserContext.getReaderContext().getReader().loadBeanDefinitions("classpath:/META-INF/spring/access/access-config.xml");
        LOGGER.debug("##### EgovAccessConfigBeanDefinitionParser access-config.xml load End #####");

    }

}
