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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.access.service.EgovAccessService;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class AuthorityResourceMetadata {

    @Getter
    @Setter
    private static List<Map<String, Object>> authorityList;
    @Getter
    @Setter
    private static List<Map<String, Object>> resourceMap;
    @Getter
    @Setter
    private EgovAccessService egovAccessService;

    public AuthorityResourceMetadata(List<Map<String, Object>> authorityList, List<Map<String, Object>> resourceMap) {
        AuthorityResourceMetadata.authorityList = authorityList;
        AuthorityResourceMetadata.resourceMap = resourceMap;
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
