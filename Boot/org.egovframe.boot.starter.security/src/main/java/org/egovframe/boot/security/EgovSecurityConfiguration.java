/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.boot.security;

import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.security.bean.*;
import org.egovframe.boot.security.secureobject.impl.SecuredObjectDAO;
import org.egovframe.boot.security.secureobject.impl.SecuredObjectServiceImpl;
import org.egovframe.boot.security.userdetails.hierarchicalroles.HierarchyStringsFactoryBean;
import org.egovframe.boot.security.userdetails.jdbc.EgovJdbcUserDetailsManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * Spring Security 자동 구성(Globals.Auth=security).
 * <p>@Bean 간 의존은 메서드 파라미터로 주입받는다(proxyBeanMethods = false).
 * 기본 DataSource 생성 이후, Spring Boot 기본 SecurityFilterChain 보다 먼저 적용된다.</p>
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  @AutoConfiguration 전환, @Bean 메서드 파라미터 주입, 요청 시점 DB 인가, 기본 hash BCrypt, ID salt SHA-256 지원, roleHierarchy 항상 생성
 * </pre>
 * @since 2025.06.01
 */
@AutoConfiguration(after = DataSourceAutoConfiguration.class, before = SecurityAutoConfiguration.class)
@EnableWebSecurity
@EnableConfigurationProperties(EgovSecurityProperties.class)
@ConditionalOnProperty(prefix = "Globals", name = "Auth", havingValue = "security")
@Slf4j
public class EgovSecurityConfiguration {

    /** hash 미지정(빈 값) 시 기본 비밀번호 인코더 : BCrypt */
    private static final String DEFAULT_HASH = "bcrypt";

    private final EgovSecurityProperties properties;

    public EgovSecurityConfiguration(EgovSecurityProperties properties) {
        this.properties = properties;
    }

    /** permitAll CSV → RequestMatcher 목록(정규식 형태는 RegexRequestMatcher, 그 외 PathPattern) */
    private static List<RequestMatcher> buildPermitAllMatchers(String csv, PathPatternRequestMatcher.Builder pp) {
        if (ObjectUtils.isEmpty(csv)) return new ArrayList<>();
        String[] tokens = csv.split(",");
        List<RequestMatcher> out = new ArrayList<>();
        for (String raw : tokens) {
            String p = raw.trim();
            if (p.isEmpty()) continue;
            if (p.startsWith("\\A") || p.startsWith("^") || p.contains("\\Z") || p.contains("$")) {
                out.add(new RegexRequestMatcher(p, null));
            } else {
                out.add(pp.matcher(p));
            }
        }
        return out;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // trim + 소문자(Locale.ROOT)로 정규화한 뒤 분기
        String rawHash = properties.getHash();
        String securityHash = (rawHash == null || rawHash.trim().isEmpty()) ? DEFAULT_HASH : rawHash.trim().toLowerCase(Locale.ROOT);
        boolean securityHashBase64 = properties.isHashBase64();
        switch (securityHash) {
            case "bcrypt":
                return new BCryptPasswordEncoder();
            case "plaintext":
            case "noop":
                return NoOpPasswordEncoder.getInstance();
            case "egov-sha256":
                // 사용자 ID salt SHA-256
                return new EgovIdSaltSha256PasswordEncoder();
            case "ldap":
                return new LdapShaPasswordEncoder();
            case "md5":
                MessageDigestPasswordEncoder md5Encoder = new MessageDigestPasswordEncoder("MD5");
                md5Encoder.setEncodeHashAsBase64(securityHashBase64);
                return md5Encoder;
            default:
                if (securityHash.startsWith("sha")) {
                    String algorithm = securityHash.replace("-", "").toUpperCase(Locale.ROOT);
                    if ("SHA".equals(algorithm)) algorithm = "SHA-256"; // "sha" 입력 시 기본값 설정
                    MessageDigestPasswordEncoder shaEncoder = new MessageDigestPasswordEncoder(algorithm);
                    shaEncoder.setEncodeHashAsBase64(securityHashBase64);
                    return shaEncoder;
                }

                return new BCryptPasswordEncoder();
        }
    }

    @Bean
    public DataSourceFactoryBean securityDataSourceFactoryBean() {
        return new DataSourceFactoryBean(properties);
    }

