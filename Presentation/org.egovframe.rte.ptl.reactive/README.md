# org.egovframe.rte.ptl.reactive

**Presentation 계층 리액티브(WebFlux) 화면처리 모듈**입니다. Spring WebFlux 기반 애플리케이션에서 쓰는 스테레오타입 어노테이션(`@EgovController` 등),
공통 오류 코드·예외·JSON 오류 응답 핸들러, Bean Validation(javax.validation) 제약 어노테이션(주민등록번호·사업자등록번호·법인등록번호·이메일·전화번호·IP·비밀번호 등)과
Functional Endpoints용 검증 헬퍼를 제공합니다. `org.egovframe.rte.fdl.logging`에 의존하며, 실행환경의 다른 모듈은 이 모듈에 의존하지 않습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.ptl.reactive:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 (spring-webflux, spring-context) / Reactor Core 3.4.41 / javaee-api 8.0.1 (javax.validation API)
- **실행환경 의존**: `org.egovframe.rte.fdl.logging` 4.3.1

---

## 특징

- **스테레오타입 어노테이션**: `@EgovController`, `@EgovRestController`, `@EgovService`, `@EgovRepository`. Spring의 `@Controller`/`@ResponseBody`/`@Service`/`@Repository`를 메타 어노테이션으로 가지며 `@AliasFor`로 빈 이름(`value`)을 전달합니다.
- **공통 오류 응답**: `EgovExceptionHandler`(`WebExceptionHandler`, `@Order(-3)`)가 `EgovException`/`EgovServiceException`/`ResponseStatusException`/그 외 예외를 `{timestamp, status, code, message}` JSON으로 통일합니다. 미분류 예외의 원본 메시지는 서버 로그에만 남기고 응답은 일반화합니다(CWE-209).
- **오류 코드 상수** `EgovErrorCode`: HTTP 상태·코드(`E001`~`E022`)·메시지 조합 13종.
- **Bean Validation 제약 어노테이션** 11종(`@EgovRrnCheck`, `@EgovCrnCheck`, `@EgovCnCheck`, `@EgovEmailCheck`, `@EgovPhoneCheck`, `@EgovMobilePhoneCheck`, `@EgovKoreanCheck`, `@EgovEnglishCheck`, `@EgovIPCheck`, `@EgovPwdCheck`, `@EgovNullCheck`)과 각 `ConstraintValidator` 구현.
- **Functional Endpoints 검증** `EgovValidation`: `javax.validation.Validator`로 객체를 검증하고 위반 시 `EgovException(INVALID_INPUT_VALUE)`을 던집니다.

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/ptl/reactive/
├── annotation/
│   ├── EgovController.java
│   ├── EgovRepository.java
│   ├── EgovRestController.java
│   └── EgovService.java
├── exception/
│   ├── EgovAbstractService.java
│   ├── EgovErrorCode.java
│   ├── EgovException.java
│   ├── EgovExceptionHandler.java
│   ├── EgovExceptionResponse.java
│   └── EgovServiceException.java
└── validation/
    ├── EgovValidation.java
    ├── EgovCnCheck.java          / EgovCnCheckValidation.java          (법인등록번호)
    ├── EgovCrnCheck.java         / EgovCrnCheckValidation.java         (사업자등록번호)
    ├── EgovRrnCheck.java         / EgovRrnCheckValidation.java         (주민등록번호)
    ├── EgovEmailCheck.java       / EgovEmailCheckValidation.java
    ├── EgovPhoneCheck.java       / EgovPhoneCheckValidation.java       (일반전화)
    ├── EgovMobilePhoneCheck.java / EgovMobilePhoneCheckValidation.java (휴대전화)
    ├── EgovKoreanCheck.java      / EgovKoreanCheckValidation.java
    ├── EgovEnglishCheck.java     / EgovEnglishCheckValidation.java
    ├── EgovIPCheck.java          / EgovIPCheckValidation.java          (IPv4/IPv6)
    ├── EgovPwdCheck.java         / EgovPwdCheckValidation.java
    └── EgovNullCheck.java        / EgovNullCheckValidation.java
