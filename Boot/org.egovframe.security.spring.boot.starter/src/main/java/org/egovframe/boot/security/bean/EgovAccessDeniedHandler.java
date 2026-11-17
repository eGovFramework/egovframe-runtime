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
package org.egovframe.boot.security.bean;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class EgovAccessDeniedHandler implements AccessDeniedHandler {

    private final String accessDeniedUrl;
    private final String csrfAccessDeniedUrl;

    public EgovAccessDeniedHandler(String accessDeniedUrl, String csrfAccessDeniedUrl) {
        this.accessDeniedUrl = accessDeniedUrl;
        this.csrfAccessDeniedUrl = csrfAccessDeniedUrl;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (accessDeniedException != null) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(accessDeniedUrl);
            dispatcher.forward(request, response);
        }

        if (accessDeniedException instanceof InvalidCsrfTokenException) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(csrfAccessDeniedUrl);
            dispatcher.forward(request, response);
        }

        if (accessDeniedException instanceof MissingCsrfTokenException) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(csrfAccessDeniedUrl);
            dispatcher.forward(request, response);
        }
    }

}
