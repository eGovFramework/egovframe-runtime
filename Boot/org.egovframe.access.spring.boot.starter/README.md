# org.egovframe.access.spring.boot.starter

**Boot Starter 접근제어(Session 방식) 모듈** 은 Spring Boot 2.7.18 자동구성(auto-configuration)으로 세션 기반 접근제어를 제공하는 스타터입니다.
DB에서 "사용자-권한" 목록과 "권한-URL 패턴" 목록을 읽어 두고, Spring MVC `HandlerInterceptor`로 로그인 여부 확인과 URL 단위 접근제한을 수행합니다.
Foundation 레이어의 `org.egovframe.rte.fdl.access`(Spring XML 네임스페이스 설정 방식)와 같은 동작 모델을 `application.yml` 속성만으로 구성할 수 있도록 한 것으로,
`fdl.access` 모듈에 의존하지 않고 `org.egovframe.boot.access` 패키지에 필요한 클래스를 자체 포함합니다. 다른 실행환경 모듈이 이 모듈에 의존하지는 않습니다.

- **Artifact**: `org.egovframe.boot:org.egovframe.access.spring.boot.starter:4.3.1`
- **Runtime**: Java 1.8 / Spring Boot 2.7.18 / Spring Framework 5.3.39-egov.1 (spring-webmvc, spring-jdbc)
- **실행환경 의존**: 없음 (`org.egovframe.rte.*` 모듈을 의존하지 않음)
- **부모 POM**: 실행환경 루트 POM `org.egovframe.rte:org.egovframe.rte.root:4.3.1` (`spring-boot-starter-parent`가 아님)

---

## 특징

- `META-INF/spring.factories`로 등록되는 Spring Boot 2.7 방식 자동구성 (`EgovAccessConfiguration`)
- `Globals.auth=session` 속성이 있을 때만 자동구성 활성화 (`@ConditionalOnProperty`)
- `org.egovframe.access.*` 속성(`@ConfigurationProperties`)만으로 DataSource, SQL, 로그인/접근거부 URL, 적용/제외 경로를 지정
- 세션 속성 `loginVO`(로그인 여부)와 `accessUser`(사용자 ID)를 기준으로 인증·인가 판단
- URL 매칭 방식은 정규식(기본) 또는 Ant 패턴(`request-matcher-type: ant`)
- 기동 시 DB에서 읽은 권한·자원 목록을 `AuthorityResourceMetadata`에 보관하며 `reload()`로 런타임 재적재 가능

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/boot/access/
├── EgovAccessConfiguration.java        # 자동구성 클래스 (WebMvcConfigurer, 인터셉터 등록, Bean 정의)
├── EgovAccessProperties.java           # @ConfigurationProperties(prefix="org.egovframe.access")
├── bean/
│   ├── AuthorityMapFactoryBean.java    # 사용자-권한 목록 FactoryBean
│   ├── AuthorityResourceMetadata.java  # 권한·자원 목록 보관(static) 및 reload()
│   ├── AuthorityUserFactoryBean.java   # sql-authority-user 값 제공 FactoryBean
│   ├── DataSourceFactoryBean.java      # data-source 속성으로 DataSource Bean 조회
│   ├── ResourceMapFactoryBean.java     # 권한-URL 목록 FactoryBean
│   └── RoleAndUrlFactoryBean.java      # sql-role-and-url 값 제공 FactoryBean
├── interceptor/
│   ├── EgovAccessInterceptor.java      # HandlerInterceptor (인증 체크, URL 권한 체크)
│   └── EgovAccessUtil.java             # Ant / 정규식 매칭 유틸
└── service/
    ├── EgovAccessService.java          # 권한·자원 조회 서비스 인터페이스
    ├── EgovUserDetailsHelper.java      # 세션 기반 인증 사용자·권한 조회 헬퍼
    └── impl/
        ├── EgovAccessDao.java          # JdbcTemplate 기반 조회 DAO
        └── EgovAccessServiceImpl.java

src/main/resources/META-INF/spring.factories   # EnableAutoConfiguration 등록
```

`src/test`는 없습니다.

---

## 자동구성

### 등록 방식

`src/main/resources/META-INF/spring.factories` (Spring Boot 2.7 방식. `AutoConfiguration.imports` 파일은 사용하지 않음)

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.egovframe.boot.access.EgovAccessConfiguration
```

### 활성화 조건

