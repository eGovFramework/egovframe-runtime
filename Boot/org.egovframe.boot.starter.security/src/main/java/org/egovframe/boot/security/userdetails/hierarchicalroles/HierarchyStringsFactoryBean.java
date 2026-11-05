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
package org.egovframe.boot.security.userdetails.hierarchicalroles;

import lombok.Setter;
import org.egovframe.boot.security.EgovSecurityProperties;
import org.egovframe.boot.security.secureobject.EgovSecuredObjectService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 권한 계층 문자열(DB 또는 기본값)을 제공하는 FactoryBean.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * </pre>
 * @since 2025.06.01
 */
public class HierarchyStringsFactoryBean implements FactoryBean<String> {

    private static final String ROLE_HIERARCHY_STRING = "ROLE_ADMIN > ROLE_USER > ROLE_ANONYMOUS";
    private final EgovSecurityProperties properties;
    @Setter
    private EgovSecuredObjectService securedObjectService;

    public HierarchyStringsFactoryBean(EgovSecurityProperties properties) {
        this.properties = properties;
    }

    public String getObject() {
        if (ObjectUtils.isEmpty(properties)) {
            throw new NoSuchBeanDefinitionException("### HierarchyStringsFactoryBean getSqlHierarchicalRoles not found.");
        }

        if (StringUtils.hasText(properties.getSqlHierarchicalRoles())) {
            return securedObjectService.getHierarchicalRoles();
        }

        return ROLE_HIERARCHY_STRING;
    }

    public Class<String> getObjectType() {
        return String.class;
    }

}
