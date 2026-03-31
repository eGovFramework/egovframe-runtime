/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.security.config;

import org.egovframe.rte.fdl.security.bean.*;
import org.egovframe.rte.fdl.security.secureobject.impl.SecuredObjectDAO;
import org.egovframe.rte.fdl.security.secureobject.impl.SecuredObjectServiceImpl;
import org.egovframe.rte.fdl.security.userdetails.hierarchicalroles.HierarchyStringsFactoryBean;
import org.egovframe.rte.fdl.security.userdetails.jdbc.EgovJdbcUserDetailsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
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
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
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
 * egov-security Java Configuration 클래스
 *
 * <p>Desc.: security-config.xml을 대체하는 Java Configuration</p>
 *
 * @author 유지보수
 * @version 5.0
 * @since 2025.06.01
 */
@Configuration
@EnableWebSecurity
public class EgovSecurityConfiguration {

    @Value("${Globals.SecurityConfigPath:}")
    private String securityConfigPath;

    @Autowired
    private ApplicationContext applicationContext;

    private EgovSecurityConfig securityConfig;

    /**
     * 설정 파일 리더 Bean. Globals.SecurityConfigPath는 서비스의 globals.properties에서 읽으며,
     * ApplicationContext를 ResourceLoader로 전달하여 서비스 classpath/리소스 기준으로 파일을 찾는다.
     */
    @Bean
    public EgovSecurityConfigReader egovSecurityConfigReader(
            @Value("${Globals.SecurityConfigPath:}") String securityConfigPath) {
        return new EgovSecurityConfigReader(securityConfigPath, applicationContext);
    }

    @Bean
    public EgovSecurityConfig egovSecurityConfig(
            @Value("${Globals.SecurityConfigPath:}") String securityConfigPath) {
        if (securityConfig == null) {
            EgovSecurityConfigReader reader = new EgovSecurityConfigReader(securityConfigPath, applicationContext);
            securityConfig = reader.readConfig();
        }
        return securityConfig;
    }

    /**
     * permitAll 문자열 CSV → RequestMatcher 리스트로 변환
     * - 정규식(\\A, ^, \\Z, $ 포함)은 RegexRequestMatcher
     * - 그 외는 정적 리소스 정적 리소스/일반 경로 → PathPattern 매처 사용
     */
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

    /**
     * DB에서 URL/정규식 권한 매핑을 읽어와서 RequestMatcher + authorities로 변환
     * 동일한 URL에 대해 여러 권한이 있는 경우 그룹화하여 처리
     * DB의 ORDER BY 절(sort_order)에 따라 LinkedHashMap의 삽입 순서 유지 (1차 정렬)
     * 동일 URL에 대한 중복 제거 후 구체성 계산으로 추가 정렬 (2차 정렬)
     */
    private static Map<RequestMatcher, Collection<String>> loadUrlAuthorizationsFromDb(EgovSecurityConfig props, JdbcTemplate jdbc, PathPatternRequestMatcher.Builder pp) {
        // DB 조회 결과를 순서대로 저장하기 위해 LinkedHashMap 사용
        // SQL의 ORDER BY 절에 의해 정렬된 순서가 그대로 유지됨
        Map<String, Collection<String>> urlToAuthorities = new LinkedHashMap<>();

        // URL 패턴의 원래 순서를 보존하기 위한 리스트
        List<String> urlOrder = new ArrayList<>();

        if (!ObjectUtils.isEmpty(props.getSqlRolesAndUrl())) {
            jdbc.query(props.getSqlRolesAndUrl(), rs -> {
                String url = rs.getString("url");
                String auth = rs.getString("authority");
                if (!ObjectUtils.isEmpty(url) && !ObjectUtils.isEmpty(auth)) {
                    // 처음 등장하는 URL 패턴의 순서 기록
                    if (!urlToAuthorities.containsKey(url)) {
                        urlOrder.add(url);
                    }
                    urlToAuthorities.computeIfAbsent(url, k -> new ArrayList<>()).add(auth);
                }
            });
        }

        // URL 패턴을 구체성에 따라 정렬 (더 구체적인 패턴이 먼저 오도록)
        // DB의 ORDER BY로 이미 정렬되었지만, 구체성 계산으로 추가 최적화
        urlOrder.sort((url1, url2) -> {
            // 더 구체적인 패턴(길이가 길고 와일드카드가 적은)이 먼저 오도록 정렬
            int specificity1 = calculateUrlSpecificity(url1);
            int specificity2 = calculateUrlSpecificity(url2);
            return Integer.compare(specificity2, specificity1); // 내림차순 정렬
        });

        // 정렬된 순서대로 RequestMatcher와 매핑
        Map<RequestMatcher, Collection<String>> result = new LinkedHashMap<>();
        for (String url : urlOrder) {
            Collection<String> authorities = urlToAuthorities.get(url);

            RequestMatcher matcher;
            if (url.startsWith("\\A") || url.startsWith("^") || url.contains("\\Z") || url.contains("$")) {
                matcher = new RegexRequestMatcher(url, null);
            } else {
                matcher = pp.matcher(url);
            }
            result.put(matcher, authorities);
        }

        return result;
    }

