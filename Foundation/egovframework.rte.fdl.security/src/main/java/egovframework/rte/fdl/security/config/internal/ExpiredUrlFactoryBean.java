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
package egovframework.rte.fdl.security.config.internal;

import egovframework.rte.fdl.logging.util.EgovJdkLogger;
import egovframework.rte.fdl.security.config.SecurityConfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

public class ExpiredUrlFactoryBean implements FactoryBean<String>, ApplicationContextAware {
	private ApplicationContext context;
	
	public static final String DEFAULT_EXPIRED_URL = "/index.htm";
	
	public String getObject() throws Exception {
		try {
			SecurityConfig config = context.getBean(SecurityConfig.class);
			
			if (StringUtils.hasText(config.getConcurrentExpiredUrl())) {
				return config.getConcurrentExpiredUrl();
			}
		} catch (NoSuchBeanDefinitionException nsbde) {
			// no-op
			EgovJdkLogger.ignore("There is no SecurityConfig.class.");
		}
		
		return DEFAULT_EXPIRED_URL;
	}

	public Class<String> getObjectType() {
		return String.class;
	}

	public boolean isSingleton() {
		return true;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}
}