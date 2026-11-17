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
package org.egovframe.boot.security.userdetails.jdbc;

import lombok.Setter;
import org.egovframe.boot.security.userdetails.EgovUserDetails;
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
import org.springframework.util.ObjectUtils;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * JDBC 기반 사용자 조회 · 관리 클래스(권한 계층 반영).
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
public class EgovJdbcUserDetailsManager extends JdbcUserDetailsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovJdbcUserDetailsManager.class);

    private EgovUsersByUsernameMapping usersByUsernameMapping;

    /** 사용자 조회 결과를 사용자 VO 로 매핑할 클래스명 */
    @Setter
    private String mapClass;

    /** 권한 계층 */
    @Setter
    private RoleHierarchy roleHierarchy = null;

    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
        if (ObjectUtils.isEmpty(clazz)) {
            clazz = Class.forName(className);
        }
        return clazz;
    }

    /** 사용자 · 권한 조회 매핑 쿼리 초기화 */
    @Override
    protected void initDao() throws ApplicationContextException {
        super.initDao();
        try {
            initMappingSqlQueries();
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 InstantiationException e) {
            LOGGER.error("EgovJdbcUserDetailsManager.initDao.Exception : {}", e.toString(), e);
        }
    }

    /** usersByUsernameQuery 로 사용자 조회 매핑을 생성한다. */
    private void initMappingSqlQueries() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        LOGGER.debug("## EgovJdbcUserDetailsManager query : {}", getUsersByUsernameQuery());
        Class<?> clazz = loadClass(this.mapClass);
        Constructor<?> constructor = clazz.getConstructor(DataSource.class, String.class);
        Object[] params = new Object[]{getDataSource(), getUsersByUsernameQuery()};
        this.usersByUsernameMapping = (EgovUsersByUsernameMapping) constructor.newInstance(params);
    }

    /** 사용자 ID 로 사용자 목록 조회 */
    @Override
    protected List<UserDetails> loadUsersByUsername(String username) {
        List<EgovUserDetails> list = usersByUsernameMapping.execute(username);
        return new ArrayList<>(list);
    }

    /** 사용자 ID 로 EgovUserDetails 조회(권한계층 반영) */
    @Override
    public EgovUserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        // 익명 사용자 처리
        if ("anonymousUser".equals(username)) {
            List<GrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ANONYMOUS")
            );
            return new EgovUserDetails("anonymousUser", "", true, true, true, true, authorities, null);
        }
        List<UserDetails> users = loadUsersByUsername(username);
        if (users.isEmpty()) {
            LOGGER.debug("### EgovJdbcUserDetailsManager.loadUserByUsername >>> Query returned no results for user '{}'", username);
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

        // 권한계층 반영
        Collection<? extends GrantedAuthority> authorities = roleHierarchy.getReachableGrantedAuthorities(dbAuths);

        return new EgovUserDetails(userDetails.getUsername(), userDetails.getPassword(), userDetails.isEnabled(), true, true, true, authorities, userDetails.getEgovUserVO());
    }

    /** 인증된 사용자 이름으로 사용자정보(EgovUserDetails)를 가져온다. */
    public EgovUserDetails getAuthenticatedUser() throws UsernameNotFoundException, DataAccessException {
        return loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    }

}
