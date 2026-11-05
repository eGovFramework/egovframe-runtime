# org.egovframe.rte.fdl.access

**Foundation 접근제어(Session 방식) 모듈**은 DB에 저장된 "권한(authority) - 보호자원(URL 패턴)" 매핑을 기동 시 읽어 와, Spring MVC `HandlerInterceptor`로 로그인 여부 확인과 URL 접근제한을 적용하는 라이브러리입니다. Spring Security를 사용하지 않고 HTTP 세션 속성만으로 인증·인가를 판단하며, `<egov-access:config>` 커스텀 네임스페이스 하나로 설정을 간소화합니다. Spring Boot 환경에서는 `org.egovframe.access.spring.boot.starter`가 이 모듈을 사용합니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.fdl.access:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 (Spring MVC, JdbcTemplate) / javax.servlet-api 4.0.1
- **실행환경 의존**: `org.egovframe.rte.fdl.property`

---

## 특징

- **인증 체크**: 세션에 `loginVO` 속성이 없으면 `loginUrl`로 리다이렉트
- **인가(권한별 접근제한)**: 세션의 `accessUser`(사용자 식별자)로 권한 목록을 찾고, 권한에 매핑된 URL 패턴과 요청 URL이 매칭되면 통과, 아니면 `accessDeniedUrl`로 리다이렉트
- **매칭 방식**: `requestMatcherType`에 따라 `regex`(기본, `java.util.regex`) 또는 `ant`(`AntPathMatcher`)
- **적용 범위**: `mappingPath`에 매칭되는 요청에만 인터셉터를 적용하고 `excludeList`(콤마 구분)는 제외. 설정 파서가 `<mvc:interceptors>` 정의를 동적으로 생성하여 등록
- **요청 경로 정규화**: `UrlPathHelper.getPathWithinApplication()`으로 디코딩·contextPath 제거 후 중복 슬래시와 `.`/`..` 세그먼트를 정규화하여 경로 조작으로 인가 패턴을 우회하지 못하도록 함
- **런타임 재적재**: `AuthorityResourceMetadata.reload()`로 DB의 권한·자원 매핑을 재시작 없이 갱신 (새 리스트로 static 참조를 원자적으로 교체)
- **프로파일 연동**: `globalAuthen="session"`이고 시스템 속성 `spring.profiles.active`가 비어 있거나 `session`을 포함할 때만 인터셉터와 내부 Bean을 등록

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/fdl/access/
├── bean/
│   ├── AccessDataSourceFactoryBean.java   # 설정의 dataSource 또는 기본 "dataSource" Bean 반환
│   ├── AuthorityMapFactoryBean.java       # 기동 시 권한-사용자 목록 조회·보관
│   ├── AuthorityResourceMetadata.java     # 권한/자원 목록 static 보관, reload()
│   ├── AuthorityUserFactoryBean.java      # sqlAuthorityUser SQL 문자열 제공
│   ├── ResourceMapFactoryBean.java        # 기동 시 권한-URL 목록 조회·보관
│   └── RoleAndUrlFactoryBean.java         # sqlRoleAndUrl SQL 문자열 제공
├── config/
│   ├── EgovAccessConfig.java                     # 설정 POJO
│   ├── EgovAccessConfigBeanDefinitionParser.java # <egov-access:config> 파서
│   ├── EgovAccessConfigNameHandler.java          # 네임스페이스 핸들러
│   └── EgovAccessConfigShare.java                # loginUrl 등 static 공유값
├── interceptor/
│   ├── EgovAccessInterceptor.java         # HandlerInterceptor 구현
│   └── EgovAccessUtil.java                # ant/regex 매처
└── service/
    ├── EgovAccessService.java
    ├── EgovUserDetailsHelper.java         # 세션 기반 사용자/권한 조회
    └── impl/
        ├── EgovAccessDao.java             # JdbcTemplate 조회
        └── EgovAccessServiceImpl.java

