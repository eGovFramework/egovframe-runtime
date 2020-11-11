package egovframework.rte.fdl.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import egovframework.rte.fdl.security.userdetails.EgovUserDetailsVO;
import egovframework.rte.fdl.security.userdetails.util.EgovUserDetailsHelper;
import egovframework.rte.fdl.security.web.CategoryController;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.method.DelegatingMethodSecurityMetadataSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.BeanIds;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;


/**
 * @author sjyoon
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath*:META-INF/spring/context-common.xml",
    "classpath*:META-INF/spring/context-datasource-jdbc.xml"
    })
public class EgovSecurityServiceTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovSecurityServiceTest.class);

	@Autowired
	private ApplicationContext context;

    @Resource(name = "dataSource")
    private DataSource dataSource;

    private boolean isHsql = true;

    @Resource(name = "jdbcProperties")
    private Properties jdbcProperties;


    @Before
    public void onSetUp() throws Exception {
    	LOGGER.debug("###### EgovSecurityServiceTest.onSetUp START ######");

    	isHsql = "hsql".equals(jdbcProperties.getProperty("usingDBMS"));

    	if (isHsql) {
			JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource("META-INF/testdata/sample_schema_hsql.sql"), true);
        }

    	// 테이블 생성 후 테스트를 위하여 여기서 처리
		context = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/context-*.xml");

		LOGGER.debug("###### EgovSecurityServiceTest.onSetUp END ######");

    }

    @After
    public void onTearDown() throws Exception {

    	LOGGER.debug("###### EgovSecurityServiceTest.onTearDown START ######");

        isHsql = "hsql".equals(jdbcProperties.getProperty("usingDBMS"));

        if (isHsql) {
			JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource("META-INF/testdata/sample_schema_hsql_drop.sql"), true);
        }

        LOGGER.debug("###### EgovSecurityServiceTest.onTearDown END ######");

    	SecurityContextHolder.clearContext();
    }

	private <T extends Filter> T getSecurityFilter(Class<T> type) {

		Map<String, DefaultSecurityFilterChain> filterChainMap = context.getBeansOfType(DefaultSecurityFilterChain.class);

		for (DefaultSecurityFilterChain filterChain : filterChainMap.values()) {
			for (Filter filter : filterChain.getFilters()) {
				if (type.isInstance(filter)) {
					return type.cast(filter);
				}
			}
		}

		throw new NoSuchBeanDefinitionException("No bean of type [" + type.getName() + "] is defined.");
	}

    @Test
    public void testBeanList() {
    	String[] list = context.getBeanDefinitionNames();

    	for (String bean : list) {
    		System.out.println("===> " + bean + " : " + context.getBean(bean).getClass());

    		if (context.getBean(bean) instanceof DefaultSecurityFilterChain) {
    			DefaultSecurityFilterChain filterChain = (DefaultSecurityFilterChain) context.getBean(bean);

    			List<Filter> filters = filterChain.getFilters();

    			for (Filter filter : filters) {

    				System.out.println("======> " + filter + " : ");
    			}
    		}
    	}
    }

    @Test
    public void testGetSecurityFilter() {
    		LogoutFilter logout  = getSecurityFilter(LogoutFilter.class);

    		assertNotNull(logout);
    }

    /**
     * DB에 사용자 정보(id/password)를 유지하여 인증처리 함.
     * DB에 등록된 사용자의 인증 확인 테스트
     * @throws Exception
     */
    @Test
    public void testAllowAccessForAuthorizedUser() throws Exception {

		UsernamePasswordAuthenticationToken login = new UsernamePasswordAuthenticationToken("jimi", "jimi");
		AuthenticationManager authManager = (AuthenticationManager) context.getBean(BeanIds.AUTHENTICATION_MANAGER);

		SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
		LOGGER.debug("### jimi's password is right!!");

		///////////
		login = new UsernamePasswordAuthenticationToken("test", "test");
		authManager = (AuthenticationManager) context.getBean(BeanIds.AUTHENTICATION_MANAGER);

		SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
		LOGGER.debug("### test's password is right!!");

		///////////
		login = new UsernamePasswordAuthenticationToken("user", "user");
		authManager = (AuthenticationManager) context.getBean(BeanIds.AUTHENTICATION_MANAGER);

		SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
		LOGGER.debug("### user's password is right!!");

		///////////
		login = new UsernamePasswordAuthenticationToken("buyer", "buyer");
		authManager = (AuthenticationManager) context.getBean(BeanIds.AUTHENTICATION_MANAGER);

		SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));
		LOGGER.debug("### buyer's password is right!!");

    }

    /**
     * DB에 등록된 사용자의 인증 실패 테스트
     * @throws Exception
     */
    @Test(expected=BadCredentialsException.class)
    public void testRejectAccessForUnauthorizedUser() throws Exception {

       UsernamePasswordAuthenticationToken login = new UsernamePasswordAuthenticationToken("jimi", "wrongpw");
		AuthenticationManager authManager = (AuthenticationManager) context.getBean(BeanIds.AUTHENTICATION_MANAGER);

		LOGGER.debug("### jimi's password is wrong!!");
       SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));

    }

    /**
     * 메소드 수행이 허용된 메소드 실행 시 성공 테스트
     * @throws Exception
     */
    @Test
    public void testMethodAndRoleMapping() throws Exception {

    	DelegatingMethodSecurityMetadataSource definitionsource = (DelegatingMethodSecurityMetadataSource) context.getBean("delegatingMethodSecurityMetadataSource");
    	Method method = null;
    	Collection<ConfigAttribute> role = null;

    	// test1 : matched role
    	try {
    		method = CategoryController.class.getMethod("selectCategoryList", (Class<?>[])null);
    	} catch (NoSuchMethodException nsme) {
    		LOGGER.error("## testMethodAndRoleMapping : {}", nsme);
    	}

   		role = definitionsource.getAttributes(method, CategoryController.class);

   		assertEquals("ROLE_USER", role.toArray()[0].toString());
   		LOGGER.debug("## testMethodAndRoleMapping : {} is {}", method.getName(), role.toArray()[0].toString());

    }

    /**
     * 메소드 수행이 허용되지 않은 메소드 실행 시 실패 테스트
     * @throws Exception
     */
    @Test
    public void testFailedMethodAndRoleMapping() throws Exception {

    	DelegatingMethodSecurityMetadataSource definitionsource = (DelegatingMethodSecurityMetadataSource) context.getBean("delegatingMethodSecurityMetadataSource");
    	Method method = null;
    	Collection<ConfigAttribute> role = null;

    	// test1 : no matched role
    	try {
    		method = CategoryController.class.getMethod("addCategoryView", (Class<?>[])null);
    	} catch (NoSuchMethodException nsme) {
    		LOGGER.error("## testMethodAndRoleMapping : {}", nsme);
    	}

   		role = definitionsource.getAttributes(method, CategoryController.class);

   		assertEquals(0, role.size());
   		LOGGER.debug("## testMethodAndRoleMapping : {} is no roles", method.getName());
    }

    /**
     * 웹 URL 접근 제어 권한에 따른 Role 맵핑을 처리함.
     * 웹 접근이 허용된 URL로 접근 시 성공 테스트
     * @throws Exception
     */
    @Test
    public void testURLAndRoleMapping() throws Exception {

    	FilterSecurityInterceptor interceptor = (FilterSecurityInterceptor) context.getBean("filterSecurityInterceptor");
        FilterInvocationSecurityMetadataSource definitionsource = interceptor.getSecurityMetadataSource();

        // "/test.do" ROLE_USER
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI(null);

        request.setServletPath("/test.do");

		FilterInvocation filterInvocation = new FilterInvocation(request, new MockHttpServletResponse(), new MockFilterChain());

        Collection<ConfigAttribute> attrs = definitionsource.getAttributes(filterInvocation);

        LOGGER.debug("### Pattern Matched url size is {} and Roles are {}", attrs.size(), attrs);
        assertTrue(attrs.contains(new SecurityConfig("ROLE_USER")));

        // "/sale/index.do" ROLE_RESTRICTED
        request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI(null);

        request.setServletPath("/sale/index.do");

		filterInvocation = new FilterInvocation(request, new MockHttpServletResponse(), new MockFilterChain());

        attrs = definitionsource.getAttributes(filterInvocation);

        LOGGER.debug("### Pattern Matched url size is {} and Roles are", attrs.size(), attrs);
        assertTrue(attrs.contains(new SecurityConfig("ROLE_RESTRICTED")));

    }

    /**
     * 웹 접근이 허용되지 않은 URL로 접근 시 실패 테스트
     * @throws Exception
     */
    @Test
    public void testFailedURLAndRoleMapping() throws Exception {

        FilterSecurityInterceptor interceptor = (FilterSecurityInterceptor) context.getBean("filterSecurityInterceptor");
        FilterInvocationSecurityMetadataSource definitionsource = interceptor.getSecurityMetadataSource();

        // "/test.do" ROLE_USER
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI(null);

        request.setServletPath("/index.do");

		FilterInvocation filterInvocation = new FilterInvocation(request, new MockHttpServletResponse(), new MockFilterChain());

		Collection<ConfigAttribute> attrs = definitionsource.getAttributes(filterInvocation);

		LOGGER.debug("### Pattern Matched url is none");
        assertNull(attrs);

    }


    /**
     * 웹 접근이 허용된 URL로 접근 시 Context 에서 지정한 로그인 화면으로 이동됨 검사
     * @throws Exception
     */
    @Test
    public void testSuccessfulUrlInvocation() throws Exception {

    	final String loginPage = "/cvpl/EgovCvplLogin.do";

    	FilterChainProxy filterChainProxy = (FilterChainProxy) context.getBean(BeanIds.SPRING_SECURITY_FILTER_CHAIN);
    	//FilterChainProxy filterChainProxy = (FilterChainProxy) context.getBean(BeanIds.FILTER_CHAIN_PROXY);

    	////////////////
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setServletPath("/test.do");

    	MockHttpServletResponse response = new MockHttpServletResponse();
    	MockFilterChain chain = new MockFilterChain();

    	filterChainProxy.doFilter(request, response, chain);

    	assertTrue(response.getRedirectedUrl().indexOf(loginPage) >= 0);
    	LOGGER.debug("### getRedirectedUrl {}", response.getRedirectedUrl());
    	LOGGER.debug("### getForwardedUrl {}", response.getForwardedUrl());
    	LOGGER.debug("### getIncludedUrl {}", response.getIncludedUrl());
    	LOGGER.debug("### getErrorMessage {}", response.getErrorMessage());
    	LOGGER.debug("### getContentAsString {}", response.getContentAsString());


    	/////////////
    	request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setServletPath("/sale/index.do");

    	response = new MockHttpServletResponse();

    	filterChainProxy.doFilter(request, response, chain);

    	assertTrue(response.getRedirectedUrl().indexOf(loginPage) >= 0);
    	LOGGER.debug("### getRedirectedUrl {}", response.getRedirectedUrl());
    	LOGGER.debug("### getForwardedUrl {}", response.getForwardedUrl());
    	LOGGER.debug("### getIncludedUrl {}", response.getIncludedUrl());
    	LOGGER.debug("### getErrorMessage {}", response.getErrorMessage());
    	LOGGER.debug("### getContentAsString {}", response.getContentAsString());

    }

    /**
     * 웹 접근이 허용되지 않은 URL로 접근 시 Context 에서 지정한 로그인 화면으로 이동되지 않음 검사
     * @throws Exception
     */
    @Test
    public void testFailureUrlInvocation() throws Exception {

    	//final String loginPage = "/cvpl/EgovCvplLogin.do";

    	FilterChainProxy filterChainProxy = (FilterChainProxy) context.getBean(BeanIds.FILTER_CHAIN_PROXY);


    	////////////////
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setServletPath("/index.do");

    	MockHttpServletResponse response = new MockHttpServletResponse();
    	MockFilterChain chain = new MockFilterChain();

    	filterChainProxy.doFilter(request, response, chain);

    	assertNull(response.getRedirectedUrl());
    	LOGGER.debug("### getRedirectedUrl is null");


    	////////////////
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setServletPath("/sale/index.doit");

    	response = new MockHttpServletResponse();
    	chain = new MockFilterChain();

    	filterChainProxy.doFilter(request, response, chain);

    	assertNull(response.getRedirectedUrl());
    	LOGGER.debug("### getRedirectedUrl is null");

    }

    /**
     * 세션처리를 위한 UserDetails 확장 테스트
     * @throws Exception
     */
    @Test
    public void testUserDetailsExt() throws Exception {

        // 인증되지 않은 사용자 체크
    	Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    	assertFalse(isAuthenticated.booleanValue());
    	LOGGER.debug("### testUserDetailsExt 인증 : {}", isAuthenticated.booleanValue());

    	EgovUserDetailsVO user = (EgovUserDetailsVO)EgovUserDetailsHelper.getAuthenticatedUser();
    	assertNull(user);
    	LOGGER.debug("### testUserDetailsExt 사용자정보 : {}", user);

    	// 로그인 jimi
    	UsernamePasswordAuthenticationToken login = new UsernamePasswordAuthenticationToken("jimi", "jimi");
		AuthenticationManager authManager = (AuthenticationManager) context.getBean(BeanIds.AUTHENTICATION_MANAGER);

        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));

        // 인증된 사용자 검증
    	isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    	assertTrue(isAuthenticated.booleanValue());
    	LOGGER.debug("### testUserDetailsExt 인증 : {}", isAuthenticated.booleanValue());

        // 검증
        // ID : jimi
        user = (EgovUserDetailsVO)EgovUserDetailsHelper.getAuthenticatedUser();

        assertNotNull(user);
        assertEquals("jimi", user.getUserId());
        assertEquals("jimi test", user.getUserName());
        assertEquals("19800604", user.getBirthDay());
        assertEquals("1234567890123", user.getSsn());
        LOGGER.debug("### testUserDetailsExt 사용자 : {}", user.getUserId());

    	// 로그인
        login = new UsernamePasswordAuthenticationToken("test", "test");
		authManager = (AuthenticationManager) context.getBean(BeanIds.AUTHENTICATION_MANAGER);

        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));

        // 인증된 사용자 검증
    	isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    	assertTrue(isAuthenticated.booleanValue());


        // ID : test
        user = (EgovUserDetailsVO)EgovUserDetailsHelper.getAuthenticatedUser();

        assertNotNull(user);
        assertEquals("test", user.getUserId());
        assertEquals("Kim, Young-Su", user.getUserName());
        assertEquals("19800604", user.getBirthDay());
        assertEquals("1234567890123", user.getSsn());
        LOGGER.debug("### testUserDetailsExt 사용자 : {}", user.getUserId());

    	// 로그인
    	login = new UsernamePasswordAuthenticationToken("user", "user");
		authManager = (AuthenticationManager) context.getBean(BeanIds.AUTHENTICATION_MANAGER);

        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));

        // 인증된 사용자 검증
    	isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    	assertTrue(isAuthenticated.booleanValue());


        // ID : test
        user = (EgovUserDetailsVO)EgovUserDetailsHelper.getAuthenticatedUser();

        assertNotNull(user);
        assertEquals("user", user.getUserId());
        assertEquals("Hong Gil-dong", user.getUserName());
        assertEquals("19800603", user.getBirthDay());
        assertEquals("8006041227717", user.getSsn());
        LOGGER.debug("### testUserDetailsExt 사용자 : {}", user.getUserId());

    	// 로그인
    	login = new UsernamePasswordAuthenticationToken("buyer", "buyer");
		authManager = (AuthenticationManager) context.getBean(BeanIds.AUTHENTICATION_MANAGER);

        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));

        // 인증된 사용자 검증
    	isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    	assertTrue(isAuthenticated.booleanValue());

        // ID : buyer
        user = (EgovUserDetailsVO)EgovUserDetailsHelper.getAuthenticatedUser();

        assertEquals("buyer", user.getUserId());
        assertEquals("Lee, Man-hong", user.getUserName());
        assertEquals("19701231", user.getBirthDay());
        assertEquals("1234567890123", user.getSsn());
        LOGGER.debug("### testUserDetailsExt 사용자 : {}", user.getUserId());

    }

    /**
     * 지정된 Role 조회 테스트
     * @throws Exception
     */
    @Test
    public void testAuthoritiesAndRoleHierarchy() throws Exception {
    	// user User : ROLE_USER
    	UsernamePasswordAuthenticationToken login = new UsernamePasswordAuthenticationToken("user", "user");
		AuthenticationManager authManager = (AuthenticationManager) context.getBean(BeanIds.AUTHENTICATION_MANAGER);

        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));

        LOGGER.debug("DEBUG : {}", EgovUserDetailsHelper.getAuthorities());

    	List<String> authorities = EgovUserDetailsHelper.getAuthorities();

    	// 1. authorites 에  ROLE_USER 권한이 있는지 체크 TRUE/FALSE
    	LOGGER.debug("########### user ROLES are {}", authorities);
    	assertTrue(authorities.contains("ROLE_USER"));
    	assertTrue(authorities.contains("ROLE_RESTRICTED"));
    	assertTrue(authorities.contains("IS_AUTHENTICATED_ANONYMOUSLY"));
    	assertTrue(authorities.contains("IS_AUTHENTICATED_FULLY"));
    	assertTrue(authorities.contains("IS_AUTHENTICATED_REMEMBERED"));


    	// 2. authorites 에  ROLE 이 여러개 설정된 경우
    	for (Iterator<String> it = authorities.iterator(); it.hasNext();) {
    		String auth = it.next();
    		LOGGER.debug("########### user ROLE is {}", auth);
    	}

    	// 3. authorites 에  ROLE 이 하나만 설정된 경우
    	String auth = (String) authorities.toArray()[0];
    	LOGGER.debug("########### user ROLE is {}", auth);


    	// buyer USER : ROLE_RESTRICTED
    	login = new UsernamePasswordAuthenticationToken("buyer", "buyer");
		authManager = (AuthenticationManager) context.getBean(BeanIds.AUTHENTICATION_MANAGER);

        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));

        LOGGER.debug("DEBUG : {}", EgovUserDetailsHelper.getAuthorities());

    	authorities = EgovUserDetailsHelper.getAuthorities();

    	LOGGER.debug("########### buyer ROLES are {}", authorities);

    	assertFalse(authorities.contains("ROLE_USER"));
    	assertFalse(authorities.contains("ROLE_ADMIN"));
    	assertTrue(authorities.contains("ROLE_RESTRICTED"));
    	assertTrue(authorities.contains("IS_AUTHENTICATED_ANONYMOUSLY"));
    	assertTrue(authorities.contains("IS_AUTHENTICATED_FULLY"));
    	assertTrue(authorities.contains("IS_AUTHENTICATED_REMEMBERED"));

    	// test USER : ROLE_ADMIN
    	login = new UsernamePasswordAuthenticationToken("test", "test");
		authManager = (AuthenticationManager) context.getBean(BeanIds.AUTHENTICATION_MANAGER);

        SecurityContextHolder.getContext().setAuthentication(authManager.authenticate(login));

        LOGGER.debug("DEBUG : {}", EgovUserDetailsHelper.getAuthorities());

    	authorities = EgovUserDetailsHelper.getAuthorities();

    	LOGGER.debug("########### test ROLES are {}", authorities);

    	assertTrue(authorities.contains("ROLE_USER"));
    	assertTrue(authorities.contains("ROLE_ADMIN"));
    	assertTrue(authorities.contains("ROLE_RESTRICTED"));
    	assertTrue(authorities.contains("IS_AUTHENTICATED_ANONYMOUSLY"));
    	assertTrue(authorities.contains("IS_AUTHENTICATED_FULLY"));
    	assertTrue(authorities.contains("IS_AUTHENTICATED_REMEMBERED"));
    }

}

