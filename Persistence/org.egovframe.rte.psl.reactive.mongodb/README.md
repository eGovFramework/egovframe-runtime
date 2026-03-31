# egovframe-rte-psl-reactive-mongodb

**Spring Data MongoDB**와 **Reactor**를 결합한 **리액티브 MongoDB** 접근 모듈입니다. `Mono`/`Flux` 기반으로 문서 저장소를 사용합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-psl-reactive-mongodb:5.0.0`
- **Runtime**: Java 17 / Spring WebFlux / Spring Data MongoDB / Reactor
- **의존**: `egovframe-rte-fdl-cmmn`

---

## 특징

- 동기 모듈(`psl.data.mongodb`)과 대응하는 **논블로킹** 스택
- Reactive Streams MongoDB 드라이버 활용

---

## 소스 디렉터리 구조

```
org/egovframe/rte/psl/reactive/mongodb/
├── connect/
│   └── EgovReactiveMongoDbConnectionFactory.java
└── repository/
    └── EgovReactiveMongoDbRepository.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovReactiveMongoDbConnectionFactory` | 리액티브 Mongo 클라이언트 설정 |
| `EgovReactiveMongoDbRepository` | 리액티브 리포지토리 베이스 |

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
  <artifactId>egovframe-rte-psl-reactive-mongodb</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 동기 MongoDB: `egovframe-rte-psl-data-mongodb`
- 상위 개요: 저장소 루트 `README.md`
