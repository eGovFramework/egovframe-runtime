# egovframe-boot-starter-access

전자정부 표준프레임워크의 Spring Boot 기반 접근 제어(Access Control) Starter 모듈입니다.

## 📋 개요

이 프로젝트는 Spring Boot 애플리케이션에서 세션 기반 인증 및 URL 패턴 기반 권한 관리를 제공하는 자동 설정 모듈입니다. 데이터베이스에서 사용자 권한 정보를 조회하여 웹 요청에 대한 접근을 제어합니다.

## ✨ 주요 기능

- **세션 기반 인증 체크**: 세션에 저장된 사용자 정보를 기반으로 인증 여부 확인
- **URL 패턴 기반 접근 제어**: Ant 패턴 또는 정규식(경로 전체 일치) 패턴을 사용한 URL 매칭
- **요청 경로 정규화**: `..`, `.`, 퍼센트 인코딩, 중복 슬래시를 정규화한 경로로 판정해 경로 조작에 의한 우회를 차단
- **권한별 리소스 관리**: 데이터베이스에서 사용자 권한과 URL 매핑 정보 조회, 실행 중 재적재(`reload()`) 지원
- **인터셉터 기반 자동 적용**: Spring MVC 인터셉터를 통한 자동 접근 제어
- **설정 기반 활성화**: `application.yml` 또는 `application.properties` 설정으로 간편하게 활성화

## 🔧 요구사항

- Java 17 이상
- Spring Boot 3.5.16 이상 (`egovframe-boot-starter-parent` 5.1.0 기준)
- Spring Web MVC(Servlet) 애플리케이션 — WebFlux 는 지원하지 않습니다
- Maven 3.6.3 이상

## 📦 설치

### egovframe-boot-starter-parent 사용 시

`egovframe-boot-starter-parent` 5.1.0 을 parent 로 사용하면 이 스타터가 **자동으로 포함**됩니다(access · crypto · logging · security 4종). 별도 의존성 선언이 필요 없습니다.

### 개별 의존성으로 추가 시

```xml
<dependency>
    <groupId>org.egovframe.boot</groupId>
    <artifactId>egovframe-boot-starter-access</artifactId>
    <version>5.1.0</version>
</dependency>
```

## ⚙️ 설정

### 1. 활성화 조건

다음 두 설정이 모두 `session` 일 때 인터셉터가 등록됩니다.

```properties
Globals.Auth=session
org.egovframe.access.global-authen=session
```

- `Globals.Auth=session` 이 아니면 자동구성 자체가 적용되지 않습니다.
- `Globals.Auth=session` 이지만 `global-authen` 이 `session` 이 아니면 빈은 등록되지만 인터셉터는 등록되지 않고 오류 로그(`EgovAccessConfiguration Invalid settings`)만 남습니다.
- 자동구성은 `DataSourceAutoConfiguration` 이후에 적용되므로 Spring Boot 가 만드는 기본 `dataSource` 를 그대로 사용할 수 있습니다.

### 2. 설정 예시

```yaml
Globals:
  Auth: session

org:
  egovframe:
    access:
      global-authen: session
      mapping-path: /**
      data-source: dataSource
      login-url: /sample/login
      access-denied-url: /sample/accessDenied
      sql-authority-user: SELECT USER_ID AS userid, AUTHOR_CODE AS authority FROM AUTHORITY_USER
      sql-role-and-url: SELECT AUTHOR_CODE AS authority, URL AS url FROM ROLE_AND_URL
      request-matcher-type: regex  # ant 또는 regex
      exclude-list: /css/**, /js/**, /img/**, /error/**, /, /sample/loginProcess, /sample/logout
```

또는 `application.properties`:

```properties
Globals.Auth=session
org.egovframe.access.global-authen=session
org.egovframe.access.mapping-path=/**
org.egovframe.access.data-source=dataSource
org.egovframe.access.login-url=/sample/login
org.egovframe.access.access-denied-url=/sample/accessDenied
org.egovframe.access.sql-authority-user=SELECT USER_ID AS userid, AUTHOR_CODE AS authority FROM AUTHORITY_USER
org.egovframe.access.sql-role-and-url=SELECT AUTHOR_CODE AS authority, URL AS url FROM ROLE_AND_URL
org.egovframe.access.request-matcher-type=regex
org.egovframe.access.exclude-list=/css/**, /js/**, /img/**, /error/**, /, /sample/loginProcess, /sample/logout
```

### 3. 설정 속성 설명