    /**
     * URL 패턴의 구체성을 계산하여 반환함. 구체적인 패턴일수록 높은 점수를 반환함.
     */
    private static int calculateUrlSpecificity(String urlPattern) {
        int specificity = 0;

        // 기본 길이 점수 (긴 패턴이 더 구체적)
        specificity += urlPattern.length() * 10;

        // 와일드카드 패널티 (와일드카드가 적을수록 더 구체적)
        specificity -= (urlPattern.split("\\*").length - 1) * 100;
        specificity -= (urlPattern.split("\\.\\*").length - 1) * 50;

        // 정확한 경로 구분자 보너스
        specificity += urlPattern.split("/").length * 5;

        return specificity;
    }

    @Bean
    public PasswordEncoder passwordEncoder(EgovSecurityConfig securityConfig) {
        String rawHash = securityConfig != null ? securityConfig.getHash() : null;
        String securityHash = (rawHash == null || rawHash.trim().isEmpty()) ? "sha-256" : rawHash.trim().toLowerCase();
        boolean securityHashBase64 = securityConfig != null && securityConfig.isHashBase64();
        switch (securityHash.toLowerCase()) {
            case "plaintext":
            case "noop":
                return NoOpPasswordEncoder.getInstance();
            case "ldap":
                return new LdapShaPasswordEncoder();
            case "md5":
                MessageDigestPasswordEncoder md5Encoder = new MessageDigestPasswordEncoder("MD5");
                md5Encoder.setEncodeHashAsBase64(securityHashBase64);
                return md5Encoder;
            default:
                if (securityHash.startsWith("sha")) {
                    String algorithm = securityHash.replace("-", "").toUpperCase();
                    if ("SHA".equals(algorithm)) algorithm = "SHA-256"; // "sha" 입력 시 기본값 설정
                    MessageDigestPasswordEncoder shaEncoder = new MessageDigestPasswordEncoder(algorithm);
                    shaEncoder.setEncodeHashAsBase64(securityHashBase64);
                    return shaEncoder;
                }

                return new BCryptPasswordEncoder();
        }
    }

    @Bean
    public DataSourceFactoryBean securityDataSourceFactoryBean(EgovSecurityConfig securityConfig) {
        return new DataSourceFactoryBean(securityConfig);
    }

    @Bean
    public AuthenticationManager authenticationManager(EgovSecurityConfig securityConfig) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(jdbcUserService(securityConfig));
        provider.setPasswordEncoder(passwordEncoder(securityConfig));

        GrantedAuthoritiesMapper mapper = new RoleHierarchyAuthoritiesMapper(roleHierarchy(securityConfig));
        provider.setAuthoritiesMapper(mapper);

