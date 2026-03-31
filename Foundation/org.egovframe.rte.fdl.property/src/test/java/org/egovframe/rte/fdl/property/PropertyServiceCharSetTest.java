package org.egovframe.rte.fdl.property;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.property.config.PropertyServiceExtendConfig;
import org.egovframe.rte.fdl.property.config.PropertyTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Property Service 다국어 지원 기능 확인 클래스
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
public class PropertyServiceCharSetTest {

    @Resource(name = "propertyServiceExtend")
    private EgovPropertyService propertyService;

    @Test
    public void testKoreanLangFromPropertiesFile() {
        assertEquals("안녕하세요.", propertyService.getString("greet.message"));
    }

    @Test
    public void testKoreanLangFromXMLElement() {
        assertEquals("가나다", propertyService.getString("cccc"));
    }

}
