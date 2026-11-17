# org.egovframe.rte.psl.dataaccess

**영속성(Persistence) 계층 데이터 접근 모듈**로, **MyBatis 3**와 레거시 **iBatis 2(sqlMap)** 연동을 함께 지원합니다.
MyBatis용 공통 부모 클래스 `EgovAbstractMapper`와 `@Mapper` 스캔 설정(`MapperConfigurer`), iBatis용 공통 부모 클래스 `EgovAbstractDAO`,
현재 Spring Framework가 제공하지 않는 `org.springframework.orm.ibatis` 패키지를 대체하는 `org.egovframe.rte.psl.orm.ibatis` 호환 계층, 타입 핸들러, `EgovMap` 유틸리티를 제공합니다.
`org.egovframe.rte.psl.data.jpa`, `org.egovframe.rte.bat.core`, `org.egovframe.rte.fdl.excel` 모듈이 이 모듈에 의존합니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.psl.dataaccess:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / MyBatis 3.5.16 / MyBatis-Spring 2.1.2 / iBatis 2.3.4.726 (javax API 기반)
- **실행환경 의존**: `org.egovframe.rte.fdl.logging`

---

## 특징

- **이중 스택 지원**: 신규 개발은 MyBatis(`SqlSessionFactoryBean` + Mapper 인터페이스), 기존 시스템은 iBatis sqlMap(`SqlMapClientFactoryBean`)을 그대로 유지할 수 있음
- **표준 부모 클래스**: `EgovAbstractMapper`(MyBatis, `SqlSessionDaoSupport` 상속)와 `EgovAbstractDAO`(iBatis, `SqlMapClientDaoSupport` 상속)가 insert/update/delete/select/list/paging 메서드를 래핑하고 `@Resource`로 `sqlSession`/`sqlMapClient` 빈을 자동 주입
- **애노테이션 기반 Mapper 스캔**: `@Mapper` 마커 애노테이션과 `MapperConfigurer`(MyBatis-Spring `MapperScannerConfigurer` 확장)로 Mapper 인터페이스를 빈으로 등록
- **iBatis 호환 계층**: 현재 Spring Framework에 포함되지 않는 `SqlMapClientFactoryBean`, `SqlMapClientTemplate`, `SqlMapClientDaoSupport`, LOB 타입 핸들러를 `org.egovframe.rte.psl.orm.ibatis` 패키지로 내장
- **타입 핸들러**: `java.util.Calendar`·포맷 문자열과 `java.sql.Timestamp` 간 변환 핸들러(MyBatis용 1종, iBatis용 2종)
- **EgovMap**: 조회 결과 컬럼명을 Camel Case 키로 변환해 담는 `ListOrderedMap` 확장 Map

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/psl/
├── dataaccess/
│   ├── EgovAbstractDAO.java              # iBatis 공통 부모 DAO
│   ├── EgovAbstractMapper.java           # MyBatis 공통 부모 DAO
│   ├── mapper/
│   │   ├── Mapper.java                   # Mapper 마커 애노테이션
│   │   └── MapperConfigurer.java         # @Mapper 스캔 configurer
│   ├── typehandler/
│   │   ├── CalendarMapperTypeHandler.java    # MyBatis TypeHandler<Calendar>
│   │   ├── CalendarTypeHandler.java          # iBatis TypeHandlerCallback (Calendar)
│   │   └── StringTimestampTypeHandler.java   # iBatis TypeHandlerCallback (String-Timestamp)
│   └── util/
│       ├── CamelUtil.java
│       └── EgovMap.java
└── orm/
    ├── ObjectOptimisticLockingFailureException.java
    ├── ObjectRetrievalFailureException.java
    └── ibatis/                               # 구 org.springframework.orm.ibatis 호환 계층
        ├── SqlMapClientCallback.java
        ├── SqlMapClientFactoryBean.java
        ├── SqlMapClientOperations.java
        ├── SqlMapClientTemplate.java
        └── support/
            ├── AbstractLobTypeHandler.java
            ├── BlobByteArrayTypeHandler.java
            ├── BlobSerializableTypeHandler.java
            ├── ClobStringTypeHandler.java
            └── SqlMapClientDaoSupport.java
