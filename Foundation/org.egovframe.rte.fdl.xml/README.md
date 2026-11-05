# org.egovframe.rte.fdl.xml

**Foundation Layer XML 모듈**입니다. JAXP **DOM/SAX** 파서로 XML 문서의 well-formed 검사와 XML Schema 검증을 수행하고, XPath 조회와 엘리먼트 추가·수정·삭제·신규 XML 생성 등 조작 기능을 `AbstractXMLUtility` 한 곳에 모아 제공합니다. DOM/SAX 검증 서비스는 팩토리(`EgovDOMFactoryServiceImpl`, `EgovSAXFactoryServiceImpl`)를 통해 Spring 빈으로 주입받아 사용합니다. 다른 실행환경 모듈이 이 모듈에 의존하지는 않습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.fdl.xml:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 (`spring-context-support`) / JAXP (JDK 내장)
- **실행환경 의존**: `org.egovframe.rte.fdl.logging`

---

## 특징

- **DOM·SAX 검증**: `parse(boolean isValid)`로 well-formed 검사(`false`) 또는 `setSCHEMAFile()`로 지정한 XSD 기반 검증(`true`)을 수행하고, 오류는 `ErrorChecker`가 모아 `ValidatorException` 메시지로 보고합니다.
- **XXE 방어**: DOM(`DocumentBuilderFactory`)과 SAX 파서 모두 `FEATURE_SECURE_PROCESSING`을 켜고 `disallow-doctype-decl`을 활성화하며 `external-general-entities`, `external-parameter-entities`, `load-external-dtd`를 비활성화합니다. 저장용 `TransformerFactory`도 `ACCESS_EXTERNAL_DTD`/`ACCESS_EXTERNAL_STYLESHEET`를 비우고 보안 처리 기능을 켭니다. `XxeProtectionTest`가 DOM/SAX 모두에서 DOCTYPE 선언을 거부하는지 검증합니다.
- **XML 조작**: XPath 결과 조회(`getResult`), 엘리먼트/텍스트 엘리먼트 추가(`addElement`, `addTextElement`), 이름·텍스트 변경(`updElement`, `updTextElement`), 삭제(`delElement`), 새 XML 파일 생성(`createNewXML`)을 제공하며 결과는 `XmlConfig.xmlpath` 등 지정 경로에 저장합니다.
- **입력 형태 선택**: 파일 경로(`setXMLFile`) 또는 문자열(`setXML`) 중 하나로 파싱 대상을 지정합니다.
- **팩토리 패턴**: `EgovAbstractXMLFactoryService`의 `createDOMValidator()`/`createSAXValidator()`로 검증 서비스를 생성합니다. 구 명명 규칙의 `abstractXMLFactoryService`/`EgovConcreteDOMFactory`/`EgovConcreteSAXFactory`도 호환용으로 남아 있습니다.

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/fdl/xml/
├── AbstractXMLUtility.java             # 파싱 공통 추상 클래스 (입력 지정, XPath, 엘리먼트 조작, 저장)
├── EgovDOMValidatorService.java        # DOM 파서 기반 검증 (ErrorHandler 구현)
├── EgovSAXValidatorService.java        # SAX 파서 기반 검증 (XXE 방어 기능 설정)
├── EgovAbstractXMLFactoryService.java  # 팩토리 추상 클래스 (createDOMValidator/createSAXValidator)
├── abstractXMLFactoryService.java      # 구 명명 규칙 팩토리 추상 클래스 (CreateDOMValidator/CreateSAXValidator)
├── EgovConcreteDOMFactory.java         # 구 명명 규칙 DOM 팩토리
├── EgovConcreteSAXFactory.java         # 구 명명 규칙 SAX 팩토리
├── SharedObject.java                   # key/value 직렬화 구조체 (엘리먼트 추가·수정 시 사용)
├── XmlConfig.java                      # XML 저장 경로(xmlpath) 설정 빈
├── error/
│   └── ErrorChecker.java               # DefaultHandler 확장, 오류 메시지를 Set에 수집
├── exception/
│   ├── UnsupportedException.java       # 지원하지 않는 팩토리 메서드 호출 시
│   └── ValidatorException.java         # 검증 실패 시
└── impl/
    ├── EgovDOMFactoryServiceImpl.java  # DOM 팩토리 구현
    ├── EgovSAXFactoryServiceImpl.java  # SAX 팩토리 구현
    └── ContentHandlerImpl.java         # SAX ContentHandler 기본 구현