        return new ProviderManager(provider);
    }

    /**
     * UserDetailsService(User, Authority)
     */
    @Bean
    public EgovJdbcUserDetailsManager jdbcUserService(EgovSecurityConfig securityConfig) {
        EgovJdbcUserDetailsManager service = new EgovJdbcUserDetailsManager();
        service.setDataSource(securityDataSourceFactoryBean(securityConfig).getObject());
        service.setUsersByUsernameQuery(usersQueryFactoryBean(securityConfig).getObject());
        service.setAuthoritiesByUsernameQuery(authoritiesQueryFactoryBean(securityConfig).getObject());
        service.setMapClass(mapClassFactoryBean(securityConfig).getObject());
        service.setRoleHierarchy(roleHierarchy(securityConfig));
        return service;
    }

    @Bean
    public UsersQueryFactoryBean usersQueryFactoryBean(EgovSecurityConfig securityConfig) {
        return new UsersQueryFactoryBean(securityConfig);
    }

    @Bean
    public AuthoritiesQueryFactoryBean authoritiesQueryFactoryBean(EgovSecurityConfig securityConfig) {
        return new AuthoritiesQueryFactoryBean(securityConfig);
    }

    @Bean
    public MapClassFactoryBean mapClassFactoryBean(EgovSecurityConfig securityConfig) {
        MapClassFactoryBean bean = new MapClassFactoryBean(securityConfig);
        bean.setDefaultMapClass("org.egovframe.rte.fdl.security.userdetails.DefaultMapUserDetailsMapping");
        return bean;
    }

    /**
     * Role Hierarchy
     */
    @Bean
    @Nullable
    public RoleHierarchy roleHierarchy(EgovSecurityConfig securityConfig) {
        if (ObjectUtils.isEmpty(securityConfig.getSqlHierarchicalRoles())) return null;
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(hierarchyStrings(securityConfig));
        return hierarchy;
    }

    @Bean
    public String hierarchyStrings(EgovSecurityConfig securityConfig) {
        HierarchyStringsFactoryBean bean = new HierarchyStringsFactoryBean(securityConfig);
        bean.setSecuredObjectService(securedObjectService(securityConfig));
        return bean.getObject();
    }

    @Bean
    public SecuredObjectServiceImpl securedObjectService(EgovSecurityConfig securityConfig) {
        SecuredObjectServiceImpl service = new SecuredObjectServiceImpl(securityConfig);
        service.setSecuredObjectDAO(securedObjectDAO(securityConfig));
        return service;
    }

    @Bean
    public SecuredObjectDAO securedObjectDAO(EgovSecurityConfig securityConfig) {
        SecuredObjectDAO dao = new SecuredObjectDAO(securityConfig);
        dao.setDataSource(securityDataSourceFactoryBean(securityConfig).getObject());
        return dao;
    }

    @Bean
    public UrlResourcesMapFactoryBean requestMap(EgovSecurityConfig securityConfig) {
        UrlResourcesMapFactoryBean map = new UrlResourcesMapFactoryBean();
        map.setSecuredObjectService(securedObjectService(securityConfig));
        UrlResourcesMapFactoryBean resultMap = map;
        EgovSecurityMetadataCache.setRequestMap(resultMap.getObject());
        return resultMap;
    }

    @Bean
    public UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter(EgovSecurityConfig securityConfig) {
        UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager(securityConfig));
        return filter;
    }

    @Bean
    public EgovLoginFailHandler egovLoginFailHandler(EgovSecurityConfig securityConfig) {
        return new EgovLoginFailHandler(securityConfig);
    }

    @Bean
    public RequestMatcherTypeFactoryBean requestMatcherTypeFactoryBean(EgovSecurityConfig securityConfig) {
        return new RequestMatcherTypeFactoryBean(securityConfig);
    }

    @Bean
    public EgovAccessDeniedHandler egovAccessDeniedHandler(EgovSecurityConfig securityConfig) {
        return new EgovAccessDeniedHandler(securityConfig);
    }

    @Bean
    public LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint(EgovSecurityConfig securityConfig) {
        return new LoginUrlAuthenticationEntryPoint(securityConfig.getLoginUrl());
    }

    @Bean
    public EgovReloadableFilterInvocationSecurityMetadataSource databaseSecurityMetadataSource(EgovSecurityConfig securityConfig) {
        LinkedHashMap<RequestMatcher, List<ConfigAttribute>> list = new LinkedHashMap<>(requestMap(securityConfig).getObject());
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> collect = new LinkedHashMap<>();
        for (RequestMatcher key : list.keySet()) {
            collect.put(key, list.get(key));
        }
        EgovReloadableFilterInvocationSecurityMetadataSource source = new EgovReloadableFilterInvocationSecurityMetadataSource(collect);
        source.setSecuredObjectService(securedObjectService(securityConfig));
        return source;
    }

    /**
     * URL-권한 매핑을 요청 시점에 databaseSecurityMetadataSource에서 조회하여 판단.
     * reload() 호출 시 반영되도록 FilterSecurityInterceptor 사용.
     */
    @Bean
    public AccessDecisionManager accessDecisionManager() {
        return new AffirmativeBased(List.of(new RoleVoter(), new AuthenticatedVoter()));
    }

    @Bean
    public FilterSecurityInterceptor filterSecurityInterceptor(AuthenticationManager authManager, EgovSecurityConfig securityConfig) {
        FilterSecurityInterceptor interceptor = new FilterSecurityInterceptor();
        interceptor.setAuthenticationManager(authManager);
        interceptor.setAccessDecisionManager(accessDecisionManager());
        interceptor.setSecurityMetadataSource(databaseSecurityMetadataSource(securityConfig));
        return interceptor;
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * SecurityFilterChain
     * URL-권한 매핑은 FilterSecurityInterceptor(databaseSecurityMetadataSource)에서 요청 시점에 조회하므로
     * reload() 호출 시 즉시 반영됨.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authManager,
                                                   SecurityContextRepository securityContextRepository, FilterSecurityInterceptor filterSecurityInterceptor,
                                                   EgovSecurityConfig securityConfig) throws Exception {

        // 익명사용자 활성화
        http.anonymous(Customizer.withDefaults());

        // AntPathRequestMatcher를 대체, Spring Web MVC의 PathPatternParser 기반으로 동작
        var pp = PathPatternRequestMatcher.withDefaults();

        // permitAll 목록
        List<RequestMatcher> permitAll = buildPermitAllMatchers(securityConfig.getPermitAllList(), pp);

        // 인가 규칙: permitAll 후 나머지는 FilterSecurityInterceptor(databaseSecurityMetadataSource)에서 URL별 권한 판단
        http.authorizeHttpRequests(auth -> {
            if (!permitAll.isEmpty()) {
                auth.requestMatchers(permitAll.toArray(new RequestMatcher[0])).permitAll();
            }
            // 나머지 URL은 FilterSecurityInterceptor가 databaseSecurityMetadataSource로 요청 시점에 조회 후 판단 (reload 반영)
            auth.anyRequest().authenticated();
        });

        // URL-권한 매핑을 요청 시점에 조회하도록 AuthorizationFilter 이전에 배치 (reload() 시 즉시 반영)
        http.addFilterBefore(filterSecurityInterceptor, AuthorizationFilter.class);

        http.securityContext(sc -> sc.securityContextRepository(securityContextRepository));

        // 인증/세션/로그인/로그아웃
        http.authenticationManager(authManager);

        http.formLogin(form -> form
                .loginPage(securityConfig.getLoginUrl())
                .loginProcessingUrl(securityConfig.getLoginProcessUrl())
                .defaultSuccessUrl(securityConfig.getDefaultTargetUrl(), securityConfig.isAlwaysUseDefaultTargetUrl())
                .failureUrl(securityConfig.getLoginFailureUrl())
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl(securityConfig.getLogoutUrl())
                .logoutSuccessUrl(securityConfig.getLogoutSuccessUrl())
                .permitAll()
        );

        if (!securityConfig.isCsrf()) {
            http.csrf(AbstractHttpConfigurer::disable);
        } else {
            http.csrf(Customizer.withDefaults());
        }

        // 세션 동시접속 제어
        int maxSessions = Math.max(securityConfig.getConcurrentMaxSessons(), 1);
        http.sessionManagement(session -> session
                .sessionConcurrency(concurrency -> {
                    concurrency.maximumSessions(maxSessions);
                    if (!ObjectUtils.isEmpty(securityConfig.getConcurrentExpiredUrl())) {
                        concurrency.expiredUrl(securityConfig.getConcurrentExpiredUrl());
                    }
                    concurrency.maxSessionsPreventsLogin(securityConfig.isErrorIfMaximumExceeded());
                })
        );

        // 예외 처리/헤더
        http.exceptionHandling(exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(loginUrlAuthenticationEntryPoint(securityConfig))
                        .accessDeniedHandler(egovAccessDeniedHandler(securityConfig))
        );

        http.headers(headers -> {

            // X-Content-Type-Options
            if (securityConfig.isSniff()) {
                headers.contentTypeOptions(Customizer.withDefaults());
            } else {
                headers.contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable);
            }

            // X-Frame-Options
            String xfo = securityConfig.getXframeOptions();
            if (!ObjectUtils.isEmpty(xfo)) {
                switch (xfo.trim().toUpperCase(Locale.ROOT)) {
                    case "SAMEORIGIN" -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
                    case "DISABLE" -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable);
                    default -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny);
                }
            }

            // X-XSS-Protection(최신 브라우저에선 의미 적음)
            if (securityConfig.isXssProtection()) {
                headers.xssProtection(Customizer.withDefaults()); // enable
            } else {
                headers.xssProtection(HeadersConfigurer.XXssConfig::disable);
            }

            // Cache-Control
            if (securityConfig.isCacheControl()) {
                headers.cacheControl(HeadersConfigurer.CacheControlConfig::disable);
            } else {
                headers.cacheControl(Customizer.withDefaults()); // enable
            }

        });

        http.addFilterBefore(usernamePasswordAuthenticationFilter(securityConfig), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
