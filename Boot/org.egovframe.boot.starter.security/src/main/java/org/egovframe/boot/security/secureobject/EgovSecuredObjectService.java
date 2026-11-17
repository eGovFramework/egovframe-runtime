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
package org.egovframe.boot.security.secureobject;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 보호자원(URL · 메서드 · 포인트컷)-권한 매핑 조회 서비스.
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
public interface EgovSecuredObjectService {

    /** URL 보호자원-권한 매핑 */
    LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getRolesAndUrl();

    /** 메서드 보호자원-권한 매핑(조회 전용, 인가에는 미적용) */
    LinkedHashMap<String, List<ConfigAttribute>> getRolesAndMethod();

    /** 포인트컷 보호자원-권한 매핑(조회 전용, 인가에는 미적용) */
    LinkedHashMap<String, List<ConfigAttribute>> getRolesAndPointcut();

    /** URL 에 가장 잘 일치하는 보호자원의 권한(DB 정규식 기능 필요) */
    List<ConfigAttribute> getMatchedRequestMapping(String url);

    /** 권한 계층 문자열 */
    String getHierarchicalRoles();

}
