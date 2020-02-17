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
package egovframework.rte.fdl.property.db.initializer;

import egovframework.rte.fdl.property.db.DbPropertySource;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.web.context.WebApplicationContext;

/**
 * 웹환경에서 DB기반의 PropertySource를 등록하는 클래스
 * @author yjLee
 *
 * @param <T>
 */
public class DBPropertySourceInitializer<T> implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final String PROPERTY_SOUCE_CONFIG_LOCATION = "propertySourceConfigLocation";

	public void initialize(ConfigurableApplicationContext ctx) {
		String xmlContextPath = ((WebApplicationContext) ctx).getServletContext().getInitParameter(PROPERTY_SOUCE_CONFIG_LOCATION);
		MutablePropertySources propertySources = ctx.getEnvironment().getPropertySources();
		propertySources.addFirst(getPropertySource(xmlContextPath));
	}

	public PropertySource<?> getPropertySource(String xmlContextPath) {
		ClassPathXmlApplicationContext propertySourceContext = new ClassPathXmlApplicationContext(xmlContextPath);
		PropertySource<?> propertySource = (PropertySource<?>) propertySourceContext.getBean(DbPropertySource.class);
		propertySourceContext.close();

		return propertySource;
	}

}
