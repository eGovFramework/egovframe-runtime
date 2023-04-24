package org.egovframe.rte.ptl.mvc.bind;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/bind/test_servlet.xml")
public class CommandMapArgumentResolverTest {

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
			private static final long serialVersionUID = 8396656162228611544L;

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
	public void testCommandMapWithHttpRequest() throws ServletException, IOException {

		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse mockResponse = new MockHttpServletResponse();
		mockRequest.setMethod("POST");
		mockRequest.setRequestURI("/test.do");
		mockRequest.addParameter("USA", "washington");
		mockRequest.addParameter("japan", "tokyo");
		mockRequest.addParameter("korea", "seoul");

		dispatcher.service(mockRequest, mockResponse);

		assertEquals("washington", mockRequest.getAttribute("USA"));
		assertEquals("tokyo", mockRequest.getAttribute("japan"));
		assertEquals("seoul", mockRequest.getAttribute("korea"));
	}

	@Test
	public void testCommandMapWithHttpMultiparRequest() throws ServletException, IOException {

		MockMultipartHttpServletRequest mockRequest = new MockMultipartHttpServletRequest();
		MockHttpServletResponse mockResponse = new MockHttpServletResponse();
		mockRequest.setMethod("POST");
		mockRequest.setRequestURI("/test.do");
		mockRequest.addParameter("USA", "washington");
		mockRequest.addParameter("japan", "tokyo");
		mockRequest.addParameter("korea", "seoul");

		dispatcher.service(mockRequest, mockResponse);

		assertEquals("washington", mockRequest.getAttribute("USA"));
		assertEquals("tokyo", mockRequest.getAttribute("japan"));
		assertEquals("seoul", mockRequest.getAttribute("korea"));
	}

}
