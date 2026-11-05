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
package org.egovframe.boot.access.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.access.service.EgovAccessService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 사용자 권한 · URL 자원 목록을 보관하고 재적재하는 메타데이터 클래스.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  reload() 시 목록 참조를 원자적으로 교체
 * </pre>
 * @since 2025.06.01
 */
@Slf4j
public class AuthorityResourceMetadata {

    @Getter
    @Setter
    private static volatile List<Map<String, Object>> authorityList;
    @Getter
    @Setter
    private static volatile List<Map<String, Object>> resourceMap;
    @Getter
    @Setter
    private EgovAccessService egovAccessService;

    public AuthorityResourceMetadata(List<Map<String, Object>> authorityList, List<Map<String, Object>> resourceMap) {
        AuthorityResourceMetadata.authorityList = authorityList;
        AuthorityResourceMetadata.resourceMap = resourceMap;
    }

    public void reload() throws Exception {
        // 새 목록으로 참조를 교체해 요청 처리 중에도 완전한 스냅샷만 보이도록 한다.
        authorityList = new ArrayList<>(egovAccessService.getAuthorityUser());
        resourceMap = new ArrayList<>(egovAccessService.getRoleAndUrl());
    }

}
