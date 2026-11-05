package org.egovframe.rte.fdl.access.interceptor;

import org.egovframe.rte.fdl.access.bean.AuthorityResourceMetadata;
import org.egovframe.rte.fdl.access.config.EgovAccessConfigShare;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 접근제어 인터셉터가 요청 경로를 정규화한 뒤 인가 패턴과 비교하는지 검증한다(경로 조작 우회 차단).
 */
public class EgovAccessInterceptorSecurityTest {

    private static final String CONTEXT_PATH = "/app";

    private final EgovAccessInterceptor interceptor = new EgovAccessInterceptor();

    private String previousMatchType;

    @Before
    public void setUp() {
        previousMatchType = EgovAccessConfigShare.DEF_REQUEST_MATCH_TYPE;
        EgovAccessConfigShare.DEF_REQUEST_MATCH_TYPE = "regex";

        List<Map<String, Object>> authorityList = new ArrayList<Map<String, Object>>();
        Map<String, Object> authority = new HashMap<String, Object>();
        authority.put("userid", "user");
        authority.put("authority", "ROLE_USER");
        authorityList.add(authority);

        List<Map<String, Object>> resourceMap = new ArrayList<Map<String, Object>>();
        Map<String, Object> resource = new HashMap<String, Object>();
        resource.put("authority", "ROLE_USER");
        resource.put("url", "^/public/.*");
        resourceMap.add(resource);

        new AuthorityResourceMetadata(authorityList, resourceMap);
    }

    @After
    public void tearDown() {
        EgovAccessConfigShare.DEF_REQUEST_MATCH_TYPE = previousMatchType;
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void 허용_경로는_통과한다() throws Exception {
        assertTrue(preHandle("/public/list.do"));
    }

    @Test
    public void 상위_이동으로_허용_패턴을_우회할_수_없다() throws Exception {
        for (String path : new String[]{"/public/../admin/user.do", "/public/%2e%2e/admin/user.do",
                "/public/%2E%2E/admin/user.do", "/public/./../admin/user.do"}) {
            assertFalse(path, preHandle(path));
        }
    }

    @Test
    public void 거부되면_접근거부_URL_로_이동한다() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        interceptor.preHandle(request("/public/../admin/user.do"), response, null);
        assertEquals(CONTEXT_PATH + EgovAccessConfigShare.DEF_ACCESS_DENIED_URL, response.getRedirectedUrl());
    }

    @Test
    public void 경로_중간의_컨텍스트_경로_문자열은_지우지_않는다() throws Exception {
        // 종전 String.replace(contextPath, "") 는 "/public/app/x" 를 "/public/x" 로 바꿔 경로를 왜곡했다
        assertTrue(preHandle("/public" + CONTEXT_PATH + "/x.do"));
    }

    private boolean preHandle(String path) throws Exception {
        return interceptor.preHandle(request(path), new MockHttpServletResponse(), null);
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", CONTEXT_PATH + path);
        request.setContextPath(CONTEXT_PATH);
        request.getSession().setAttribute("loginVO", new Object());
        request.getSession().setAttribute("accessUser", "user");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        return request;
    }

}
