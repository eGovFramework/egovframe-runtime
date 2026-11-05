# egovframe-boot-starter-logging

전자정부 표준프레임워크의 Spring Boot 로깅 스타터 라이브러리입니다.

## 1. 개요

`egovframe-boot-starter-logging`은 아래 원칙으로 설계된 Spring Boot 로깅 자동 구성(Auto Configuration) 스타터입니다.

1. **무설정 기본 로깅**: 의존성을 주입받는 것만으로, 별도의 설정 없이 요청 로깅 기능이 즉시 동작합니다.
2. **선택적 트레이스 기능**: 클라우드(분산 트레이싱) 환경에서 활용할 수 있도록 traceId/spanId 상관관계 기능을 제공하되, 기본값은 off이며 필요할 때만 켜서 씁니다.
3. **트레이스 사용 시 로그에 노출**: 트레이스 기능을 켜면 별도의 logback/log4j2 설정 없이도 traceId/spanId가 로그 메시지에 함께 노출됩니다.
4. **기존 로깅 설정의 대체**: 이 스타터를 적용하면 애플리케이션 자체의 `log4j2.xml`/`logback-spring.xml` 같은 커스텀 로깅 설정 파일 없이도 동일하거나 더 나은 로깅을 제공하는 것을 목표로 합니다. (적용 예: `egovframe-boot-starter-access-template`, `egovframe-boot-starter-crypto-template`, `egovframe-boot-starter-security-template` — 실행환경 저장소에 포함되지 않은 별도 템플릿 프로젝트)

## 2. 주요 기능

- **요청 로깅(기본 on)**: 매 요청의 method, URI, 응답 상태, 처리 시간을 `org.egovframe.boot.logging.access` 로거로 기록. 별도 설정 없이 의존성만 추가하면 동작
  - 클라이언트가 보낸 method/URI에 개행 문자(CR/LF)가 포함되어 있으면 `_`로 치환한 뒤 기록해 로그 위조(Log Injection, CWE-117)를 방지
