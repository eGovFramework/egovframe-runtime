package org.egovframe.rte.ptl.mvc.exception;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletResponse;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 *
 * 시스템명 : 실행환경시스템
 * 서브시스템명 : 화면처리
 * 요구사항ID : REQ-RTE-114
 * 요구사항명 : 예외처리기능.
 * 설명 : 발생하는 에러에 따른 처리로직 개발 확보함.
 *
 * @author Ham Cheol
 */

public class ExceptionResolverTest {

	/**
	 *
	 *
	 * @throws Exception
	 */
	@Test
	public void testExceptionViewNameMapping() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		SimpleMappingExceptionResolver exceptionResolver = new SimpleMappingExceptionResolver();
		Object handler = new Object();

		Properties props = new Properties();
		props.setProperty("java.lang.Exception", "genericErrorView");
		props.setProperty("java.lang.NumberFormatException", "numberFormatErrorView");
		exceptionResolver.setExceptionMappings(props);
		ModelAndView mav = exceptionResolver.resolveException(request, response, handler, new Exception());
		assertEquals("genericErrorView", mav.getViewName());
		ModelAndView mav2 = exceptionResolver.resolveException(request, response, handler, new NumberFormatException());
		assertEquals("numberFormatErrorView", mav2.getViewName());
	}

	@SuppressWarnings("unused")
	@Test
	public void testErrorStatusViewNameMapping() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		SimpleMappingExceptionResolver exceptionResolver = new SimpleMappingExceptionResolver();
		Object handler = new Object();

		exceptionResolver.setDefaultErrorView("genericErrorView");
		exceptionResolver.setDefaultStatusCode(HttpServletResponse.SC_BAD_REQUEST);
		ModelAndView mav = exceptionResolver.resolveException(request, response, handler, new Exception());
		assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
	}
}
