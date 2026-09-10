package org.egovframe.rte.ptl.mvc.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

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

    @Test
    public void getParameterValuesShouldNotEscapeRepeatedCallsTwice() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("param01", "<b>test&\"'</b>");
        HTMLTagFilterRequestWrapper requestWrapper = new HTMLTagFilterRequestWrapper(request);

        String[] expected = {"&lt;b&gt;test&amp;&quot;&apos;&lt;/b&gt;"};

        assertArrayEquals(expected, requestWrapper.getParameterValues("param01"));
        assertArrayEquals(expected, requestWrapper.getParameterValues("param01"));
    }

    @Test
    public void getParameterValuesShouldNotMutateOriginalRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("param01", "<b>test</b>");
        HTMLTagFilterRequestWrapper requestWrapper = new HTMLTagFilterRequestWrapper(request);

        requestWrapper.getParameterValues("param01");

        assertEquals("<b>test</b>", request.getParameter("param01"));
    }

    // ---------------------------------------------------------------- FilterConfig 초기화 파라미터

    private HttpServletRequest filteredRequest(HTMLTagFilter filter, MockHttpServletRequest request)
            throws IOException, ServletException {
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        return (HttpServletRequest) chain.getRequest();
    }

    @Test
    public void 설정이_없으면_기존_동작_그대로_모든_태그를_인코딩한다() throws IOException, ServletException {
        HTMLTagFilter filter = new HTMLTagFilter();
        filter.init(new MockFilterConfig());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("content", "<p>단락</p>");

        assertEquals("&lt;p&gt;단락&lt;/p&gt;", filteredRequest(filter, request).getParameter("content"));
    }

    @Test
    public void 허용_태그는_속성_없는_여닫는_형태만_통과한다() throws IOException, ServletException {
        HTMLTagFilter filter = new HTMLTagFilter();
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("allowedTags", " p , , br ");
        filter.init(config);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("content", "<p>단락</p><br/><BR><br /><script>alert(1)</script>");
        request.addParameter("attack", "<p onclick=x>y</p>");

        HttpServletRequest filtered = filteredRequest(filter, request);
        assertEquals("<p>단락</p><br/><BR><br />&lt;script&gt;alert(1)&lt;/script&gt;",
                filtered.getParameter("content"), "허용 태그(대소문자·자기닫힘 포함)만 통과");
        assertEquals("&lt;p onclick=x&gt;y</p>", filtered.getParameter("attack"),
                "속성이 붙으면 허용 태그라도 통과하지 않는다");
    }

    @Test
    public void 문자열_끝의_허용_태그도_통과한다_공통컴포넌트_사본의_경계_버그_회귀() throws IOException, ServletException {
        // 공통컴포넌트 사본은 값이 허용 태그로 끝나면 &lt;p> 혼종을 만들었다(checkNextWhiteListTag 경계 오류)
        HTMLTagFilter filter = new HTMLTagFilter();
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("allowedTags", "p");
        filter.init(config);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("content", "문단 시작 <p>");

        assertEquals("문단 시작 <p>", filteredRequest(filter, request).getParameter("content"));
    }

    @Test
    public void 허용_태그가_있어도_앰퍼샌드는_계속_인코딩한다_사본의_주석_처리와_대조() throws IOException, ServletException {
        // 공통컴포넌트 사본은 & 인코딩이 주석 처리되어 &lt; 선인코딩 입력이 그대로 통과했다
        HTMLTagFilter filter = new HTMLTagFilter();
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("allowedTags", "p");
        filter.init(config);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("content", "&lt;script&gt;");

        assertEquals("&amp;lt;script&amp;gt;", filteredRequest(filter, request).getParameter("content"));
    }

    @Test
    public void 리치텍스트_파라미터는_인코딩을_우회하고_나머지는_인코딩한다() throws IOException, ServletException {
        HTMLTagFilter filter = new HTMLTagFilter();
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("richTextParameters", "nttCn");
        filter.init(config);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("nttCn", "<b>본문&\"'</b>");
        request.addParameter("title", "<b>");

        HttpServletRequest filtered = filteredRequest(filter, request);
        assertEquals("<b>본문&\"'</b>", filtered.getParameter("nttCn"), "우회 파라미터는 원문 유지");
        assertEquals("&lt;b&gt;", filtered.getParameter("title"), "그 외 파라미터는 종전대로 인코딩");
        assertArrayEquals(new String[]{"<b>본문&\"'</b>"}, filtered.getParameterValues("nttCn"));
        assertArrayEquals(new String[]{"<b>본문&\"'</b>"}, filtered.getParameterMap().get("nttCn"));
    }

    @Test
    public void 리치텍스트_우회도_원본_배열을_노출하지_않는다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("nttCn", "<b>본문</b>");
        HTMLTagFilterRequestWrapper wrapper = new HTMLTagFilterRequestWrapper(
                request, java.util.Collections.emptySet(), java.util.Collections.singleton("nttCn"));

        wrapper.getParameterValues("nttCn")[0] = "변조";

        assertEquals("<b>본문</b>", wrapper.getParameter("nttCn"), "반환 배열을 바꿔도 원본은 그대로");
    }

    @Test
    public void 제외_경로는_래핑_없이_통과한다() throws IOException, ServletException {
        HTMLTagFilter filter = new HTMLTagFilter();
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("excludePaths", "/api/*, /health");
        filter.init(config);

        MockHttpServletRequest apiRequest = new MockHttpServletRequest("POST", "/api/things");
        apiRequest.addParameter("content", "<b>");
        assertSame(apiRequest, filteredRequest(filter, apiRequest), "접두 일치 경로는 원 요청 그대로");

        MockHttpServletRequest healthRequest = new MockHttpServletRequest("GET", "/health");
        assertSame(healthRequest, filteredRequest(filter, healthRequest), "정확 일치 경로도 제외");

        MockHttpServletRequest boardRequest = new MockHttpServletRequest("POST", "/board/list");
        boardRequest.addParameter("content", "<b>");
        HttpServletRequest filtered = filteredRequest(filter, boardRequest);
        assertInstanceOf(HTMLTagFilterRequestWrapper.class, filtered, "그 외 경로는 래핑");
        assertEquals("&lt;b&gt;", filtered.getParameter("content"));
    }

}
