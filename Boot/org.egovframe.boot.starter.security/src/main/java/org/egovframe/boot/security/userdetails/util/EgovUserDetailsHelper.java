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
package org.egovframe.boot.security.userdetails.util;

import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.security.EgovSecurityMetadataCache;
import org.egovframe.boot.security.bean.EgovReloadableFilterInvocationSecurityMetadataSource;
import org.egovframe.boot.security.secureobject.impl.SelfRegexRequestMatcher;
import org.egovframe.boot.security.userdetails.EgovUserDetails;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * 인증 사용자 · 권한 · 보호자원 정보를 조회하는 헬퍼.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  PathPatternRequestMatcher 패턴 추출, 기본 해시 BCrypt
 * </pre>
 * @since 2025.06.01
 */
@Slf4j
public final class EgovUserDetailsHelper {

    private static final String PATH_PATTERN_PREFIX = "PathPattern [";

    /** 인증 사용자 VO(EgovUserDetails 가 아니면 principal) */
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

    /** 인증 객체의 권한 목록(로그인 시 권한계층 반영), 미인증 시 null. 예) [ROLE_ADMIN, ROLE_USER, ...] */
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

    /** 인증 사용자 여부(익명 제외) */
    public static Boolean isAuthenticated() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (ObjectUtils.isEmpty(authentication)) {
            log.debug("## EgovUserDetailsHelper.isAuthenticated object is null!!");
            return Boolean.FALSE;
        }
        String username = authentication.getName();
        if (username.equals("anonymousUser")) {
            log.debug("## EgovUserDetailsHelper.isAuthenticated >>> username is {}", username);
            return Boolean.FALSE;
        }
        Object principal = authentication.getPrincipal();
        return (!ObjectUtils.isEmpty(principal));
    }

    /** 기본 인코더(BCrypt)로 검증되는 비밀번호 해시 */
    public static String getHashedPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    /** (권한, URL 패턴) 목록(기동 시점 보호자원 기준) */
    public static List<Map.Entry<String, String>> getRoleAndPatternList() {
        Map<RequestMatcher, List<ConfigAttribute>> map = EgovSecurityMetadataCache.getRequestMap();
        if (map == null || map.isEmpty()) {
            log.warn("## getRoleAndPatternList: requestMap is empty or not initialized!");
            return Collections.emptyList();
        }

        List<Map.Entry<String, String>> result = new ArrayList<>();

        for (Map.Entry<RequestMatcher, List<ConfigAttribute>> entry : map.entrySet()) {
            String pattern = extractPatternFromMatcher(entry.getKey());
            List<ConfigAttribute> roles = entry.getValue();

            for (ConfigAttribute attr : roles) {
                result.add(new AbstractMap.SimpleEntry<>(attr.getAttribute(), pattern));
            }
        }

        return result;
    }

    /** 주어진 권한에 허용된 URL 패턴(쉼표 구분) */
    public static String getAccessiblePatterns(List<Map.Entry<String, String>> rolePatternList, List<String> authorList) {
        if (rolePatternList == null || authorList == null) {
            return "";
        }

        Set<String> patternSet = new LinkedHashSet<>();

        for (Map.Entry<String, String> entry : rolePatternList) {
            String role = entry.getKey();
            String pattern = entry.getValue();

            if (authorList.contains(role)) {
                patternSet.add(pattern);
            }
        }

        return String.join(",", patternSet);
    }

    private static String extractPatternFromMatcher(RequestMatcher matcher) {
        if (matcher instanceof SelfRegexRequestMatcher selfRegexRequestMatcher) {
            return selfRegexRequestMatcher.getPattern();
        }

        String desc = matcher.toString();

        // PathPatternRequestMatcher 는 패턴 getter 가 없어 toString "PathPattern [GET /path]" 에서 추출
        if (matcher instanceof PathPatternRequestMatcher && desc.startsWith(PATH_PATTERN_PREFIX) && desc.endsWith("]")) {
            String pattern = desc.substring(PATH_PATTERN_PREFIX.length(), desc.length() - 1);
            int space = pattern.indexOf(' ');
            return (space > 0 && !pattern.startsWith("/")) ? pattern.substring(space + 1) : pattern;
        }

        int start = desc.indexOf("pattern='");
        if (start != -1) {
            start += "pattern='".length();
            int end = desc.indexOf("'", start);
            if (end != -1) {
                return desc.substring(start, end);
            }
        }

        return desc;
    }

    /** 보호자원 메타데이터 재적재 */
    public static void reloadSecurityMetadata(EgovReloadableFilterInvocationSecurityMetadataSource source) {
        if (source == null) {
            log.warn("## reloadSecurityMetadata: source is null!");
            return;
        }

        try {
            source.reload();
        } catch (Exception e) {
            log.error("## reloadSecurityMetadata: Error during reloading security metadata", e);
        }
    }

}
