# org.egovframe.security.spring.boot.starter

**Boot Starter 접근제어(Spring Security 방식) 모듈** 은 Spring Boot 2.7.18 자동구성(auto-configuration)으로 Spring Security 5.8.16-egov.1 기반
폼 로그인·세션 관리·URL 단위 접근제어를 구성하는 스타터입니다. 사용자·권한, "보호자원(URL)-권한" 매핑, 권한 계층(Role Hierarchy)을 DB에서 읽어 사용하며,
Foundation 레이어의 `org.egovframe.rte.fdl.security`(Spring XML 네임스페이스 설정 방식)와 같은 동작 모델을 `application.yml` 속성만으로 제공합니다.
`fdl.security` 모듈에 의존하지 않고 `org.egovframe.boot.security` 패키지에 필요한 클래스를 자체 포함합니다. 다른 실행환경 모듈이 이 모듈에 의존하지는 않습니다.

- **Artifact**: `org.egovframe.boot:org.egovframe.security.spring.boot.starter:4.3.1`
- **Runtime**: Java 1.8 / Spring Boot 2.7.18 / Spring Framework 5.3.39-egov.1 / Spring Security 5.8.16-egov.1
- **실행환경 의존**: 없음 (`org.egovframe.rte.*` 모듈을 의존하지 않음)
- **부모 POM**: 실행환경 루트 POM `org.egovframe.rte:org.egovframe.rte.root:4.3.1` (`spring-boot-starter-parent`가 아님)

---

## 특징

- `META-INF/spring.factories`로 등록되는 Spring Boot 2.7 방식 자동구성 (`EgovSecurityConfiguration`, `@EnableWebSecurity`)
- `Globals.auth=security` 속성이 있을 때만 자동구성 활성화 (`@ConditionalOnProperty`)
- `SecurityFilterChain` Bean 하나로 폼 로그인/로그아웃, 동시 세션 제어, CSRF·보안 헤더, 접근거부 처리를 구성
- `DaoAuthenticationProvider` + `EgovJdbcUserDetailsManager`(JDBC 사용자·권한 조회) + `hash` 속성에 따른 `PasswordEncoder`
- DB의 URL-권한 매핑을 `FilterSecurityInterceptor`와 `EgovReloadableFilterInvocationSecurityMetadataSource`로 적용하며 `reload()`로 런타임 재적재 가능
- URL 매칭 방식 `ant`(기본) / `regex` / `ciRegex`, 권한 계층은 DB 조회 또는 `role-hierarchy-string`으로 지정

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/boot/security/
├── EgovSecurityConfiguration.java                 # 자동구성 클래스 (SecurityFilterChain, AuthenticationManager 등 Bean 정의)
├── EgovSecurityProperties.java                    # @ConfigurationProperties(prefix="org.egovframe.security")
├── bean/
│   ├── DataSourceFactoryBean.java                 # data-source 속성으로 DataSource Bean 조회
│   ├── EgovAccessDeniedHandler.java               # 접근거부·CSRF 오류 시 forward
│   ├── EgovLoginFailHandler.java                  # 로그인 실패 메시지(loginFailMsg) 설정 후 forward
│   ├── EgovReloadableFilterInvocationSecurityMetadataSource.java  # URL-권한 메타데이터 (reload 지원)
│   ├── MethodResourcesMapFactoryBean.java         # method/pointcut-권한 맵 FactoryBean (자동구성에서 Bean으로 등록하지 않음)
│   ├── RequestMatcherTypeFactoryBean.java         # AnyRequestMatcher 반환
│   └── UrlResourcesMapFactoryBean.java            # URL-권한 맵 FactoryBean
├── secureobject/
│   ├── EgovSecuredObjectService.java              # 보호자원 조회 서비스 인터페이스
│   └── impl/
│       ├── SecuredObjectDAO.java                  # NamedParameterJdbcTemplate 기반 DAO (기본 SQL 내장)
│       ├── SecuredObjectServiceImpl.java
│       └── SelfRegexRequestMatcher.java           # RegexRequestMatcher 래퍼 (equals/hashCode 구현)
└── userdetails/
    ├── DefaultMapUserDetailsMapping.java          # 조회 컬럼을 Map(camelCase)으로 담는 기본 매핑
    ├── EgovUserDetails.java                       # User 확장 (egovVO 보관)
    ├── hierarchicalroles/HierarchyStringsFactoryBean.java   # 권한 계층 문자열 FactoryBean
    ├── jdbc/EgovJdbcUserDetailsManager.java       # JdbcUserDetailsManager 확장
    ├── jdbc/EgovUsersByUsernameMapping.java       # MappingSqlQuery<EgovUserDetails> 추상 클래스
    └── util/
        ├── CamelCaseUtil.java
        └── EgovUserDetailsHelper.java             # SecurityContext 기반 인증 사용자·권한 조회 헬퍼

