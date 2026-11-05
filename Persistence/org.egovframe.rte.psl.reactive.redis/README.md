# org.egovframe.rte.psl.reactive.redis

**영속성(Persistence) 계층 리액티브 Redis 연동 모듈**입니다. **Spring Data Redis 2.7.18**과 **Lettuce 6.5.5.RELEASE**를 기반으로,
호스트·포트·비밀번호로 `LettuceConnectionFactory`를 만드는 `EgovRedisConfiguration`과 `ReactiveRedisTemplate`을 확장하여 List 연산(`opsForList()`) 기반
`Mono`/`Flux` CRUD 메서드를 제공하는 `EgovRedisRepository`를 제공합니다. 값 직렬화에는 Jackson(`Jackson2JsonRedisSerializer`)을 사용합니다.
이 모듈에 의존하는 다른 실행환경 모듈은 없습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.psl.reactive.redis:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Spring Data Redis 2.7.18 / Lettuce 6.5.5.RELEASE / Jackson 2.18.11 / Reactor Core 3.4.41
- **실행환경 의존**: `org.egovframe.rte.fdl.logging`

---

## 특징

- **논블로킹 Redis 접근**: Lettuce 기반 `ReactiveRedisConnectionFactory`와 `ReactiveRedisTemplate`을 사용하여 `Mono`/`Flux`로 결과 반환
- **Standalone 접속 설정 단순화**: `EgovRedisConfiguration(host, port, password)`가 `RedisStandaloneConfiguration`으로 `LettuceConnectionFactory` 생성
- **List 기반 공통 메서드**: `selectData`(range), `findIndex`(indexOf), `countData`(size), `insertData`(leftPush), `updateData`(set), `deleteData`(remove), `deleteAllData`(delete)
- **JSON 직렬화**: `RedisSerializationContext`에 `StringRedisSerializer`(키)와 `Jackson2JsonRedisSerializer`(값)를 조합하여 객체 저장
- **Spring WebFlux 연계**: `spring-webflux`에 의존하며 테스트 설정은 `@EnableWebFlux` Java Config로 구성

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/psl/reactive/redis/
├── connect/
│   └── EgovRedisConfiguration.java     # host/port/password -> LettuceConnectionFactory
└── repository/
    └── EgovRedisRepository.java        # ReactiveRedisTemplate 확장 (List 연산 CRUD)

src/test/java/org/egovframe/rte/psl/reactive/redis/repository/
├── RedisConfiguration.java             # @Configuration, 커넥션 팩토리·직렬화 컨텍스트 빈 (localhost:6379)
├── RedisTest.java                      # StepVerifier 기반 insert 검증
├── Sample.java                         # @RedisHash("sample") 엔티티 (Lombok)
└── SampleRepository.java               # EgovRedisRepository<Sample> 구현
```

`src/main/resources`, `src/test/resources`는 없습니다 (접속 정보는 테스트 설정 클래스에 하드코딩).

---

## 주요 클래스

| 클래스 | 설명 |
|--------|------|
| `EgovRedisConfiguration` | 생성자로 `host`, `port`, `password`를 받고 `reactiveRedisConnectionFactory()`가 `RedisStandaloneConfiguration`을 구성하여 `LettuceConnectionFactory`를 반환 |
| `EgovRedisRepository<T>` | `ReactiveRedisTemplate` 확장. 생성자에 `ReactiveRedisConnectionFactory`와 `RedisSerializationContext`를 받음. `selectData(key)`: `Flux<T>`(range 0~-1), `findIndex(key, entity)`: `Mono<Long>`, `countData(key)`: `Mono<Long>`, `insertData(key, entity)`(leftPush), `updateData(key, idx, entity)`(set), `deleteData(key, entity)`: `Mono<Boolean>`(remove), `deleteAllData(key)`: `Mono<Boolean>`(delete) |

---

## 주요 의존성

모듈 POM은 버전을 선언하지 않으며 루트 POM `dependencyManagement`의 버전이 적용됩니다.

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.logging` | 4.3.1 | compile |
| `org.springframework:spring-webflux`, `spring-context`, `spring-tx` | 5.3.39-egov.1 | compile |
| `org.springframework.data:spring-data-redis` | 2.7.18 | compile (Spring Data Commons 2.7.18-egov.1 전이) |
| `io.lettuce:lettuce-core` | 6.5.5.RELEASE | compile (전이되는 Netty는 루트 POM `netty-bom` 4.1.138.Final로 관리) |
| `com.fasterxml.jackson.core:jackson-core`, `jackson-databind` | 2.18.11 | compile (`jackson-bom`) |
| `io.projectreactor:reactor-core` | 3.4.41 | compile |
| `io.projectreactor:reactor-test`, `org.assertj:assertj-core` | 3.4.41 / 3.27.7 | test |
| `org.projectlombok:lombok` | 1.18.36 | test (`Sample` 엔티티) |
| `org.springframework:spring-test`, `junit`, `easymock`, `com.googlecode.log4jdbc:log4jdbc` | 5.3.39-egov.1 / 4.13.2 / 5.2.0 / 1.2 | test |

