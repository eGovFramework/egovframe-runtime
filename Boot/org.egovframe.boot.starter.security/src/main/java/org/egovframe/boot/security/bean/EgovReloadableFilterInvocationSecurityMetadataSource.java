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

import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.egovframe.boot.security.secureobject.EgovSecuredObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.*;
import java.util.Map.Entry;

/**
 * URL 보호자원-권한 매핑을 보관하고 재적재하는 메타데이터 소스.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  reload() 원자 교체, 요청 단위 조회 메서드 추가
 * </pre>
 * @since 2025.06.01
 */
public class EgovReloadableFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovReloadableFilterInvocationSecurityMetadataSource.class);
    // reload() 시 새 맵으로 교체(요청 스레드는 완전한 스냅샷만 읽음)
    private volatile Map<RequestMatcher, Collection<ConfigAttribute>> requestMap;
    @Setter
    private EgovSecuredObjectService securedObjectService;

    public EgovReloadableFilterInvocationSecurityMetadataSource(LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap) {
        this.requestMap = requestMap;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        Set<ConfigAttribute> allAttributes = new HashSet<>();
        for (Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {
            allAttributes.addAll(entry.getValue());
        }
        return allAttributes;
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) {
        return getAttributes(((FilterInvocation) object).getRequest());
    }

    /** 요청에 처음 일치하는 보호자원의 권한 목록(없으면 null) */
    public Collection<ConfigAttribute> getAttributes(HttpServletRequest request) {
        for (Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {
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

    public void reload() {
        LinkedHashMap<RequestMatcher, List<ConfigAttribute>> reloadedMap = securedObjectService.getRolesAndUrl();
        Iterator<Entry<RequestMatcher, List<ConfigAttribute>>> iterator = reloadedMap.entrySet().iterator();
        // 새 맵을 완성한 뒤 교체(제자리 갱신 시 요청 스레드가 빈/부분 맵을 볼 수 있음)
        Map<RequestMatcher, Collection<ConfigAttribute>> reloaded = new LinkedHashMap<>();
        while (iterator.hasNext()) {
            Entry<RequestMatcher, List<ConfigAttribute>> entry = iterator.next();
            reloaded.put(entry.getKey(), entry.getValue());
        }
        this.requestMap = reloaded;
        LOGGER.info("Secured Url Resources - Role Mappings reloaded at Runtime!");
    }

}
