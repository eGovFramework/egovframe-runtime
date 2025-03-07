package org.egovframe.rte.fdl.access.config;

import org.egovframe.rte.fdl.access.interceptor.EgovAccessUtil;
import org.egovframe.rte.fdl.access.service.EgovUserDetailsHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/*.xml"})
public class EgovAccessConfigTest {
    protected MockHttpServletRequest request;
    protected MockHttpSession session;

    @Before
    public void setUp() {
        session = new MockHttpSession();
        session.setAttribute("accessUser","GNRUSER");

        request = new MockHttpServletRequest();
        request.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @After
    public void clear(){
        session.clearAttributes();
        session = null;
    }

    @Test
    public void test() {
        String requestMatchType = "regex";

        String authUrl = "/sym/ccm/zip/EgovCcmZipList.do";
        Assert.assertTrue(authCheck(requestMatchType, authUrl));
    }

    public Boolean authCheck(String requestMatchType, String url) {
        boolean authCheck = false;
        List<String> authList = EgovUserDetailsHelper.getAuthorities();
        List<Map<String, Object>> rolesList = EgovUserDetailsHelper.getRoles();

        if (!ObjectUtils.isEmpty(authList) && !ObjectUtils.isEmpty(rolesList)) {
            List<String> urlList = new ArrayList<>();
            List<String> roleList = new ArrayList<>();
            for (String auth : authList) {
                Iterator<Map<String, Object>> iterator = rolesList.iterator();
                Map<String, Object> roleMap;
                while (iterator.hasNext()) {
                    roleMap = iterator.next();
                    if (auth.equals(roleMap.get("authority"))) {
                        roleList.add((String) roleMap.get("url"));
                    }
                }
            }

            urlList = roleList.stream().distinct().collect(Collectors.toList());

            for (String authUrl : urlList) {
                if ("ant".equals(requestMatchType)) {
                    authCheck = EgovAccessUtil.antMatcher(authUrl, url);
                } else {
                    authCheck = EgovAccessUtil.regexMatcher(authUrl, url);
                }
                if (authCheck) break;
            }
        }

        return authCheck;
    }

}
