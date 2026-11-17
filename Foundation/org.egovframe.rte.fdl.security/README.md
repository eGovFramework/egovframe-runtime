# org.egovframe.rte.fdl.security

**Foundation Layer 보안(Security) 모듈**입니다. **Spring Security 5.8.16-egov.1**의 XML 네임스페이스 설정(`<http>`, `<authentication-manager>`)을 기반으로, 폼 로그인·세션 관리·보안 헤더·CSRF와 **DB 기반 URL/메서드/포인트컷 단위 접근제어**를 한 번에 구성해 주는 전용 네임스페이스 `egov-security`(`<egov-security:config>`, `<egov-security:initializer>`, `<egov-security:secured-object-config>`)를 제공합니다. 인가는 `FilterSecurityInterceptor` + `AffirmativeBased`(`RoleVoter`, `AuthenticatedVoter`) + DB에서 읽은 `FilterInvocationSecurityMetadataSource`로 동작하며, 사용자·권한은 `JdbcUserDetailsManager`를 확장한 `EgovJdbcUserDetailsManager`가 JDBC로 조회합니다. 다른 실행환경 모듈이 이 모듈에 의존하지는 않으며, 웹 적용 프로젝트에서 직접 사용합니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.fdl.security:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Spring Security 5.8.16-egov.1 / javax.servlet 4.0.1
- **실행환경 의존**: `org.egovframe.rte.fdl.string` (→ `fdl.logging` 전이)

---

## 특징