```

`src/main/resources`는 없습니다.

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `AbstractXMLUtility` | `setXMLFile`/`getXMLFile`, `setSCHEMAFile`/`getSCHEMAFile`, `setXML`/`getXML`(문자열 입력), 추상 메서드 `parse(boolean isValid)`, `getResult(Document, xpath)`, `addElement`, `addTextElement`, `addNode`, `addTextNode`, `updElement`, `updTextElement`, `updTextNode`, `chgNode`, `removeNode`, `delElement`, `createNewXML`, `createXML`. 저장 시 `TransformerFactory`에 보안 속성 적용 |
| `EgovDOMValidatorService` | `DocumentBuilder`로 파싱. XXE 방어 기능을 적용한 `DocumentBuilderFactory`를 사용하며, `ErrorHandler`(`warning`/`error`/`fatalError`) 구현으로 오류를 수집해 `ValidatorException` 발생 |
| `EgovSAXValidatorService` | `SAXParser`로 파싱. XXE 방어 기능을 적용하고, `isValid=true`면 `validation`, `validation/schema`, `schema-full-checking` 기능을 켜고 스키마 검증 |
| `EgovAbstractXMLFactoryService` | `createDOMValidator()`, `createSAXValidator()` 추상 팩토리 |
| `EgovDOMFactoryServiceImpl` / `EgovSAXFactoryServiceImpl` | 각각 DOM/SAX 검증 서비스를 생성. 지원하지 않는 쪽 메서드는 `UnsupportedException` |
| `ErrorChecker` | `DefaultHandler` 확장. `getErrorReport()`로 수집한 오류 메시지 `Set` 반환 |
| `SharedObject` | `getKey()`/`getValue()`. 엘리먼트 이름과 텍스트 값을 묶어 `List<SharedObject>`로 전달 |
| `XmlConfig` | `xmlpath` 프로퍼티. 생성·수정한 XML을 저장할 디렉터리 |

---

## 주요 의존성

| 라이브러리 | 버전 | scope / 비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.logging` | 4.3.1 | compile. Log4j 2 / SLF4J 전이 |
| `org.springframework:spring-context-support` | 5.3.39-egov.1 | compile (commons-logging 제외) |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `jaxen:jaxen` | 2.0.0 | test. XPath 예제 |
| `net.sf.ehcache:ehcache` | 2.10.9.2 | test (slf4j-api 제외). `ehcache` 패키지 캐시 연동 예제 |
| `spring-test` / `junit` | 5.3.39-egov.1 / 4.13.2 | test |

버전은 모두 루트 `pom.xml`의 `dependencyManagement`에서 관리하며 모듈 POM에는 선언하지 않습니다. XML 파싱 자체는 JDK 내장 JAXP(DOM/SAX/XPath/Transformer)를 사용합니다.

---

## 설정 및 사용 예시

### 팩토리 빈 등록 (src/test/resources/spring/context-xmltest.xml)

```xml
<bean id="domconcreteCont" class="org.egovframe.rte.fdl.xml.impl.EgovDOMFactoryServiceImpl"/>
<bean id="saxconcreteCont" class="org.egovframe.rte.fdl.xml.impl.EgovSAXFactoryServiceImpl"/>
```

### 저장 경로 설정 (src/test/resources/spring/egovxmlCfg.xml, egovxml.properties)

```xml
<context:property-placeholder location="classpath*:spring/egovxml.properties"/>

<bean id="xmlconfig" class="org.egovframe.rte.fdl.xml.XmlConfig">
    <property name="xmlpath" value="${egovxmlsaved.path}"/>
</bean>
```

```properties
egovxmlsaved.path=/Users/EGOV2024/temp/
```

### 검증 및 조작 (ControlXMLTest, XxeProtectionTest)

```java
@Resource(name = "domconcreteCont")
EgovDOMFactoryServiceImpl domconcrete;
@Resource(name = "saxconcreteCont")
EgovSAXFactoryServiceImpl saxconcrete;

// 1) SAX 파서로 well-formed 검사 (파일 입력)
EgovSAXValidatorService saxValidator = saxconcrete.createSAXValidator();
saxValidator.setXMLFile(fileName);          // src/test/resources/spring/context-test.xml
boolean wellFormed = saxValidator.parse(false);

// 2) 스키마 검증
saxValidator.setSCHEMAFile(schemaFileName);
boolean valid = saxValidator.parse(true);   // 실패 시 ValidatorException

// 3) 문자열 입력 (DOM)
EgovDOMValidatorService dom = new EgovDOMValidatorService();
dom.setXML("<?xml version=\"1.0\"?><foo>bar</foo>");
dom.parse(false);                           // true. DOCTYPE 포함 문서는 거부

// 4) XPath 조회와 엘리먼트 조작
List<Node> nodes = dom.getResult(doc, "/information/person");
List<SharedObject> list = Arrays.asList(new SharedObject("name", "Hong"), new SharedObject("age", "30"));
dom.addTextElement(doc, "person", list, savePath);   // 텍스트 엘리먼트 추가 후 savePath에 저장
dom.updElement(doc, "person1", "person", savePath);  // 엘리먼트 이름 변경
dom.delElement(doc, "person1", savePath);            // 엘리먼트 삭제
dom.createNewXML(doc, "information", list, savePath);// 새 XML 문서 생성
```

### 테스트

| 테스트 | 내용 |
|---|---|
| `ControlXMLTest` | SAX 서비스로 `context-test.xml`의 well-formed 검사(`parse(isValid)`), XPath 조회, 엘리먼트 추가/수정/삭제/신규 XML 생성 |
| `XxeProtectionTest` | DOM/SAX 모두 DOCTYPE 외부 엔티티 문서를 거부하고 일반 문서는 파싱 |
| `ehcache/CacheXMLTest` | XPath(`//*[@*]`) 조회 결과를 `SharedObject`로 직렬화하여 소켓(`localhost:64208`)으로 Ehcache 2.x 기반 `CacheServer`에 보내는 예제. 서버는 테스트가 기동하지 않으며, 접속 실패는 로그로만 남기고 통과 |

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Foundation/org.egovframe.rte.fdl.xml -am
```

## 테스트

```bash
mvn test -pl Foundation/org.egovframe.rte.fdl.xml
```

외부 서버는 필요하지 않습니다. 생성·수정 결과 XML은 테스트 코드가 지정하는 경로에 파일로 저장되므로 쓰기 가능한 경로가 필요합니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.fdl.xml</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>`을 생략할 수 있습니다. `-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록이 필요합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.logging](../org.egovframe.rte.fdl.logging/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
