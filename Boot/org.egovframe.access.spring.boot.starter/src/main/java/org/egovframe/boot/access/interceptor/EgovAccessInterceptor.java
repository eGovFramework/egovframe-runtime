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
package org.egovframe.boot.access.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.access.EgovAccessProperties;
import org.egovframe.boot.access.service.EgovUserDetailsHelper;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class EgovAccessInterceptor implements HandlerInterceptor {

    private final EgovAccessProperties properties;

    public EgovAccessInterceptor(EgovAccessProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String requestUrl = request.getRequestURI().replace(request.getContextPath(),"");

        // 인증 체크
        if (!EgovUserDetailsHelper.isAuthenticated()) {
            log.debug("### EgovAccessInterceptor Authentication not authorized... ");
            response.sendRedirect(request.getContextPath().concat(properties.getLoginUrl()));
            return false;
        }

        // 권한별 접근 제한
        if (authCheck(requestUrl)) {
            log.debug("### EgovAccessInterceptor URL pattern matched... ");
            return true;
        }

        // 허가되지 않은 경우 접근 제한
        response.sendRedirect(request.getContextPath().concat(properties.getAccessDeniedUrl()));
        return false;
    }

    public Boolean authCheck(String requestUrl) {
        boolean authCheck = false;
        List<String> authList = EgovUserDetailsHelper.getAuthorities();
        List<Map<String, Object>> rolesList = EgovUserDetailsHelper.getRoles();

        if (!ObjectUtils.isEmpty(authList) && !ObjectUtils.isEmpty(rolesList)) {
            List<String> roleList = new ArrayList<>();
            for (String auth : authList) {
                Iterator<Map<String, Object>> iterator = rolesList.iterator();
                Map<String, Object> roleMap;
                while (iterator.hasNext()) {
                    roleMap = iterator.next();
                    if (auth.equals(roleMap.get("authority"))) {
                        roleList.add((String) roleMap.get("url"));
                    }
                }
            }

            List<String> urlList = roleList.stream().distinct().collect(Collectors.toList());
            for (String authUrl : urlList) {
                if ("ant".equals(properties.getRequestMatcherType())) {
                    authCheck = EgovAccessUtil.antMatcher(authUrl, requestUrl);
                } else {
                    authCheck = EgovAccessUtil.regexMatcher(authUrl, requestUrl);
                }
                if (authCheck) break;
            }
        }

        return authCheck;
    }

}
