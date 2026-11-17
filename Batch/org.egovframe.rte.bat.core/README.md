# org.egovframe.rte.bat.core

**배치(Batch) 실행환경 모듈**입니다. Spring Batch 4.3.10을 기반으로 대용량 파일·DB 연동 Reader/Writer, 복합(Composite) Reader, Job/Step/Chunk 리스너, Job·Step 범위 변수 공유, 커맨드라인·스케줄러 실행기 등 배치 업무 개발에 필요한 확장 기능을 제공합니다. 실행환경의 다른 모듈은 이 모듈에 의존하지 않으며, 배치 적용 프로젝트(배치 템플릿 등)가 직접 사용합니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.bat.core:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Spring Batch 4.3.10 / Spring Integration 5.5.20-egov.1
- **실행환경 의존**: `org.egovframe.rte.fdl.cmmn`, `org.egovframe.rte.psl.dataaccess` (compile), `org.egovframe.rte.fdl.idgnr`, `spring-modules-validation` (test)

---

## 특징

- **Spring Batch 표준 확장**: `ItemReader`/`ItemWriter`/`LineMapper`/`LineTokenizer`/`FieldExtractor` 계열을 Egov 네이밍으로 래핑·보완
- **레거시 연계 파일 형식**: 고정 길이(문자/바이트 단위)·구분자(이스케이프 문자 지원)·바이트 단위 Tokenizer, 인덱스 파일 Reader/Writer, 파티션 공유 Writer
- **DB·MyBatis 연동**: `MyBatisPagingItemReader`/`MyBatisBatchItemWriter` 확장, JDBC Batch Writer와 `PreparedStatement` 세터 유틸
- **변수 공유**: Job/Step 실행 컨텍스트와 싱글톤 리소스 변수를 리스너(`EgovJobVariableListener`, `EgovStepVariableListener`, `EgovResourceVariable`)로 공유
- **운영 편의**: Job/Step/Chunk 전후 리스너, 출력 파일 리스너, `EgovBatchRunner`(JobOperator 래퍼), 커맨드라인·Quartz 스케줄 실행기
- **기타**: Flow `Decider`, 이벤트 알림 트리거, 셸 스크립트·파일 삭제 Tasklet, DB 메시지 기반 `EgovBatchException`

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/bat/
├── exception/
│   └── EgovBatchException.java
├── item/
│   ├── DefaultItemReader.java
│   └── DefaultItemWriter.java
├── support/
│   ├── EgovJobVariableListener.java
│   ├── EgovResourceVariable.java
│   └── EgovStepVariableListener.java
└── core/
    ├── event/                  # EventNoticeTrigger, EgovEventNoticeTrigger
    ├── job/flow/               # EgovDecider
    ├── launch/support/         # EgovBatchRunner, EgovCommandLineRunner, EgovSchedulerRunner
    ├── listener/               # Job/Step/Chunk Pre/PostProcessor, EgovOutputFileListener
    ├── reflection/             # EgovReflectionSupport
    ├── step/                   # TaskletShellStep, TaskletDeleteStep, ShellScriptSupport
    └── item/
        ├── composite/          # EgovCompositeItemMapper, EgovItemsMapper
        │   ├── provider/       # EgovCompositeDataProvider
        │   └── reader/         # EgovCompositeCursorReader, EgovCompositeFileReader, EgovCompositePagingReader
        ├── database/           # EgovJdbcBatchItemWriter, EgovMyBatisBatchItemWriter, EgovMyBatisPagingItemReader
        │   └── support/        # EgovItemPreparedStatementSetter, EgovMethodMapItemPreparedStatementSetter
        └── file/               # EgovByteReader(Factory), EgovFlatFileByteReader, EgovIndexFileReader/Writer,
            │                   # EgovLineMapper, EgovPartitionFlatFileItemWriter
            ├── mapping/        # EgovByteLineMapper, EgovDefaultLineMapper, EgovObjectMapper
            └── transform/      # Egov*LineTokenizer, EgovFixedByte*Tokenizer, EgovFixedLengthTokenizer,
                                # EgovFixedLengthLineAggregator, EgovFieldExtractor
