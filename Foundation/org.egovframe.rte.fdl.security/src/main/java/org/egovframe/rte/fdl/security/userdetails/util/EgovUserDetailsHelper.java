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
package org.egovframe.rte.fdl.security.userdetails.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.egovframe.rte.fdl.security.userdetails.EgovUserDetails;
import org.egovframe.rte.fdl.string.EgovObjectUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;

/**
 * 사용자 계정 정보를 처리하는 유틸 클래스
 *
 * <p><b>NOTE:</b>사용자 계정 정보와 권한정보를 조회할 수 있는 유틸 클래스</p>
 * 
 * @author 실행환경 개발팀 윤성종
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01   윤성종             최초 생성
 * 2014.01.22   한성곤             Spring Security 3.2.X 업그레이드 적용
 * </pre>
 */
public final class EgovUserDetailsHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovUserDetailsHelper.class);
	
	private EgovUserDetailsHelper() {
	}
	
    /**
     * 인증된 사용자객체를 VO형식으로 가져온다.
     * 
     * @return 사용자 ValueObject
     */
    public static Object getAuthenticatedUser() {
        Authentication authentication = getAuthentication();
        if (authentication == null) return null;
        if (authentication.getPrincipal() instanceof EgovUserDetails) {
        	EgovUserDetails details = (EgovUserDetails) authentication.getPrincipal();
        	LOGGER.debug("## EgovUserDetailsHelper.getAuthenticatedUser : AuthenticatedUser is {}", details.getUsername());
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
     * 
     * @return 사용자 권한정보 목록
     */
    public static List<String> getAuthorities() {
        List<String> listAuth = new ArrayList<String>();
        Authentication authentication = getAuthentication();
        if (authentication == null) return null;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();
		while (iter.hasNext()) {
        	GrantedAuthority auth = iter.next();
        	listAuth.add(auth.getAuthority());
        	LOGGER.debug("## EgovUserDetailsHelper.getAuthorities : Authority is {}", auth.getAuthority());
		}
        return listAuth;
    }

    private static Authentication getAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (EgovObjectUtil.isNull(authentication)) {
            LOGGER.debug("## authentication object is null!!");
            return null;
        }
        return authentication;
    }

    /**
     * 인증된 사용자 여부를 체크한다.
     * 
     * @return 인증된 사용자 여부(TRUE / FALSE)
     */
    public static Boolean isAuthenticated() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (EgovObjectUtil.isNull(authentication)) {
        	LOGGER.debug("## authentication object is null!!");
            return Boolean.FALSE;
        }
        String username = authentication.getName();
        if (username.equals("anonymousUser")) {		// 기존 2.0.8의 경우 'roleAnonymous'
        	LOGGER.debug("## username is {}", username);
            return Boolean.FALSE;
        }
        Object principal = authentication.getPrincipal();
        return (Boolean.valueOf(!EgovObjectUtil.isNull(principal)));
    }

    /**
     * 기본 algorithmd(SHA-256)에 대한 패스워드 얻기.
     * 
     * @param password
     * @return
     */
    public static String getHashedPassword(String password) {
        DelegatingPasswordEncoder delegatingPasswordEncoder = (DelegatingPasswordEncoder)PasswordEncoderFactories.createDelegatingPasswordEncoder();
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(new MessageDigestPasswordEncoder("SHA-256"));
        String hashed = delegatingPasswordEncoder.encode(password);
        return hashed;
    }

}
