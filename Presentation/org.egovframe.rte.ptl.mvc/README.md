# egovframe-rte-ptl-mvc

**Spring MVC** 기반 **표현(Presentation)** 계층 확장입니다. 입력 검증, HTML 태그 필터, **페이지네이션 JSP 태그**(KRDS/기본 렌더러)를 제공합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-ptl-mvc:5.0.0`
- **Runtime**: Java 17 / Spring WebMVC / Jakarta Servlet·JSP
- **의존**: `egovframe-rte-fdl-cmmn`

---

## 특징

- **공공 UI 패턴**: `PaginationTag`, `PaginationInfo`, 렌더러 플러그인
- **보안·품질**: `HTMLTagFilter`로 요청 파라미터 XSS 완화 패턴
- **검증**: `RteGenericValidator`, 어노테이션 예외 핸들러 베이스

---

## 소스 디렉터리 구조

```
org/egovframe/rte/ptl/mvc/
├── validation/
│   └── RteGenericValidator.java
├── bind/exception/
│   └── AbstractAnnotationExceptionHandler.java
├── filter/
│   ├── HTMLTagFilter.java
│   └── HTMLTagFilterRequestWrapper.java
└── tags/ui/
    ├── PaginationTag.java
    └── pagination/
        ├── PaginationInfo.java
        ├── PaginationManager.java
        ├── PaginationRenderer.java
        ├── DefaultPaginationManager.java
        ├── DefaultPaginationRenderer.java
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
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 리액티브 표현 계층: `egovframe-rte-ptl-reactive`
- 상위 개요: 저장소 루트 `README.md`
