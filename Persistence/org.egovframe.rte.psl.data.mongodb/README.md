# org.egovframe.rte.psl.data.mongodb

**영속성(Persistence) 계층 Spring Data MongoDB(동기) 연동 예제 모듈**입니다. **Spring Data MongoDB 3.4.18-egov.1**(표준프레임워크 포크)과
**MongoDB Java Driver(sync) 4.11.5**를 Spring Framework 5.3 환경에서 사용하는 방법을 테스트 코드와 XML 설정 예제로 제공합니다.
이 모듈은 `src/main`이 없고 `src/test`만 있으며, 별도의 실행환경 클래스를 제공하지 않습니다. 빌드 결과 JAR은 의존성(`org.egovframe.rte.fdl.cmmn`, Spring Data MongoDB, 드라이버)을 적용 프로젝트에 전달하는 역할을 합니다.
이 모듈에 의존하는 다른 실행환경 모듈은 없습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.psl.data.mongodb:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Spring Data MongoDB 3.4.18-egov.1 / MongoDB Driver 4.11.5
- **실행환경 의존**: `org.egovframe.rte.fdl.cmmn`

---

## 특징

- **`MongoTemplate` 예제**: 컬렉션 생성/삭제, `save`, `findOne`/`findById`, `upsert`, `remove` 등 `MongoOperations` 기본 연산
- **`MongoRepository` 예제**: `findByName`, `Page<Person> findByName(String, Pageable)`, `deleteByName` 등 메서드 이름 기반 쿼리와 `@Query` JSON 쿼리
- **지리공간(GeoSpatial) 쿼리**: `findByLocationNear(Point, Distance)`와 `2d` 인덱스 사용 예제
- **XML 네임스페이스 설정**: `<mongo:mongo-client>`(credential 포함), `<mongo:db-factory>`, `<mongo:repositories>`
- **인증/비인증 접속 예제**: 인증 접속(`MongoOperationsAuthTest`)과 `MongoClients.create("mongodb://host:port")` 직접 생성(`MongoOperationsAnonymousTest`)

---

## 소스 디렉터리 구조

`src/main`은 없습니다. 테스트 소스와 설정만 있습니다.

```
src/test/java/org/egovframe/rte/psl/data/mongodb/
├── MongoOperationsAnonymousTest.java   # MongoClients.create()로 직접 MongoTemplate 생성
├── MongoOperationsAuthTest.java        # XML 설정(인증)으로 주입된 MongoTemplate 사용
├── MongoTemplateTest.java              # save/findOne/upsert/findById/remove
├── PersonRepositoryTest.java           # 페이징, 쿼리 메서드, @Query, GeoSpatial, delete
├── domain/
│   ├── Address.java
│   └── Person.java                     # @Id String id, name, age, Address address, double[] location
└── repository/
    └── PersonRepository.java           # MongoRepository<Person, String>

src/test/resources/
├── META-INF/spring/context-common.xml        # property-placeholder, component-scan
├── META-INF/spring/context-data-mongodb.xml  # mongo-client, db-factory, MongoTemplate, repositories
├── META-INF/spring/mongo.properties          # 접속 정보
└── log4j2.xml
```

---

## 주요 기능

| 구분 | 테스트 클래스 | 설명 |
|---|---|---|
| 기본 연산 | `MongoTemplateTest` | `dropCollection`/`createCollection`, `save`, `findOne(query(where("_id").is(...)))`, `upsert`, `findById`, `remove` |
| 인증 접속 | `MongoOperationsAuthTest` | `context-data-mongodb.xml`의 `mongoTemplate` 빈으로 컬렉션 생성/삭제 |
| 비인증 접속 | `MongoOperationsAnonymousTest` | `new MongoTemplate(MongoClients.create("mongodb://host:port"), "user")` |
| Repository | `PersonRepositoryTest` | 페이징 조회, 쿼리 메서드, `@Query("{ 'name' : ?0 }")`, `findByLocationNear`, `deleteByName` |

`PersonRepository`:

