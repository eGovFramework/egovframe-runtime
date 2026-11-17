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
// EgovSecurityServiceTest 와 동일 컨텍스트 조합 : 컨텍스트 캐시 공유
// (다른 조합이면 같은 HSQLDB URL 에 testdb.sql 재실행 → "object name already exists")
@ContextConfiguration(classes = { EgovSecurityTestDatasource.class, EgovSecurityConfiguration.class, EgovSecurityTestConfig.class })
public class SecuredObjectServiceImplTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testGetRolesAndUrlWithDefaultRequestMatcherType() {
        // requestMatcherType 미지정 EgovSecurityConfig (SecuredObjectServiceImpl 필드 기본값 "ant" 사용)
        EgovSecurityConfig config = new EgovSecurityConfig();

        SecuredObjectDAO dao = new SecuredObjectDAO(config);
        dao.setDataSource(dataSource);
        dao.setSqlRolesAndUrl(
                "SELECT r.ROLE_PTTRN AS url, a.AUTHOR_CODE AS authority "
                        + "FROM ROLES r INNER JOIN AUTHROLES a ON r.ROLE_CODE = a.ROLE_CODE ORDER BY r.ROLE_SORT");

        SecuredObjectServiceImpl service = new SecuredObjectServiceImpl(config);
        service.setSecuredObjectDAO(dao);

        // SecuredObjectDAO 는 "regex"/"ciRegex" 만 RequestMatcher, 그 외("ant" 포함)는 String 키
        // : getRolesAndUrl() 의 (RequestMatcher) 무조건 캐스팅 시 ClassCastException
        assertDoesNotThrow(service::getRolesAndUrl);
    }

}