    @Bean
    public AuthenticationManager authenticationManager(EgovJdbcUserDetailsManager jdbcUserService,
                                                       @Qualifier("passwordEncoder") PasswordEncoder passwordEncoder,
                                                       @Qualifier("roleHierarchy") RoleHierarchy roleHierarchy) {
        DaoAuthenticationProvider provider;
        if (passwordEncoder instanceof EgovIdSaltSha256PasswordEncoder idSaltPasswordEncoder) {
            // username 을 salt 로 전달하는 전용 Provider (기본 PasswordEncoder 는 timing 공격 방지 비교용으로 유지)
            EgovDaoAuthenticationProvider egovProvider = new EgovDaoAuthenticationProvider();
            egovProvider.setIdSaltPasswordEncoder(idSaltPasswordEncoder);
            provider = egovProvider;
        } else {
            provider = new DaoAuthenticationProvider();
            provider.setPasswordEncoder(passwordEncoder);
        }
        provider.setUserDetailsService(jdbcUserService);

        GrantedAuthoritiesMapper mapper = new RoleHierarchyAuthoritiesMapper(roleHierarchy);
        provider.setAuthoritiesMapper(mapper);

        return new ProviderManager(provider);
    }

    /** UserDetailsService(User, Authority) */
    @Bean
    public EgovJdbcUserDetailsManager jdbcUserService(DataSourceFactoryBean securityDataSourceFactoryBean,
                                                      UsersQueryFactoryBean usersQueryFactoryBean,
                                                      AuthoritiesQueryFactoryBean authoritiesQueryFactoryBean,
                                                      MapClassFactoryBean mapClassFactoryBean,
                                                      @Qualifier("roleHierarchy") RoleHierarchy roleHierarchy) {
        EgovJdbcUserDetailsManager service = new EgovJdbcUserDetailsManager();
        service.setDataSource(securityDataSourceFactoryBean.getObject());
        service.setUsersByUsernameQuery(usersQueryFactoryBean.getObject());
        service.setAuthoritiesByUsernameQuery(authoritiesQueryFactoryBean.getObject());
        service.setMapClass(mapClassFactoryBean.getObject());
        service.setRoleHierarchy(roleHierarchy);
        return service;
    }

    @Bean
    public UsersQueryFactoryBean usersQueryFactoryBean() {
        return new UsersQueryFactoryBean(properties);
    }

    @Bean
    public AuthoritiesQueryFactoryBean authoritiesQueryFactoryBean() {
        return new AuthoritiesQueryFactoryBean(properties);
    }

    @Bean
    public MapClassFactoryBean mapClassFactoryBean() {
        MapClassFactoryBean bean = new MapClassFactoryBean(properties);
        bean.setDefaultMapClass("org.egovframe.boot.security.userdetails.DefaultMapUserDetailsMapping");
        return bean;
    }

    /** Role Hierarchy */
    @Bean
    public RoleHierarchy roleHierarchy(@Qualifier("hierarchyStrings") ObjectProvider<String> hierarchyStrings) {
        // 항상 생성 : sqlHierarchicalRoles 미설정 시 기본 계층(ROLE_ADMIN > ROLE_USER > ROLE_ANONYMOUS)
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(hierarchyStrings.getIfAvailable());
        return hierarchy;
    }

    @Bean
    public String hierarchyStrings(SecuredObjectServiceImpl securedObjectService) {
        HierarchyStringsFactoryBean bean = new HierarchyStringsFactoryBean(properties);
        bean.setSecuredObjectService(securedObjectService);
        return bean.getObject();
    }

    @Bean
    public SecuredObjectServiceImpl securedObjectService(SecuredObjectDAO securedObjectDAO) {
        SecuredObjectServiceImpl service = new SecuredObjectServiceImpl(properties);
        service.setSecuredObjectDAO(securedObjectDAO);
        return service;
    }

    @Bean
    public SecuredObjectDAO securedObjectDAO(DataSourceFactoryBean securityDataSourceFactoryBean) {
        SecuredObjectDAO dao = new SecuredObjectDAO();
        dao.setDataSource(securityDataSourceFactoryBean.getObject());
        return dao;
    }

    @Bean
    public UrlResourcesMapFactoryBean requestMap(SecuredObjectServiceImpl securedObjectService) {
        UrlResourcesMapFactoryBean map = new UrlResourcesMapFactoryBean();
        map.setSecuredObjectService(securedObjectService);
        UrlResourcesMapFactoryBean resultMap = map;
        EgovSecurityMetadataCache.setRequestMap(resultMap.getObject());
        return resultMap;
    }

