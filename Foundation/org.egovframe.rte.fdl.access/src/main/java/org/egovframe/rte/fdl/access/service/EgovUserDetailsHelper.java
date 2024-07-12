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
package org.egovframe.rte.fdl.access.service;

import org.egovframe.rte.fdl.access.bean.AuthorityResourceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 사용자 계정 정보를 처리하는 유틸 클래스
 *
 * <p>Desc.: 사용자 계정 정보를 처리하는 유틸 클래스</p>
 *
 * @author ESFC
 * @since 2019.10.01
 * @version 3.9
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2019.10.01	ESFC			최초 생성
 * </pre>
 */
public class EgovUserDetailsHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovUserDetailsHelper.class);

	public static Object getAuthenticatedUser() {
		if (RequestContextHolder.getRequestAttributes() == null) {
			return null;
		}
		return RequestContextHolder.getRequestAttributes().getAttribute("loginVO", RequestAttributes.SCOPE_SESSION);
	}

	public static List<String> getAuthorities() {
		List<String> list = new ArrayList<String>();
		if (RequestContextHolder.getRequestAttributes() == null) {
			return null;
		} else {
			String accessUser = (String) RequestContextHolder.getRequestAttributes().getAttribute("accessUser", RequestAttributes.SCOPE_SESSION);
			List<Map<String, Object>> listmap = AuthorityResourceMetadata.getAuthorityList();
			if (!StringUtils.isEmpty(accessUser) && !ObjectUtils.isEmpty(listmap)) {
				Iterator<Map<String, Object>> iterator = listmap.iterator();
				Map<String, Object> tempMap;
				while (iterator.hasNext()) {
					tempMap = iterator.next();
					if (accessUser.equals(tempMap.get("userid"))) {
						list.add((String) tempMap.get("authority"));
					}
				}
			}
			return list;
		}
	}

	public static Boolean isAuthenticated() {
		if (RequestContextHolder.getRequestAttributes() == null) {
			return false;
		} else {
			if (RequestContextHolder.getRequestAttributes().getAttribute("loginVO", RequestAttributes.SCOPE_SESSION) == null) {
				return false;
			} else {
				return true;
			}
		}
	}

}
