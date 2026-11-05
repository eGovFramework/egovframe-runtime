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
package org.egovframe.boot.access.bean;

import lombok.Setter;
import org.egovframe.boot.access.service.EgovAccessService;
import org.springframework.beans.factory.FactoryBean;

import java.util.List;
import java.util.Map;

@Setter
public class AuthorityMapFactoryBean implements FactoryBean<List<Map<String, Object>>> {

    private EgovAccessService egovAccessService;
    private List<Map<String, Object>> authorityList;

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
    public Class<?> getObjectType() {
        return null;
    }

}
