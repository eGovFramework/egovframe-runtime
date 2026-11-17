package org.egovframe.rte.fdl.security.bean;

import org.egovframe.rte.fdl.security.config.EgovSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EgovLoginFailHandlerTest {

    @Test
    public void onAuthenticationFailure_usesLoginFailureUrl() throws Exception {
        EgovSecurityConfig config = new EgovSecurityConfig();
        config.setLoginFailureUrl("/uat/uia/loginFail.do");
        // 접근거부 URL 과 로그인 실패 URL 을 구분해 이동 대상 확인
        config.setAccessDeniedUrl("/system/accessDenied.do");

        EgovLoginFailHandler handler = new EgovLoginFailHandler(config);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(request, response, new BadCredentialsException("bad"));

        assertEquals("/uat/uia/loginFail.do", response.getForwardedUrl());
    }

    @Test
    public void onAuthenticationFailure_fallsBackToDefaultUrl_whenLoginFailureUrlBlank() throws Exception {
        EgovSecurityConfig config = new EgovSecurityConfig();
        config.setLoginFailureUrl(" ");
        config.setAccessDeniedUrl("/system/accessDenied.do");

        EgovLoginFailHandler handler = new EgovLoginFailHandler(config);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertDoesNotThrow(() -> handler.onAuthenticationFailure(request, response, new BadCredentialsException("bad")));
        assertEquals("/index.html", response.getForwardedUrl());
    }

}