| 조건 | 내용 |
|---|---|
| `@ConditionalOnProperty(prefix="Globals", name="auth", havingValue="session")` | `Globals.auth=session`일 때만 `EgovAccessConfiguration` 전체가 적용됨 |
| `org.egovframe.access.global-authen=session` | `addInterceptors()`에서 이 값이 `session`일 때만 `EgovAccessInterceptor`를 등록. 다른 값이면 오류 로그만 남기고 인터셉터를 등록하지 않음 |

`EgovAccessConfiguration`은 `@EnableConfigurationProperties(EgovAccessProperties.class)`로 속성 클래스를 활성화하고 `WebMvcConfigurer`를 구현하여
`mapping-path`에 인터셉터를 추가하고 `exclude-list`(쉼표 구분)를 제외 경로로 등록합니다.

### 생성되는 Bean

| Bean 이름 | 타입 | 설명 |
|---|---|---|
| `accessDataSourceFactoryBean` | `DataSourceFactoryBean` | `data-source` 속성에 지정한 이름의 `DataSource` Bean을 반환. 미지정 시 `dataSource` Bean 사용, 없으면 `NoSuchBeanDefinitionException` |
| `authorityUserFactoryBean` | `AuthorityUserFactoryBean` | `sql-authority-user` 문자열 반환 (비어 있으면 예외) |
| `roleAndUrlFactoryBean` | `RoleAndUrlFactoryBean` | `sql-role-and-url` 문자열 반환 (비어 있으면 예외) |
| `egovAccessDao` | `EgovAccessDao` | `JdbcTemplate.queryForList()`로 두 SQL 실행 |
| `egovAccessService` | `EgovAccessServiceImpl` | DAO 위임 서비스 |
| `authorityMap` | `AuthorityMapFactoryBean` | `initMethod="init"`: 기동 시 사용자-권한 목록 적재 |
| `resourceMap` | `ResourceMapFactoryBean` | `initMethod="init"`: 기동 시 권한-URL 목록 적재 |
| `authorityResourceMetadata` | `AuthorityResourceMetadata` | 두 목록을 static으로 보관. `reload()` 호출 시 DB에서 다시 읽어 교체 |

---

## 설정 속성

prefix: `org.egovframe.access` (`EgovAccessProperties`). 모든 속성은 `String`이며 **기본값이 없습니다**.
속성 키는 Spring Boot relaxed binding을 따르므로 `global-authen`, `globalAuthen` 모두 사용할 수 있습니다.

| 속성 키 (`org.egovframe.access.` 생략) | 필드 | 설명 |
|---|---|---|
| `global-authen` | `globalAuthen` | `session`일 때만 인터셉터 등록 |
| `mapping-path` | `mappingPath` | 인터셉터를 적용할 경로 패턴 (`addPathPatterns`) |
| `data-source` | `dataSource` | 사용할 `DataSource` Bean 이름. 미지정 시 `dataSource` |
| `login-url` | `loginUrl` | 미인증(세션에 `loginVO` 없음) 시 리다이렉트할 URL (contextPath가 앞에 붙음) |
| `access-denied-url` | `accessDeniedUrl` | 권한 없음 시 리다이렉트할 URL (contextPath가 앞에 붙음) |
| `sql-authority-user` | `sqlAuthorityUser` | 사용자-권한 조회 SQL. 결과 컬럼 `userid`, `authority` 필요 |
| `sql-role-and-url` | `sqlRoleAndUrl` | 권한-URL 패턴 조회 SQL. 결과 컬럼 `authority`, `url` 필요 |
| `request-matcher-type` | `requestMatcherType` | `ant`이면 `AntPathMatcher`, 그 외(미지정 포함)는 `java.util.regex` `find()` 매칭 |
| `exclude-list` | `excludeList` | 인터셉터 제외 경로(쉼표 구분). 자동구성에서 `split(",")`을 호출하므로 반드시 지정 |

`Globals.auth`는 이 스타터의 `@ConditionalOnProperty` 조건 키이며 `EgovAccessProperties`에는 포함되지 않습니다.

이 모듈은 `additional-spring-configuration-metadata.json`을 제공하지 않습니다. `spring-boot-configuration-processor`가 optional 의존성으로 선언되어 있으나
루트 POM의 `maven-compiler-plugin`이 `annotationProcessorPaths`에 Lombok만 지정하므로 빌드 시 `spring-configuration-metadata.json`은 생성되지 않습니다.

### 인터셉터 동작 (`EgovAccessInterceptor.preHandle`)

