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
package org.egovframe.rte.fdl.access.interceptor;

import org.egovframe.rte.fdl.access.config.EgovAccessConfigShare;
import org.egovframe.rte.fdl.access.service.EgovUserDetailsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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

/**
 * 인증 및 접근제한 인터셉터
 *
 * <p>Desc.: 인증 및 접근제한 인터셉터</p>
 *
 * @author ESFC
 * @since 2019.10.01
 * @version 3.9
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2019.10.01	ESFC            최초 생성
 * 2024.03.29   ESFC            권한별 접근 제한 수정
 * </pre>
 */
public class EgovAccessInterceptor implements HandlerInterceptor, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovAccessInterceptor.class);
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String requestUrl = request.getRequestURI().replace(request.getContextPath(),"");

        // 인증 체크
        if (!EgovUserDetailsHelper.isAuthenticated()) {
            LOGGER.debug("##### EgovAccessInterceptor Authentication not authorized... ");
            response.sendRedirect(request.getContextPath() + EgovAccessConfigShare.DEF_LOGIN_URL);
            return false;
        }

        // 권한별 접근 제한
        if (authCheck(requestUrl)) {
            LOGGER.debug("##### EgovAccessInterceptor URL pattern matched... ");
            return true;
        }

        // 허가되지 않은 경우 접근 제한
        response.sendRedirect(request.getContextPath() + EgovAccessConfigShare.DEF_ACCESS_DENIED_URL);
        return false;
    }

    public Boolean authCheck(String requestUrl) {
        boolean authCheck = false;
        List<String> authList = EgovUserDetailsHelper.getAuthorities();
        List<Map<String, Object>> rolesList = EgovUserDetailsHelper.getRoles();

        if (!ObjectUtils.isEmpty(authList) && !ObjectUtils.isEmpty(rolesList)) {
            List<String> urlList = new ArrayList<>();
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

            urlList = roleList.stream().distinct().collect(Collectors.toList());

            for (String authUrl : urlList) {
                if ("ant".equals(EgovAccessConfigShare.DEF_REQUEST_MATCH_TYPE)) {
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
