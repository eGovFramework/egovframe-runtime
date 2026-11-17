/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package org.egovframe.boot.security.userdetails.util;

import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.security.userdetails.EgovUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public final class EgovUserDetailsHelper {

    /**
     * 인증된 사용자객체를 VO형식으로 가져온다.
     */
    public static Object getAuthenticatedUser() {
        Authentication authentication = getAuthentication();
        if (authentication == null) return null;
        if (authentication.getPrincipal() instanceof EgovUserDetails) {
        	EgovUserDetails details = (EgovUserDetails) authentication.getPrincipal();
	        return details.getEgovUserVO();
        } else {
        	return authentication.getPrincipal();
        }
    }

    /**
     * 인증된 사용자의 권한 정보를 가져온다.
     * 예) [ROLE_ADMIN, ROLE_USER,
     * ROLE_A, ROLE_B, ROLE_RESTRICTED,
     * IS_AUTHENTICATED_FULLY,
     * IS_AUTHENTICATED_REMEMBERED,
     * IS_AUTHENTICATED_ANONYMOUSLY]
     */
    public static List<String> getAuthorities() {
        List<String> listAuth = new ArrayList<String>();
        Authentication authentication = getAuthentication();
        if (authentication == null) return null;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority auth : authorities) {
            listAuth.add(auth.getAuthority());
        }
        return listAuth;
    }

    private static Authentication getAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (ObjectUtils.isEmpty(authentication)) {
            return null;
        }
        return authentication;
    }

    /**
     * 인증된 사용자 여부를 체크한다.
     */
    public static Boolean isAuthenticated() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (ObjectUtils.isEmpty(authentication)) {
        	log.debug("## EgovUserDetailsHelper.isAuthenticated object is null!!");
            return Boolean.FALSE;
        }
        String username = authentication.getName();
        if (username.equals("anonymousUser")) {		// 기존 2.0.8의 경우 'roleAnonymous'
            log.debug("## EgovUserDetailsHelper.isAuthenticated >>> username is {}", username);
            return Boolean.FALSE;
        }
        Object principal = authentication.getPrincipal();
        return (!ObjectUtils.isEmpty(principal));
    }

    /**
     * 기본 algorithmd(SHA-256)에 대한 패스워드 얻기.
     */
    public static String getHashedPassword(String password) {
        DelegatingPasswordEncoder passwordEncoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        passwordEncoder.setDefaultPasswordEncoderForMatches(new MessageDigestPasswordEncoder("SHA-256"));
        return passwordEncoder.encode(password);
    }

}