```

`src/main/resources`에는 설정 파일이 없습니다.

---

## 주요 클래스

### 어노테이션 (`annotation`)

| 어노테이션 | 메타 어노테이션 | 설명 |
|---|---|---|
| `@EgovController` | `@Controller` | 컨트롤러 표시 |
| `@EgovRestController` | `@Controller` + `@ResponseBody` | REST 컨트롤러 표시 |
| `@EgovService` | `@Service` | 서비스 표시 |
| `@EgovRepository` | `@Repository` | 리포지토리 표시 |

### 예외 처리 (`exception`)

| 클래스 | 설명 |
|---|---|
| `EgovErrorCode` | `INVALID_INPUT_VALUE`(400/E001), `INVALID_TYPE_VALUE`(400/E002), `ENTITY_NOT_FOUND`(400/E003), `UNAUTHORIZED`(401/E004), `JWT_EXPIRED`(401/E005), `ACCESS_DENIED`(403/E006), `NOT_FOUND`(404/E007), `METHOD_NOT_ALLOWED`(405/E008), `NOT_ACCEPTABLE`(406/E009), `REQUIRE_USER_JOIN`(412/E010), `UNPROCESSABLE_ENTITY`(422/E011), `INTERNAL_SERVER_ERROR`(500/E021), `SERVICE_UNAVAILABLE`(503/E022) |
| `EgovException` | 업무 로직에서 던지는 `RuntimeException`. `EgovErrorCode`와 메시지를 가지며 `new EgovException(String)`은 `INVALID_INPUT_VALUE`로 매핑 |
| `EgovServiceException` | 서비스 구현체에서 던지는 `RuntimeException`. `new EgovServiceException(String)`은 `INTERNAL_SERVER_ERROR`로 매핑 |
| `EgovAbstractService` | 서비스 구현체가 상속하는 추상 클래스. `egovServiceException(message)`로 `EgovServiceException` 생성 |
| `EgovExceptionResponse` | 응답 본문 값 객체(`timestamp`(yyyy-MM-dd HH:mm:ss), `status`, `code`, `message`). `of(errorCode, message)` |
| `EgovExceptionHandler` | `WebExceptionHandler` 구현(`@Order(-3)`). `EgovException`/`EgovServiceException`은 담긴 오류 코드로, `ResponseStatusException`은 상태 코드를 `EgovErrorCode`로 변환(401·403·404·405·406·422·503, 그 외 4xx는 E001, 5xx는 E021)하여 예외 메시지(요청 경로 등)를 싣지 않고, 그 외 예외는 500/E021 일반화 메시지로 응답. 이미 전송이 시작된 응답은 건드리지 않음. JSON 직렬화는 json-simple `JSONObject` 사용 |

### 검증 (`validation`)

| 어노테이션 / 검증 클래스 | 규칙 |
|---|---|
| `@EgovRrnCheck` / `EgovRrnCheckValidation` | 주민등록번호(2020년 10월 이전 체계): 13자리, 7번째 자리 1~4, 가중치 검사 |
| `@EgovCrnCheck` / `EgovCrnCheckValidation` | 사업자등록번호: 10자리 가중치 검사 |
| `@EgovCnCheck` / `EgovCnCheckValidation` | 법인등록번호: 13자리 가중치 검사 |
| `@EgovEmailCheck` / `EgovEmailCheckValidation` | 이메일 정규식 |
| `@EgovPhoneCheck` / `EgovPhoneCheckValidation` | 일반전화 `\d{2,3}\d{3,4}\d{4}` (하이픈 제거 후) |
| `@EgovMobilePhoneCheck` / `EgovMobilePhoneCheckValidation` | 휴대전화 `01[016-9]` + 7~8자리 (하이픈 제거 후) |
| `@EgovKoreanCheck` / `EgovKoreanCheckValidation` | 한글만 (`ㄱ-ㅎ가-힣`) |
| `@EgovEnglishCheck` / `EgovEnglishCheckValidation` | 영문자만 |
| `@EgovIPCheck` / `EgovIPCheckValidation` | IPv4 또는 IPv6 형식. 기본 메시지 `Invalid IP Address` |
| `@EgovPwdCheck` / `EgovPwdCheckValidation` | 8~20자, 공백 없이 영문자·숫자·특수문자(`~!@#$%^&*?`) 조합, 같은 문자 3회 이상 반복 금지, 연속 문자(abc, 123) 3개 이상 금지. 정적 메서드 `passwordCheck`, `repetitivePasswordCheck`, `consecutivePasswordCheck` |
| `@EgovNullCheck` / `EgovNullCheckValidation` | 값이 비어 있지 않을 것. 기본 메시지 `Please enter required values` |
| `EgovValidation` | 생성자로 받은 `javax.validation.Validator`로 `validate(T)` 수행. 위반이 있으면 메시지를 `, `로 이어 `EgovException(INVALID_INPUT_VALUE, …)` |

