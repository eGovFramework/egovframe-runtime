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
package org.egovframe.boot.security.userdetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Arrays;
import java.util.Collection;

/**
 * 사용자 VO 를 함께 보관하는 인증 사용자 정보 클래스.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * </pre>
 * @since 2025.06.01
 */
public class EgovUserDetails extends User {

    /** Generated Serial Version ID */
    private static final long serialVersionUID = 2517403097977414969L;

    private Object egovVO;

    /** 계정 상태(만료·잠금 등)를 모두 지정하는 생성자 */
    public EgovUserDetails(String username, String password, boolean enabled,
                           boolean accountNonExpired, boolean credentialsNonExpired,
                           boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities,
                           Object egovVO) throws IllegalArgumentException {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.egovVO = egovVO;
    }

    /** 계정 상태는 정상, 권한은 임시값(HOLDER)으로 두는 생성자 */
    public EgovUserDetails(String username, String password, boolean enabled, Object egovVO) throws IllegalArgumentException {
        this(username, password, enabled, true, true, true,
                Arrays.asList(new SimpleGrantedAuthority("HOLDER")), egovVO);
    }

    /** 사용자 VO */
    public Object getEgovUserVO() {
        return egovVO;
    }

    /** 사용자 VO 지정 */
    public void setEgovUserVO(Object egovVO) {
        this.egovVO = egovVO;
    }

}
