package org.egovframe.boot.security;

import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.security.bean.*;
import org.egovframe.boot.security.secureobject.impl.SecuredObjectDAO;
import org.egovframe.boot.security.secureobject.impl.SecuredObjectServiceImpl;
import org.egovframe.boot.security.userdetails.hierarchicalroles.HierarchyStringsFactoryBean;
import org.egovframe.boot.security.userdetails.jdbc.EgovJdbcUserDetailsManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.*;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.*;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(EgovSecurityProperties.class)
@ConditionalOnProperty(prefix="Globals", name="auth", havingValue="security")
@Slf4j
public class EgovSecurityConfiguration {

    private final EgovSecurityProperties properties;

    public EgovSecurityConfiguration(EgovSecurityProperties properties) {
        this.properties = properties;
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(jdbcUserService());
        authenticationProvider.setPasswordEncoder(passwordEncoder(properties.getHash(), properties.isHashBase64()));
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (!properties.isCsrf()) {
            http.csrf().disable();
        }

        if (!properties.isSniff()) {
            http.headers().defaultsDisabled();
        }

        http.headers()
                .frameOptions()
                .disable()
                .addHeaderWriter(new StaticHeadersWriter("X-FRAME-OPTIONS", properties.getXframeOptions()));

        if (properties.isXssProtection()) {
            http.headers().xssProtection().xssProtectionEnabled(true).block(true);
        }

        if (properties.isCacheControl()) {
            http.headers().cacheControl().disable();
        }

        http.anonymous();

        http.requestMatcher(requestMatcherTypeFactoryBean().getObject());

        String[] excludeUrl = Stream.of(properties.getExcludeList().split(",")).map(String::trim).toArray(String[]::new);
        for (String pattern : excludeUrl) {
            http.authorizeRequests().antMatchers(pattern).permitAll();
        }

        http.authorizeRequests().anyRequest().authenticated();

        http.formLogin()
                .loginPage(properties.getLoginUrl())
                .usernameParameter("egov_security_username")
                .passwordParameter("egov_security_password")
                .successForwardUrl(properties.getDefaultTargetUrl())
                .failureHandler(egovLoginFailHandler());

        http.logout()
                .logoutUrl(properties.getLogoutUrl())
                .logoutSuccessUrl(properties.getLogoutSuccessUrl())
                .clearAuthentication(true);

        http.sessionManagement()
                .sessionAuthenticationStrategy(sas())
                .enableSessionUrlRewriting(false);

        http.addFilterBefore(filterSecurityInterceptor(), FilterSecurityInterceptor.class);
        http.addFilterAt(concurrencyFilter(), ConcurrentSessionFilter.class);
        http.httpBasic().authenticationEntryPoint(loginUrlAuthenticationEntryPoint());
        http.exceptionHandling().accessDeniedHandler(egovAccessDeniedHandler());

        return http.build();
    }

    public PasswordEncoder passwordEncoder(String hash, boolean isHashBase64) {
        if ("plaintext".equals(hash)) {
            return NoOpPasswordEncoder.getInstance();
        } else if ("noop".equals(hash)) {
            return NoOpPasswordEncoder.getInstance();
        } else if ("md5".equals(hash)) {
            return new MessageDigestPasswordEncoder("MD5");
        } else if ("sha".equals(hash)) {
            return new MessageDigestPasswordEncoder("SHA-1");
        } else if ("sha-256".equals(hash)) {
            return new MessageDigestPasswordEncoder("SHA-256");
        } else if ("sha256".equals(hash)) {
            return new StandardPasswordEncoder();
        } else if ("ldap".equals(hash)) {
            return new LdapShaPasswordEncoder();
        } else {
            return new BCryptPasswordEncoder();
        }
    }

    @Bean
    public DataSourceFactoryBean dataSourceFactoryBean() {
        return new DataSourceFactoryBean(properties);
    }

    @Bean
    public EgovLoginFailHandler egovLoginFailHandler() {
        return new EgovLoginFailHandler(properties);
    }

    @Bean
    public RequestMatcherTypeFactoryBean requestMatcherTypeFactoryBean() {
        return new RequestMatcherTypeFactoryBean(properties);
    }

    @Bean
    public EgovAccessDeniedHandler egovAccessDeniedHandler() throws Exception {
        return new EgovAccessDeniedHandler(properties.getAccessDeniedUrl(), properties.getCsrfAccessDeniedUrl());
    }

    @Bean
    public LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint() throws Exception {
        return new LoginUrlAuthenticationEntryPoint(properties.getLoginUrl());
    }

    @Bean
    public ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlAuthenticationStrategy() throws Exception {
        ConcurrentSessionControlAuthenticationStrategy authenticationStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
        authenticationStrategy.setMaximumSessions(properties.getConcurrentMaxSessons());
        authenticationStrategy.setExceptionIfMaximumExceeded(properties.isErrorIfMaximumExceeded());
        return authenticationStrategy;
    }

