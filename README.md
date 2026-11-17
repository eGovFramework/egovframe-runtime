# 표준프레임워크 실행환경 4.3.1

[![eGovFrame](https://img.shields.io/badge/eGovFrame-4.3.1-134F8C?labelColor=white&logo=data:image/svg%2Bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxNzMuMjgyIDE3My4yODIiPjxwYXRoIGZpbGw9IiNmZmYiIGQ9Ik0xNzMuMjgyIDg2LjY1YzAgNDcuODQ0LTM4Ljc5NCA4Ni42MzItODYuNjQ2IDg2LjYzMkMzOC43OTkgMTczLjI4MiAwIDEzNC40OTQgMCA4Ni42NSAwIDM4Ljc4OCAzOC43OTkgMCA4Ni42MzYgMGM0Ny44NTIgMCA4Ni42NDYgMzguNzg4IDg2LjY0NiA4Ni42NSIvPjxwYXRoIGZpbGw9IiMwMDM3NjQiIGQ9Ik0xMjcuMzg5IDgwLjU5OGMtMTMuNzkxLTkuMzY1LTMxLjQzOS01LjU0Mi00MC43MTcgOC41MzMtNy43MiAxMS43NjctMTkuNDA1IDEzLjIzNS0yMy45MDYgMTMuMjM1LTE0Ljc1MSAwLTI0LjgyNC0xMC4zNjctMjcuODE4LTIxLjA5NWgtLjAxYy0uMDM5LS4xMDgtLjA1OS0uMi0uMDktLjMwNy0uMDI1LS4xMi0uMDU2LS4yMzMtLjA4OS0uMzY2LTEuMTc3LTQuNDY3LTEuNDY3LTYuNjA5LTEuNDY3LTExLjM2OCAwLTI1LjY1IDI2LjMyMS01NC4yMTMgNjQuMjE1LTU0LjIxMyAzOC44MjkgMCA2MS4wNSAyOS41NDUgNjYuNzggNDUuOTc5LS4xMDctLjI5NS0uMjA5LS41ODEtLjI5MS0uODc3LTExLjAxNS0zMi4xMjUtNDEuNDc0LTU1LjIxNi03Ny4zNTctNTUuMjE2LTQ1LjEzIDAtODEuNzI5IDM2LjU5LTgxLjcyOSA4MS43MzkgMCA0MC4zNTEgMjkuMTA4IDc0Ljg5MSA2OS40NzkgNzQuODkxIDMyLjE5NyAwIDUzLjg0Mi0xOC4wNTIgNjMuNzU3LTQyLjkzIDUuNDUtMTMuNjE0IDEuNTk1LTI5LjYwNS0xMC43NTctMzguMDA1Ii8%2BPHBhdGggZmlsbD0iI2U0MDMyZSIgZD0iTTE2NC43ODggNjIuNTg5Yy00Ljc3Ny0xNi4wMjYtMjcuMTUzLTQ3LjU3MS02Ny4yODItNDcuNTcxLTM3Ljg5NCAwLTY0LjIxNCAyOC41NjMtNjQuMjE0IDU0LjIxMiAwIDQuNzU5LjI5IDYuOTAxIDEuNDY2IDExLjM2OC0uNDg5LTEuOTUxLS43NC0zLjkwOC0uNzQtNS44MjMgMC0yNi43MjEgMjYuNzQxLTQ1LjIyNyA1NC4yNDgtNDUuMjI3IDM3LjIxOCAwIDY3LjM4OCAzMC4xNzQgNjcuMzg4IDY3LjM5IDAgMjkuMTczLTE2Ljc4NSA1NC40MzctNDEuMTc5IDY2LjU2NXYuMDIzYzMxLjQ1NS0xMS4zOTEgNTMuOTA4LTQxLjUxMiA1My45MDgtNzYuODg0IDAtOC4zNzgtMS4xMjctMTUuNzU5LTMuNTk1LTI0LjA1MyIvPjwvc3ZnPg%3D%3D)](https://www.egovframe.go.kr)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue?logo=apache&logoColor=white)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Java](https://img.shields.io/badge/Java-1.8-orange?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk8/)
[![Spring Framework](https://img.shields.io/badge/Spring%20Framework-5.3.39--egov.1-brightgreen?logo=spring&logoColor=white)](https://spring.io/projects/spring-framework)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

## 실행환경 소개

표준프레임워크 실행환경은 전자정부 사업에서 개발하는 업무 프로그램의 실행에 필요한 공통모듈 등 업무 프로그램 개발 시 화면, 서버 프로그램, 데이터 처리, 배치 처리 등의 필수적인 기능을 패턴화하여 미리 구현해 둔 라이브러리 코드 묶음임

### 실행환경 특징

- 실행환경 사용 시 [필수적인 서비스]들과 타 서비스에 [의존성이 높은 서비스]들을 [핵심 서비스]로 제공
- 실행환경 사용에 필수적이지 않은 서비스들은 [선택 서비스]로 분류하여 각 사업에서 선택적으로 적용할 수 있도록 제공
- 핵심 서비스는 기본적으로 적용되며 선택 서비스는 각 사업의 필요에 맞게 개발환경을 통해 선택적으로 적용 가능

### 실행환경 4.3.1 버전의 주요 특징

- Spring Boot 2.7.18 지원
- Spring Framework 5.3.39 버전으로 업그레이드 (보안 수정을 이식한 `5.3.39-egov.1` 포크 사용)
- Spring Security 5.8.16 버전으로 업그레이드 (`5.8.16-egov.1` 포크 사용)
- Spring Integration `5.5.20-egov.1`, Spring Data Commons `2.7.18-egov.1`, Spring Data MongoDB `3.4.18-egov.1` 포크 사용
- 취약점 대응을 위한 서드파티 라이브러리 버전 정비 (아래 [의존성 변경 내역](#의존성-변경-내역) 참고)
- 라이브러리·플러그인 버전을 루트 `pom.xml`의 `dependencyManagement`/`pluginManagement`에서 일괄 관리 (각 모듈 POM은 버전을 선언하지 않음)
- Spring Boot 자동구성 스타터 3종(`org.egovframe.boot` 그룹)을 루트 POM의 모듈로 포함하여 실행환경과 같은 4.3.1 버전으로 관리
- 적용 프로젝트가 상속하는 부모 POM 2종(Boot 기반, Servlet 기반) 제공
- `-egov.1` 포크 아티팩트는 표준프레임워크 Maven 저장소(`https://maven.egovframe.go.kr/maven/`)에서 받으며, 루트 POM에 저장소가 선언되어 있음

## 실행환경 구성

```
org.egovframe.rte
  ├─Batch
  │  └─org.egovframe.rte.bat.core
  ├─Boot
  │  ├─org.egovframe.access.spring.boot.starter
  │  ├─org.egovframe.crypto.spring.boot.starter
  │  └─org.egovframe.security.spring.boot.starter
  ├─Foundation
  │  ├─org.egovframe.rte.fdl.access
  │  ├─org.egovframe.rte.fdl.cmmn
  │  ├─org.egovframe.rte.fdl.crypto
  │  ├─org.egovframe.rte.fdl.excel
  │  ├─org.egovframe.rte.fdl.filehandling
  │  ├─org.egovframe.rte.fdl.idgnr
  │  ├─org.egovframe.rte.fdl.logging
  │  ├─org.egovframe.rte.fdl.property
  │  ├─org.egovframe.rte.fdl.reactive
  │  ├─org.egovframe.rte.fdl.security
  │  ├─org.egovframe.rte.fdl.string
  │  └─org.egovframe.rte.fdl.xml
  ├─Integration
  │  ├─org.egovframe.rte.itl.integration
  │  └─org.egovframe.rte.itl.webservice
  ├─Parent
  │  ├─org.egovframe.boot.starter.parent
  │  └─org.egovframe.web.config.parent
  ├─Persistence
  │  ├─org.egovframe.rte.psl.data.jpa
  │  ├─org.egovframe.rte.psl.data.mongodb
  │  ├─org.egovframe.rte.psl.dataaccess
  │  ├─org.egovframe.rte.psl.reactive.cassandra
  │  ├─org.egovframe.rte.psl.reactive.mongodb
  │  ├─org.egovframe.rte.psl.reactive.r2dbc
  │  └─org.egovframe.rte.psl.reactive.redis
  └─Presentation
     ├─org.egovframe.rte.ptl.mvc
     ├─org.egovframe.rte.ptl.reactive
     └─spring-modules-validation
```

루트 POM(`org.egovframe.rte:org.egovframe.rte.root:4.3.1`)이 위 30개 모듈을 빌드합니다.
`Boot`와 `Parent`를 제외한 모듈의 groupId는 `org.egovframe.rte`이며, `spring-modules-validation`의 버전은 `0.9`로 고정되어 있습니다.

### 실행환경 구성 설명

- [`org.egovframe.rte.bat.core`](Batch/org.egovframe.rte.bat.core/README.md) : 대용량 데이터 처리 지원을 위해 작업수행 및 결과 관리 및 스케줄링 관리 기능 제공
- [`org.egovframe.access.spring.boot.starter`](Boot/org.egovframe.access.spring.boot.starter/README.md) : Spring Boot 자동구성 기반 접근제어(Session 방식) 스타터 (`org.egovframe.boot` 그룹)
- [`org.egovframe.crypto.spring.boot.starter`](Boot/org.egovframe.crypto.spring.boot.starter/README.md) : Spring Boot 자동구성 기반 ARIA 암복호화 스타터 (`org.egovframe.boot` 그룹)
- [`org.egovframe.security.spring.boot.starter`](Boot/org.egovframe.security.spring.boot.starter/README.md) : Spring Boot 자동구성 기반 스프링 시큐리티 접근제어 스타터 (`org.egovframe.boot` 그룹)
- [`org.egovframe.rte.fdl.access`](Foundation/org.egovframe.rte.fdl.access/README.md) : Session 방식으로 접근제어 권한관리 설정을 간소화할 수 있는 방법 제공
- [`org.egovframe.rte.fdl.cmmn`](Foundation/org.egovframe.rte.fdl.cmmn/README.md) : 공통으로 사용되는 업무 흐름제어, 에러 처리 등의 기능 제공
- [`org.egovframe.rte.fdl.crypto`](Foundation/org.egovframe.rte.fdl.crypto/README.md) : ARIA 블록암호 알고리즘 기반 암복호화 설정을 간소화할 수 있는 방법 제공
- [`org.egovframe.rte.fdl.excel`](Foundation/org.egovframe.rte.fdl.excel/README.md) : 엑셀파일을 구성하거나 서버에 업로드·다운로드할 수 있는 기능 제공
- [`org.egovframe.rte.fdl.filehandling`](Foundation/org.egovframe.rte.fdl.filehandling/README.md) : 파일을 구성하거나 서버에 업로드·다운로드할 수 있는 기능 제공
- [`org.egovframe.rte.fdl.idgnr`](Foundation/org.egovframe.rte.fdl.idgnr/README.md) : 시스템을 개발할 때 필요한 유일한 ID를 생성할 수 있는 기능 제공
- [`org.egovframe.rte.fdl.logging`](Foundation/org.egovframe.rte.fdl.logging/README.md) : 로그 처리와 관련된 기능 제공
- [`org.egovframe.rte.fdl.property`](Foundation/org.egovframe.rte.fdl.property/README.md) : 설정을 구성하거나 관리하는 기능 제공
- [`org.egovframe.rte.fdl.reactive`](Foundation/org.egovframe.rte.fdl.reactive/README.md) : Spring WebFlux 구성과 관련된 기능 제공
- [`org.egovframe.rte.fdl.security`](Foundation/org.egovframe.rte.fdl.security/README.md) : 스프링 시큐리티를 기반으로 한 접근제어 권한관리 설정을 간소화할 수 있는 방법 제공
- [`org.egovframe.rte.fdl.string`](Foundation/org.egovframe.rte.fdl.string/README.md) : 시스템을 개발할 때 필요한 문자열 데이터를 다루기 위해 다양한 기능 제공
- [`org.egovframe.rte.fdl.xml`](Foundation/org.egovframe.rte.fdl.xml/README.md) : XML 파일을 다루기 위해 다양한 기능 제공
- [`org.egovframe.rte.itl.integration`](Integration/org.egovframe.rte.itl.integration/README.md) : 전자정부 표준프레임워크 기반의 시스템이 타 시스템과의 연계를 위해 사용하는 인터페이스 표준 정의
- [`org.egovframe.rte.itl.webservice`](Integration/org.egovframe.rte.itl.webservice/README.md) : 전자정부 표준프레임워크 Integration 서비스에 따라 웹서비스를 요청하고 제공하기 위한 라이브러리
- [`org.egovframe.boot.starter.parent`](Parent/org.egovframe.boot.starter.parent/README.md) : Spring Boot 2.7.18 기반 적용 프로젝트가 상속하는 부모 POM (`org.egovframe.boot` 그룹, 실행환경 루트 POM BOM import)
- [`org.egovframe.web.config.parent`](Parent/org.egovframe.web.config.parent/README.md) : Spring Boot를 사용하지 않는 웹 프로젝트(공통컴포넌트, 웹 템플릿 등)가 상속하는 부모 POM (`org.egovframe.web` 그룹, 실행환경 루트 POM BOM import)
- [`org.egovframe.rte.psl.data.jpa`](Persistence/org.egovframe.rte.psl.data.jpa/README.md) : Spring JPA 연동 예제 제공
- [`org.egovframe.rte.psl.data.mongodb`](Persistence/org.egovframe.rte.psl.data.mongodb/README.md) : Spring과 MongoDB 연동 예제 제공
- [`org.egovframe.rte.psl.dataaccess`](Persistence/org.egovframe.rte.psl.dataaccess/README.md) : 데이터베이스 연결, 데이터처리, 트랜잭션 관리 기능 제공
- [`org.egovframe.rte.psl.reactive.cassandra`](Persistence/org.egovframe.rte.psl.reactive.cassandra/README.md) : cassandra 연동(비동기) 기능 제공
- [`org.egovframe.rte.psl.reactive.mongodb`](Persistence/org.egovframe.rte.psl.reactive.mongodb/README.md) : mongodb 연동(비동기) 기능 제공
- [`org.egovframe.rte.psl.reactive.r2dbc`](Persistence/org.egovframe.rte.psl.reactive.r2dbc/README.md) : r2dbc 연동(h2, 비동기) 기능 제공
- [`org.egovframe.rte.psl.reactive.redis`](Persistence/org.egovframe.rte.psl.reactive.redis/README.md) : redis 연동(비동기) 기능 제공
- [`org.egovframe.rte.ptl.mvc`](Presentation/org.egovframe.rte.ptl.mvc/README.md) : 화면 처리를 위한 기능 제공
- [`org.egovframe.rte.ptl.reactive`](Presentation/org.egovframe.rte.ptl.reactive/README.md) : 커스텀 어노테이션, 예외처리, 유효성검증 기능 제공
- [`spring-modules-validation`](Presentation/spring-modules-validation/README.md) : Form 데이터의 유효성을 검증하기 위한 라이브러리

### 주요 의존성 버전

루트 `pom.xml` 기준입니다.

| 구분 | 버전 |
|---|---|
| Java | 1.8 (`java.version`), 컴파일 `release` 8 (`java.release.version`) |
| Spring Framework / Security / Integration / Batch | 5.3.39-egov.1 / 5.8.16-egov.1 / 5.5.20-egov.1 / 4.3.10 |
| Spring Boot (Boot Starter 모듈 전용) | 2.7.18 |
| Spring Data Commons | 2.7.18-egov.1 |
| Spring Data (JPA / MongoDB / Redis / Cassandra / R2DBC) | 2.7.18 / 3.4.18-egov.1 / 2.7.18 / 3.4.18 / 1.5.18 |
| Reactor Core / Reactive Streams | 3.4.41 / 1.0.4 |
| Hibernate / Validator | 5.6.15.Final / 6.2.5.Final |
| MyBatis / MyBatis-Spring / iBatis | 3.5.16 / 2.1.2 / 2.3.4.726 |
| Jackson / Netty | 2.18.11 / 4.1.138.Final |
| Log4j2 / SLF4J | 2.25.5 / 2.0.17 |
| Micrometer Core | 1.15.12 |
| Apache CXF / Jetty / Neethi | 3.5.11 / 9.4.58.v20250814 / 3.2.2 |
| Apache POI / JXLS / Commons Configuration2 | 5.4.0 / 2.14.0 / 2.15.0 |
| MongoDB Driver / Cassandra Driver / Lettuce / r2dbc-h2 | 4.11.5 / 4.17.0 / 6.5.5.RELEASE / 0.9.1.RELEASE |
| MySQL Connector/J | 8.4.0 (`com.mysql:mysql-connector-j`) |
| HSQLDB / PostgreSQL / H2 | 2.7.3 (`jdk8` 분류자) / 42.7.12 / 2.2.220 |
| Lombok / AspectJ | 1.18.36 / 1.9.23 |
| JUnit / AssertJ / EasyMock / PowerMock / DBUnit (test) | 4.13.2 / 3.27.7 / 5.2.0 / 2.0.9 / 2.7.3 |

MongoDB 드라이버(`mongodb-driver-core`, `mongodb-driver-sync`, `mongodb-driver-reactivestreams`)는 Java 17 전용(클래스 파일 major 61)인 `bson-record-codec`을 제외하고 관리합니다.

### 빌드 플러그인 버전

루트 `pom.xml`의 `pluginManagement` 기준입니다. 전체 모듈에 compiler, source, javadoc, surefire, jacoco 플러그인이 적용되어 빌드 시 sources·javadoc JAR이 함께 생성됩니다.

| 플러그인 | 버전 | 비고 |
|---|---|---|
| maven-compiler-plugin | 3.14.0 | `release` 8, `parameters` true, Lombok annotation processor |
| maven-source-plugin | 3.3.1 | `attach-sources` |
| maven-javadoc-plugin | 3.11.2 | `release` 8, `doclint` none, `attach-javadoc` |
| maven-surefire-plugin | 3.5.2 | `**/*Test.java` 포함, `**/Abstract*.java`·`**/*Suite.java` 제외 |
| jacoco-maven-plugin | 0.8.12 | `prepare-agent` |
| maven-deploy-plugin | 3.1.4 | 부모 POM 2종과 같은 버전 (`-DaltDeploymentRepository` 형식 통일) |
| maven-pmd-plugin | 3.26.0 | |
| maven-project-info-reports-plugin / maven-surefire-report-plugin / maven-jxr-plugin | 3.4.4 / 3.3.0 / 3.3.0 | `reporting` (`mvn site`) |

### 의존성 변경 내역

이전 배포본 대비 변경된 라이브러리입니다. 실행환경 루트 `pom.xml`의 `dependencyManagement`는 적용 프로젝트에 전달되지 않으므로,
전이 의존성으로 들어오는 라이브러리는 적용 프로젝트에서 `mvn dependency:tree`로 버전을 확인하십시오. 부모 POM(`Parent`)을 상속하면 같은 버전이 적용됩니다.

| 라이브러리 | 변경 전 | 변경 후 | 적용 위치 |
|---|---|---|---|
| Jackson (core / databind / annotations) | 2.18.3 | 2.18.11 | 루트 `jackson-bom` |
| Log4j2 (api / core / slf4j2-impl) | 2.24.3 | 2.25.5 | 루트 (`log4j-core`, `log4j-slf4j2-impl` 관리. `log4j-api`는 부모 POM에서 같은 버전으로 고정) |
| Commons Configuration2 | 2.11.0 | 2.15.0 | 루트 (`fdl.property`. Commons Text 1.15.0, Commons Logging 1.3.6 전이 상향) |
| Micrometer Core | 1.9.17 | 1.15.12 | 루트, `bat.core` (CVE-2026-40984, spring-batch-core 전이 의존성) |
| Spring Data Commons | 2.7.18 | 2.7.18-egov.1 | 루트 |
| Reactive Streams | 1.0.3 | 1.0.4 | 루트 (`psl.reactive.cassandra` 전이 의존성) |
| Jetty (server / http / io / util / security / continuation) | 9.4.57.v20241219 | 9.4.58.v20250814 | 루트 `jetty-bom` (`itl.webservice`) |
| Neethi | 3.2.1 | 3.2.2 | 루트 (`itl.webservice`) |
| JAXB Runtime / TXW2 / istack-commons-runtime | 2.3.1 / 2.3.1 / 3.0.7 | 2.3.5 / 2.3.5 / 3.0.12 | `itl.webservice` (cxf-core 전이 의존성) |
| Commons Logging (`fdl.excel`) | 1.3.4 | 1.3.5 | `fdl.excel` (commons-dbcp2 test 전이 의존성) |
| PostgreSQL JDBC | 42.3.1 | 42.7.12 | 루트 (모듈에서는 사용하지 않으며 BOM 전달용) |
| H2 (test) | 2.1.210 | 2.2.220 | 루트 (`psl.reactive.r2dbc`의 r2dbc-h2 전이 의존성) |
| c3p0 / mchange-commons-java (test) | 0.9.5.4 / 0.2.15 | 0.14.0 / 0.6.0 | 루트 (`bat.core`의 quartz 전이 의존성) |
| AssertJ (test) | 3.27.3 | 3.27.7 | 루트 (`psl.reactive.*`. Byte Buddy 1.18.3 전이 상향) |

빌드 설정 변경: `maven-compiler-plugin`의 `release` 값을 `${java.version}`(1.8)에서 `${java.release.version}`(8)으로 변경하였습니다.
`release` 옵션은 `1.8` 표기를 허용하지 않아 JDK 9 이상에서 빌드가 실패하던 문제를 수정한 것입니다. `maven-javadoc-plugin`도 같은 속성을 사용합니다.

### 부모 POM (Parent)

적용 프로젝트는 아래 부모 POM을 상속하여 실행환경과 같은 의존성 버전을 사용할 수 있습니다.
각 부모 POM의 상세 설명은 [Boot Starter Parent README](Parent/org.egovframe.boot.starter.parent/README.md)와
[Web Config Parent README](Parent/org.egovframe.web.config.parent/README.md)를 참고하십시오.

```xml
<!-- Spring Boot 2.7.18 기반 프로젝트 -->
<parent>
    <groupId>org.egovframe.boot</groupId>
    <artifactId>org.egovframe.boot.starter.parent</artifactId>
    <version>4.3.1</version>
</parent>

<!-- Spring Boot를 사용하지 않는 웹 프로젝트 -->
<parent>
    <groupId>org.egovframe.web</groupId>
    <artifactId>org.egovframe.web.config.parent</artifactId>
    <version>4.3.1</version>
</parent>
```

두 부모 POM은 실행환경 루트 POM(`org.egovframe.rte.root` 4.3.1)을 BOM으로 가져와(`scope=import`) 버전을 관리합니다.
실행환경 모듈 전체(Boot Starter 3종 포함)와 루트 POM이 관리하는 라이브러리(Spring 포크, Spring Data, Jackson, Log4j2, Netty, Jetty, DB 드라이버,
Apache Commons, 테스트 라이브러리 등)의 버전이 그대로 적용되며, 부모 POM에는 루트 POM이 관리하지 않는 라이브러리만 선언합니다.
따라서 부모 POM을 사용하려면 루트 POM(`org.egovframe.rte.root`)도 함께 저장소에 배포되어 있어야 합니다.
BOM import는 루트 POM의 `<repositories>` 설정을 상속하지 않으므로, 적용 프로젝트의 `pom.xml` 또는 `settings.xml`에
표준프레임워크 저장소(`https://maven.egovframe.go.kr/maven/`)를 등록해야 `-egov.1` 포크 아티팩트를 받을 수 있습니다.

| 구분 | `org.egovframe.boot.starter.parent` | `org.egovframe.web.config.parent` |
|---|---|---|
| 부모 | `spring-boot-starter-parent` 2.7.18 | 없음 |
| 실행환경 버전 | 루트 POM BOM import | 루트 POM BOM import |
| 추가 관리 | Spring Cloud 2021.0.9, QueryDSL 5.0.0, AspectJ Tools 1.9.7, Commons Logging 1.3.6, Log4j API 2.25.5 (Boot Starter 3종은 루트 POM BOM으로 4.3.1 관리) | 공통컴포넌트·웹 템플릿 전용 라이브러리(Spring Batch 4.3.10, Spring LDAP 2.4.1, MariaDB 3.4.0, javax API, OAuth/SNS, UI/에디터 등), JAXB Runtime 2.3.5, istack-commons-runtime 3.0.12, Commons Logging 1.3.6, xmlbeans 5.3.0, Log4j API 2.25.5 |
| Spring Boot 재정의 | 17개 속성 (아래 참고) | 해당 없음 |
| pluginManagement | `maven-deploy-plugin` 3.1.4 | compiler 3.13.0(`source`/`target` 1.8), war 3.4.0, pmd 3.23.0, surefire 3.3.0, javadoc 3.7.0, deploy 3.1.4 |

- `org.egovframe.boot.starter.parent` : 루트 BOM import는 Spring Boot가 가져오는 BOM(spring-framework-bom, jackson-bom, netty-bom 등)보다 우선하지만,
  `spring-boot-dependencies`에 직접 선언된 항목보다는 후순위입니다. 이 항목들은 같은 이름의 속성으로 실행환경과 같은 버전을 지정합니다.
  - 재정의 속성(17개): `aspectj`, `assertj`, `cassandra-driver`, `commons-codec`, `commons-dbcp2`, `commons-lang3`, `h2`, `jaxen`, `lettuce`, `lombok`,
    `mongodb`, `mysql`, `postgresql`, 그리고 실행환경 전이 의존성에 맞춘 `log4j2`, `logback`(1.2.13), `commons-pool2`(2.12.0), Servlet 기반 부모 POM과 같은 `mariadb`(3.4.0)
  - `aspectj.version`(1.9.23)은 `aspectjtools`(AspectJ 컴파일러)의 버전도 바꾸지만, `aspectjtools`는 1.9.8부터 Java 11, 1.9.21부터 Java 17 이상에서만 실행되므로
    Java 8에서 동작하는 1.9.7로 별도 지정합니다(`aspectjrt`, `aspectjweaver` 1.9.23은 Java 8 호환).
  - 단, SLF4J는 Spring Boot 관리 버전(1.7.36)을 유지합니다. Spring Boot 2.7의 기본 로깅(Logback 1.2)은 SLF4J 1.x 전용이며,
    2.x로 올리면 기동 시 `log4j-slf4j2-impl cannot be present with log4j-to-slf4j` 오류가 발생합니다.
- `org.egovframe.web.config.parent` : 부모 POM에 직접 선언한 항목이 BOM import보다 우선하므로, 실행환경과 같은 라이브러리를 다른 버전으로 선언하지 않습니다.
  JDK 8의 javac에는 `release` 옵션이 없으므로 compiler 플러그인에 `source`/`target` 1.8을 지정합니다.
- Log4j API : 루트 POM은 `log4j-core`와 `log4j-slf4j2-impl`(2.25.5)만 관리하고 `log4j-api`는 관리하지 않습니다. xmlbeans 5.3.0 등이 `log4j-api` 2.24.x를 전이시키면
  nearest-wins 규칙으로 core와 api 버전이 어긋날 수 있어, 두 부모 POM 모두 `log4j-api`를 2.25.5로 고정합니다.
  Boot 기반 부모 POM은 `log4j2.version`과 분리된 `log4j-api.version` 속성을 사용하므로, 적용 프로젝트가 `log4j2.version`을 바꿔도 core와 api가 어긋나지 않습니다.
- MySQL 드라이버는 루트 POM이 새 좌표 `com.mysql:mysql-connector-j`(8.4.0)만 관리합니다. 이전 좌표 `mysql:mysql-connector-java`는
  8.0.33으로 relocation되어 실행환경의 8.4.0보다 낮은 버전이 선택되므로, 이전 좌표를 사용하는 프로젝트는 새 좌표로 변경하십시오.
- Servlet 기반 부모 POM의 `xmlbeans`는 실행환경(POI 5.4.0)에 맞춰 5.3.0으로 관리합니다. xmlbeans 2.x API를 직접 사용하는 코드는 확인이 필요합니다.
- 4.3.1은 JDK 1.8 기반이므로 부모 POM이 관리하는 라이브러리는 Java 8에서 동작하는 버전(클래스 파일 major 52 이하)으로 정합니다.
  BOM에 함께 포함된 일부 선택 모듈(`log4j-jpl`, `jetty-alpn-java-*`, `spring-security-saml2-service-provider`, `bson-record-codec`)은 Java 9 이상 전용이며,
  Java 8 애플리케이션에서는 사용하지 않습니다.
- HSQLDB는 Java 8용 `jdk8` 분류자 항목을 루트 POM이 관리하며, Servlet 기반 부모 POM은 분류자 없는 항목(2.7.x는 Java 11용)을 관리하지 않습니다.
  `<classifier>jdk8</classifier>`를 지정하여 사용하십시오.
- MariaDB 드라이버(`org.mariadb.jdbc:mariadb-java-client` 3.4.0)는 공통컴포넌트 4.3.x의 MariaDB 프로파일에서 사용하므로 관리합니다.
- Servlet 기반 부모 POM의 버전 속성 이름은 기존 프로젝트 속성(`org.egovframe.rte.version`, `spring.maven.artifact.version`)과 겹치지 않습니다.
  기존 속성을 계속 참조하는 의존성은 이전 버전으로 남을 수 있으므로 전환 후 `mvn dependency:tree`로 확인하십시오.

## 실행환경 구동 방법

### Eclipse IDE

1. 개발환경 Eclipse IDE 를 실행함
2. Eclipse IDE 메뉴에서 File>Import… 를 클릭하여 프로젝트를 가져옴
3. 실행환경의 각 서비스는 모듈 형식으로 제공되므로 각 서비스별로 JUnit을 이용하여 구성한 테스트케이스를 통해 설정이나 기능을 점검함

### Maven

- 요구사항: JDK 8 이상, Maven 3.6 이상
- `-egov.1` 포크 아티팩트(Spring Framework, Spring Security, Spring Integration, Spring Data Commons, Spring Data MongoDB)는
  루트 POM에 선언된 표준프레임워크 저장소(`https://maven.egovframe.go.kr/maven/`)에서 받습니다.

```bash
# 전체 빌드 및 로컬 저장소 설치 (기본 goal이 install이므로 mvn clean 만 실행해도 install까지 수행)
mvn clean install

# 테스트 생략
mvn clean install -DskipTests

# 특정 모듈만 빌드
mvn clean install -pl Foundation/org.egovframe.rte.fdl.cmmn -am
```

빌드 결과물은 각 모듈의 `target/` 아래에 `<artifactId>-4.3.1.jar`, `-sources.jar`, `-javadoc.jar`로 생성됩니다.
부모 POM 2종(`Parent`)은 루트 POM과 함께 설치·배포되어야 적용 프로젝트에서 사용할 수 있습니다.

```bash
# 원격 저장소 배포 (maven-deploy-plugin 3.1.4 형식)
mvn deploy -DskipTests -DaltDeploymentRepository=<id>::<url>
```

테스트는 다음과 같이 실행합니다. surefire는 `*Test.java`만 실행하며 `Abstract*.java`, `*Suite.java`는 제외합니다.

```bash
mvn test
```

일부 모듈의 테스트는 로컬 서버가 필요합니다. 서버가 없으면 해당 모듈의 테스트가 접속 오류로 실패합니다.
접속 정보는 각 모듈의 `src/test/resources` 또는 테스트 설정 클래스에 있습니다.

| 모듈 | 필요 서버 | 접속 정보 |
|---|---|---|
| `fdl.access` | MySQL (`127.0.0.1:3306`) | DB `com`, 계정 `com` / `com01` (`META-INF/spring/test-jdbc.properties`) |
| `psl.data.mongodb`, `psl.reactive.mongodb` | MongoDB (`localhost:27017`) | DB `com`, 계정 `com` / `com01` |
| `psl.reactive.cassandra` | Cassandra (`localhost:9042`) | keyspace `com`, datacenter `datacenter1` |
| `psl.reactive.redis` | Redis (`localhost:6379`) | 비밀번호 `redis001` |

그 외 모듈의 테스트는 HSQLDB 인메모리 DB 등 내장 자원만 사용합니다.

## 참조

1. [Contribution 가이드](https://github.com/eGovFramework/egovframe-runtime/wiki)
2. [실행환경 위키가이드](https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3)
3. [개발환경 다운로드](https://www.egovframe.go.kr/home/sub.do?menuNo=94)
4. [실행환경 다운로드](https://www.egovframe.go.kr/home/sub.do?menuNo=92)
5. [Boot Starter Parent README](Parent/org.egovframe.boot.starter.parent/README.md)
6. [Web Config Parent README](Parent/org.egovframe.web.config.parent/README.md)

## 라이선스

전자정부 표준프레임워크는 [Apache 2.0](https://www.egovframe.go.kr/home/sub.do?menuNo=23) 라이선스를 따르고 있는 오픈소스 프로그램입니다.
