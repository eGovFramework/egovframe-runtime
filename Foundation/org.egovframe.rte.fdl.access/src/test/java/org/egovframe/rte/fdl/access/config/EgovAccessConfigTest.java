package org.egovframe.rte.fdl.access.config;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.egovframe.rte.fdl.access.bean.AuthorityResourceMetadata;
import org.egovframe.rte.fdl.access.interceptor.EgovAccessUtil;
import org.egovframe.rte.fdl.access.service.EgovUserDetailsHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/context-access.xml","classpath*:META-INF/spring/test-config.xml"})
public class EgovAccessConfigTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovAccessConfigTest.class);

    protected MockHttpServletRequest request;
    protected MockHttpSession session;

    private ApplicationContext context;

    @Before
    public void setUp() throws Exception{
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
    public void test() throws Exception {

        context = new ClassPathXmlApplicationContext(new String[]{"classpath*:META-INF/spring/context-access.xml","classpath*:META-INF/spring/test-config.xml"});

        String[] contextList = context.getBeanDefinitionNames();
        for (String bean : contextList) {
            LOGGER.debug("##### EgovAccessConfigTest context list >>> {} ", context.getBean(bean).getClass());
        }

        String requestMatchType = "regex";
        String url = "/sym/ccm/zip/EgovCcmZipList.do";
        boolean matchStatus = false;
        List<String> authorityList = EgovUserDetailsHelper.getAuthorities();
        LOGGER.debug("##### EgovAccessConfigTest authorityList : {} #####", authorityList);
        String authority = "";
        for (String str : authorityList) {
            authority = str;
        }

        AuthorityResourceMetadata authorityResourceMetadata = context.getBean(AuthorityResourceMetadata.class);
        List<Map<String, Object>> list = authorityResourceMetadata.getResourceMap();
        Iterator<Map<String, Object>> iterator = list.iterator();
        Map<String, Object> tempMap;
        while (iterator.hasNext()) {
            tempMap = iterator.next();
            if (authority.equals(tempMap.get("authority"))) {
                if ("ant".equals(requestMatchType)) {
                    LOGGER.debug("##### EgovAccessConfigTest Ant pattern #####");
                    matchStatus = EgovAccessUtil.antMatcher((String) tempMap.get("url"), url);
                    LOGGER.debug("##### EgovAccessConfigTest ant pattern : {} #####", tempMap.get("url"));
                    LOGGER.debug("##### EgovAccessConfigTest ant url : {} #####", url);
                    LOGGER.debug("##### EgovAccessConfigTest ant match status : {} #####", matchStatus);
                } else {
                    LOGGER.debug("##### EgovAccessConfigTest Regex pattern #####");
                    matchStatus = EgovAccessUtil.regexMatcher((String) tempMap.get("url"), url);
                    LOGGER.debug("##### EgovAccessConfigTest regex pattern : {} #####", tempMap.get("url"));
                    LOGGER.debug("##### EgovAccessConfigTest regex url : {} #####", url);
                    LOGGER.debug("##### EgovAccessConfigTest regex match status : {} #####", matchStatus);
                }
            }
        }

        Assert.assertTrue(matchStatus);

    }

}