    @Bean
    public SessionFixationProtectionStrategy sessionFixationProtectionStrategy() {
        return new SessionFixationProtectionStrategy();
    }

    @Bean
    public SessionRegistryImpl sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public ConcurrentSessionFilter concurrencyFilter() {
        return new ConcurrentSessionFilter(sessionRegistry());
    }

    @Bean
    public RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(sessionRegistry());
    }

    @Bean
    public CompositeSessionAuthenticationStrategy sas() throws Exception {
        List<SessionAuthenticationStrategy> list = new ArrayList<>();
        list.add(concurrentSessionControlAuthenticationStrategy());
        list.add(sessionFixationProtectionStrategy());
        list.add(registerSessionAuthenticationStrategy());
        return new CompositeSessionAuthenticationStrategy(list);
    }

    @Bean
    public SecuredObjectDAO securedObjectDAO() throws Exception {
        SecuredObjectDAO securedObjectDAO = new SecuredObjectDAO();
        securedObjectDAO.setDataSource(Objects.requireNonNull(dataSourceFactoryBean().getObject()));
        return securedObjectDAO;
    }

    @Bean
    public SecuredObjectServiceImpl securedObjectService() throws Exception {
        SecuredObjectServiceImpl securedObjectService = new SecuredObjectServiceImpl(properties);
        securedObjectService.setSecuredObjectDAO(securedObjectDAO());
        return securedObjectService;
    }

    @Bean
    public HierarchyStringsFactoryBean hierarchyStrings() throws Exception {
        HierarchyStringsFactoryBean hierarchyStrings = new HierarchyStringsFactoryBean(properties);
        hierarchyStrings.setSecuredObjectService(securedObjectService());
        return hierarchyStrings;
    }

    @Bean
    public UrlResourcesMapFactoryBean requestMap() throws Exception {
        UrlResourcesMapFactoryBean requestMap = new UrlResourcesMapFactoryBean();
        requestMap.setSecuredObjectService(securedObjectService());
        return requestMap;
    }

    @Bean
    public RoleHierarchyImpl roleHierarchy() throws Exception {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(hierarchyStrings().getObject());
        return roleHierarchy;
    }

    @Bean
    public EgovJdbcUserDetailsManager jdbcUserService() throws Exception {
        EgovJdbcUserDetailsManager jdbcUserService = new EgovJdbcUserDetailsManager();
        jdbcUserService.setRoleHierarchy(roleHierarchy());
        jdbcUserService.setDataSource(Objects.requireNonNull(dataSourceFactoryBean().getObject()));
        jdbcUserService.setUsersByUsernameQuery(properties.getJdbcUsersByUsernameQuery());
        jdbcUserService.setAuthoritiesByUsernameQuery(properties.getJdbcAuthoritiesByUsernameQuery());
        jdbcUserService.setMapClass(properties.getJdbcMapClass());
        return jdbcUserService;
    }

    @Bean
    public RoleVoter roleVoter() {
        RoleVoter roleVoter = new RoleVoter();
        roleVoter.setRolePrefix("");
        return roleVoter;
    }

    @Bean
    public AuthenticatedVoter authenticatedVoter() {
        return new AuthenticatedVoter();
    }

    @Bean
    public AffirmativeBased accessDecisionManager() {
        List<AccessDecisionVoter<?>> list = new ArrayList<>();
        list.add(roleVoter());
        list.add(authenticatedVoter());
        AffirmativeBased accessDecisionManager = new AffirmativeBased(list);
        accessDecisionManager.setAllowIfAllAbstainDecisions(false);
        return accessDecisionManager;
    }

    @Bean
    public EgovReloadableFilterInvocationSecurityMetadataSource databaseSecurityMetadataSource() throws Exception {
        LinkedHashMap<RequestMatcher, List<ConfigAttribute>> list = new LinkedHashMap<>(requestMap().getObject());
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> collect = new LinkedHashMap<>();
        for (RequestMatcher key : list.keySet()) {
            collect.put(key, list.get(key));
        }
        EgovReloadableFilterInvocationSecurityMetadataSource databaseSecurityMetadataSource;
        databaseSecurityMetadataSource = new EgovReloadableFilterInvocationSecurityMetadataSource(collect);
        databaseSecurityMetadataSource.setSecuredObjectService(securedObjectService());
        return databaseSecurityMetadataSource;
    }

    @Bean
    public FilterSecurityInterceptor filterSecurityInterceptor() throws Exception {
        FilterSecurityInterceptor filterSecurityInterceptor = new FilterSecurityInterceptor();
        filterSecurityInterceptor.setAuthenticationManager(authenticationManager());
        filterSecurityInterceptor.setAccessDecisionManager(accessDecisionManager());
        filterSecurityInterceptor.setSecurityMetadataSource(databaseSecurityMetadataSource());
        return filterSecurityInterceptor;
    }

    @Bean
    public UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter() throws Exception {
        UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }

}
