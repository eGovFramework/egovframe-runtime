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
package org.egovframe.boot.access.service;

import org.egovframe.boot.access.bean.AuthorityResourceMetadata;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EgovUserDetailsHelper {

	public static Object getAuthenticatedUser() {
		if (RequestContextHolder.getRequestAttributes() == null) {
			return null;
		}
		return RequestContextHolder.getRequestAttributes().getAttribute("loginVO", RequestAttributes.SCOPE_SESSION);
	}

	public static List<String> getAuthorities() {
		List<String> list = new ArrayList<>();
		if (isAuthenticated()) {
			String accessUser = (String) RequestContextHolder.getRequestAttributes().getAttribute("accessUser", RequestAttributes.SCOPE_SESSION);
			List<Map<String, Object>> authList = AuthorityResourceMetadata.getAuthorityList();
			if (!ObjectUtils.isEmpty(accessUser) && !ObjectUtils.isEmpty(authList)) {
				Iterator<Map<String, Object>> iterator = authList.iterator();
				Map<String, Object> authMap;
				while (iterator.hasNext()) {
					authMap = iterator.next();
					if (accessUser.equals(authMap.get("userid"))) {
						list.add((String) authMap.get("authority"));
					}
				}
			}
			return list;
		} else {
			return null;
		}
	}

	public static Boolean isAuthenticated() {
		if (RequestContextHolder.getRequestAttributes() == null) {
			return false;
		} else {
			return RequestContextHolder.getRequestAttributes().getAttribute("loginVO", RequestAttributes.SCOPE_SESSION) != null;
		}
	}

	public static List<Map<String, Object>> getRoles() {
		if (!isAuthenticated()) {
			return null;
		} else {
			List<Map<String, Object>> list = AuthorityResourceMetadata.getResourceMap();
			if (ObjectUtils.isEmpty(list)) {
				return null;
			} else {
				return list;
			}
		}
	}

}
