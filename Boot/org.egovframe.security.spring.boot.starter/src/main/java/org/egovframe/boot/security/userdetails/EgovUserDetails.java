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
package org.egovframe.boot.security.userdetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Arrays;
import java.util.Collection;

public class EgovUserDetails extends User {

    /**
	 * Generated Serial Version ID
	 */
	private static final long serialVersionUID = 2517403097977414969L;

	private Object egovVO;

    /**
     * User 클래스의 생성자 Override
     * 
     * @param username 사용자계정
     * @param password 사용자 패스워드
     * @param enabled 사용자계정 사용여부
     * @param accountNonExpired
     * @param credentialsNonExpired
     * @param accountNonLocked
     * @param authorities
     * @param egovVO 사용자 VO객체
     */
    public EgovUserDetails(String username, String password, boolean enabled,
            boolean accountNonExpired, boolean credentialsNonExpired,
            boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities,
            Object egovVO) throws IllegalArgumentException {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.egovVO = egovVO;
    }

    /**
     * EgovUserDetails 생성자
     */
    public EgovUserDetails(String username, String password, boolean enabled, Object egovVO) throws IllegalArgumentException {
    	this(username, password, enabled, true, true, true,
    			Arrays.asList(new SimpleGrantedAuthority("HOLDER")), egovVO);
    }

    /**
     * 인증 객체 얻기.
     */
    public Object getEgovUserVO() {
        return egovVO;
    }

    /**
     * 인증 객체 지정.
     * 
     * @param egovVO 사용자VO객체
     */
    public void setEgovUserVO(Object egovVO) {
        this.egovVO = egovVO;
    }

}