- **설정 간소화 네임스페이스**: `<egov-security:config>` 한 줄로 로그인/로그아웃/실패/접근거부 URL, DataSource, 사용자·권한 조회 SQL, 요청 매처 종류(`ant`/`regex`/`ciRegex`), 비밀번호 해시(`sha-256`/`sha`/`md5`/`bcrypt`/`plaintext`), 동시 세션 제한, 보안 헤더, CSRF를 지정합니다.
- **DB 기반 보호자원 관리**: `SECURED_RESOURCES`/`SECURED_RESOURCES_ROLE`/`ROLES`/`ROLES_HIERARCHY` 테이블에서 URL·메서드·포인트컷 패턴과 권한, 역할 계층을 읽어 `RequestMatcher`/메서드명 → `ConfigAttribute` 맵을 구성합니다. 기본 SQL은 `SecuredObjectDAO` 상수로 제공되며 `<egov-security:secured-object-config>`로 교체할 수 있습니다.
- **런타임 재적재**: `EgovReloadableFilterInvocationSecurityMetadataSource.reload()`로 DB 변경을 재기동 없이 반영합니다(4.3.1에서 맵을 비우지 않고 새 맵으로 원자 교체하도록 개선).
- **메서드·포인트컷 보안**: `MethodSecurityInterceptor` + `MapBasedMethodSecurityMetadataSource`(DB 매핑) + `@Secured`/JSR-250 어노테이션 소스를 `DelegatingMethodSecurityMetadataSource`로 결합하고, `ProtectPointcutPostProcessor`로 AspectJ 포인트컷 표현식 기반 보호도 지원합니다.
- **보안 헤더 개별 제어(4.3.1 개선)**: `sniff`, `xframeOptions`(`DENY`/`SAMEORIGIN`/`DISABLE`, 그 외 값은 `DENY`), `xssProtection`, `cacheControl` 옵션이 각각 독립적으로 적용됩니다. `cacheControl=true`는 Cache-Control 보호 헤더를 **비활성화**하므로 경고 로그가 남습니다.
- **로그인 실패 일반화(4.3.1 추가)**: `EgovLoginFailHandler`가 실패 원인(계정 없음·잠김·만료 등)과 무관하게 동일한 `BadCredentialsException` 메시지만 세션에 남겨 계정 열거(CWE-203/204)를 방지합니다.
- **사용자 정보 확장**: `EgovUserDetails`(`User` 확장, 업무 VO 보관), `EgovUsersByUsernameMapping`(`MappingSqlQuery`)으로 사용자 테이블 컬럼을 VO에 매핑하고, `EgovUserDetailsHelper`로 인증 사용자·권한 목록을 정적 메서드로 조회합니다.

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/fdl/security/
├── config/
│   ├── EgovSecurityNameHandler.java                       # egov-security 네임스페이스 핸들러
│   ├── EgovSecurityConfigBeanDefinitionParser.java        # <egov-security:config> 파서 → SecurityConfig
│   ├── EgovSecurityConfigInitializerBeanDefinitionParser.java # <egov-security:initializer> 파서 → SecurityConfigInitializer
│   ├── EgovSecuritySecuredObjectConfigBeanDefinitionParser.java # <egov-security:secured-object-config> 파서 → SecuredObjectConfig
│   ├── SecurityConfig.java                                # config 속성 보관 POJO
│   ├── SecuredObjectConfig.java                           # 보호자원 SQL 보관 POJO
│   ├── SecurityConfigInitializer.java                     # 생성된 필터·빈에 URL/쿼리/해시/세션 설정 주입
│   ├── EgovSecurityHttp.java                              # <http> XML 문자열 생성 (헤더·CSRF·로그인·세션)
│   ├── EgovSecurityConfigShare.java                       # 헤더·CSRF 옵션 정적 공유 값
│   └── internal/                                          # SecurityConfig 값을 꺼내 주는 FactoryBean 모음
│       ├── AuthoritiesQueryFactoryBean.java  CsrfAccessDeniedUrlFactoryBean.java
│       ├── DataSourceFactoryBean.java        ErrorIfMaximumExceededFactoryBean.java
│       ├── ExpiredUrlFactoryBean.java        LoginFormUrlFactoryBean.java
│       ├── MapClassFactoryBean.java          MaximumSessionsFactoryBean.java
│       ├── RequestMatcherTypeFactoryBean.java UsersQueryFactoryBean.java
│       └── EgovSessionInformationExpiredStrategy.java     # 세션 만료 시 expiredUrl로 redirect
├── intercept/
│   ├── EgovReloadableFilterInvocationSecurityMetadataSource.java # URL→권한 메타데이터 소스 (reload 지원)
│   ├── UrlResourcesMapFactoryBean.java                    # DB URL-권한 맵 FactoryBean
│   ├── MethodResourcesMapFactoryBean.java                 # DB 메서드/포인트컷-권한 맵 FactoryBean (resourceType)
│   ├── LookupAttributesMethodReplacer.java                # 요청별 best-match 조회용 MethodReplacer
│   ├── EgovLoginFailHandler.java                          # 로그인 실패 처리기 (메시지 일반화)
│   └── CsrfAccessDeniedHandler.java                       # CSRF 거부 시 csrfAccessDeniedUrl로 forward
├── securedobject/
│   ├── EgovSecuredObjectService.java                      # 보호자원 조회 서비스 인터페이스
│   └── impl/
│       ├── SecuredObjectServiceImpl.java                  # 서비스 구현 (requestMatcherType 반영)
│       ├── SecuredObjectDAO.java                          # NamedParameterJdbcTemplate 기반 DAO, 기본 SQL 상수
│       └── SelfRegexRequestMatcher.java                   # equals/hashCode를 구현한 정규식 RequestMatcher
└── userdetails/
    ├── EgovUserDetails.java                               # User 확장 (egovVO 보관)
    ├── DefaultMapUserDetailsMapping.java                  # ResultSet → Map 기반 기본 매핑
    ├── jdbc/
    │   ├── EgovJdbcUserDetailsManager.java                # JdbcUserDetailsManager 확장
    │   └── EgovUsersByUsernameMapping.java                # MappingSqlQuery<EgovUserDetails> 추상 클래스
    ├── hierarchicalroles/
    │   └── HierarchyStringsFactoryBean.java               # DB 역할 계층 → RoleHierarchyImpl 문자열
    └── util/
        ├── EgovUserDetailsHelper.java                     # 인증 사용자/권한/인증 여부/해시 정적 유틸
        └── CamelCaseUtil.java                             # 컬럼명 → camelCase 변환