---

## 설정 및 사용 예시

테스트 설정 클래스(`src/test/java/.../RedisConfiguration.java`):

```java
@Configuration
@EnableWebFlux
@ComponentScan(basePackages = "org.egovframe.rte.psl.reactive.redis.repository")
public class RedisConfiguration {

    private String host = "localhost";
    private int port = 6379;
    private String password = "redis001";

    @Bean(name = "reactiveRedisConnectionFactory")
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new EgovRedisConfiguration(host, port, password).reactiveRedisConnectionFactory();
    }

    @Bean(name = "sampleSerializationContext")
    public RedisSerializationContext<String, Sample> sampleSerializationContext() {
        Jackson2JsonRedisSerializer<Sample> serializer = new Jackson2JsonRedisSerializer<>(Sample.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, Sample> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        return builder.value(serializer).hashValue(serializer).hashKey(serializer).build();
    }
}
```

엔티티와 Repository(`Sample.java`, `SampleRepository.java`):

```java
@Data @AllArgsConstructor @NoArgsConstructor
@RedisHash("sample")
public class Sample {
    @Id
    private String id;
    @Indexed
    private String sampleId;
    @Indexed
    private String name;
    // description, useYn, regUser ...
}

@Repository
public class SampleRepository extends EgovRedisRepository<Sample> {
    public SampleRepository(
            @Qualifier("reactiveRedisConnectionFactory") ReactiveRedisConnectionFactory connectionFactory,
            @Qualifier("sampleSerializationContext") RedisSerializationContext serializationContext) {
        super(connectionFactory, serializationContext);
    }

    public Flux<Sample> selectAllSample() {
        return selectData("sample");
    }

    public Mono<Boolean> insertSample(Sample sample) {
        return insertData("sample", sample).hasElement();
    }

    public Mono<Sample> updateSample(Sample sample) {
        return findIndex("sample", sample).flatMap(idx -> updateData("sample", idx, sample));
    }

    public Mono<Boolean> deleteSample(Sample sample) {
        return deleteData("sample", sample).hasElement();
    }
}
```

테스트(`RedisTest`)는 `repository.insertSample(sample).as(StepVerifier::create).expectNextMatches(item -> item.booleanValue() == true).verifyComplete()`로 결과를 검증합니다.

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Persistence/org.egovframe.rte.psl.reactive.redis -am
```

## 테스트

```bash
mvn test -pl Persistence/org.egovframe.rte.psl.reactive.redis
```

테스트는 로컬 Redis 서버가 필요합니다. 서버가 없으면 접속 오류로 실패합니다.

| 항목 | 값 (`RedisConfiguration.java`) |
|---|---|
| 호스트 / 포트 | `localhost` / `6379` |
| 비밀번호 | `redis001` |
| 키 | `sample` (List 타입) |

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.psl.reactive.redis</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>` 생략 가능.
`-egov.1` 포크 아티팩트(Spring Framework, Spring Data Commons)를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록 필요.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.logging](../../Foundation/org.egovframe.rte.fdl.logging/README.md), [org.egovframe.rte.psl.reactive.cassandra](../org.egovframe.rte.psl.reactive.cassandra/README.md), [org.egovframe.rte.psl.reactive.mongodb](../org.egovframe.rte.psl.reactive.mongodb/README.md), [org.egovframe.rte.psl.reactive.r2dbc](../org.egovframe.rte.psl.reactive.r2dbc/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
