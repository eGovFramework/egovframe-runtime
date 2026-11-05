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

import jakarta.servlet.http.HttpServletRequest;
import org.egovframe.boot.security.bean.EgovReloadableFilterInvocationSecurityMetadataSource;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * DB 보호자원-권한 매핑을 요청 시점에 조회해 판단하는 AuthorizationManager
 * (egovframe-rte-fdl-security 의 FilterSecurityInterceptor + RoleVoter/AuthenticatedVoter 판정과 동일).
 * <ul>
 *     <li>매핑 있음 : ROLE_* 는 보유 권한(계층 포함), IS_AUTHENTICATED_* 는 인증 수준과 비교해 하나라도 맞으면 허용</li>
 *     <li>매핑 없음 : permitAll 목록은 허용, 나머지는 인증(익명 제외) 필요</li>
 * </ul>
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2026.10.23   유지보수  최초 생성 (egovframe-rte-fdl-security 의 FilterSecurityInterceptor 방식 이식)
 * </pre>
 * @since 2026.10.23
 */
public class EgovDatabaseAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String IS_AUTHENTICATED_FULLY = "IS_AUTHENTICATED_FULLY";
    private static final String IS_AUTHENTICATED_REMEMBERED = "IS_AUTHENTICATED_REMEMBERED";
    private static final String IS_AUTHENTICATED_ANONYMOUSLY = "IS_AUTHENTICATED_ANONYMOUSLY";

    private final EgovReloadableFilterInvocationSecurityMetadataSource metadataSource;
    private final RoleHierarchy roleHierarchy;
    private final List<RequestMatcher> permitAll;
    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    public EgovDatabaseAuthorizationManager(EgovReloadableFilterInvocationSecurityMetadataSource metadataSource,
                                            RoleHierarchy roleHierarchy,
                                            List<RequestMatcher> permitAll) {
        this.metadataSource = metadataSource;
        this.roleHierarchy = roleHierarchy;
        this.permitAll = List.copyOf(permitAll);
    }

    @Override
    public AuthorizationResult authorize(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Collection<ConfigAttribute> attributes = metadataSource.getAttributes(context.getRequest());
        if (attributes == null || attributes.isEmpty()) {
            return new AuthorizationDecision(isPermitAll(context.getRequest()) || trustResolver.isAuthenticated(authentication.get()));
        }
        return new AuthorizationDecision(isGranted(authentication.get(), attributes));
    }

    private boolean isPermitAll(HttpServletRequest request) {
        for (RequestMatcher matcher : permitAll) {
            if (matcher.matches(request)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Spring Security 6.5 의 추상 메서드라 유지하며 authorize 와 같은 판정을 반환한다.
     *
     * @deprecated {@link #authorize(Supplier, RequestAuthorizationContext)} 를 사용
     */
    @Deprecated
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        return (AuthorizationDecision) authorize(authentication, context);
    }

    private boolean isGranted(Authentication auth, Collection<ConfigAttribute> attributes) {
        if (auth == null) {
            return false;
        }
        Set<String> authorities = reachableAuthorities(auth);
        for (ConfigAttribute attribute : attributes) {
            String value = attribute.getAttribute();
            if (value == null) {
                continue;
            }
            // RoleVoter
            if (value.startsWith(ROLE_PREFIX) && authorities.contains(value)) {
                return true;
            }
            // AuthenticatedVoter
            boolean fully = trustResolver.isFullyAuthenticated(auth);
            boolean rememberMe = trustResolver.isRememberMe(auth);
            if (IS_AUTHENTICATED_FULLY.equals(value) && fully) {
                return true;
            }
            if (IS_AUTHENTICATED_REMEMBERED.equals(value) && (rememberMe || fully)) {
                return true;
            }
            if (IS_AUTHENTICATED_ANONYMOUSLY.equals(value) && (trustResolver.isAnonymous(auth) || fully || rememberMe)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> reachableAuthorities(Authentication auth) {
        Collection<? extends GrantedAuthority> granted = auth.getAuthorities();
        if (roleHierarchy != null) {
            granted = roleHierarchy.getReachableGrantedAuthorities(granted);
        }
        return granted.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

}
