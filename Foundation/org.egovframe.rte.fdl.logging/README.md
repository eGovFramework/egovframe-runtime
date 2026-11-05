# org.egovframe.rte.fdl.logging

**Foundation Layer 로깅(Logging) 모듈**입니다. 실행환경의 로깅 구현체인 **Apache Log4j 2**와 **SLF4J 2.x** 브리지(`log4j-slf4j2-impl`, `jcl-over-slf4j`, `log4j-over-slf4j`)를 한 곳에서 의존성으로 묶어 제공하며, 여기에 Log4j 2 JDBC Appender용 커넥션 팩토리, 요청 식별자 검증, 리소스 해제 등의 소형 유틸리티 클래스를 더합니다. `fdl.cmmn`, `fdl.string`, `fdl.xml`, `psl.dataaccess`, `psl.reactive.*`, `ptl.reactive`, `itl.integration` 등 대부분의 실행환경 모듈이 이 모듈을 통해 로깅 의존성을 전이받습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.fdl.logging:4.3.1`
- **Runtime**: Java 1.8 / Log4j 2 2.25.5 / SLF4J 2.0.17 (Spring Framework 5.3.39-egov.1은 테스트 전용)
- **실행환경 의존**: 없음

---

## 특징

- **Log4j 2 + SLF4J 2 통합**: `log4j-core`와 `log4j-slf4j2-impl`을 compile 의존성으로 제공하여 SLF4J API로 작성한 로그가 Log4j 2로 출력됩니다.
- **레거시 로깅 API 브리지**: `jcl-over-slf4j`(Commons Logging), `log4j-over-slf4j`(Log4j 1.x API)를 함께 포함하여 Spring 등 서드파티 라이브러리의 로그를 SLF4J로 모읍니다.
- **JDBC Appender 연동**: Spring `DataSource`를 주입받아 Log4j 2 `JDBCAppender`에 `Connection`을 공급하는 `EgovConnectionFactory`를 제공합니다.
- **요청 식별자 검증**: 게이트웨이가 넘긴 `X-Request-Id` 등의 값을 MDC·응답 헤더에 싣기 전에 형식을 검증하는 `EgovRequestIds`를 제공합니다(로그 인젝션 CWE-117 대응, 4.3.1 추가).
- **리소스 해제 유틸**: 스트림·JDBC 객체·소켓을 예외 없이 닫는 `EgovResourceReleaser`와 JDK 로거 래퍼 `EgovJdkLogger`를 제공합니다.
- **설정 예제 제공**: `src/test/resources/log4j2.xml`에 Console/File/RollingFile/일자별 롤링/JDBC/Socket/SMTP Appender와 각종 Filter 설정 예제가 포함되어 있습니다.

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/fdl/logging/
├── db/
│   └── EgovConnectionFactory.java   # Log4j 2 JDBCAppender용 Connection 공급
└── util/
    ├── EgovJdkLogger.java           # java.util.logging 래퍼 (ignore/debug/info)
    ├── EgovRequestIds.java          # 요청 식별자(X-Request-Id 등) 형식 검증
    └── EgovResourceReleaser.java    # Closeable/JDBC/Socket 자원 해제
```

