# org.egovframe.rte.itl.webservice

**연계(Integration) 웹서비스 모듈**입니다. `org.egovframe.rte.itl.integration`의 연계 표준(컨텍스트·서비스·메시지·타입)을 **Apache CXF 기반 JAX-WS(SOAP) 웹서비스**로 구현합니다. 연계 정의 메타데이터와 웹서비스 클라이언트/서버 정의를 Hibernate DAO로 읽어 서비스 엔드포인트 클래스를 런타임에 생성하고, SOAP 호출과 `EgovIntegrationMessage` 간 변환을 수행합니다. 실행환경의 다른 모듈은 이 모듈에 의존하지 않습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.itl.webservice:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Apache CXF 3.5.11 / Jetty 9.4.58.v20250814 / JAX-WS API (`javax.xml.ws`) 2.3.1
- **실행환경 의존**: `org.egovframe.rte.itl.integration` (→ `org.egovframe.rte.fdl.logging`)

---

## 특징

- **웹서비스 컨텍스트**: `EgovWebServiceContext`가 `EgovIntegrationContext`를 구현. 기관·시스템 ID 기준으로 연계 정의를 읽어 제공 서비스(서버)와 요청 서비스(클라이언트)를 초기화
- **SOAP 서버 공개**: `EgovWebServiceServlet`(CXF `CXFNonSpringServlet` 확장)이 Spring 컨텍스트의 `egovWebServiceContext` bean을 찾아 `javax.xml.ws.Endpoint.publish`로 엔드포인트 공개
- **클라이언트 호출**: `EgovWebService`(`AbstractService` 확장)가 `EgovWebServiceClient`를 통해 원격 SOAP 서비스를 호출. 동기/비동기·타임아웃 처리는 `AbstractService` 상속
- **동적 클래스 생성**: `EgovWebServiceClassLoaderImpl`이 `Type`, `ServiceEndpointInterfaceInfo`, `ServiceEndpointInfo`에 해당하는 클래스를 `org.egovframe.rte.itl.webservice.generated` 패키지 아래에 런타임 생성
- **메시지 변환·브리지**: `MessageConverterImpl`이 Map/List 메시지와 VO 간 변환, `ServiceBridgeImpl`이 생성된 ServiceEndpoint와 `EgovIntegrationServiceProvider`를 연결
- **설정 메타데이터**: `WebServiceClientDefinition`, `WebServiceServerDefinition`, `MappingInfo`와 Hibernate DAO

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/itl/webservice/
├── EgovWebService.java                      # AbstractService 확장, 클라이언트 측 연계 서비스
├── EgovWebServiceContext.java               # EgovIntegrationContext 구현
├── EgovWebServiceMessage.java               # SimpleMessage 확장
├── EgovWebServiceMessageHeader.java         # EgovIntegrationMessageHeader 구현
├── EgovWebServiceServlet.java               # CXFNonSpringServlet 확장
├── data/
│   ├── MappingInfo.java
│   ├── WebServiceClientDefinition.java
│   ├── WebServiceServerDefinition.java
│   └── dao/                                 # WebServiceClientDefinitionDao, WebServiceServerDefinitionDao
│       └── hibernate/                       # HibernateWebServiceClientDefinitionDao, HibernateWebServiceServerDefinitionDao
└── service/
    ├── EgovWebServiceClassLoader.java
    ├── EgovWebServiceClient.java
    ├── MessageConverter.java
    ├── ServiceBridge.java
    ├── ServiceEndpointInfo.java
    ├── ServiceEndpointInterfaceInfo.java
    ├── ServiceParamInfo.java
    └── impl/                                # EgovWebServiceClassLoaderFactoryBean, EgovWebServiceClassLoaderImpl,
                                             # EgovWebServiceClientImpl, MessageConverterImpl, ServiceBridgeImpl,
                                             # ServiceEndpointInfoImpl, ServiceEndpointInterfaceInfoImpl, ServiceParamInfoImpl

