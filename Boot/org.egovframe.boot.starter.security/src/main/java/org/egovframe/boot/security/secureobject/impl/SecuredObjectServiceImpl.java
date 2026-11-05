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
package org.egovframe.boot.security.secureobject.impl;

import lombok.Setter;
import org.egovframe.boot.security.EgovSecurityProperties;
import org.egovframe.boot.security.secureobject.EgovSecuredObjectService;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ObjectUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * 보호자원 조회 서비스 구현체.
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
public class SecuredObjectServiceImpl implements EgovSecuredObjectService {

    private final EgovSecurityProperties properties;
    @Setter
    private SecuredObjectDAO securedObjectDAO;
    private String requestMatcherType = "ant";    // 설정값이 없을 때만 사용

    public SecuredObjectServiceImpl(EgovSecurityProperties properties) {
        this.properties = properties;
    }

    public LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getRolesAndUrl() {
        if (!ObjectUtils.isEmpty(properties.getRequestMatcherType())) {
            this.requestMatcherType = properties.getRequestMatcherType();
        }
        LinkedHashMap<RequestMatcher, List<ConfigAttribute>> ret = new LinkedHashMap<>();
        LinkedHashMap<Object, List<ConfigAttribute>> data = securedObjectDAO.getRolesAndUrl(requestMatcherType);
        Set<Object> keys = data.keySet();
        for (Object key : keys) {
            ret.put((RequestMatcher) key, data.get(key));
        }
        return ret;
    }

    public LinkedHashMap<String, List<ConfigAttribute>> getRolesAndMethod() {
        LinkedHashMap<String, List<ConfigAttribute>> ret = new LinkedHashMap<>();
        LinkedHashMap<Object, List<ConfigAttribute>> data = securedObjectDAO.getRolesAndMethod();
        return getRolesAndPath(ret, data);
    }

    public LinkedHashMap<String, List<ConfigAttribute>> getRolesAndPointcut() {
        LinkedHashMap<String, List<ConfigAttribute>> ret = new LinkedHashMap<>();
        LinkedHashMap<Object, List<ConfigAttribute>> data = securedObjectDAO.getRolesAndPointcut();
        return getRolesAndPath(ret, data);
    }

    private LinkedHashMap<String, List<ConfigAttribute>> getRolesAndPath(
            LinkedHashMap<String, List<ConfigAttribute>> ret, LinkedHashMap<Object, List<ConfigAttribute>> data) {
        Set<Object> keys = data.keySet();
        for (Object key : keys) {
            ret.put((String) key, data.get(key));
        }
        return ret;
    }

    public List<ConfigAttribute> getMatchedRequestMapping(String url) {
        return securedObjectDAO.getRegexMatchedRequestMapping(url);
    }

    public String getHierarchicalRoles() {
        return securedObjectDAO.getHierarchicalRoles();
    }

}
