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
package org.egovframe.rte.fdl.security.userdetails.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egovframe.rte.fdl.security.userdetails.EgovUserDetails;
import org.egovframe.rte.fdl.string.EgovObjectUtil;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

/**
 * 사용자계정 정보를 DB에서 관리할수 있도록 구현한 클래스
 * 
 * <p><b>NOTE:</b> org.springframework.security.userdetails.jdbc.JdbcUserDetailsManager 를 확장하여 사용자 계정 정보를 DB에서 관리할 수 있도록 구현한 클래스이다.</p>
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
 * 2017.02.15   장동한             시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
 * </pre>
 */
public class EgovJdbcUserDetailsManager extends JdbcUserDetailsManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovJdbcUserDetailsManager.class);

    private EgovUsersByUsernameMapping usersByUsernameMapping;
    private String mapClass;
    private RoleHierarchy roleHierarchy = null;

    /**
     * 사용자 테이블의 쿼리 조회 컬럼과 세션에서 사용할 사용자 VO와 메핑 할 클래스를 지정한다.
     * 
     * @param mapClass
     */
    public void setMapClass(String mapClass) {
        this.mapClass = mapClass;
    }

    /**
     * Role Hierarchy를 지원한다.
     * 
     * @param roleHierarchy
     */
    public void setRoleHierarchy(RoleHierarchy roleHierarchy) {
        this.roleHierarchy = roleHierarchy;
    }

    /**
     * JdbcUserDetailsManager 클래스 재정의
     */
    @Override
	protected void initDao() throws ApplicationContextException {
		super.initDao();
        try {
            initMappingSqlQueries();
        } catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            LOGGER.error("EgovJdbcUserDetailsManager.initDao.Exception : {}", e.toString(), e);
        }
    }

    /**
     * jdbc-user-service의 usersByUsernameQuery 사용자조회 쿼리와
     * authoritiesByUsernameQuery 권한조회 쿼리를 이용하여 정보를 저장한다.
     * 
     * @throws Exception
     * @throws ClassNotFoundException
     */
	private void initMappingSqlQueries() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		LOGGER.debug("## EgovJdbcUserDetailsManager query : {}", getUsersByUsernameQuery());
		LOGGER.debug("Mapping Class : {}", this.mapClass);
        Class<?> clazz = EgovObjectUtil.loadClass(this.mapClass);
        Constructor<?> constructor = clazz.getConstructor(new Class[] {DataSource.class, String.class });
        Object[] params = new Object[] { getDataSource(), getUsersByUsernameQuery() };
        this.usersByUsernameMapping = (EgovUsersByUsernameMapping) constructor.newInstance(params);
    }

    /**
     * JdbcDaoImpl 클래스의 loadUsersByUsername 메소드 재정의.
     * 사용자명(또는 ID)로 UserDetails 정보를 조회하여 리스트 형식으로 저장한다.
     */
    @Override
    protected List<UserDetails> loadUsersByUsername(String username) {
        List<EgovUserDetails> list = usersByUsernameMapping.execute(username);
        ArrayList<UserDetails> newList = new ArrayList<UserDetails>();
        for (EgovUserDetails user : list) {
        	newList.add(user);
        }
        return newList;
    }

    /**
     * JdbcDaoImpl 클래스의 loadUsersByUsername 메소드 재정의.
     * 사용자명(또는 ID)로 EgovUserDetails의 정보를 조회한다.
     * 
     * @param username
     * @return
     * @throws UsernameNotFoundException
     * @throws DataAccessException
     */
    @Override
    public EgovUserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        List<UserDetails> users = loadUsersByUsername(username);
        if (users.size() == 0) {
        	LOGGER.debug("Query returned no results for user '{}'", username);
			throw new UsernameNotFoundException(messages.getMessage("EgovJdbcUserDetailsManager.notFound", new Object[] { username }, "Username {0} not found"));
        }

        UserDetails obj = users.get(0);
        EgovUserDetails userDetails = (EgovUserDetails) obj;
        Set<GrantedAuthority> dbAuthsSet = new HashSet<GrantedAuthority>();
        dbAuthsSet.addAll(loadUserAuthorities(userDetails.getUsername()));
        List<GrantedAuthority> dbAuths = new ArrayList<GrantedAuthority>(dbAuthsSet);
        addCustomAuthorities(userDetails.getUsername(), dbAuths);
        if (dbAuths.size() == 0) {
			throw new UsernameNotFoundException(messages.getMessage("EgovJdbcUserDetailsManager.noAuthority", new Object[] { username }, "User {0} has no GrantedAuthority"));
        }

        // RoleHierarchyImpl 에서 저장한 Role Hierarchy 정보가 저장된다.
        Collection<? extends GrantedAuthority> authorities = roleHierarchy.getReachableGrantedAuthorities(dbAuths);

        // JdbcDaoImpl 클래스의 createUserDetails 메소드 재정의
        return new EgovUserDetails(userDetails.getUsername(), userDetails.getPassword(), userDetails.isEnabled(), true, true, true, authorities, userDetails.getEgovUserVO());
    }

    /**
     * 인증된 사용자 이름으로 사용자정보(EgovUserDetails)를 가져온다.
     * 
     * @return
     * @throws UsernameNotFoundException
     * @throws DataAccessException
     */
    public EgovUserDetails getAuthenticatedUser() throws UsernameNotFoundException, DataAccessException {
        return loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    }

}
