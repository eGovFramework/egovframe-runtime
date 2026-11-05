/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.boot.access.bean;

import org.egovframe.boot.access.EgovAccessProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * 접근 제어에 사용할 DataSource 를 빈 이름으로 찾아 제공하는 FactoryBean.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  빈 이름이 비어 있으면 기본 dataSource 사용
 * </pre>
 * @since 2025.06.01
 */
public class DataSourceFactoryBean implements FactoryBean<DataSource>, ApplicationContextAware {

    private static final String DEF_DATASOURCE_NAME = "dataSource";
    private final EgovAccessProperties properties;
    private ApplicationContext context;

    public DataSourceFactoryBean(EgovAccessProperties properties) {
        this.properties = properties;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public DataSource getObject() {
        // 비어 있으면 기본 dataSource 사용
        if (StringUtils.hasText(properties.getDataSource())) {
            return (DataSource) context.getBean(properties.getDataSource());
        } else {
            if (context.containsBean(DEF_DATASOURCE_NAME)) {
                return (DataSource) context.getBean(DEF_DATASOURCE_NAME);
            } else {
                throw new NoSuchBeanDefinitionException("### AccessDataSourceFactoryBean getDataSource is empty");
            }
        }
    }

    @Override
    public Class<DataSource> getObjectType() {
        return DataSource.class;
    }

}
