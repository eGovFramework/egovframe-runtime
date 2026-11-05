# 전자정부 표준프레임워크 Web Config Parent

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-1.8-orange.svg)](https://www.oracle.com/java/)
[![eGovFrame](https://img.shields.io/badge/eGovFrame-4.3.1-green.svg)](https://www.egovframe.go.kr)
[![Spring Framework](https://img.shields.io/badge/Spring%20Framework-5.3.39--egov.1-brightgreen.svg)](https://spring.io)

## 📋 개요

Spring Boot를 사용하지 않는 웹 프로젝트(공통컴포넌트, 웹 템플릿 등)용 Maven Parent POM입니다. 5.x의 `egovframe-web-config-parent`에 대응합니다.
실행환경 루트 POM(`org.egovframe.rte.root` 4.3.1)을 BOM으로 가져오고, 루트 POM이 관리하지 않는 웹 프로젝트용 라이브러리만 추가로 관리합니다.

| 항목 | 값 |
|------|-----|
| `groupId` | `org.egovframe.web` |
| `artifactId` | `org.egovframe.web.config.parent` |
| `version` | `4.3.1` |
| `packaging` | `pom` |
| 상위 POM | 없음 |

## ✨ 주요 특징

- **Java 1.8** — 관리 라이브러리는 Java 8에서 동작하는 버전(class 52 이하)
- **실행환경 4.3.1 BOM import** — RTE 모듈 전체, Spring 5.3.39-egov.1 / Security 5.8.16-egov.1, Spring Data, Jackson, Log4j2/SLF4J, Netty, Jetty, DB 드라이버, Apache Commons, 테스트 라이브러리 등
- **웹 프로젝트 추가 라이브러리** — Spring Batch·LDAP, javax API, MariaDB, OAuth/SNS, UI/에디터, XML 유틸 등
- **pluginManagement** — Compiler(`source`/`target` 1.8), WAR, PMD, Surefire, Javadoc, Deploy

## 🚀 시작하기

### 요구사항

- Java 8 이상
- Maven 3.6 이상
- 표준프레임워크 저장소 `https://maven.egovframe.go.kr/maven/` (`-egov.1` Spring 아티팩트)

> ⚠️ 이 POM은 `<repositories>`를 선언하지 않으므로, 프로젝트 `pom.xml` 또는 `settings.xml`에 표준프레임워크 저장소를 등록하세요.
> 실행환경 4.3.1이 저장소에 없으면 소스에서 먼저 설치합니다.
>
> ```bash
> mvn -f egovframe-runtime-4.3.1/pom.xml install -DskipTests
> ```

### 사용 방법

```xml
<parent>
    <groupId>org.egovframe.web</groupId>
    <artifactId>org.egovframe.web.config.parent</artifactId>
    <version>4.3.1</version>
</parent>

<repositories>
    <repository>
        <id>egovframe</id>
        <url>https://maven.egovframe.go.kr/maven/</url>
    </repository>
</repositories>

<dependencies>
    <!-- 버전 생략 가능 -->
    <dependency>
        <groupId>org.egovframe.rte</groupId>
        <artifactId>org.egovframe.rte.ptl.mvc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.egovframe.rte</groupId>
        <artifactId>org.egovframe.rte.psl.dataaccess</artifactId>
    </dependency>
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

`dependencyManagement`만 필요하면 `<type>pom</type>`, `<scope>import</scope>`로 가져올 수도 있습니다(이 경우 pluginManagement는 적용되지 않음).

### 기존 프로젝트 전환 시 확인 사항

- 버전 속성 이름은 기존 프로젝트 속성(`org.egovframe.rte.version`, `spring.maven.artifact.version`)과 겹치지 않습니다. 기존 속성을 계속 참조하는 의존성은 이전 버전으로 남을 수 있으므로 `mvn dependency:tree`로 확인하세요.
- MySQL 드라이버는 `com.mysql:mysql-connector-j`를 사용하세요. 구 좌표 `mysql:mysql-connector-java`는 8.0.33으로 고정됩니다.
- HSQLDB 2.7.x 기본 JAR은 Java 11용이므로 `<classifier>jdk8</classifier>`를 지정하세요.

## 📦 관리 의존성 (`dependencyManagement`)

### 실행환경 BOM

`org.egovframe.rte:org.egovframe.rte.root:4.3.1` (import) — RTE 모듈(`org.egovframe.rte.*` 24종 + `spring-modules-validation`)과 실행환경 관리 라이브러리.
상세 버전은 [실행환경 루트 POM](../../pom.xml)을 참고하세요.

### 이 POM에서 추가 관리

| 구분 | 아티팩트 | 버전 |
|------|----------|------|
| Spring | `spring-batch-core`, `spring-batch-infrastructure` | 4.3.10 |
| | `spring-ldap-core` | 2.4.1 |
| Java EE (javax) | `javax.websocket-api` | 1.1 |
| | `javax.faces-api` | 2.3 |
| | `javax.inject` | 1 |
| | `org.glassfish:javax.json` | 1.1.4 |
| | `yasson` | 1.0.2 |
| Jackson 1.x | `jackson-mapper-asl` (EOL) | 1.9.13 |
| Apache Commons | `commons-compress` | 1.27.1 |
| | `commons-net` | 3.11.1 |
| | `commons-email` | 1.6.0 |
| | `commons-fileupload` | 1.5 |
| Database | `mariadb-java-client` | 3.4.0 |
| 실행환경 전이 의존성 | `jaxb-runtime`, `txw2` | 2.3.5 |
| | `istack-commons-runtime` | 3.0.12 |
| | `commons-logging` | 1.3.6 |
| | `xmlbeans` | 5.3.0 |
| | `log4j-api` | 2.25.5 |
| XML, 유틸 | `xercesImpl` | 2.12.2 |
| | `quartz-jobs` | 2.3.2 |
| | `oro` | 2.0.8 |
| | `icu4j` | 75.1 |
| | `javaparser-core` | 3.26.1 |
| UI, 에디터, 변환 | `ckeditor-java-core` | 3.5.3 |
| | `batik-ext` | 1.17 |
| | `ajaxtags-resources` | 1.5.7 |
| | `jodconverter` | 2.2.1 |
| OAuth, SNS, LDAP, EMS | `scribejava-apis`, `scribejava-core` | 8.3.3 |
| | `twitter4j-core` | 4.0.7 |
| | `jp.takke.twitter4j-v2` | 1.4.2 |
| | `ldapsdk` | 4.1 |
| | `sndng-mail` | 1.0 |
| Test | `junit-jupiter-api` | 5.11.0 |
| | `selenium-java` | 4.13.0 |

> Altibase·CUBRID·Tibero·Oracle 드라이버는 관리하지 않습니다. 필요 시 프로젝트에서 직접 선언하세요.

## 🔧 `pluginManagement`

| 플러그인 | 버전 | 설정 |
|----------|------|------|
| maven-compiler-plugin | 3.13.0 | `source`/`target` 1.8, UTF-8 |
| maven-war-plugin | 3.4.0 | - |
| maven-pmd-plugin | 3.23.0 | - |
| maven-surefire-plugin | 3.3.0 | - |
| maven-javadoc-plugin | 3.7.0 | - |
| maven-deploy-plugin | 3.1.4 | 실행환경과 동일 |

> JDK 8 javac에는 `release` 옵션이 없으므로 `source`/`target`을 사용합니다.

## 🔄 5.x와의 차이

| 구분 | 4.3.1 (본 POM) | 5.x |
|------|----------------|-----|
| Java | 1.8 | 17 |
| Spring Framework | 5.3.39-egov.1 | 6.2.x |
| Servlet API | `javax.*` | `jakarta.*` |
| artifactId 형식 | `org.egovframe.web.config.parent`, `org.egovframe.rte.fdl.cmmn` | `egovframe-web-config-parent`, `egovframe-rte-fdl-cmmn` |

## 🔧 Boot Starter Parent와의 관계

| 구분 | [Boot Starter Parent](../org.egovframe.boot.starter.parent) | Web Config Parent (본 POM) |
|------|---------------------|----------------------------|
| 대상 | Spring Boot 애플리케이션 | Boot 미사용 웹(WAR) 애플리케이션 |
| 상위 POM | `spring-boot-starter-parent` 2.7.18 | 없음 |
| 공통 | 실행환경 4.3.1 BOM import | 실행환경 4.3.1 BOM import |

## 📚 참고 자료

- [전자정부 표준프레임워크](https://www.egovframe.go.kr)

## 📄 라이선스

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
