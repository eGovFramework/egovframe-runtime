# org.egovframe.rte.fdl.property

**Foundation Layer 프로퍼티(Property) 모듈**입니다. **Apache Commons Configuration 2**의 `PropertiesConfiguration`을 기반으로 여러 외부 `.properties` 파일과 Spring XML에 직접 선언한 키/값을 하나의 서비스(`EgovPropertyService`)로 통합하여 타입별(`String`/`int`/`long`/`boolean` 등) 조회 기능을 제공하고, 필요 시 DB 테이블의 키/값을 Spring `Environment`의 `PropertySource`로 등록하는 기능을 제공합니다. `fdl.crypto`, `fdl.access` 모듈이 이 모듈에 의존합니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.fdl.property:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Commons Configuration2 2.15.0
- **실행환경 의존**: `org.egovframe.rte.fdl.cmmn` (메시지 기반 예외 `FdlException` 등)

---

## 특징

- **다중 프로퍼티 소스 통합**: `extFileName`에 지정한 여러 파일(`classpath:`, `classpath*:`, `file:` 와일드카드 패턴 지원)과 `properties` 맵의 값을 하나의 `PropertiesConfiguration`으로 병합합니다.
- **파일별 인코딩 지정**: 파일 항목을 `<map>`(`encoding`, `filename`)으로 선언하면 UTF-8 등 인코딩을 개별 지정할 수 있어 한글 프로퍼티를 안전하게 읽습니다(미지정 시 `PropertiesConfiguration.DEFAULT_ENCODING`).
- **타입 변환 조회**: `getString/getInt/getLong/getFloat/getDouble/getBoolean/getStringArray`와 기본값 오버로드, `getKeys(prefix)`를 제공합니다.
- **런타임 갱신**: `refreshPropertyFiles()`로 외부 파일을 다시 읽어 변경된 값을 반영합니다.
- **DB 기반 PropertySource**: `DbPropertySource`가 `SELECT PKEY, PVALUE FROM ...` 결과를 `Environment`의 `PropertySource`로 제공하며, 웹 환경에서는 `DBPropertySourceInitializer`(`ApplicationContextInitializer`)가 `web.xml` 설정만으로 이를 최우선 소스로 등록합니다.
- **국제화 메시지**: 초기화·갱신 오류 메시지를 `messages/properties*.properties`(ko/en)로 제공합니다.

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/fdl/property/
├── EgovPropertyService.java                 # 프로퍼티 조회 서비스 인터페이스
├── impl/
│   └── EgovPropertyServiceImpl.java         # Commons Configuration2 기반 구현
└── db/
    ├── DbPropertySource.java                # Spring PropertySource 구현 (DB)
    ├── DbPropertySourceDelegate.java        # JdbcTemplate로 PKEY/PVALUE 적재
    └── initializer/
        └── DBPropertySourceInitializer.java # 웹 컨텍스트 초기화 시 DB PropertySource 등록

src/main/resources/org/egovframe/rte/fdl/property/messages/
├── properties.properties                    # 오류·디버그 메시지 (기본, 한국어)
├── properties_ko.properties
└── properties_en.properties
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovPropertyService` | 애플리케이션에서 유일한 키로 값을 조회하는 서비스 인터페이스. `getBoolean/getDouble/getFloat/getInt/getLong/getString`(기본값 오버로드 포함), `getStringArray`, `getKeys()`, `getKeys(prefix)`, `refreshPropertyFiles()` |
| `EgovPropertyServiceImpl` | `InitializingBean`/`DisposableBean`/`ResourceLoaderAware`/`ApplicationContextAware` 구현. `afterPropertiesSet()`에서 `extFileName`의 파일들을 `ResourcePatternResolver`로 찾아 읽고 `properties` 맵을 `addProperty()`로 추가. 키가 비어 있으면 `FdlException`(`error.properties.check.essential`) 발생 |
| `DbPropertySource` | `org.springframework.core.env.PropertySource<DbPropertySourceDelegate>` 구현. 생성자 `(name, DataSource, sql)`로 선언하며 `getProperty(key)`를 위임 |
| `DbPropertySourceDelegate` | 생성 시 `JdbcTemplate.queryForList(sql)`를 실행하여 컬럼 `PKEY`/`PVALUE`(상수 `PROPERTY_SOURCE_KEY`, `PROPERTY_SOURCE_VALUE`)를 `Map`에 적재 |
| `DBPropertySourceInitializer` | `ApplicationContextInitializer<ConfigurableApplicationContext>` 구현. 서블릿 컨텍스트 파라미터 `propertySourceConfigLocation`의 XML에서 `DbPropertySource` 빈을 꺼내 `Environment`의 `MutablePropertySources`에 `addFirst()`로 등록 |

---

## 주요 의존성

| 라이브러리 | 버전 | scope / 비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.cmmn` | 4.3.1 | compile. `FdlException`, 로깅 의존성 전이 |
| `org.apache.commons:commons-configuration2` | 2.15.0 | compile. `PropertiesConfiguration` (Commons Text 1.15.0, Commons Logging 1.3.6 전이) |
| `org.springframework:spring-web` | 5.3.39-egov.1 | compile (commons-logging 제외). `WebApplicationContext` 사용 |
| `org.springframework:spring-jdbc` | 5.3.39-egov.1 | compile. `JdbcTemplate` |
| `javax.servlet:javax.servlet-api` | 4.0.1 | provided |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `org.springframework:spring-test`, `junit`, `easymock` | 5.3.39-egov.1 / 4.13.2 / 5.2.0 | test |

