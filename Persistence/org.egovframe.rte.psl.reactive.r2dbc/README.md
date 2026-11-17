# egovframe-rte-psl-reactive-r2dbc

**Spring Data R2DBC** 기반 **논블로킹 관계형 DB** 접근을 위한 Egov 확장입니다. 연결 팩토리와 리액티브 리포지토리 베이스를 제공합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-psl-reactive-r2dbc:5.1.0`
- **Runtime**: Java 17 / Spring Framework 6.x / Spring Data R2DBC / Project Reactor
- **의존**: `egovframe-rte-fdl-cmmn`

---

## 특징

- **논블로킹 JDBC 대체**: 블로킹 `DataSource` 대신 R2DBC 드라이버 사용
- **감사 필드**: `EgovReactiveBaseEntity`(등록자·등록일시·수정자·수정일시, `@EnableR2dbcAuditing` 필요), Reactor Context 기반 `EgovContextReactiveAuditorAware`
- **WebFlux 친화**: 컨트롤러에서 `Mono`/`Flux`로 끝까지 파이프 구성 가능

---

## 소스 디렉터리 구조

```
org/egovframe/rte/psl/reactive/r2dbc/
├── audit/
│   └── EgovContextReactiveAuditorAware.java
├── connect/
│   └── EgovR2dbcConnectionFactory.java
├── entity/
│   └── EgovReactiveBaseEntity.java
└── repository/
    └── EgovR2dbcRepository.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovR2dbcConnectionFactory` | `ConnectionFactory` 빌드 및 설정 |
| `EgovR2dbcRepository` | R2DBC 리포지토리 공통 인터페이스 |
| `EgovReactiveBaseEntity` | R2DBC 감사 필드 공통 상위 엔티티 |
| `EgovContextReactiveAuditorAware` | Reactor Context 의 사용자 ID 를 감사자로 반환하는 `ReactiveAuditorAware` |

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
  <artifactId>egovframe-rte-psl-reactive-r2dbc</artifactId>
  <version>5.1.0</version>
</dependency>
```

---

## 참고

- 표현 계층 WebFlux: `egovframe-rte-ptl-reactive`
- 상위 개요: 저장소 루트 `README.md`