| 속성 | 설명 | 필수 | 기본값 |
|------|------|------|--------|
| `global-authen` | 인증 방식. `session` 만 지원 | ✅ | - |
| `login-url` | 인증되지 않은 사용자를 리다이렉트할 URL | ✅ | - |
| `access-denied-url` | 접근 권한이 없는 사용자를 리다이렉트할 URL | ✅ | - |
| `sql-authority-user` | 사용자별 권한을 조회하는 SQL. 결과 컬럼 `userid`, `authority` | ✅ | - |
| `sql-role-and-url` | 권한별 허용 URL 을 조회하는 SQL. 결과 컬럼 `authority`, `url` | ✅ | - |
| `mapping-path` | 인터셉터를 적용할 URL 패턴 | ❌ | `/**` (전체 경로) |
| `exclude-list` | 인터셉터에서 제외할 URL 패턴(쉼표 구분, 빈 항목은 무시) | ❌ | 제외 없음(`login-url`, `access-denied-url` 은 항상 제외) |
| `data-source` | 권한 정보 조회에 사용할 DataSource 빈 이름(비어 있거나 공백이면 기본값) | ❌ | `dataSource` |
| `request-matcher-type` | URL 매칭 방식. `ant` 이면 Ant 패턴, 그 외(미지정 포함)는 정규식 | ❌ | 정규식 |

- 조회 결과의 컬럼명은 대소문자를 구분하지 않습니다. 테이블 컬럼명이 다르면 위 예시처럼 별칭(`AS userid`)을 지정하세요.
- `login-url`, `access-denied-url` 을 지정하지 않으면 리다이렉트 시 오류가 발생합니다.
- `login-url`, `access-denied-url` 은 리다이렉트가 반복되지 않도록 `exclude-list` 와 관계없이 항상 인터셉터에서 제외됩니다.
- 기본 `mapping-path` 가 전체 경로(`/**`)이므로 정적 리소스(`/css/**` 등), 오류 페이지(`/error/**`), 로그인 처리 URL 처럼 인증 없이 접근해야 하는 경로는 `exclude-list` 에 지정하세요.

## 🗄️ 데이터베이스 스키마

테이블 구조는 자유롭게 정할 수 있으며, 위 두 SQL 의 **결과 컬럼**만 맞추면 됩니다. 예시는 다음과 같습니다.

### 권한 사용자 테이블 (AUTHORITY_USER)

```sql
CREATE TABLE AUTHORITY_USER (
    USER_ID     VARCHAR(50) NOT NULL,
    AUTHOR_CODE VARCHAR(50) NOT NULL,
    PRIMARY KEY (USER_ID, AUTHOR_CODE)
);
```

### 역할 및 URL 매핑 테이블 (ROLE_AND_URL)

```sql
CREATE TABLE ROLE_AND_URL (
    AUTHOR_CODE VARCHAR(50)  NOT NULL,
    URL         VARCHAR(255) NOT NULL,
    PRIMARY KEY (AUTHOR_CODE, URL)
);
```

## 🧭 URL 판정 규칙

1. 요청 경로는 `UrlPathHelper.getPathWithinApplication()` 으로 디코딩하고 contextPath 를 **접두사 위치에서만** 제거한 뒤, `StringUtils.cleanPath()` 로 `.` / `..` 세그먼트를 정규화합니다.
   - 예: `/sample/../admin/delete` → `/admin/delete` 로 판정
2. 로그인 사용자(`accessUser`)의 권한에 매핑된 URL 패턴 중 하나라도 일치하면 허용합니다.
3. `request-matcher-type=ant` 이면 Ant 패턴(`/user/**`), 그 외에는 정규식으로 비교하며 **정규식은 경로 전체가 일치해야** 합니다.
   - 예: 패턴 `/sample/.*` 는 `/sample/list` 에는 일치, `/admin/sample/delete` 에는 불일치
   - 부분 일치를 의도했던 패턴(예: `/admin`)은 `/admin.*` 처럼 전체 경로를 표현하도록 작성해야 합니다.
4. 일치하는 패턴이 없으면 `access-denied-url` 로 리다이렉트합니다.

## 💻 사용 방법

### 1. 세션에 사용자 정보 저장

로그인 성공 후 세션에 다음 정보를 저장해야 합니다.

```java
@PostMapping("/sample/loginProcess")
public String login(HttpSession session, @RequestParam String userId) {
    // 로그인 처리...

    session.setAttribute("loginVO", userVO);    // 인증 여부 판단에 사용
    session.setAttribute("accessUser", userId); // sql-authority-user 결과의 userid 와 비교

    return "redirect:/main";
}
```

### 2. 현재 사용자 정보 조회

```java
import org.egovframe.boot.access.service.EgovUserDetailsHelper;

if (EgovUserDetailsHelper.isAuthenticated()) {
    Object user = EgovUserDetailsHelper.getAuthenticatedUser();          // 세션의 loginVO
    List<String> authorities = EgovUserDetailsHelper.getAuthorities();   // 사용자 권한 목록
    List<Map<String, Object>> roles = EgovUserDetailsHelper.getRoles();  // 권한별 URL 매핑 목록
}
```