모듈 디렉터리에는 테스트 실행 시 생성되는 로그 출력 폴더 `logs/`(`file/`, `rolling/`, `daily/`)와 JDBC Appender용 테이블 생성 스크립트 `sql/db2.sql`이 함께 들어 있습니다. `sql/db2.sql`은 `src/test/resources/META-INF/testdata/dialect/db2.sql`과 같은 내용(`db_log` 테이블)입니다.

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovConnectionFactory` | Spring에서 설정한 `DataSource`를 `setDataSource()`로 주입받아 싱글톤으로 보관하고, Log4j 2 `JDBCAppender`의 `<ConnectionFactory method="getDatabaseConnection"/>`에서 호출할 정적 메서드 `getDatabaseConnection()`을 제공 |
| `EgovRequestIds` | `acceptOrGenerate(String)`: 전달된 요청 ID가 허용 형식(영숫자와 `. _ : -`, 1~128자, 패턴 `ALLOWED`)이면 그대로 승계하고, 아니면 새 UUID를 생성. `isAcceptable(String)`: 형식 검사만 수행 |
| `EgovResourceReleaser` | `close(Closeable...)`, `closeDBObjects(Wrapper...)`(`ResultSet`/`Statement`/`Connection`), `closeSocketObjects(Socket, ServerSocket)`, `closeSockets(Socket...)`: null 검사 후 닫고, 닫는 중 발생한 예외는 `EgovJdkLogger.ignore()`로 흡수 |
| `EgovJdkLogger` | `java.util.logging` 기반의 정적 유틸. `ignore()`(Level.OFF), `debug()`(Level.FINEST), `info()`(Level.INFO) 메서드 제공. 실행환경 내부에서 "무시해도 되는 예외"를 기록하는 용도 |

> 로그 출력 정책(Appender, Layout, Level, Filter)은 모듈 코드가 아니라 애플리케이션의 `log4j2.xml`로 제어합니다.

---

## 주요 의존성

| 라이브러리 | 버전 | scope / 비고 |
|---|---|---|
| `org.apache.logging.log4j:log4j-core` | 2.25.5 | compile. Log4j 2 구현체 (`log4j-api` 전이) |
| `org.apache.logging.log4j:log4j-slf4j2-impl` | 2.25.5 | compile. SLF4J 2.x → Log4j 2 바인딩 |
| `org.slf4j:jcl-over-slf4j` | 2.0.17 | compile. Commons Logging → SLF4J 브리지 |
| `org.slf4j:log4j-over-slf4j` | 2.0.17 | compile. Log4j 1.x API → SLF4J 브리지 |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `com.googlecode.log4jdbc:log4jdbc` | 1.2 | test (slf4j-api 제외) |
| `org.springframework:spring-context` / `spring-jdbc` / `spring-jms` / `spring-test` | 5.3.39-egov.1 | test |
| `org.hsqldb:hsqldb` (`jdk8`) / `commons-dbcp2` / `mysql-connector-j` | 2.7.3 / 2.13.0 / 8.4.0 | test |
| `aspectjweaver` / `aspectjrt` / `cglib` / `aopalliance` / `commons-lang3` | 1.9.23 / 1.9.23 / 3.3.0 / 1.0 / 3.20.0 | test (AOP 로깅 예제) |
| `junit` | 4.13.2 | test |

버전은 모두 루트 `pom.xml`의 `dependencyManagement`에서 관리하며 모듈 POM에는 선언하지 않습니다. `spring-jms`, `spring-test`는 `commons-logging`을 제외하여 `jcl-over-slf4j`와 충돌하지 않도록 합니다.

---

## 설정 및 사용 예시

### log4j2.xml (src/test/resources/log4j2.xml 발췌)

```xml
<Configuration status="error">
    <Appenders>
        <Console name="console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d %5p [%c] %m%n"/>
        </Console>
        <File name="file" fileName="./logs/file/sample.log" append="false">
            <PatternLayout pattern="%d %5p [%c] %m%n"/>
        </File>
        <!-- 크기 기준 롤링 -->
        <RollingFile name="rollingFile" fileName="./logs/rolling/rollingSample.log"
                     filePattern="./logs/rolling/rollingSample.%i.log">
            <PatternLayout><Pattern>%d %5p [%c] %m%n</Pattern></PatternLayout>
            <Policies><SizeBasedTriggeringPolicy size="1000"/></Policies>
            <DefaultRolloverStrategy max="3" fileIndex="min"/>
        </RollingFile>
        <!-- 시간 기준 롤링 -->
        <RollingFile name="dailyRollingFile" fileName="./logs/daily/dailyRollingSample.log"
                     filePattern="./logs/daily/dailyRollingSample.log.%d{yyyy-MM-dd-HH-mm-ss}">
            <PatternLayout><Pattern>%d %5p [%c] %m%n</Pattern></PatternLayout>
            <Policies><TimeBasedTriggeringPolicy/></Policies>
        </RollingFile>
    </Appenders>
    <Loggers>
        <Logger name="org.egovframe" level="debug" additivity="false">
            <AppenderRef ref="console"/>
        </Logger>
        <Root level="error">
            <AppenderRef ref="console"/>
        </Root>
    </Loggers>
