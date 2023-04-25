package org.egovframe.rte.fdl.property;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

/**
 * Property Service 다국어 지원 기능 확인 클래스
 * @author 실행환경 개발팀 김태호
 * @since 2009.02.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.02.01  김태호          최초 생성
 * 
 * </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring/context-common.xml",
    "classpath*:/spring/context-properties.xml" })
public class PropertyServiceCharSetTest {

    @Resource(name = "propertyService")
    protected EgovPropertyService propertyService = null;

    @Test
    public void testKoreanLangFromPropertiesFile() throws Exception {
        assertEquals("안녕하세요.", propertyService.getString("greet.message"));

    }

    @Test
    public void testKoreanLangFromXMLElement() throws Exception {
        assertEquals("가나다", propertyService.getString("cccc"));
    }

}