src/main/resources/META-INF/spring.factories       # EnableAutoConfiguration 등록
```

`src/test`는 없습니다.

---

## 자동구성

### 등록 방식

`src/main/resources/META-INF/spring.factories` (Spring Boot 2.7 방식. `AutoConfiguration.imports` 파일은 사용하지 않음)

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.egovframe.boot.security.EgovSecurityConfiguration
```

### 활성화 조건

| 조건 | 내용 |
|---|---|
| `@ConditionalOnProperty(prefix="Globals", name="auth", havingValue="security")` | `Globals.auth=security`일 때만 `EgovSecurityConfiguration` 전체가 적용됨 |

`EgovSecurityConfiguration`은 `@Configuration`, `@EnableWebSecurity`, `@EnableConfigurationProperties(EgovSecurityProperties.class)`가 붙어 있습니다.
이 스타터는 `spring-boot-starter-security`를 함께 가져오므로, `Globals.auth`가 `security`가 아니면 이 자동구성은 빠지고 Spring Boot 기본 보안 자동구성이 동작합니다.

### `SecurityFilterChain` 구성 (`securityFilterChain(HttpSecurity)`)

| 항목 | 내용 |
|---|---|
| CSRF | `csrf=false`이면 `http.csrf().disable()` |
| 보안 헤더 | `sniff=false`이면 `headers().defaultsDisabled()`. `frameOptions()`를 비활성화하고 `StaticHeadersWriter("X-FRAME-OPTIONS", xframe-options)` 추가. `xss-protection=true`이면 `xssProtection().block(true)`. `cache-control=true`이면 `cacheControl().disable()` |
| 요청 범위 | `http.requestMatcher(AnyRequestMatcher)` (`RequestMatcherTypeFactoryBean`) |
| 인가 | `exclude-list`(쉼표 구분)의 각 패턴을 `antMatchers(pattern).permitAll()`, 그 외 `anyRequest().authenticated()` |
| 폼 로그인 | `loginPage(login-url)`, 파라미터명 `egov_security_username` / `egov_security_password`, `successForwardUrl(default-target-url)`, 실패 시 `EgovLoginFailHandler` |
| 로그아웃 | `logoutUrl(logout-url)`, `logoutSuccessUrl(logout-success-url)`, `clearAuthentication(true)` |
| 세션 | `CompositeSessionAuthenticationStrategy`(동시 세션 제어 → 세션 고정 보호 → 세션 등록), URL rewriting 비활성화, `ConcurrentSessionFilter` |
| URL-권한 | `FilterSecurityInterceptor`(DB 메타데이터 소스, `AffirmativeBased`)를 기본 `FilterSecurityInterceptor` 앞에 추가 |
| 기타 | `httpBasic()` 진입점 `LoginUrlAuthenticationEntryPoint(login-url)`, `accessDeniedHandler(EgovAccessDeniedHandler)`, `anonymous()` 허용 |

### `hash` 속성과 `PasswordEncoder`

| `hash` 값 | `PasswordEncoder` |
|---|---|
| `plaintext`, `noop` | `NoOpPasswordEncoder` |
| `md5` | `MessageDigestPasswordEncoder("MD5")` |
| `sha` | `MessageDigestPasswordEncoder("SHA-1")` |
| `sha-256` | `MessageDigestPasswordEncoder("SHA-256")` |
| `sha256` | `StandardPasswordEncoder` |
| `ldap` | `LdapShaPasswordEncoder` |
| 그 외(미지정 포함) | `BCryptPasswordEncoder` |

### 생성되는 Bean

