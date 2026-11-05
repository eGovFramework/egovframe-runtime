# org.egovframe.rte.fdl.cmmn

**Foundation 공통(Common) 모듈**은 업무 서비스 구현체가 상속하는 추상 클래스, 메시지 리소스 기반 예외 계층, AOP 기반 예외 후처리(`ExceptionTransfer`), 추적(Trace) 처리 기능을 제공합니다. `fdl.excel`, `fdl.filehandling`, `fdl.idgnr`, `fdl.property`, `fdl.string`, `fdl.xml`, `psl.dataaccess`, `ptl.mvc`, `itl.*` 등 대부분의 실행환경 모듈이 이 모듈(의 `EgovBizException`/`FdlException`)에 의존합니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.fdl.cmmn:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / AspectJ 1.9.23 / Commons Lang3 3.20.0
- **실행환경 의존**: `org.egovframe.rte.fdl.logging`

---

## 특징

- **서비스 추상화**: `EgovAbstractServiceImpl`을 상속하면 `egovLogger`, `processException()`, `leaveaTrace()`를 바로 사용
- **메시지 리소스 기반 예외**: `BaseException`, `BaseRuntimeException`, `EgovBizException`, `FdlException`이 `MessageSource` + 메시지 키 + 파라미터 + `Locale`로 메시지를 생성
- **AOP 예외 후처리**: `ExceptionTransfer.transfer()`를 `after-throwing` 어드바이스로 등록하면 예외 종류별로 로깅·후처리 핸들러 실행·재전파를 수행
- **패키지 패턴 기반 핸들러 연결**: `DefaultExceptionHandleManager`가 `AntPathMatcher` 패턴(`**exception.*` 등)으로 발생 위치(`클래스.메서드`)를 매칭하여 `ExceptionHandler.occur()` 호출. 발생 위치를 인자로 전달하므로 동시 요청 간 상태가 섞이지 않음
- **추적(Trace)**: `LeaveaTrace`가 `TraceHandlerService` 목록을 패턴 매칭하여 `TraceHandler.todo()` 실행
- **정보 노출 방지**: `DataAccessException`은 원본을 서버 로그에 남기고 일반화된 메시지(`A data access error occurred. See server log for details.`)로 재전파 (CWE-209 대응)
- 기본 메시지 번들 `message/message-common*.properties` 제공 (`fail.common.msg`, `fail.data.sql`, `info.nodata.msg`)

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/fdl/cmmn/
├── EgovAbstractServiceImpl.java          # 업무 서비스 추상 클래스
├── aspect/
│   └── ExceptionTransfer.java            # after-throwing 어드바이스
├── exception/
│   ├── BaseException.java
│   ├── BaseRuntimeException.java
│   ├── EgovBizException.java             # extends BaseException (업무 예외)
│   ├── FdlException.java                 # extends BaseException (실행환경 모듈 예외)
│   ├── handler/
│   │   └── ExceptionHandler.java         # occur(Exception, String packageName)
│   └── manager/
│       ├── AbstractExceptionHandleManager.java
│       ├── DefaultExceptionHandleManager.java
│       └── ExceptionHandlerService.java
└── trace/
    ├── LeaveaTrace.java
    ├── handler/
    │   ├── TraceHandler.java             # todo(Class<?>, String message)
    │   └── DefaultTraceHandler.java      # 예시 구현 (info 로그)
    └── manager/
        ├── AbstractTraceHandleManager.java
        ├── DefaultTraceHandleManager.java
        └── TraceHandlerService.java

