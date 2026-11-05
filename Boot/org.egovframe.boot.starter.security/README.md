# egovframe-boot-starter-security

전자정부 표준프레임워크의 Spring Boot Security Starter 모듈입니다.

## 📋 개요

`egovframe-boot-starter-security`는 Spring Security를 기반으로 한 전자정부 표준프레임워크의 보안 모듈입니다.
JDBC 기반 사용자 인증, DB 기반 URL 권한 관리(실행 중 재적재), Role Hierarchy 등 보안 기능을 제공합니다.

## ✨ 주요 기능

- **JDBC 기반 사용자 인증**: 데이터베이스를 통한 사용자 및 권한 조회
- **DB 기반 URL 인가**: 보호자원(URL)–권한 매핑을 **요청 시점에** 조회하여 판단, `reload()` 로 재기동 없이 반영
- **Role Hierarchy 지원**: 계층적 권한 구조(미설정 시 기본 계층 `ROLE_ADMIN > ROLE_USER > ROLE_ANONYMOUS`)
- **다양한 패스워드 인코딩**: BCrypt(기본값), SHA 계열, 사용자 ID salt SHA-256(`egov-sha256`), MD5, LDAP 등
- **세션 관리**: 동시 접속 세션 수 제어
- **보안 헤더 설정**: X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, Cache-Control
- **CSRF 보호**: CSRF 토큰 기반 보호 기능(선택)
- **정규식 및 PathPattern 지원**: URL 매칭을 위한 유연한 패턴 지원

## 🔧 요구사항

- Java 17 이상
- Spring Boot 3.5.16 이상 (`egovframe-boot-starter-parent` 5.1.0 기준)
- Spring Web MVC(Servlet) 애플리케이션 — WebFlux 는 지원하지 않습니다
- Maven 3.6.3 이상
- Spring Security 6.5.11-egov.1 (`egovframe-rte-root` 5.1.0 이 지정하는 eGov 자체 빌드 — 해당 빌드를 받을 수 있는 Maven 저장소 필요)

## 📦 설치

### egovframe-boot-starter-parent 사용 시

`egovframe-boot-starter-parent` 5.1.0 을 parent 로 사용하면 이 스타터가 **자동으로 포함**됩니다(access · crypto · logging · security 4종). 별도 의존성 선언이 필요 없습니다. 아래 "활성화 조건" 의 주의사항을 반드시 확인하세요.

### 개별 의존성으로 추가 시

```xml
<dependency>
    <groupId>org.egovframe.boot</groupId>
    <artifactId>egovframe-boot-starter-security</artifactId>
    <version>5.1.0</version>
</dependency>
```

## ⚙️ 설정

### 1. 활성화 조건

```properties
Globals.Auth=security
```

| `Globals.Auth` | 적용되는 보안 설정 |
|----------------|--------------------|
| `security` | eGov 보안 설정(`securityFilterChain`) 만 적용 — Spring Boot 기본 보안은 적용되지 않음 |
| 그 외 / 미지정 | eGov 보안 설정 미적용. **이 스타터가 classpath 에 있으므로 Spring Boot 기본 보안(`defaultSecurityFilterChain`, 모든 URL 인증 요구)이 활성화됨** |

Spring Security 를 사용하지 않는 애플리케이션(예: `Globals.Auth=session` 으로 access 스타터만 사용)은 Spring Boot 보안 자동구성을 제외하세요.

```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```

자동구성은 `DataSourceAutoConfiguration` 이후, `SecurityAutoConfiguration` 이전에 적용됩니다.

### 2. 설정 예시

