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
package egovframework.rte.fdl.access.bean;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import egovframework.rte.fdl.access.service.EgovAccessService;

/**
 * 권한 및 접근제한 정보를 매핑하는 클래스
 *
 * <p>Desc.: 권한 및 접근제한 정보를 매핑하는 클래스</p>
 *
 * @author Egovframework Center
 * @since 2019.10.01
 * @version 3.9
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 * 수정일			수정자					수정내용
 * ---------------------------------------------------------------------------------
 * 2019.10.01	Egovframework Center	최초 생성
 *
 * </pre>
 */
public class AuthorityResourceMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorityResourceMetadata.class);

    private EgovAccessService egovAccessService;
    private static List<Map<String, Object>> authorityList;
    private List<Map<String, Object>> resourceMap;

    public AuthorityResourceMetadata(List<Map<String, Object>> authorityList, List<Map<String, Object>> resourceMap) {
        this.authorityList = authorityList;
        this.resourceMap = resourceMap;
    }

    public EgovAccessService getEgovAccessService() {
        return egovAccessService;
    }

    public void setEgovAccessService(EgovAccessService egovAccessService) {
        this.egovAccessService = egovAccessService;
    }

    public static List<Map<String, Object>> getAuthorityList() {
        return authorityList;
    }

    public List<Map<String, Object>> getResourceMap() {
        return resourceMap;
    }

    public void reload() throws Exception {
        List<Map<String, Object>> list = egovAccessService.getRoleAndUrl();
        Iterator<Map<String, Object>> iterator = list.iterator();
        resourceMap.clear();
        while (iterator.hasNext()) {
            resourceMap.add(iterator.next());
        }
        LOGGER.info("##### AuthorityResourceMetadata >>> Role Mappings reloaded at Runtime #####");
    }

}
