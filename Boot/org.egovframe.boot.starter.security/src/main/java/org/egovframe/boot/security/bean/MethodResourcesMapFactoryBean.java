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
package org.egovframe.boot.security.bean;

import lombok.Setter;
import org.egovframe.boot.security.secureobject.EgovSecuredObjectService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.util.ObjectUtils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 메서드 보호자원-권한 매핑을 제공하는 FactoryBean.
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
public class MethodResourcesMapFactoryBean implements FactoryBean<LinkedHashMap<String, List<ConfigAttribute>>> {

    @Setter
    private EgovSecuredObjectService securedObjectService;
    @Setter
    private String resourceType;
    private LinkedHashMap<String, List<ConfigAttribute>> resourcesMap;

    public void init() throws Exception {
        if ("method".equals(resourceType)) {
            resourcesMap = securedObjectService.getRolesAndMethod();
        } else if ("pointcut".equals(resourceType)) {
            resourcesMap = securedObjectService.getRolesAndPointcut();
        } else {
            throw new Exception("### MethodResourcesMapFactoryBean.init resourceType must be 'method' or 'pointcut'");
        }
    }

    @Override
    public LinkedHashMap<String, List<ConfigAttribute>> getObject() throws Exception {
        if (ObjectUtils.isEmpty(resourcesMap)) {
            init();
        }
        return resourcesMap;
    }

    @Override
    public Class<?> getObjectType() {
        return LinkedHashMap.class;
    }

}
