package org.egovframe.rte.fdl.security;

import org.egovframe.rte.fdl.security.config.EgovSecurityConfig;
import org.egovframe.rte.fdl.security.config.EgovSecurityConfiguration;
import org.egovframe.rte.fdl.security.config.EgovSecurityTestConfig;
import org.egovframe.rte.fdl.security.config.EgovSecurityTestDatasource;
import org.egovframe.rte.fdl.security.userdetails.EgovUserDetailsVO;
import org.egovframe.rte.fdl.security.userdetails.util.EgovUserDetailsHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.BeanIds;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * egov-security 모듈 사용자별 로그인·권한관리 테스트
 *
 * <p>- 사용자별 로그인(인증) 및 권한(ROLE) 검증</p>
 * <p>- UserDetails 확장, 권한 계층, URL별 접근권한 검증</p>
 *
 * @author 유지보수
 * @version 5.0
 * @since 2025.06.01
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { EgovSecurityTestDatasource.class, EgovSecurityConfiguration.class, EgovSecurityTestConfig.class })
public class EgovSecurityServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovSecurityServiceTest.class);

    @Autowired
    private ApplicationContext applicationContext;

    @AfterEach
    public void clear() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 설정 경로(conf/egov-security-config.properties)에서 로드된 설정 검증.
     * 설정 파일 경로·내용(id, HSQLDB용 SQL: USERS, AUTHORITIES, ROLES, ROLESHIERARCHY) 확인.
     */
    @Test
    public void testPropertiesConfigurationLoading() {
        EgovSecurityConfig config = applicationContext.getBean(EgovSecurityConfig.class);
        assertNotNull(config, "EgovSecurityConfig 빈이 로드되어야 함");
        assertEquals("egovSecurityConfig", config.getId(), "conf 설정 파일에서 id 로드");
        assertEquals("dataSource", config.getDataSource());
        assertNotNull(config.getJdbcUsersByUsernameQuery());
        assertTrue(config.getJdbcUsersByUsernameQuery().contains("USERS"), "USERS 테이블 기반 쿼리");
        assertNotNull(config.getJdbcAuthoritiesByUsernameQuery());
        assertTrue(config.getJdbcAuthoritiesByUsernameQuery().contains("AUTHORITIES"), "AUTHORITIES 테이블 기반 쿼리");
        assertNotNull(config.getSqlHierarchicalRoles());
        assertTrue(config.getSqlHierarchicalRoles().contains("ROLESHIERARCHY"), "ROLESHIERARCHY 기반 계층 쿼리");
        assertNotNull(config.getSqlRolesAndUrl());
        assertTrue(config.getSqlRolesAndUrl().contains("ROLES") && config.getSqlRolesAndUrl().contains("AUTHROLES"),
                "ROLES/AUTHROLES 기반 URL-역할 매핑 쿼리");

        LOGGER.debug("### properties 설정 파일에서 로드된 security 설정: id={}", config.getId());
    }

    /**
     * DB에 등록된 사용자(user, admin, jimi, test, buyer) 로그인 성공 테스트
     */
    @Test
    public void testAllowAccessForAuthorizedUser() {
        AuthenticationManager authManager = (AuthenticationManager) applicationContext.getBean(BeanIds.AUTHENTICATION_MANAGER);

        for (String[] cred : new String[][] { { "user", "1" }, { "admin", "1" }, { "jimi", "jimi" }, { "test", "test" }, { "buyer", "buyer" } }) {
            UsernamePasswordAuthenticationToken login = new UsernamePasswordAuthenticationToken(cred[0], cred[1]);
            SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
            LOGGER.debug("### EgovSecurityServiceTest.testAllowAccessForAuthorizedUser {}'s password is right!!", cred[0]);
        }
    }

    /**
     * 잘못된 비밀번호로 로그인 시 인증 실패 테스트
     */
    @Test
    void testRejectAccessForUnauthorizedUser() {
        UsernamePasswordAuthenticationToken login = new UsernamePasswordAuthenticationToken("user", "wrongpw");
        AuthenticationManager authManager = (AuthenticationManager) applicationContext.getBean(BeanIds.AUTHENTICATION_MANAGER);

        assertThrows(BadCredentialsException.class, () -> {
            SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
        });
    }

    /**
     * user 로그인(인증) 후 ROLE_USER 권한이 부여되는지 확인
     */
    @Test
    public void testLoginUser() {
        AuthenticationManager authManager = (AuthenticationManager) applicationContext.getBean(BeanIds.AUTHENTICATION_MANAGER);
        SecurityContextHolder.getContext().setAuthentication(
                authManager.authenticate(new UsernamePasswordAuthenticationToken("user", "1")));

        List<String> authorities = EgovUserDetailsHelper.getAuthorities();
        assertNotNull(authorities, "user 권한 목록");
        assertTrue(authorities.contains("ROLE_USER"), "user는 ROLE_USER 보유");
        assertFalse(authorities.contains("ROLE_ADMIN"), "user는 ROLE_ADMIN 미보유");
        LOGGER.debug("#### user 로그인 후 권한: {}", authorities);
    }

    /**
     * admin 로그인(인증) 후 ROLE_ADMIN 권한이 부여되는지 확인
     */
    @Test
    public void testLoginAdmin() {
        AuthenticationManager authManager = (AuthenticationManager) applicationContext.getBean(BeanIds.AUTHENTICATION_MANAGER);
        SecurityContextHolder.getContext().setAuthentication(
                authManager.authenticate(new UsernamePasswordAuthenticationToken("admin", "1")));

        List<String> authorities = EgovUserDetailsHelper.getAuthorities();
        assertNotNull(authorities, "admin 권한 목록");
        assertTrue(authorities.contains("ROLE_ADMIN"), "admin은 ROLE_ADMIN 보유");
        LOGGER.debug("#### admin 로그인 후 권한: {}", authorities);
    }

    /**
     * user(ROLE_USER)로 로그인 후 URL별 필요 역할에 따라 접근 가능/불가 검증
     * - /, /sample/list, /sample/detail 허용
     * - /sample/add, /sample/update, /sample/delete 차단
     */
    @Test
    public void testAccessRightsForUser() {
        AuthenticationManager authManager = (AuthenticationManager) applicationContext.getBean(BeanIds.AUTHENTICATION_MANAGER);
        SecurityContextHolder.getContext().setAuthentication(
                authManager.authenticate(new UsernamePasswordAuthenticationToken("user", "1")));

        assertTrue(canAccess("/"), "user: / 접근 허용");
        assertTrue(canAccess("/sample/list"), "user: /sample/list 접근 허용");
        assertTrue(canAccess("/sample/detail"), "user: /sample/detail 접근 허용");
        assertFalse(canAccess("/sample/add"), "user: /sample/add 접근 차단");
        assertFalse(canAccess("/sample/update"), "user: /sample/update 접근 차단");
        assertFalse(canAccess("/sample/delete"), "user: /sample/delete 접근 차단");

        LOGGER.debug("### user 접근권한 테스트 통과");
    }

    /**
     * admin(ROLE_ADMIN)으로 로그인 후 샘플 URL 전부 접근 허용 검증
     */
    @Test
    public void testAccessRightsForAdmin() {
        AuthenticationManager authManager = (AuthenticationManager) applicationContext.getBean(BeanIds.AUTHENTICATION_MANAGER);
        SecurityContextHolder.getContext().setAuthentication(
                authManager.authenticate(new UsernamePasswordAuthenticationToken("admin", "1")));

        assertTrue(canAccess("/"), "admin: / 접근 허용");
        assertTrue(canAccess("/sample/list"), "admin: /sample/list 접근 허용");
        assertTrue(canAccess("/sample/detail"), "admin: /sample/detail 접근 허용");
        assertTrue(canAccess("/sample/add"), "admin: /sample/add 접근 허용");
        assertTrue(canAccess("/sample/update"), "admin: /sample/update 접근 허용");
        assertTrue(canAccess("/sample/delete"), "admin: /sample/delete 접근 허용");

        LOGGER.debug("### admin 접근권한 테스트 통과");
    }

    /**
     * 세션처리를 위한 UserDetails 확장 테스트 (sampledb 기준 user, admin, jimi, test, buyer)
     */
    @Test
    public void testUserDetailsExt() {
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
        assertFalse(isAuthenticated.booleanValue());
        EgovUserDetailsVO user = (EgovUserDetailsVO) EgovUserDetailsHelper.getAuthenticatedUser();
        assertNull(user);

        AuthenticationManager authManager = (AuthenticationManager) applicationContext.getBean(BeanIds.AUTHENTICATION_MANAGER);

        // user (password 1)
        UsernamePasswordAuthenticationToken login = new UsernamePasswordAuthenticationToken("user", "1");
        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
        assertTrue(EgovUserDetailsHelper.isAuthenticated().booleanValue());
        user = (EgovUserDetailsVO) EgovUserDetailsHelper.getAuthenticatedUser();
        assertNotNull(user);
        assertEquals("user", user.getUserId());
        assertNotNull(user.getUserName());
        assertNotNull(user.getBirthDay());
        assertNotNull(user.getSsn());

        // admin (password 1)
        login = new UsernamePasswordAuthenticationToken("admin", "1");
        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
        assertTrue(EgovUserDetailsHelper.isAuthenticated().booleanValue());
        user = (EgovUserDetailsVO) EgovUserDetailsHelper.getAuthenticatedUser();
        assertNotNull(user);
        assertEquals("admin", user.getUserId());
        assertEquals("Admin", user.getUserName());

        // jimi
        login = new UsernamePasswordAuthenticationToken("jimi", "jimi");
        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
        user = (EgovUserDetailsVO) EgovUserDetailsHelper.getAuthenticatedUser();
        assertNotNull(user);
        assertEquals("jimi", user.getUserId());
        assertEquals("jimi test", user.getUserName());
        assertEquals("19800102", user.getBirthDay());
        assertEquals("1234567890123", user.getSsn());

        // test
        login = new UsernamePasswordAuthenticationToken("test", "test");
        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
        user = (EgovUserDetailsVO) EgovUserDetailsHelper.getAuthenticatedUser();
        assertNotNull(user);
        assertEquals("test", user.getUserId());
        assertEquals("Kim, Young-Su", user.getUserName());
        assertEquals("19800103", user.getBirthDay());
        assertEquals("1234567890123", user.getSsn());

        // buyer
        login = new UsernamePasswordAuthenticationToken("buyer", "buyer");
        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
        user = (EgovUserDetailsVO) EgovUserDetailsHelper.getAuthenticatedUser();
        assertNotNull(user);
        assertEquals("buyer", user.getUserId());
        assertEquals("Lee, Man-hong", user.getUserName());
        assertEquals("19800104", user.getBirthDay());
        assertEquals("1234567890123", user.getSsn());
    }

    /**
     * 지정된 Role 및 계층 조회 테스트 (sampledb: user, admin, buyer, test 및 ROLESHIERARCHY)
     */
    @Test
    public void testAuthoritiesAndRoleHierarchy() {
        AuthenticationManager authManager = (AuthenticationManager) applicationContext.getBean(BeanIds.AUTHENTICATION_MANAGER);

        // user (password 1) : ROLE_USER, ROLE_RESTRICTED
        UsernamePasswordAuthenticationToken login = new UsernamePasswordAuthenticationToken("user", "1");
        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
        List<String> authorities = EgovUserDetailsHelper.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("ROLE_RESTRICTED"));

        // admin : ROLE_ADMIN
        login = new UsernamePasswordAuthenticationToken("admin", "1");
        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
        authorities = EgovUserDetailsHelper.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.contains("ROLE_ADMIN"));

        // buyer : ROLE_RESTRICTED only
        login = new UsernamePasswordAuthenticationToken("buyer", "buyer");
        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
        authorities = EgovUserDetailsHelper.getAuthorities();
        assertNotNull(authorities);
        assertFalse(authorities.contains("ROLE_USER"));
        assertFalse(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("ROLE_RESTRICTED"));

        // test : ROLE_ADMIN, ROLE_RESTRICTED (and ROLE_USER from hierarchy)
        login = new UsernamePasswordAuthenticationToken("test", "test");
        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
        authorities = EgovUserDetailsHelper.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("ROLE_RESTRICTED"));
    }

    /**
     * 현재 인증된 사용자의 권한으로 주어진 URL 접근이 허용되는지 판단.
     */
    private boolean canAccess(String url) {
        FilterSecurityInterceptor interceptor = applicationContext.getBean("filterSecurityInterceptor", FilterSecurityInterceptor.class);
        FilterInvocationSecurityMetadataSource metadataSource = interceptor.getSecurityMetadataSource();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath(url);
        FilterInvocation fi = new FilterInvocation(request, new MockHttpServletResponse(), new MockFilterChain());
        Collection<org.springframework.security.access.ConfigAttribute> attrs = metadataSource.getAttributes(fi);

        if (attrs == null || attrs.isEmpty()) {
            return true;
        }
        List<String> userAuthorities = EgovUserDetailsHelper.getAuthorities();
        if (userAuthorities == null) {
            return false;
        }
        for (org.springframework.security.access.ConfigAttribute attr : attrs) {
            String role = attr.getAttribute();
            if (role != null && userAuthorities.contains(role)) {
                return true;
            }
        }
        return false;
    }
}