| Bean 이름 | 타입 | 설명 |
|---|---|---|
| `authenticationManager` | `ProviderManager` | `DaoAuthenticationProvider`(`jdbcUserService`, `PasswordEncoder`) |
| `securityFilterChain` | `SecurityFilterChain` | 위 표의 구성 |
| `dataSourceFactoryBean` | `DataSourceFactoryBean` | `data-source` 이름의 `DataSource` Bean 반환, 미지정 시 `dataSource`, 없으면 `NoSuchBeanDefinitionException` |
| `egovLoginFailHandler` | `EgovLoginFailHandler` | 예외 종류별 메시지를 request 속성 `loginFailMsg`에 넣고 `access-denied-url`로 forward |
| `requestMatcherTypeFactoryBean` | `RequestMatcherTypeFactoryBean` | `AnyRequestMatcher.INSTANCE` |
| `egovAccessDeniedHandler` | `EgovAccessDeniedHandler` | `access-denied-url`로 forward, CSRF 토큰 오류는 `csrf-access-denied-url`로 forward |
| `loginUrlAuthenticationEntryPoint` | `LoginUrlAuthenticationEntryPoint` | `login-url` |
| `concurrentSessionControlAuthenticationStrategy` | `ConcurrentSessionControlAuthenticationStrategy` | `concurrent-max-sessons`, `error-if-maximum-exceeded` |
| `sessionFixationProtectionStrategy` | `SessionFixationProtectionStrategy` | |
| `sessionRegistry` | `SessionRegistryImpl` | |
| `concurrencyFilter` | `ConcurrentSessionFilter` | |
| `registerSessionAuthenticationStrategy` | `RegisterSessionAuthenticationStrategy` | |
| `sas` | `CompositeSessionAuthenticationStrategy` | 위 세 전략 조합 |
| `securedObjectDAO` | `SecuredObjectDAO` | `@PostConstruct`에서 `sql-*` 속성이 있으면 기본 SQL을 교체 |
| `securedObjectService` | `SecuredObjectServiceImpl` | `request-matcher-type`에 따라 `AntPathRequestMatcher` / `SelfRegexRequestMatcher` 생성 |
| `hierarchyStrings` | `HierarchyStringsFactoryBean` | `role-hierarchy-string`이 있으면 그 값, 없으면 DB(`sql-hierarchical-roles`) 조회 |
| `requestMap` | `UrlResourcesMapFactoryBean` | URL-권한 `LinkedHashMap` |
| `roleHierarchy` | `RoleHierarchyImpl` | `hierarchyStrings` 적용 |
| `jdbcUserService` | `EgovJdbcUserDetailsManager` | `jdbc-users-by-username-query`, `jdbc-authorities-by-username-query`, `jdbc-map-class`, `roleHierarchy` |
| `roleVoter` | `RoleVoter` | rolePrefix `""` |
| `authenticatedVoter` | `AuthenticatedVoter` | |
| `accessDecisionManager` | `AffirmativeBased` | `roleVoter` + `authenticatedVoter`, `allowIfAllAbstainDecisions=false` |
| `databaseSecurityMetadataSource` | `EgovReloadableFilterInvocationSecurityMetadataSource` | `requestMap` 기반. `reload()`로 DB 재조회 |
| `filterSecurityInterceptor` | `FilterSecurityInterceptor` | `authenticationManager` + `accessDecisionManager` + `databaseSecurityMetadataSource` |
| `usernamePasswordAuthenticationFilter` | `UsernamePasswordAuthenticationFilter` | `authenticationManager` 지정 |

---

## 설정 속성

prefix: `org.egovframe.security` (`EgovSecurityProperties`). 속성 키는 Spring Boot relaxed binding을 따르므로 kebab-case와 필드명(camelCase) 모두 사용할 수 있습니다.

