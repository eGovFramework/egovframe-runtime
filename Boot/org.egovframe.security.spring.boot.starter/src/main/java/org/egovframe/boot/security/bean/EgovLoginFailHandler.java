package org.egovframe.boot.security.bean;

import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.security.EgovSecurityProperties;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class EgovLoginFailHandler implements AuthenticationFailureHandler {

    private static final String DEFAULT_LOGIN_FAILURE_URL = "/index.html";

    private final EgovSecurityProperties properties;

    public EgovLoginFailHandler(EgovSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if (properties == null) {
            throw new NoSuchBeanDefinitionException("### EgovLoginFailHandler EgovSecurityProperties is not defined");
        }

        String failureUrl;
        if (StringUtils.hasText(properties.getAccessDeniedUrl())) {
            failureUrl = properties.getAccessDeniedUrl();
        } else {
            failureUrl = DEFAULT_LOGIN_FAILURE_URL;
        }

        if (exception instanceof AuthenticationServiceException) {
            log.debug("##### EgovLoginFailHandler onAuthenticationFailure >>> The user doesn't exist.");
            request.setAttribute("loginFailMsg", "The user doesn't exist.");
        } else if (exception instanceof BadCredentialsException) {
            log.debug("##### EgovLoginFailHandler onAuthenticationFailure >>> Invalid username or password.");
            request.setAttribute("loginFailMsg", "Invalid username or password.");
        } else if (exception instanceof LockedException) {
            log.debug("##### EgovLoginFailHandler onAuthenticationFailure >>> This account is locked.");
            request.setAttribute("loginFailMsg", "This account is locked.");
        } else if (exception instanceof DisabledException) {
            log.debug("##### EgovLoginFailHandler onAuthenticationFailure >>> This account is disabled.");
            request.setAttribute("loginFailMsg", "This account is disabled.");
        } else if (exception instanceof AccountExpiredException) {
            log.debug("##### EgovLoginFailHandler onAuthenticationFailure >>> The account is expired.");
            request.setAttribute("loginFailMsg", "The account is expired.");
        } else if (exception instanceof CredentialsExpiredException) {
            log.debug("##### EgovLoginFailHandler onAuthenticationFailure >>> The password has expired.");
            request.setAttribute("loginFailMsg", "The password has expired.");
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(failureUrl);
        dispatcher.forward(request, response);
    }

}
