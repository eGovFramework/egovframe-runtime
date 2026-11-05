/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package org.egovframe.boot.security.secureobject;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;
import java.util.List;

public interface EgovSecuredObjectService {

    /**
     * 롤에 대한 URL의 매핑 정보를 얻어온다.
     */
    LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getRolesAndUrl() throws Exception;

    /**
     * 롤에 대한 메소드의 매핑 정보를 얻어온다.
     */
    LinkedHashMap<String, List<ConfigAttribute>> getRolesAndMethod() throws Exception;

    /**
     * 롤에 대한 AOP pointcut 메핑 정보를 얻어온다.
     */
    LinkedHashMap<String, List<ConfigAttribute>> getRolesAndPointcut() throws Exception;

    /**
     * Best 매칭 정보를 얻어온다.
     */
    List<ConfigAttribute> getMatchedRequestMapping(String url) throws Exception;

    /**
     * 롤의 계층적 구조를 얻어온다.
     */
    String getHierarchicalRoles() throws Exception;

}
