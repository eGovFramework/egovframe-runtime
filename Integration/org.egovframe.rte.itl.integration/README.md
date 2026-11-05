# org.egovframe.rte.itl.integration

**연계(Integration) 실행환경 모듈**입니다. 전자정부 표준프레임워크 기반 시스템이 타 시스템과 연계할 때 사용하는 **인터페이스 표준**(연계 컨텍스트·서비스·메시지·타입)과, 기관·시스템·서비스·레코드 타입·연계 정의 메타데이터를 Hibernate로 조회하는 DAO를 제공합니다. 실제 전송 수단(SOAP 웹서비스 등)은 이 모듈에 의존하는 `org.egovframe.rte.itl.webservice`가 구현합니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.itl.integration:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Hibernate 5.6.15.Final
- **실행환경 의존**: `org.egovframe.rte.fdl.logging`

---

## 특징

- **연계 API 표준**: `EgovIntegrationContext`(서비스 조회·모니터 등록), `EgovIntegrationService`(동기/비동기 송신), `EgovIntegrationServiceProvider`(서비스 제공), `EgovIntegrationServiceCallback`/`EgovIntegrationServiceResponse`(비동기 응답)
- **표준 메시지**: 헤더(`EgovIntegrationMessageHeader`, 결과 코드 `ResultCode` 포함)와 바디로 구성된 `EgovIntegrationMessage`. 단순 구현 `SimpleMessage`와 타입 검증 구현 `TypedMessage`
- **타입 시스템**: `PrimitiveType`, `RecordType`, `ListType`으로 메시지 형식을 정의하고 `TypeLoaderUsingMetadata`가 레코드 타입 메타데이터에서 `Type`을 해석. 상속·순환 상속 검사 포함
- **메타데이터·DAO**: `OrganizationDefinition`, `SystemDefinition`, `ServiceDefinition`, `RecordTypeDefinition`(+`RecordTypeFieldDefinition`), `IntegrationDefinition`과 Hibernate(`HibernateDaoSupport`) 기반 `*DefinitionDao` 구현
- **비동기 처리 기반 클래스**: `AbstractService`가 스레드 기반 `sendAsync`와 타임아웃 처리를 구현하므로 하위 클래스는 `doSend`, `createRequestMessage`만 구현
- **모니터링**: `EgovIntegrationServiceMonitor.onEvent(EgovIntegrationServiceEvent)`로 연계 이벤트 수신

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/itl/integration/
├── EgovIntegrationContext.java
├── EgovIntegrationException.java
├── EgovIntegrationMessage.java
├── EgovIntegrationMessageHeader.java        # ResultCode 열거형 포함
├── EgovIntegrationNoSuchServiceException.java
├── EgovIntegrationService.java
├── EgovIntegrationServiceCallback.java
├── EgovIntegrationServiceProvider.java
├── EgovIntegrationServiceResponse.java
├── message/
│   ├── simple/                              # SimpleMessage, SimpleMessageHeader
│   └── typed/                               # TypedMessage, TypedMessageHeader, TypedMap, TypedList, TypedListIterator
├── metadata/                                # IntegrationDefinition, OrganizationDefinition, RecordTypeDefinition,
│   │                                        # RecordTypeFieldDefinition, ServiceDefinition, SystemDefinition
│   └── dao/                                 # *DefinitionDao 인터페이스 5종
│       └── hibernate/                       # Hibernate*DefinitionDao 구현 5종
├── monitor/                                 # EgovIntegrationServiceEvent, EgovIntegrationServiceMonitor
├── support/
│   └── AbstractService.java
├── type/                                    # Type, AbstractType, PrimitiveType, RecordType, ListType, TypeLoader,
│   │                                        # CircularInheritanceException, NoSuchRecordFieldException,
│   │                                        # NoSuchTypeException, UnassignableValueException
│   └── support/                             # PrimitiveTypeFactoryBean, TypeLoaderUsingMetadata
└── util/
    └── Validatable.java

