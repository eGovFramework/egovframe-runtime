# egovframe-rte-fdl-security

`egovframe-rte-fdl-security`는 **Spring Security 6.x** 기반으로 **폼 로그인·세션·URL(요청) 단위 접근제어**를 구성하고, **DB에서 URL–권한 매핑·사용자·역할 계층**을 읽어 쓰도록 돕는 라이브러리입니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-fdl-security:5.0.0`
- **Runtime**: Java 17, Spring Framework 6.x, Spring Security 6.x

---

## 소스 디렉터리 구조

```
org/egovframe/rte/fdl/security/
├── config/           # Security Java Config, Config POJO, Reader, 캐시, AuthorizationManager
├── bean/             # Reloadable 메타데이터 소스, 핸들러, FactoryBean
├── secureobject/     # URL·역할 JDBC 서비스, DAO, RequestMatcher
└── userdetails/      # EgovUserDetails, JDBC 매핑, 역할 계층 FactoryBean, 헬퍼
    └── jdbc/
```

---

## 주요 동작 개요

- **인증**: `DaoAuthenticationProvider` + `EgovJdbcUserDetailsManager`(JDBC 사용자/권한 조회) + 설정에 따른 `PasswordEncoder`(BCrypt, SHA 계열, MD5, LDAP, noop 등).
- **URL 인가**: `FilterSecurityInterceptor` + `EgovReloadableFilterInvocationSecurityMetadataSource`로 **요청 URL과 매칭되는 권한**을 판단. `reload()` 시 DB 변경을 런타임에 반영 가능.
- **permitAll**: `permitAllList`(CSV)는 PathPattern 또는 정규식(`\A`, `^`, `\Z`, `$` 포함 시 `RegexRequestMatcher`)으로 처리.
- **그 외**: 동시 세션 제한, CSRF/헤더(XFO, XSS, Cache-Control), 로그인 실패·접근 거부 핸들러 등.

---

## 클래스 구성 및 특징

### 설정 (`org.egovframe.rte.fdl.security.config`)

| 클래스 | 특징 |
|--------|------|
| **`EgovSecurityConfiguration`** | `@EnableWebSecurity` 단일 `SecurityFilterChain` 구성. 폼 로그인/로그아웃, `FilterSecurityInterceptor` 삽입, `AuthenticationManager`, `PasswordEncoder`, JDBC UserDetails 등 전체 Bean 정의. |
| **`EgovSecurityConfig`** | `egov-security-config.properties`에 대응하는 설정 POJO(로그인 URL, JDBC 쿼리, `sqlRolesAndUrl`, `permitAllList`, CSRF, 동시 세션, 해시 방식 등). |
| **`EgovSecurityConfigReader`** | properties → `EgovSecurityConfig` 매핑. 기본 경로: `egovframework/egovProps/conf/egov-security-config.properties`. `Globals.SecurityConfigPath`로 경로 지정 가능. |
| **`EgovSecurityMetadataCache`** | URL 리소스 맵 등 메타데이터 캐시용. |
| **`EgovMultipleRoleAuthorizationManager`** | 동일 URL에 여러 권한이 매핑된 경우 **OR**로 허용하고, `RoleHierarchy`가 있으면 도달 가능 권한까지 고려하는 `AuthorizationManager`(URL 인가 확장 시 활용 가능). |

### Bean·필터 (`org.egovframe.rte.fdl.security.bean`)

| 클래스 | 특징 |
|--------|------|
| **`EgovReloadableFilterInvocationSecurityMetadataSource`** | `RequestMatcher` → `ConfigAttribute` 맵으로 URL별 권한 조회. `reload()` 시 `SecuredObjectService`로 DB를 다시 읽어 맵 갱신. |
| **`UrlResourcesMapFactoryBean`** | DB 기반 URL–권한 맵을 FactoryBean으로 제공. |
| **`MethodResourcesMapFactoryBean`** | **Spring Security 6.x에서는 DB 기반 메서드 권한 매핑이 지원되지 않아** `init()` 시 **빈 맵**만 반환. 메서드 보안은 아래 **어노테이션·Pointcut 방식**으로 서비스에서 별도 구성. |
| **`DataSourceFactoryBean`**, **`UsersQueryFactoryBean`**, **`AuthoritiesQueryFactoryBean`**, **`MapClassFactoryBean`**, **`RequestMatcherTypeFactoryBean`** | 설정·DataSource·JDBC 쿼리·UserDetails 매핑 클래스·요청 매처 타입(ant/regex 등) 주입. |
| **`EgovLoginFailHandler`**, **`EgovAccessDeniedHandler`** | 로그인 실패·접근 거부 처리. |

### 보호 자원·DAO (`org.egovframe.rte.fdl.security.secureobject`)

| 클래스 | 특징 |
|--------|------|
| **`EgovSecuredObjectService` / `SecuredObjectServiceImpl`** | DB에서 URL–역할 매핑, 정규식 매핑, 계층 역할 문자열 조회. |
| **`SecuredObjectDAO`** | 실제 JDBC 조회. |
| **`SelfRegexRequestMatcher`** | 정규식 기반 요청 매칭. |

### 사용자 정보 (`org.egovframe.rte.fdl.security.userdetails`)

| 클래스 | 특징 |
|--------|------|
| **`EgovJdbcUserDetailsManager`** | `JdbcUserDetailsManager` 확장, `EgovUserDetails` 및 커스텀 `jdbcMapClass` 매핑 지원. |
| **`EgovUserDetails`** | `User` 확장, 업무용 VO(`egovVO`) 등 부가 정보 보관. |
| **`EgovUserDetailsHelper`**, **`CamelCaseUtil`**, **`DefaultMapUserDetailsMapping`**, **`EgovUsersByUsernameMapping`** | 조회 결과 매핑·유틸. |
| **`HierarchyStringsFactoryBean`** | 역할 계층 문자열 로딩. |

### ARIA 등 (`impl/aria` 없음 — crypto와 혼동 주의)

이 모듈은 **웹 보안·JDBC UserDetails·URL 메타데이터** 중심입니다.

---

## 다른 서비스에서 사용하는 방법

### 1) Maven 의존성

```xml
<dependency>
  <groupId>org.egovframe.rte</groupId>
  <artifactId>egovframe-rte-fdl-security</artifactId>
  <version>5.0.0</version>
