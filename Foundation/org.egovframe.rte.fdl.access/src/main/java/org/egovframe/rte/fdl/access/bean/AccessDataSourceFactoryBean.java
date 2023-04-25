/*
 * Copyright 2008-2019 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.access.bean;

import org.egovframe.rte.fdl.access.config.EgovAccessConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.sql.DataSource;

/**
 * dataSource 지정을 처리하는 factory bean 클래스
 *
 * <p>Desc.: 설정 간소화 처리에 사용되는 내부 factory bean</p>
 *
 * @author Egovframework Center
 * @since 2019.10.01
 * @version 3.9
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				    수정내용
 * ----------------------------------------------
 * 2019.10.01	Egovframework Center	최초 생성
 * </pre>
 */
public class AccessDataSourceFactoryBean implements FactoryBean<DataSource>, ApplicationContextAware {

    private static final String DEF_DATASOURCE_NAME = "dataSource";
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public DataSource getObject() throws Exception {
        EgovAccessConfig config = context.getBean(EgovAccessConfig.class);
        if (config == null) {
            throw new NoSuchBeanDefinitionException("##### AccessDataSourceFactoryBean EgovAccessConfig is not defined");
        }

        if (config.getDataSource() != null) {
            return config.getDataSource();
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

    @Override
    public boolean isSingleton() {
        return true;
    }

}