제약 어노테이션의 기본 메시지는 `@EgovIPCheck`, `@EgovNullCheck`를 제외하고 `Invalid input values`이며, `message`·`groups`·`payload` 속성으로 재정의할 수 있습니다.
검증 클래스는 null 값을 별도로 처리하지 않으므로(`EgovIPCheckValidation`, `EgovNullCheckValidation` 제외) null 허용 필드에는 `@EgovNullCheck`를 함께 쓰거나 null을 방지해야 합니다.

---

## 주요 의존성

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.logging` | 4.3.1 | compile (POM 선언. Log4j2 연동) |
| `org.springframework:spring-webflux` | 5.3.39-egov.1 | compile. Reactor Core 3.4.41 전이 |
| `org.springframework:spring-context` | 5.3.39-egov.1 | compile |
| `javax:javaee-api` | 8.0.1 | compile. `javax.validation` API 제공 |
| `javax.servlet:javax.servlet-api` | 4.0.1 | provided |
| `org.slf4j:slf4j-api` | 2.0.17 | compile |
| `com.googlecode.json-simple:json-simple` | 1.1.1 | compile. 오류 응답 JSON 직렬화 |
| `org.hibernate.validator:hibernate-validator` | 6.2.5.Final | test. Bean Validation 구현체 |
| `javax.validation:validation-api`, `javax.el:javax.el-api`, `org.glassfish:javax.el` | 2.0.1.Final / 3.0.0 / 3.0.0 | test |
| `org.springframework:spring-test`, `junit:junit`, `org.easymock:easymock`, `org.powermock:powermock-module-junit4`, `org.powermock:powermock-api-easymock` | 5.3.39-egov.1 / 4.13.2 / 5.2.0 / 2.0.9 / 2.0.9 | test |

버전은 모두 루트 POM `dependencyManagement`에서 관리되며 모듈 POM에는 선언하지 않습니다.
런타임에 제약 어노테이션을 실제로 검증하려면 적용 프로젝트에 Bean Validation 구현체(hibernate-validator 등)가 필요합니다.

---

## 설정 및 사용 예시

### 컨트롤러와 예외 응답 (`src/test/java/.../exception/SampleController.java`, `EgovExceptionHandlerTest.java`)

```java
@EgovController
public class SampleController {
    @GetMapping("/test")
    public Mono<String> test() {
        return Mono.error(new EgovException(EgovErrorCode.NOT_FOUND));
    }
}
```

```java
WebTestClient client = WebTestClient.bindToController(new SampleController())
        .controllerAdvice(new EgovExceptionHandler())
        .build();
client.get().uri("/test").accept(MediaType.APPLICATION_JSON).exchange()
        .expectStatus().isNotFound();     // {"timestamp":…, "status":…, "code":"E007", "message":"Page not found."}
```

애플리케이션에서는 `EgovExceptionHandler`를 빈으로 등록하면 WebFlux가 `WebExceptionHandler`로 인식합니다.

### 제약 어노테이션 (`src/test/java/.../validation/SampleDto.java`, `EgovNotNullValidationTest.java`)

```java
public class SampleDto {
    @EgovNullCheck(message = "Please enter required values")
    private String sampleId;
    // getter / setter
}
```

```java
ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
Validator validator = factory.getValidator();
Set<ConstraintViolation<SampleDto>> violations = validator.validate(dto);
```

### Functional Endpoints 검증

```java
EgovValidation validation = new EgovValidation(validator);   // javax.validation.Validator
SampleDto checked = validation.validate(dto);                 // 위반 시 EgovException(INVALID_INPUT_VALUE)
```

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Presentation/org.egovframe.rte.ptl.reactive -am
```

결과물: `target/org.egovframe.rte.ptl.reactive-4.3.1.jar`, `-sources.jar`, `-javadoc.jar`

## 테스트

```bash
mvn test -pl Presentation/org.egovframe.rte.ptl.reactive
```

외부 서버 없이 `WebTestClient`, `MockServerWebExchange`, hibernate-validator(test)만 사용합니다.
`EgovExceptionHandlerSecurityTest`는 오류 응답이 내부 정보·요청 경로를 노출하지 않는지 검증합니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.ptl.reactive</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>` 생략 가능.
`-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록 필요.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.reactive](../../Foundation/org.egovframe.rte.fdl.reactive/README.md), [org.egovframe.rte.fdl.logging](../../Foundation/org.egovframe.rte.fdl.logging/README.md), [org.egovframe.rte.ptl.mvc](../org.egovframe.rte.ptl.mvc/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
