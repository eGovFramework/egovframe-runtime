# egovframe-rte-fdl-access

`egovframe-rte-fdl-access`는 **DB 기반의 “권한(Authority) - 보호자원(URL 패턴)” 매핑**을 읽어와, Spring MVC `HandlerInterceptor`로 **로그인 여부 확인 및 URL 접근제한**을 적용하는 라이브러리입니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-fdl-access:5.0.0`
- **Runtime**: Java 17 / Spring Framework 6.x (Spring MVC)

---

## 소스 디렉터리 구조

```
org/egovframe/rte/fdl/access/
├── bean/           # Authority·Resource 맵, DataSource 등 FactoryBean
├── config/         # EgovAccessConfig*, Reader, Java Config, HandlerMapping 후처리
├── interceptor/    # EgovAccessInterceptor, EgovAccessUtil
└── service/        # EgovAccessService, EgovUserDetailsHelper, DAO
    └── impl/
```

---

## 주요 동작 개요

- **인증 체크**: 세션에 `loginVO`가 없으면 로그인 URL로 리다이렉트
- **인가(권한별 접근 제한)**: 세션의 `accessUser`(사용자 식별자)로 권한 목록을 찾고, 권한에 매핑된 URL 패턴과 요청 URL이 매칭되면 통과
- **매칭 방식**: `regex`(기본) 또는 `ant`
- **적용 범위**: `mappingPath`에 매칭되는 요청에만 인터셉터 적용, `excludeList`는 제외

---

## 클래스 구성 및 특징

### 설정/부트스트랩 (`org.egovframe.rte.fdl.access.config`)

- **`EgovAccessConfig`**
  - 접근제어 설정을 담는 POJO
  - 주요 필드: `globalAuthen`, `mappingPath`, `dataSource`, `loginUrl`, `accessDeniedUrl`, `sqlAuthorityUser`, `sqlRoleAndUrl`, `requestMatcherType`, `excludeList`

- **`EgovAccessConfigReader`**
  - properties 파일을 읽어 `EgovAccessConfig`로 매핑
  - 경로 결정 순서:
    - `Globals.AccessConfigPath`(서비스 설정) → 실패 시 기본값 → 최종적으로 기본 설정 객체 생성
  - 기본 설정 파일 경로(클래스패스): `egovframework/egovProps/conf/egov-access-config.properties`

- **`EgovAccessConfiguration`**
  - (기존 XML의 대체) Java Config
  - `Globals.AccessConfigPath`로 설정을 로드하고, DAO/Service/메타데이터/후처리기를 Bean으로 구성

- **`EgovAccessHandlerMappingPostProcessor`**
  - `RequestMappingHandlerMapping`에 `MappedInterceptor(EgovAccessInterceptor)`를 **우선순위 0번**으로 주입
  - 동작 조건:
    - `globalAuthen=session` 일 때만 등록
    - `spring.profiles.active`가 비어있거나, 프로필 문자열에 `session`이 포함되면 등록

### DB 조회 및 매핑 (`org.egovframe.rte.fdl.access.service`, `...service.impl`, `...bean`)

- **`EgovAccessService` / `EgovAccessServiceImpl`**
  - DB에서 권한 사용자 목록과 권한-URL 목록을 조회하는 서비스 인터페이스/구현

- **`EgovAccessDao`**
  - `JdbcTemplate`로 조회 수행
  - SQL은 `EgovAccessConfig.sqlAuthorityUser`, `EgovAccessConfig.sqlRoleAndUrl`에서 주입

- **FactoryBeans**
  - **`DataSourceFactoryBean`**: `EgovAccessConfig.dataSource` 이름의 `DataSource` Bean을 찾아 사용(없으면 `dataSource` 기본)
  - **`AuthorityUserFactoryBean`**, **`RoleAndUrlFactoryBean`**: 설정에서 SQL을 읽어 DAO에 주입
  - **`AuthorityMapFactoryBean`**, **`ResourceMapFactoryBean`**: 기동 시 DB에서 목록을 읽어 `List<Map<String,Object>>` 형태로 보관

- **`AuthorityResourceMetadata`**
  - 권한목록/리소스맵을 **static 필드**로 보관(전역 공유)
  - `reload()`로 DB에서 다시 읽어 리스트를 갱신

### 인터셉터/유틸 (`org.egovframe.rte.fdl.access.interceptor`, `...service`)

- **`EgovAccessInterceptor`**
  - `preHandle()`에서 로그인 체크 → 권한별 URL 매칭 → 미허가 시 접근거부 URL로 리다이렉트

- **`EgovAccessUtil`**
  - URL 패턴 매칭 유틸
  - `antMatcher`(AntPathMatcher) / `regexMatcher`(정규식)

- **`EgovUserDetailsHelper`**
  - 세션 기반 사용자/권한 조회 유틸
  - 사용 세션 키:
    - `loginVO` (인증 여부 판단)
    - `accessUser` (권한 조회용 사용자 식별자)

---

## 다른 서비스에서 사용하는 방법

아래 예시는 “서비스(웹 애플리케이션)”에서 이 라이브러리를 붙이는 최소 절차입니다.

### 1) 의존성 추가 (Maven)

서비스 프로젝트의 `pom.xml`에 추가합니다.

```xml
<dependency>
  <groupId>org.egovframe.rte</groupId>
  <artifactId>egovframe-rte-fdl-access</artifactId>
  <version>5.0.0</version>
