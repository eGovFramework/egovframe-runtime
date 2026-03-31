package org.egovframe.rte.fdl.property;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.property.config.PropertyServiceConfig;
import org.egovframe.rte.fdl.property.config.PropertyTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Property Service 기본 기능 확인 클래스
 *
 * @author Vincent Han
 * @version 1.0
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2014.08.12  Vincent Han          최초 생성
 * @since 2014.08.12
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PropertyTestConfig.class, PropertyServiceConfig.class})
public class PropertyServiceExtFileTest {

    @Resource(name = "propertyService")
    private EgovPropertyService propertyService;

    @Test
    public void testPropertiesService() {
        assertEquals("안녕하세요.", propertyService.getString("greet.message"));
    }

}
