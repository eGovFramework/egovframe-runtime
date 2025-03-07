/*
 * Copyright 2008-2019 MOIS(Ministry of the Interior and Safety).
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 권한 및 접근제한 정보를 매핑하는 클래스
 *
 * <p>Desc.: 권한 및 접근제한 정보를 매핑하는 클래스</p>
 *
 * @author ESFC
 * @since 2019.10.01
 * @version 3.9
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2019.10.01	ESFC            최초 생성
 * 2021.02.01   ESFC            권한재설정 수정
 * 2024.03.29   ESFC            권한재설정(reload() method) 수정
 * </pre>
 */
public class AuthorityResourceMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorityResourceMetadata.class);

    private static List<Map<String, Object>> authorityList;
    private static List<Map<String, Object>> resourceMap;
    private EgovAccessService egovAccessService;

    public AuthorityResourceMetadata(List<Map<String, Object>> authorityList, List<Map<String, Object>> resourceMap) {
        this.authorityList = authorityList;
        this.resourceMap = resourceMap;
    }

    public void setEgovAccessService(EgovAccessService egovAccessService) {
        this.egovAccessService = egovAccessService;
    }

    public static List<Map<String, Object>> getAuthorityList() {
        return authorityList;
    }

    public static List<Map<String, Object>> getResourceMap() {
        return resourceMap;
    }

    public void reload() throws Exception {
        authorityList.clear();
        resourceMap.clear();

        List<Map<String, Object>> authList = egovAccessService.getAuthorityUser();
        authorityList.addAll(authList);

        List<Map<String, Object>> list = egovAccessService.getRoleAndUrl();
        resourceMap.addAll(list);

        LOGGER.info("##### AuthorityResourceMetadata >>> Role Mappings reloaded at Runtime #####");
    }

}
