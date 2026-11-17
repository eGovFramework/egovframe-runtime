# org.egovframe.rte.fdl.idgnr

**Foundation ID 생성(ID Generation) 모듈**은 시스템 개발 시 필요한 유일한 ID를 DB 시퀀스, 채번 테이블(블록 단위), UUID 방식으로 생성하는 `EgovIdGnrService`와, 생성된 ID에 접두어·자릿수 채움을 적용하는 정책(`EgovIdGnrStrategy`)을 제공합니다. `org.egovframe.rte.bat.core`가 이 모듈에 의존합니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.fdl.idgnr:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 (`spring-context`, `spring-jdbc` provided)
- **실행환경 의존**: `org.egovframe.rte.fdl.cmmn` (`FdlException`)

---

## 특징

- **세 가지 채번 방식**: `EgovSequenceIdGnrServiceImpl`(DB 시퀀스 쿼리), `EgovTableIdGnrServiceImpl`(채번 테이블에서 `blockSize`만큼 블록을 미리 할당해 메모리에서 분배), `EgovUUIdGnrServiceImpl`(시각 기반 UUID 또는 `UUID.randomUUID()`)
- **다양한 타입**: `getNextBigDecimalId()`, `getNextLongId()`, `getNextIntegerId()`, `getNextShortId()`, `getNextByteId()`, `getNextStringId()`. 타입 최대값 초과 시 `FdlException`(`error.idgnr.greater.maxid`)
- **정책 적용**: `getNextStringId(EgovIdGnrStrategy)` 또는 `getNextStringId(String strategyId)`(Bean 이름)로 `EgovIdGnrStrategyImpl`의 `prefix` + `fillChar`로 `cipers` 자릿수 채움 적용
- **동시성·다중 WAS**: 테이블 방식은 `TransactionTemplate` 안에서 `SELECT ... FOR UPDATE` 행 잠금(기본, `selectForUpdate=false`로 해제 가능)으로 블록을 할당해 여러 WAS가 같은 DB를 공유해도 블록이 중복되지 않음. 서비스 내부는 `synchronized`
- **SQL 삽입 방지**: 테이블명·컬럼명은 안전한 식별자 패턴(`스키마.테이블` 허용)만 받고, 구분값(`tableName`)은 바인드 변수로 전달
- **UUID 보안 주의**: 주소(IP 또는 MAC) 설정 시 시각 기반 UUID(version 1)를 생성하므로 예측 가능. 세션ID·토큰 등 보안 목적에는 주소를 설정하지 않은 기본 경로(`UUID.randomUUID()`) 사용 (CWE-330)
- 메시지 번들 `org/egovframe/rte/fdl/idgnr/messages/idgnr*.properties` 제공 (`messageSource` Bean 필요)

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/fdl/idgnr/
├── EgovIdGnrService.java                 # 서비스 인터페이스
├── EgovIdGnrStrategy.java                # makeId(String originalId)
└── impl/
    ├── AbstractIdGnrService.java         # 타입 변환·정책 적용 공통 구현 (ApplicationContextAware, BeanFactoryAware)
    ├── AbstractDataIdGnrService.java     # dataSource, query 보유 (DisposableBean)
    ├── AbstractDataBlockIdGnrService.java# blockSize 단위 블록 할당 (InitializingBean)
    ├── EgovSequenceIdGnrServiceImpl.java # 시퀀스 방식
    ├── EgovTableIdGnrServiceImpl.java    # 채번 테이블 방식
    ├── EgovUUIdGnrServiceImpl.java       # UUID 방식
    ├── EgovSequenceIdGnrService.java     # @Deprecated (Impl 사용)
    ├── EgovTableIdGnrService.java        # @Deprecated (Impl 사용)
    ├── EgovUUIdGnrService.java           # @Deprecated (Impl 사용)
    ├── Base64.java                       # 내부용 Base64 인코더
    └── strategy/
        └── EgovIdGnrStrategyImpl.java    # prefix / cipers / fillChar 정책

