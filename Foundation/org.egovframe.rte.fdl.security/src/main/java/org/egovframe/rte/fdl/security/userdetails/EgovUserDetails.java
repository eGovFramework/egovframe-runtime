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
package org.egovframe.rte.fdl.security.userdetails;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * 사용자 계정 정보를 관리하기 위한 클래스
 * 
 * <p><b>NOTE:</b>org.springframework.security.userdetails.UserDetails
 * 인터페이스를 확장하여 사용자 계정의 여러가지 정보를 세션으로 관리할 수 있다.</p>
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
     * @throws IllegalArgumentException
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
     * 
     * @param username
     * @param password
     * @param enabled
     * @param egovVO
     * @throws IllegalArgumentException
     */
    public EgovUserDetails(String username, String password, boolean enabled, Object egovVO) throws IllegalArgumentException {
    	this(username, password, enabled, true, true, true,
    			Arrays.asList(new GrantedAuthority[] {new SimpleGrantedAuthority("HOLDER")}), egovVO);
    }

    /**
     * 인증 객체 얻기.
     * 
     * @return 사용자VO 객체
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