```

`src/main/resources`에는 설정 파일이 없습니다. 배치 Job 설정 예시는 `src/test/resources/org/egovframe/batch/` 아래에 있습니다.

---

## 주요 기능 (패키지별)

### `core/item/file`

| 영역 | 클래스 | 설명 |
|------|--------|------|
| Tokenizer | `EgovDelimitedLineTokenizer`, `EgovEscapableDelimitedLineTokenizer` | 구분자 기반 토크나이저. Escapable 버전은 이스케이프 문자가 포함된 구분자 처리 |
| Tokenizer | `EgovFixedLengthTokenizer`, `EgovFixedByteLengthTokenizer`, `EgovFixedByteTokenizer` | 고정 길이(문자/바이트) 토크나이저. `EgovAbstractLineTokenizer`/`EgovAbstractByteLineTokenizer`를 상속 |
| Mapper | `EgovDefaultLineMapper`, `EgovByteLineMapper`, `EgovObjectMapper` | 한 라인을 토크나이저로 자른 뒤 `EgovObjectMapper`(`type`, `names` 속성)로 VO에 매핑 |
| Aggregator | `EgovFixedLengthLineAggregator`, `EgovFieldExtractor` | 쓰기 시 getter 호출로 필드 추출, 고정 길이 문자열 생성 |
| Reader/Writer | `EgovFlatFileByteReader`, `EgovByteReader`, `EgovByteReaderFactory` | Flat File을 바이트 단위로 읽음 |
| Reader/Writer | `EgovIndexFileReader`, `EgovIndexFileWriter` | 인덱스(순번) 파일명을 기준으로 입력/출력 파일 결정 (`indexResource` 속성) |
| Writer | `EgovPartitionFlatFileItemWriter` | 파티션 실행 시 여러 스레드가 하나의 출력 파일을 공유하여 Write |

### `core/item/database`

| 클래스 | 설명 |
|--------|------|
| `EgovMyBatisPagingItemReader` | MyBatis-Spring `MyBatisPagingItemReader` 확장. `resourceVariable`, `jobVariable`, `stepVariable` 속성으로 변수 공유 |
| `EgovMyBatisBatchItemWriter` | MyBatis-Spring `MyBatisBatchItemWriter` 확장. 변수 공유 기능 제공 (Map item 엔트리 누락 문제 수정 반영) |
| `EgovJdbcBatchItemWriter` | JDBC Batch Writer. `EgovItemPreparedStatementSetter` 계열 세터 필수 |
| `EgovItemPreparedStatementSetter`, `EgovMethodMapItemPreparedStatementSetter` | `params` 속성(필드명 목록)에 따라 getter를 호출하여 `PreparedStatement`에 설정 |

### `core/item/composite`

- `EgovCompositePagingReader`, `EgovCompositeCursorReader`, `EgovCompositeFileReader`: ItemReaderList에 등록된 여러 Reader의 아이템을 함께 읽는 `ItemStreamReader`
- `EgovCompositeItemMapper`, `EgovItemsMapper`, `EgovCompositeDataProvider`: 읽어들인 각 아이템을 `Object[]` 형태로 매핑

### `core/listener`, `support`

| 클래스 | 설명 |
|--------|------|
| `EgovJobPreProcessor`, `EgovJobPostProcessor` | `JobExecutionListenerSupport` 확장, Job 전후 처리 |
| `EgovStepPreProcessor`, `EgovStepPostProcessor` | `StepListenerSupport` 확장, Step 전후 처리 |
| `EgovChunkPreProcessor`, `EgovChunkPostProcessor` | `ChunkListenerSupport` 확장, Chunk 전후 처리 |
| `EgovOutputFileListener` | 입력 파일과 같은 이름으로 출력 파일명 지정 |
| `EgovJobVariableListener` | `JobExecutionListener`. `pros`(Properties) 속성의 값을 jobExecutionContext에 저장 |
| `EgovStepVariableListener` | `StepExecutionListener`. `pros` 속성의 값을 stepExecutionContext에 저장 |
| `EgovResourceVariable` | 싱글톤 방식 리소스 변수 (Map 형태) |

### `core/launch/support`

| 클래스 | 설명 |
|--------|------|
| `EgovBatchRunner` | `JobOperator`/`JobExplorer`/`JobRepository`를 생성자로 받아 `start`, `restart`, `stop`, `createUniqueJobParameters`, `getJobExecution` 등 제공 |
| `EgovCommandLineRunner` | `start(jobPath, jobIdentifier, parameters, opts)`로 XML Job Context를 로딩하여 실행. 파라미터는 `name=value` 형식 |
| `EgovSchedulerRunner` | `contextPath`, `schedulerJobPath`, `jobPaths`, `delayTime`으로 Quartz 스케줄 컨텍스트를 기동하고 `delayTime`(ms) 동안 대기 (음수이면 반복 대기) |

### `core/step`, `core/job/flow`, `core/event`, `exception`

| 클래스 | 설명 |
|--------|------|
| `TaskletShellStep` | `shellScript`, `encoding` 속성으로 셸 스크립트 실행 Tasklet |
| `TaskletDeleteStep` | `directory` 속성의 파일 삭제 Tasklet |
| `ShellScriptSupport` | `shellCmd(command, encoding)` 정적 유틸. 셸 메타문자 차단, `setAllowedExecutables(Set)`로 허용 실행파일 목록 제한 가능 (기본 null = 제한 없음) |
| `EgovDecider` | `JobExecutionDecider` 구현, stepExecution의 exitCode를 그대로 반환 |
| `EventNoticeTrigger`, `EgovEventNoticeTrigger` | 이벤트 알림용 `invoke` 메소드를 파라미터 타입별로 정의한 인터페이스와 기본 구현 |
| `EgovBatchException` | `BaseRuntimeException`(fdl.cmmn) 확장. `DataSource`와 메시지 키(또는 SQL)로 DB에서 에러 메시지를 조회 |
| `EgovReflectionSupport` | Reader/Writer가 사용하는 Reflection 유틸 |

---

## 주요 의존성

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte.fdl.cmmn`, `org.egovframe.rte.psl.dataaccess` | 4.3.1 | compile. MyBatis 3.5.16 / MyBatis-Spring 2.1.2는 `psl.dataaccess`를 통해 전이 |
| `spring-batch-test`, `spring-batch-integration` | 4.3.10 | compile (`spring-batch-core`, `spring-batch-infrastructure` 전이) |
| `spring-integration-core` | 5.5.20-egov.1 | compile (`spring-batch-integration`이 가져오는 버전을 소비자에게도 적용) |
| `micrometer-core` | 1.15.12 | compile (`spring-batch-core` 전이 의존성, CVE-2026-40984 대응) |
| `spring-webmvc`, `spring-context-support`, `spring-orm`, `spring-messaging`, `spring-aop`, `spring-tx`, `spring-jdbc` | 5.3.39-egov.1 | compile |
| `javax.annotation-api` | 1.3.2 | compile |
| `commons-io` | 2.18.0 | compile |
| `spring-test`, `spring-oxm` | 5.3.39-egov.1 | test |
| `quartz` | 2.3.2 | test (c3p0 0.14.0 전이) |
| `hsqldb` (`jdk8`), `commons-dbcp2`, `javax.mail`, `javax.servlet-api`, `junit` | 2.7.3 / 2.13.0 / 1.5.0-b01 / 4.0.1 / 4.13.2 | test |

