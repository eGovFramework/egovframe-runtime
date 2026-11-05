# org.egovframe.rte.psl.reactive.cassandra

**영속성(Persistence) 계층 리액티브 Cassandra 연동 모듈**입니다. **Spring Data Cassandra 3.4.18**과 **DataStax Java Driver 4.17.0**을 기반으로
`CqlSession`을 감싼 `ReactiveSession`을 만드는 `EgovCassandraConfiguration`과, `ReactiveCassandraTemplate`을 확장하여 `Mono`/`Flux` 기반 CRUD 메서드를 제공하는
`EgovCassandraRepository`를 제공합니다. 이 모듈에 의존하는 다른 실행환경 모듈은 없습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.psl.reactive.cassandra:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Spring Data Cassandra 3.4.18 / Cassandra Java Driver 4.17.0 / Reactor Core 3.4.41
- **실행환경 의존**: `org.egovframe.rte.fdl.logging`

---

## 특징

- **논블로킹 Cassandra 접근**: Spring Data Cassandra의 `ReactiveCassandraTemplate`을 확장하여 `Mono`/`Flux`로 결과 반환
- **접속 설정 단순화**: `EgovCassandraConfiguration`에 데이터센터·keyspace·contact point·포트·계정을 설정하면 `DefaultBridgedReactiveSession`을 생성
- **공통 CRUD 메서드**: `selectAllData`, `selectOneData`, `countData`, `insertData`, `updateData`, `deleteData` 제공 (`Query`/`Criteria` 기반)
- **Spring WebFlux 연계**: `spring-webflux`에 의존하며 테스트 설정은 `@EnableWebFlux` + `@EnableCassandraRepositories`로 구성
- **Java Config 기반**: XML 설정 없이 `@Configuration` 클래스에서 `ReactiveSession` 빈을 등록하는 방식

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/psl/reactive/cassandra/
├── connect/
│   └── EgovCassandraConfiguration.java   # CqlSession -> ReactiveSession 생성
└── repository/
    └── EgovCassandraRepository.java      # ReactiveCassandraTemplate 확장 (제네릭 CRUD)

src/test/java/org/egovframe/rte/psl/reactive/cassandra/repository/
├── CassandraConfiguration.java           # @Configuration, reactiveSession 빈 (localhost:9042, keyspace com)
├── CassandraTest.java                    # StepVerifier 기반 insert 검증
├── Sample.java                           # @Table("sample") 엔티티 (Lombok)
└── SampleRepository.java                 # EgovCassandraRepository<Sample> 구현
```

`src/main/resources`, `src/test/resources`는 없습니다 (접속 정보는 테스트 설정 클래스에 하드코딩).

---

## 주요 클래스

| 클래스 | 설명 |
|--------|------|
| `EgovCassandraConfiguration` | `dataCenterName`, `keyspaceName`, `contactPoint`, `port`, `username`, `password` 프로퍼티를 가진 POJO. `reactiveSession()`이 `CqlSession.builder()`로 세션을 만들고 `DefaultBridgedReactiveSession`으로 감싸 `ReactiveSession`을 반환 |
| `EgovCassandraRepository<T>` | `ReactiveCassandraTemplate` 확장. 생성자에 `ReactiveSession`을 받으며 `selectAllData(Query, Class<T>)`: `Flux<T>`, `selectOneData`: `Mono<T>`, `countData`: `Mono<Long>`, `insertData`/`updateData`/`deleteData(T)`: `Mono<T>` 제공 |

---

## 주요 의존성

모듈 POM은 버전을 선언하지 않으며 루트 POM `dependencyManagement`의 버전이 적용됩니다.

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.logging` | 4.3.1 | compile |
| `org.springframework:spring-webflux`, `spring-context`, `spring-tx` | 5.3.39-egov.1 | compile |
| `org.springframework.data:spring-data-cassandra` | 3.4.18 | compile (Spring Data Commons 2.7.18-egov.1 전이) |
| `com.datastax.oss:java-driver-core` | 4.17.0 | compile |
| `io.projectreactor:reactor-core` | 3.4.41 | compile (Reactive Streams 1.0.4 전이) |
| `io.projectreactor:reactor-test`, `org.assertj:assertj-core` | 3.4.41 / 3.27.7 | test |
| `org.projectlombok:lombok` | 1.18.36 | test (`Sample` 엔티티) |
| `org.springframework:spring-test`, `junit`, `easymock`, `com.googlecode.log4jdbc:log4jdbc` | 5.3.39-egov.1 / 4.13.2 / 5.2.0 / 1.2 | test |

