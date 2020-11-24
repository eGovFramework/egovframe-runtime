package org.egovframe.rte.ptl.mvc.matrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.MatrixVariableMethodArgumentResolver;

public class MatrixVariableTest {
	private MatrixVariableMethodArgumentResolver resolver;

	private MethodParameter paramString;
	private MethodParameter paramColors;
	private MethodParameter paramYear;

	private ModelAndViewContainer mavContainer;

	private ServletWebRequest webRequest;

	private MockHttpServletRequest request;

	@Before
	public void setUp() throws Exception {
		this.resolver = new MatrixVariableMethodArgumentResolver();

		Method method = getClass().getMethod("handle", String.class, List.class, int.class);
		this.paramString = new MethodParameter(method, 0);
		this.paramColors = new MethodParameter(method, 1);
		this.paramYear = new MethodParameter(method, 2);

		this.paramColors.initParameterNameDiscovery(new LocalVariableTableParameterNameDiscoverer());

		this.mavContainer = new ModelAndViewContainer();
		this.request = new MockHttpServletRequest();
		this.webRequest = new ServletWebRequest(request, new MockHttpServletResponse());

		Map<String, MultiValueMap<String, String>> params = new LinkedHashMap<String, MultiValueMap<String, String>>();
		this.request.setAttribute(HandlerMapping.MATRIX_VARIABLES_ATTRIBUTE, params);
	}

	@Test
	public void supportsParameter() {
		assertFalse(resolver.supportsParameter(paramString));
		assertTrue(resolver.supportsParameter(paramColors));
		assertTrue(resolver.supportsParameter(paramYear));
	}

	@Test
	public void resolveArgument() throws Exception {
		MultiValueMap<String, String> params = getMatrixVariables("cars");
		params.add("colors", "red");
		params.add("colors", "green");
		params.add("colors", "blue");

		assertEquals(Arrays.asList("red", "green", "blue"), this.resolver.resolveArgument(this.paramColors, this.mavContainer, this.webRequest, null));
	}

	@Test
	public void resolveArgumentPathVariable() throws Exception {
		getMatrixVariables("cars").add("year", "2006");

		assertEquals("2006", this.resolver.resolveArgument(this.paramYear, this.mavContainer, this.webRequest, null));
	}

	@Test
	public void resolveArgumentDefaultValue() throws Exception {
		assertEquals("2014", resolver.resolveArgument(this.paramYear, this.mavContainer, this.webRequest, null));
	}

	@Test(expected = ServletRequestBindingException.class)
	public void resolveArgumentMultipleMatches() throws Exception {
		getMatrixVariables("var1").add("colors", "red");
		getMatrixVariables("var2").add("colors", "green");

		this.resolver.resolveArgument(this.paramColors, this.mavContainer, this.webRequest, null);
	}

	@Test(expected = ServletRequestBindingException.class)
	public void resolveArgumentRequired() throws Exception {
		resolver.resolveArgument(this.paramColors, this.mavContainer, this.webRequest, null);
	}

	@Test
	public void resolveArgumentNoMatch() throws Exception {
		MultiValueMap<String, String> params = getMatrixVariables("cars");
		params.add("anotherYear", "2012");

		assertEquals("2012", this.resolver.resolveArgument(this.paramYear, this.mavContainer, this.webRequest, null));
	}

	@SuppressWarnings("unchecked")
	private MultiValueMap<String, String> getMatrixVariables(String pathVarName) {
		Map<String, MultiValueMap<String, String>> matrixVariables = (Map<String, MultiValueMap<String, String>>) this.request.getAttribute(HandlerMapping.MATRIX_VARIABLES_ATTRIBUTE);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		matrixVariables.put(pathVarName, params);

		return params;
	}

	public void handle(String stringArg, @MatrixVariable List<String> colors,
			@MatrixVariable(value = "year", pathVar = "cars", required = false, defaultValue = "2014") int preferredYear) {
	}
}