버전은 모두 루트 `pom.xml`의 `dependencyManagement`에서 관리하며 모듈 POM에는 선언하지 않습니다.

---

## 설정 및 사용 예시

### 프로퍼티 서비스 빈 설정 (src/test/resources/spring/context-properties.xml)

```xml
<bean name="propertyService" class="org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl"
      destroy-method="destroy">
    <!-- 외부 프로퍼티 파일: 문자열 또는 encoding/filename 맵 -->
    <property name="extFileName">
        <set>
            <map>
                <entry key="encoding" value="UTF-8"/>
                <entry key="filename" value="file:./src/**/refresh-resource.properties"/>
            </map>
            <value>file:./src/**/resource.properties</value>
            <map>
                <entry key="encoding" value="UTF-8"/>
                <entry key="filename" value="classpath:properties/korean-resource.properties"/>
            </map>
        </set>
    </property>
    <!-- XML에 직접 정의하는 키/값 -->
    <property name="properties">
        <map>
            <entry key="AAAA" value="1234"/>
            <entry key="bbbb" value="4567"/>
            <entry key="cccc" value="가나다"/>
        </map>
    </property>
</bean>
```

`FdlException` 메시지를 사용하려면 `messageSource` 빈이 필요합니다 (`src/test/resources/spring/context-common.xml`).

```xml
<bean name="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
    <property name="useCodeAsDefaultMessage" value="true"/>
    <property name="basenames">
        <list>
            <value>classpath:/org/egovframe/rte/fdl/property/messages/properties</value>
        </list>
    </property>
</bean>
```

### 서비스 사용 (PropertyServiceBasicTest)

```java
@Resource(name = "propertyService")
EgovPropertyService propertyService;

propertyService.getString("greet.message");            // "안녕하세요."
propertyService.getInt("number.int");                   // 1234
propertyService.getInt("number.int", 123);              // 기본값 오버로드
propertyService.getStringArray("tokens_on_a_line");     // "first token, second token" → 2개
propertyService.getString("AAAA");                      // XML <map> 값 "1234"
propertyService.refreshPropertyFiles();                 // 외부 파일 재적재
```

### DB 기반 PropertySource (src/test/java/.../db/ReadMe, propertysource-context.xml, web.xml)

1. 키/값 테이블을 준비합니다. 컬럼명은 `PKEY`, `PVALUE`를 사용합니다 (`db/ddl.sql`, `db/dml.sql`).

```sql
CREATE TABLE PROPERTY (
    PKEY   VARCHAR(20) NOT NULL PRIMARY KEY,
    PVALUE VARCHAR(20) NOT NULL
);
INSERT INTO PROPERTY (PKEY, PVALUE) VALUES ('egov.test.sample01', 'sample01');
```

2. PropertySource 전용 XML에 `DbPropertySource` 빈을 선언합니다.

```xml
<jdbc:embedded-database id="dataSource" type="HSQL">
    <jdbc:script location="classpath:db/ddl.sql"/>
    <jdbc:script location="classpath:db/dml.sql"/>
</jdbc:embedded-database>

<bean id="dbPropertySource" class="org.egovframe.rte.fdl.property.db.DbPropertySource">
    <constructor-arg value="dbPropertySource"/>
    <constructor-arg ref="dataSource"/>
    <constructor-arg value="SELECT PKEY, PVALUE FROM PROPERTY"/>
</bean>
```

3. `web.xml`에 초기화 클래스와 XML 위치를 등록합니다.

```xml
<context-param>
    <param-name>contextInitializerClasses</param-name>
    <param-value>org.egovframe.rte.fdl.property.db.initializer.DBPropertySourceInitializer</param-value>
</context-param>
<context-param>
    <param-name>propertySourceConfigLocation</param-name>
    <param-value>classpath:/META-INF/spring/propertysource-context.xml</param-value>
</context-param>
```

4. 이후 XML의 `${egov.test.sample01}` 플레이스홀더(`<context:property-placeholder/>`)나 `Environment.getProperty("egov.test.sample01")`로 값을 읽습니다.

### Spring @PropertySource (PropertySourceAnnotationTests)

이 모듈 없이도 Spring 자체의 `@PropertySource("classpath:properties/p1.properties")`로 `Environment`에 파일을 등록할 수 있으며, 테스트에 그 예제(`p1.properties`, `p2.properties`, LIFO 우선순위)가 포함되어 있습니다.

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Foundation/org.egovframe.rte.fdl.property -am
```

## 테스트

```bash
mvn test -pl Foundation/org.egovframe.rte.fdl.property
```

외부 서버는 필요하지 않습니다. 테스트는 `src/test/resources/properties/*.properties`와 HSQLDB 인메모리 DB만 사용합니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.fdl.property</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>`을 생략할 수 있습니다. `-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록이 필요합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.cmmn](../org.egovframe.rte.fdl.cmmn/README.md), [org.egovframe.rte.fdl.crypto](../org.egovframe.rte.fdl.crypto/README.md), [org.egovframe.rte.fdl.access](../org.egovframe.rte.fdl.access/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