src/main/resources/org/egovframe/rte/itl/integration/metadata/dao/hibernate/
├── IntegrationDefinition.hbm.xml            # INTEGRATION
├── OrganizationDefinition.hbm.xml           # ORGANIZATION
├── RecordTypeDefinition.hbm.xml             # RECORD_TYPE, RECORD_TYPE_FIELD
├── ServiceDefinition.hbm.xml                # SERVICE
└── SystemDefinition.hbm.xml                 # SYSTEM
```

---

## 주요 기능

| 영역 | 클래스 | 설명 |
|------|--------|------|
| 연계 컨텍스트 | `EgovIntegrationContext` | `getService(id)`로 연계 서비스 조회(없으면 `EgovIntegrationNoSuchServiceException`), `getDefaultTimeout()`, `attachMonitor`/`detachMonitor` |
| 연계 서비스 | `EgovIntegrationService` | `createRequestMessage()`, `sendSync(message[, timeout])`, `sendAsync(message)` → `EgovIntegrationServiceResponse.receive([timeout])`, `sendAsync(message, callback)` |
| 연계 서비스 | `AbstractService` | `EgovIntegrationService`의 추상 구현. 내부 스레드로 비동기 송신·타임아웃(`ResultCode.TIME_OUT`) 처리. 하위 클래스는 `doSend(requestMessage)` 구현 |
| 서비스 제공 | `EgovIntegrationServiceProvider` | `service(requestMessage, responseMessage)`. `ServiceDefinition.serviceProviderBeanId`로 지정되는 Spring bean이 구현 |
| 메시지 | `EgovIntegrationMessage`, `EgovIntegrationMessageHeader` | 헤더(연계 ID, 제공/요청 기관·시스템·서비스 ID, 결과 코드 등)와 바디(`Map`) |
| 메시지 | `SimpleMessage`, `SimpleMessageHeader` | 타입 검증 없는 기본 구현 |
| 메시지 | `TypedMessage`, `TypedMessageHeader`, `TypedMap`, `TypedList` | `RecordType`/`ListType`에 맞춰 값 할당을 검증하는 구현(`UnassignableValueException`, `NoSuchRecordFieldException`) |
| 타입 | `PrimitiveType` | boolean, string, byte, short, integer, long, biginteger, float, double, bigdecimal, calendar 등 기본 타입 |
| 타입 | `RecordType`, `ListType` | `<key, value>` 레코드 타입(상속 가능), 단일 타입 리스트 |
| 타입 | `TypeLoader`, `TypeLoaderUsingMetadata` | `getType(typeId)`. `RecordTypeDefinitionDao`에서 정의를 읽어 `Type` 생성, `typeId[]` 표기로 리스트 타입 지원, 순환 상속 시 `CircularInheritanceException` |
| 타입 | `PrimitiveTypeFactoryBean` | bean id(예: `string`, `integer`)에 해당하는 `PrimitiveType`을 Spring 설정에서 참조하도록 하는 FactoryBean |
| 메타데이터 | `IntegrationDefinition` 외 5종 | 연계 정의 모델. 모두 `Validatable.isValid()` 구현 |
| DAO | `HibernateIntegrationDefinitionDao` 외 4종 | `org.springframework.orm.hibernate5.support.HibernateDaoSupport` 확장, `sessionFactory` 주입 |

---

## 주요 의존성

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte.fdl.logging` | 4.3.1 | compile |
| `spring-context-support`, `spring-orm` | 5.3.39-egov.1 | compile |
| `hibernate-entitymanager` | 5.6.15.Final | compile (`hibernate-core` 전이) |
| `javax.annotation-api` | 1.3.2 | compile |
| `spring-test`, `spring-context` | 5.3.39-egov.1 | test |
| `hsqldb` (`jdk8`), `junit`, `dbunit`, `easymock` | 2.7.3 / 4.13.2 / 2.7.3 / 5.2.0 | test |

---

## 설정 및 사용 예시

### Hibernate DAO 설정 (`src/test/resources/.../metadata/dao/hibernate/context.xml`)

