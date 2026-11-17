/*
 * Copyright 2008-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.ptl.mvc.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * HTMLTagFilter.java
 *
 * <p>요청 파라미터의 HTML 특수문자({@code < > & " '})를 엔티티로 인코딩하는 필터.
 * 초기화 파라미터 allowedTags·richTextParameters·excludePaths로 허용 태그·인코딩 우회·제외 경로를 지정한다.
 * 인코딩을 우회한 리치텍스트 값은 저장·출력 전에 화이트리스트 HTML 정제기로 정제해야 한다(XSS 방지).</p>
 *
 * @author 실행환경 개발팀 함철
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.05.30	함철				최초 생성
 * 2026.10.25	실행환경 개발팀		FilterConfig 초기화 파라미터 추가
 * </pre>
 * @since 2009.06.01
 */
public class HTMLTagFilter implements Filter {

    /** 초기화 파라미터: 허용 태그 이름(쉼표 구분, 속성 없는 형태만 통과). */
    public static final String INIT_PARAM_ALLOWED_TAGS = "allowedTags";

    /** 초기화 파라미터: 인코딩을 우회할 리치텍스트 파라미터명(쉼표 구분). */
    public static final String INIT_PARAM_RICH_TEXT_PARAMETERS = "richTextParameters";

    /** 초기화 파라미터: 제외 경로(쉼표 구분, {@code /*}로 끝나면 접두 일치). */
    public static final String INIT_PARAM_EXCLUDE_PATHS = "excludePaths";

    private Set<String> allowedTagNames = Collections.emptySet();
    private Set<String> richTextParameterNames = Collections.emptySet();
    private Set<String> excludePaths = Collections.emptySet();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (isExcludedPath(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(new HTMLTagFilterRequestWrapper(httpRequest, allowedTagNames, richTextParameterNames), response);
    }

    public void init(FilterConfig config) {
        if (config == null) {
            return;
        }
        this.allowedTagNames = parseCsv(config.getInitParameter(INIT_PARAM_ALLOWED_TAGS));
        this.richTextParameterNames = parseCsv(config.getInitParameter(INIT_PARAM_RICH_TEXT_PARAMETERS));
        this.excludePaths = parseCsv(config.getInitParameter(INIT_PARAM_EXCLUDE_PATHS));
    }

    public void destroy() {
    }

    /** 컨텍스트 상대 경로 기준으로 제외 대상인지 판정한다. */
    private boolean isExcludedPath(HttpServletRequest request) {
        if (excludePaths.isEmpty()) {
            return false;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        for (String exclude : excludePaths) {
            if (exclude.endsWith("/*")) {
                String base = exclude.substring(0, exclude.length() - 2);
                if (path.equals(base) || path.startsWith(base + "/")) {
                    return true;
                }
            } else if (path.equals(exclude)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> parseCsv(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> parsed = new LinkedHashSet<String>();
        for (String token : raw.split(",")) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                parsed.add(trimmed);
            }
        }
        return parsed;
    }

}