```

`src/main/resources`는 없습니다. 테스트 설정은 `src/test/resources/META-INF/spring/*.xml`, 매퍼는 `src/test/resources/META-INF/sqlmap/`에 있습니다.

---

## 주요 클래스

| 클래스 | 설명 |
|--------|------|
| `EgovAbstractMapper` | MyBatis 연동 공통 부모 DAO. `SqlSessionDaoSupport`를 상속하며 `@Resource(name="sqlSession")`으로 `SqlSessionFactory`를 주입받음. `insert`/`update`/`delete`/`selectOne`/`selectList`/`selectMap`/`listWithPaging`/`listToOutUsingResultHandler` 제공 (`selectByPk`, `list`는 deprecated) |
| `EgovAbstractDAO` | iBatis 연동 공통 부모 DAO. `SqlMapClientDaoSupport`를 상속하며 `@Resource(name="sqlMapClient")`로 `SqlMapClient`를 주입받음. `insert`/`update`/`delete`/`select`/`list`/`listWithPaging`/`map` 제공 (`selectByPk`는 deprecated) |
| `Mapper` | Mapper 인터페이스용 마커 애노테이션(`@Component` 메타 애노테이션 포함, 값은 빈 이름). MyBatis의 `org.apache.ibatis.annotations.Mapper`와 이름이 같으므로 import 시 `org.egovframe.rte.psl.dataaccess.mapper.Mapper`를 사용 |
| `MapperConfigurer` | MyBatis-Spring `MapperScannerConfigurer` 확장. 생성자에서 `annotationClass=Mapper.class`, `sqlSessionFactoryBeanName="sqlSession"`을 기본 설정 |
| `CalendarMapperTypeHandler` | MyBatis `TypeHandler<Calendar>` 구현. `java.util.Calendar` ↔ `java.sql.Timestamp` 변환 |
| `CalendarTypeHandler` | iBatis `TypeHandlerCallback` 구현. `java.util.Calendar` ↔ `java.sql.Timestamp` 변환 |
| `StringTimestampTypeHandler` | iBatis `TypeHandlerCallback` 구현. 포맷된 날짜 문자열 ↔ `java.sql.Timestamp` 변환 |
| `EgovMap` | Commons Collections4 `ListOrderedMap` 확장. `put` 시 키를 Camel Case로 변환하여 DB 컬럼명을 VO 속성명처럼 조회 가능 |
| `CamelUtil` | `convert2CamelCase(String)` 정적 메서드로 언더스코어 표기를 Camel Case로 변환 |
| `SqlMapClientFactoryBean` | iBatis `SqlMapClient`를 생성하는 `FactoryBean`. `configLocation`, `mappingLocations`, `dataSource`, `lobHandler` 등 설정 |
| `SqlMapClientTemplate` / `SqlMapClientOperations` / `SqlMapClientCallback` | iBatis 실행 템플릿과 인터페이스. `SQLException`을 Spring `DataAccessException`으로 변환 |
| `SqlMapClientDaoSupport` | `SqlMapClientTemplate`를 보유하는 DAO 지원 클래스 (`EgovAbstractDAO`의 부모) |
| `AbstractLobTypeHandler`, `BlobByteArrayTypeHandler`, `BlobSerializableTypeHandler`, `ClobStringTypeHandler` | Spring `LobHandler`를 이용하는 iBatis BLOB/CLOB 타입 핸들러 |
| `ObjectRetrievalFailureException`, `ObjectOptimisticLockingFailureException` | `org.egovframe.rte.psl.orm` 패키지의 ORM 예외 클래스 |

---

## 주요 의존성

모듈 POM은 버전을 선언하지 않으며 루트 POM `dependencyManagement`의 버전이 적용됩니다.

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.logging` | 4.3.1 | compile |
| `org.springframework:spring-context`, `spring-orm` | 5.3.39-egov.1 | compile (commons-logging 제외) |
| `org.mybatis:mybatis` | 3.5.16 | compile |
| `org.mybatis:mybatis-spring` | 2.1.2 | compile |
| `org.apache.ibatis:ibatis-sqlmap` | 2.3.4.726 | compile |
| `javax:javaee-api` | 8.0.1 | compile |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `org.apache.commons:commons-collections4` | 4.4 | compile (`EgovMap`) |
| `org.springframework:spring-test`, `junit`, `easymock` | 5.3.39-egov.1 / 4.13.2 / 5.2.0 | test |
| `org.apache.commons:commons-dbcp2`, `commons-lang3` | 2.13.0 / 3.20.0 | test |
| `org.hsqldb:hsqldb` (`jdk8` 분류자), `opensymphony:oscache` | 2.7.3 / 2.4.1 | test (인메모리 DB, iBatis cacheModel 테스트) |

---

## 설정 및 사용 예시

아래 설정은 `src/test/resources/META-INF/spring/`의 테스트 설정을 기준으로 합니다.

### MyBatis 설정 (`context-mapper.xml`)

```xml
<bean id="sqlSession" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="configLocation" value="classpath:/META-INF/sqlmap/sql-mapper-config.xml"/>
    <property name="mapperLocations" value="classpath:/META-INF/sqlmap/mappers/*.xml"/>
</bean>

<!-- @Mapper 인터페이스 스캔 (sqlSessionFactoryBeanName 기본값: sqlSession) -->
<bean class="org.egovframe.rte.psl.dataaccess.mapper.MapperConfigurer">
    <property name="basePackage" value="org.egovframe.rte.**.mapper"/>
</bean>

<!-- 배치 실행용 SqlSessionTemplate -->
<bean id="batchSqlSessionTemplate" class="org.mybatis.spring.SqlSessionTemplate">
    <constructor-arg index="0" ref="sqlSession"/>
    <constructor-arg index="1" value="BATCH"/>
</bean>
```

MyBatis 설정 파일(`sql-mapper-config.xml`)에 타입 핸들러를 등록합니다.

```xml
<configuration>
    <typeHandlers>
        <typeHandler handler="org.egovframe.rte.psl.dataaccess.typehandler.CalendarMapperTypeHandler"/>
    </typeHandlers>
</configuration>
```

### MyBatis Mapper 인터페이스 / 공통 DAO 사용

```java
// Mapper 인터페이스 방식 (src/test/java/.../mapper/EmployerMapper.java)
@Mapper("employerMapper")
public interface EmployerMapper {
    List<EmpVO> selectEmployerList(EmpVO vo);
    EmpVO selectEmployer(BigDecimal empNo);
    void insertEmployer(EmpVO vo);
    int updateEmployer(EmpVO vo);
    int deleteEmployer(BigDecimal empNo);
}

// 공통 부모 클래스 상속 방식 (src/test/java/.../dao/DeptMapper.java)
@Repository("deptMapper")
public class DeptMapper extends EgovAbstractMapper {
    public void insertDept(String queryId, DeptVO vo) { insert(queryId, vo); }
    public DeptVO selectDept(String queryId, DeptVO vo) { return (DeptVO) selectOne(queryId, vo); }
    public List<DeptVO> selectDeptList(String queryId, DeptVO searchVO) { return selectList(queryId, searchVO); }
}
```

### iBatis 설정 (`context-sqlMap.xml`)

```xml
<bean id="sqlMapClient" class="org.egovframe.rte.psl.orm.ibatis.SqlMapClientFactoryBean">
    <property name="configLocation" value="classpath:/META-INF/sqlmap/sql-map-config.xml"/>
    <property name="dataSource" ref="dataSource"/>
</bean>
```

`sql-map-config.xml`에 iBatis용 타입 핸들러를 등록합니다.

```xml
<sqlMapConfig>
    <settings useStatementNamespaces="false" defaultStatementTimeout="100"/>
    <typeHandler javaType="java.util.Calendar" jdbcType="TIMESTAMP"
                 callback="org.egovframe.rte.psl.dataaccess.typehandler.CalendarTypeHandler"/>
    <typeHandler javaType="string" jdbcType="TIMESTAMP"
                 callback="org.egovframe.rte.psl.dataaccess.typehandler.StringTimestampTypeHandler"/>
    <sqlMap resource="META-INF/sqlmap/mappings/testcase-basic.xml"/>
</sqlMapConfig>
```

```java
@Repository("deptDAO")
public class DeptDAO extends EgovAbstractDAO {
    public void insertDept(String queryId, DeptVO vo) { insert(queryId, vo); }
    public DeptVO selectDept(String queryId, DeptVO vo) { return (DeptVO) select(queryId, vo); }
    public List<DeptVO> selectDeptListWithPaging(String queryId, DeptVO vo, int pageIndex, int pageSize) {
        return (List<DeptVO>) listWithPaging(queryId, vo, pageIndex, pageSize);
    }
}
```

### 데이터소스 / 트랜잭션 (`context-datasource.xml`, `context-transaction.xml`)

```xml
<bean id="dataSource" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close">
    <property name="driverClassName" value="${jdbc.driver}"/>
    <property name="url" value="${jdbc.url}"/>
    <property name="username" value="${jdbc.username}"/>
    <property name="password" value="${jdbc.password}"/>
    <property name="defaultAutoCommit" value="false"/>
    <property name="poolPreparedStatements" value="true"/>
</bean>

<bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <property name="dataSource" ref="dataSource"/>
</bean>
```

테스트의 `jdbc.properties`는 HSQLDB 인메모리(`jdbc:hsqldb:mem:testdb`, `sa`)를 사용하며, `META-INF/testdata/`에 HSQL·MySQL·Oracle·Tibero용 DDL/초기 데이터 스크립트가 있습니다.

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Persistence/org.egovframe.rte.psl.dataaccess -am
```

## 테스트

```bash
mvn test -pl Persistence/org.egovframe.rte.psl.dataaccess
```

테스트는 HSQLDB 인메모리 DB만 사용하므로 외부 서버가 필요 없습니다.
`src/test/java/.../ibatis/*Test.java`(iBatis)와 `.../mybatis/*Test.java`(MyBatis)가 기본 CRUD, 배치, cacheModel, CallableStatement, LOB, 동적 SQL, ResultHandler/RowHandler, selectKey 등을 검증합니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.psl.dataaccess</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>` 생략 가능.
`-egov.1` 포크 아티팩트(Spring Framework 등)를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록 필요.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.logging](../../Foundation/org.egovframe.rte.fdl.logging/README.md), [org.egovframe.rte.psl.data.jpa](../org.egovframe.rte.psl.data.jpa/README.md), [org.egovframe.rte.bat.core](../../Batch/org.egovframe.rte.bat.core/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