src/main/resources/message/
├── message-common.properties
├── message-common_en.properties
└── message-common_ko.properties
```

---

## 주요 클래스

| 클래스 | 설명 |
|---|---|
| `EgovAbstractServiceImpl` | 추상 클래스. `protected Logger egovLogger`, `@Resource(name="messageSource") MessageSource`, `@Resource(name="leaveaTrace") LeaveaTrace`를 보유. `processException(msgKey[, msgArgs][, exception][, locale][, ExceptionCreator])`로 `EgovBizException` 생성, `leaveaTrace(msgKey[, msgArgs][, locale])`로 추적 실행 |
| `exception.BaseException` | `Exception` 상속. `messageKey`, `messageParameters`, `wrappedException`을 보관하고 `MessageSource`로 메시지 해석. 생성자 다수 (`(MessageSource, messageKey[, messageParameters][, defaultMessage][, locale][, wrappedException])`) |
| `exception.BaseRuntimeException` | `RuntimeException` 버전의 `BaseException` |
| `exception.EgovBizException` | 업무 예외. `BaseException` 상속, `ExceptionTransfer`에서 후처리 핸들러 실행 대상 |
| `exception.FdlException` | 실행환경 모듈(Foundation 등)에서 던지는 예외. `ExceptionTransfer`는 로깅만 하고 후처리 없이 재전파 |
| `exception.handler.ExceptionHandler` | `occur(Exception exception, String packageName)` 인터페이스. 후처리(메일 발송, 알림 등) 구현 지점 |
| `exception.manager.ExceptionHandlerService` | `setPatterns`, `setHandlers`, `setReqExpMatcher`, `run(Exception)`, `run(Exception, packageName)`(default 메서드), `hasReqExpMatcher` |
| `exception.manager.AbstractExceptionHandleManager` / `DefaultExceptionHandleManager` | 패턴 배열과 핸들러 배열을 받아, `PathMatcher`로 발생 위치가 매칭되는 첫 패턴의 핸들러들을 실행 |
| `aspect.ExceptionTransfer` | `transfer(JoinPoint, Exception)`: `EgovBizException` → 로깅·핸들러 실행 후 재전파 / `RuntimeException` → 로깅·핸들러 실행 후 재전파(`DataAccessException`은 메시지 일반화) / `FdlException` → 로깅 후 재전파 / 그 외 → `fail.common.msg` 메시지의 `BaseException`으로 변환. `exceptionHandlerService` 프로퍼티로 `ExceptionHandlerService[]` 주입, 기본 매처 `AntPathMatcher` |
| `trace.LeaveaTrace` | `trace(msgKey[, msgArgs][, locale], Class<?>)` 등으로 메시지를 해석해 `TraceHandlerService[]`에 전달 (`traceHandlerServices` 프로퍼티) |
| `trace.handler.TraceHandler` / `DefaultTraceHandler` | `todo(Class<?> clazz, String message)`. 기본 구현은 실행 여부만 info 로그로 남기는 예시 구현이며 실제 처리는 사용자 구현체에서 수행 |
| `trace.manager.TraceHandlerService` / `AbstractTraceHandleManager` / `DefaultTraceHandleManager` | 패턴·핸들러·`PathMatcher`를 가지고 `trace(Class<?>, String)` 수행 |

---

## 주요 의존성

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.logging` | 4.3.1 | compile |
| `spring-context-support` (commons-logging 제외), `spring-tx` | 5.3.39-egov.1 | compile |
| `org.apache.commons:commons-lang3` | 3.20.0 | compile |
| `org.aspectj:aspectjweaver`, `aspectjrt` | 1.9.23 | compile |
| `spring-jdbc`, `spring-test`, `spring-webmvc` | 5.3.39-egov.1 | test |
| `javax.servlet-api` / `javax.mail:mail` | 4.0.1 / 1.5.0-b01 | test |
| `commons-dbcp2` / `hsqldb`(`jdk8`) / `ehcache` | 2.13.0 / 2.7.3 / 2.10.9.2 | test |
| `junit` / `easymock` | 4.13.2 / 5.2.0 | test |

버전은 루트 POM(`org.egovframe.rte.root` 4.3.1)의 `dependencyManagement`에서 관리하며 모듈 POM은 버전을 선언하지 않습니다.

---

## 설정 및 사용 예시

### 1) 메시지 소스와 LeaveaTrace (`src/test/resources/spring/context-common.xml`)

