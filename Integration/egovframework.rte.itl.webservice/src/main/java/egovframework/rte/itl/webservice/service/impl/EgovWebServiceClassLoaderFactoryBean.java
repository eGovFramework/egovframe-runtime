/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package egovframework.rte.itl.webservice.service.impl;

import egovframework.rte.itl.webservice.service.EgovWebServiceClassLoader;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * EgovWebServiceCalssLoader의 FactoryBean 클래스
 * <p>
 * <b>NOTE:</b> EgovWebServiceCalssLoader의 FactoryBean class이다. </p>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009.06.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 * 
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.06.01  심상호           최초 생성
 *
 * </pre>
 */
public class EgovWebServiceClassLoaderFactoryBean implements
		FactoryBean<Object>, ApplicationContextAware {
	private EgovWebServiceClassLoader bean;

	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public Object getObject() throws Exception {
		if (bean == null) {
			bean = createEgovWebServiceClassLoader();
		}
		return bean;
	}

	public Class<?> getObjectType() {
		return EgovWebServiceClassLoader.class;
	}

	public boolean isSingleton() {
		return true;
	}

	private EgovWebServiceClassLoader createEgovWebServiceClassLoader() {
		if (applicationContext == null
				|| applicationContext.getClassLoader() == null) {
			return new EgovWebServiceClassLoaderImpl();
		} else {
			return new EgovWebServiceClassLoaderImpl(applicationContext.getClassLoader());
		}
	}
}