src/main/resources/org/egovframe/rte/itl/webservice/
├── context/context-webservice.xml           # CXF 설정 import + DAO/sessionFactory/typeLoader/classLoader bean
└── data/dao/hibernate/
    ├── WebServiceClientDefinition.hbm.xml   # WEB_SERVICE_CLIENT, WEB_SERVICE_MAPPING_INFO
    └── WebServiceServerDefinition.hbm.xml   # WEB_SERVICE_SERVER
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovWebServiceContext` | `organizationId`, `systemId`, `defaultTimeout`, `integrationDefinitionDao`, `webServiceClientDefinitionDao`, `webServiceServerDefinitionDao`, `typeLoader`, `classLoader` 속성을 받아 `init()`에서 서버 정보와 클라이언트를 생성. `getService(id)`, `publishServer(Bus)`, `attachMonitor`/`detachMonitor` 제공 |
| `EgovWebServiceServlet` | `loadBus()`에서 `WebApplicationContextUtils`로 `egovWebServiceContext` bean을 얻어 `publishServer(getBus())` 호출 |
| `EgovWebService` | `EgovWebService(id, defaultTimeout, integrationDefinition, client)`. `createRequestMessage()`는 연계 정의의 기관·시스템·서비스 ID가 채워진 헤더를 가진 메시지를 반환 |
| `EgovWebServiceClientImpl` | `EgovWebServiceClient` 구현. `service(requestMessage)`로 원격 SOAP 호출 |
| `EgovWebServiceClassLoaderImpl`, `EgovWebServiceClassLoaderFactoryBean` | 타입·엔드포인트 인터페이스·엔드포인트 구현 클래스를 런타임 생성하는 클래스로더와 FactoryBean |
| `MessageConverterImpl` | `EgovIntegrationMessage`(Map/List) ↔ 생성된 VO 변환 |
| `ServiceBridgeImpl` | `ServiceBridgeImpl(provider, serviceEndpointInfo, messageConverter)`. `doService(params)`로 생성된 엔드포인트의 호출을 `EgovIntegrationServiceProvider.service(...)`에 위임 |
| `ServiceEndpointInfo`, `ServiceEndpointInterfaceInfo`, `ServiceParamInfo` | 엔드포인트·인터페이스·파라미터 매핑 정보 (impl 패키지에 구현체) |
| `WebServiceClientDefinition` | 서비스 정의, 네임스페이스, WSDL 주소, 서비스명, 포트명, 오퍼레이션명, 요청/응답 `MappingInfo` |
| `WebServiceServerDefinition` | 서비스 정의, 네임스페이스, 공개 주소, 서비스명, 포트명, 오퍼레이션명 |
| `HibernateWebServiceClientDefinitionDao`, `HibernateWebServiceServerDefinitionDao` | `HibernateDaoSupport` 확장, `sessionFactory` 주입 |

---

## 주요 의존성

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte.itl.integration` | 4.3.1 | compile (`fdl.logging`, `hibernate-entitymanager` 5.6.15.Final 전이) |
| `cxf-rt-frontend-jaxws`, `cxf-rt-transports-http-jetty` | 3.5.11 | compile (Jetty 9.4.58.v20250814, Neethi 3.2.2는 루트 POM에서 관리) |
| `jaxb-runtime`, `txw2` / `istack-commons-runtime` | 2.3.5 / 3.0.12 | `cxf-core` 전이 의존성. 이 모듈 POM의 `dependencyManagement`에서 버전 정렬 |
| `spring-orm`, `spring-web`, `spring-context` | 5.3.39-egov.1 | compile |
| `jaxws-api` (`javax.xml.ws`) | 2.3.1 | compile |
| `javax.annotation-api` | 1.3.2 | compile |
| `javax.servlet-api` | 4.0.1 | provided |
| `spring-test`, `hibernate-entitymanager`, `hsqldb` (`jdk8`), `commons-dbcp2`, `junit`, `dbunit`, `easymock` | 5.3.39-egov.1 / 5.6.15.Final / 2.7.3 / 2.13.0 / 4.13.2 / 2.7.3 / 5.2.0 | test |

---

## 설정 및 사용 예시

### 기본 설정 (`src/main/resources/org/egovframe/rte/itl/webservice/context/context-webservice.xml`)

모듈에 포함된 설정 파일입니다. CXF 기본 설정을 import하고 DAO·SessionFactory·TypeLoader·ClassLoader bean을 정의합니다. `dataSource` bean은 적용 프로젝트에서 별도로 정의해야 합니다.

