package org.egovframe.rte.fdl.security.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.egovframe.rte.fdl.security.userdetails.jdbc.EgovJdbcUserDetailsManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * sqlHierarchicalRoles를 설정하지 않은 배포(DB 기반 role hierarchy 미사용)에서
 * 로그인 경로가 NPE 없이 동작하는지 검증.
 *
 * EgovSecurityTestDatasource(공유 in-memory HSQLDB "testdb")를 그대로 재사용하면
 * 이 클래스만의 다른 컨텍스트 클래스 조합 때문에 Spring 테스트 컨텍스트 캐시가 갈려
 * 같은 HSQLDB URL에 testdb.sql이 두 번 실행되어 "object name already exists"로
 * 깨진다. 그래서 이 테스트 전용의 독립된 in-memory DB를 따로 띄운다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        EgovSecurityConfigurationRoleHierarchyTest.IsolatedTestDatasource.class,
        EgovSecurityConfiguration.class,
        EgovSecurityConfigurationRoleHierarchyTest.NoHierarchyConfig.class
})
public class EgovSecurityConfigurationRoleHierarchyTest {

    @Autowired
    private EgovJdbcUserDetailsManager jdbcUserService;

    @Test
    public void testLoadUserByUsernameWorksWithoutSqlHierarchicalRoles() {
        // HierarchyStringsFactoryBean.getObject()는 sqlHierarchicalRoles 미설정 시
        // 안전 기본값("ROLE_ADMIN > ROLE_USER > ROLE_ANONYMOUS")을 반환하도록 이미 구현돼 있다.
        // roleHierarchy()가 이 기본값을 거치지 않고 조기에 null을 반환하면
        // EgovJdbcUserDetailsManager.loadUserByUsername()이 NPE로 죽는다.
        assertDoesNotThrow(() -> jdbcUserService.loadUserByUsername("user"));
    }

    @Configuration
    static class IsolatedTestDatasource {

        @Bean(name = "dataSource")
        public DataSource dataSource() {
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName("org.hsqldb.jdbcDriver");
            ds.setUrl("jdbc:hsqldb:mem:rolehierarchytestdb");
            ds.setUsername("sa");
            ds.setPassword("");
            ds.setDefaultAutoCommit(true);
            ds.setPoolPreparedStatements(true);

            try (Connection conn = ds.getConnection()) {
                ScriptUtils.executeSqlScript(conn, new ClassPathResource("META-INF/testdata/testdb.sql"));
            } catch (Exception e) {
                throw new IllegalStateException("Isolated test database initialization failed", e);
            }

            return ds;
        }
    }

    @Configuration
    static class NoHierarchyConfig {

        @Bean
        @Primary
        public EgovSecurityConfig egovSecurityConfig() {
            // src/test/resources/egovframework/conf/egov-security-config.properties와
            // 동일한 필드 구성(EgovSecurityConfiguration의 securityFilterChain() 전체가
            // 정상적으로 뜨려면 sqlRolesAndUrl 등도 HSQLDB 테스트 스키마와 맞아야 한다) —
            // 단 sqlHierarchicalRoles만 의도적으로 비워서 재현 대상 시나리오를 만든다.
            EgovSecurityConfig config = new EgovSecurityConfig();
            config.setId("noHierarchyConfig");
            config.setLoginUrl("/uat/uia/egovLoginUsr.do");
            config.setLoginProcessUrl("/uat/uia/actionSecurityProcess.do");
            config.setLogoutUrl("/uat/uia/actionSecurityLogout.do");
            config.setLogoutSuccessUrl("/uat/uia/egovLoginUsr.do");
            config.setLoginFailureUrl("/uat/uia/egovLoginUsr.do?login_error=1");
            config.setAccessDeniedUrl("/sec/ram/accessDenied.do");
            config.setDataSource("dataSource");
            config.setJdbcUsersByUsernameQuery(
                    "SELECT USER_ID, USER_PASSWORD AS PASSWORD, ENABLED, USER_NAME, BIRTH_DAY, SSN FROM USERS WHERE USER_ID = ?");
            config.setJdbcAuthoritiesByUsernameQuery(
                    "SELECT USER_ID, AUTHOR_CODE AS AUTHORITY FROM AUTHORITIES WHERE USER_ID = ?");
            config.setJdbcMapClass("org.egovframe.rte.fdl.security.userdetails.EgovUserDetailsMapping");
            config.setRequestMatcherType("regex");
            config.setHash("egov-sha256");
            config.setHashBase64(true);
            config.setConcurrentMaxSessons(1);
            config.setConcurrentExpiredUrl("/EgovContent.do");
            config.setErrorIfMaximumExceeded(false);
            config.setDefaultTargetUrl("/EgovContent.do");
            config.setAlwaysUseDefaultTargetUrl(true);
            config.setSniff(true);
            config.setXframeOptions("SAMEORIGIN");
            config.setXssProtection(true);
            config.setCacheControl(false);
            config.setCsrf(false);
            config.setPermitAllList(
                    "/css/**,/js/**,/images/**,/resource/**,/public/**,/uat/uia/**,/index.do,/EgovContent.do,/sec/ram/accessDenied.do,/sale/**");
            config.setSqlRolesAndUrl(
                    "SELECT r.ROLE_PTTRN AS url, a.AUTHOR_CODE AS authority "
                            + "FROM ROLES r INNER JOIN AUTHROLES a ON r.ROLE_CODE = a.ROLE_CODE ORDER BY r.ROLE_SORT");
            // sqlHierarchicalRoles는 의도적으로 미설정 — 재현 대상 시나리오.
            return config;
        }
    }

}
