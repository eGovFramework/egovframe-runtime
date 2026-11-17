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
package org.egovframe.boot.access.service.impl;

import lombok.Setter;
import org.egovframe.boot.access.service.EgovAccessService;

import java.util.List;
import java.util.Map;

/**
 * 접근 제어 정보 조회 서비스 구현체.
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
@Setter
public class EgovAccessServiceImpl implements EgovAccessService {

    private EgovAccessDao egovAccessDao;

    public List<Map<String, Object>> getAuthorityUser() {
        return egovAccessDao.getAuthorityUser();
    }

    public List<Map<String, Object>> getRoleAndUrl() {
        return egovAccessDao.getRoleAndUrl();
    }

}
