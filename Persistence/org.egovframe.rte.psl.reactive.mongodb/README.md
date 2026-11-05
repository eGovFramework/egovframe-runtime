# org.egovframe.rte.psl.reactive.mongodb

**영속성(Persistence) 계층 리액티브 MongoDB 연동 모듈**입니다. **Spring Data MongoDB 3.4.18-egov.1**(표준프레임워크 포크)과
**MongoDB Reactive Streams Driver 4.11.5**를 기반으로, 접속 URL로 `ReactiveMongoDatabaseFactory`를 만드는 `EgovMongoDbConnectionFactory`와
`ReactiveMongoTemplate`을 확장하여 `Mono`/`Flux` 기반 CRUD 메서드를 제공하는 `EgovMongoDbRepository`를 제공합니다.
동기 방식은 [org.egovframe.rte.psl.data.mongodb](../org.egovframe.rte.psl.data.mongodb/README.md)를 사용합니다. 이 모듈에 의존하는 다른 실행환경 모듈은 없습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.psl.reactive.mongodb:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Spring Data MongoDB 3.4.18-egov.1 / MongoDB Driver 4.11.5 / Reactor Core 3.4.41
- **실행환경 의존**: `org.egovframe.rte.fdl.logging`

---

## 특징

- **논블로킹 MongoDB 접근**: `mongodb-driver-reactivestreams`와 `ReactiveMongoTemplate`을 사용하여 `Mono`/`Flux`로 결과 반환
- **접속 설정 단순화**: DB 이름과 접속 URL(`mongodb://user:pw@host:port/db`)만으로 `SimpleReactiveMongoDatabaseFactory` 생성
- **공통 CRUD 메서드**: `selectAllData`, `selectOneData`, `countData`, `insertData`, `updateData`(findAndModify), `deleteData`(findAndRemove) 제공
- **Spring WebFlux 연계**: `spring-webflux`에 의존하며 테스트 설정은 `@EnableWebFlux` + `@EnableMongoRepositories`로 구성
- **Java Config 기반**: XML 설정 없이 `@Configuration` 클래스에서 `ReactiveMongoDatabaseFactory` 빈을 등록하는 방식

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/psl/reactive/mongodb/
├── connect/
│   └── EgovMongoDbConnectionFactory.java   # 접속 URL -> ReactiveMongoDatabaseFactory
└── repository/
    └── EgovMongoDbRepository.java          # ReactiveMongoTemplate 확장 (제네릭 CRUD)

src/test/java/org/egovframe/rte/psl/reactive/mongodb/repository/
├── MongoDbConfiguration.java               # @Configuration, reactiveMongoDatabaseFactory 빈
├── MongoDbTest.java                        # StepVerifier 기반 insert 검증
├── Sample.java                             # @Document(collection="sample") (Lombok)
└── SampleRepository.java                   # EgovMongoDbRepository<Sample> 구현
```

`src/main/resources`, `src/test/resources`는 없습니다 (접속 정보는 테스트 설정 클래스에 하드코딩).

---

## 주요 클래스

| 클래스 | 설명 |
|--------|------|
| `EgovMongoDbConnectionFactory` | 생성자로 `mongoDbName`, `mongoDbUrl`을 받고 `reactiveMongoDatabaseFactory()`가 `ConnectionString` → `MongoClientSettings` → `MongoClients.create()`(reactivestreams)로 `SimpleReactiveMongoDatabaseFactory`를 반환 |
| `EgovMongoDbRepository<T>` | `ReactiveMongoTemplate` 확장. 생성자에 `ReactiveMongoDatabaseFactory`를 받으며 `selectAllData(Query, Class<T>)`: `Flux<T>`, `selectOneData`: `Mono<T>`, `countData`: `Mono<Long>`, `insertData(T)`: `Mono<T>`, `updateData(Query, Update, Class<T>)`: `Mono<T>`(findAndModify), `deleteData(Query, Class<T>)`: `Mono<T>`(findAndRemove) 제공 |

---

## 주요 의존성

모듈 POM은 버전을 선언하지 않으며 루트 POM `dependencyManagement`의 버전이 적용됩니다.

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.logging` | 4.3.1 | compile |
| `org.springframework:spring-webflux`, `spring-context`, `spring-tx` | 5.3.39-egov.1 | compile |
| `org.springframework.data:spring-data-mongodb` | 3.4.18-egov.1 | compile (Spring Data Commons 2.7.18-egov.1 전이) |
| `org.mongodb:mongodb-driver-reactivestreams`, `mongodb-driver-core`, `mongodb-driver-sync` | 4.11.5 | compile (JDK 1.8에서 사용할 수 없는 `bson-record-codec`(class 61)은 루트 POM에서 제외) |
| `io.projectreactor:reactor-core` | 3.4.41 | compile |
| `io.projectreactor:reactor-test`, `org.assertj:assertj-core` | 3.4.41 / 3.27.7 | test |
| `org.projectlombok:lombok` | 1.18.36 | test (`Sample` 엔티티) |
| `org.springframework:spring-test`, `junit`, `easymock`, `com.googlecode.log4jdbc:log4jdbc` | 5.3.39-egov.1 / 4.13.2 / 5.2.0 / 1.2 | test |