```java
public interface PersonRepository extends MongoRepository<Person, String> {
    List<Person> findByName(String name);
    Page<Person> findByName(String name, Pageable pageable);
    Person findByAddress(Address address);
    List<Person> deleteByName(String name);
    Long deletePersonByName(String lastname);
    List<Person> findByLocationNear(Point location, Distance distance);

    @Query("{ 'name' : ?0 }")
    List<Person> findByPersonName(String name);
}
```

---

## 주요 의존성

모듈 POM은 버전을 선언하지 않으며 루트 POM `dependencyManagement`의 버전이 적용됩니다.

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.cmmn` | 4.3.1 | compile (Spring Framework 전이) |
| `org.springframework.data:spring-data-mongodb` | 3.4.18-egov.1 | compile (spring-core/beans/context/expression/tx, slf4j 제외 → 실행환경 버전 사용) |
| `org.mongodb:mongodb-driver-sync` | 4.11.5 | compile (JDK 1.8에서 사용할 수 없는 `bson-record-codec`(class 61)은 루트 POM에서 제외) |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `org.springframework:spring-test`, `junit`, `easymock` | 5.3.39-egov.1 / 4.13.2 / 5.2.0 | test |

Spring Data Commons는 `2.7.18-egov.1` 포크가 전이 의존성으로 적용됩니다.

---

## 설정 및 사용 예시

`src/test/resources/META-INF/spring/context-data-mongodb.xml`:

```xml
<beans xmlns:mongo="http://www.springframework.org/schema/data/mongo"
       xmlns:context="http://www.springframework.org/schema/context" ...>

    <context:property-placeholder location="classpath:META-INF/spring/mongo.properties"/>

    <mongo:mongo-client id="mongoClient" host="${mongodb.host}" port="${mongodb.port}"
                        credential="${mongodb.username}:${mongodb.password}@${mongodb.database}"/>

    <mongo:db-factory id="mongoDbFactory" mongo-client-ref="mongoClient" dbname="${mongodb.database}"/>

    <bean id="mongoTemplate" class="org.springframework.data.mongodb.core.MongoTemplate">
        <constructor-arg name="mongoDbFactory" ref="mongoDbFactory"/>
    </bean>

    <mongo:repositories base-package="org.egovframe.rte.psl.data.mongodb.repository"/>
</beans>
```

`mongo.properties`:

```properties
mongodb.host=localhost
mongodb.port=27017
mongodb.database=com
mongodb.username=com
mongodb.password=com01
```

`MongoTemplate` 사용 예(`MongoTemplateTest`):

```java
@Autowired
private MongoTemplate mongoTemplate;

mongoTemplate.dropCollection("person");
mongoTemplate.createCollection("person");
mongoTemplate.save(person, "person");
person = mongoTemplate.findOne(query(where("_id").is("1001")), Person.class);
mongoTemplate.upsert(query(where("_id").is("1001")), update("age", 40), Person.class);
person = mongoTemplate.findById("1001", Person.class);
mongoTemplate.remove(person);
```

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Persistence/org.egovframe.rte.psl.data.mongodb -am
```

## 테스트

```bash
mvn test -pl Persistence/org.egovframe.rte.psl.data.mongodb
```

테스트는 로컬 MongoDB 서버가 필요합니다. 서버가 없으면 접속 오류로 실패합니다.

| 항목 | 값 (`META-INF/spring/mongo.properties`) |
|---|---|
| 호스트 / 포트 | `localhost` / `27017` |
| 데이터베이스 | `com` |
| 계정 / 비밀번호 | `com` / `com01` |

`MongoOperationsAnonymousTest`는 인증 없이 `user` 데이터베이스에 접속하므로 인증이 강제된 서버에서는 실패할 수 있습니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.psl.data.mongodb</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>` 생략 가능.
`-egov.1` 포크 아티팩트(Spring Framework, Spring Data Commons, Spring Data MongoDB)를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록 필요.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.cmmn](../../Foundation/org.egovframe.rte.fdl.cmmn/README.md), [org.egovframe.rte.psl.reactive.mongodb](../org.egovframe.rte.psl.reactive.mongodb/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