Quartz는 test scope이므로 `EgovSchedulerRunner`를 사용하는 적용 프로젝트는 `org.quartz-scheduler:quartz`를 직접 추가해야 합니다.

---

## 설정 및 사용 예시

테스트 리소스(`src/test/resources/org/egovframe/batch/`)의 설정입니다. Spring Batch XML 네임스페이스(`spring-batch-3.0.xsd`)를 사용합니다.

### 배치 기반 설정 (`batch-runner-context.xml`)

```xml
<bean class="org.egovframe.rte.bat.core.launch.support.EgovBatchRunner">
    <constructor-arg ref="jobOperator"/>
    <constructor-arg ref="jobExplorer"/>
    <constructor-arg ref="jobRepository"/>
</bean>

<bean id="jobLauncher" class="org.springframework.batch.core.launch.support.SimpleJobLauncher">
    <property name="jobRepository" ref="jobRepository"/>
</bean>
<bean id="jobRepository" class="org.springframework.batch.core.repository.support.JobRepositoryFactoryBean"
      p:dataSource-ref="dataSource" p:transactionManager-ref="transactionManager" p:lobHandler-ref="lobHandler"/>
<bean id="jobOperator" class="org.springframework.batch.core.launch.support.SimpleJobOperator"
      p:jobLauncher-ref="jobLauncher" p:jobExplorer-ref="jobExplorer"
      p:jobRepository-ref="jobRepository" p:jobRegistry-ref="jobRegistry"/>
<bean id="jobExplorer" class="org.springframework.batch.core.explore.support.JobExplorerFactoryBean"
      p:dataSource-ref="dataSource"/>
<bean id="jobRegistry" class="org.springframework.batch.core.configuration.support.MapJobRegistry"/>
```