```xml
<bean id="messageSource" class="org.springframework.context.support.ReloadableResourceBundleMessageSource">
    <property name="basenames">
        <list>
            <value>classpath:/message/message-common</value>
        </list>
    </property>
    <property name="cacheSeconds" value="60"/>
</bean>

<bean id="leaveaTrace" class="org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace">
    <property name="traceHandlerServices">
        <list>
            <ref bean="traceHandlerService"/>
        </list>
    </property>
</bean>

<bean id="traceHandlerService" class="org.egovframe.rte.fdl.cmmn.trace.manager.DefaultTraceHandleManager">
    <property name="patterns">
        <list><value>*</value></list>
    </property>
    <property name="handlers">
        <list><ref bean="defaultTraceHandler"/></list>
    </property>
    <property name="reqExpMatcher" ref="antPathMater"/>
</bean>

<bean id="antPathMater" class="org.springframework.util.AntPathMatcher"/>
<bean id="defaultTraceHandler" class="org.egovframe.rte.fdl.cmmn.trace.handler.DefaultTraceHandler"/>
```

`EgovAbstractServiceImpl`은 Bean 이름 `messageSource`, `leaveaTrace`를 `@Resource`로 주입받으므로 두 Bean은 이 이름으로 정의해야 합니다.

### 2) 예외 후처리 AOP (`src/test/resources/spring/context-aspect.xml`)

```xml
<aop:config>
    <aop:pointcut id="serviceMethod" expression="execution(* org.egovframe.rte.bsl..*(..))"/>
    <aop:aspect ref="exceptionTransfer">
        <aop:after-throwing throwing="exception" pointcut-ref="serviceMethod" method="transfer"/>
    </aop:aspect>
</aop:config>

<bean id="exceptionTransfer" class="org.egovframe.rte.fdl.cmmn.aspect.ExceptionTransfer">
    <property name="exceptionHandlerService">
        <list>
            <ref bean="defaultExceptionHandleManager"/>
        </list>
    </property>
</bean>

<bean id="defaultExceptionHandleManager"
      class="org.egovframe.rte.fdl.cmmn.exception.manager.DefaultExceptionHandleManager">
    <property name="patterns">
        <list><value>**exception.*</value></list>
    </property>
    <property name="handlers">
        <list><ref bean="egovHandler"/></list>
    </property>
</bean>

<bean id="egovHandler" class="org.egovframe.rte.fdl.exception.handler.EgovServiceExceptionHandler"/>
```

`patterns`는 발생 위치 문자열(`클래스 canonical name + "." + 메서드명`)과 Ant 패턴으로 비교됩니다. `egovHandler`는 `ExceptionHandler`를 구현한 사용자 클래스(테스트에서는 메일 발송 예시)입니다.

### 3) 업무 서비스 구현

```java
public class SampleServiceImpl extends EgovAbstractServiceImpl implements SampleService {

    public void doSomething(String id) throws Exception {
        if (id == null) {
            // 메시지 키 + 파라미터로 EgovBizException 생성 (현재 LocaleContextHolder 로케일 사용)
            throw processException("info.nodata.msg", new String[]{id});
        }
        leaveaTrace("message.exception.test");   // TraceHandler 실행
        egovLogger.debug("done");
    }
}
```

### 4) ExceptionHandler 구현

```java
public class SampleExceptionHandler implements ExceptionHandler {
    @Override
    public void occur(Exception exception, String packageName) {
        // packageName: 예외가 발생한 "클래스.메서드"
    }
}
```

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Foundation/org.egovframe.rte.fdl.cmmn -am
```

## 테스트

```bash
mvn test -pl Foundation/org.egovframe.rte.fdl.cmmn
```

테스트는 HSQLDB 인메모리(`spring/schema.sql`, `spring/test-data.sql`), Ehcache, ConcurrentMap 캐시 등 내장 자원만 사용하며 외부 서버가 필요 없습니다. `context-aspect.xml`의 `SimpleSSLMail` Bean은 테스트용 예시 설정으로, 실제 메일 발송 실패는 핸들러 내부에서 처리됩니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.fdl.cmmn</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>`을 생략할 수 있습니다. `-egov.1` 포크 아티팩트(Spring Framework 등)를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록이 필요합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.logging](../org.egovframe.rte.fdl.logging/README.md), [org.egovframe.rte.psl.dataaccess](../../Persistence/org.egovframe.rte.psl.dataaccess/README.md), [org.egovframe.rte.ptl.mvc](../../Presentation/org.egovframe.rte.ptl.mvc/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
