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
package org.egovframe.rte.fdl.security.bean;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.egovframe.rte.fdl.security.config.EgovSecurityConfig;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.util.ObjectUtils;

import java.io.IOException;

public class EgovAccessDeniedHandler implements AccessDeniedHandler {

    private final EgovSecurityConfig config;

    public EgovAccessDeniedHandler(EgovSecurityConfig config) {
        this.config = config;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (ObjectUtils.isEmpty(config)) {
            throw new NoSuchBeanDefinitionException("### EgovAccessDeniedHandler EgovSecurityProperties not found.");
        }

        if (!ObjectUtils.isEmpty(accessDeniedException)) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(config.getAccessDeniedUrl());
            dispatcher.forward(request, response);
        }

        if (accessDeniedException instanceof InvalidCsrfTokenException) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(config.getCsrfAccessDeniedUrl());
            dispatcher.forward(request, response);
        }

        if (accessDeniedException instanceof MissingCsrfTokenException) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(config.getCsrfAccessDeniedUrl());
            dispatcher.forward(request, response);
        }
    }

}
