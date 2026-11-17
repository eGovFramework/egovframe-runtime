# org.egovframe.rte.fdl.string

**Foundation Layer 문자열(String) 유틸리티 모듈**입니다. 문자열·날짜·숫자·객체 처리에 필요한 정적 유틸리티 클래스와, 4.3.1에서 추가된 **출력 컨텍스트별 XSS 인코더**(`EgovOutputEncoder`)를 제공합니다. 도메인에 의존하지 않는 순수 유틸 성격이며, `fdl.filehandling`, `fdl.security` 모듈이 이 모듈에 의존합니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.fdl.string:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Commons Codec 1.18.0
- **실행환경 의존**: `org.egovframe.rte.fdl.logging`

---

## 특징

- **문자열 처리**: null 처리, 패딩/정렬, 토큰 분리, 패턴 매칭, camelCase/underscore 변환, 한/영 변환, 숫자 타입 변환, Base64 인코딩·비밀번호 해시 등 60여 개 정적 메서드(`EgovStringUtil`).
- **날짜 계산**: `yyyyMMdd` 문자열 기반의 날짜 더하기/빼기, 요일, 일수·개월수 차이, 만 나이, 현재 시각 조회(`EgovDateUtil`).
- **정밀 숫자 연산**: 문자열 숫자를 `BigDecimal`로 사칙연산하고 `RoundingMode`로 반올림 방식을 지정(`EgovNumericUtil`).
- **안전한 리플렉션**: 클래스 로딩·인스턴스화 시 허용 클래스 목록(`setAllowedClassNames`)으로 Unsafe Reflection(CWE-470)을 제한(`EgovObjectUtil`, 4.3.1 추가).
- **컨텍스트별 출력 인코딩(4.3.1 추가)**: HTML 본문·속성·JavaScript 문자열·URL 파라미터·XML 각각에 맞는 인코딩 메서드를 제공하여 XSS를 출력 시점에 방어(`EgovOutputEncoder`).

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/fdl/string/
├── EgovStringUtil.java      # 문자열 유틸
├── EgovDateUtil.java        # 날짜 유틸
├── EgovNumericUtil.java     # BigDecimal 기반 숫자 연산 유틸
├── EgovObjectUtil.java      # 클래스 로딩/인스턴스화, null·empty 검사
└── EgovOutputEncoder.java   # 컨텍스트별 XSS 출력 인코더
```

`src/main/resources`는 없습니다.

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovStringUtil` | `isEmpty`/`isNotEmpty`/`isEmptyTrimmed`/`isNull`, `null2void`/`null2string`, `trim`/`ltrim`/`rtrim`, `lPad`/`rPad`/`fillString`, `alignLeft`/`alignCenter`/`alignRight`, `getTokens`/`getStringArray`, `search`/`countOf`/`replace`/`reverse`/`concat`, `capitalize`/`swapFirstLetterCase`, `convertToCamelCase`/`convertToUnderScore`, `isAlpha`/`isAlphaNumeric`/`isNumeric`/`isPatternMatch`/`isPatternMatching`/`containsInvalidChars`/`containsMaxSequence`, `string2integer`/`string2long`/`string2float`/`string2double` 및 역변환, `toEng`/`toKor`, `toSubString`/`getLastString`, `encodeString`/`decodeString`(Base64, Commons Codec), `encodePassword(password, algorithm)`(MessageDigest + Base64) |
| `EgovDateUtil` | `getCalcDateAsString`/`getCalcYearAsString`/`getCalcMonthAsString`/`getCalcDayAsString`(및 `AsInt`), `getCurrentDateAsString`/`getCurrentDateTimeAsString`/`getCurrentTimeAsString` 등 현재 시각, `getDayOfWeekAsString`, `getDayCount`/`getDayCountWithFormatter`/`getDayCountForMonth`/`getDayOfWeekCount`/`getTimeCount`, `getFullAge`/`getCurrentFullAge`, `dateFormatCheck`, `toString(date, format, locale)` |
| `EgovNumericUtil` | `isNumber`, `plus`/`minus`/`multiply`/`divide`(scale, `RoundingMode` 오버로드), `setScale`. 상수 `ROUND_UP`, `ROUND_DOWN`, `ROUND_HALF_UP`, `ROUND_UNNECESSARY` |
| `EgovObjectUtil` | `loadClass(className)`, `instantiate(className)`, `instantiate(className, types, values)`, `isNull`, `isEmpty`. `setAllowedClassNames(Set<String>)`로 허용 목록을 지정하면 목록 밖 클래스는 로딩하지 않음(기본 null = 제한 없음) |
| `EgovOutputEncoder` | `forHtml`(본문·따옴표 속성), `forHtmlAttribute`(따옴표 없는 속성), `forJavaScript`(따옴표 문자열 리터럴), `forUrl`(쿼리 파라미터), `forXml`. 모든 메서드가 null을 빈 문자열로 반환하고 예외를 던지지 않음. `href`/`src`에 값 전체를 넣는 경우의 `javascript:` 스킴 검사, CSS 컨텍스트, JSON 직렬화는 다루지 않음 |

