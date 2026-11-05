/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.access.EgovAccessProperties;
import org.egovframe.boot.access.service.EgovUserDetailsHelper;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 세션 인증 여부와 사용자 권한의 URL 패턴으로 요청 접근을 판정하는 인터셉터.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  요청 경로 정규화(UrlPathHelper, cleanPath) 후 판정
 * </pre>
 * @since 2025.06.01
 */
@Slf4j
public class EgovAccessInterceptor implements HandlerInterceptor {

    // 요청 경로 디코딩 + contextPath 접두사 제거
    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

    private final EgovAccessProperties properties;

    public EgovAccessInterceptor(EgovAccessProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 디코딩 후 "." / ".." 를 정규화해 경로 조작으로 인가 패턴을 우회하지 못하게 한다.
        String requestUrl = StringUtils.cleanPath(URL_PATH_HELPER.getPathWithinApplication(request));

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
