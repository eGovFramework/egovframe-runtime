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
package org.egovframe.boot.security.bean;

import org.egovframe.boot.security.EgovSecurityProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.sql.DataSource;

public class DataSourceFactoryBean implements FactoryBean<DataSource>, ApplicationContextAware {

    private static final String DEF_DATASOURCE_NAME = "dataSource";

    private ApplicationContext context;

    private final EgovSecurityProperties properties;

    public DataSourceFactoryBean(EgovSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public DataSource getObject() throws Exception {
        if (properties.getDataSource() != null) {
            return (DataSource) context.getBean(properties.getDataSource());
        } else {
            if (context.containsBean(DEF_DATASOURCE_NAME)) {
                return (DataSource) context.getBean(DEF_DATASOURCE_NAME);
            } else {
                throw new NoSuchBeanDefinitionException("### DataSourceFactoryBean getDataSource is empty");
            }
        }
    }

    @Override
    public Class<DataSource> getObjectType() {
        return DataSource.class;
    }

}
