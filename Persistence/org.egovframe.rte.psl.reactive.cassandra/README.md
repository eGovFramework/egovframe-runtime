# egovframe-rte-psl-reactive-cassandra

**Spring Data Cassandra**를 사용하는 **리액티브 Cassandra** 연동 모듈입니다. 연결·세션 구성과 Egov 표준 리포지토리 인터페이스를 제공합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-psl-reactive-cassandra:5.0.0`
- **Runtime**: Java 17 / Spring Data Cassandra / Cassandra Java Driver / Reactor
- **의존**: `egovframe-rte-fdl-logging`

---

## 특징

- 컬럼 패밀리 기반 대용량 쓰기/조회에 적합한 **비동기 API**
- `EgovCassandraConfiguration`으로 드라이버·클러스터 설정 표준화

---

## 소스 디렉터리 구조

```
org/egovframe/rte/psl/reactive/cassandra/
├── connect/
│   └── EgovCassandraConfiguration.java
└── repository/
    └── EgovCassandraRepository.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovCassandraConfiguration` | CQL 세션·템플릿 Bean 구성 |
| `EgovCassandraRepository` | Cassandra 리포지토리 공통 인터페이스 |

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
  <artifactId>egovframe-rte-psl-reactive-cassandra</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 상위 개요: 저장소 루트 `README.md`
