# egovframe-rte-ptl-mvc

**Spring MVC** 기반 **표현(Presentation)** 계층 확장입니다. 입력 검증, HTML 태그 필터, **페이지네이션 JSP 태그**(KRDS/기본 렌더러)를 제공합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-ptl-mvc:5.1.0`
- **Runtime**: Java 17 / Spring WebMVC / Jakarta Servlet·JSP
- **의존**: `egovframe-rte-fdl-cmmn`, `egovframe-rte-fdl-filehandling`

---

## 특징

- **공공 UI 패턴**: `PaginationTag`, `PaginationInfo`, 렌더러 플러그인
- **보안·품질**: `HTMLTagFilter`로 요청 파라미터 XSS 완화 패턴
- **출력 정제**: `EgovHtmlSanitizer`(jsoup 파서 기반 화이트리스트, jsoup 은 optional — 사용 시 앱에서 직접 선언)
- **검증**: `RteGenericValidator`, 어노테이션 예외 핸들러 베이스, REST 예외 핸들러 `EgovRestExceptionHandler`(RFC 9457 ProblemDetail)
- **웹 편의**: 요청 컨텍스트(`EgovRequestContext`), 안전 쿠키(`EgovCookies`), 세션 만료 쿠키 필터, 중복 제출 방지 토큰, 업로드 검증(`EgovMultipartFiles`, `EgovValidatingMultipartResolver`)

---

## 소스 디렉터리 구조

```
org/egovframe/rte/ptl/mvc/
├── validation/
│   └── RteGenericValidator.java
├── bind/exception/
│   ├── AbstractAnnotationExceptionHandler.java
│   └── EgovRestExceptionHandler.java
├── context/                # EgovRequestContext
├── cookie/                 # EgovCookies
├── filter/
│   ├── HTMLTagFilter.java
│   ├── HTMLTagFilterRequestWrapper.java
│   ├── EgovHtmlSanitizer.java
│   └── EgovHtmlSanitizePolicy.java
├── session/                # EgovSessionExpiry, EgovSessionExpiryCookieFilter
├── token/                  # EgovSubmitTokens, EgovSubmitTokenStore, EgovSubmitTokenException
├── upload/                 # EgovMultipartFiles, EgovValidatingMultipartResolver
└── tags/ui/
    ├── PaginationTag.java
    ├── SubmitTokenTag.java
    └── pagination/
        ├── PaginationInfo.java
        ├── PaginationManager.java
        ├── PaginationRenderer.java
        ├── DefaultPaginationManager.java
        ├── DefaultPaginationRenderer.java
        ├── MessageSourcePaginationRenderer.java
        ├── AbstractPaginationRenderer.java
        └── AbstractKrdsPaginationRenderer.java
```

---

## 주요 기능

| 영역 | 설명 |
|------|------|
| **페이지네이션** | `PaginationTag` + `PaginationInfo` + `PaginationRenderer` 구현 교체 |
| **KRDS** | `AbstractKrdsPaginationRenderer` — 디자인 시스템 연동 훅 |
| **필터** | `HTMLTagFilter` — 지정 태그 제거/이스케이프 |
| **검증** | `RteGenericValidator` — 폼/커맨드 객체 검증 보조 |
| **REST 예외** | `EgovRestExceptionHandler` — RFC 9457(ProblemDetail) 기본 예외 응답 |
| **출력 정제** | `EgovHtmlSanitizer` — 신뢰할 수 없는 HTML 의 XSS 요소 제거 |
| **중복 제출 방지** | `EgovSubmitTokens` + `SubmitTokenTag` — 세션 기반 제출 토큰 |
| **업로드 검증** | `EgovMultipartFiles`, `EgovValidatingMultipartResolver` — `EgovUploadPolicy` 적용 |
| **세션·쿠키** | `EgovSessionExpiryCookieFilter`, `EgovCookies` — 세션 만료 쿠키, 안전 기본값 쿠키 |

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
  <artifactId>egovframe-rte-ptl-mvc</artifactId>
  <version>5.1.0</version>
</dependency>
```

---

## 참고

- 리액티브 표현 계층: `egovframe-rte-ptl-reactive`
- 상위 개요: 저장소 루트 `README.md`
