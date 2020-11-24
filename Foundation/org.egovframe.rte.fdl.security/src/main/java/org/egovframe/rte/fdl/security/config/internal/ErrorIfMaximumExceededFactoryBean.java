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
package org.egovframe.rte.fdl.security.config.internal;

import org.egovframe.rte.fdl.logging.util.EgovJdkLogger;
import org.egovframe.rte.fdl.security.config.SecurityConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 중복로그인 옵 지정을 처리하는 factory bean 클래스
 *
 * <p>Desc.: 설정 간소화 처리에 사용되는 내부 factory bean</p>
 *
 * @author Egovframework Center
 * @since 2020.08.31
 * @version 3.10
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자					수정내용
 * ----------------------------------------------
 * 2020.08.31	Egovframework Center	최초 생성
 *
 * </pre>
 */
public class ErrorIfMaximumExceededFactoryBean implements FactoryBean<Boolean>, ApplicationContextAware {

	public static final boolean ERROR_IF_MAXIMUM_EXCEEDED = false;
	private ApplicationContext context;

	public Boolean getObject() throws Exception {
		try {
			SecurityConfig config = context.getBean(SecurityConfig.class);
			if ((Boolean) config.getErrorIfMaximumExceeded() instanceof Boolean) {
				return config.getErrorIfMaximumExceeded();
			}
		}  catch (NoSuchBeanDefinitionException nsbde) {
			EgovJdkLogger.ignore("There is no SecurityConfig.class.");
		}

		return ERROR_IF_MAXIMUM_EXCEEDED;
	}

	public Class<Boolean> getObjectType() {
		return Boolean.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

}