src/main/resources/
├── META-INF/spring.handlers, spring.schemas                # 네임스페이스 등록
├── META-INF/spring/security/
│   ├── security-config.xml                                 # 인증·세션·DB 메타데이터·FilterSecurityInterceptor 빈
│   ├── method-config.xml                                   # MethodSecurityInterceptor + DB 메서드 매핑
│   └── pointcut-config.xml                                 # ProtectPointcutPostProcessor + DB 포인트컷 매핑
├── org/egovframe/rte/fdl/security/config/egov-security-4.3.1.xsd
└── org/egovframe/rte/fdl/security/messages/message-security*.properties
```

---

## 주요 동작 개요

1. `<egov-security:config>`를 파싱하면 `SecurityConfig` 빈이 만들어지고, 헤더·CSRF 옵션은 `EgovSecurityConfigShare` 정적 필드에 반영됩니다.
2. `<egov-security:initializer>`를 파싱하면 `security-config.xml`을 로드한 뒤 `EgovSecurityHttp.getHttp()`가 생성한 `<http>` 정의(엔트리포인트 `loginUrlAuthenticationEntryPoint`, 요청 매처 `requestMatcherTypeFactoryBean`, `<form-login login-page="/egov_security_login" authentication-failure-handler-ref="egovLoginFailHandler"/>`, `<logout logout-url="/egov_security_logout"/>`, `<session-management session-authentication-strategy-ref="sas"/>`, `<custom-filter before="FILTER_SECURITY_INTERCEPTOR" ref="filterSecurityInterceptor"/>`, `<custom-filter position="CONCURRENT_SESSION_FILTER" ref="concurrencyFilter"/>`, `<access-denied-handler ref="csrfAccessDeniedHandler"/>`)를 등록합니다. `supportMethod="true"`(기본)면 `method-config.xml`, `supportPointcut="true"`면 `pointcut-config.xml`을 추가 로드합니다.
3. `SecurityConfigInitializer.init()`(`@PostConstruct`)이 생성된 필터·프로바이더를 찾아 접근거부/로그인/실패/로그아웃 URL, JDBC 쿼리와 매핑 클래스, 비밀번호 인코더(`NoOpPasswordEncoder`, `MessageDigestPasswordEncoder`(MD5/SHA-1/SHA-256, `hashBase64`), `BCryptPasswordEncoder`), 동시 세션 수와 만료 URL, 기본 이동 URL을 주입합니다.
4. **인증**: `<authentication-manager alias="authenticationManager">` + `jdbcUserService`(`EgovJdbcUserDetailsManager`)가 `usersByUsernameQuery`/`authoritiesByUsernameQuery`로 사용자·권한을 읽고 `RoleHierarchyImpl`(DB 계층 문자열)을 적용합니다.
5. **URL 인가**: `FilterSecurityInterceptor` → `EgovReloadableFilterInvocationSecurityMetadataSource`(`UrlResourcesMapFactoryBean`이 DB에서 만든 `RequestMatcher → ConfigAttribute` 맵) → `AffirmativeBased`(`RoleVoter(rolePrefix="")`, `AuthenticatedVoter`, `allowIfAllAbstainDecisions=false`).

---

## 주요 클래스

### 설정 (`config`)

| 클래스 | 설명 |
|--------|------|
| `EgovSecurityNameHandler` | `config`, `initializer`, `secured-object-config` 엘리먼트에 각 파서를 등록하는 `NamespaceHandlerSupport` |
| `SecurityConfig` | `loginUrl`, `logoutSuccessUrl`, `loginFailureUrl`, `accessDeniedUrl`, `dataSource`, `jdbcUsersByUsernameQuery`, `jdbcAuthoritiesByUsernameQuery`, `jdbcMapClass`, `requestMatcherType`, `hash`, `hashBase64`, `concurrentMaxSessons`, `concurrentExpiredUrl`, `errorIfMaximumExceeded`, `defaultTargetUrl`, `alwaysUseDefaultTargetUrl`, `useExpressions`, `sniff`, `xframeOptions`, `xssProtection`, `cacheControl`, `csrf`, `csrfAccessDeniedUrl` 보관 |
| `SecuredObjectConfig` | `sqlRolesAndUrl`, `sqlRolesAndMethod`, `sqlRolesAndPointcut`, `sqlRegexMatchedRequestMapping`, `sqlHierarchicalRoles`, `roleHierarchyString` 보관. `SecuredObjectDAO.securedObjectConfigInit()`이 컨텍스트에서 찾아 기본 SQL을 덮어씀 |
| `SecurityConfigInitializer` | `ApplicationContextAware`. `init()`에서 `ExceptionTranslationFilter`, `UsernamePasswordAuthenticationFilter`, `LogoutFilter` 등 생성된 필터와 `DaoAuthenticationProvider`를 찾아 설정값을 반영 |
| `EgovSecurityHttp` | 싱글톤. `getHttp(useExpressions, sniff, xFrameOptions, xssProtection, cacheControl, csrf)`로 `<http>` 정의 XML을 `InputStreamResource`로 생성 |
| `config.internal.*FactoryBean` | `SecurityConfig`의 각 값을 `FactoryBean`으로 노출하여 `security-config.xml`의 빈 정의가 참조 (`DataSourceFactoryBean`, `UsersQueryFactoryBean`, `AuthoritiesQueryFactoryBean`, `MapClassFactoryBean`, `RequestMatcherTypeFactoryBean`, `LoginFormUrlFactoryBean`, `MaximumSessionsFactoryBean`, `ExpiredUrlFactoryBean`, `ErrorIfMaximumExceededFactoryBean`, `CsrfAccessDeniedUrlFactoryBean`) |
| `EgovSessionInformationExpiredStrategy` | `SessionInformationExpiredStrategy` 구현. 중복 로그인으로 만료된 세션 감지 시 `contextPath + expiredUrl`로 redirect |

### 인터셉터·핸들러 (`intercept`)

| 클래스 | 설명 |
|--------|------|
| `EgovReloadableFilterInvocationSecurityMetadataSource` | `FilterInvocationSecurityMetadataSource` 구현. 생성자에 `LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>`를 받고, `getAttributes()`에서 요청과 매칭되는 첫 항목을 반환. `reload()`는 `EgovSecuredObjectService.getRolesAndUrl()`로 새 맵을 만들어 교체 |
| `UrlResourcesMapFactoryBean` | `init()`에서 `securedObjectService.getRolesAndUrl()` 결과를 보관하는 `FactoryBean<LinkedHashMap<RequestMatcher, List<ConfigAttribute>>>` |
| `MethodResourcesMapFactoryBean` | `resourceType`이 `method`면 `getRolesAndMethod()`, `pointcut`이면 `getRolesAndPointcut()`로 맵 구성. 다른 값이면 예외 |
| `LookupAttributesMethodReplacer` | 매 요청마다 DB에서 best-match 보호자원을 찾기 위해 `lookupAttributes`를 가로채는 `MethodReplacer` |
| `EgovLoginFailHandler` | `SimpleUrlAuthenticationFailureHandler` 확장. 실제 예외는 debug 로그로만 남기고 세션에는 일반 메시지의 `BadCredentialsException`만 저장 |
| `CsrfAccessDeniedHandler` | `AccessDeniedHandler` 구현. 생성자로 받은 `csrfAccessDeniedUrl`로 `RequestDispatcher.forward` |

### 보호자원 (`securedobject`)

| 클래스 | 설명 |
|--------|------|
| `EgovSecuredObjectService` | `getRolesAndUrl()`, `getRolesAndMethod()`, `getRolesAndPointcut()`, `getMatchedRequestMapping(url)`, `getHierarchicalRoles()` |
| `SecuredObjectServiceImpl` | `requestMatcherType`(기본 `ant`, `SecurityConfig`가 있으면 그 값)에 따라 `AntPathRequestMatcher` 또는 `SelfRegexRequestMatcher`로 키를 만들어 DAO 결과를 변환 |
| `SecuredObjectDAO` | 기본 SQL 상수 `DEF_ROLES_AND_URL_QUERY`, `DEF_ROLES_AND_METHOD_QUERY`, `DEF_ROLES_AND_POINTCUT_QUERY`, `DEF_REGEX_MATCHED_REQUEST_MAPPING_QUERY_ORACLE10G`(Oracle `REGEXP_LIKE` 전용), `DEF_HIERARCHICAL_ROLES_QUERY`. `setDataSource()`로 `NamedParameterJdbcTemplate` 생성 |
| `SelfRegexRequestMatcher` | `RegexRequestMatcher`를 감싸고 `pattern`/`httpMethod` 기준 `equals`/`hashCode`를 구현하여 맵 키로 사용 가능 |

### 사용자 정보 (`userdetails`)

| 클래스 | 설명 |
|--------|------|
| `EgovJdbcUserDetailsManager` | `JdbcUserDetailsManager` 확장. `mapClass`(`EgovUsersByUsernameMapping` 구현 클래스명)와 `roleHierarchy` 설정, `loadUserByUsername()`이 `EgovUserDetails` 반환, `getAuthenticatedUser()` 제공 |
| `EgovUsersByUsernameMapping` | `MappingSqlQuery<EgovUserDetails>` 추상 클래스. `mapRow()`를 구현하여 사용자 테이블 컬럼을 VO로 매핑 |
| `DefaultMapUserDetailsMapping` | 기본 매핑 구현. ResultSet 전체 컬럼을 `Map`으로 담아 `EgovUserDetails.egovVO`에 보관 |
| `EgovUserDetails` | `org.springframework.security.core.userdetails.User` 확장. `getEgovUserVO()`/`setEgovUserVO()` |
| `HierarchyStringsFactoryBean` | `init()`에서 `securedObjectService.getHierarchicalRoles()`로 `ROLE_ADMIN > ROLE_USER` 형식 문자열 생성 |
| `EgovUserDetailsHelper` | `getAuthenticatedUser()`(VO 반환), `getAuthorities()`, `isAuthenticated()`, `getHashedPassword(password)`(`DelegatingPasswordEncoder`, 기본 SHA-256 매칭) |
| `CamelCaseUtil` | `convert2CamelCase(underScore)` |

---

## 주요 의존성

| 라이브러리 | 버전 | scope / 비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.string` | 4.3.1 | compile. `EgovObjectUtil` 등 사용, `fdl.logging` 전이 |
| `org.springframework.security:spring-security-config` | 5.8.16-egov.1 | compile (spring-core/beans/context/aop/expression 제외 후 Spring 5.3.39-egov.1 사용). `spring-security-core`, `spring-security-web` 전이 |
| `org.springframework.security:spring-security-taglibs` | 5.8.16-egov.1 | compile. JSP `<sec:authorize>` 등 |
| `org.springframework:spring-webmvc` | 5.3.39-egov.1 | compile (commons-logging 제외) |
| `org.springframework:spring-jdbc` | 5.3.39-egov.1 | compile. `JdbcUserDetailsManager`, `NamedParameterJdbcTemplate` |
| `org.aspectj:aspectjweaver` | 1.9.23 | compile. 포인트컷 기반 메서드 보안 |
| `javax.servlet:javax.servlet-api` | 4.0.1 | provided |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `spring-test` / `junit` / `easymock` / `hsqldb`(`jdk8`) / `log4jdbc` | 5.3.39-egov.1 / 4.13.2 / 5.2.0 / 2.7.3 / 1.2 | test |

