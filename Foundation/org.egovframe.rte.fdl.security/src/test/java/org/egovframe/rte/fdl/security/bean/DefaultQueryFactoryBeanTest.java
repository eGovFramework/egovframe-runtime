package org.egovframe.rte.fdl.security.bean;

import org.egovframe.rte.fdl.security.config.EgovSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.apache.commons.dbcp2.BasicDataSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 설정을 생략했을 때 쓰이는 기본 조회 쿼리가 실제로 동작하는지 검증.
 *
 * UsersQueryFactoryBean과 AuthoritiesQueryFactoryBean은 같은 목적의 형제이고
 * 둘 다 user_id 컬럼을 쓰는 Spring Security 표준 스키마를 가정한다. 그 스키마를
 * 그대로 만들어 두 기본 쿼리를 나란히 실행한다.
 */
public class DefaultQueryFactoryBeanTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:mem:defaultquerytestdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setDefaultAutoCommit(true);
        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("CREATE TABLE USERS(USER_ID VARCHAR(30) NOT NULL PRIMARY KEY,"
                + " PASSWORD VARCHAR(100) NOT NULL, ENABLED BOOLEAN DEFAULT TRUE)");
        jdbcTemplate.execute("CREATE TABLE AUTHORITIES(USER_ID VARCHAR(30) NOT NULL,"
                + " AUTHORITY VARCHAR(50) NOT NULL)");
        jdbcTemplate.update("INSERT INTO USERS VALUES(?, ?, ?)", "tester", "{noop}pw", true);
        jdbcTemplate.update("INSERT INTO AUTHORITIES VALUES(?, ?)", "tester", "ROLE_USER");
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DROP TABLE AUTHORITIES");
        jdbcTemplate.execute("DROP TABLE USERS");
    }

    @Test
    public void usersDefaultQueryRunsOnStandardSchema() {
        String query = new UsersQueryFactoryBean(new EgovSecurityConfig()).getObject();

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(query, "tester"));
    }

    @Test
    public void authoritiesDefaultQueryRunsOnStandardSchema() {
        String query = new AuthoritiesQueryFactoryBean(new EgovSecurityConfig()).getObject();

        assertDoesNotThrow(() -> jdbcTemplate.queryForList(query, "tester"));
        assertEquals(1, jdbcTemplate.queryForList(query, "tester").size());
    }

}
