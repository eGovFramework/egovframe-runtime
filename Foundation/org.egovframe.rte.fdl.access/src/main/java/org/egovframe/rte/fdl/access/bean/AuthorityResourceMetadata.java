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
package org.egovframe.rte.fdl.access.bean;

import org.egovframe.rte.fdl.access.service.EgovAccessService;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 권한 및 접근제한 정보를 매핑하는 클래스
 *
 * <p>Desc.: 권한 및 접근제한 정보를 매핑하는 클래스</p>
 *
 * @author 유지보수
 * @version 3.9
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2019.10.01	유지보수            최초 생성
 * 2021.02.01   유지보수            권한재설정 수정
 * 2024.03.29   유지보수            권한재설정(reload() method) 수정
 * </pre>
 * @since 2019.10.01
 */
public class AuthorityResourceMetadata {

    private static List<Map<String, Object>> authorityList;
    private static List<Map<String, Object>> resourceMap;
    private EgovAccessService egovAccessService;

    public AuthorityResourceMetadata(List<Map<String, Object>> authorityList, List<Map<String, Object>> resourceMap) {
        this.authorityList = authorityList;
        this.resourceMap = resourceMap;
    }

    public static List<Map<String, Object>> getAuthorityList() {
        return authorityList;
    }

    public static List<Map<String, Object>> getResourceMap() {
        return resourceMap;
    }

    public void setEgovAccessService(EgovAccessService egovAccessService) {
        this.egovAccessService = egovAccessService;
    }

    public void reload() throws Exception {
        List<Map<String, Object>> authList = egovAccessService.getAuthorityUser();
        Iterator<Map<String, Object>> authIterator = authList.iterator();
        authorityList.clear();
        while (authIterator.hasNext()) {
            authorityList.add(authIterator.next());
        }

        List<Map<String, Object>> rolelist = egovAccessService.getRoleAndUrl();
        Iterator<Map<String, Object>> roleIterator = rolelist.iterator();
        resourceMap.clear();
        while (roleIterator.hasNext()) {
            resourceMap.add(roleIterator.next());
        }
    }

}