---

## 설정 및 사용 예시

테스트 설정 클래스(`src/test/java/.../MongoDbConfiguration.java`):

```java
@Configuration
@EnableWebFlux
@ComponentScan(basePackages = "org.egovframe.rte.psl.reactive.mongodb.repository")
@EnableMongoRepositories
public class MongoDbConfiguration {

    private String mongoDbName = "com";
    private String mongoDbUrl = "mongodb://com:com01@localhost:27017/com";

    @Bean(name = "reactiveMongoDatabaseFactory")
    public ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory() {
        return new EgovMongoDbConnectionFactory(mongoDbName, mongoDbUrl).reactiveMongoDatabaseFactory();
    }
}
```

도큐먼트와 Repository(`Sample.java`, `SampleRepository.java`):

```java
@Data @AllArgsConstructor @NoArgsConstructor
@Document(collection = "sample")
public class Sample {
    @Id
    private Integer id;
    @Field("sample_id")
    private String sampleId;
    @Field("name")
    private String name;
    // description, use_yn, reg_user ...
}

@Repository
public class SampleRepository extends EgovMongoDbRepository<Sample> {
    public SampleRepository(ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
        super(reactiveMongoDatabaseFactory);
    }

    public Mono<Sample> selectOneSample(int id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        return selectOneData(query, Sample.class);
    }

    public Mono<Sample> updateSample(Sample sample) {
        Query query = new Query().addCriteria(Criteria.where("id").is(sample.getId()));
        Update update = new Update().set("sample_id", sample.getSampleId()).set("name", sample.getName());
        return updateData(query, update, Sample.class);
    }

    public Mono<Sample> insertSample(Sample sample) {
        return insertData(sample);
    }
}
```

테스트(`MongoDbTest`)는 `@Before`에서 `repository.dropCollection(Sample.class).block()`으로 컬렉션을 비운 뒤
`repository.insertSample(sample).as(StepVerifier::create).expectNextMatches(...).verifyComplete()`로 결과를 검증합니다.

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Persistence/org.egovframe.rte.psl.reactive.mongodb -am
```

## 테스트

```bash
mvn test -pl Persistence/org.egovframe.rte.psl.reactive.mongodb
```

테스트는 로컬 MongoDB 서버가 필요합니다. 서버가 없으면 접속 오류로 실패합니다.

| 항목 | 값 (`MongoDbConfiguration.java`) |
|---|---|
| 접속 URL | `mongodb://com:com01@localhost:27017/com` |
| 데이터베이스 / 컬렉션 | `com` / `sample` |
| 계정 / 비밀번호 | `com` / `com01` |

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.psl.reactive.mongodb</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>` 생략 가능.
`-egov.1` 포크 아티팩트(Spring Framework, Spring Data Commons, Spring Data MongoDB)를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록 필요.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.logging](../../Foundation/org.egovframe.rte.fdl.logging/README.md), [org.egovframe.rte.psl.data.mongodb](../org.egovframe.rte.psl.data.mongodb/README.md), [org.egovframe.rte.psl.reactive.cassandra](../org.egovframe.rte.psl.reactive.cassandra/README.md), [org.egovframe.rte.psl.reactive.r2dbc](../org.egovframe.rte.psl.reactive.r2dbc/README.md), [org.egovframe.rte.psl.reactive.redis](../org.egovframe.rte.psl.reactive.redis/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