```yaml
Globals:
  Auth: security

org:
  egovframe:
    security:
      # 로그인/로그아웃 URL
      login-url: /login
      login-process-url: /loginProcess
      login-failure-url: /loginFailure
      logout-url: /logout
      logout-success-url: /
      access-denied-url: /accessDenied
      csrf-access-denied-url: /csrfAccessDenied

      # 데이터소스 (빈 이름)
      data-source: dataSource

      # 사용자 / 권한 조회 쿼리
      jdbc-users-by-username-query: "SELECT USER_ID,PASSWORD,ENABLED,USER_NAME,BIRTH_DAY,SSN FROM USERS WHERE USER_ID = ?"
      jdbc-authorities-by-username-query: "SELECT USER_ID,AUTHORITY FROM AUTHORITIES WHERE USER_ID = ?"

      # 패스워드 해시 (기본 bcrypt)
      hash: bcrypt
      hash-base64: true

      # URL 매칭 방식 : regex(기본) / ciRegex / url
      request-matcher-type: url

      # 세션 관리
      concurrent-max-sessons: 1
      concurrent-expired-url: /login
      error-if-maximum-exceeded: false

      # 로그인 성공 후 이동 URL
      default-target-url: /
      always-use-default-target-url: false

      # 보안 헤더
      sniff: true
      xframe-options: SAMEORIGIN  # DENY, SAMEORIGIN, DISABLE
      xss-protection: true
      cache-control: false

      # CSRF 보호
      csrf: false

      # 인증 없이 접근 가능한 경로
      permit-all-list: "/css/**, /js/**, /images/**, /resource/**, /public/**"

      # (선택) Role Hierarchy SQL — 결과 컬럼 child, parent
      sql-hierarchical-roles: "SELECT CHILD_ROLE AS child, PARENT_ROLE AS parent FROM ROLES_HIERARCHY"

      # (선택) URL-권한 매핑 SQL — 결과 컬럼 url, authority. 미지정 시 기본 쿼리 사용
      sql-roles-and-url: "SELECT a.RESOURCE_PATTERN AS url, b.AUTHORITY AS authority FROM SECURED_RESOURCES a, SECURED_RESOURCES_ROLE b WHERE a.RESOURCE_ID = b.RESOURCE_ID AND a.RESOURCE_TYPE = 'url' ORDER BY a.SORT_ORDER"
```

### 3. 주요 설정 속성

| 속성 | 기본값 | 설명 |
|------|--------|------|
| `hash` | `bcrypt` | 패스워드 인코더(아래 "패스워드 인코딩" 참고). 비어 있으면 `bcrypt` |
| `hash-base64` | `true` | MD5 / SHA 계열 해시를 Base64 로 표현할지 여부 |
| `request-matcher-type` | `regex` | `regex` : 정규식, `ciRegex` : 대소문자 무시 정규식, 그 외(예: `url`) : PathPattern(정규식 형태는 자동으로 정규식 처리) |
| `data-source` | `dataSource` | 사용자·권한·보호자원 조회에 사용할 DataSource 빈 이름 |
| `login-url` | `/login` | 로그인 페이지 |
| `login-process-url` | `/loginProcess` | 로그인 처리 URL(폼 파라미터 `username`, `password`) |
| `login-failure-url` | `/loginFailure` | 로그인 실패 시 이동 URL |
| `access-denied-url` | `/accessDenied` | 권한 없음 시 forward URL |
| `csrf-access-denied-url` | `/csrfAccessDenied` | CSRF 토큰 오류 시 forward URL |
| `permit-all-list` | `/css/**, /js/**, /images/**, /resource/**, /public/**` | DB 매핑이 없을 때 인증 없이 허용할 경로 |
| `sql-hierarchical-roles` | - | Role Hierarchy SQL. 미지정 시 기본 계층 사용 |
| `sql-roles-and-url` | - | URL-권한 매핑 SQL. 미지정 시 기본 쿼리(`SECURED_RESOURCES` / `SECURED_RESOURCES_ROLE`) |

⚠️ `request-matcher-type` 기본값은 `regex` 입니다. 보호자원에 `/admin/**` 같은 PathPattern 형식이 있으면 정규식 해석에 실패해 **기동이 실패**하므로, PathPattern 을 쓰려면 `url` 로 지정하세요.

### 4. 데이터베이스 스키마

#### 사용자 테이블 (USERS)

```sql
CREATE TABLE USERS (
    USER_ID   VARCHAR(50)  PRIMARY KEY,
    PASSWORD  VARCHAR(255) NOT NULL,   -- BCrypt 해시는 60자
    ENABLED   NUMBER(1)    DEFAULT 1,
    USER_NAME VARCHAR(100),
    BIRTH_DAY VARCHAR(8),
    SSN       VARCHAR(13)
);
```

#### 권한 테이블 (AUTHORITIES)

```sql
CREATE TABLE AUTHORITIES (
    USER_ID   VARCHAR(50) NOT NULL,
    AUTHORITY VARCHAR(50) NOT NULL,
    PRIMARY KEY (USER_ID, AUTHORITY),
    FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID)
);
```

