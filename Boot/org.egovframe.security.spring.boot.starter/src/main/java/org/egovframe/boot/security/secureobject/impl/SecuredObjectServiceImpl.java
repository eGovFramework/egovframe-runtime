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
package org.egovframe.boot.security.secureobject.impl;

import lombok.Setter;
import org.egovframe.boot.security.EgovSecurityProperties;
import org.egovframe.boot.security.secureobject.EgovSecuredObjectService;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class SecuredObjectServiceImpl implements EgovSecuredObjectService {

	private final EgovSecurityProperties properties;
	@Setter
    private SecuredObjectDAO securedObjectDAO;
	private String requestMatcherType = "ant";	// default

    public SecuredObjectServiceImpl(EgovSecurityProperties properties) {
        this.properties = properties;
    }

    public LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getRolesAndUrl() {
		if (properties.getRequestMatcherType() != null) {
			this.requestMatcherType = properties.getRequestMatcherType();
		}
		LinkedHashMap<RequestMatcher, List<ConfigAttribute>> ret = new LinkedHashMap<>();
		LinkedHashMap<Object, List<ConfigAttribute>> data = securedObjectDAO.getRolesAndUrl(requestMatcherType);
		Set<Object> keys = data.keySet();
		for (Object key : keys) {
			ret.put((RequestMatcher) key, data.get(key));
		}
		return ret;
	}

	public LinkedHashMap<String, List<ConfigAttribute>> getRolesAndMethod() {
		LinkedHashMap<String, List<ConfigAttribute>> ret = new LinkedHashMap<>();
		LinkedHashMap<Object, List<ConfigAttribute>> data = securedObjectDAO.getRolesAndMethod();
		return getRolesAndPath(ret, data);
	}

	public LinkedHashMap<String, List<ConfigAttribute>> getRolesAndPointcut() {
		LinkedHashMap<String, List<ConfigAttribute>> ret = new LinkedHashMap<>();
		LinkedHashMap<Object, List<ConfigAttribute>> data = securedObjectDAO.getRolesAndPointcut();
		return getRolesAndPath(ret, data);
	}

	private LinkedHashMap<String, List<ConfigAttribute>> getRolesAndPath(
			LinkedHashMap<String, List<ConfigAttribute>> ret, LinkedHashMap<Object, List<ConfigAttribute>> data) {
		Set<Object> keys = data.keySet();
		for (Object key : keys) {
			ret.put((String) key, data.get(key));
		}
		return ret;
	}

	public List<ConfigAttribute> getMatchedRequestMapping(String url) {
		return securedObjectDAO.getRegexMatchedRequestMapping(url);
	}

	public String getHierarchicalRoles() {
		return securedObjectDAO.getHierarchicalRoles();
	}

}