</dependency>
```

### 2) 설정 파일 준비 (`egov-access-config.properties`)

기본 탐색 경로는 클래스패스의 `egovframework/egovProps/conf/egov-access-config.properties` 입니다.

예시 파일(키 이름은 `EgovAccessConfig`의 프로퍼티명과 동일해야 합니다):

```properties
# 필수는 아니지만, 권장: 식별자
id=egovAccessConfig

# session 기반 인터셉터 등록 조건(기본값: session)
globalAuthen=session

# 인터셉터 적용 URL 패턴(기본값: /**/*.do)
mappingPath=/**/*.do

# 서비스 컨텍스트의 DataSource 빈 이름(기본값: dataSource)
dataSource=dataSource

# 인증/인가 실패 시 리다이렉트 URL
loginUrl=/uat/uia/egovLoginUsr.do
accessDeniedUrl=/uat/uia/egovLoginUsr.do?auth_error=1

# 매칭 방식: regex(기본) 또는 ant
requestMatcherType=regex

# 인터셉터 제외 목록(콤마 구분)
excludeList=/uat/uia/**, /index.do

# 권한 사용자 조회 SQL (컬럼 별칭: userid, authority)
sqlAuthorityUser=SELECT USER_ID AS userid, AUTHOR_CODE AS authority FROM AUTHORITIES

# 권한-URL 조회 SQL (컬럼 별칭: url, authority)
sqlRoleAndUrl=SELECT r.ROLE_PTTRN AS url, a.AUTHOR_CODE AS authority FROM ROLES r INNER JOIN AUTHROLES a ON r.ROLE_CODE = a.ROLE_CODE ORDER BY r.ROLE_SORT
```

### 3) 서비스 설정에 `Globals.AccessConfigPath` 지정

서비스에서 `Globals.AccessConfigPath`를 지정하면 해당 경로의 설정 파일을 우선 로드합니다.

- 예: `globals.properties`에 추가

```properties
Globals.AccessConfigPath=egovframework/egovProps/conf/egov-access-config.properties
```

경로는 다음 형태를 지원합니다.

- `classpath:...`
- `file:/absolute/path/...`
- 상대경로(기본적으로 classpath로 해석)

### 4) 스프링 컨텍스트에 설정 클래스 등록

서비스가 Spring Boot/Spring MVC 기반일 때, 아래 중 하나로 `EgovAccessConfiguration`이 로딩되도록 구성합니다.

- **Spring Boot**: 애플리케이션 시작 클래스나 설정 클래스에서 Import

```java
@Import(org.egovframe.rte.fdl.access.config.EgovAccessConfiguration.class)
public class AppConfig {
}
```

- **컴포넌트 스캔**으로 잡히는 구조라면(패키지 스캔 범위에 포함) 별도 설정 없이도 로딩될 수 있습니다.

### 5) 로그인 성공 시 세션 속성 세팅

이 라이브러리는 세션의 아래 키를 사용합니다.

- `loginVO`: 인증 여부 판단(존재하면 인증됨으로 간주)
- `accessUser`: 권한 매핑 조회용 사용자 식별자(예: 사용자 ID)

따라서 서비스의 로그인 처리(컨트롤러/필터 등)에서 아래처럼 세션에 값을 넣어줘야 합니다.

```java
request.getSession().setAttribute("loginVO", loginVO);
request.getSession().setAttribute("accessUser", userId);
```

### 6) DB(또는 SQL) 준비

기본 SQL은 아래 **컬럼 별칭**을 전제로 합니다.

- **권한 사용자 목록**: `userid`, `authority`
- **권한-URL 목록**: `url`, `authority`

테이블/컬럼명이 서비스마다 다르면, `sqlAuthorityUser`, `sqlRoleAndUrl` 값을 서비스에 맞게 변경해서 사용하세요.

---

## 운영 팁

- **적용 범위 조정**: `mappingPath`, `excludeList`로 “로그인 페이지/정적 리소스/홈” 등을 반드시 제외하세요.
- **패턴 타입 선택**: `requestMatcherType=ant`를 쓰면 `/admin/**` 같은 Ant 패턴을 사용할 수 있습니다.
- **권한 변경 반영**: `AuthorityResourceMetadata.reload()` 호출로 DB 변경을 런타임에 반영할 수 있습니다(호출 주체는 서비스에서 구현).

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
  <artifactId>egovframe-rte-fdl-access</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 라이선스

Apache License 2.0

