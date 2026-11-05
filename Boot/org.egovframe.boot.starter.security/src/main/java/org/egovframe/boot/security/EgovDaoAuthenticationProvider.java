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
package org.egovframe.boot.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 사용자 ID를 salt로 사용하는 비밀번호 검증을 수행하는 AuthenticationProvider.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2026.10.23   유지보수  최초 생성 (egovframe-rte-fdl-security 이식)
 * </pre>
 * @since 2026.10.23
 */
public class EgovDaoAuthenticationProvider extends DaoAuthenticationProvider {

    private EgovIdSaltSha256PasswordEncoder idSaltPasswordEncoder;

    public void setIdSaltPasswordEncoder(EgovIdSaltSha256PasswordEncoder idSaltPasswordEncoder) {
        this.idSaltPasswordEncoder = idSaltPasswordEncoder;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            logger.debug("Failed to authenticate since no credentials provided");
            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        String presentedPassword = authentication.getCredentials().toString();
        if (!idSaltPasswordEncoder.matches(presentedPassword, userDetails.getPassword(), userDetails.getUsername())) {
            logger.debug("Failed to authenticate since password does not match stored value");
            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }
}