</dependency>
```

서비스에 **`DataSource`** Bean(설정의 `dataSource` 이름과 일치), **`spring-security-config`**가 끌려오며 **웹 애플리케이션**이어야 합니다.

### 2) 설정 파일

- 기본: 클래스패스 `egovframework/egovProps/conf/egov-security-config.properties`
- 또는 `globals.properties` 등에 다음 추가:

```properties
Globals.SecurityConfigPath=egovframework/egovProps/conf/egov-security-config.properties
```

설정 키는 `EgovSecurityConfig`의 프로퍼티명과 동일해야 합니다. (예: `loginUrl`, `loginProcessUrl`, `dataSource`, `jdbcUsersByUsernameQuery`, `jdbcAuthoritiesByUsernameQuery`, `sqlRolesAndUrl`, `requestMatcherType`, `permitAllList`, `hash`, `csrf` 등.)

### 3) Spring에 `EgovSecurityConfiguration` 로드

```java
import org.egovframe.rte.fdl.security.config.EgovSecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EgovSecurityConfiguration.class)
public class EgovSecurityImportConfig {
}
```

> **주의**: 애플리케이션에 이미 `@EnableWebSecurity` + `SecurityFilterChain`이 있다면 **중복**됩니다. 이 라이브러리는 **전용 `EgovSecurityConfiguration` 한 벌**을 쓰거나, 필요 시 서비스에서 설정을 분리·병합해야 합니다.

### 4) 런타임 URL 권한 갱신

`EgovReloadableFilterInvocationSecurityMetadataSource` Bean을 주입받아 `reload()` 호출 시 DB의 URL–권한 매핑을 다시 읽습니다.

---

## Spring Security — 메서드 기반 설정 예시

Spring Security 6에서는 **메서드 단위**는 주로 **`@EnableMethodSecurity` + SpEL 어노테이션**으로 둡니다.  
(`MethodResourcesMapFactoryBean`은 빈 맵만 제공하므로, **DB로 메서드 권한을 일괄 로딩하는 구 방식은 이 모듈에서 사용하지 않습니다**.)

### 별도 설정 클래스 (웹 설정과 분리)

`EgovSecurityConfiguration`에 `@EnableMethodSecurity`를 붙이지 않은 경우, **다른 `@Configuration`**에서만 켭니다.

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class MethodSecurityConfig {
}
```

### 서비스(또는 컴포넌트) 메서드

