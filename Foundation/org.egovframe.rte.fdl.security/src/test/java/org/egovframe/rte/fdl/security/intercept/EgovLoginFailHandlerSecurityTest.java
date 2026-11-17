package org.egovframe.rte.fdl.security.intercept;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 로그인 실패 원인과 무관하게 같은 예외(메시지)가 화면에 전달되는지 검증한다(계정 열거 방지).
 */
public class EgovLoginFailHandlerSecurityTest {

    @Test
    public void 실패_원인이_달라도_세션에_남는_예외는_같다() throws Exception {
        String expected = null;
        for (AuthenticationException cause : new AuthenticationException[]{
                new UsernameNotFoundException("no such user: admin"),
                new LockedException("User account is locked"),
                new DisabledException("User is disabled"),
                new BadCredentialsException("Bad credentials")}) {
            Object saved = fail(cause);
            assertTrue(saved instanceof BadCredentialsException);
            String message = ((BadCredentialsException) saved).getMessage();
            if (expected == null) {
                expected = message;
            }
            assertEquals(cause.getClass().getSimpleName(), expected, message);
        }
    }

    @Test
    public void 실패_URL_로_이동한다() throws Exception {
        EgovLoginFailHandler handler = new EgovLoginFailHandler("/login?error");
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationFailure(new MockHttpServletRequest(), response, new LockedException("locked"));
        assertEquals("/login?error", response.getRedirectedUrl());
    }

    private static Object fail(AuthenticationException cause) throws Exception {
        EgovLoginFailHandler handler = new EgovLoginFailHandler("/login?error");
        MockHttpServletRequest request = new MockHttpServletRequest();
        handler.onAuthenticationFailure(request, new MockHttpServletResponse(), cause);
        return request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

}
