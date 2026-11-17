/*
 * Copyright 2008-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.security.intercept;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 실패 원인과 무관하게 동일한 예외(메시지)만 화면에 전달하는 로그인 실패 처리 클래스.
 * 세션에는 일반 메시지의 {@link BadCredentialsException}만 저장하고 실제 원인은 debug 로그로 남긴다(계정 열거 CWE-203/204 방지).
 *
 * @author 실행환경 개발팀
 * @since 2026.10.25
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.10.25	실행환경 개발팀		최초 생성 (5.1.0 기능 이식)
 * </pre>
 */
public class EgovLoginFailHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovLoginFailHandler.class);

    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    public EgovLoginFailHandler() {
        super();
    }

    public EgovLoginFailHandler(String defaultFailureUrl) {
        super(defaultFailureUrl);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        LOGGER.debug("### EgovLoginFailHandler onAuthenticationFailure : {}", exception.getClass().getSimpleName());
        super.onAuthenticationFailure(request, response, genericException());
    }

    /**
     * 화면에 전달할 일반 예외(Spring Security 자격 증명 실패 메시지)를 반환한다.
     */
    protected AuthenticationException genericException() {
        return new BadCredentialsException(
                messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
    }

}
