/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.security.config;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * 여러 권한을 OR 조건으로 평가하는 커스텀 AuthorizationManager
 *
 * <p>Desc.: 동일한 URL에 대해 여러 권한이 매핑된 경우 OR 조건으로 평가하여
 * 사용자가 해당 권한 중 하나라도 가지고 있으면 접근을 허용함
 * 권한계층구조가 설정된 경우 상위 권한을 가진 사용자도 하위 권한이 필요한 리소스에 접근할 수 있음</p>
 *
 * @author 유지보수
 * @version 5.0
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
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();

        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        Collection<? extends GrantedAuthority> userAuthorities = auth.getAuthorities();

        // 권한계층구조가 설정된 경우 도달 가능한 모든 권한을 계산
        Collection<? extends GrantedAuthority> reachableAuthorities = userAuthorities;
        if (roleHierarchy != null) {
            reachableAuthorities = roleHierarchy.getReachableGrantedAuthorities(userAuthorities);
        }

        // 사용자가 가진 권한(계층구조 포함) 중 하나라도 요구되는 권한과 일치하면 허용 (OR 조건)
        for (GrantedAuthority userAuth : reachableAuthorities) {
            if (authorities.contains(userAuth.getAuthority())) {
                return new AuthorizationDecision(true);
            }
        }

        return new AuthorizationDecision(false);
    }

}