| 속성 키 (`org.egovframe.security.` 생략) | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `login-url` | String | `/index.html` | 로그인 페이지 URL, `httpBasic` 진입점 |
| `logout-url` | String | 없음 | 로그아웃 처리 URL |
| `login-failure-url` | String | 없음 | 선언만 되어 있고 현재 자동구성에서 사용하지 않음 (로그인 실패는 `access-denied-url`로 forward) |
| `logout-success-url` | String | 없음 | 로그아웃 성공 후 이동 URL |
| `access-denied-url` | String | `/index.html` | 접근거부·로그인 실패 시 forward URL |
| `data-source` | String | 없음 | 사용할 `DataSource` Bean 이름. 미지정 시 `dataSource` |
| `jdbc-users-by-username-query` | String | `SELECT USER_ID, PASSWORD AS PASSWORD FROM USERS WHERE USER_ID = ?` | 사용자 조회 SQL. `DefaultMapUserDetailsMapping`은 결과 컬럼 `user_id`, `password`, `enabled`를 읽음 |
| `jdbc-authorities-by-username-query` | String | `SELECT USER_ID, AUTHOR_CODE AUTHORITY FROM AUTHORITIES WHERE USER_ID = ?` | 권한 조회 SQL |
| `jdbc-map-class` | String | `org.egovframe.boot.security.userdetails.DefaultMapUserDetailsMapping` | `EgovUsersByUsernameMapping` 구현 클래스 (생성자 `(DataSource, String)`) |
| `request-matcher-type` | String | 없음 (`SecuredObjectServiceImpl` 기본 `ant`) | `ant` / `regex` / `ciRegex`(대소문자 무시 정규식) |
| `hash` | String | 없음 (BCrypt) | 위 `PasswordEncoder` 표 참고 |
| `hash-base64` | boolean | `true` | `passwordEncoder()`에 전달되지만 현재 구현에서 사용하지 않음 |
| `concurrent-max-sessons` | int | `-1` | 사용자당 최대 세션 수 (-1: 무제한). 필드명 `concurrentMaxSessons` 그대로임 |
| `concurrent-expired-url` | String | `/index.html` | 선언만 되어 있고 현재 자동구성에서 사용하지 않음 |
| `error-if-maximum-exceeded` | boolean | `false` | 최대 세션 초과 시 예외 발생 여부 |
| `default-target-url` | String | 없음 | 로그인 성공 시 forward URL (`successForwardUrl`) |
| `always-use-default-target-url` | boolean | `false` | 선언만 되어 있고 현재 자동구성에서 사용하지 않음 |
| `sniff` | boolean | `false` | `false`이면 Spring Security 기본 보안 헤더 비활성화 |
| `xframe-options` | String | 없음 | `X-FRAME-OPTIONS` 헤더 값 (예: `SAMEORIGIN`) |
| `xss-protection` | boolean | `false` | `true`이면 `X-XSS-Protection` 헤더(block) 사용 |
| `cache-control` | boolean | `false` | `true`이면 Cache-Control 헤더 작성 비활성화 |
| `csrf` | boolean | `false` | `false`이면 CSRF 보호 비활성화 |
| `support-method` | boolean | `false` | 선언만 되어 있고 현재 자동구성에서 사용하지 않음 |
| `support-pointcut` | boolean | `false` | 선언만 되어 있고 현재 자동구성에서 사용하지 않음 |
| `csrf-access-denied-url` | String | `/index.html` | CSRF 토큰 오류 시 forward URL |
| `sql-roles-and-url` | String | `SecuredObjectDAO.DEF_ROLES_AND_URL_QUERY` | URL-권한 조회 SQL. 결과 컬럼 `url`, `authority` |
| `sql-roles-and-method` | String | `SecuredObjectDAO.DEF_ROLES_AND_METHOD_QUERY` | method-권한 조회 SQL. 결과 컬럼 `method`, `authority` |
| `sql-roles-and-pointcut` | String | `SecuredObjectDAO.DEF_ROLES_AND_POINTCUT_QUERY` | pointcut-권한 조회 SQL. 결과 컬럼 `pointcut`, `authority` |
| `sql-regex-matched-request-mapping` | String | `SecuredObjectDAO.DEF_REGEX_MATCHED_REQUEST_MAPPING_QUERY_ORACLE10G` | `getMatchedRequestMapping(url)`용 SQL (`:url` 파라미터) |
| `sql-hierarchical-roles` | String | `SecuredObjectDAO.DEF_HIERARCHICAL_ROLES_QUERY` | 권한 계층 조회 SQL. 결과 컬럼 `child`, `parent` |
| `role-hierarchy-string` | String | 없음 | 지정 시 DB 조회 대신 이 값을 권한 계층으로 사용 (예: `ROLE_ADMIN > ROLE_USER`) |
| `exclude-list` | String | 없음 | `permitAll` 처리할 Ant 패턴(쉼표 구분). 자동구성에서 `split(",")`을 호출하므로 반드시 지정 |

