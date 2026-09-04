package org.egovframe.rte.ptl.mvc.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link HTMLTagFilter} 초기화 파라미터의 <b>보안 경계</b> 회귀 검증(CWE-79 입력 필터).
 *
 * <p>기능 테스트({@link HTMLTagFilterTest})가 정상 동작을 다루고, 이 클래스는 세 설정
 * (allowedTags·richTextParameters·excludePaths)이 우회 입력에서 방어를 잃지 않는지 고정한다.</p>
 */
public class HTMLTagFilterSecurityTest {

    private HttpServletRequest wrap(HTMLTagFilter filter, MockHttpServletRequest request)
            throws IOException, ServletException {
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        return (HttpServletRequest) chain.getRequest();
    }

    private HTMLTagFilter filterWith(String name, String value) {
        HTMLTagFilter filter = new HTMLTagFilter();
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter(name, value);
        filter.init(config);
        return filter;
    }

    @Test
    public void 허용_태그를_지정해도_속성_붙은_태그는_통과하지_못한다() throws IOException, ServletException {
        HTMLTagFilter filter = filterWith("allowedTags", "p, br, b");
        MockHttpServletRequest request = new MockHttpServletRequest();
        String[] attacks = {
                "<p onclick=alert(1)>x</p>",
                "<p onclick=\"alert(1)\">x</p>",
                "<b style=\"x:expression(alert(1))\">x</b>",
                "<p id=x>y</p>",
                "<br onfocus=alert(1)>",
                "<p\tonclick=alert(1)>x</p>" };
        for (int i = 0; i < attacks.length; i++) {
            request.setParameter("p" + i, attacks[i]);
        }
        HttpServletRequest filtered = wrap(filter, request);
        for (int i = 0; i < attacks.length; i++) {
            String out = filtered.getParameter("p" + i);
            assertFalse(out.contains("onclick") && out.contains("<p onclick"), "속성 태그 원문 통과: " + out);
            assertTrue(out.startsWith("&lt;"), "속성이 붙은 태그의 여는 꺾쇠는 인코딩돼야 한다: " + out);
        }
    }

    @Test
    public void 허용_태그가_있어도_스크립트_꺾쇠와_앰퍼샌드는_계속_인코딩된다() throws IOException, ServletException {
        HTMLTagFilter filter = filterWith("allowedTags", "p, br");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("a", "<script>alert(1)</script>");
        request.setParameter("b", "&lt;script&gt;");
        request.setParameter("c", "<img src=x onerror=alert(1)>");
        HttpServletRequest filtered = wrap(filter, request);
        assertFalse(filtered.getParameter("a").contains("<script"), "script 원문 통과: " + filtered.getParameter("a"));
        assertTrue(filtered.getParameter("b").startsWith("&amp;lt;"), "앰퍼샌드 미인코딩: " + filtered.getParameter("b"));
        assertFalse(filtered.getParameter("c").contains("<img"), "img 원문 통과: " + filtered.getParameter("c"));
    }

    @Test
    public void 허용_태그_이름은_ASCII_글자만_인정하고_제어문자_섞인_태그는_인코딩된다() throws IOException, ServletException {
        HTMLTagFilter filter = filterWith("allowedTags", "p");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("a", "<p" + (char) 0x00 + ">x");
        request.setParameter("b", "<p" + (char) 0x09 + ">x");
        request.setParameter("c", "<pscript>x");
        HttpServletRequest filtered = wrap(filter, request);
        assertTrue(filtered.getParameter("a").startsWith("&lt;"), "NUL 섞인 태그가 통과: " + filtered.getParameter("a"));
        assertTrue(filtered.getParameter("c").startsWith("&lt;"), "다른 태그가 접두 일치로 통과: " + filtered.getParameter("c"));
    }

    @Test
    public void 리치텍스트_우회_파라미터만_우회하고_다른_파라미터는_인코딩된다() throws IOException, ServletException {
        HTMLTagFilter filter = filterWith("richTextParameters", "nttCn");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("nttCn", "<b>본문</b>");
        request.setParameter("title", "<img src=x onerror=alert(1)>");
        HttpServletRequest filtered = wrap(filter, request);
        assertFalse(filtered.getParameter("title").contains("<img"), "일반 파라미터가 우회됨: " + filtered.getParameter("title"));
        assertTrue(filtered.getParameter("nttCn").contains("<b>"), "우회 파라미터가 인코딩됨: " + filtered.getParameter("nttCn"));
    }

    @Test
    public void 제외_경로는_접미_트릭으로_과도하게_일치하지_않는다() throws IOException, ServletException {
        HTMLTagFilter filter = new HTMLTagFilter();
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("excludePaths", "/api/*, /health");
        filter.init(config);

        // 제외 대상이 "아닌" 경로들은 여전히 래핑(인코딩)돼야 한다 — 접미 트릭으로 필터를 우회할 수 없다
        String[] notExcluded = { "/apix/things", "/health-check", "/board/list", "/health/sub" };
        for (String path : notExcluded) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
            request.setParameter("c", "<b>");
            HttpServletRequest filtered = wrap(filter, request);
            assertInstanceOf(HTMLTagFilterRequestWrapper.class, filtered, path + " 이 래핑을 벗어남");
            assertTrue("&lt;b&gt;".equals(filtered.getParameter("c")), path + " 에서 인코딩 누락: " + filtered.getParameter("c"));
        }
    }

}
