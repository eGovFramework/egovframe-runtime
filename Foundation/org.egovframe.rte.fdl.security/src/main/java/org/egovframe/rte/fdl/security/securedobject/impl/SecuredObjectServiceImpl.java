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
package org.egovframe.rte.fdl.security.securedobject.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.egovframe.rte.fdl.security.config.SecurityConfig;
import org.egovframe.rte.fdl.security.securedobject.EgovSecuredObjectService;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 보호객체 관리를 지원하는 구현 클래스
 * 
 * <p><b>NOTE:</b> Spring Security의 초기 데이터를 DB로 부터 조회하여
 * 보호된 자원 접근 권한을 지원, 제어 할 수 있도록 구현한 클래스이다.</p>
 * 
 * @author ByungHun Woo
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	윤성종				최초 생성
 * 2014.01.22	한성곤				Spring Security 3.2.X 업그레이드 적용
 * </pre>
 */
public class SecuredObjectServiceImpl implements EgovSecuredObjectService, ApplicationContextAware {

	private SecuredObjectDAO securedObjectDAO;

    private String requestMatcherType = "ant";	// default

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		if (context.getBeanNamesForType(SecurityConfig.class).length > 0) {
			SecurityConfig config = context.getBean(SecurityConfig.class);
			if (config != null) {
				requestMatcherType = config.getRequestMatcherType();
			}
		}
	}

    public void setSecuredObjectDAO(SecuredObjectDAO securedObjectDAO) {
        this.securedObjectDAO = securedObjectDAO;
    }
    
    public void setRequestMatcherType(String requestMatcherType) {
    	this.requestMatcherType = requestMatcherType;
    }
    
	public LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getRolesAndUrl() throws Exception {
		LinkedHashMap<RequestMatcher, List<ConfigAttribute>> ret = new LinkedHashMap<RequestMatcher, List<ConfigAttribute>>();
		LinkedHashMap<Object, List<ConfigAttribute>> data = securedObjectDAO.getRolesAndUrl(requestMatcherType);
		Set<Object> keys = data.keySet();
		for (Object key : keys) {
			ret.put((RequestMatcher) key, data.get(key));
		}
		return ret;
	}

	public LinkedHashMap<String, List<ConfigAttribute>> getRolesAndMethod() throws Exception {
		LinkedHashMap<String, List<ConfigAttribute>> ret = new LinkedHashMap<String, List<ConfigAttribute>>();
		LinkedHashMap<Object, List<ConfigAttribute>> data = securedObjectDAO.getRolesAndMethod();
		return getRolesAndPath(ret, data);
	}

	public LinkedHashMap<String, List<ConfigAttribute>> getRolesAndPointcut() throws Exception {
		LinkedHashMap<String, List<ConfigAttribute>> ret = new LinkedHashMap<String, List<ConfigAttribute>>();
		LinkedHashMap<Object, List<ConfigAttribute>> data = securedObjectDAO.getRolesAndPointcut();
		return getRolesAndPath(ret, data);
	}

	private LinkedHashMap<String, List<ConfigAttribute>> getRolesAndPath(LinkedHashMap<String, List<ConfigAttribute>> ret, LinkedHashMap<Object, List<ConfigAttribute>> data) {
		Set<Object> keys = data.keySet();
		for (Object key : keys) {
			ret.put((String) key, data.get(key));
		}
		return ret;
	}

	public List<ConfigAttribute> getMatchedRequestMapping(String url) throws Exception {
		return securedObjectDAO.getRegexMatchedRequestMapping(url);
	}

	public String getHierarchicalRoles() throws Exception {
		return securedObjectDAO.getHierarchicalRoles();
	}

}