src/main/resources/org/egovframe/rte/fdl/idgnr/messages/
├── idgnr.properties
├── idgnr_en.properties
└── idgnr_ko.properties
```

---

## 주요 클래스

| 클래스 | 설명 |
|---|---|
| `EgovIdGnrService` | `getNextBigDecimalId()`, `getNextLongId()`, `getNextIntegerId()`, `getNextShortId()`, `getNextByteId()`, `getNextStringId()`, `getNextStringId(String strategyId)`, `getNextStringId(EgovIdGnrStrategy)` (모두 `throws FdlException`) |
| `EgovIdGnrStrategy` / `impl.strategy.EgovIdGnrStrategyImpl` | `makeId(originalId)`. 구현체 프로퍼티 `prefix`, `cipers`(기본 5), `fillChar`(기본 `'0'`). 예: prefix `TEST-`, cipers 5 → `TEST-00001` |
| `impl.AbstractIdGnrService` | `useBigDecimals` 프로퍼티, `strategy` 프로퍼티(`setStrategy`/`getStrategy`), 타입별 `getNext*Id()` 최종 구현, `getNextLongIdChecked(maxId)` |
| `impl.AbstractDataIdGnrService` | `dataSource`, `query` 프로퍼티. `destroy()`에서 자원 정리 |
| `impl.AbstractDataBlockIdGnrService` | `blockSize` 프로퍼티, `allocateBigDecimalIdBlock(blockSize)`/`allocateLongIdBlock(blockSize)` 추상 메서드, `afterPropertiesSet()` |
| `impl.EgovSequenceIdGnrServiceImpl` | `query`에 지정한 시퀀스 조회 SQL을 실행해 ID 반환 (예: `SELECT seq.NEXTVAL FROM DUAL`, HSQLDB `call NEXT VALUE FOR idstest`) |
| `impl.EgovTableIdGnrServiceImpl` | 프로퍼티 `table`(기본 `ids`), `tableName`(기본 `id`), `tableNameFieldName`(기본 `table_name`), `nextIdFieldName`(기본 `next_id`), `blockSize`, `selectForUpdate`(기본 `true`). 행이 없으면 초기값을 INSERT 한 뒤 블록 할당 |
| `impl.EgovUUIdGnrServiceImpl` | `address` 프로퍼티(IP `a.b.c.d` 또는 MAC `xx:xx:xx:xx:xx:xx`, 미설정 시 `UUID.randomUUID()`). `getNextStringId()`, `getNextBigDecimalId()`, `getNextByteId()`만 지원하며 int/long/short/정책 적용은 `FdlException`(`error.idgnr.not.supported`) |

---

## 주요 의존성

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.cmmn` | 4.3.1 | compile |
| `spring-context` (commons-logging 제외) | 5.3.39-egov.1 | compile |
| `spring-jdbc` | 5.3.39-egov.1 | provided (`JdbcTemplate`, `TransactionTemplate` 사용. 적용 프로젝트가 제공) |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `spring-test`, `commons-dbcp2`, `mysql-connector-j`, `hsqldb`(`jdk8`), `junit`, `easymock` | 5.3.39-egov.1 / 2.13.0 / 8.4.0 / 2.7.3 / 4.13.2 / 5.2.0 | test |

버전은 루트 POM(`org.egovframe.rte.root` 4.3.1)의 `dependencyManagement`에서 관리하며 모듈 POM은 버전을 선언하지 않습니다.

---

## 설정 및 사용 예시

### 1) 공통 설정 (`src/test/resources/spring/context-common.xml`)

```xml
<jdbc:embedded-database id="dataSource" type="HSQL">
    <jdbc:script location="classpath:testdata/tab_sample_create.sql"/>
    <jdbc:script location="classpath:testdata/seq_sample_create.sql"/>
</jdbc:embedded-database>

<bean name="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
    <property name="useCodeAsDefaultMessage" value="true"/>
    <property name="basenames">
        <list>
            <value>org/egovframe/rte/fdl/idgnr/messages/idgnr</value>
        </list>
    </property>
</bean>
```

