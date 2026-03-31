# egovframe-rte-fdl-xml

**DOM/SAX** 기반 XML 파싱·검증·팩토리 서비스를 제공합니다. Spring `spring-context-support`와 연동해 **XML ↔ 자바 객체** 처리 시나리오를 지원합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-fdl-xml:5.0.0`
- **Runtime**: Java 17 / Spring Framework 6.x / Jakarta EE API (provided)
- **의존**: `egovframe-rte-fdl-logging`

---

## 특징

- **검증/파싱 분리**: DOM·SAX 각각 Validator + Factory 구현
- **공유 리소스**: `SharedObject`, `XmlConfig`로 파서 설정 재사용
- **레거시 연계**: 전자문서·연계 메시지 XML 처리에 활용

---

## 소스 디렉터리 구조

```
org/egovframe/rte/fdl/xml/
├── AbstractXMLUtility.java
├── EgovAbstractXMLFactoryService.java
├── EgovDOMValidatorService.java
├── EgovSAXValidatorService.java
├── SharedObject.java
├── XmlConfig.java
├── error/
│   └── ErrorChecker.java
├── exception/
│   ├── UnsupportedException.java
│   └── ValidatorException.java
└── impl/
    ├── EgovDOMFactoryServiceImpl.java
    ├── EgovSAXFactoryServiceImpl.java
    └── ContentHandlerImpl.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovDOMValidatorService` / `EgovDOMFactoryServiceImpl` | DOM 트리 검증·파싱 |
| `EgovSAXValidatorService` / `EgovSAXFactoryServiceImpl` | SAX 스트림 검증·파싱 |
| `AbstractXMLUtility` | 공통 XML 유틸 베이스 |

---

## 빌드

```bash
# 이 모듈 디렉터리에서 실행
mvn clean package
```

## 테스트

```bash
# 단위/통합 테스트 실행
mvn test
```

## 최소 설정 예시

```xml
<dependency>
  <groupId>org.egovframe.rte</groupId>
  <artifactId>egovframe-rte-fdl-xml</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 상위 개요: 저장소 루트 `README.md`
