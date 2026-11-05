# 전자정부 표준프레임워크 Boot Starter Parent

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-1.8-orange.svg)](https://www.oracle.com/java/)
[![eGovFrame](https://img.shields.io/badge/eGovFrame-4.3.1-green.svg)](https://www.egovframe.go.kr)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)

## 📋 개요

전자정부 표준프레임워크 **실행환경 4.3.1** 기반 Spring Boot 프로젝트용 Maven Parent POM입니다.
`spring-boot-starter-parent` 2.7.18을 상속하고, 실행환경 루트 POM(`org.egovframe.rte.root` 4.3.1)을 BOM으로 가져와 실행환경 모듈과 라이브러리 버전을 한곳에서 관리합니다.

| 항목 | 값 |
|------|-----|
| `groupId` | `org.egovframe.boot` |
| `artifactId` | `org.egovframe.boot.starter.parent` |
| `version` | `4.3.1` |
| `packaging` | `pom` |
| Parent | `org.springframework.boot:spring-boot-starter-parent` 2.7.18 |

## ✨ 주요 특징

- **Java 1.8** — 관리 라이브러리는 Java 8에서 동작하는 버전(class 52 이하)
- **실행환경 4.3.1 BOM import** — RTE 모듈 전체, Spring 포크(`-egov.1`), Jackson, Log4j2, Netty, Jetty, DB 드라이버, 테스트 라이브러리 등
- **Spring Boot 속성 재정의** — Boot가 직접 관리하는 항목을 실행환경 버전으로 정렬
- **eGovFrame Boot Starter 4.3.1** (access·crypto·security) — 실행환경 BOM import로 관리
- **Spring Cloud 2021.0.9**, **QueryDSL 5.0.0**
- `maven-deploy-plugin` 3.1.4 (Boot 기본 2.8.2 대체, 실행환경과 동일)

## 🚀 시작하기

### 요구사항

- Java 8 이상
- Maven 3.6 이상
- 표준프레임워크 저장소 `https://maven.egovframe.go.kr/maven/` (`-egov.1` Spring 아티팩트)

> ⚠️ BOM import는 저장소 설정을 상속하지 않으므로, 프로젝트 `pom.xml` 또는 `settings.xml`에 표준프레임워크 저장소를 등록하세요.
> 실행환경 4.3.1이 저장소에 없으면 소스에서 먼저 설치합니다.
>
> ```bash
> mvn -f egovframe-runtime-4.3.1/pom.xml install -DskipTests
> ```

### 사용 방법

```xml
<parent>
    <groupId>org.egovframe.boot</groupId>
    <artifactId>org.egovframe.boot.starter.parent</artifactId>
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
        <groupId>org.egovframe.boot</groupId>
        <artifactId>org.egovframe.security.spring.boot.starter</artifactId>
    </dependency>
</dependencies>
```

## 📦 관리 의존성 (`dependencyManagement`)

### 전자정부 표준프레임워크

| 구분 | 아티팩트 | 버전 |
|------|----------|------|
| 실행환경 BOM | `org.egovframe.rte:org.egovframe.rte.root` (import) | 4.3.1 |
| Boot Starter (BOM 경유) | `org.egovframe.boot:org.egovframe.access.spring.boot.starter` | 4.3.1 |
| | `org.egovframe.boot:org.egovframe.crypto.spring.boot.starter` | 4.3.1 |
| | `org.egovframe.boot:org.egovframe.security.spring.boot.starter` | 4.3.1 |

실행환경 BOM으로 관리되는 RTE 모듈(`org.egovframe.rte.*` 24종 + `spring-modules-validation`)과 라이브러리 버전은 [실행환경 루트 POM](../../pom.xml)을 참고하세요.

### 이 POM에서 추가 관리

| 아티팩트 | 버전 | 비고 |
|----------|------|------|
| `spring-cloud-dependencies` (import) | 2021.0.9 | |
| `querydsl-jpa`, `querydsl-apt` | 5.0.0 | |
| `aspectjtools` | 1.9.7 | 1.9.8+는 Java 11+ 필요 |
| `commons-logging` | 1.3.6 | Boot·실행환경 미관리 |
| `log4j-api` | 2.25.5 | 실행환경 `log4j-core`와 같은 버전으로 고정 (`log4j2.version`과 분리, 전이 2.24.x 방지) |

### Spring Boot 속성 재정의

`spring-boot-dependencies`에 직접 선언된 항목은 실행환경 BOM보다 우선하므로, Boot 속성으로 실행환경 버전에 맞춥니다.

| 속성 | 값 | 속성 | 값 |
|------|-----|------|-----|
| `aspectj.version` | 1.9.23 | `lombok.version` | 1.18.36 |
| `assertj.version` | 3.27.7 | `mongodb.version` | 4.11.5 |
| `cassandra-driver.version` | 4.17.0 | `mysql.version` | 8.4.0 |
| `commons-codec.version` | 1.18.0 | `postgresql.version` | 42.7.12 |
| `commons-dbcp2.version` | 2.13.0 | `mariadb.version` | 3.4.0 |
| `commons-lang3.version` | 3.20.0 | `log4j2.version` | 2.25.5 |
| `h2.version` | 2.2.220 | `logback.version` | 1.2.13 |
| `jaxen.version` | 2.0.0 | `commons-pool2.version` | 2.12.0 |
| `lettuce.version` | 6.5.5.RELEASE | | |

> `slf4j.version`은 재정의하지 않습니다(Boot 기본 1.7.36 유지).
> 2.x로 올리면 `log4j-slf4j2-impl`과 `spring-boot-starter-logging`의 `log4j-to-slf4j`가 충돌하여 기동 시 `LoggingException`이 발생합니다.

## 🔧 Web Config Parent와의 관계

| 구분 | Boot Starter Parent (본 POM) | [Web Config Parent](../org.egovframe.web.config.parent) |
|------|------------------------------|-------------------|
| 대상 | Spring Boot 애플리케이션 | Boot 미사용 웹(WAR) 애플리케이션 |
| 상위 POM | `spring-boot-starter-parent` 2.7.18 | 없음 |
| 공통 | 실행환경 4.3.1 BOM import | 실행환경 4.3.1 BOM import |

## 📚 참고 자료

- [전자정부 표준프레임워크](https://www.egovframe.go.kr)
- [Spring Boot 2.7 문서](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/)

## 📄 라이선스

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