버전은 모두 루트 `pom.xml`의 `dependencyManagement`에서 관리하며 모듈 POM에는 선언하지 않습니다.

---

## 설정 및 사용 예시

### 1) 네임스페이스 설정 (src/test/resources/META-INF/spring/config-context.xml)

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:security="http://www.springframework.org/schema/security"
       xmlns:egov-security="http://maven.egovframe.go.kr/schema/egov-security"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/security http://www.springframework.org/schema/security/spring-security.xsd
           http://maven.egovframe.go.kr/schema/egov-security http://maven.egovframe.go.kr/schema/egov-security/egov-security-4.3.1.xsd">

    <!-- 정적 자원은 보안 필터 제외 -->
    <security:http pattern="/css/**" security="none"/>
    <security:http pattern="/images/**" security="none"/>
    <security:http pattern="/js/**" security="none"/>
    <security:http pattern="\A/WEB-INF/jsp/.*\Z" request-matcher="regex" security="none"/>

    <egov-security:config id="securityConfig"
        loginUrl="/cvpl/EgovCvplLogin.do"
        logoutSuccessUrl="/cvpl/EgovCvplLogin.do"
        loginFailureUrl="/cvpl/EgovCvplLogin.do?login_error=1"
        accessDeniedUrl="/system/accessDenied.do"
        dataSource="dataSource"
        jdbcUsersByUsernameQuery="SELECT USER_ID,PASSWORD,ENABLED,USER_NAME,BIRTH_DAY,SSN FROM USERS WHERE USER_ID = ?"
        jdbcAuthoritiesByUsernameQuery="SELECT USER_ID,AUTHORITY FROM AUTHORITIES WHERE USER_ID = ?"
        jdbcMapClass="org.egovframe.rte.fdl.security.userdetails.EgovUserDetailsMapping"
        requestMatcherType="regex"
        hash="md5"/>

    <egov-security:initializer id="initializer" supportMethod="true" supportPointcut="false"/>
