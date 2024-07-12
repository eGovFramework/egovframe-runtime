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
package org.egovframe.rte.fdl.access.service.impl;

import org.egovframe.rte.fdl.access.service.EgovAccessService;

import java.util.List;
import java.util.Map;

/**
 * DB기반의 보호된 자원 관리를 지원하는 구현 클래스
 *
 * <p>Desc.: DB기반의 보호된 자원 관리를 지원하는 구현 클래스</p>
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
 * </pre>
 */
public class EgovAccessServiceImpl implements EgovAccessService {

    private EgovAccessDao egovAccessDao;

    public void setEgovAccessDao(EgovAccessDao egovAccessDao) {
        this.egovAccessDao = egovAccessDao;
    }

    public List<Map<String, Object>> getAuthorityUser() throws Exception {
        return egovAccessDao.getAuthorityUser();
    }

    public List<Map<String, Object>> getRoleAndUrl() throws Exception {
        return egovAccessDao.getRoleAndUrl();
    }

}
