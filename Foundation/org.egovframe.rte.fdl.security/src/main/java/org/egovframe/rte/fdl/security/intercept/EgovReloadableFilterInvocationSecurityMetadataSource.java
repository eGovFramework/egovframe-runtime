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
package org.egovframe.rte.fdl.security.intercept;

import org.egovframe.rte.fdl.security.securedobject.EgovSecuredObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.Map.Entry;

/**
 * MetadataSource 처리.
 *
 * @author 실행환경 개발팀 윤성종
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01   윤성종		        최초 생성
 * 2014.01.22   한성곤		        Spring Security 3.2.X 업그레이드 적용
 * 2026.10.25   실행환경 개발팀      reload() 시 맵을 비운 뒤 다시 채우던 방식을 새 맵 원자 교체로 변경
 *                               (재적재 중 빈 맵으로 인가가 적용되지 않는 구간 제거)
 * </pre>
 * @since 2009.06.01
 */
public class EgovReloadableFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovReloadableFilterInvocationSecurityMetadataSource.class);
    /**
     * URL-권한 매핑. reload() 시 새 맵을 완성한 뒤 참조를 한 번에 교체한다(volatile).
     * 기존처럼 clear() 후 다시 채우면 그 사이 요청이 빈 맵을 보고 권한 검사 없이 통과할 수 있다.
     */
    private volatile Map<RequestMatcher, Collection<ConfigAttribute>> requestMap;
    private EgovSecuredObjectService securedObjectService;

    public EgovReloadableFilterInvocationSecurityMetadataSource(LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap) {
        this.requestMap = requestMap;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        Set<ConfigAttribute> allAttributes = new HashSet<ConfigAttribute>();
        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {
            allAttributes.addAll(entry.getValue());
        }
        return allAttributes;
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) {
        final HttpServletRequest request = ((FilterInvocation) object).getRequest();
        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {
            if (entry.getKey().matches(request)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }

    public void setSecuredObjectService(EgovSecuredObjectService securedObjectService) {
        this.securedObjectService = securedObjectService;
    }

    public void reload() throws Exception {
        LinkedHashMap<RequestMatcher, List<ConfigAttribute>> reloadedMap = securedObjectService.getRolesAndUrl();
        Map<RequestMatcher, Collection<ConfigAttribute>> reloaded = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        for (Entry<RequestMatcher, List<ConfigAttribute>> entry : reloadedMap.entrySet()) {
            reloaded.put(entry.getKey(), entry.getValue());
        }
        // 새 맵을 완성한 뒤 한 번에 교체한다(조회 중인 요청은 이전 맵을 끝까지 사용).
        this.requestMap = reloaded;
        LOGGER.info("Secured Url Resources - Role Mappings reloaded at Runtime!");
    }

}
