# egovframe-rte-itl-webservice

**Apache CXF** 기반 **JAX-WS(SOAP)** 웹서비스 클라이언트·서버 브리지를 제공합니다. 연계 정의(`WebServiceClientDefinition`, `WebServiceServerDefinition`)를 Hibernate DAO로 관리하고, `itl.integration` 메시지와 변환기로 연결합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-itl-webservice:5.0.0`
- **Runtime**: Java 17 / Spring 6.x / Jakarta XML WS / CXF 4.x
- **의존**: `egovframe-rte-itl-integration`

---

## 특징

- **SOAP 엔드포인트**: `EgovWebServiceServlet`, `EgovWebServiceContext`
- **클라이언트**: `EgovWebServiceClient`, 동적 클래스 로더 FactoryBean
- **브리지**: `ServiceBridge` — 연계 서비스와 웹서비스 호출 매핑
- **메시지 변환**: `MessageConverter` 및 구현체
- **데이터 계층**: `HibernateWebService*DefinitionDao`

---

## 소스 디렉터리 구조 (요약)

```
org/egovframe/rte/itl/webservice/
├── EgovWebService.java
├── EgovWebServiceContext.java
├── EgovWebServiceServlet.java
├── EgovWebServiceMessage*.java
├── data/
│   ├── WebServiceClientDefinition.java
│   ├── WebServiceServerDefinition.java
│   ├── MappingInfo.java
│   └── dao/ (+ hibernate/)
└── service/
    ├── EgovWebServiceClient.java
    ├── EgovWebServiceClassLoader.java
    ├── ServiceBridge.java
    ├── MessageConverter.java
    ├── ServiceEndpointInfo*.java
    └── impl/
```

---

## 주요 기능

| 구성요소 | 설명 |
|----------|------|
| `EgovWebServiceClientImpl` | 원격 SOAP 호출 |
| `ServiceBridgeImpl` | 내부 연계와 WS 간 어댑터 |
| `MessageConverterImpl` | 페이로드 ↔ `EgovIntegrationMessage` 변환 |
| `EgovWebServiceClassLoaderFactoryBean` | WSDL/스텁 클래스 로딩 |

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
  <artifactId>egovframe-rte-itl-webservice</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 연계 코어: `egovframe-rte-itl-integration`
- 상위 개요: 저장소 루트 `README.md`
