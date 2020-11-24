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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * egov-security schema namespace 'secured-object-config' element 처리를 담당하는 bean definition parser 클래스
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
 * </pre>
 */
public class EgovSecuritySecuredObjectConfigBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected Class<?> getBeanClass(Element element) {
		return SecuredObjectConfig.class;
	}

	@Override
	protected void doParse(Element element, BeanDefinitionBuilder bean) {

		String sqlRolesAndUrl = element.getAttribute("sqlRolesAndUrl");
		if (StringUtils.hasText(sqlRolesAndUrl)) {
			bean.addPropertyValue("sqlRolesAndUrl", sqlRolesAndUrl);
		}
		
		String sqlRolesAndMethod = element.getAttribute("sqlRolesAndMethod");
		if (StringUtils.hasText(sqlRolesAndMethod)) {
			bean.addPropertyValue("sqlRolesAndMethod", sqlRolesAndMethod);
		}
		
		String sqlRolesAndPointcut = element.getAttribute("sqlRolesAndPointcut");
		if (StringUtils.hasText(sqlRolesAndPointcut)) {
			bean.addPropertyValue("sqlRolesAndPointcut", sqlRolesAndPointcut);
		}
		
		String sqlRegexMatchedRequestMapping = element.getAttribute("sqlRegexMatchedRequestMapping");
		if (StringUtils.hasText(sqlRegexMatchedRequestMapping)) {
			bean.addPropertyValue("sqlRegexMatchedRequestMapping", sqlRegexMatchedRequestMapping);
		}
		
		String sqlHierarchicalRoles = element.getAttribute("sqlHierarchicalRoles");
		if (StringUtils.hasText(sqlHierarchicalRoles)) {
			bean.addPropertyValue("sqlHierarchicalRoles", sqlHierarchicalRoles);
		}
		
		String roleHierarchyString = element.getAttribute("roleHierarchyString");
		if (StringUtils.hasText(roleHierarchyString)) {
			bean.addPropertyValue("roleHierarchyString", roleHierarchyString);
		}
	}

}
