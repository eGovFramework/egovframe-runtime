package egovframework.rte.fdl.excel;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * FileServiceTest is TestCase of File Handling Service
 * @author Seongjong Yoon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/spring/*.xml"})
public class EgovPOIExcelControllerTest extends AbstractJUnit4SpringContextTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovPOIExcelControllerTest.class);

    @Autowired
    ApplicationContext applicationContext;

    DispatcherServlet dispatcher;

	@Before
    public void init() {
        this.dispatcher = new DispatcherServlet() {
            /**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = -1645563296360811037L;

			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
                GenericWebApplicationContext wac = new GenericWebApplicationContext();
                wac.setParent(applicationContext);
                wac.refresh();
                return wac;
            }
        };
        try {
            this.dispatcher.init(new MockServletConfig());
        } catch (ServletException e) {
            e.printStackTrace();
        }

        LOGGER.debug("######  EgovExcelServiceControllerTest  ######");
    }

    /**
     * [Flow #-6] 엑셀 파일 생성 : 멥으로 데이터를 전송하여 엑셀로 다운로드
     */
	@Test
	public void testExcelDownloadMap() throws ServletException, IOException {
		LOGGER.debug("################################################");

		MockHttpServletRequest request = new MockHttpServletRequest("GET","/sale/listPOIExcelCategory.do");
		MockHttpServletResponse response = new MockHttpServletResponse();

		dispatcher.service(request, response);

		LOGGER.debug("## status : {}", response.getStatus());
		assertEquals(200, response.getStatus());

		LOGGER.debug("response.getContentType() : {}", response.getContentType());
		assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
	}

    /**
     * [Flow #-7] 엑셀 파일 생성 :  VO로 데이터를 전송하여 엑셀로 다운로드
     */
	@Test
	public void testExcelDownloadVO() throws ServletException, IOException {
		LOGGER.debug("################################################");

		MockHttpServletRequest request = new MockHttpServletRequest("GET","/sale/listPOIExcelVOCategory.do");
		MockHttpServletResponse response = new MockHttpServletResponse();

		dispatcher.service(request, response);

		LOGGER.debug("## status : {}", response.getStatus());
		assertEquals(200, response.getStatus());

		LOGGER.debug("response.getContentType() : {}", response.getContentType());
		assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
	}
}