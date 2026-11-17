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
package org.egovframe.boot.security.userdetails.jdbc;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.security.userdetails.EgovUserDetails;
import org.springframework.context.ApplicationContextException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Slf4j
public class EgovJdbcUserDetailsManager extends JdbcUserDetailsManager {

    private EgovUsersByUsernameMapping usersByUsernameMapping;
    /**
     * -- SETTER --
     *  사용자 테이블의 쿼리 조회 컬럼과 세션에서 사용할 사용자 VO와 메핑 할 클래스를 지정한다.
     */
    @Setter
    private String mapClass;
    /**
     * -- SETTER --
     *  Role Hierarchy를 지원한다.
     */
    @Setter
    private RoleHierarchy roleHierarchy = null;

    /**
     * JdbcUserDetailsManager 클래스 재정의
     */
    @Override
	protected void initDao() throws ApplicationContextException {
		super.initDao();
        try {
            initMappingSqlQueries();
        } catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            log.error("### EgovJdbcUserDetailsManager.initDao.Exception : {}", e.toString());
        }
    }

    /**
     * jdbc-user-service의 usersByUsernameQuery 사용자조회 쿼리와
     * authoritiesByUsernameQuery 권한조회 쿼리를 이용하여 정보를 저장한다.
     */
	private void initMappingSqlQueries() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		log.info("### EgovJdbcUserDetailsManager.initMappingSqlQueries >>> query : {}}", getUsersByUsernameQuery());
		log.info("### EgovJdbcUserDetailsManager.initMappingSqlQueries >>> Mapping Class : {}", this.mapClass);
        Class<?> clazz = loadClass(this.mapClass);
        Constructor<?> constructor = clazz.getConstructor(DataSource.class, String.class);
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
        return new ArrayList<>(list);
    }

    /**
     * JdbcDaoImpl 클래스의 loadUsersByUsername 메소드 재정의.
     * 사용자명(또는 ID)로 EgovUserDetails의 정보를 조회한다.
     */
    @Override
    public EgovUserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        List<UserDetails> users = loadUsersByUsername(username);
        if (users.isEmpty()) {
        	log.debug("### EgovJdbcUserDetailsManager.loadUserByUsername >>> Query returned no results for user '{}'", username);
			throw new UsernameNotFoundException(messages.getMessage("### EgovJdbcUserDetailsManager.notFound", new Object[] { username }, "Username {0} not found"));
        }

        UserDetails obj = users.get(0);
        EgovUserDetails userDetails = (EgovUserDetails) obj;
        Set<GrantedAuthority> dbAuthsSet = new HashSet<>(loadUserAuthorities(userDetails.getUsername()));
        List<GrantedAuthority> dbAuths = new ArrayList<>(dbAuthsSet);
        addCustomAuthorities(userDetails.getUsername(), dbAuths);
        if (dbAuths.isEmpty()) {
			throw new UsernameNotFoundException(messages.getMessage("### EgovJdbcUserDetailsManager.noAuthority", new Object[] { username }, "User {0} has no GrantedAuthority"));
        }

        // RoleHierarchyImpl 에서 저장한 Role Hierarchy 정보가 저장된다.
        Collection<? extends GrantedAuthority> authorities = roleHierarchy.getReachableGrantedAuthorities(dbAuths);

        // JdbcDaoImpl 클래스의 createUserDetails 메소드 재정의
        return new EgovUserDetails(userDetails.getUsername(), userDetails.getPassword(), userDetails.isEnabled(), true, true, true, authorities, userDetails.getEgovUserVO());
    }

    /**
     * 인증된 사용자 이름으로 사용자정보(EgovUserDetails)를 가져온다.
     */
    public EgovUserDetails getAuthenticatedUser() throws UsernameNotFoundException, DataAccessException {
        return loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
        if (clazz == null) {
            clazz = Class.forName(className);
        }
        return clazz;
    }

}
