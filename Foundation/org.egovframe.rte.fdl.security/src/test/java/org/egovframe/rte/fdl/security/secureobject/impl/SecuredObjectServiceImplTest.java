package org.egovframe.rte.fdl.security.secureobject.impl;

import org.egovframe.rte.fdl.security.config.EgovSecurityConfig;
import org.egovframe.rte.fdl.security.config.EgovSecurityConfiguration;
import org.egovframe.rte.fdl.security.config.EgovSecurityTestConfig;
import org.egovframe.rte.fdl.security.config.EgovSecurityTestDatasource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
// EgovSecurityServiceTest와 동일한 컨텍스트 클래스 조합을 써서 Spring 테스트 컨텍스트 캐시를 공유한다.
// 다른 조합을 쓰면 같은 in-memory HSQLDB URL에 두 번째 DataSource가 만들어져 testdb.sql의
// CREATE TABLE이 "object name already exists"로 실패한다.
@ContextConfiguration(classes = { EgovSecurityTestDatasource.class, EgovSecurityConfiguration.class, EgovSecurityTestConfig.class })
public class SecuredObjectServiceImplTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testGetRolesAndUrlWithDefaultRequestMatcherType() {
        // requestMatcherType을 지정하지 않은 EgovSecurityConfig — properties 파일에
        // 이 키를 안 적으면 SecuredObjectServiceImpl 자신의 필드 기본값("ant")이 그대로 쓰인다.
        EgovSecurityConfig config = new EgovSecurityConfig();

        SecuredObjectDAO dao = new SecuredObjectDAO(config);
        dao.setDataSource(dataSource);
        dao.setSqlRolesAndUrl(
                "SELECT r.ROLE_PTTRN AS url, a.AUTHOR_CODE AS authority "
                        + "FROM ROLES r INNER JOIN AUTHROLES a ON r.ROLE_CODE = a.ROLE_CODE ORDER BY r.ROLE_SORT");

        SecuredObjectServiceImpl service = new SecuredObjectServiceImpl(config);
        service.setSecuredObjectDAO(dao);

        // SecuredObjectDAO는 "regex"/"ciRegex"만 RequestMatcher를 만들고 그 외("ant" 포함)엔
        // 순수 String을 반환하는데, SecuredObjectServiceImpl.getRolesAndUrl()은 무조건
        // (RequestMatcher) key로 캐스팅해 ClassCastException이 난다.
        assertDoesNotThrow(service::getRolesAndUrl);
    }

}