```java
@Autowired
private EgovBatchRunner egovBatchRunner;

String jobParameters = egovBatchRunner.createUniqueJobParameters();
long executionId = egovBatchRunner.start("batchRunnerTestJob", jobParameters);
egovBatchRunner.getJobOperator().stop(executionId);
long resumedExecutionId = egovBatchRunner.restart(executionId);
```

### 구분자 파일 Reader + Job 변수 (`jobs/delimitedToDelimitedJob-JobVariable.xml`)

```xml
<bean id="egovJobVariableListener" class="org.egovframe.rte.bat.support.EgovJobVariableListener">
    <property name="pros">
        <props>
            <prop key="JobVariableKey1">JobVariableValue1</prop>
        </props>
    </property>
</bean>

<job id="delimitedToDelimitedJob-JobVariable" parent="eGovBaseJob" xmlns="http://www.springframework.org/schema/batch">
    <listeners>
        <listener ref="egovJobVariableListener"/>
    </listeners>
    <step id="delimitedToDelimitedStep-JobVariable" parent="eGovBaseStep">
        <tasklet>
            <chunk reader="...delimitedItemReader" processor="...itemProcessor" writer="...delimitedItemWriter" commit-interval="2"/>
        </tasklet>
    </step>
</job>

<bean id="...delimitedItemReader" class="org.springframework.batch.item.file.FlatFileItemReader" scope="step">
    <property name="resource" value="#{jobParameters[inputFile]}"/>
    <property name="lineMapper">
        <bean class="org.egovframe.rte.bat.core.item.file.mapping.EgovDefaultLineMapper">
            <property name="lineTokenizer">
                <bean class="org.egovframe.rte.bat.core.item.file.transform.EgovDelimitedLineTokenizer">
                    <property name="delimiter" value=","/>
                </bean>
            </property>
            <property name="objectMapper">
                <bean class="org.egovframe.rte.bat.core.item.file.mapping.EgovObjectMapper">
                    <property name="type" value="org.egovframe.brte.sample.common.domain.trade.CustomerCredit"/>
                    <property name="names" value="name,credit"/>
                </bean>
            </property>
        </bean>
    </property>
</bean>
```

### MyBatis Reader/Writer (`jobs/mybatisJob.xml`, `context-mybatis.xml`)

