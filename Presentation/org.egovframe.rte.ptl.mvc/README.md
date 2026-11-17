# org.egovframe.rte.ptl.mvc

**Presentation 계층 화면처리(MVC) 모듈**입니다. Spring MVC(javax Servlet/JSP) 기반 웹 애플리케이션에서 반복되는 화면처리 기능,
즉 페이지네이션 JSP 태그, 요청 파라미터 HTML 태그 필터, Commons Validator 확장 검증 규칙, REST 예외 핸들러, 중복 제출 방지 토큰,
세션 만료 쿠키 필터, 안전 기본값 쿠키 빌더, 요청 컨텍스트 파사드를 제공합니다.
`org.egovframe.rte.fdl.cmmn`과 `spring-modules-validation`(0.9)에 의존하며, 실행환경의 다른 모듈은 이 모듈에 의존하지 않습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.ptl.mvc:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 (spring-webmvc) / javax.servlet-api 4.0.1 · javax.servlet.jsp-api 2.3.3 (provided) / Commons Validator 1.9.0
- **실행환경 의존**: `org.egovframe.rte.fdl.cmmn` 4.3.1, `spring-modules-validation` 0.9

---

## 특징

- **페이지네이션 태그** `<ui:pagination>`: `PaginationInfo`를 받아 `PaginationRenderer`가 HTML을 생성하며, `PaginationManager` 빈 설정으로 렌더러를 `type` 속성별로 교체할 수 있습니다.
- **HTML 태그 필터** `HTMLTagFilter`: 요청 파라미터의 `< > & " '`를 엔티티로 인코딩합니다. 초기화 파라미터 `allowedTags`, `richTextParameters`, `excludePaths`로 허용 태그·리치텍스트 우회·제외 경로를 지정할 수 있습니다.
- **폼 검증 확장** `RteFieldChecks`/`RteGenericValidator`: Commons Validator(`validator-rules.xml`)에서 호출할 수 있는 주민등록번호·한글·영문·HTML 태그·비밀번호 규칙을 추가합니다. `FieldChecks`는 `spring-modules-validation`의 클래스입니다.
- **REST 예외 응답** `EgovRestExceptionHandler`: 실행환경 예외 계층과 Spring MVC 표준 예외를 `application/problem+json` 형식(RFC 7807)으로 매핑하는 기본 구현입니다. 내부 정보·거부된 입력값은 응답에 싣지 않습니다.
- **중복 제출 방지 토큰** `EgovSubmitTokens` + `<ui:submitToken>` 태그: 세션 저장소(`EgovSubmitTokenStore`)에서 토큰을 원자적으로 발급·소비합니다.
- **세션 만료 쿠키 필터** `EgovSessionExpiryCookieFilter`/`EgovSessionExpiry`: 서버 시각과 세션 만료 예정 시각을 쿠키로 내려 화면이 잔여 시간을 표시할 수 있게 합니다.
- **쿠키 빌더** `EgovCookies`: HttpOnly·SameSite=Lax·컨텍스트 루트 Path를 기본값으로 하고 헤더 주입 문자를 생성 시점에 거부합니다.
- **요청 컨텍스트 파사드** `EgovRequestContext`: `RequestContextHolder`의 현재 요청·세션·헤더를 `Optional`로 조회합니다.

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/ptl/mvc/
├── bind/
│   ├── AnnotationCommandMapArgumentResolver.java   (@Deprecated)
│   ├── CommandMapArgumentResolver.java             (@Deprecated)
│   ├── annotation/
│   │   ├── CommandMap.java                         (@Deprecated)
│   │   └── EgovRequestMappingHandlerAdapter.java   (@Deprecated)
│   └── exception/
│       ├── AbstractAnnotationExceptionHandler.java
│       └── EgovRestExceptionHandler.java
├── context/
│   └── EgovRequestContext.java
├── cookie/
│   └── EgovCookies.java
├── filter/
│   ├── HTMLTagFilter.java
│   └── HTMLTagFilterRequestWrapper.java
├── handler/
│   └── SimpleUrlAnnotationHandlerMapping.java      (@Deprecated)
├── session/
│   ├── EgovSessionExpiry.java
│   └── EgovSessionExpiryCookieFilter.java
├── tags/ui/
│   ├── PaginationTag.java
│   ├── SubmitTokenTag.java
│   └── pagination/
│       ├── AbstractPaginationRenderer.java
│       ├── DefaultPaginationManager.java
│       ├── DefaultPaginationRenderer.java
│       ├── PaginationInfo.java
│       ├── PaginationManager.java
│       └── PaginationRenderer.java
├── token/
│   ├── EgovSubmitTokenException.java
│   ├── EgovSubmitTokenStore.java
│   └── EgovSubmitTokens.java
└── validation/
    ├── RteFieldChecks.java
    └── RteGenericValidator.java