</beans>
```

`spring.schemas`는 `egov-security.xsd`와 `egov-security-4.3.1.xsd` 두 위치를 모듈 내 `egov-security-4.3.1.xsd`로 매핑합니다.

`<egov-security:config>` 주요 속성(XSD 기준):

| 속성 | 기본값 | 설명 |
|---|---|---|
| `loginUrl`, `logoutSuccessUrl`, `loginFailureUrl`, `accessDeniedUrl` | (필수) | 로그인 페이지, 로그아웃 후 이동, 로그인 실패, 접근 거부 URL |
| `dataSource` | `dataSource` | 사용자·보호자원 조회용 DataSource 빈 이름 |
| `jdbcUsersByUsernameQuery` | `select user_id, password, enabled, users.* from users where user_id = ?` | 사용자 조회 SQL |
| `jdbcAuthoritiesByUsernameQuery` | `select user_id, authority from authorites where user_id = ?` | 권한 조회 SQL |
| `jdbcMapClass` | `...userdetails.DefaultMapUserDetailsMapping` | `EgovUsersByUsernameMapping` 구현 클래스 |
| `requestMatcherType` | `regex` | `ant` / `regex` / `ciRegex` |
| `hash`, `hashBase64` | `sha-256`, `true` | `plaintext` / `md5` / `sha` / `sha-256` / `bcrypt` |
| `concurrentMaxSessons`, `concurrentExpiredUrl`, `errorIfMaximumExceeded` | -, -, `false` | 동시 세션 수(기본 999), 만료 세션 이동 URL, 초과 시 두 번째 로그인 거부 여부 |
| `defaultTargetUrl`, `alwaysUseDefaultTargetUrl` | -, `true` | 로그인 성공 후 이동 URL |
| `useExpressions` | `false` | `<http use-expressions>` |
| `sniff`, `xframeOptions`, `xssProtection`, `cacheControl` | `true`, (`SAMEORIGIN`), `true`, `false` | 보안 헤더 |
| `csrf`, `csrfAccessDeniedUrl` | `false`, - | CSRF 보호 및 거부 시 forward URL |

`<egov-security:secured-object-config>`로 `sqlRolesAndUrl`, `sqlRolesAndMethod`, `sqlRolesAndPointcut`, `sqlRegexMatchedRequestMapping`, `sqlHierarchicalRoles`, `roleHierarchyString`을 지정하면 `SecuredObjectDAO`의 기본 SQL을 대체합니다.

### 2) 보호자원 테이블 (src/test/resources/META-INF/testdata/sample_schema_hsql.sql)

기본 SQL이 참조하는 테이블은 `USERS`, `AUTHORITIES(USER_ID, AUTHORITY)`, `ROLES(AUTHORITY, ...)`, `ROLES_HIERARCHY(PARENT_ROLE, CHILD_ROLE)`, `SECURED_RESOURCES(RESOURCE_ID, RESOURCE_PATTERN, RESOURCE_TYPE, SORT_ORDER, ...)`, `SECURED_RESOURCES_ROLE(RESOURCE_ID, AUTHORITY)`입니다. `RESOURCE_TYPE`은 `url`, `method`, `pointcut`을 사용합니다.

```sql
insert into secured_resources (resource_id, resource_name, resource_pattern, description, resource_type, sort_order, create_date)
values ('web-000001', 'test_resource_1', '\A/test\.do\Z', '', 'url', 1, '2009-03-16');
insert into secured_resources_role(resource_id, authority) values ('web-000001', 'ROLE_USER');

