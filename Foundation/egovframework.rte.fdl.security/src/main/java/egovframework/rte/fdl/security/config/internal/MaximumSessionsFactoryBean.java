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

public class MaximumSessionsFactoryBean implements FactoryBean<Integer>, ApplicationContextAware {
	private ApplicationContext context;
	
	public static final int DEFAULT_MAX_SESSIONS = -1;

	public Integer getObject() throws Exception {
		try {
			SecurityConfig config = context.getBean(SecurityConfig.class);
			
			if (config.getConcurrentMaxSessons() > 0) {
				return config.getConcurrentMaxSessons();
			}
		} catch (NoSuchBeanDefinitionException nsbde) {
			// no-op
			EgovJdkLogger.ignore("There is no SecurityConfig.class.");
		}
		
		return DEFAULT_MAX_SESSIONS;
	}

	public Class<Integer> getObjectType() {
		return Integer.class;
	}

	public boolean isSingleton() {
		return true;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}
}