#### 보호자원 테이블 (기본 쿼리 기준)

```sql
CREATE TABLE SECURED_RESOURCES (
    RESOURCE_ID      VARCHAR(50)  PRIMARY KEY,
    RESOURCE_PATTERN VARCHAR(500) NOT NULL,  -- URL 패턴 (PathPattern 또는 정규식)
    RESOURCE_TYPE    VARCHAR(20)  NOT NULL,  -- 'url'
    SORT_ORDER       NUMBER       DEFAULT 0  -- 먼저 일치하는 자원이 적용되므로 구체적인 패턴을 앞에
);

CREATE TABLE SECURED_RESOURCES_ROLE (
    RESOURCE_ID VARCHAR(50) NOT NULL,
    AUTHORITY   VARCHAR(50) NOT NULL,        -- ROLE_* 또는 IS_AUTHENTICATED_*
    PRIMARY KEY (RESOURCE_ID, AUTHORITY)
);
```

#### Role Hierarchy 테이블 (선택)

```sql
CREATE TABLE ROLES_HIERARCHY (
    CHILD_ROLE  VARCHAR(50) NOT NULL,  -- 상위(포함하는) 역할
    PARENT_ROLE VARCHAR(50) NOT NULL,  -- 하위(포함되는) 역할
    PRIMARY KEY (CHILD_ROLE, PARENT_ROLE)
);
```

계층 문자열은 각 행을 `child > parent` 로 만들어 사용합니다. 컬럼 이름과 달리 **`CHILD_ROLE` 에 상위 역할**을 넣어야 합니다.

```sql
-- ROLE_ADMIN > ROLE_USER > ROLE_ANONYMOUS
INSERT INTO ROLES_HIERARCHY (CHILD_ROLE, PARENT_ROLE) VALUES ('ROLE_ADMIN', 'ROLE_USER');
INSERT INTO ROLES_HIERARCHY (CHILD_ROLE, PARENT_ROLE) VALUES ('ROLE_USER', 'ROLE_ANONYMOUS');
```

## 🧭 URL 인가 판정 규칙

매 요청마다 보호자원 목록(`SORT_ORDER` 순)에서 **처음 일치하는 자원**의 권한으로 판단합니다.

| 상황 | 판정 |
|------|------|
| 일치하는 보호자원 있음 — `ROLE_*` | 사용자 권한(Role Hierarchy 포함)에 해당 역할이 하나라도 있으면 허용 (OR) |
| 일치하는 보호자원 있음 — `IS_AUTHENTICATED_ANONYMOUSLY` | 익명 포함 모두 허용 |
| 일치하는 보호자원 있음 — `IS_AUTHENTICATED_REMEMBERED` | remember-me 또는 완전 인증 사용자 허용 |
| 일치하는 보호자원 있음 — `IS_AUTHENTICATED_FULLY` | 완전 인증 사용자만 허용(remember-me 제외) |
| 일치하는 보호자원 있음 — 그 외 값(예: 접두사 없는 `ADMIN`) | 판정에 사용하지 않음. 허용 조건이 하나도 없으면 거부 |
| 일치하는 보호자원 없음 | `permit-all-list` 이면 허용, 그 외는 인증 사용자(익명 제외)면 허용 |

- 거부 시 : 익명 사용자는 `login-url` 로 리다이렉트, 인증 사용자는 `access-denied-url`(CSRF 오류는 `csrf-access-denied-url`)로 forward 합니다(forward 1회).
- `permit-all-list` 경로라도 일치하는 보호자원이 있으면 보호자원 규칙이 먼저 적용됩니다.
- 정규식으로 처리되는 패턴 : `\A`, `^` 로 시작하거나 `\Z` 를 포함하거나 `$` 로 끝나는 패턴(`request-matcher-type=url` 일 때).

### 보호자원 재적재

```java
@Autowired
private EgovReloadableFilterInvocationSecurityMetadataSource databaseSecurityMetadataSource;

public void refresh() {
    EgovUserDetailsHelper.reloadSecurityMetadata(databaseSecurityMetadataSource); // 또는 databaseSecurityMetadataSource.reload()
}
```

재적재는 새 목록을 만든 뒤 한 번에 교체하므로, 처리 중인 요청이 비어 있거나 일부만 채워진 목록을 보지 않습니다.

