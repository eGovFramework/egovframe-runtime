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
package org.egovframe.boot.security.bean;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.egovframe.boot.security.EgovSecurityProperties;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 접근 거부 시 accessDeniedUrl(CSRF 오류는 csrfAccessDeniedUrl)로 forward 하는 핸들러.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  목적지를 하나로 정해 forward 1회 수행, 기본 URL 적용
 * </pre>
 * @since 2025.06.01
 */
public class EgovAccessDeniedHandler implements AccessDeniedHandler {

    private static final String DEFAULT_ACCESS_DENIED_URL = "/index.html";

    private final EgovSecurityProperties properties;

    public EgovAccessDeniedHandler(EgovSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (ObjectUtils.isEmpty(properties)) {
            throw new NoSuchBeanDefinitionException("### EgovAccessDeniedHandler EgovSecurityProperties not found.");
        }

        if (ObjectUtils.isEmpty(accessDeniedException)) {
            return;
        }

        // 목적지를 하나로 정해 forward 는 한 번만 수행(두 번째 forward 는 커밋된 응답에서 IllegalStateException)
        boolean isCsrfFailure = accessDeniedException instanceof InvalidCsrfTokenException
                || accessDeniedException instanceof MissingCsrfTokenException;
        String targetUrl = isCsrfFailure ? properties.getCsrfAccessDeniedUrl() : properties.getAccessDeniedUrl();
        if (!StringUtils.hasText(targetUrl)) {
            targetUrl = DEFAULT_ACCESS_DENIED_URL;
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(targetUrl);
        dispatcher.forward(request, response);
    }

}