```xml
<bean id="hibernateIntegrationDefinitionDao"
      class="org.egovframe.rte.itl.integration.metadata.dao.hibernate.HibernateIntegrationDefinitionDao">
    <property name="sessionFactory" ref="sessionFactory"/>
</bean>
<!-- HibernateOrganizationDefinitionDao, HibernateSystemDefinitionDao,
     HibernateServiceDefinitionDao, HibernateRecordTypeDefinitionDao 도 같은 방식 -->

<jdbc:embedded-database id="myDataSource" type="HSQL"/>

<bean id="sessionFactory" class="org.springframework.orm.hibernate5.LocalSessionFactoryBean">
    <property name="dataSource" ref="myDataSource"/>
    <property name="mappingResources">
        <list>
            <value>/org/egovframe/rte/itl/integration/metadata/dao/hibernate/OrganizationDefinition.hbm.xml</value>
            <value>/org/egovframe/rte/itl/integration/metadata/dao/hibernate/SystemDefinition.hbm.xml</value>
            <value>/org/egovframe/rte/itl/integration/metadata/dao/hibernate/ServiceDefinition.hbm.xml</value>
            <value>/org/egovframe/rte/itl/integration/metadata/dao/hibernate/RecordTypeDefinition.hbm.xml</value>
            <value>/org/egovframe/rte/itl/integration/metadata/dao/hibernate/IntegrationDefinition.hbm.xml</value>
        </list>
    </property>
    <property name="hibernateProperties">
        <value>
            hibernate.dialect=org.hibernate.dialect.HSQLDialect
            hibernate.show_sql=false
            hibernate.hbm2ddl.auto=create-drop
        </value>
    </property>
</bean>

<bean id="transactionManager" class="org.springframework.orm.hibernate5.HibernateTransactionManager">
    <property name="sessionFactory" ref="sessionFactory"/>
</bean>
```

### PrimitiveType bean 등록 (`src/test/resources/.../type/support/context.xml`)

```xml
<bean id="string"  class="org.egovframe.rte.itl.integration.type.support.PrimitiveTypeFactoryBean"/>
<bean id="integer" class="org.egovframe.rte.itl.integration.type.support.PrimitiveTypeFactoryBean"/>
<bean id="calendar" class="org.egovframe.rte.itl.integration.type.support.PrimitiveTypeFactoryBean"/>
```

### 연계 서비스 구현과 호출 (`AbstractServiceTest`)

```java
public class EchoService extends AbstractService {
    public EchoService(String id, long defaultTimeout) {
        super(id, defaultTimeout);
    }
    protected EgovIntegrationMessage doSend(EgovIntegrationMessage requestMessage) {
        return requestMessage;   // 실제 전송 로직
    }
    public EgovIntegrationMessage createRequestMessage() {
        return new SimpleMessage(new SimpleMessageHeader());
    }
}

EgovIntegrationService service = new EchoService("testService", 5000);
EgovIntegrationMessage request = service.createRequestMessage();

// 동기 호출
EgovIntegrationMessage response = service.sendSync(request);
// 비동기 호출
EgovIntegrationServiceResponse async = service.sendAsync(request);
EgovIntegrationMessage asyncResponse = async.receive(3000);
if (asyncResponse.getHeader().getResultCode() == ResultCode.TIME_OUT) { /* 타임아웃 처리 */ }
```

### 레코드 타입 메타데이터 (`TypeLoaderUsingMetadataTest`)

```java
// RecordTypeDefinition(id, name, parent, fields)
RecordTypeDefinition header = new RecordTypeDefinition("header", "header", null, fields);  // fields: Map<String, RecordTypeFieldDefinition>
fields.put("serviceId", new RecordTypeFieldDefinition("string"));
fields.put("children", new RecordTypeFieldDefinition("recordJuminCheck[]"));   // 리스트 타입

TypeLoaderUsingMetadata typeLoader = new TypeLoaderUsingMetadata();
typeLoader.setRecordTypeDefinitionDao(recordTypeDefinitionDao);
typeLoader.afterPropertiesSet();
Type type = typeLoader.getType("messageJuminCheck");
```

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Integration/org.egovframe.rte.itl.integration -am
```

## 테스트

```bash
mvn test -pl Integration/org.egovframe.rte.itl.integration
```

외부 서버는 필요 없습니다. DAO 테스트는 `<jdbc:embedded-database type="HSQL">` 인메모리 DB와 DBUnit 데이터셋(`dataset.xml`)을 사용합니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.itl.integration</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>` 생략 가능. `-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록 필요.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.itl.webservice](../org.egovframe.rte.itl.webservice/README.md) (이 모듈에 의존하는 SOAP 웹서비스 구현), [org.egovframe.rte.fdl.logging](../../Foundation/org.egovframe.rte.fdl.logging/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