insert into secured_resources (resource_id, resource_name, resource_pattern, description, resource_type, sort_order, create_date)
values ('mtd-000001', 'test_resource_1', 'org.egovframe.rte.fdl.security.web.CategoryController.selectCategoryList', '', 'method', 1, '2009-03-16');
insert into secured_resources (resource_id, resource_name, resource_pattern, description, resource_type, sort_order, create_date)
values ('mtd-000003', 'test_resource_3', 'execution(* org.egovframe.rte.security..service.*Service.insert*(..))', '', 'pointcut', 1000, '2009-03-16');
```

### 3) 네임스페이스 없이 직접 빈 구성 (src/test/resources/META-INF/spring/context-security.xml)

`<egov-security:*>` 대신 Spring Security XML을 직접 쓸 때는 다음 빈을 선언합니다 (발췌).

```xml
<beans:bean id="securedObjectService" class="org.egovframe.rte.fdl.security.securedobject.impl.SecuredObjectServiceImpl">
    <beans:property name="securedObjectDAO" ref="securedObjectDAO"/>
    <beans:property name="requestMatcherType" value="regex"/>
</beans:bean>
<beans:bean id="securedObjectDAO" class="org.egovframe.rte.fdl.security.securedobject.impl.SecuredObjectDAO">
    <beans:property name="dataSource" ref="dataSource"/>
