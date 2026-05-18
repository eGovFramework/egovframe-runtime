package org.egovframe.rte.ptl.mvc.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HTMLTagFilterTest {

    @Test
    public void HTMLTagFilterDoFilterTest() throws IOException, ServletException {
        HTMLTagFilter tagFilter = new HTMLTagFilter();
        tagFilter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());
    }

    @Test
    public void HTMLTagFilterRequestWrapperTest() {
        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("param01", "param01");
        request.setAttribute("param02", "param02");
        HTMLTagFilterRequestWrapper requestWrapper = new HTMLTagFilterRequestWrapper(request);
        String param = (String) requestWrapper.getAttribute("param01");

        assertEquals("param01", param);
    }

    @Test
    public void getParameterMapShouldNotEscapeRepeatedCallsTwice() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("param01", "<b>test&\"'</b>");
        HTMLTagFilterRequestWrapper requestWrapper = new HTMLTagFilterRequestWrapper(request);

        String[] expected = {"&lt;b&gt;test&amp;&quot;&apos;&lt;/b&gt;"};

        assertArrayEquals(expected, requestWrapper.getParameterMap().get("param01"));
        assertArrayEquals(expected, requestWrapper.getParameterMap().get("param01"));
    }

    @Test
    public void getParameterMapShouldNotMutateOriginalRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("param01", "<b>test</b>");
        HTMLTagFilterRequestWrapper requestWrapper = new HTMLTagFilterRequestWrapper(request);

        Map<String, String[]> parameterMap = requestWrapper.getParameterMap();
        parameterMap.get("param01")[0] = "changed";

        assertEquals("&lt;b&gt;test&lt;/b&gt;", requestWrapper.getParameter("param01"));
    }

    @Test
    public void getParameterMapShouldReturnMutableCopy() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("param01", "<b>test</b>");
        HTMLTagFilterRequestWrapper requestWrapper = new HTMLTagFilterRequestWrapper(request);

        Map<String, String[]> parameterMap = requestWrapper.getParameterMap();
        parameterMap.put("param02", new String[]{"added"});

        assertArrayEquals(new String[]{"added"}, parameterMap.get("param02"));
    }

}