인증되지 않은 상태에서는 `getAuthorities()`, `getRoles()` 가 `null` 을 반환합니다.

### 3. 권한 정보 재적재

DB 의 권한·URL 매핑을 변경한 뒤 재기동 없이 반영하려면 `AuthorityResourceMetadata#reload()` 를 호출합니다.

```java
@Autowired
private AuthorityResourceMetadata authorityResourceMetadata;

public void refresh() throws Exception {
    authorityResourceMetadata.reload();
}
```

재적재는 새 목록을 만든 뒤 참조를 한 번에 교체하므로, 처리 중인 요청은 항상 완전한 이전/이후 목록 중 하나만 봅니다. `getAuthorityList()` / `getResourceMap()` 이 반환한 목록을 보관해 두고 갱신을 기대하는 코드는 `reload()` 후 다시 조회해야 합니다.

## 📂 패키지 구성

| 패키지 | 내용 |
|--------|------|
| `org.egovframe.boot.access` | 세션 기반 접근 제어 자동 구성(`EgovAccessConfiguration`, `Globals.Auth=session`)과 설정 프로퍼티(`org.egovframe.access.*`) |
| `org.egovframe.boot.access.bean` | 접근 제어에 사용하는 FactoryBean(DataSource·권한/URL 조회 쿼리·권한 및 자원 목록)과 권한·자원 메타데이터(재적재 지원) |
| `org.egovframe.boot.access.interceptor` | 요청 경로를 정규화한 뒤 사용자 권한의 URL 패턴(Ant / 정규식 전체 일치)으로 접근을 판정하는 인터셉터와 매칭 유틸리티 |
| `org.egovframe.boot.access.service` | 권한 정보 조회 서비스 인터페이스와 세션의 사용자·권한 정보를 조회하는 헬퍼 |
| `org.egovframe.boot.access.service.impl` | 권한 정보 조회 서비스 구현체와 JDBC 기반 DAO |

## 🏗️ 아키텍처

### 주요 컴포넌트

- **EgovAccessConfiguration**: 자동 설정 클래스(`WebMvcConfigurer`)
- **EgovAccessProperties**: 설정 프로퍼티(`org.egovframe.access.*`)
- **EgovAccessInterceptor**: 요청 인터셉터 (인증 및 권한 체크)
- **EgovAccessUtil**: Ant / 정규식 매칭 유틸리티(정규식 컴파일 결과 캐시)
- **EgovAccessService / EgovAccessDao**: 권한 정보 조회 서비스와 DAO
- **AuthorityResourceMetadata**: 권한 및 리소스 메타데이터 보관·재적재
- **EgovUserDetailsHelper**: 사용자 정보 조회 헬퍼 클래스

### 동작 흐름

1. 클라이언트 요청 → `EgovAccessInterceptor.preHandle()` 호출 (`mapping-path` 일치, `exclude-list` 불일치 요청)
2. 세션에서 인증 정보 확인 (`EgovUserDetailsHelper.isAuthenticated()`)
3. 인증되지 않은 경우 → `login-url` 로 리다이렉트
4. 인증된 경우 → 정규화한 요청 경로와 사용자 권한의 URL 패턴 매칭
5. 일치하면 요청 처리 계속, 일치하지 않으면 `access-denied-url` 로 리다이렉트

## ⚠️ egovframe-boot-starter-parent 사용 시 주의

parent 를 사용하면 `egovframe-boot-starter-security` 도 함께 포함되어 Spring Security 가 classpath 에 있게 됩니다. 이때 `Globals.Auth=session` 이면 eGov 보안 설정은 적용되지 않지만 **Spring Boot 기본 보안(`defaultSecurityFilterChain`, 모든 URL 인증 요구)이 활성화**됩니다. 세션 방식만 사용한다면 Spring Boot 보안 자동구성을 제외하세요.

```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```

## 🔒 보안 고려사항

- 세션 기반 인증을 사용하므로 세션 보안 설정(타임아웃, 쿠키 `HttpOnly` / `Secure`)을 적절히 구성하세요.
- `sql-authority-user`, `sql-role-and-url` 은 설정 파일의 고정 SQL 로 실행되며 요청 값이 결합되지 않습니다. 사용자 입력으로 SQL 을 구성하지 마세요.
- HTTPS 를 사용하여 세션 쿠키를 보호하세요.

## 📄 라이선스

이 프로젝트는 Apache License 2.0 하에 배포됩니다.

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
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)

## 🤝 기여

이슈 및 Pull Request를 환영합니다. 기여하기 전에 프로젝트의 코딩 스타일과 가이드라인을 확인해주세요.

## 📞 문의

전자정부 표준프레임워크 관련 문의사항은 [공식 사이트](https://www.egovframe.go.kr)를 참고하세요.

---

**Version**: 5.1.0
**Last Updated**: 2026.10.23
