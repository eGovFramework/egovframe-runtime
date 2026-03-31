# egovframe-rte-itl-integration

**시스템 간 연계(Integration)** 메타데이터·메시지·타입 로더를 **Hibernate ORM**과 결합해 제공합니다. 기관·서비스·레코드 타입 정의를 DB에서 로딩하고, 형식화된 메시지(`TypedMessage` 등)와 통합 컨텍스트로 연계 서비스를 표현합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-itl-integration:5.0.0`
- **Runtime**: Java 17 / Spring Framework 6.x / Hibernate ORM
- **의존**: `egovframe-rte-fdl-logging`

---

## 특징

- **메타데이터 중심**: `IntegrationDefinition`, `SystemDefinition`, `ServiceDefinition`, `RecordTypeDefinition` 등
- **DAO 계층**: Hibernate 기반 `*DefinitionDao` 구현체
- **메시지 모델**: `SimpleMessage`, `TypedMessage`, 헤더·리스트 컨테이너
- **타입 시스템**: `PrimitiveType`, `RecordType`, `TypeLoader`, FactoryBean
- **모니터링**: `EgovIntegrationServiceMonitor`, 이벤트 객체

---

## 소스 디렉터리 구조 (요약)

```
org/egovframe/rte/itl/integration/
├── EgovIntegrationContext.java
├── EgovIntegrationService.java
├── EgovIntegrationServiceProvider.java
├── EgovIntegrationServiceCallback.java
├── EgovIntegrationMessage*.java
├── EgovIntegrationException.java
├── message/
│   ├── simple/
│   └── typed/
├── metadata/              # 연계 정의 엔티티
│   └── dao/
│       └── hibernate/
├── monitor/
├── support/
│   └── AbstractService.java
├── type/                  # Record/Primitive 타입, 로더
│   └── support/
└── util/
    └── Validatable.java
```

---

## 주요 기능

| 영역 | 설명 |
|------|------|
| **연계 API** | `EgovIntegrationService`, 응답·콜백·프로바이더 |
| **메시지** | 헤더+바디 구조, Typed 컬렉션 |
| **Hibernate DAO** | 정의 엔티티 CRUD |
| **타입 로딩** | 메타데이터 기반 필드 타입 해석 |

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
  <artifactId>egovframe-rte-itl-integration</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 웹서비스(SOAP) 모듈: `egovframe-rte-itl-webservice` (본 모듈에 의존)
- 상위 개요: 저장소 루트 `README.md`