```xml
<bean id="sqlSession" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="configLocation" value="classpath:org/egovframe/batch/mapper/sql-mybatis-config.xml"/>
</bean>
<bean class="org.egovframe.rte.psl.dataaccess.mapper.MapperConfigurer">
    <property name="basePackage" value="org.egovframe.rte.bat.mapper"/>
</bean>

<bean id="mybatisJobStep.mybatisItemReader"
      class="org.egovframe.rte.bat.core.item.database.EgovMyBatisPagingItemReader" scope="step">
    <property name="sqlSessionFactory" ref="sqlSession"/>
    <property name="resourceVariable" ref="egovResourceVariable"/>
    <property name="jobVariable" ref="egovJobVariable"/>
    <property name="stepVariable" ref="egovStepVariable"/>
    <property name="queryId" value="EmpMapper.selectEmpList"/>
    <property name="pageSize" value="#{new Integer(10)}"/>
</bean>

<bean id="mybatisJobStep.mybatisItemWriter"
      class="org.egovframe.rte.bat.core.item.database.EgovMyBatisBatchItemWriter">
    <property name="resourceVariable" ref="egovResourceVariable"/>
    <property name="jobVariable" ref="egovJobVariable"/>
    <property name="stepVariable" ref="egovStepVariable"/>
    <property name="sqlSessionFactory" ref="sqlSession"/>
    <property name="statementId" value="EmpMapper.updateEmp1"/>
</bean>

<bean id="mybatisJobStep.jdbcItemWriter" class="org.egovframe.rte.bat.core.item.database.EgovJdbcBatchItemWriter">
    <property name="assertUpdates" value="true"/>
    <property name="itemPreparedStatementSetter">
        <bean class="org.egovframe.rte.bat.core.item.database.support.EgovMethodMapItemPreparedStatementSetter"/>
    </property>
    <property name="sql" value="UPDATE CUSTOMER set credit = ? where name = ? "/>
    <property name="params" value="credit,name"/>
    <property name="dataSource" ref="dataSource"/>
</bean>
```

### 셸 스크립트 Tasklet (`jobs/delimitedToDelimitedJob-ShellStep.xml`)

```xml
<bean id="taskletShell" class="org.egovframe.rte.bat.core.step.TaskletShellStep" scope="step">
    <property name="shellScript" value="#{jobExecutionContext[shellScript]}"/>
    <property name="encoding" value="#{jobExecutionContext[encoding]}"/>
</bean>
```

`ShellScriptSupport.shellCmd`는 셸 메타문자(`; | & \` $ ( ) \ < >`, 개행)만 차단하므로 신뢰할 수 없는 값을 `shellScript`로 전달하지 말고, 실행 프로그램을 제한하려면 `ShellScriptSupport.setAllowedExecutables(Set)`을 설정하십시오.

### 스케줄러 실행 (`EgovSchedulerRunner`)

```java
EgovSchedulerRunner runner = new EgovSchedulerRunner(
        "/META-INF/spring/launch/context-scheduler.xml",   // SchedulerFactoryBean, CronTriggerFactoryBean
        "/META-INF/spring/launch/context-job.xml",         // MethodInvokingJobDetailFactoryBean
        new ArrayList<String>(), 30000);                   // 추가 Job 경로, 대기 시간(ms)
runner.start();
```

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Batch/org.egovframe.rte.bat.core -am
```

## 테스트

```bash
mvn test -pl Batch/org.egovframe.rte.bat.core
```

외부 서버는 필요 없습니다. 테스트는 `<jdbc:embedded-database type="HSQL">`로 생성한 HSQLDB 인메모리 DB(`testdata/sampledb.sql`, `testdata/sampledb_mybatis.script`)를 사용하며, 출력 파일은 `target/test-outputs/` 아래에 생성됩니다. `EgovSchedulerRunnerTest`는 스케줄러 대기 시간(30초·40초) 때문에 1분 이상 걸립니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.bat.core</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>` 생략 가능. `-egov.1` 포크 아티팩트(Spring Framework, Spring Integration)를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록 필요.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.cmmn](../../Foundation/org.egovframe.rte.fdl.cmmn/README.md), [org.egovframe.rte.psl.dataaccess](../../Persistence/org.egovframe.rte.psl.dataaccess/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