- **트레이스 상관관계 표시(기본 off, 선택 기능)**: `trace.enabled=true`로 켜면 [Micrometer Tracing](https://micrometer.io/docs/tracing) `Tracer` 빈이 있을 때(예: `micrometer-tracing-bridge-otel` 연동) 실제 traceId/spanId를, 없으면 요청 단위로 생성한 대체 ID를 MDC에 넣어 로그에 표시
- **자동 로그 패턴 주입**: 트레이스 기능을 켰고 사용자가 `logging.pattern.correlation`을 직접 지정하지 않았다면, Spring Boot 기본 로깅(Logback) 패턴에 상관관계 표시를 자동으로 주입해 zero-config로 동작
- **세분화된 on/off**: 스타터 전체, 트레이스 상관관계, 요청 로깅을 각각 독립적으로 켜고 끌 수 있음

## 3. 요구사항

- Java 17 이상
- Spring Boot 3.5.16 이상
- Spring Web MVC(Servlet) 애플리케이션 — 서블릿 필터로 동작하며 WebFlux 는 지원하지 않습니다

## 4. 설치

### egovframe-boot-starter-parent 사용 시

`egovframe-boot-starter-parent` 5.1.0 을 parent 로 사용하면 이 스타터가 **자동으로 포함**됩니다(access · crypto · logging · security 4종). 이 경우 아무 설정 없이도 요청 로깅이 동작하므로, 사용하지 않으려면 `org.egovframe.logging.enabled=false` 로 끄세요.

### Maven

```xml
<dependency>
    <groupId>org.egovframe.boot</groupId>
    <artifactId>egovframe-boot-starter-logging</artifactId>
    <version>5.1.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'org.egovframe.boot:egovframe-boot-starter-logging:5.1.0'
```

분산 트레이싱(OpenTelemetry)과 연동하려면 아래 의존성을 함께 추가하세요.

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
```

이 의존성이 없어도 스타터는 요청마다 생성한 대체 ID로 상관관계 표시를 계속 제공합니다.

## 5. 설정

**아무 설정 없이 의존성만 추가해도** 요청 로깅은 즉시 동작합니다. 트레이스(traceId/spanId) 기능은 클라우드 환경 등에서 필요할 때만 아래처럼 켜서 사용하는 선택 기능입니다.

### application.yml

```yaml
org:
  egovframe:
    logging:
      enabled: true            # 스타터 전체 on/off (기본값 true)
      trace:
        enabled: true           # 트레이스(traceId/spanId) 기능 on/off — 기본값 false, 필요할 때만 켬
        trace-id-key: traceId
        span-id-key: spanId
      request:
        enabled: true            # 요청 로깅 on/off (기본값 true)
        exclude-patterns: /actuator/**
```

### application.properties

```properties
# 트레이스 기능을 켜고 싶을 때만 추가하면 됩니다. (기본값 false)
org.egovframe.logging.trace.enabled=true
```

### 설정 속성 설명

| 속성 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `enabled` | boolean | `true` | 스타터 전체 활성화 여부 |
| `trace.enabled` | boolean | **`false`** | 트레이스 상관관계(traceId/spanId) 표시 on/off — 클라우드/분산 환경에서 필요할 때만 켜는 **선택 기능** |
| `trace.trace-id-key` | String | `traceId` | traceId를 담을 MDC 키 이름 |
| `trace.span-id-key` | String | `spanId` | spanId를 담을 MDC 키 이름 |
| `request.enabled` | boolean | `true` | 요청 로깅 on/off |
| `request.exclude-patterns` | String | `/actuator/**` | 요청 로깅에서 제외할 Ant 스타일 경로 패턴(콤마로 구분) |

`trace.enabled=true`로 켜면 MDC 삽입과 `logging.pattern.correlation` 자동 주입이 함께 활성화되어 로그에 상관관계 정보가 표시됩니다. `request.enabled`와는 독립적으로 동작하므로 트레이스만 켜거나 요청 로깅만 꺼서 쓸 수도 있습니다.

## 6. 사용 예제

### 기본 상태 (트레이스 off) — 의존성만 추가

```
2026-08-24T17:26:04.038+09:00  INFO 28205 --- [logging-smoke] [io-18080-exec-1] org.egovframe.boot.logging.access : GET /hello - status=200 time=17ms
```

### `org.egovframe.logging.trace.enabled=true`로 켠 경우

```
2026-08-24T17:26:04.032+09:00  INFO 28205 --- [logging-smoke] [io-18080-exec-1] [f7c8644c273447cdae32cbd2ef324c97,] smoke.SmokeApplication : handling /hello
2026-08-24T17:26:04.038+09:00  INFO 28205 --- [logging-smoke] [io-18080-exec-1] [f7c8644c273447cdae32cbd2ef324c97,] org.egovframe.boot.logging.access : GET /hello - status=200 time=17ms
```

`micrometer-tracing-bridge-otel`을 추가하면 대괄호 안의 값이 실제 OpenTelemetry traceId/spanId로 대체되어, 배포단 OTel 수집기(Jaeger/Tempo 등)의 트레이스와 애플리케이션 로그가 동일한 ID로 연결됩니다.

### 실제 적용 예시: egovframe-boot-starter-*-template

별도 템플릿 프로젝트인 `egovframe-boot-starter-access-template`, `egovframe-boot-starter-crypto-template`, `egovframe-boot-starter-security-template` 세 템플릿 모두 원래 `spring-boot-starter-log4j2` + 직접 작성한 `log4j2.xml`로 로깅을 구성했지만, 이 스타터로 완전히 대체했습니다.

- `spring-boot-starter-log4j2` 의존성과 `log4j2.xml` 삭제 → Spring Boot 기본 Logback 사용
- 일부 `egovframe-rte-*` 모듈이 `log4j-slf4j2-impl`을 전이 의존성으로 끌고 와 Logback과 SLF4J 프로바이더가 충돌하므로, 해당 모듈들에서 `log4j-slf4j2-impl`을 `<exclusions>`로 제외 (`pom.xml` 참고)
- 기존 `log4j2.xml`이 지정하던 패키지별 로그 레벨은 `logging.level.egovframework=DEBUG` 같은 표준 Boot 프로퍼티로 대체 (별도 XML 없이 `application.properties`만으로 설정)
- `org.egovframe.logging.trace.enabled=true` 한 줄만 추가해 traceId/spanId 표시를 데모로 활성화

결과적으로 이 템플릿들에는 커스텀 로깅 설정 파일이 전혀 없으며, 의존성과 `application.properties`만으로 요청 로깅 + (선택 시) 트레이스 상관관계가 모두 동작합니다.

### 커스텀 logback 패턴에서 활용

애플리케이션이 자체 `logback-spring.xml`을 사용 중이라면(즉, `logging.pattern.correlation` 자동 주입 대상이 아닌 경우) 아래와 같이 MDC 값을 직접 참조할 수 있습니다.

```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n</pattern>
```

### Log4j2를 사용하는 경우

이 스타터는 애플리케이션이 Spring Boot 기본 로깅(Logback)을 그대로 쓸 때 zero-config로 동작하도록 설계되었습니다. 부득이 `spring-boot-starter-log4j2`로 전환해 자체 `log4j2.xml`을 써야 한다면 `logging.pattern.correlation` 자동 주입 대상이 아니므로 패턴을 직접 추가해야 합니다. 이때 Log4j2의 `%X{key}`는 Logback과 달리 `:-` 기본값 연산자를 지원하지 않으므로(지원하지 않는 문법이 붙으면 항상 빈 값으로 처리됨) 키만 지정합니다.

```xml
<PatternLayout pattern="%d %5p [%X{traceId},%X{spanId}] [%c] %m%n" />
```

## 7. 제공되는 Bean

이 스타터는 다음 Bean들을 자동으로 등록합니다:

| Bean 이름 | 등록 조건 | 필터 순서 | 설명 |
|-----------|-----------|-----------|------|
| `egovTraceLoggingFilter` | `trace.enabled=true` | `HIGHEST_PRECEDENCE` | 요청 처리 구간 동안 traceId/spanId를 MDC에 채우는 서블릿 필터 |
| `egovRequestLoggingFilter` | `request.enabled=true`(기본) | `HIGHEST_PRECEDENCE + 1` | 요청/응답 정보를 access 로그로 남기는 서블릿 필터 |

두 Bean 모두 `FilterRegistrationBean` 으로 등록되며, `enabled=false` 이면 어느 것도 등록되지 않습니다. 트레이스 로그 패턴 자동 주입은 `EnvironmentPostProcessor`(`META-INF/spring.factories`)로 Environment 준비 단계에서 수행됩니다.

## 8. 패키지 구성

| 패키지 | 내용 |
|--------|------|
| `org.egovframe.boot.logging` | 로깅 자동 구성(`EgovLoggingConfiguration`)과 설정 프로퍼티(`org.egovframe.logging.*`) |
| `org.egovframe.boot.logging.env` | 트레이스 사용 시 `logging.pattern.correlation` 에 traceId/spanId 패턴을 주입하는 EnvironmentPostProcessor |
| `org.egovframe.boot.logging.filter` | 요청 로깅 필터와 traceId/spanId 를 MDC 에 설정하는 트레이스 로깅 필터 |

## 9. 라이선스

이 프로젝트는 Apache License 2.0 하에 배포됩니다.

```
Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 10. 관련 링크

- [전자정부 표준프레임워크 공식 사이트](https://www.egovframe.go.kr)

## 11. 버전 정보

- **현재 버전**: 5.1.0
- **Java 버전**: 17
- **Spring Boot 버전**: 3.5.16
- **Last Updated**: 2026.10.23

## 12. 기여

이슈 및 개선 사항은 GitHub Issues를 통해 제출해 주세요.