</beans:bean>

<beans:bean id="requestMap" class="org.egovframe.rte.fdl.security.intercept.UrlResourcesMapFactoryBean" init-method="init">
    <beans:property name="securedObjectService" ref="securedObjectService"/>
</beans:bean>
<beans:bean id="databaseSecurityMetadataSource"
            class="org.egovframe.rte.fdl.security.intercept.EgovReloadableFilterInvocationSecurityMetadataSource">
    <beans:constructor-arg ref="requestMap"/>
    <beans:property name="securedObjectService" ref="securedObjectService"/>
</beans:bean>
<beans:bean id="filterSecurityInterceptor" class="org.springframework.security.web.access.intercept.FilterSecurityInterceptor">
    <beans:property name="authenticationManager" ref="org.springframework.security.authenticationManager"/>
    <beans:property name="accessDecisionManager" ref="accessDecisionManager"/>
    <beans:property name="securityMetadataSource" ref="databaseSecurityMetadataSource"/>
</beans:bean>

<http request-matcher="regex">
    <headers defaults-disabled="true"><frame-options policy="SAMEORIGIN"/></headers>
    <csrf disabled="true"/>
    <access-denied-handler error-page="/denied"/>
    <form-login login-processing-url="/j_spring_security_check"
                authentication-failure-url="/cvpl/EgovCvplLogin.do?login_error=1"
                default-target-url="/index.jsp?flag=L" login-page="/cvpl/EgovCvplLogin.do"/>
    <anonymous/>
    <logout logout-success-url="/cvpl/EgovCvplLogin.do"/>
    <custom-filter before="FILTER_SECURITY_INTERCEPTOR" ref="filterSecurityInterceptor"/>
</http>

<authentication-manager>
    <authentication-provider user-service-ref="jdbcUserService">
        <password-encoder ref="messageDigestPasswordEncoder"/>
    </authentication-provider>
</authentication-manager>
<beans:bean id="jdbcUserService" class="org.egovframe.rte.fdl.security.userdetails.jdbc.EgovJdbcUserDetailsManager">
    <beans:property name="usersByUsernameQuery" value="SELECT USER_ID,PASSWORD,ENABLED,USER_NAME,BIRTH_DAY,SSN FROM USERS WHERE USER_ID = ?"/>
    <beans:property name="authoritiesByUsernameQuery" value="SELECT USER_ID,AUTHORITY FROM AUTHORITIES WHERE USER_ID = ?"/>
    <beans:property name="roleHierarchy" ref="roleHierarchy"/>
    <beans:property name="dataSource" ref="dataSource"/>
    <beans:property name="mapClass" value="org.egovframe.rte.fdl.security.userdetails.EgovUserDetailsMapping"/>
