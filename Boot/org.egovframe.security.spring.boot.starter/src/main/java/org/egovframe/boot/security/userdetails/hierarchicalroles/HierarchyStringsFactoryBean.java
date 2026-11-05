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
package org.egovframe.boot.security.userdetails.hierarchicalroles;

import lombok.Setter;
import org.egovframe.boot.security.EgovSecurityProperties;
import org.egovframe.boot.security.secureobject.EgovSecuredObjectService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.StringUtils;

public class HierarchyStringsFactoryBean implements FactoryBean<String> {

    private final EgovSecurityProperties properties;
    @Setter
    private EgovSecuredObjectService securedObjectService;
    private String hierarchyStrings;

    public HierarchyStringsFactoryBean(EgovSecurityProperties properties) {
        this.properties = properties;
    }

    public void init() throws Exception {
        if (properties == null) {
            throw new NoSuchBeanDefinitionException("### HierarchyStringsFactoryBean EgovSecurityProperties is not defined");
        }

        if (StringUtils.hasText(properties.getRoleHierarchyString())) {
            hierarchyStrings = properties.getRoleHierarchyString();
        } else {
            hierarchyStrings = securedObjectService.getHierarchicalRoles();
        }
    }

    @Override
    public String getObject() throws Exception {
        if (hierarchyStrings == null) {
            init();
        }
        return hierarchyStrings;
    }

    @Override
    public Class<String> getObjectType() {
        return String.class;
    }

}