1. 요청 URI에서 contextPath를 제거한 경로를 구함
2. `EgovUserDetailsHelper.isAuthenticated()`: 세션 속성 `loginVO`가 없으면 `login-url`로 리다이렉트
3. 세션 속성 `accessUser`(사용자 ID)로 `AuthorityResourceMetadata`의 사용자-권한 목록에서 권한을 찾고, 권한에 매핑된 URL 패턴 중 하나라도 요청 경로와 매칭되면 통과
4. 매칭되지 않으면 `access-denied-url`로 리다이렉트

로그인 처리 시 세션에 `loginVO`(로그인 사용자 객체)와 `accessUser`(SQL 결과의 `userid`와 비교할 값)를 넣는 것은 적용 프로젝트의 책임입니다.

---

## 설정 및 사용 예시

`application.yml` (아래 키는 모두 `EgovAccessProperties` 필드와 `EgovAccessConfiguration` 조건에 실제 존재하는 키입니다)

```yaml
Globals:
  auth: session

org:
  egovframe:
    access:
      global-authen: session
      mapping-path: /**
      data-source: dataSource
      login-url: /login.do
      access-denied-url: /accessDenied.do
      request-matcher-type: regex
      exclude-list: /login.do,/accessDenied.do,/css/**,/js/**
      sql-authority-user: SELECT USER_ID AS userid, AUTHOR_CODE AS authority FROM AUTHORITIES
      sql-role-and-url: SELECT AUTHORITY AS authority, URL AS url FROM ROLE_URL ORDER BY SORT_ORDER
```

Java 코드에서 인증 사용자·권한 조회

```java
import org.egovframe.boot.access.service.EgovUserDetailsHelper;

Object loginVO = EgovUserDetailsHelper.getAuthenticatedUser();   // 세션 "loginVO"
List<String> authorities = EgovUserDetailsHelper.getAuthorities(); // 세션 "accessUser" 기준 권한 목록
Boolean authenticated = EgovUserDetailsHelper.isAuthenticated();
```

권한·자원 정보가 DB에서 바뀐 경우 `AuthorityResourceMetadata` Bean의 `reload()`를 호출하면 재기동 없이 반영됩니다.

---

## 주요 의존성

모듈 POM은 버전을 선언하지 않으며 루트 POM `dependencyManagement`에서 관리합니다.
Spring Boot는 `spring-boot-dependencies` BOM 전체가 아니라 아래 아티팩트만 개별 관리하고, Spring Framework는 루트 POM의 `spring-framework-bom`(5.3.39-egov.1)으로 맞춥니다.

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.springframework.boot:spring-boot-autoconfigure` | 2.7.18 | compile |
| `org.springframework.boot:spring-boot-autoconfigure-processor` | 2.7.18 | optional |
| `org.springframework.boot:spring-boot-configuration-processor` | 2.7.18 | optional |
| `org.springframework.boot:spring-boot-starter-web` | 2.7.18 | compile (`spring-webmvc` 5.3.39-egov.1) |
| `org.springframework.boot:spring-boot-starter-jdbc` | 2.7.18 | compile (`spring-jdbc` 5.3.39-egov.1) |
| `org.projectlombok:lombok` | 1.18.36 | provided |

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Boot/org.egovframe.access.spring.boot.starter -am
```

결과물: `target/org.egovframe.access.spring.boot.starter-4.3.1.jar`, `-sources.jar`, `-javadoc.jar`

## 테스트

이 모듈에는 `src/test`가 없어 실행되는 테스트가 없습니다. `mvn test -pl Boot/org.egovframe.access.spring.boot.starter`는 컴파일만 수행합니다.

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
        <artifactId>org.egovframe.access.spring.boot.starter</artifactId>
    </dependency>
</dependencies>
```

부모 POM을 상속하지 않는 경우 `<version>4.3.1</version>`을 명시합니다.
`-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소를 프로젝트 `pom.xml` 또는 `settings.xml`에 등록해야 합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 부모 POM: [org.egovframe.boot.starter.parent](../../Parent/org.egovframe.boot.starter.parent/README.md)
- 같은 기능의 Foundation 모듈(Spring XML 설정 방식): [org.egovframe.rte.fdl.access](../../Foundation/org.egovframe.rte.fdl.access/README.md)
- 관련 스타터: [org.egovframe.security.spring.boot.starter](../org.egovframe.security.spring.boot.starter/README.md) (`Globals.auth=security`), [org.egovframe.crypto.spring.boot.starter](../org.egovframe.crypto.spring.boot.starter/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
