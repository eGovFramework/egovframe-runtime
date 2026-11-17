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
package org.egovframe.rte.fdl.security.bean;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.egovframe.rte.fdl.security.config.EgovSecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class EgovLoginFailHandler implements AuthenticationFailureHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovLoginFailHandler.class);

    private static final String DEFAULT_LOGIN_FAILURE_URL = "/index.html";

    // 계정 열거(CWE-203/204) 방지 : 화면에는 원인 무관 일반 메시지만 노출 (실제 원인은 서버 LOGGER.debug)
    private static final String GENERIC_LOGIN_FAIL_MSG = "Invalid username or password.";

    private final EgovSecurityConfig config;

    public EgovLoginFailHandler(EgovSecurityConfig config) {
        this.config = config;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if (ObjectUtils.isEmpty(config)) {
            throw new NoSuchBeanDefinitionException("### EgovLoginFailHandler getLoginFailureUrl not found.");
        }

        String failureUrl;
        if (StringUtils.hasText(config.getLoginFailureUrl())) {
            failureUrl = config.getLoginFailureUrl();
        } else {
            failureUrl = DEFAULT_LOGIN_FAILURE_URL;
        }

        if (exception instanceof AuthenticationServiceException) {
            LOGGER.debug("### EgovLoginFailHandler onAuthenticationFailure : The user doesn't exist.");
        } else if (exception instanceof BadCredentialsException) {
            LOGGER.debug("### EgovLoginFailHandler onAuthenticationFailure : Invalid username or password.");
        } else if (exception instanceof LockedException) {
            LOGGER.debug("### EgovLoginFailHandler onAuthenticationFailure : This account is locked.");
        } else if (exception instanceof DisabledException) {
            LOGGER.debug("### EgovLoginFailHandler onAuthenticationFailure : This account is disabled.");
        } else if (exception instanceof AccountExpiredException) {
            LOGGER.debug("### EgovLoginFailHandler onAuthenticationFailure : The account is expired.");
        } else if (exception instanceof CredentialsExpiredException) {
            LOGGER.debug("### EgovLoginFailHandler onAuthenticationFailure : The password has expired.");
        } else {
            LOGGER.debug("### EgovLoginFailHandler onAuthenticationFailure : {}", exception.getClass().getSimpleName());
        }
        // 원인 무관 동일 일반 메시지 노출 (계정 열거 방지)
        request.setAttribute("loginFailMsg", GENERIC_LOGIN_FAIL_MSG);

        RequestDispatcher dispatcher = request.getRequestDispatcher(failureUrl);
        dispatcher.forward(request, response);
    }

}