`SecuredObjectDAO`의 기본 SQL은 `SECURED_RESOURCES`, `SECURED_RESOURCES_ROLE`, `ROLES_HIERARCHY` 테이블을 사용합니다.
`Globals.auth`는 이 스타터의 `@ConditionalOnProperty` 조건 키이며 `EgovSecurityProperties`에는 포함되지 않습니다.

이 모듈은 `additional-spring-configuration-metadata.json`을 제공하지 않습니다. `spring-boot-configuration-processor`가 optional 의존성으로 선언되어 있으나
루트 POM의 `maven-compiler-plugin`이 `annotationProcessorPaths`에 Lombok만 지정하므로 빌드 시 `spring-configuration-metadata.json`은 생성되지 않습니다.

---

## 주요 클래스

| 클래스 | 설명 |
|---|---|
| `EgovJdbcUserDetailsManager` | `JdbcUserDetailsManager` 확장. `jdbc-map-class`를 리플렉션으로 생성하여 사용자 조회, 권한에 `RoleHierarchy` 적용, `getAuthenticatedUser()` 제공 |
| `EgovUserDetails` | Spring Security `User` 확장. `getEgovUserVO()`로 사용자 VO(기본 매핑에서는 조회 컬럼의 camelCase `Map`) 반환 |
| `DefaultMapUserDetailsMapping` | `user_id`, `password`, `enabled` 컬럼과 전체 컬럼 Map으로 `EgovUserDetails` 생성 |
| `EgovUserDetailsHelper` | `getAuthenticatedUser()`, `getAuthorities()`, `isAuthenticated()`(anonymousUser 제외), `getHashedPassword(password)`(SHA-256 `DelegatingPasswordEncoder`) |
| `SecuredObjectDAO` / `SecuredObjectServiceImpl` | URL·method·pointcut-권한 맵, 정규식 best-match, 권한 계층 문자열 조회 |
| `EgovReloadableFilterInvocationSecurityMetadataSource` | `FilterInvocationSecurityMetadataSource` 구현. `reload()` 호출 시 `getRolesAndUrl()`로 맵 교체 |
| `EgovLoginFailHandler` | `AuthenticationServiceException`, `BadCredentialsException`, `LockedException`, `DisabledException`, `AccountExpiredException`, `CredentialsExpiredException`별 `loginFailMsg` 설정 |
| `EgovAccessDeniedHandler` | `InvalidCsrfTokenException`, `MissingCsrfTokenException`은 `csrf-access-denied-url`, 그 외는 `access-denied-url`로 forward |
| `SelfRegexRequestMatcher` | `RegexRequestMatcher` 래퍼. 맵 키로 쓰기 위해 `equals`/`hashCode` 구현 |

---

## 설정 및 사용 예시

`application.yml` (아래 키는 모두 `EgovSecurityProperties` 필드와 `EgovSecurityConfiguration` 조건에 실제 존재하는 키입니다)

```yaml
Globals:
  auth: security

org:
  egovframe:
    security:
      data-source: dataSource
      login-url: /login.do
      logout-url: /logout.do
      logout-success-url: /index.do
      default-target-url: /main.do
      access-denied-url: /accessDenied.do
      csrf-access-denied-url: /accessDenied.do
      exclude-list: /login.do,/index.do,/css/**,/js/**,/images/**
      request-matcher-type: ant
      hash: sha-256
      concurrent-max-sessons: 1
      error-if-maximum-exceeded: false
      csrf: false
      sniff: true
      xframe-options: SAMEORIGIN
      xss-protection: true
      cache-control: false
      jdbc-users-by-username-query: SELECT USER_ID, PASSWORD, ENABLED FROM USERS WHERE USER_ID = ?
      jdbc-authorities-by-username-query: SELECT USER_ID, AUTHOR_CODE AUTHORITY FROM AUTHORITIES WHERE USER_ID = ?
      role-hierarchy-string: ROLE_ADMIN > ROLE_USER
```

