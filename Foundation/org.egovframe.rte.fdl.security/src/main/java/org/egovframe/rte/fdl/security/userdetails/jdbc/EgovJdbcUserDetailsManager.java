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
package org.egovframe.rte.fdl.security.userdetails.jdbc;

import org.egovframe.rte.fdl.security.userdetails.EgovUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.egovframe.rte.fdl.string.EgovObjectUtil.loadClass;

/**
 * 사용자계정 정보를 DB에서 관리할수 있도록 구현한 클래스
 *
 * <p><b>NOTE:</b> org.springframework.security.userdetails.jdbc.JdbcUserDetailsManager를
 * 확장하여 사용자 계정 정보를 DB에서 관리할 수 있도록 구현한 클래스이다.</p>
 *
 * @author 실행환경 개발팀 윤성종
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
 * @since 2009.06.01
 */
public class EgovJdbcUserDetailsManager extends JdbcUserDetailsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovJdbcUserDetailsManager.class);

    private EgovUsersByUsernameMapping usersByUsernameMapping;
    private String mapClass;
    private RoleHierarchy roleHierarchy = null;

    /**
     *
     * @param roleHierarchy
     */
    public void setRoleHierarchy(RoleHierarchy roleHierarchy) {
        this.roleHierarchy = roleHierarchy;
    }

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
     * JdbcUserDetailsManager 클래스 재정의
     */
    @Override
    protected void initDao() throws ApplicationContextException {
        super.initDao();
        try {
            initMappingSqlQueries();
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            LOGGER.debug("[{}] EgovJdbcUserDetailsManager initDao() : {}", e.getClass().getName(), e.getMessage());
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
        LOGGER.debug("## EgovJdbcUserDetailsManager initMappingSqlQueries() query : {}", getUsersByUsernameQuery());
        Class<?> clazz = loadClass(this.mapClass);
        Constructor<?> constructor = clazz.getConstructor(DataSource.class, String.class);
        Object[] params = new Object[]{getDataSource(), getUsersByUsernameQuery()};
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
     *
     * @param username
     * @return
     * @throws UsernameNotFoundException
     * @throws DataAccessException
     */
    @Override
    public EgovUserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        // 익명 사용자 처리
        if ("anonymousUser".equals(username)) {
            List<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
            return new EgovUserDetails("anonymousUser", "", true, true, true, true, authorities, null);
        }
        List<UserDetails> users = loadUsersByUsername(username);
        if (users.isEmpty()) {
            LOGGER.debug("### EgovJdbcUserDetailsManager loadUserByUsername() Query returned no results for user '{}'", username);
            throw new UsernameNotFoundException(messages.getMessage("### EgovJdbcUserDetailsManager.notFound", new Object[]{username}, "Username {0} not found"));
        }

        UserDetails obj = users.get(0);
        EgovUserDetails userDetails = (EgovUserDetails) obj;
        Set<GrantedAuthority> dbAuthsSet = new HashSet<>(loadUserAuthorities(userDetails.getUsername()));
        List<GrantedAuthority> dbAuths = new ArrayList<>(dbAuthsSet);
        addCustomAuthorities(userDetails.getUsername(), dbAuths);
        if (dbAuths.isEmpty()) {
            throw new UsernameNotFoundException(messages.getMessage("### EgovJdbcUserDetailsManager.noAuthority", new Object[]{username}, "User {0} has no GrantedAuthority"));
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