### method / pointcut 보호자원

`sql-roles-and-method`, `sql-roles-and-pointcut`, `support-method`, `support-pointcut` 설정과 `EgovSecuredObjectService#getRolesAndMethod()` / `getRolesAndPointcut()` 조회 API 는 제공되지만, **인가 처리에는 적용되지 않습니다**. 메서드 단위 권한은 Spring Security 의 `@EnableMethodSecurity` 와 `@PreAuthorize` 를 사용하세요.

## 🔐 패스워드 인코딩

`hash` 값(대소문자·앞뒤 공백 무시)에 따라 인코더가 결정됩니다.

| `hash` | 인코더 | 비고 |
|--------|--------|------|
| `bcrypt` / 미지정 / 빈 값 | `BCryptPasswordEncoder` | **기본값** |
| `sha`, `sha-256`, `sha256`, `sha-512` 등 `sha` 로 시작 | `MessageDigestPasswordEncoder` | `sha` 는 SHA-256, `hash-base64` 적용 |
| `egov-sha256` | `EgovIdSaltSha256PasswordEncoder` + `EgovDaoAuthenticationProvider` | 사용자 ID 를 salt 로 사용(`EgovFileScrty.encryptPassword(password, id)` 와 동일) |
| `md5` | `MessageDigestPasswordEncoder("MD5")` | `hash-base64` 적용 |
| `ldap` | `LdapShaPasswordEncoder` | |
| `plaintext` / `noop` | `NoOpPasswordEncoder` | 개발 환경 전용 |
| 그 외 값 | `BCryptPasswordEncoder` | |

기본 인코더로 검증되는 해시는 `EgovUserDetailsHelper.getHashedPassword(password)`(BCrypt)로 만들 수 있습니다.

## 🚀 사용 예제

### 1. 기본 사용

`Globals.Auth=security` 와 DB 스키마만 준비하면 로그인 폼(`login-process-url`), 로그아웃, URL 인가가 자동으로 구성됩니다.

### 2. 현재 사용자 정보 가져오기

```java
import org.egovframe.boot.security.userdetails.util.EgovUserDetailsHelper;

if (EgovUserDetailsHelper.isAuthenticated()) {
    Object userVO = EgovUserDetailsHelper.getAuthenticatedUser();     // 매핑 클래스가 만든 사용자 VO
    List<String> authorities = EgovUserDetailsHelper.getAuthorities(); // 권한 목록
}
```

DB 에서 다시 조회한 `EgovUserDetails` 가 필요하면 `EgovJdbcUserDetailsManager#getAuthenticatedUser()` 를 사용합니다.

```java
@Autowired
private EgovJdbcUserDetailsManager userDetailsManager;

EgovUserDetails details = userDetailsManager.getAuthenticatedUser();
```

### 3. 권한별 접근 가능 URL 목록

```java
List<Map.Entry<String, String>> rolePatterns = EgovUserDetailsHelper.getRoleAndPatternList(); // (권한, URL 패턴)
String patterns = EgovUserDetailsHelper.getAccessiblePatterns(rolePatterns, EgovUserDetailsHelper.getAuthorities());
```

`getRoleAndPatternList()` 는 기동 시점에 적재된 보호자원 정보를 기준으로 합니다.

### 4. 로그인 실패 처리

기본 로그인 흐름은 실패 시 `login-failure-url` 로 이동합니다. 실패 원인을 화면에 구분해 보여주지 않는 `EgovLoginFailHandler`(Bean 이름 `egovLoginFailHandler`)도 제공되며, 필요 시 애플리케이션에서 연결해 사용할 수 있습니다. 이 핸들러는 원인과 무관하게 `loginFailMsg` 요청 속성에 `"Invalid username or password."` 만 설정하고(계정 열거 방지), 실제 원인은 debug 로그로만 남깁니다.

## 📂 패키지 구성