---

## 주요 의존성

| 라이브러리 | 버전 | scope / 비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.logging` | 4.3.1 | compile. Log4j 2 / SLF4J 전이 |
| `org.springframework:spring-context` | 5.3.39-egov.1 | compile (commons-logging 제외). `@Nullable`, `ClassUtils` 등 |
| `commons-codec:commons-codec` | 1.18.0 | compile. Base64 |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `spring-test` / `junit` / `easymock` | 5.3.39-egov.1 / 4.13.2 / 5.2.0 | test |

버전은 모두 루트 `pom.xml`의 `dependencyManagement`에서 관리하며 모듈 POM에는 선언하지 않습니다.

---

## 설정 및 사용 예시

모든 클래스가 정적 메서드만 제공하므로 Spring 빈 설정이 필요 없습니다.

### 문자열·날짜·숫자

```java
EgovStringUtil.isEmpty("");                               // true
EgovStringUtil.lPad("12", 5, '0');                         // "00012"
EgovStringUtil.convertToCamelCase("user_name");           // "userName"
EgovStringUtil.encodePassword("pass", "SHA-256");          // Base64 해시

EgovDateUtil.getCalcDateAsString("2024", "01", "31", 1, "month"); // 한 달 뒤
EgovDateUtil.getDayCount("20240101", "20240131");          // 30

EgovNumericUtil.divide("10", "3", 2, EgovNumericUtil.ROUND_HALF_UP); // "3.33"
```

### 출력 인코딩 (EgovOutputEncoder Javadoc)

```jsp
<td><%= EgovOutputEncoder.forHtml(v) %></td>                 HTML 본문
<input value="<%= EgovOutputEncoder.forHtml(v) %>">           따옴표로 감싼 속성
<div class=<%= EgovOutputEncoder.forHtmlAttribute(v) %>>      따옴표 없는 속성
<script>var s = '<%= EgovOutputEncoder.forJavaScript(v) %>';</script>
<a href="/view?id=<%= EgovOutputEncoder.forUrl(v) %>">
```

인코딩은 저장 시점이 아니라 **출력 시점에 한 번만** 적용합니다. JSP `<c:out>`(기본 `escapeXml="true"`)과 함께 쓰면 이중 인코딩이 되므로 주의합니다.

### 리플렉션 허용 목록 (EgovObjectUtilSecurityTest)

```java
EgovObjectUtil.setAllowedClassNames(new HashSet<>(Arrays.asList("com.example.SafeBean")));
EgovObjectUtil.instantiate("com.example.SafeBean");   // 허용
EgovObjectUtil.instantiate("java.lang.Runtime");      // 허용 목록에 없으면 로딩하지 않음
```

### 테스트

| 테스트 | 내용 |
|---|---|
| `EgovStringUtilTest`, `EgovDateUtilTest`, `EgovNumericUtilTest`, `EgovObjectUtilTest` | 각 유틸 메서드 동작 |
| `EgovObjectUtilSecurityTest` | 허용 목록 유무에 따른 클래스 로딩 제한 |
| `EgovOutputEncoderSecurityTest` | BMP 전수 문자 왕복, HTML/속성/스크립트/URL/XML 컨텍스트별 인코딩 |

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Foundation/org.egovframe.rte.fdl.string -am
```

## 테스트

```bash
mvn test -pl Foundation/org.egovframe.rte.fdl.string
```

외부 서버는 필요하지 않습니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.fdl.string</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>`을 생략할 수 있습니다. `-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록이 필요합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.logging](../org.egovframe.rte.fdl.logging/README.md), [org.egovframe.rte.fdl.filehandling](../org.egovframe.rte.fdl.filehandling/README.md), [org.egovframe.rte.fdl.security](../org.egovframe.rte.fdl.security/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
