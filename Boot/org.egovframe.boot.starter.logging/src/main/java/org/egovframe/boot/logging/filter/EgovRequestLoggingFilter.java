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
package org.egovframe.boot.logging.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 요청(method, URI, 응답 상태, 처리 시간)을 {@code org.egovframe.boot.logging.access} 로거로 기록한다.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2026.08.24   유지보수  최초 생성
 * </pre>
 * @since 2026.08.24
 */
public class EgovRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger ACCESS_LOG = LoggerFactory.getLogger("org.egovframe.boot.logging.access");

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> excludePatterns;

    public EgovRequestLoggingFilter(String excludePatterns) {
        this.excludePatterns = new ArrayList<>();
        if (StringUtils.hasText(excludePatterns)) {
            for (String pattern : excludePatterns.split(",")) {
                if (StringUtils.hasText(pattern)) {
                    this.excludePatterns.add(pattern.trim());
                }
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String pattern : excludePatterns) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            ACCESS_LOG.info("{} {} - status={} time={}ms", sanitize(request.getMethod()),
                    sanitize(request.getRequestURI()), response.getStatus(), elapsed);
        }
    }

    /** 로그 위조(CWE-117) 방지를 위해 CR/LF 를 치환한다. */
    private static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder sanitized = null;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\r' || c == '\n') {
                if (sanitized == null) {
                    sanitized = new StringBuilder(value.substring(0, i));
                }
                sanitized.append('_');
            } else if (sanitized != null) {
                sanitized.append(c);
            }
        }
        return sanitized == null ? value : sanitized.toString();
    }

}
