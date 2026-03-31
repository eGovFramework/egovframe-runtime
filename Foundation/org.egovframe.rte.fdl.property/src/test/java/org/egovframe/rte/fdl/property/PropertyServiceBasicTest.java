package org.egovframe.rte.fdl.property;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.property.config.PropertyServiceExtendConfig;
import org.egovframe.rte.fdl.property.config.PropertyTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property Service 기본 기능 확인 클래스
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
public class PropertyServiceBasicTest {

    @Resource(name = "propertyServiceExtend")
    private EgovPropertyService propertyService;

    @Test
    public void testPropertiesService() {

        assertEquals("안녕하세요.", propertyService.getString("greet.message"));

        assertNotNull(propertyService.getString("tokens_on_multiple_lines"));

        assertEquals("1234", propertyService.getString("AAAA"));

        assertEquals(Double.valueOf(1234), Double.valueOf(propertyService.getDouble("number.double")));

        assertEquals(Float.valueOf(1234), Float.valueOf(propertyService.getFloat("number.float")));

        assertEquals(Integer.valueOf(1234), Integer.valueOf(propertyService.getInt("number.int")));

        assertEquals(Long.valueOf(1234), Long.valueOf(propertyService.getLong("number.long")));

        assertNotNull(propertyService.getString("AAAA", ""));

        assertEquals(Double.valueOf(1234), Double.valueOf(propertyService.getDouble("number.double", 123.4)));

        assertEquals(Float.valueOf(1234), Float.valueOf(propertyService.getFloat("number.float", (float) 123.4)));

        assertEquals(Integer.valueOf(1234), Integer.valueOf(propertyService.getInt("number.int", 123)));

        assertEquals(Long.valueOf(1234), Long.valueOf(propertyService.getLong("number.long", 1234)));

        assertNotNull(propertyService.getKeys());

        assertNotNull(propertyService.getKeys("number"));

        assertTrue(propertyService.getBoolean("boolean"));

        assertFalse(propertyService.getBoolean("notexistboolean", false));

        assertEquals(2, propertyService.getStringArray("tokens_on_a_line").length);

        assertEquals("~!@#$%^&*()_+;{}|", propertyService.getString("special.test"));

    }

}
