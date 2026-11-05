# org.egovframe.rte.psl.reactive.r2dbc

**영속성(Persistence) 계층 리액티브 관계형 DB(R2DBC) 연동 모듈**입니다. **Spring Data R2DBC 1.5.18**을 기반으로,
R2DBC URL로 `ConnectionFactory`를 만드는 `EgovR2dbcConnectionFactory`와 `R2dbcEntityTemplate`을 확장하여 `Mono`/`Flux` 기반 CRUD 메서드를 제공하는
`EgovR2dbcRepository`를 제공합니다. 테스트는 **r2dbc-h2 0.9.1.RELEASE**(H2 2.2.220) 인메모리 DB를 사용합니다. 이 모듈에 의존하는 다른 실행환경 모듈은 없습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.psl.reactive.r2dbc:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Spring Data R2DBC 1.5.18 / Reactor Core 3.4.41 (테스트: r2dbc-h2 0.9.1.RELEASE, H2 2.2.220)
- **실행환경 의존**: `org.egovframe.rte.fdl.logging`

---

## 특징

- **논블로킹 관계형 DB 접근**: 블로킹 JDBC `DataSource` 대신 R2DBC `ConnectionFactory`와 `R2dbcEntityTemplate`을 사용하여 `Mono`/`Flux`로 결과 반환
- **접속 설정 단순화**: R2DBC URL 문자열 하나로 `ConnectionFactories.get(url)` 호출하여 `ConnectionFactory` 생성 (드라이버는 적용 프로젝트에서 선택)
- **공통 CRUD 메서드**: `selectAllData`, `selectOneData`, `countData`, `insertData`, `updateData`, `deleteData` 제공 (`org.springframework.data.relational.core.query.Query` 기반)
- **리액티브 트랜잭션·초기화 예제**: `R2dbcTransactionManager`, `ConnectionFactoryInitializer` + `ResourceDatabasePopulator`로 스키마 스크립트 실행
- **Spring WebFlux 연계**: `spring-webflux`에 의존하며 테스트 설정은 `@EnableWebFlux` + `@EnableR2dbcRepositories`로 구성

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/psl/reactive/r2dbc/
├── connect/
│   └── EgovR2dbcConnectionFactory.java   # R2DBC URL -> ConnectionFactory
└── repository/
    └── EgovR2dbcRepository.java          # R2dbcEntityTemplate 확장 (제네릭 CRUD)

src/test/java/org/egovframe/rte/psl/reactive/r2dbc/repository/
├── R2dbcConfiguration.java               # @Configuration, connectionFactory/initializer/transactionManager 빈
├── R2dbcTest.java                        # StepVerifier 기반 insert 검증
├── Sample.java                           # @Table(name="sample") 엔티티 (Lombok)
└── SampleRepository.java                 # EgovR2dbcRepository<Sample> 구현

src/test/resources/script/r2dbc-h2.sql    # sample 테이블 생성 스크립트 (H2)
```

`src/main/resources`는 없습니다.

---

## 주요 클래스

| 클래스 | 설명 |
|--------|------|
| `EgovR2dbcConnectionFactory` | 생성자로 `r2dbcUrl`을 받고 `connectionFactory()`가 `io.r2dbc.spi.ConnectionFactories.get(url)`로 `ConnectionFactory`를 반환 |
| `EgovR2dbcRepository<T>` | `R2dbcEntityTemplate` 확장. 생성자에 `ConnectionFactory`를 받으며 `selectAllData(Query, Class<T>)`: `Flux<T>`, `selectOneData`: `Mono<T>`, `countData`: `Mono<Long>`, `insertData`/`updateData`/`deleteData(T)`: `Mono<T>` 제공 |

---

## 주요 의존성

모듈 POM은 버전을 선언하지 않으며 루트 POM `dependencyManagement`의 버전이 적용됩니다.

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.logging` | 4.3.1 | compile |
| `org.springframework:spring-webflux`, `spring-context`, `spring-tx` | 5.3.39-egov.1 | compile |
| `org.springframework.data:spring-data-r2dbc` | 1.5.18 | compile (Spring Data Commons 2.7.18-egov.1, spring-r2dbc 전이) |
| `io.projectreactor:reactor-core` | 3.4.41 | compile |
| `io.r2dbc:r2dbc-h2` | 0.9.1.RELEASE | test (H2 2.2.220 전이) |
| `io.projectreactor:reactor-test`, `org.assertj:assertj-core` | 3.4.41 / 3.27.7 | test |
| `org.projectlombok:lombok` | 1.18.36 | test (`Sample` 엔티티) |
| `org.springframework:spring-test`, `junit`, `easymock`, `com.googlecode.log4jdbc:log4jdbc` | 5.3.39-egov.1 / 4.13.2 / 5.2.0 / 1.2 | test |

