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
package org.egovframe.boot.access.bean;

import org.egovframe.boot.access.EgovAccessProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.StringUtils;

public class RoleAndUrlFactoryBean implements FactoryBean<String> {

    private final EgovAccessProperties properties;

    public RoleAndUrlFactoryBean(EgovAccessProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getObject() {
        if (properties == null) {
            throw new NoSuchBeanDefinitionException("### RoleAndUrlFactoryBean EgovAccessProperties is not defined");
        }

        if (StringUtils.hasText(properties.getSqlRoleAndUrl())) {
            return properties.getSqlRoleAndUrl();
        } else {
            throw new NoSuchBeanDefinitionException("### RoleAndUrlFactoryBean getSqlRoleAndUrl is empty");
        }
    }

    @Override
    public Class<String> getObjectType() {
        return String.class;
    }

}
