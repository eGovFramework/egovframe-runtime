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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
 * 2026.10.25   실행환경 개발팀      reload() 시 목록 참조 원자 교체(갱신 중 빈 목록으로 접근 판정되는 구간 제거)
 * </pre>
 * @since 2019.10.01
 */
public class AuthorityResourceMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorityResourceMetadata.class);

    private static volatile List<Map<String, Object>> authorityList;
    private static volatile List<Map<String, Object>> resourceMap;
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
        // 새 리스트를 만들어 static 참조를 원자적으로 교체한다.
        // 기존에는 clear()+addAll()로 제자리 변경해, 갱신 중인 요청이 빈(부분) 목록을 읽어 접근이 잘못 판정되거나
        // 순회 중 ConcurrentModificationException 이 발생할 수 있었다.
        authorityList = new ArrayList<Map<String, Object>>(egovAccessService.getAuthorityUser());
        resourceMap = new ArrayList<Map<String, Object>>(egovAccessService.getRoleAndUrl());

        LOGGER.info("### AuthorityResourceMetadata >>> Role Mappings reloaded at Runtime ###");
    }

}