```xml
<import resource="classpath:META-INF/cxf/cxf.xml"/>
<import resource="classpath:META-INF/cxf/cxf-extension-soap.xml"/>
<import resource="classpath:META-INF/cxf/cxf-servlet.xml"/>

<bean id="integrationDefinitionDao"
      class="org.egovframe.rte.itl.integration.metadata.dao.hibernate.HibernateIntegrationDefinitionDao">
    <property name="sessionFactory" ref="sessionFactory"/>
</bean>
<bean id="recordTypeDefinitionDao"
      class="org.egovframe.rte.itl.integration.metadata.dao.hibernate.HibernateRecordTypeDefinitionDao">
    <property name="sessionFactory" ref="sessionFactory"/>
</bean>
<bean id="webServiceClientDefinitionDao"
      class="org.egovframe.rte.itl.webservice.data.dao.hibernate.HibernateWebServiceClientDefinitionDao">
    <property name="sessionFactory" ref="sessionFactory"/>
</bean>
<bean id="webServiceServerDefinitionDao"
      class="org.egovframe.rte.itl.webservice.data.dao.hibernate.HibernateWebServiceServerDefinitionDao">
    <property name="sessionFactory" ref="sessionFactory"/>
</bean>

<bean id="sessionFactory" class="org.springframework.orm.hibernate5.LocalSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="mappingResources">
        <list>
            <value>/org/egovframe/rte/itl/integration/metadata/dao/hibernate/OrganizationDefinition.hbm.xml</value>
            <value>/org/egovframe/rte/itl/integration/metadata/dao/hibernate/SystemDefinition.hbm.xml</value>
            <value>/org/egovframe/rte/itl/integration/metadata/dao/hibernate/ServiceDefinition.hbm.xml</value>
            <value>/org/egovframe/rte/itl/integration/metadata/dao/hibernate/RecordTypeDefinition.hbm.xml</value>
            <value>/org/egovframe/rte/itl/integration/metadata/dao/hibernate/IntegrationDefinition.hbm.xml</value>
            <value>/org/egovframe/rte/itl/webservice/data/dao/hibernate/WebServiceClientDefinition.hbm.xml</value>
            <value>/org/egovframe/rte/itl/webservice/data/dao/hibernate/WebServiceServerDefinition.hbm.xml</value>
        </list>
    </property>
    <property name="hibernateProperties">
        <props>
            <prop key="hibernate.dialect">org.hibernate.dialect.HSQLDialect</prop>
            <prop key="hibernate.hbm2ddl.auto">validate</prop>
        </props>
    </property>
</bean>

<bean id="typeLoader" class="org.egovframe.rte.itl.integration.type.support.TypeLoaderUsingMetadata">
    <property name="recordTypeDefinitionDao" ref="recordTypeDefinitionDao"/>
</bean>

<bean id="classLoader" class="org.egovframe.rte.itl.webservice.service.impl.EgovWebServiceClassLoaderFactoryBean"/>
```

### 웹서비스 컨텍스트 bean

`EgovWebServiceServlet`은 bean id `egovWebServiceContext`를 조회하므로 아래와 같이 등록합니다. 속성명은 `EgovWebServiceContext`의 setter 기준이며, 초기화는 `init()`에서 수행합니다.

```xml
<bean id="egovWebServiceContext" class="org.egovframe.rte.itl.webservice.EgovWebServiceContext" init-method="init">
    <property name="organizationId" value="ORG00000"/>
    <property name="systemId" value="SYS00000"/>
    <property name="defaultTimeout" value="5000"/>
    <property name="integrationDefinitionDao" ref="integrationDefinitionDao"/>
    <property name="webServiceClientDefinitionDao" ref="webServiceClientDefinitionDao"/>
    <property name="webServiceServerDefinitionDao" ref="webServiceServerDefinitionDao"/>
    <property name="typeLoader" ref="typeLoader"/>
    <property name="classLoader" ref="classLoader"/>
</bean>
```

`web.xml`에는 `org.egovframe.rte.itl.webservice.EgovWebServiceServlet`을 서블릿으로 등록합니다. 서블릿은 `loadBus` 시점에 위 컨텍스트의 `publishServer(getBus())`를 호출하여 `WebServiceServerDefinition`의 주소로 엔드포인트를 공개합니다.

### 연계 서비스 호출

```java
EgovIntegrationContext context = ...;   // egovWebServiceContext bean
EgovIntegrationService service = context.getService("AtoB");   // IntegrationDefinition id

EgovIntegrationMessage request = service.createRequestMessage();
request.getBody().put("juminNo", "...");

EgovIntegrationMessage response = service.sendSync(request);
if (response.getHeader().getResultCode() == ResultCode.OK) { /* 정상 */ }
```

### 서비스 제공자 구현

`ServiceDefinition.serviceProviderBeanId`에 지정한 bean이 `EgovIntegrationServiceProvider`를 구현하면, 생성된 엔드포인트가 `ServiceBridgeImpl`을 통해 `service(requestMessage, responseMessage)`를 호출합니다.

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Integration/org.egovframe.rte.itl.webservice -am
```

## 테스트

```bash
mvn test -pl Integration/org.egovframe.rte.itl.webservice
```

외부 서버는 필요 없습니다. DAO 테스트는 `commons-dbcp2` `BasicDataSource`로 연결한 HSQLDB 인메모리 DB(`jdbc:hsqldb:mem:test/test`)와 DBUnit 데이터셋(`dataset.xml`)을 사용하고, `EgovWebServiceContextTest`/`EgovWebServiceTest`는 인메모리 DAO·Echo 클라이언트로 동작합니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.itl.webservice</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>` 생략 가능. `-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록 필요. 서블릿 컨테이너에 배포하는 경우 `javax.servlet-api`는 컨테이너가 제공합니다(provided).

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.itl.integration](../org.egovframe.rte.itl.integration/README.md) (연계 표준 인터페이스·메타데이터)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
