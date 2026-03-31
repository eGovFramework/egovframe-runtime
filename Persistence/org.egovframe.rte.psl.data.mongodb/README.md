# egovframe-rte-psl-data-mongodb

**Spring Data MongoDB**(동기)와 공식 **MongoDB Java Driver**를 묶어, Egov 형식의 **연결 팩토리**와 **리포지토리** 베이스를 제공합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-psl-data-mongodb:5.0.0`
- **Runtime**: Java 17 / Spring Data MongoDB 4.x / Driver 5.x
- **의존**: `egovframe-rte-fdl-cmmn`

---

## 특징

- **문서 저장소**: 도큐먼트 CRUD, 트랜잭션 어노테이션과 Spring `MongoTemplate` 활용
- **표준 진입점**: `EgovMongodbConnectionFactory`, `EgovMongoDbRepository`

---

## 소스 디렉터리 구조

```
org/egovframe/rte/psl/data/mongodb/
├── connect/
│   └── EgovMongodbConnectionFactory.java
└── repository/
    └── EgovMongoDbRepository.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovMongodbConnectionFactory` | `MongoClient` / 설정 프로퍼티 기반 연결 생성 |
| `EgovMongoDbRepository` | 비즈니스 리포지토리가 상속할 공통 인터페이스 |

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
  <artifactId>egovframe-rte-psl-data-mongodb</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 리액티브 MongoDB: `egovframe-rte-psl-reactive-mongodb`
- 상위 개요: 저장소 루트 `README.md`