</Configuration>
```

같은 파일에 `MarkerFilter`, `ThresholdFilter`, `DynamicThresholdFilter`, `RegexFilter`를 사용하는 Appender와 MDC 패턴(`%X{class} %X{method} %X{testKey}`) 예제, 주석 처리된 `JDBC`/`Socket`/`SMTP` Appender 예제가 있습니다.

### JDBC Appender와 EgovConnectionFactory

Spring 설정(`src/test/resources/META-INF/spring/context-connectionFactory.xml`)에서 `DataSource`를 주입하면 `JDBCAppender`가 정적 메서드로 커넥션을 얻을 수 있습니다.

```xml
<bean id="egovConnectionFactory" class="org.egovframe.rte.fdl.logging.db.EgovConnectionFactory">
    <property name="dataSource" ref="dataSource"/>
</bean>
```

```xml
<JDBC name="db" tableName="db_log">
    <ConnectionFactory class="org.egovframe.rte.fdl.logging.db.EgovConnectionFactory"
                       method="getDatabaseConnection"/>
    <Column name="eventDate" isEventTimestamp="true"/>
    <Column name="level" pattern="%p"/>
    <Column name="logger" pattern="%c"/>
    <Column name="message" pattern="%m"/>
    <Column name="exception" pattern="%ex{full}"/>
</JDBC>
```

`db_log` 테이블 생성 스크립트는 `sql/db2.sql`(DB2 기준, 미검증 안내 포함)을 참고합니다.

### 요청 식별자 검증 (EgovRequestIds Javadoc 예시)

```java
String requestId = EgovRequestIds.acceptOrGenerate(request.getHeader("X-Request-Id"));
MDC.put("requestId", requestId);
response.setHeader("X-Request-Id", requestId);
```

### SLF4J API 사용 (Slf4JLoggerTest)

```java
org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("org.egovframe");
logger.debug("Slf4jLoggerTest - {}", arg);   // log4j-slf4j2-impl을 통해 Log4j 2로 출력
```

### 테스트에서 다루는 주제

| 테스트 | 내용 |
|---|---|
| `LogTargetTest` | Console / File / RollingFile / 일자별 RollingFile Appender 출력 확인 |
| `LogLayoutTest`, `LogLevelTest`, `LoggerHierarchyTest`, `LoggerMatchingTest` | PatternLayout 파싱, 로거 레벨·상속·additivity, MDC 출력 |
| `Log4j2FilterTest`, `Log4j2FlowTracingTest`, `Log4j2ParameterizedTest` | Filter 4종, `entry()/exit()/catching()` 흐름 추적(Marker FLOW/EXCEPTION), 파라미터 치환 로깅 |
| `MethodLoggingTest` | Spring AOP(`context-aspect.xml`)로 서비스 메서드 호출 전 파라미터 로깅 |
| `Slf4JLoggerTest` | SLF4J API 경유 출력 |
| `EgovRequestIdsSecurityTest` | 개행·제어문자·과대 길이 요청 ID 차단 |

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Foundation/org.egovframe.rte.fdl.logging -am
```

## 테스트

```bash
mvn test -pl Foundation/org.egovframe.rte.fdl.logging
```

외부 서버는 필요하지 않습니다. `context-datasource.xml`은 HSQLDB 인메모리 DB(`jdbc:embedded-database type="HSQL"`)를 사용하며, 테스트 로그는 모듈 디렉터리의 `logs/` 아래에 생성됩니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.fdl.logging</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>`을 생략할 수 있습니다. `-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록이 필요합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.cmmn](../org.egovframe.rte.fdl.cmmn/README.md), [org.egovframe.rte.psl.dataaccess](../../Persistence/org.egovframe.rte.psl.dataaccess/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