권한 문자열은 JDBC에서 오는 값과 맞춥니다. (예: `ROLE_ADMIN`, `ROLE_USER`)

```java
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class SampleAdminService {

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteAll() {
        // 관리자만 실행
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public void approve(String id) {
        // 둘 중 하나
    }
}
```

### 의존성

메서드 보안 AOP는 **`spring-aop`**가 클래스패스에 있어야 합니다. (`spring-security-config` 사용 시 보통 함께 해결됩니다.)

---

## Spring Security — Pointcut 방식 설정 예시

**Pointcut**으로 “특정 패키지/클래스의 모든 메서드”에 동일한 접근 규칙을 걸려면, **AspectJ 포인트컷 + `MethodSecurityInterceptor`** 조합을 쓸 수 있습니다.  
이때 서비스 모듈에 **`aspectjweaver`**(및 필요 시 **`spring-aspects`**)를 **compile/runtime** 스코프로 추가해야 합니다. (본 라이브러리 POM에서는 AspectJ가 test 스코프이므로 **서비스에서 명시 추가** 권장.)

### 예시: AspectJ Pointcut + `MethodSecurityInterceptor`

특정 패키지·네이밍 규칙의 Bean 메서드에 **동일 권한**을 한 번에 걸 때 사용하는 패턴입니다.  
`MapBasedMethodSecurityMetadataSource`의 **맵 키**는 Spring Security가 인식하는 **메서드 패턴**(예: `패키지.클래스.메서드*`)이어야 하며, 포인트컷 표현식과 **일치하도록** 맞춥니다.

```java
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.access.method.MapBasedMethodSecurityMetadataSource;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableAspectJAutoProxy
public class PointcutMethodSecurityConfig {

    @Bean
    public AccessDecisionManager methodAccessDecisionManager() {
        return new AffirmativeBased(List.of(new RoleVoter(), new AuthenticatedVoter()));
    }

    /**
     * 예: com.example.app.service 패키지의 *Service Bean public 메서드에 ROLE_USER 요구.
     * 패키지·클래스명·맵 키는 프로젝트에 맞게 수정.
     */
    @Bean
    public Advisor userServiceMethodSecurityAdvisor(
            AuthenticationConfiguration authConfiguration,
            AccessDecisionManager methodAccessDecisionManager) throws Exception {

        AuthenticationManager authenticationManager = authConfiguration.getAuthenticationManager();

        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(public * com.example.app.service..*Service.*(..))");

        Map<String, List<ConfigAttribute>> methodMap = new LinkedHashMap<>();
        methodMap.put("com.example.app.service.*Service.*",
                List.of(new SecurityConfig("ROLE_USER")));

        MapBasedMethodSecurityMetadataSource metadataSource =
                new MapBasedMethodSecurityMetadataSource(methodMap);

        MethodSecurityInterceptor interceptor = new MethodSecurityInterceptor();
        interceptor.setAuthenticationManager(authenticationManager);
        interceptor.setAccessDecisionManager(methodAccessDecisionManager);
        interceptor.setSecurityMetadataSource(metadataSource);

        return new DefaultPointcutAdvisor(pointcut, interceptor);
    }
}
```

> **참고**: Spring Security 마이너 버전에 따라 클래스 패키지·생성자 시그니처가 다를 수 있습니다. 빌드 오류 시 해당 버전 **Reference → Authorization → Method Security** 를 확인하세요. 운영 단순화를 위해 **가능하면 메서드 기반(@PreAuthorize)** 을 우선 검토하는 것이 좋습니다.

### Pointcut 사용 시 체크리스트

1. **`@EnableAspectJAutoProxy`** 또는 **AspectJ 로드타임 위빙(LTW)** 으로 AOP 활성화.
2. 서비스 `pom.xml`에 **`aspectjweaver`** 의존성 추가.
3. `MapBasedMethodSecurityMetadataSource`의 **메서드 패턴 키**는 패키지·클래스·메서드 시그니처 규칙에 맞게 지정.

---

## 빌드

```bash
# 이 모듈 디렉터리에서 실행
mvn clean package
```

## 테스트

```bash
# 단위/통합 테스트 실행
mvn test
```

## 최소 설정 예시

```xml
<dependency>
  <groupId>org.egovframe.rte</groupId>
  <artifactId>egovframe-rte-fdl-security</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 라이선스

Apache License 2.0