채번 테이블 DDL (`testdata/tab_sample_create.sql`):

```sql
CREATE TABLE ids (
    table_name VARCHAR(16) NOT NULL,
    next_id    DECIMAL(30) NOT NULL,
    PRIMARY KEY (table_name)
);
INSERT INTO ids VALUES ('id', '0');
```

### 2) 채번 테이블 방식 (`src/test/resources/spring/context-tableid.xml`)

```xml
<bean name="basicService" class="org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl"
      destroy-method="destroy">
    <property name="dataSource" ref="dataSource"/>
    <property name="blockSize" value="10"/>
    <property name="table" value="idttest"/>
    <property name="tableName" value="id"/>
</bean>

<!-- BigDecimal ID -->
<bean name="Ids-TestBigDecimalRequestIdsSize10" class="org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl"
      destroy-method="destroy">
    <property name="dataSource" ref="dataSource"/>
    <property name="useBigDecimals" value="true"/>
    <property name="blockSize" value="10"/>
    <property name="table" value="idttest"/>
    <property name="tableName" value="test"/>
</bean>
```

### 3) 시퀀스 방식 (`src/test/resources/spring/context-sequenceid.xml`)

```xml
<bean name="Ids-TestSequenceSimpleRequestIdsSize1"
      class="org.egovframe.rte.fdl.idgnr.impl.EgovSequenceIdGnrServiceImpl" destroy-method="destroy">
    <property name="dataSource" ref="dataSource"/>
    <property name="query" value="call NEXT VALUE FOR idstest"/>   <!-- Oracle: SELECT idstest.NEXTVAL FROM DUAL -->
</bean>
```

### 4) UUID 방식 (`src/test/resources/spring/context-uuid.xml`)

```xml
<bean name="UUIdGenerationService" class="org.egovframe.rte.fdl.idgnr.impl.EgovUUIdGnrServiceImpl">
    <property name="address" value="00:00:F0:79:19:5B"/>     <!-- MAC 또는 IP(100.128.120.107) -->
</bean>

<!-- 주소 미설정: UUID.randomUUID() -->
<bean name="UUIdGenerationServiceWithoutAddress" class="org.egovframe.rte.fdl.idgnr.impl.EgovUUIdGnrServiceImpl"/>
```

### 5) 정책(Strategy) 적용

```xml
<bean name="strategy" class="org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl">
    <property name="prefix" value="TEST-"/>
    <property name="cipers" value="5"/>
    <property name="fillChar" value="0"/>
</bean>
```

```java
@Resource(name = "basicService")
private EgovIdGnrService idGnrService;

long id = idGnrService.getNextLongId();
String strId = idGnrService.getNextStringId("strategy");   // 예: TEST-00001
```

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Foundation/org.egovframe.rte.fdl.idgnr -am
```

## 테스트

```bash
mvn test -pl Foundation/org.egovframe.rte.fdl.idgnr
```

테스트는 `<jdbc:embedded-database type="HSQL">` 인메모리 DB(`testdata/tab_sample_create.sql`, `seq_sample_create.sql`)만 사용하며 외부 서버가 필요 없습니다. `EgovTableIdGnrServiceWithThreadTest`는 다중 스레드 채번, `EgovIdGnrSecurityTest`는 SQL 식별자 검증과 UUID 형식을 검증합니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.fdl.idgnr</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>`을 생략할 수 있습니다. `spring-jdbc`는 provided 이므로 적용 프로젝트에서 별도로 포함해야 합니다. `-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록이 필요합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.cmmn](../org.egovframe.rte.fdl.cmmn/README.md), [org.egovframe.rte.bat.core](../../Batch/org.egovframe.rte.bat.core/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