src/main/resources/
├── META-INF/spring.handlers               # egov-access 네임스페이스 → EgovAccessConfigNameHandler
├── META-INF/spring.schemas                # egov-access.xsd / egov-access-4.3.1.xsd 매핑
├── META-INF/spring/access/access-config.xml   # 파서가 로드하는 내부 Bean 정의
└── org/egovframe/rte/fdl/access/config/egov-access-4.3.1.xsd
```

---

## 주요 클래스

| 클래스 | 설명 |
|---|---|
| `config.EgovAccessConfig` | `<egov-access:config>` 속성을 담는 POJO. 필드: `globalAuthen`, `dataSource`(DataSource 참조), `loginUrl`, `accessDeniedUrl`, `sqlAuthorityUser`, `sqlRoleAndUrl`, `requestMatcherType`, `excludeList`, `mappingPath` |
| `config.EgovAccessConfigBeanDefinitionParser` | `AbstractSingleBeanDefinitionParser` 구현. 속성을 `EgovAccessConfig`에 매핑하고, `loginUrl`·`accessDeniedUrl`·`requestMatcherType`을 `EgovAccessConfigShare`에 저장. `globalAuthen="session"`이면 `mappingPath`/`excludeList`로 `<mvc:interceptors>` XML을 생성해 로드한 뒤 `classpath:/META-INF/spring/access/access-config.xml`을 로드 |
| `config.EgovAccessConfigNameHandler` | `NamespaceHandlerSupport` 구현. `config` 엘리먼트에 위 파서 등록 |
| `config.EgovAccessConfigShare` | static 기본값: `DEF_LOGIN_URL`(`/uat/uia/egovLoginUsr.do`), `DEF_ACCESS_DENIED_URL`(`/uat/uia/egovLoginUsr.do?auth_error=1`), `DEF_REQUEST_MATCH_TYPE`(`regex`) |
| `interceptor.EgovAccessInterceptor` | `HandlerInterceptor`, `ApplicationContextAware`. `preHandle()`에서 요청 경로 정규화 → `EgovUserDetailsHelper.isAuthenticated()` 인증 체크 → `authCheck(requestUrl)` 권한별 URL 매칭 → 실패 시 리다이렉트 |
| `interceptor.EgovAccessUtil` | `antMatcher(pattern, input)`(`AntPathMatcher.match`), `regexMatcher(pattern, input)`(`Pattern`/`Matcher.find`) |
| `service.EgovUserDetailsHelper` | `RequestContextHolder`의 세션 속성 조회 유틸. `getAuthenticatedUser()`(`loginVO`), `isAuthenticated()`, `getAuthorities()`(`accessUser`로 권한 코드 목록), `getRoles()`(권한-URL 목록) |
| `service.EgovAccessService` / `service.impl.EgovAccessServiceImpl` | `getAuthorityUser()`, `getRoleAndUrl()` 서비스 인터페이스/구현 |
| `service.impl.EgovAccessDao` | `JdbcTemplate.queryForList()`로 두 SQL을 실행. `dataSource`, `authorityUserQuery`, `roleAndUrlQuery` 프로퍼티 |
| `bean.AccessDataSourceFactoryBean` | `EgovAccessConfig.dataSource`가 있으면 사용, 없으면 컨텍스트의 `dataSource` Bean, 둘 다 없으면 `NoSuchBeanDefinitionException` |
| `bean.AuthorityUserFactoryBean` / `bean.RoleAndUrlFactoryBean` | 설정의 `sqlAuthorityUser` / `sqlRoleAndUrl` 문자열을 `FactoryBean<String>`으로 제공 |
| `bean.AuthorityMapFactoryBean` / `bean.ResourceMapFactoryBean` | `init()`에서 서비스로 목록을 조회해 `List<Map<String,Object>>`로 보관 (`init-method="init"`) |
| `bean.AuthorityResourceMetadata` | 권한 목록·자원 맵을 `static volatile` 필드로 보관(`getAuthorityList()`, `getResourceMap()`). `reload()`로 DB에서 다시 읽어 교체 |

`access-config.xml`에는 `accessDataSourceFactoryBean`, `authorityUserFactoryBean`, `roleAndUrlFactoryBean`, `egovAccessDao`, `egovAccessService`, `authorityMap`, `resourceMap`, `authorityResource` Bean이 정의되어 있으며 파서가 자동으로 로드합니다.

---

## 주요 의존성

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.property` | 4.3.1 | compile |
| `spring-context-support`, `spring-webmvc` | 5.3.39-egov.1 | compile (`spring-jdbc`, `spring-web` 전이) |
| `javax.servlet:javax.servlet-api` | 4.0.1 | provided |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `com.mysql:mysql-connector-j` | 8.4.0 | compile |
| `spring-test`, `junit`, `easymock`, `log4jdbc` | 5.3.39-egov.1 / 4.13.2 / 5.2.0 / 1.2 | test |

버전은 루트 POM(`org.egovframe.rte.root` 4.3.1)의 `dependencyManagement`에서 관리하며 모듈 POM은 버전을 선언하지 않습니다.

---

## 설정 및 사용 예시

