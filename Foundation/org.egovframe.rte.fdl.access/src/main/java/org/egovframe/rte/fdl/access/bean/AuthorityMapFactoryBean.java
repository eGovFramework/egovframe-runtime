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

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.FactoryBean;
import org.egovframe.rte.fdl.access.service.EgovAccessService;

/**
 * 권한 정보를 처리하는 factory bean 클래스
 *
 * <p>Desc.: 권한 정보를 처리하는 factory bean 클래스</p>
 *
 * @author Egovframework Center
 * @since 2019.10.01
 * @version 3.9
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				    수정내용
 * ----------------------------------------------
 * 2019.10.01	Egovframework Center	최초 생성
 * </pre>
 */
public class AuthorityMapFactoryBean implements FactoryBean<List<Map<String, Object>>> {

    private EgovAccessService egovAccessService;
    private List<Map<String, Object>> authorityList;

    public void setEgovAccessService(EgovAccessService egovAccessService) {
        this.egovAccessService = egovAccessService;
    }

    public void init() throws Exception {
        authorityList = egovAccessService.getAuthorityUser();
    }

    @Override
    public List<Map<String, Object>> getObject() throws Exception {
        if (authorityList == null) {
            init();
        }
        return authorityList;
    }

    @Override
    public Class<List> getObjectType() {
        return List.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
