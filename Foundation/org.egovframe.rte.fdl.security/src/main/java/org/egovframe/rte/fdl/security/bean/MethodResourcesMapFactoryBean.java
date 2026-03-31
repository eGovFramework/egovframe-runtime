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
package org.egovframe.rte.fdl.security.bean;

import org.egovframe.rte.fdl.security.secureobject.EgovSecuredObjectService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.util.ObjectUtils;

import java.util.LinkedHashMap;
import java.util.List;

public class MethodResourcesMapFactoryBean implements FactoryBean<LinkedHashMap<String, List<ConfigAttribute>>> {

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
        resourcesMap = new LinkedHashMap<>();
    }

    @Override
    public Class<?> getObjectType() {
        return LinkedHashMap.class;
    }

}