### 1) Spring XML 설정 (`src/test/resources/META-INF/spring/context-access.xml` 기준)

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:egov-access="http://maven.egovframe.go.kr/schema/egov-access"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://maven.egovframe.go.kr/schema/egov-access http://maven.egovframe.go.kr/schema/egov-access/egov-access-4.3.1.xsd">

    <egov-access:config id="egovAccessConfig"
        globalAuthen="session"
        mappingPath="/**/*.do"
        dataSource="dataSource"
        loginUrl="/uat/uia/egovLoginUsr.do"
        accessDeniedUrl="/uat/uia/egovLoginUsr.do?auth_error=1"
        sqlAuthorityUser="select concat(b.user_se, b.user_id) userid, a.author_code authority
            from comtnemplyrscrtyestbs a, comvnusermaster b
            where a.scrty_dtrmn_trget_id = b.esntl_id"
        sqlRoleAndUrl="select a.role_pttrn url, b.author_code authority
            from comtnroleinfo a, comtnauthorrolerelate b
            where a.role_code = b.role_code and a.role_ty = 'url'
            order by a.role_sort"
        requestMatcherType="regex"
        excludeList="/uat/uia/**, /index.do, /EgovLeft.do, /EgovContent.do, /EgovTop.do, /EgovBottom.do" />
</beans>
```

| 속성 | 설명 |
|---|---|
| `globalAuthen` | 인증 방식. `session`일 때만 인터셉터와 내부 Bean이 등록됨 |
| `mappingPath` | 인터셉터를 적용할 URL 패턴 (`<mvc:mapping path>`) |
| `dataSource` | 권한 조회에 사용할 `DataSource` Bean 이름 |
| `loginUrl` | 미인증 시 리다이렉트 URL (contextPath가 앞에 붙음) |
| `accessDeniedUrl` | 권한 없음 시 리다이렉트 URL |
| `sqlAuthorityUser` | 권한-사용자 조회 SQL. 컬럼 별칭 `userid`, `authority` 필수 |
| `sqlRoleAndUrl` | 권한-URL 조회 SQL. 컬럼 별칭 `url`, `authority` 필수 |
| `requestMatcherType` | `regex` 또는 `ant` |
| `excludeList` | 인터셉터 제외 경로(콤마 구분, `<mvc:exclude-mapping path>`) |

XSD상 모든 속성은 `use="required"`입니다. `DataSource` Bean(예: `DriverManagerDataSource`)은 별도로 정의해야 합니다.

### 2) 로그인 성공 시 세션 속성 설정

이 모듈은 Spring Security 없이 아래 세션 키만 사용하므로 로그인 처리 코드에서 값을 넣어야 합니다.

```java
request.getSession().setAttribute("loginVO", loginVO);   // 존재하면 인증된 것으로 판단
request.getSession().setAttribute("accessUser", userId);  // sqlAuthorityUser의 userid 와 비교되는 식별자
```

### 3) 권한 변경 런타임 반영

```java
@Resource(name = "authorityResource")
private AuthorityResourceMetadata authorityResource;

authorityResource.reload();   // DB에서 권한-사용자, 권한-URL 목록을 다시 읽음
```

### 4) 프로파일 제어

파서는 시스템 속성 `spring.profiles.active`가 비어 있거나 `session` 문자열을 포함할 때만 인터셉터를 등록합니다. `-Dspring.profiles.active=security` 처럼 지정하면 이 모듈의 인터셉터가 등록되지 않습니다.

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Foundation/org.egovframe.rte.fdl.access -am
```

## 테스트

```bash
mvn test -pl Foundation/org.egovframe.rte.fdl.access
```

- `EgovAccessConfigTest`는 `classpath*:/META-INF/spring/*.xml`로 컨텍스트를 올리며 **MySQL 서버가 필요**합니다.
  `src/test/resources/META-INF/spring/test-jdbc.properties` 기준: `jdbc:log4jdbc:mysql://127.0.0.1:3306/com?serverTimezone=UTC`, 계정 `com` / `com01`
  (드라이버 `net.sf.log4jdbc.DriverSpy`). 공통컴포넌트 테이블(`comtnemplyrscrtyestbs`, `comvnusermaster`, `comtnroleinfo`, `comtnauthorrolerelate`)과 사용자 `GNRUSER`가 있어야 통과합니다.
- `EgovAccessInterceptorSecurityTest`는 `MockHttpServletRequest`만 사용하는 단위 테스트로 DB가 필요 없습니다 (경로 정규화·우회 차단 검증).

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.fdl.access</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>`을 생략할 수 있습니다. `-egov.1` 포크 아티팩트(Spring Framework 등)를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록이 필요합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.property](../org.egovframe.rte.fdl.property/README.md), [org.egovframe.rte.fdl.security](../org.egovframe.rte.fdl.security/README.md) (Spring Security 기반 접근제어), [org.egovframe.access.spring.boot.starter](../../Boot/org.egovframe.access.spring.boot.starter/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
