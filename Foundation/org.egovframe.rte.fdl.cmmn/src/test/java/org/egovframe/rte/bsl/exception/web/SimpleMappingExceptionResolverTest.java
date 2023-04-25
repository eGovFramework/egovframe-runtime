package org.egovframe.rte.bsl.exception.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/exception-resolver.xml" })
public class SimpleMappingExceptionResolverTest {

	@Resource(name = "smeResovler")
	private SimpleMappingExceptionResolver exceptionResolver;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private Object handler1;
	private Object handler2;
	private Exception exception;

	@Before
	public void setUp() throws Exception {
		handler1 = new String();
		handler2 = new Object();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		request.setMethod("GET");
		exception = new Exception();
	}


	@Test
	public void testDefaultErrorView() {
		// 설정 값 테스트
		ModelAndView mav = exceptionResolver.resolveException(request, response, handler1, exception);
		System.out.println(mav.getViewName());
		assertEquals("common/error", mav.getViewName());

		// 설정후 테스트
		exceptionResolver.setDefaultErrorView("common-error");
		mav = exceptionResolver.resolveException(request, response, handler1, exception);
		assertEquals("common-error", mav.getViewName());
		assertEquals(exception, mav.getModel().get(SimpleMappingExceptionResolver.DEFAULT_EXCEPTION_ATTRIBUTE));
	}

	@Test
	public void testDefaultErrorViewDifferentHandler() {
		// 매핑되는 핸들러가 틀린 경우
		exceptionResolver.setMappedHandlers(Collections.singleton(handler1));
		ModelAndView mav1 = exceptionResolver.resolveException(request, response, handler2, exception);
		// 검증
		assertNull(mav1);
	}

	@Test
	public void testDefaultErrorViewDifferentHandlerClass() {
		// 매핑되는 핸들러 클래스가 다른 경우
		exceptionResolver.setMappedHandlerClasses(new Class[] { String.class });
		ModelAndView mav2 = exceptionResolver.resolveException(request, response, handler2, exception);
		assertNull(mav2);
	}

	@Test
	public void testNullExceptionAttribute() {
		// 디볼드 재수정후 테스트
		exceptionResolver.setDefaultErrorView("common/error");
		exceptionResolver.setExceptionAttribute(null);
		ModelAndView mav3 = exceptionResolver.resolveException(request, response, handler1, exception);
		assertEquals("common/error", mav3.getViewName());
		assertNull(mav3.getModel().get(SimpleMappingExceptionResolver.DEFAULT_EXCEPTION_ATTRIBUTE));
	}

	@Test
	public void testTwoMappings() {
		// ExceptionCase 으로 설정되어 있는 exceptionViewCase View 가져오기
		ModelAndView mav4 = exceptionResolver.resolveException(request, response, handler1, new ExceptionCase1());
		assertEquals("common/exceptionViewCase1", mav4.getViewName());

		ModelAndView mav5 = exceptionResolver.resolveException(request, response, handler1, new ExceptionCase2());
		assertEquals("common/exceptionViewCase2", mav5.getViewName());

	}

	@Test
	public void testNullExceptionMappings() {
		// 별도의 Exception Map 없는 상태에서 default 적용 테스트
		exceptionResolver.setExceptionMappings(null);
		ModelAndView mav6 = exceptionResolver.resolveException(request, response, handler1, exception);
		assertEquals("common/error", mav6.getViewName());
	}

	@Test
	public void testSimpleExceptionMapping() {
		// Exception 발생시 error 뷰 가져오기
		Properties props = new Properties();
		props.setProperty("Exception", "error");
		exceptionResolver.setExceptionMappings(props);
		ModelAndView mav7 = exceptionResolver.resolveException(request, response, handler1, exception);
		assertEquals("error", mav7.getViewName());
	}

	@Test
	public void testExactExceptionMappingWithHandlerSpecified() {
		// Exception 발생시 error 뷰 가져오기
		Properties props = new Properties();
		props.setProperty("java.lang.Exception", "error");
		exceptionResolver.setMappedHandlers(Collections.singleton(handler1));
		exceptionResolver.setExceptionMappings(props);
		ModelAndView mav8 = exceptionResolver.resolveException(request, response, handler1, exception);
		assertEquals("error", mav8.getViewName());
	}

	@Test
	public void testExactExceptionMappingWithHandlerClassSpecified() {
		// Class 타입으로 매핑된 error 뷰 가져오기
		Properties props = new Properties();
		props.setProperty("java.lang.Exception", "error");
		exceptionResolver.setMappedHandlerClasses(new Class[] { String.class });
		exceptionResolver.setExceptionMappings(props);
		ModelAndView mav9 = exceptionResolver.resolveException(request, response, handler1, exception);
		assertEquals("error", mav9.getViewName());
	}

	@Test
	public void testSimpleExceptionMappingWithHandlerSpecifiedButWrongHandler() {
		// 등록된 매핑과 다른 handler 일 경우 null 가져오기
		Properties props = new Properties();
		props.setProperty("Exception", "error");
		exceptionResolver.setMappedHandlers(Collections.singleton(handler1));
		exceptionResolver.setExceptionMappings(props);
		ModelAndView mav10 = exceptionResolver.resolveException(request, response, handler2, exception);
		assertNull(mav10);
	}

	@Test
	public void testSimpleExceptionMappingWithHandlerClassSpecifiedButWrongHandler() {
		// 등록된 매핑과 다른 handler type 일 경우 null 가져오기
		Properties props = new Properties();
		props.setProperty("Exception", "error");
		exceptionResolver.setMappedHandlerClasses(new Class[] { String.class });
		exceptionResolver.setExceptionMappings(props);
		ModelAndView mav11 = exceptionResolver.resolveException(request, response, handler2, exception);
		assertNull(mav11);
	}

	@Test
	public void testTwoMappingsOneShortOneLong() {
		// Exception Mappings 정보를 수정한 후 테스트 하기
		Properties props = new Properties();
		props.setProperty("Exception", "error");
		props.setProperty("AnotherException", "another-error");
		exceptionResolver.setMappedHandlers(Collections.singleton(handler1));
		exceptionResolver.setExceptionMappings(props);
		ModelAndView mav12 = exceptionResolver.resolveException(request, response, handler1, exception);
		assertEquals("error", mav12.getViewName());
	}

	@Test
	public void testThreeMappings() {
		Properties props = new Properties();
		props.setProperty("java.lang.Exception", "error");
		props.setProperty("org.egovframe.rte.bsl.exception.web.ExceptionCase1", "ExceptionViewCase1");
		props.setProperty("org.egovframe.rte.bsl.exception.web.ExceptionCase2", "ExceptionViewCase2");
		exceptionResolver.setMappedHandlers(Collections.singleton(handler1));
		exceptionResolver.setExceptionMappings(props);
		ModelAndView mav13 = exceptionResolver.resolveException(request, response, handler1, new ExceptionCase1());
		assertEquals("ExceptionViewCase1", mav13.getViewName());
	}

}
