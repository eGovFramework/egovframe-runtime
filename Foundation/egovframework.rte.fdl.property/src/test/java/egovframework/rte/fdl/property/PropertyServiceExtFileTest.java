package egovframework.rte.fdl.property;

import static org.junit.Assert.assertEquals;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Property Service 기본 기능 확인 클래스
 *
 * @author Vincent Han
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2014.08.12  Vincent Han          최초 생성
 *
 * </pre>
 * @since 2014.08.12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:/spring/context-common.xml",
        "classpath*:/spring/context-extfile.xml"})
public class PropertyServiceExtFileTest extends AbstractJUnit4SpringContextTests {

    @Resource(name = "propertyService")
    protected EgovPropertyService propertyService;

    /**
     * 기본 처리 테스트
     *
     * @throws Exception fail to test
     */
    @Test
    public void testPropertiesService() throws Exception {

        assertEquals("안녕하세요.", propertyService.getString("greet.message"));

    }

}