R2DBC 드라이버는 compile 의존성에 포함되지 않으므로 적용 프로젝트에서 사용하는 DB의 R2DBC 드라이버를 직접 추가해야 합니다.

---

## 설정 및 사용 예시

테스트 설정 클래스(`src/test/java/.../R2dbcConfiguration.java`):

```java
@Configuration
@EnableWebFlux
@ComponentScan(basePackages = "org.egovframe.rte.psl.reactive.r2dbc.repository")
@EnableR2dbcRepositories
public class R2dbcConfiguration {

    private String r2dbcUrl = "r2dbc:h2:mem:///sampledb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

    @Bean(name = "connectionFactory")
    public ConnectionFactory connectionFactory() {
        return new EgovR2dbcConnectionFactory(r2dbcUrl).connectionFactory();
    }

    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("script/r2dbc-h2.sql")));
        return initializer;
    }

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}
```

초기화 스크립트(`src/test/resources/script/r2dbc-h2.sql`):

```sql
DROP ALL OBJECTS DELETE FILES;
CREATE MEMORY TABLE IF NOT EXISTS sample(id int PRIMARY KEY auto_increment, sample_id VARCHAR(16) NOT NULL,
    name VARCHAR(50), description VARCHAR(100), use_yn CHAR(1), reg_user VARCHAR(10));
```

엔티티와 Repository(`Sample.java`, `SampleRepository.java`):

```java
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "sample")
public class Sample {
    @Id
    private Integer id;
    @Column(value = "sample_id")
    private String sampleId;
    @Column(value = "name")
    private String name;
    // description, use_yn, reg_user ...
}

@Repository
public class SampleRepository extends EgovR2dbcRepository<Sample> {
    public SampleRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    public Flux<Sample> selectAllSample() {
        return selectAllData(Query.empty(), Sample.class);
    }

    public Mono<Sample> selectOneSample(int id) {
        return selectOneData(Query.query(where("id").is(id)), Sample.class);
    }

    public Mono<Sample> insertSample(Sample sample) {
        return insertData(sample);
    }
}
```

테스트(`R2dbcTest`)는 `repository.insertSample(sample).as(StepVerifier::create).expectNextMatches(...).verifyComplete()`로 결과를 검증합니다.

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Persistence/org.egovframe.rte.psl.reactive.r2dbc -am
```

## 테스트

```bash
mvn test -pl Persistence/org.egovframe.rte.psl.reactive.r2dbc
```

테스트는 r2dbc-h2 인메모리 DB(`r2dbc:h2:mem:///sampledb`)만 사용하므로 외부 서버가 필요 없습니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.psl.reactive.r2dbc</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>` 생략 가능.
`-egov.1` 포크 아티팩트(Spring Framework, Spring Data Commons)를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록 필요.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.logging](../../Foundation/org.egovframe.rte.fdl.logging/README.md), [org.egovframe.rte.ptl.reactive](../../Presentation/org.egovframe.rte.ptl.reactive/README.md), [org.egovframe.rte.psl.reactive.cassandra](../org.egovframe.rte.psl.reactive.cassandra/README.md), [org.egovframe.rte.psl.reactive.mongodb](../org.egovframe.rte.psl.reactive.mongodb/README.md), [org.egovframe.rte.psl.reactive.redis](../org.egovframe.rte.psl.reactive.redis/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
