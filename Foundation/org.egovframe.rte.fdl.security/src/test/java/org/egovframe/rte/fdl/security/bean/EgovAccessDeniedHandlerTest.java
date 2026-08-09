package org.egovframe.rte.fdl.security.bean;

import org.egovframe.rte.fdl.security.config.EgovSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EgovAccessDeniedHandlerTest {

    @Test
    public void handle_fallsBackToDefaultUrl_whenCsrfAccessDeniedUrlBlank() throws Exception {
        EgovSecurityConfig config = new EgovSecurityConfig();
        config.setAccessDeniedUrl("/system/accessDenied.do");
        // csrfAccessDeniedUrl은 의도적으로 미설정 상태(null)로 둔다.

        EgovAccessDeniedHandler handler = new EgovAccessDeniedHandler(config);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertDoesNotThrow(() -> handler.handle(request, response, new MissingCsrfTokenException("csrf-token")));
        assertEquals("/index.html", response.getForwardedUrl());
    }

    @Test
    public void handle_fallsBackToDefaultUrl_whenAccessDeniedUrlBlank() throws Exception {
        EgovSecurityConfig config = new EgovSecurityConfig();
        config.setAccessDeniedUrl(" ");
        // 일반 접근거부 분기(CSRF 아님)도 동일 가드를 타는지 확인한다.

        EgovAccessDeniedHandler handler = new EgovAccessDeniedHandler(config);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertDoesNotThrow(() -> handler.handle(request, response, new AccessDeniedException("denied")));
        assertEquals("/index.html", response.getForwardedUrl());
    }

    @Test
    public void handle_usesConfiguredCsrfUrl_whenPresent() throws Exception {
        EgovSecurityConfig config = new EgovSecurityConfig();
        config.setAccessDeniedUrl("/system/accessDenied.do");
        config.setCsrfAccessDeniedUrl("/system/csrfDenied.do");

        EgovAccessDeniedHandler handler = new EgovAccessDeniedHandler(config);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new MissingCsrfTokenException("csrf-token"));

        assertEquals("/system/csrfDenied.do", response.getForwardedUrl());
    }

}
