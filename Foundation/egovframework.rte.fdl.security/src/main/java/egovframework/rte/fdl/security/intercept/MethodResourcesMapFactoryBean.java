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
package egovframework.rte.fdl.security.intercept;

import java.util.LinkedHashMap;
import java.util.List;

import egovframework.rte.fdl.security.securedobject.EgovSecuredObjectService;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.access.ConfigAttribute;

/**
 * Bean의 초기화 데이터 제공 기능을 구현 클래스
 * @since 2014.01.22
 * @version 3.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2014.01.22  한성곤		Spring Security 3.2.X 업그레이드 적용 (기존 ResourcesMapFactoryBean  분리)
 * 
 * </pre>
 */
public class MethodResourcesMapFactoryBean implements FactoryBean<LinkedHashMap<String, List<ConfigAttribute>>> {

    private String resourceType;

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    private EgovSecuredObjectService securedObjectService;

    public void setSecuredObjectService(EgovSecuredObjectService securedObjectService) {
        this.securedObjectService = securedObjectService;
    }

    private LinkedHashMap<String, List<ConfigAttribute>> resourcesMap;

    public void init() throws Exception {
        if ("method".equals(resourceType)) {
            resourcesMap = securedObjectService.getRolesAndMethod();
        } else if ("pointcut".equals(resourceType)) {
            resourcesMap = securedObjectService.getRolesAndPointcut();
        } else {
            throw new Exception("resourceType must be 'method' or 'pointcut'");
        }
    }

    public LinkedHashMap<String, List<ConfigAttribute>> getObject() throws Exception {
        if (resourcesMap == null) {
            init();
        }
        return resourcesMap;
    }

    @SuppressWarnings("rawtypes")
	public Class<LinkedHashMap> getObjectType() {
        return LinkedHashMap.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