---

## 설정 및 사용 예시

테스트 설정 클래스(`src/test/java/.../CassandraConfiguration.java`):

```java
@Configuration
@EnableWebFlux
@ComponentScan(basePackages = "org.egovframe.rte.psl.reactive.cassandra.repository")
@EnableCassandraRepositories
public class CassandraConfiguration {

    @Bean(name = "reactiveSession")
    public ReactiveSession reactiveSession() {
        EgovCassandraConfiguration config = new EgovCassandraConfiguration();
        config.setDataCenterName("datacenter1");
        config.setKeyspaceName("com");
        config.setContactPoint("localhost");
        config.setPort(9042);
        config.setUsername("com");
        config.setPassword("com01");
        return config.reactiveSession();
    }
}
```

엔티티와 Repository(`Sample.java`, `SampleRepository.java`):

```java
@Data @AllArgsConstructor @NoArgsConstructor
@Table("sample")
public class Sample {
    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String id;
    @PrimaryKeyColumn(name = "sample_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String sampleId;
    @Column(value = "name")
    private String name;
    // description, use_yn, reg_user ...
}

@Repository
public class SampleRepository extends EgovCassandraRepository<Sample> {
    public SampleRepository(ReactiveSession reactiveSession) {
        super(reactiveSession);
    }

    public Flux<Sample> selectAllSample() {
        return selectAllData(Query.empty(), Sample.class);
    }

    public Mono<Sample> selectOneSample(String sampleId) {
        return selectOneData(Query.query(
                Criteria.where("id").is("part01"),
                Criteria.where("sample_id").is(sampleId)), Sample.class);
    }

    public Mono<Sample> insertSample(Sample sample) {
        return insertData(sample);
    }
}
```

테스트(`CassandraTest`)는 `repository.insertSample(sample).as(StepVerifier::create).expectNextMatches(...).verifyComplete()`로 결과를 검증합니다.

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Persistence/org.egovframe.rte.psl.reactive.cassandra -am
```

## 테스트

```bash
mvn test -pl Persistence/org.egovframe.rte.psl.reactive.cassandra
```

테스트는 로컬 Cassandra 서버가 필요합니다. 서버가 없으면 접속 오류로 실패합니다.

| 항목 | 값 (`CassandraConfiguration.java`) |
|---|---|
| contact point / 포트 | `localhost` / `9042` |
| 데이터센터 | `datacenter1` |
| keyspace | `com` (테이블 `sample`: `id`, `sample_id`, `name`, `description`, `use_yn`, `reg_user`) |
| 계정 / 비밀번호 | `com` / `com01` |

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.psl.reactive.cassandra</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>` 생략 가능.
`-egov.1` 포크 아티팩트(Spring Framework, Spring Data Commons)를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록 필요.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.logging](../../Foundation/org.egovframe.rte.fdl.logging/README.md), [org.egovframe.rte.psl.reactive.mongodb](../org.egovframe.rte.psl.reactive.mongodb/README.md), [org.egovframe.rte.psl.reactive.r2dbc](../org.egovframe.rte.psl.reactive.r2dbc/README.md), [org.egovframe.rte.psl.reactive.redis](../org.egovframe.rte.psl.reactive.redis/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
