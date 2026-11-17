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
package org.egovframe.rte.fdl.security.bean;

import org.egovframe.rte.fdl.security.secureobject.EgovSecuredObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.util.ObjectUtils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @deprecated Spring Security 6.x 의 DB 기반 method 권한 미지원으로 항상 빈 맵 반환 (no-op).
 * {@code @PreAuthorize}/{@code @PostAuthorize} 등 어노테이션 기반 메서드 보안으로 이전
 */
@Deprecated
public class MethodResourcesMapFactoryBean implements FactoryBean<LinkedHashMap<String, List<ConfigAttribute>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodResourcesMapFactoryBean.class);

    private EgovSecuredObjectService securedObjectService;
    private String resourceType;
    private LinkedHashMap<String, List<ConfigAttribute>> resourcesMap;

    public void setSecuredObjectService(EgovSecuredObjectService securedObjectService) {
        this.securedObjectService = securedObjectService;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public LinkedHashMap<String, List<ConfigAttribute>> getObject() throws Exception {
        if (ObjectUtils.isEmpty(resourcesMap)) {
            init();
        }
        return resourcesMap;
    }

    /**
     * DB 기반 method 권한은 Spring Security 6.x에서 미지원이므로 빈 맵 사용.
     * 메서드 보안은 @PreAuthorize 등 어노테이션으로 처리.
     */
    public void init() {
        LOGGER.warn("MethodResourcesMapFactoryBean is deprecated and always returns an empty map - " +
                "DB-based method security is not supported on Spring Security 6.x. Method-level authorization " +
                "via this bean will NOT be enforced. Migrate to @PreAuthorize/@PostAuthorize annotations.");
        resourcesMap = new LinkedHashMap<>();
    }

    @Override
    public Class<?> getObjectType() {
        return LinkedHashMap.class;
    }

}
