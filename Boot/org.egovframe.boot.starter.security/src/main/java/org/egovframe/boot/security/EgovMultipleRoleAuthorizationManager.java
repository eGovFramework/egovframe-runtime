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

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * 여러 권한 중 하나라도 보유(권한계층 포함)하면 허용하는 AuthorizationManager.
 * <p>기본 설정에서는 사용하지 않으며 사용자 정의 인가 규칙용으로 제공한다.</p>
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  authorize() 구현, check() 는 위임으로 유지
 * </pre>
 * @since 2025.06.01
 */
public class EgovMultipleRoleAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final Collection<String> authorities;
    private final RoleHierarchy roleHierarchy;

    public EgovMultipleRoleAuthorizationManager(Collection<String> authorities) {
        this.authorities = authorities;
        this.roleHierarchy = null;
    }

    public EgovMultipleRoleAuthorizationManager(Collection<String> authorities, RoleHierarchy roleHierarchy) {
        this.authorities = authorities;
        this.roleHierarchy = roleHierarchy;
    }

    @Override
    public AuthorizationResult authorize(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        return decide(authentication.get());
    }

    /**
     * Spring Security 6.5 의 추상 메서드이며 RequestMatcherDelegatingAuthorizationManager 가 호출하므로 authorize 와 같은 판정을 반환한다.
     *
     * @deprecated {@link #authorize(Supplier, RequestAuthorizationContext)} 를 사용
     */
    @Deprecated
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        return decide(authentication.get());
    }

    private AuthorizationDecision decide(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        Collection<? extends GrantedAuthority> userAuthorities = auth.getAuthorities();
        
        // 권한계층 반영
        Collection<? extends GrantedAuthority> reachableAuthorities = userAuthorities;
        if (roleHierarchy != null) {
            reachableAuthorities = roleHierarchy.getReachableGrantedAuthorities(userAuthorities);
        }

        // 하나라도 일치하면 허용(OR)
        for (GrantedAuthority userAuth : reachableAuthorities) {
            if (authorities.contains(userAuth.getAuthority())) {
                return new AuthorizationDecision(true);
            }
        }

        return new AuthorizationDecision(false);
    }

}

