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
 * 로그인 실패 처리기 — 실패 원인과 무관하게 동일한 예외(메시지)만 화면에 전달한다.
 *
 * <p>기본 {@link SimpleUrlAuthenticationFailureHandler}는 실제 실패 예외(계정 없음·잠김·비활성·만료 등)를
 * 세션의 {@code SPRING_SECURITY_LAST_EXCEPTION}에 그대로 저장하므로, 화면이 그 메시지를 출력하면 계정 존재
 * 여부와 상태가 드러난다(계정 열거, CWE-203/204). 이 처리기는 이동 방식(redirect/forward)과 실패 URL 설정은
 * 그대로 두고, 저장되는 예외만 일반 메시지의 {@link BadCredentialsException}으로 바꾼다.
 * 실제 원인은 서버 debug 로그로만 남긴다.</p>
 *
 * @author 실행환경 개발팀
 * @since 2026.10.25
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.10.25	실행환경 개발팀		최초 생성 (5.1.0 EgovLoginFailHandler 의 계정 열거 방지 정책 반영)
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
     * 화면에 전달할 일반 예외. 메시지는 Spring Security 메시지 번들의 자격 증명 실패 문구를 쓴다.
     */
    protected AuthenticationException genericException() {
        return new BadCredentialsException(
                messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
    }

}
