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
package org.egovframe.rte.fdl.security.secureobject.impl;

import org.egovframe.rte.fdl.security.config.EgovSecurityConfig;
import org.egovframe.rte.fdl.security.secureobject.EgovSecuredObjectService;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * 보호객체 관리를 지원하는 구현 클래스
 *
 * <p><b>NOTE:</b> Spring Security의 초기 데이터를 DB로 부터 조회하여
 * 보호된 자원 접근 권한을 지원, 제어 할 수 있도록 구현한 클래스이다.</p>
 *
 * @author ByungHun Woo
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	윤성종				최초 생성
 * 2014.01.22	한성곤				Spring Security 3.2.X 업그레이드 적용
 * </pre>
 * @since 2009.06.01
 */
public class SecuredObjectServiceImpl implements EgovSecuredObjectService {

    private final EgovSecurityConfig config;
    private SecuredObjectDAO securedObjectDAO;
    private String requestMatcherType = "ant";    // default

    public SecuredObjectServiceImpl(EgovSecurityConfig config) {
        this.config = config;
    }

    public void setSecuredObjectDAO(SecuredObjectDAO securedObjectDAO) {
        this.securedObjectDAO = securedObjectDAO;
    }

    public LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getRolesAndUrl() {
        if (config.getRequestMatcherType() != null) {
            this.requestMatcherType = config.getRequestMatcherType();
        }
        LinkedHashMap<RequestMatcher, List<ConfigAttribute>> ret = new LinkedHashMap<>();
        LinkedHashMap<Object, List<ConfigAttribute>> data = securedObjectDAO.getRolesAndUrl(requestMatcherType);
        Set<Object> keys = data.keySet();
        for (Object key : keys) {
            ret.put((RequestMatcher) key, data.get(key));
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