src/main/resources/META-INF/
└── taglib.tld        (uri: http://egovframework.gov/ctl/ui — pagination, submitToken 태그)
```

`@Deprecated` 클래스(`CommandMap` 계열, `SimpleUrlAnnotationHandlerMapping`)는 하위 호환을 위해 남아 있으며, `@RequestParam` 등 Spring MVC 표준 기능으로 대체되었습니다.

---

## 주요 클래스

### 페이지네이션 (`tags.ui`, `tags.ui.pagination`)

| 클래스 | 설명 |
|---|---|
| `PaginationTag` | `<ui:pagination paginationInfo type jsFunction>` 태그 구현. `paginationManager` 빈이 있으면 그 빈에서, 없으면 `DefaultPaginationManager`에서 렌더러를 얻어 출력 |
| `PaginationInfo` | 페이징 계산 빈. 입력 필드 `currentPageNo`, `recordCountPerPage`, `pageSize`, `totalRecordCount`로 `totalPageCount`, `firstPageNoOnPageList`, `lastPageNoOnPageList`, `firstRecordIndex`, `lastRecordIndex`를 계산 |
| `PaginationManager` / `DefaultPaginationManager` | `rendererType` Map(key → `PaginationRenderer`)에서 태그의 `type` 값에 맞는 렌더러를 반환. 없으면 `DefaultPaginationRenderer` |
| `PaginationRenderer` / `AbstractPaginationRenderer` / `DefaultPaginationRenderer` | `[처음][이전] 1 2 3 … [다음][마지막]` 형식 렌더링. `firstPageLabel` 등 6개 라벨 문자열(`MessageFormat`)을 하위 클래스에서 바꿔 UI를 커스터마이징 |

### 필터·쿠키·세션·요청 컨텍스트

| 클래스 | 설명 |
|---|---|
| `HTMLTagFilter` | `javax.servlet.Filter`. 요청을 `HTMLTagFilterRequestWrapper`로 감싸 파라미터의 HTML 특수문자를 인코딩. 초기화 파라미터 `allowedTags`(속성 없는 태그만 통과), `richTextParameters`(인코딩 우회 파라미터명), `excludePaths`(`/*` 접미는 접두 일치) |
| `HTMLTagFilterRequestWrapper` | `getParameter`, `getParameterValues`, `getParameterMap`을 재정의하여 인코딩된 값을 반환 |
| `EgovCookies` | `builder(name, value)` / `expire(name)` 빌더(`httpOnly`, `sameSite`, `secure`, `path`, `domain`, `maxAge`, `partitioned`, `encodeValue`, `build`, `addTo`)와 읽기 헬퍼 `value`, `decodedValue`. `SameSite=None`이면서 Secure가 아닌 조합, 제어 문자·CR/LF·`;` 포함 입력은 `IllegalArgumentException` |
| `EgovSessionExpiryCookieFilter` | 세션이 있는 요청마다 `egovLatestServerTime`, `egovExpireSessionTime` 쿠키를 갱신. 초기화 파라미터 `serverTimeCookieName`, `expiryTimeCookieName`, `cookiePath`, `httpOnly`(기본 false), `sameSite`(기본 Lax), `excludePathPatterns`(Ant 패턴, 쉼표 구분) |
| `EgovSessionExpiry` | `from(request)`/`of(session, serverTime)`로 얻는 값 객체. `getServerTime`, `getExpiryTime`(무제한 세션이면 `UNLIMITED` = -1), `getRemainingMillis`, `isUnlimited`, `isExpiredAt` |
| `EgovRequestContext` | 정적 파사드. `isPresent`, `getRequest`, `getRequiredRequest`, `getResponse`, `getSession`(세션을 만들지 않음), `getOrCreateSession`, `getRequestUri`, `getMethod`, `getRemoteAddress`(전달 헤더를 해석하지 않음), `getHeader`, `getParameter` |

### 중복 제출 방지 토큰 (`token`)

| 클래스 | 설명 |
|---|---|
| `EgovSubmitTokens` | `issue(request[, tokenKey])`로 UUID 토큰을 발급해 세션에 보관하고, `validate(...)`/`validateOrThrow(...)`로 1회 소비. `reset(request)`로 전체 삭제. 파라미터명 `egovSubmitToken`, 기본 키 `DEFAULT`, 세션당 최대 32개 |
| `EgovSubmitTokenStore` | 세션 속성에 저장되는 `Serializable` 저장소. `put`/`consume`/`isIssued`/`clear`를 `synchronized`로 처리하여 동시 요청 중 하나만 통과 |
| `EgovSubmitTokenException` | 검증 실패 unchecked 예외. `Reason`(`NO_SESSION`, `NO_PARAMETER`, `TOKEN_NOT_ISSUED`, `TOKEN_MISMATCH`)과 `isDuplicateSubmit()` 제공 |
| `SubmitTokenTag` | `<ui:submitToken [tokenKey]/>` 태그. 토큰을 발급해 `<input type="hidden" name="egovSubmitToken" .../>`를 출력 |

### 예외 처리 (`bind.exception`)

| 클래스 | 설명 |
|---|---|
| `AbstractAnnotationExceptionHandler` | `@ExceptionHandler`가 붙은 추상 메서드 골격(`handleException`, `handleRuntimeException`, `handleBaseException`, `handleEgovBizException`, `handleFdlException`). `@ControllerAdvice` 하위 클래스에서 구현하여 `ModelAndView` 반환 |
| `EgovRestExceptionHandler` | 동작하는 기본 구현. `BindException`(`MethodArgumentNotValidException` 포함) → 400 + `errors`(필드명·메시지, 거부된 값 미포함), `EgovBizException` → 400 + `messageKey`, `TypeMismatchException`·`HttpMessageNotReadableException` → 400 일반화 메시지, `FdlException`/`BaseException`/그 외 → 500 일반화 메시지. 405·415·406·404 등 Spring 표준 예외와 `ResponseStatusException`은 해당 상태 보존(405는 `Allow` 헤더). Spring Security `AccessDeniedException`·`AuthenticationException`은 클래스 이름으로 판별하여 다시 던짐(이 모듈은 Spring Security에 의존하지 않음). `setMessageSource`로 메시지 코드 해석 가능 |

### 폼 검증 (`validation`)

| 클래스 | 설명 |
|---|---|
| `RteFieldChecks` | `org.springmodules.validation.commons.FieldChecks`를 상속. `validator-rules.xml`에서 호출하는 정적 메서드 `validateIhIdNum`(주민등록번호), `validateKorean`, `validateEnglish`, `validateHtmlTag`, `validatePassword1`~`validatePassword4`, `validatePwdCheckComb3`, `validatePwdCheckComb4`, `validatePwdCheckSeries`, `validatePwdCheckRepeat` |
| `RteGenericValidator` | 실제 검증 로직. `isValidIdIhNum`, `isKorean`, `isEnglish`, `isHtmlTag`, `checkLength`(8~20자), `checkCharacterType`, `isMoreThan2CharTypeComb`, `isMoreThan3CharTypeComb`, `isRepeatedNTimes`, `checkSeries`, `isSeriesCharacter`, `isRepeatCharacter` |

---

## 주요 의존성

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.cmmn` | 4.3.1 | compile. `EgovBizException`, `BaseException`, `FdlException` (commons-logging 제외) |
| `org.egovframe.rte:spring-modules-validation` | 0.9 | compile. `FieldChecks`(`RteFieldChecks`의 부모), Valang/Commons Validator 연동 |
| `org.springframework:spring-webmvc` | 5.3.39-egov.1 | compile (commons-logging 제외) |
| `javax.servlet:javax.servlet-api` | 4.0.1 | provided |
| `javax.servlet.jsp:javax.servlet.jsp-api` | 2.3.3 | provided |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `commons-validator:commons-validator` | 1.9.0 | compile. `ValidatorAction`, `Field` (commons-logging 제외) |
| `org.springframework:spring-test`, `junit:junit`, `org.easymock:easymock`, `org.antlr:antlr` | 5.3.39-egov.1 / 4.13.2 / 5.2.0 / 3.5 | test |

버전은 모두 루트 POM `dependencyManagement`에서 관리되며 모듈 POM에는 선언하지 않습니다.

---

## 설정 및 사용 예시

### 페이지네이션 태그

`PaginationManager` 빈을 등록하면 `type` 속성으로 렌더러를 고를 수 있습니다. 빈이 없으면 `DefaultPaginationRenderer`가 사용됩니다.
(`PaginationManager` Javadoc 예시를 4.3.1 패키지명으로 옮긴 것입니다.)

```xml
<bean id="imageRenderer" class="com.example.tag.ImagePaginationRenderer"/>
<bean id="textRenderer" class="com.example.tag.TextPaginationRenderer"/>
<bean id="paginationManager" class="org.egovframe.rte.ptl.mvc.tags.ui.pagination.DefaultPaginationManager">
    <property name="rendererType">
        <map>
            <entry key="image" value-ref="imageRenderer"/>
            <entry key="text" value-ref="textRenderer"/>
        </map>
    </property>
</bean>
```

```jsp
<%@ taglib prefix="ui" uri="http://egovframework.gov/ctl/ui" %>
<ui:pagination paginationInfo="${paginationInfo}" type="image" jsFunction="linkPage"/>
```

### HTML 태그 필터 (web.xml)

```xml
<filter>
    <filter-name>htmlTagFilter</filter-name>
    <filter-class>org.egovframe.rte.ptl.mvc.filter.HTMLTagFilter</filter-class>
    <init-param><param-name>allowedTags</param-name><param-value>p, br</param-value></init-param>
    <init-param><param-name>richTextParameters</param-name><param-value>nttCn</param-value></init-param>
    <init-param><param-name>excludePaths</param-name><param-value>/api/*</param-value></init-param>
</filter>
```

초기화 파라미터 없이 등록하면 모든 파라미터의 `< > & " '`를 인코딩합니다. `richTextParameters`로 우회한 값은 저장·출력 전에 화이트리스트 HTML 정제기로 정제해야 합니다.

### 세션 만료 쿠키 필터 (web.xml)

```xml
<filter>
    <filter-name>sessionExpiryCookieFilter</filter-name>
    <filter-class>org.egovframe.rte.ptl.mvc.session.EgovSessionExpiryCookieFilter</filter-class>
    <init-param>
        <param-name>excludePathPatterns</param-name>
        <param-value>/css/**,/js/**,/images/**</param-value>
    </init-param>
</filter>
```

최초 진입 화면처럼 쿠키가 아직 없는 경우에는 컨트롤러에서 직접 값을 얻습니다.

```java
EgovSessionExpiry.from(request).ifPresent(expiry -> model.addAttribute("sessionExpiry", expiry));
```

### 중복 제출 방지 토큰

```jsp
<form action="/orders" method="post">
    <ui:submitToken/>                        <!-- 기본 키 -->
    <ui:submitToken tokenKey="orderForm"/>    <!-- 폼별 키 -->
</form>
```

```java
// JSP 가 아닌 뷰: 폼 화면을 그릴 때 발급
model.addAttribute("submitToken", EgovSubmitTokens.issue(request));

// 처리 컨트롤러: 실패 시 EgovSubmitTokenException
EgovSubmitTokens.validateOrThrow(request);
orderService.register(vo);
return "redirect:/orders";
```

### 쿠키 빌더

```java
EgovCookies.builder("theme", "dark").maxAge(Duration.ofDays(30)).addTo(request, response);
EgovCookies.builder("name", "홍길동").encodeValue().addTo(request, response);   // 비 ASCII 값은 인코딩 선택
Optional<String> theme = EgovCookies.value(request, "theme");
EgovCookies.expire("theme").addTo(request, response);                        // 같은 Path 로 삭제
```

### REST 예외 핸들러

그대로 빈 등록하지 않고 애플리케이션에서 `@RestControllerAdvice` 하위 클래스로 등록합니다.

```java
@RestControllerAdvice(basePackages = "egovframework.example")
public class ExampleExceptionHandler extends EgovRestExceptionHandler { }
```

화면(JSP) 응답용으로는 `AbstractAnnotationExceptionHandler`를 상속한 `@ControllerAdvice` 클래스에서 각 `handleXxx` 메서드를 구현합니다 (`src/test/java/.../bind/exception/AnnotationExceptionHandler.java` 참고).

### 요청 컨텍스트

```java
EgovRequestContext.getRequestUri().ifPresent(uri -> log.info("요청 {}", uri));
```

`DispatcherServlet`이 처리하는 요청에서는 바로 쓸 수 있고, 필터 단계 등 그 밖의 경로에서 쓰려면 `RequestContextListener` 등록이 필요합니다.

### 테스트 설정 (`src/test/resources/META-INF/spring/`)

`async`, `bind`, `bind/exception`, `handler` 하위의 `test_servlet.xml`과 `mvcTest`, `pagination` 하위의 `test-servlet.xml`이 `<mvc:annotation-driven/>`, `<context:component-scan/>`, `RequestMappingHandlerMapping` 인터셉터 등록 예시를 담고 있습니다. 로그 설정은 `log4j2.xml`입니다.

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Presentation/org.egovframe.rte.ptl.mvc -am
```

결과물: `target/org.egovframe.rte.ptl.mvc-4.3.1.jar`, `-sources.jar`, `-javadoc.jar`

## 테스트

```bash
mvn test -pl Presentation/org.egovframe.rte.ptl.mvc
```

외부 서버 없이 Spring Test의 `MockMvc`, `MockHttpServletRequest`, `MockPageContext`만 사용합니다.
`*SecurityTest`는 헤더 주입(CWE-113), 정보 노출(CWE-209), 토큰 원자성 등 보안 성질을 고정하는 회귀 테스트입니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.ptl.mvc</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>` 생략 가능.
`-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록 필요.
Servlet/JSP API는 `provided`이므로 적용 프로젝트의 WAS 또는 POM에서 제공해야 합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.cmmn](../../Foundation/org.egovframe.rte.fdl.cmmn/README.md), [spring-modules-validation](../spring-modules-validation/README.md), [org.egovframe.rte.ptl.reactive](../org.egovframe.rte.ptl.reactive/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
