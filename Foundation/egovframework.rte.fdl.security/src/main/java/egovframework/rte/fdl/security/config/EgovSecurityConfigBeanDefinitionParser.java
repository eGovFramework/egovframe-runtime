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
package egovframework.rte.fdl.security.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * egov-security schema namespace 'config' element 처리를 담당하는 bean definition parser 클래스
 * 
 *<p>Desc.: 설정 간소화 처리에 사용되는 bean definition parser</p>
 *
 * @author Vincent Han
 * @since 2014.03.12
 * @version 3.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일		수정자					수정내용
 *  ---------------------------------------------------------------------------------
 *   2014.03.12	한성곤					Spring Security 설정 간소화 기능 추가
 *   2020.05.27	Egovframework Center	CSRF Access Denied 처리 URL 추가
 * 
 * </pre>
 */
public class EgovSecurityConfigBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected Class<?> getBeanClass(Element element) {
		return SecurityConfig.class;
	}

	@Override
	protected void doParse(Element element, BeanDefinitionBuilder bean) {
		String loginUrl = element.getAttribute("loginUrl");
		if (StringUtils.hasText(loginUrl)) {
			bean.addPropertyValue("loginUrl", loginUrl);
		}

		String logoutSuccessUrl = element.getAttribute("logoutSuccessUrl");
		if (StringUtils.hasText(logoutSuccessUrl)) {
			bean.addPropertyValue("logoutSuccessUrl", logoutSuccessUrl);
		}

		String loginFailureUrl = element.getAttribute("loginFailureUrl");
		if (StringUtils.hasText(loginFailureUrl)) {
			bean.addPropertyValue("loginFailureUrl", loginFailureUrl);
		}

		String accessDeniedUrl = element.getAttribute("accessDeniedUrl");
		if (StringUtils.hasText(accessDeniedUrl)) {
			bean.addPropertyValue("accessDeniedUrl", accessDeniedUrl);
		}

		String dataSource = element.getAttribute("dataSource");
		if (StringUtils.hasText(dataSource)) {
			bean.addPropertyReference("dataSource", dataSource);
		}

		String jdbcUsersByUsernameQuery = element.getAttribute("jdbcUsersByUsernameQuery");
		if (StringUtils.hasText(jdbcUsersByUsernameQuery)) {
			bean.addPropertyValue("jdbcUsersByUsernameQuery", jdbcUsersByUsernameQuery);
		}

		String jdbcAuthoritiesByUsernameQuery = element.getAttribute("jdbcAuthoritiesByUsernameQuery");
		if (StringUtils.hasText(jdbcAuthoritiesByUsernameQuery)) {
			bean.addPropertyValue("jdbcAuthoritiesByUsernameQuery", jdbcAuthoritiesByUsernameQuery);
		}

		String jdbcMapClass = element.getAttribute("jdbcMapClass");
		if (StringUtils.hasText(jdbcMapClass)) {
			bean.addPropertyValue("jdbcMapClass", jdbcMapClass);
		}

		String requestMatcherType = element.getAttribute("requestMatcherType");
		if (StringUtils.hasText(requestMatcherType)) {
			bean.addPropertyValue("requestMatcherType", requestMatcherType);
		}

		String hash = element.getAttribute("hash");
		if (StringUtils.hasText(hash)) {
			bean.addPropertyValue("hash", hash);
		}

		String hashBase64 = element.getAttribute("hashBase64");
		if (StringUtils.hasText(hashBase64)) {
			bean.addPropertyValue("hashBase64", hashBase64);
		}

		String concurrentMaxSessons = element.getAttribute("concurrentMaxSessons");
		if (StringUtils.hasText(concurrentMaxSessons)) {
			bean.addPropertyValue("concurrentMaxSessons", concurrentMaxSessons);
		}

		String concurrentExpiredUrl = element.getAttribute("concurrentExpiredUrl");
		if (StringUtils.hasText(concurrentExpiredUrl)) {
			bean.addPropertyValue("concurrentExpiredUrl", concurrentExpiredUrl);
		}

		String errorIfMaximumExceeded = element.getAttribute("errorIfMaximumExceeded");
		if (StringUtils.hasText(errorIfMaximumExceeded)) {
			bean.addPropertyValue("errorIfMaximumExceeded", errorIfMaximumExceeded);
		}

		String defaultTargetUrl = element.getAttribute("defaultTargetUrl");
		if (StringUtils.hasText(defaultTargetUrl)) {
			bean.addPropertyValue("defaultTargetUrl", defaultTargetUrl);
		}

		String alwaysUseDefaultTargetUrl = element.getAttribute("alwaysUseDefaultTargetUrl");
		if (StringUtils.hasText(alwaysUseDefaultTargetUrl)) {
			bean.addPropertyValue("alwaysUseDefaultTargetUrl", alwaysUseDefaultTargetUrl);
			EgovSecurityConfigShare.alwaysUseDefaultTargetUrl = Boolean.valueOf(alwaysUseDefaultTargetUrl);
		}

		String sniff = element.getAttribute("sniff");
		if (StringUtils.hasText(sniff)) {
			bean.addPropertyValue("sniff", sniff);
			EgovSecurityConfigShare.sniff = Boolean.valueOf(sniff);
		}

		String xFrameOptions = element.getAttribute("xFrameOptions");
		if (StringUtils.hasText(xFrameOptions)) {
			bean.addPropertyValue("xFrameOptions", xFrameOptions);
			EgovSecurityConfigShare.xFrameOptions = xFrameOptions;
		}

		String xssProtection = element.getAttribute("xssProtection");
		if (StringUtils.hasText(xssProtection)) {
			bean.addPropertyValue("xssProtection", xssProtection);
			EgovSecurityConfigShare.xssProtection = Boolean.valueOf(xssProtection);  
		}

		String cacheControl = element.getAttribute("cacheControl");
		if (StringUtils.hasText(cacheControl)) {
			bean.addPropertyValue("cacheControl", cacheControl);
			EgovSecurityConfigShare.cacheControl = Boolean.valueOf(cacheControl);
		}

		String csrf = element.getAttribute("csrf");
		if (StringUtils.hasText(csrf)) {
			bean.addPropertyValue("csrf", csrf);
			EgovSecurityConfigShare.csrf = Boolean.valueOf(csrf);  
		}

		String csrfAccessDeniedUrl = element.getAttribute("csrfAccessDeniedUrl");
		if (StringUtils.hasText(csrfAccessDeniedUrl)) {
			bean.addPropertyValue("csrfAccessDeniedUrl", csrfAccessDeniedUrl);
		}
	}

}
