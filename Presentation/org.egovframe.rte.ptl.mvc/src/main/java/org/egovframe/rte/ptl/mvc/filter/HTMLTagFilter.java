/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * HTMLTagFilter.java
 *
 * <p>요청 파라미터의 HTML 특수문자를 엔티티로 인코딩하는 필터. 초기화 파라미터 없이 등록하면
 * 종전과 완전히 같은 동작(모든 {@code < > & " '} 인코딩)이며, 아래 초기화 파라미터로
 * 허용 태그·리치텍스트 우회·제외 경로를 지정할 수 있다.</p>
 *
 * <pre>
 * &lt;filter&gt;
 *     &lt;filter-name&gt;htmlTagFilter&lt;/filter-name&gt;
 *     &lt;filter-class&gt;org.egovframe.rte.ptl.mvc.filter.HTMLTagFilter&lt;/filter-class&gt;
 *     &lt;init-param&gt;&lt;param-name&gt;allowedTags&lt;/param-name&gt;&lt;param-value&gt;p, br&lt;/param-value&gt;&lt;/init-param&gt;
 *     &lt;init-param&gt;&lt;param-name&gt;richTextParameters&lt;/param-name&gt;&lt;param-value&gt;nttCn&lt;/param-value&gt;&lt;/init-param&gt;
 *     &lt;init-param&gt;&lt;param-name&gt;excludePaths&lt;/param-name&gt;&lt;param-value&gt;/api/*&lt;/param-value&gt;&lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 *
 * <p><b>리치텍스트 우회 파라미터는 출력 시점 정제가 전제다.</b> 우회한 값은 저장·출력 전에
 * 반드시 {@link EgovHtmlSanitizer} 로 정제한다 — 우회만 하고 정제하지 않으면 XSS 무방비가 된다.</p>
 *
 * @author 실행환경 개발팀 함철
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.05.30	함철				최초 생성
 * 2026.09.01	실행환경 개발팀		FilterConfig 초기화 파라미터 승격 —
 *                              allowedTags·richTextParameters·excludePaths
 * </pre>
 * @since 2009.06.01
 */
public class HTMLTagFilter implements Filter {

    /** 초기화 파라미터 — 쉼표 구분 허용 태그 이름(예: {@code p, br}). 속성 없는 형태만 통과한다. */
    public static final String INIT_PARAM_ALLOWED_TAGS = "allowedTags";

    /** 초기화 파라미터 — 쉼표 구분 리치텍스트 파라미터명. 해당 파라미터는 인코딩을 우회한다. */
    public static final String INIT_PARAM_RICH_TEXT_PARAMETERS = "richTextParameters";

    /** 초기화 파라미터 — 쉼표 구분 제외 경로. {@code /*} 로 끝나면 접두 일치, 아니면 정확 일치. */
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
