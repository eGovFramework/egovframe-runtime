package egovframework.rte.ptl.mvc.handler;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 *
 * 시스템명 : 실행환경시스템
 * 서브시스템명 : 화면처리
 * 요구사항ID : REQ-RTE-113
 * 요구사항명 : 전후처리기능 제공.
 * 설명 : 웹 요청/응답로직의 전후처리 기능 제공함.
 *
 * @author Ham Cheol
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/handler/test_servlet.xml")
public class WebRequestInterceptorTest {

	@Autowired
	private ApplicationContext ctx;

	private DispatcherServlet dispatcher;

	/**
	 * org.springframework.web.servlet.mvc.annotation.ServletAnnotationControllerTests 를 참조했다.
	 * @throws ServletException
	 */
	@Before
	public void setUp() throws ServletException {
		this.dispatcher = new DispatcherServlet() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = -6801286643507797419L;

			@Override
			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) throws BeansException {
				GenericWebApplicationContext genericCtx = new GenericWebApplicationContext();
				genericCtx.setParent(ctx);
				genericCtx.refresh();
				return genericCtx;
			}
		};
		dispatcher.init(new MockServletConfig());
	}

	@Test
	public void testPrePostHandle() throws ServletException, IOException {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		dispatcher.service(request, response);
		ArrayList<?> array = (ArrayList<?>) request.getAttribute("interceptor");
		assertEquals("OneInterceptor.preHandle", array.get(0));
		assertEquals("TwoInterceptor.preHandle", array.get(1));
		assertEquals("InterceptorTestController", array.get(2));
		assertEquals("TwoInterceptor.postHandle", array.get(3));
		assertEquals("OneInterceptor.postHandle", array.get(4));
	}
}
