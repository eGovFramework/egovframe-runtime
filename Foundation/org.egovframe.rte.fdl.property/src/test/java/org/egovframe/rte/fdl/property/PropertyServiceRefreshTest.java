package org.egovframe.rte.fdl.property;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.property.config.PropertyServiceExtendConfig;
import org.egovframe.rte.fdl.property.config.PropertyTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PropertyServiceRefreshTest
 * <b>NOTE</b>: Property Service 리로딩 기능 확인.
 *
 * @author 실행환경 개발팀 김태호
 * @version 1.0
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2009.02.01  김태호          최초 생성
 * @since 2009.02.01
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PropertyTestConfig.class, PropertyServiceExtendConfig.class})
public class PropertyServiceRefreshTest {

    @Resource(name = "propertyServiceExtend")
    private EgovPropertyService propertyService;

    @Test
    public void testRefreshPropertiesFiles() throws FdlException, IOException {
        for (String value : propertyService.getStringArray("tokens_on_multiple_lines")) {
            System.out.println("### tokens_on_multiple_lines >>> " + value);
        }

        assertEquals(4, propertyService.getStringArray("tokens_on_multiple_lines").length);
        assertEquals("first token refresh", propertyService.getString("tokens_on_multiple_lines"));
        assertEquals(Double.valueOf(1234), Double.valueOf(propertyService.getDouble("number.double")));
        propertyService.refreshPropertyFiles();
        assertEquals("first token refresh", propertyService.getString("tokens_on_multiple_lines"));
        assertEquals(Double.valueOf(1234), Double.valueOf(propertyService.getDouble("number.double")));
    }

}
