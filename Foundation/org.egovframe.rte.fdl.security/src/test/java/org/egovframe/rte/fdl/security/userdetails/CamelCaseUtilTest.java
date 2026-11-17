package org.egovframe.rte.fdl.security.userdetails;

import org.egovframe.rte.fdl.security.config.EgovSecurityConfiguration;
import org.egovframe.rte.fdl.security.config.EgovSecurityTestConfig;
import org.egovframe.rte.fdl.security.config.EgovSecurityTestDatasource;
import org.egovframe.rte.fdl.security.userdetails.util.CamelCaseUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * CamelCase 유틸리티 테스트 (JUnit 5)
 *
 * @author EgovFramework Team
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { EgovSecurityTestDatasource.class, EgovSecurityConfiguration.class, EgovSecurityTestConfig.class })
public class CamelCaseUtilTest {

    @Test
    public void convert2CamelCase() {
        assertEquals("repoNameEgovframeRuntime", CamelCaseUtil.convert2CamelCase("REPO_NAME_egovframe_Runtime"));
        assertEquals("column1", CamelCaseUtil.convert2CamelCase("column1"));
        assertEquals("column1", CamelCaseUtil.convert2CamelCase("column_1"));
        assertEquals("column1Test", CamelCaseUtil.convert2CamelCase("column_1_test"));
        assertEquals("Column1Test", CamelCaseUtil.convert2CamelCase("_column_1_test_"));
    }

    /**
     * null·빈 문자열은 그대로 반환 (charAt(0) 예외·NPE 방지 가드).
     */
    @Test
    public void convert2CamelCaseEmptyAndNull() {
        assertEquals("", CamelCaseUtil.convert2CamelCase(""));
        assertNull(CamelCaseUtil.convert2CamelCase(null));
    }

}
