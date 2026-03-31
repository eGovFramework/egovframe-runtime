/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.property.db.initializer;

import jakarta.servlet.ServletContext;
import org.egovframe.rte.fdl.property.db.DbPropertySource;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * 웹환경에서 DB기반의 PropertySource를 등록하는 Initializer 클래스
 * Spring 6.x 기준으로 WebApplicationContext 타입 분기 추가.
 */
public class DBPropertySourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String PROPERTY_SOURCE_CONFIG_LOCATION = "propertySourceConfigLocation";

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        if (ctx instanceof ConfigurableWebApplicationContext webCtx) {
            ServletContext servletContext = webCtx.getServletContext();
            String xmlContextPath = servletContext.getInitParameter(PROPERTY_SOURCE_CONFIG_LOCATION);

            if (xmlContextPath != null) {
                MutablePropertySources propertySources = ctx.getEnvironment().getPropertySources();
                propertySources.addFirst(getPropertySource(xmlContextPath));
            } else {
                throw new IllegalStateException("ServletContext init parameter '" +
                        PROPERTY_SOURCE_CONFIG_LOCATION + "' is not set.");
            }
        } else {
            throw new IllegalStateException("ApplicationContext is not a WebApplicationContext");
        }
    }

    private PropertySource<?> getPropertySource(String xmlContextPath) {
        try (ClassPathXmlApplicationContext propertySourceContext = new ClassPathXmlApplicationContext(xmlContextPath)) {
            return propertySourceContext.getBean(DbPropertySource.class);
        }
    }

}