    @Bean
    public UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager);
        return filter;
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
    public EgovAccessDeniedHandler egovAccessDeniedHandler() {
        return new EgovAccessDeniedHandler(properties);
    }

    @Bean
    public LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint() {
        return new LoginUrlAuthenticationEntryPoint(properties.getLoginUrl());
    }

    @Bean
    public EgovReloadableFilterInvocationSecurityMetadataSource databaseSecurityMetadataSource(UrlResourcesMapFactoryBean requestMap,
                                                                                              SecuredObjectServiceImpl securedObjectService) {
        LinkedHashMap<RequestMatcher, List<ConfigAttribute>> list = new LinkedHashMap<>(requestMap.getObject());
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> collect = new LinkedHashMap<>();
        for (RequestMatcher key : list.keySet()) {
            collect.put(key, list.get(key));
        }
        EgovReloadableFilterInvocationSecurityMetadataSource source = new EgovReloadableFilterInvocationSecurityMetadataSource(collect);
        source.setSecuredObjectService(securedObjectService);
        return source;
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /** SecurityFilterChain */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, AuthenticationManager authManager, SecurityContextRepository securityContextRepository,
            EgovReloadableFilterInvocationSecurityMetadataSource databaseSecurityMetadataSource,
            @Qualifier("roleHierarchy") RoleHierarchy roleHierarchy,
            LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint,
            EgovAccessDeniedHandler egovAccessDeniedHandler,
            UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter
    ) throws Exception {

        // 익명사용자 활성화
        http.anonymous(Customizer.withDefaults());

        var pp = PathPatternRequestMatcher.withDefaults();

        // permitAll 목록
        List<RequestMatcher> permitAll = buildPermitAllMatchers(properties.getPermitAllList(), pp);

        // 인가 : 요청 시점에 DB 보호자원 매핑으로 판단(reload 즉시 반영), 매핑이 없으면 permitAll / 인증 필요
        http.authorizeHttpRequests(auth -> auth.anyRequest()
                .access(new EgovDatabaseAuthorizationManager(databaseSecurityMetadataSource, roleHierarchy, permitAll)));

        http.securityContext(sc -> sc.securityContextRepository(securityContextRepository));

        // 인증/세션/로그인/로그아웃
        http.authenticationManager(authManager);

        http.formLogin(form -> form
                .loginPage(properties.getLoginUrl())
                .loginProcessingUrl(properties.getLoginProcessUrl())
                .defaultSuccessUrl(properties.getDefaultTargetUrl(), properties.isAlwaysUseDefaultTargetUrl())
                .failureUrl(properties.getLoginFailureUrl())
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl(properties.getLogoutUrl())
                .logoutSuccessUrl(properties.getLogoutSuccessUrl())
                .permitAll()
        );

        if (!properties.isCsrf()) {
            http.csrf(AbstractHttpConfigurer::disable);
        } else {
            http.csrf(Customizer.withDefaults());
        }

        // 세션 동시접속 제어
        int maxSessions = Math.max(properties.getConcurrentMaxSessons(), 1);
        http.sessionManagement(session -> session
                .sessionConcurrency(concurrency -> {
                    concurrency.maximumSessions(maxSessions);
                    if (!ObjectUtils.isEmpty(properties.getConcurrentExpiredUrl())) {
                        concurrency.expiredUrl(properties.getConcurrentExpiredUrl());
                    }
                    concurrency.maxSessionsPreventsLogin(properties.isErrorIfMaximumExceeded());
                })
        );

        // 예외 처리/헤더
        http.exceptionHandling(exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(loginUrlAuthenticationEntryPoint)
                        .accessDeniedHandler(egovAccessDeniedHandler)
        );

        http.headers(headers -> {

            // X-Content-Type-Options
            if (properties.isSniff()) {
                headers.contentTypeOptions(Customizer.withDefaults());
            } else {
                headers.contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable);
            }

            // X-Frame-Options
            String xfo = properties.getXframeOptions();
            if (!ObjectUtils.isEmpty(xfo)) {
                switch (xfo.trim().toUpperCase(Locale.ROOT)) {
                    case "SAMEORIGIN" -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
                    case "DISABLE" -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable);
                    default -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny);
                }
            }

            // X-XSS-Protection
            if (properties.isXssProtection()) {
                headers.xssProtection(Customizer.withDefaults());
            } else {
                headers.xssProtection(HeadersConfigurer.XXssConfig::disable);
            }

            // Cache-Control
            if (properties.isCacheControl()) {
                headers.cacheControl(HeadersConfigurer.CacheControlConfig::disable);
            } else {
                headers.cacheControl(Customizer.withDefaults());
            }

        });

        http.addFilterBefore(usernamePasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