| 패키지 | 내용 |
|--------|------|
| `org.egovframe.boot.security` | Spring Security 자동 구성(`EgovSecurityConfiguration`, `Globals.Auth=security`), 설정 프로퍼티(`org.egovframe.security.*`), DB 보호자원 기반 인가 관리자와 사용자 ID salt SHA-256 인증 클래스 |
| `org.egovframe.boot.security.bean` | 보안 설정에서 사용하는 FactoryBean(DataSource·쿼리·매핑 클래스·URL/메서드 자원 매핑), 접근 거부·로그인 실패 핸들러, 재적재 가능한 보안 메타데이터 소스 |
| `org.egovframe.boot.security.secureobject` | 보호자원(URL·메서드·포인트컷)과 권한 매핑 정보를 조회하는 서비스 인터페이스 |
| `org.egovframe.boot.security.secureobject.impl` | 보호자원 조회 서비스 구현체, DB 조회 DAO, 정규식 기반 요청 매처 |
| `org.egovframe.boot.security.userdetails` | 인증 사용자 정보 클래스와 사용자 조회 결과를 사용자 VO 로 매핑하는 기본 매핑 클래스 |
| `org.egovframe.boot.security.userdetails.hierarchicalroles` | DB 또는 기본값으로 권한 계층(Role Hierarchy) 문자열을 생성하는 FactoryBean |
| `org.egovframe.boot.security.userdetails.jdbc` | JDBC 기반 사용자 조회·관리 클래스와 사용자 조회 결과 매핑 추상 클래스 |
| `org.egovframe.boot.security.userdetails.util` | 인증 사용자·권한·보호자원 정보를 조회하는 헬퍼와 컬럼명 camelCase 변환 유틸리티 |

## 📚 주요 클래스

- `EgovSecurityConfiguration`: Spring Security 자동 설정 클래스
- `EgovSecurityProperties`: 보안 설정 프로퍼티(`org.egovframe.security.*`)
- `EgovDatabaseAuthorizationManager`: DB 보호자원 기반 URL 인가 관리자
- `EgovReloadableFilterInvocationSecurityMetadataSource`: 재적재 가능한 보호자원 메타데이터 소스
- `EgovJdbcUserDetailsManager`: JDBC 기반 사용자 관리자
- `EgovUserDetails`: 사용자 상세 정보 클래스
- `EgovIdSaltSha256PasswordEncoder` / `EgovDaoAuthenticationProvider`: 사용자 ID salt SHA-256 인증
- `EgovLoginFailHandler` / `EgovAccessDeniedHandler`: 로그인 실패 · 접근 거부 처리
- `EgovMultipleRoleAuthorizationManager`: 다중 권한(OR) 인가 관리자 — 기본 설정에서는 사용하지 않으며 사용자 정의 인가 규칙용으로 제공

## 🔄 5.1.0 변경 사항 (이전 빌드 대비)

| 항목 | 변경 내용 | 확인 필요 |
|------|-----------|-----------|
| 기본 해시 | `SHA256`(실제로는 BCrypt 로 선택되던 결함 포함) → **`bcrypt`** | SHA-256 / MD5 로 저장된 비밀번호는 `hash` 를 명시 |
| URL 인가 | 기동 시 1회 적재(`sql-roles-and-url` 지정 시에만) → **요청 시점 조회, 기본 쿼리 적용, `reload()` 즉시 반영** | 그동안 막히지 않던 URL 이 차단될 수 있음 |
| 보호자원 순서 | 패턴 구체성 순 → **DB `SORT_ORDER` 순, 처음 일치한 자원만 적용** | `SORT_ORDER` 정비 |
| Role Hierarchy | 미설정 시 `null`(로그인 NPE) → **기본 계층 `ROLE_ADMIN > ROLE_USER > ROLE_ANONYMOUS`** | |
| 로그인 실패 핸들러 | `access-denied-url` 로 이동 · 원인별 메시지 → **`login-failure-url` · 일반 메시지** | 원인별 메시지에 의존하던 화면 |
| 접근 거부 핸들러 | CSRF 예외 시 forward 2회 → **1회** | |

## 📝 라이선스

이 프로젝트는 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 라이선스 하에 배포됩니다.

```
Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 🔗 관련 링크

- [전자정부 표준프레임워크 공식 사이트](https://www.egovframe.go.kr)
- [Spring Security 공식 문서](https://spring.io/projects/spring-security)

## 🤝 기여

이슈 및 풀 리퀘스트를 환영합니다. 기여하기 전에 기여 가이드라인을 확인해주세요.

## 📞 문의

전자정부 표준프레임워크 관련 문의사항은 [공식 사이트](https://www.egovframe.go.kr)를 참고하세요.

---

**Version**: 5.1.0
**Last Updated**: 2026.10.23