</beans:bean>
```

메서드 보안은 같은 파일의 `methodSecurityMetadataSourceAdvisor` + `methodSecurityInterceptor` + `delegatingMethodSecurityMetadataSource`(`MapBasedMethodSecurityMetadataSource`(`MethodResourcesMapFactoryBean resourceType="method"`), `SecuredAnnotationSecurityMetadataSource`, `Jsr250MethodSecurityMetadataSource`) 빈으로 구성합니다.

### 4) 사용자 매핑 클래스 (src/test/java/.../userdetails/EgovUserDetailsMapping.java)

```java
public class EgovUserDetailsMapping extends EgovUsersByUsernameMapping {
    public EgovUserDetailsMapping(DataSource ds, String usersByUsernameQuery) {
        super(ds, usersByUsernameQuery);
    }
    @Override
    protected EgovUserDetails mapRow(ResultSet rs, int rownum) throws SQLException {
        EgovUserDetailsVO vo = new EgovUserDetailsVO();   // 업무 VO에 컬럼 매핑
        vo.setUserId(rs.getString("USER_ID"));
        // ...
        return new EgovUserDetails(rs.getString("USER_ID"), rs.getString("PASSWORD"), rs.getBoolean("ENABLED"), vo);
    }
}
```

### 5) 코드에서 인증 정보·재적재 사용

```java
Object vo = EgovUserDetailsHelper.getAuthenticatedUser();     // mapRow에서 넣은 업무 VO
List<String> roles = EgovUserDetailsHelper.getAuthorities();  // 권한 목록
Boolean loggedIn = EgovUserDetailsHelper.isAuthenticated();

// DB의 URL-권한 매핑 변경 후 재적재
EgovReloadableFilterInvocationSecurityMetadataSource mds =
        context.getBean("databaseSecurityMetadataSource", EgovReloadableFilterInvocationSecurityMetadataSource.class);
mds.reload();
```

### 테스트에서 다루는 주제

| 테스트 | 내용 |
|---|---|
| `EgovSecurityConfigTest` | `config-context.xml`(네임스페이스) 기반. 빈 목록, 필터 조회, 사용자별 인증 성공/실패(`BadCredentialsException`), 메서드·URL 권한 매핑 |
| `EgovSecurityServiceTest` | `context-security.xml`(직접 빈 구성) 기반 동일 시나리오 |
| `EgovSecurityHttpSecurityTest` | `sniff=false`가 기본 헤더 전체를 끄지 않음, `X-Frame-Options` 허용 값 검증, 로그인 실패 처리기 연결 |
| `EgovLoginFailHandlerSecurityTest` | 실패 원인이 달라도 세션에 남는 예외가 같음, 실패 URL 이동 |
| `CamelCaseUtilTest` | 언더스코어 → camelCase 변환 |

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Foundation/org.egovframe.rte.fdl.security -am
```

## 테스트

```bash
mvn test -pl Foundation/org.egovframe.rte.fdl.security
```

외부 서버는 필요하지 않습니다. `jdbc.properties`의 HSQLDB 인메모리 DB(`jdbc:hsqldb:mem:testdb`, `usingDBMS=hsql`)에 `sample_schema_hsql.sql`을 적재하여 실행합니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.fdl.security</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>`을 생략할 수 있습니다. `-egov.1` 포크 아티팩트(Spring Framework, Spring Security)를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록이 필요합니다. 웹 애플리케이션의 `web.xml`에는 Spring Security 표준대로 `springSecurityFilterChain`(`DelegatingFilterProxy`)을 등록해야 합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.string](../org.egovframe.rte.fdl.string/README.md), [org.egovframe.rte.fdl.access](../org.egovframe.rte.fdl.access/README.md)(Session 방식 접근제어), [org.egovframe.security.spring.boot.starter](../../Boot/org.egovframe.security.spring.boot.starter/README.md)(Spring Boot 자동구성)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
