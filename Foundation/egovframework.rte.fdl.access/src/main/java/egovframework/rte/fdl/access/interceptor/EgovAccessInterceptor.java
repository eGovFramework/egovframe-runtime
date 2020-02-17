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
package egovframework.rte.fdl.access.interceptor;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import egovframework.rte.fdl.access.bean.AuthorityResourceMetadata;
import egovframework.rte.fdl.access.config.EgovAccessConfigShare;
import egovframework.rte.fdl.access.service.EgovUserDetailsHelper;

/**
 * 인증 및 접근제한 인터셉터
 *
 * <p>Desc.: 인증 및 접근제한 인터셉터</p>
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
public class EgovAccessInterceptor extends HandlerInterceptorAdapter implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovAccessInterceptor.class);
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        LOGGER.debug("##### EgovAccessInterceptor Start #####");

        // 인증 체크
        boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
        LOGGER.debug("##### EgovAccessInterceptor interceptor isAuthenticated >>> {}", isAuthenticated);
        if (!isAuthenticated) {
            response.sendRedirect(request.getContextPath() + EgovAccessConfigShare.DEF_LOGIN_URL);
            return false;
        }

        String requestMatchType = EgovAccessConfigShare.DEF_REQUEST_MATCH_TYPE;
        String url = request.getRequestURI().replace(request.getContextPath(),"");
        boolean matchStatus = false;

        // 권한 체크
        List<String> authorityList = EgovUserDetailsHelper.getAuthorities();
        LOGGER.debug("##### EgovAccessInterceptor authorityList : {} #####", authorityList);
        String authority = "";
        for (String str : authorityList) {
            authority = str;
        }

        // 권한별 접근 제한
        AuthorityResourceMetadata authorityResourceMetadata = context.getBean(AuthorityResourceMetadata.class);
        List<Map<String, Object>> list = authorityResourceMetadata.getResourceMap();
        Iterator<Map<String, Object>> iterator = list.iterator();
        Map<String, Object> tempMap;
        while (iterator.hasNext()) {
            tempMap = iterator.next();
            if (authority.equals(tempMap.get("authority"))) {
                // Ant Style Path Check
                if ("ant".equals(requestMatchType)) {
                    LOGGER.debug("##### EgovAccessInterceptor ant pattern #####");
                    matchStatus = EgovAccessUtil.antMatcher((String) tempMap.get("url"), url);
                    LOGGER.debug("##### EgovAccessInterceptor ant pattern : {} , url : {}, match : {} #####", tempMap.get("url"), url, matchStatus);
                }
                // Regular Expression Style Path Check
                else {
                    LOGGER.debug("##### EgovAccessInterceptor regex pattern #####");
                    matchStatus = EgovAccessUtil.regexMatcher((String) tempMap.get("url"), url);
                    LOGGER.debug("##### EgovAccessConfigTest regex pattern : {} , url : {}, match : {} #####", tempMap.get("url"), url, matchStatus);
                }
                if (matchStatus) {
                    return true;
                }
            }
        }

        // 허가되지 않은 경우 접근 제한
        if (!matchStatus) {
            response.sendRedirect(request.getContextPath() + EgovAccessConfigShare.DEF_ACCESS_DENIED_URL);
            return false;
        }

        return true;
    }

}
