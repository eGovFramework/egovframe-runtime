# org.egovframe.rte.fdl.reactive

**Foundation Layer 리액티브(Reactive) 모듈**입니다. **Spring WebFlux** 기반 서비스에서 공통으로 쓰는 두 가지 소형 빌딩 블록, 즉 Reactor `Context`의 값을 SLF4J MDC로 전파하는 `EgovMdcContextConfig`와 해시 기반 시퀀스(식별자) 생성기 `EgovSequenceGenerator`를 제공합니다. 다른 실행환경 모듈이 이 모듈에 의존하지는 않으며, WebFlux 적용 프로젝트에서 직접 사용합니다. 표현 계층의 WebFlux 확장(커스텀 어노테이션, 예외처리, 유효성검증)은 `ptl.reactive` 모듈에 있습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.fdl.reactive:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 (`spring-webflux`) / Reactor Core 3.4.41 / SLF4J 2.0.17
- **실행환경 의존**: 없음

---

## 특징

- **Reactor Context → MDC 전파**: `Hooks.onEachOperator()`로 모든 Mono/Flux 연산자에 `EgovSubscriber`를 끼워 넣어, `onNext` 시점마다 구독 `Context`의 키/값을 `MDC.setContextMap()`으로 복사합니다. 스레드가 바뀌는 논블로킹 파이프라인에서도 로그에 요청 식별 정보가 유지됩니다.
- **민감 키 제외(4.3.1 추가)**: `EgovMdcContextConfig.setExcludedKeys(Set<String>)`로 세션ID·토큰 등 로그에 남기면 안 되는 Context 키를 MDC 복사 대상에서 제외합니다(CWE-532 대응). 기본값은 빈 집합(제외 없음)입니다.
- **라이프사이클 연동**: `@PostConstruct`에서 훅을 등록하고 `@PreDestroy`에서 `Hooks.resetOnEachOperator()`로 해제하므로 Spring 빈으로 등록만 하면 됩니다.
- **시퀀스 생성**: 현재 시각(`yyyyMMddHHmmssSSS`)과 5자리 영숫자 난수를 붙인 문자열을 `MessageDigest`(SHA-1, SHA-256 등 호출 시 지정)로 해시하여 16진수 문자열을 반환합니다. 알고리즘이 없으면 빈 문자열을 반환합니다.

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/fdl/reactive/
├── idgnr/
│   └── EgovSequenceGenerator.java   # 해시 기반 시퀀스 생성 (정적 메서드)
└── logging/
    └── EgovMdcContextConfig.java    # Reactor Context → MDC 복사 훅, 내부 클래스 EgovSubscriber
```

`src/main/resources`, `src/test/resources`는 없습니다.

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovMdcContextConfig` | `contextOperatorHook()`(`@PostConstruct`)에서 `Hooks.onEachOperator(MDC_CONTEXT_KEY, Operators.lift(...))`를 등록하고, `cleanupHook()`(`@PreDestroy`)에서 해제. 정적 메서드 `setExcludedKeys(Set<String>)`로 MDC 복사 제외 키 지정 |
| `EgovMdcContextConfig.EgovSubscriber<T>` | `CoreSubscriber<T>` 구현. `onNext()`마다 `currentContext()`의 항목을 (제외 키를 걸러) `MDC.setContextMap()`에 넣고, Context가 비어 있으면 `MDC.clear()` |
| `EgovSequenceGenerator` | `generateSequence(String instance)`: `MessageDigest` 알고리즘명을 받아 시각+난수 문자열의 해시를 16진수로 반환. 보조 메서드 `localeDateTime()`, `generateRandomString()`, `generateHash(data, instance)`는 `protected static` |

---

## 주요 의존성

| 라이브러리 | 버전 | scope / 비고 |
|---|---|---|
| `org.springframework:spring-webflux` | 5.3.39-egov.1 | compile. `reactor-core` 3.4.41, `reactive-streams` 1.0.4 전이 |
| `org.springframework:spring-context` | 5.3.39-egov.1 | compile |
| `org.slf4j:slf4j-api` | 2.0.17 | compile. `MDC`, `Logger` |
| `javax:javaee-api` | 8.0.1 | compile. `@PostConstruct`, `@PreDestroy` |
| `javax.servlet:javax.servlet-api` | 4.0.1 | provided |
| `junit` / `easymock` / `powermock-module-junit4` / `powermock-api-easymock` | 4.13.2 / 5.2.0 / 2.0.9 / 2.0.9 | test |

버전은 모두 루트 `pom.xml`의 `dependencyManagement`에서 관리하며 모듈 POM에는 선언하지 않습니다. 이 모듈은 로깅 구현체(`log4j-core` 등)를 포함하지 않으므로 적용 프로젝트에서 `fdl.logging` 또는 별도의 SLF4J 바인딩을 추가해야 MDC 값이 실제 로그에 출력됩니다.

---

## 설정 및 사용 예시

### MDC 전파 빈 등록 (Java Config)

```java
@Configuration
public class ReactiveLoggingConfig {

    @Bean
    public EgovMdcContextConfig egovMdcContextConfig() {
        // 세션ID 등 민감한 Context 키는 로그에서 제외
        EgovMdcContextConfig.setExcludedKeys(new HashSet<>(Arrays.asList("sessionId", "token")));
        return new EgovMdcContextConfig();
    }
}
```

XML로 등록할 경우 `<bean class="org.egovframe.rte.fdl.reactive.logging.EgovMdcContextConfig"/>`만 선언하면 `@PostConstruct`/`@PreDestroy`가 동작합니다(`<context:annotation-config/>` 필요).

### Reactor Context에 값 넣기

```java
Mono.just(result)
    .doOnNext(r -> LOGGER.info("processed"))           // 로그 패턴의 %X{requestId}에 값 출력
    .contextWrite(Context.of("requestId", requestId));  // 이 값이 onNext 시 MDC로 복사됨
```

로그 패턴에는 `%X{키}`를 사용합니다(예: `%d %5p [%X{requestId}] %m%n`).

### 시퀀스 생성 (EgovSequenceGeneratorTest)

```java
String seq1 = EgovSequenceGenerator.generateSequence("SHA-1");    // 40자리 16진수
String seq2 = EgovSequenceGenerator.generateSequence("SHA-256");  // 64자리 16진수
EgovSequenceGenerator.generateSequence("NO-SUCH-ALGORITHM");      // "" (빈 문자열)
```

테스트(`EgovSequenceGeneratorTest`)는 해시 형식, 1000회 생성 시 유일성, 잘못된 알고리즘명 처리를 검증합니다.

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Foundation/org.egovframe.rte.fdl.reactive -am
```

## 테스트

```bash
mvn test -pl Foundation/org.egovframe.rte.fdl.reactive
```

외부 서버는 필요하지 않습니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.fdl.reactive</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>`을 생략할 수 있습니다. `-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록이 필요합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.ptl.reactive](../../Presentation/org.egovframe.rte.ptl.reactive/README.md), [org.egovframe.rte.fdl.idgnr](../org.egovframe.rte.fdl.idgnr/README.md), [org.egovframe.rte.fdl.logging](../org.egovframe.rte.fdl.logging/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
