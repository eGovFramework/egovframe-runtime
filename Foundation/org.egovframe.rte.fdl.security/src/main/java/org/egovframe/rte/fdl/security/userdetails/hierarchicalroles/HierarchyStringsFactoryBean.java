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
package org.egovframe.rte.fdl.security.userdetails.hierarchicalroles;

import org.egovframe.rte.fdl.security.config.EgovSecurityConfig;
import org.egovframe.rte.fdl.security.secureobject.EgovSecuredObjectService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * DB기반의 롤 계층정보를 지원하는 비즈니스 구현 클래스
 *
 * <p><b>NOTE:</b> DB 기반의 Role 계층 관계 정보를 얻어 이를 참조하는 Bean 의 초기화 데이터로 제공한다.</p>
 *
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01   윤성종		        최초 생성
 * 2014.01.22   한성곤		        Spring Security 3.2.X 업그레이드 적용, 설정 간소화 처리 관련 변경
 * </pre>
 * @since 2009.06.01
 */
public class HierarchyStringsFactoryBean implements FactoryBean<String> {

    private static final String ROLE_HIERARCHY_STRING = "ROLE_ADMIN > ROLE_USER > ROLE_ANONYMOUS";
    private final EgovSecurityConfig config;
    private EgovSecuredObjectService securedObjectService;

    public HierarchyStringsFactoryBean(EgovSecurityConfig config) {
        this.config = config;
    }

    public void setSecuredObjectService(EgovSecuredObjectService securedObjectService) {
        this.securedObjectService = securedObjectService;
    }

    public String getObject() {
        if (ObjectUtils.isEmpty(config)) {
            throw new NoSuchBeanDefinitionException("### HierarchyStringsFactoryBean getSqlHierarchicalRoles not found.");
        }
        if (StringUtils.hasText(config.getSqlHierarchicalRoles())) {
            return securedObjectService.getHierarchicalRoles();
        }

        return ROLE_HIERARCHY_STRING;
    }

    public Class<String> getObjectType() {
        return String.class;
    }

}