로그인 폼은 `login-url` 페이지에서 `egov_security_username`, `egov_security_password` 파라미터로 `POST` 합니다.

인증 사용자·권한 조회

```java
import org.egovframe.boot.security.userdetails.util.EgovUserDetailsHelper;

Object userVO = EgovUserDetailsHelper.getAuthenticatedUser();   // EgovUserDetails.getEgovUserVO()
List<String> authorities = EgovUserDetailsHelper.getAuthorities();
Boolean authenticated = EgovUserDetailsHelper.isAuthenticated();
```

URL-권한 매핑이 DB에서 바뀐 경우 `EgovReloadableFilterInvocationSecurityMetadataSource` Bean(`databaseSecurityMetadataSource`)의 `reload()`를 호출하면 재기동 없이 반영됩니다.

---

## 주요 의존성

모듈 POM은 버전을 선언하지 않으며 루트 POM `dependencyManagement`에서 관리합니다.
Spring Boot는 `spring-boot-dependencies` BOM 전체가 아니라 아래 아티팩트만 개별 관리하고, Spring Framework·Spring Security는 루트 POM의
`spring-framework-bom`(5.3.39-egov.1), `spring-security-bom`(5.8.16-egov.1)으로 맞춥니다.

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.springframework.boot:spring-boot-autoconfigure` | 2.7.18 | compile |
| `org.springframework.boot:spring-boot-autoconfigure-processor` | 2.7.18 | optional |
| `org.springframework.boot:spring-boot-configuration-processor` | 2.7.18 | optional |
| `org.springframework.boot:spring-boot-starter-web` | 2.7.18 | compile (`spring-webmvc` 5.3.39-egov.1) |
| `org.springframework.boot:spring-boot-starter-jdbc` | 2.7.18 | compile (`spring-jdbc` 5.3.39-egov.1) |
| `org.springframework.boot:spring-boot-starter-security` | 2.7.18 | compile (`spring-security-config`, `spring-security-web`, `spring-security-core` 5.8.16-egov.1) |
| `org.projectlombok:lombok` | 1.18.36 | provided |

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Boot/org.egovframe.security.spring.boot.starter -am
```

결과물: `target/org.egovframe.security.spring.boot.starter-4.3.1.jar`, `-sources.jar`, `-javadoc.jar`

## 테스트

이 모듈에는 `src/test`가 없어 실행되는 테스트가 없습니다. `mvn test -pl Boot/org.egovframe.security.spring.boot.starter`는 컴파일만 수행합니다.

---

## Maven 의존성 추가

Spring Boot 적용 프로젝트는 [org.egovframe.boot.starter.parent](../../Parent/org.egovframe.boot.starter.parent/README.md) 4.3.1을 부모 POM으로 상속하고
스타터를 버전 없이 추가합니다. 부모 POM이 실행환경 루트 POM을 BOM으로 가져오므로 이 스타터(4.3.1)와 Spring 포크 버전이 함께 적용됩니다.

```xml
<parent>
    <groupId>org.egovframe.boot</groupId>
    <artifactId>org.egovframe.boot.starter.parent</artifactId>
    <version>4.3.1</version>
</parent>

<dependencies>
    <dependency>
        <groupId>org.egovframe.boot</groupId>
        <artifactId>org.egovframe.security.spring.boot.starter</artifactId>
    </dependency>
</dependencies>
```

부모 POM을 상속하지 않는 경우 `<version>4.3.1</version>`을 명시합니다.
`-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소를 프로젝트 `pom.xml` 또는 `settings.xml`에 등록해야 합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 부모 POM: [org.egovframe.boot.starter.parent](../../Parent/org.egovframe.boot.starter.parent/README.md)
- 같은 기능의 Foundation 모듈(Spring XML 설정 방식): [org.egovframe.rte.fdl.security](../../Foundation/org.egovframe.rte.fdl.security/README.md)
- 관련 스타터: [org.egovframe.access.spring.boot.starter](../org.egovframe.access.spring.boot.starter/README.md) (`Globals.auth=session`), [org.egovframe.crypto.spring.boot.starter](../org.egovframe.crypto.spring.boot.starter/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
