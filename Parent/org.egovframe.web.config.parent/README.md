# 전자정부 표준프레임워크 Web Config Parent (Maven BOM)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![eGovFrame](https://img.shields.io/badge/eGovFrame-5.1.0-green.svg)](https://www.egovframe.go.kr)
[![Spring Framework](https://img.shields.io/badge/Spring%20Framework-6.2.19-brightgreen.svg)](https://spring.io)

## 📋 개요

**egovframe-web-config-parent**는 전자정부 표준프레임워크(The eGovernment Standard Framework) 기반 **웹(MVC)** 프로젝트용 Maven **BOM**(Bill of Materials)입니다.

| 항목 | 값 |
|------|-----|
| `groupId` | `org.egovframe.web` |
| `artifactId` | `egovframe-web-config-parent` |
| `version` | `5.1.0` |
| `packaging` | `pom` |
| 공식 사이트 | [egovframe.go.kr](https://www.egovframe.go.kr) |

`dependencyManagement`에 **import** 하여 RTE·Jakarta·Spring·DB·유틸 등 버전을 한곳에서 맞출 수 있습니다.

**Spring Boot 기반** 프로젝트는 **egovframe-boot-starter-parent**를 parent로 쓰는 것을 권장합니다.

## ✨ 주요 특징

- **Maven BOM** – `dependencyManagement` import로 버전 통합 관리
- **eGovFrame RTE 5.1.0** – 배치·FDL·ITL·PSL·PTL 전 모듈 버전 관리
- **Java 17** (`maven-compiler-plugin` `release` 17), 인코딩 UTF-8
- **Spring Framework 6.2.19** – `egovframe-rte-root` 5.1.0 BOM import로 Spring Security(6.5.11-egov.1)·Batch(5.2.6)·Data(2025.0.13)·Integration(6.5.10-egov.1)·Micrometer·Reactor 등을 함께 관리
- **Jakarta EE 9** – Servlet 5.0, JSP 3.0, EL 4.0, JSTL 2.0, Validation 3.0, WebSocket 2.0, JSON-P 2.0/JSON-B 2.0, Mail 2.0, JAXB 3.0, Faces 3.0 등 (Tomcat 10.0 등 Servlet 5.0 컨테이너 기준)
- **DB 드라이버** – MySQL Connector/J, MariaDB, PostgreSQL, HSQLDB (본 BOM에 명시된 범위)
- **Spring Boot 비의존** – Spring Boot·Spring Cloud 및 Boot 스타터/autoconfigure 아티팩트는 관리 대상에서 제외
- Jackson, **SLF4J + Log4j2**, Apache Commons, HTTP Client 5, JAX-WS 등
- **Spring AI** – `1.1.8` (Boot 비의존 아티팩트만 개별 관리, `.m2`에 egov 빌드가 있는 `spring-ai-commons`·`model`·`pdf-document-reader`·`template-st`·`transformers`는 `1.1.8-egov.1`)
- **LangChain4j** – `1.18.1`, beta 모듈 `1.18.1-beta28` (Boot 스타터 제외, 개별 관리)
- **ONNX Runtime** `1.20.0`, **HuggingFace Tokenizers** `0.32.0`
- **pluginManagement** – Compiler, WAR, PMD, Surefire, Javadoc 플러그인 버전 고정

## 🚀 시작하기

### 요구사항

- **Java 17** 이상
- **Maven 3.6** 이상

### 사용 방법

Spring Boot를 쓰지 않는 **웹(MVC)** 프로젝트 `pom.xml`에서 BOM을 import 합니다.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.egovframe.web</groupId>
            <artifactId>egovframe-web-config-parent</artifactId>
            <version>5.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 예제

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>my-egovframe-web-project</artifactId>
    <version>1.0.0</version>
    <packaging>war</packaging>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.egovframe.web</groupId>
                <artifactId>egovframe-web-config-parent</artifactId>
                <version>5.1.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.egovframe.rte</groupId>
            <artifactId>egovframe-rte-ptl-mvc</artifactId>
        </dependency>
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
        </dependency>
    </dependencies>
</project>
```

## 📦 포함된 주요 의존성 (`dependencyManagement`)

### 전자정부 표준프레임워크 RTE (5.1.0)

#### Batch
- `egovframe-rte-bat-core`

#### AI (PoC)
- `egovframe-rte-ai-core` – 프레임워크 중립 AI 기반
- `egovframe-rte-ai-langchain4j` – LangChain4j 어댑터
- `egovframe-rte-ai-springai` – Spring AI 어댑터
- 위 AI 모듈이 쓰는 `hwplib` 1.1.9, `hwpxlib` 1.0.8, `pdfbox` 3.0.7, `jedis` 6.0.0 은 `egovframe-rte-root` 5.1.0 에서 함께 관리

#### Foundation Layer (FDL)
- `egovframe-rte-fdl-access` – 접근 제어
- `egovframe-rte-fdl-cmmn` – 공통
- `egovframe-rte-fdl-crypto` – 암호화
- `egovframe-rte-fdl-excel` – Excel
- `egovframe-rte-fdl-filehandling` – 파일
- `egovframe-rte-fdl-idgnr` – ID 생성
- `egovframe-rte-fdl-logging` – 로깅
- `egovframe-rte-fdl-property` – 프로퍼티
- `egovframe-rte-fdl-reactive` – Reactive
- `egovframe-rte-fdl-security` – 보안
- `egovframe-rte-fdl-string` – 문자열
- `egovframe-rte-fdl-xml` – XML

#### Integration Layer (ITL)
- `egovframe-rte-itl-integration`
- `egovframe-rte-itl-webservice`

#### Persistence Layer (PSL)
- `egovframe-rte-psl-data-jpa`, `egovframe-rte-psl-data-mongodb`, `egovframe-rte-psl-dataaccess`
- `egovframe-rte-psl-reactive-cassandra`, `egovframe-rte-psl-reactive-mongodb`, `egovframe-rte-psl-reactive-r2dbc`, `egovframe-rte-psl-reactive-redis`

#### Presentation Layer (PTL)
- `egovframe-rte-ptl-mvc`, `egovframe-rte-ptl-reactive`

### Jakarta / 웹

Jakarta EE 9 기준 : Servlet(`jakarta.servlet-api` 5.0.0)·JSP(3.0.0)·EL(4.0.0)·JSTL(`jakarta.servlet.jsp.jstl-api` 2.0.0)·Validation(3.0.2, Hibernate Validator 7.0.5.Final)·WebSocket(2.0.0)·JSON(JSON-P 2.0.1, JSON-B 2.0.0)·Mail(`com.sun.mail:jakarta.mail` 2.0.1)·JAXB(3.0)·JAX-WS(3.0)·Faces(3.0.0)·Annotation(2.0.0)·Inject 등 (버전은 `pom.xml` properties 참고)

> JPA(`jakarta.persistence-api`)·Hibernate ORM 은 `egovframe-rte-root` 버전을 유지합니다. WAS 가 제공하지 않는 애플리케이션 내장 라이브러리이며, Spring Data JPA 가 Hibernate 6.6 을 요구하기 때문입니다.

### Spring

- Spring Framework (**6.2.19**), Spring Security (**6.5.11-egov.1**), Spring Batch (**5.2.6**), Spring Data (**2025.0.13**), Spring Integration (**6.5.10-egov.1**) — `egovframe-rte-root` import
- `spring-ldap-core` (**3.3.8**)

### DB (본 BOM에 정의된 드라이버)

- **MySQL** (`mysql-connector-j` 26.7.0, `egovframe-rte-root` 관리)
- **MariaDB** (`mariadb-java-client` 3.5.8)
- **PostgreSQL** (42.7.11)
- **HSQLDB** (2.7.4)

> Altibase·CUBRID·Tibero·Oracle 등은 이 parent POM의 `dependencyManagement`에 포함되어 있지 않습니다. 필요 시 자식 프로젝트에서 별도 선언하세요.

### 기타 카테고리

| 영역 | 예시 |
|------|------|
| JSON/XML | Jackson **2.22.3**, Hibernate Validator, Yasson **2.0.4**, Parsson, JSON-Simple **1.1.1**, XMLBeans, ANTLR4 |
| 로깅 | Log4j **2.26.1**, SLF4J **2.0.20**, log4jdbc |
| Commons | BeanUtils, Codec, Compress, DBCP2, Email/FileUpload(Jakarta), IO, Lang3, Net, Pool2 |
| HTTP/WS | HttpClient5 **5.6.1**, Jakarta WebSocket **2.0.0**, JAX-WS RT, JAX-RPC, JMS API 등 |
| UI/도구 | CKEditor Java Core, Batik, Ajaxtags |
| OAuth/SNS | ScribeJava, Twitter4j **4.0.7**(common-components 의 `twitter4j.JSONObject` 사용과 호환되는 버전) |
| LDAP | LDAP SDK |
| 테스트/빌드 | JUnit 4 / JUnit 6.1.3, Selenium 4.44.0, JavaParser, Lombok 1.18.48 |
| EMS/레거시 | sndng-mail, soap, xalan |
| Spring AI | `spring-ai-*` **1.1.8** (Boot 비의존 65종 개별 관리), egov 빌드 5종 **1.1.8-egov.1** |
| LangChain4j | **1.18.1** / beta **1.18.1-beta28** (Boot 스타터 제외 101종 개별 관리) — `langchain4j-bom` import 없음 |
| AI/ML | ONNX Runtime **1.20.0**, HuggingFace Tokenizers **0.32.0** |

## 🔧 `pluginManagement` (빌드)

자식 프로젝트에서 버전 생략 시 아래가 기준이 됩니다.

| 플러그인 | 버전 (pom 기준) |
|----------|-----------------|
| maven-compiler-plugin | 3.16.0 (`release` 17) |
| maven-war-plugin | 3.5.1 |
| maven-pmd-plugin | 3.28.0 |
| maven-surefire-plugin | 3.6.0 |
| maven-javadoc-plugin | 3.12.0 |

## 🔧 Boot Parent와의 관계

| 구분 | Boot Starter Parent | Web Config Parent (본 BOM) |
|------|---------------------|----------------------------|
| 용도 | Spring Boot용 **Parent POM** | 웹(MVC)용 **BOM** |
| 사용 | `<parent>` | `<dependencyManagement>` **import** |
| Spring Boot | Boot BOM/Parent에서 관리 | **미포함** |
| 대상 | Boot 앱 | WAR 등 전통 웹 앱 |

Boot 기반은 **egovframe-boot-starter-parent**를 쓰고, BOM만 필요하면 본 artifact를 import 하면 됩니다.

## 📚 참고 자료

- [전자정부 표준프레임워크](https://www.egovframe.go.kr)
- [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- 전자정부 표준프레임워크 개발자 센터 – [nttList (menuNo=64)](https://www.egovframe.go.kr/home/ntt/nttList.do?menuNo=64)

## 📄 라이선스

Apache License 2.0

## 🤝 기여하기

1. Fork → 2. 브랜치 생성 → 3. 커밋 → 4. Push → 5. Pull Request

## 📞 문의

- https://www.egovframe.go.kr  
- 이슈: 저장소 GitHub Issues

---

**egovframe-web-config-parent 5.1.0** – 전자정부 표준프레임워크 웹(MVC) Maven BOM
