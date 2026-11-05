# 전자정부 표준프레임워크 Boot Starter Parent

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![eGovFrame](https://img.shields.io/badge/eGovFrame-5.1.0-green.svg)](https://www.egovframe.go.kr)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen.svg)](https://spring.io/projects/spring-boot)

## 📋 개요

전자정부 표준프레임워크 Boot Starter Parent는 전자정부 표준프레임워크 기반의 Spring Boot 프로젝트를 위한 Maven Parent POM입니다. 이 프로젝트는 전자정부 표준프레임워크의 모든 모듈과 Spring 생태계의 주요 라이브러리들의 버전을 중앙에서 관리하여, 일관된 의존성 관리를 제공합니다.

**Parent**: `spring-boot-starter-parent` 3.5.16  
**Packaging**: `pom` (의존성 BOM 역할)

## ✨ 주요 특징

- **Spring Boot 3.5.16** 기반
- **Java 17** 지원
- 전자정부 표준프레임워크 **5.1.0** (RTE·Boot Starter) 모듈 통합
- Spring 생태계 버전 관리 (Spring Framework, Spring Cloud, Spring Security, Spring Batch)
- **Spring AI 1.1.8** (`spring-ai-bom` import, egov 빌드 5종은 **1.1.8-egov.1** 개별 관리) — Ollama, PGVector·Redis 벡터 스토어, RAG, PDF/Markdown 리더, JDBC 채팅 메모리 등
- **LangChain4J 1.18.1** 및 **1.18.1-beta28** (문서 파서·PGVector·임베딩·Reactor·Hugging Face 등)
- **PostgreSQL** JDBC 42.7.11, **HSQLDB** 2.7.4
- Reactive 프로그래밍 지원 (WebFlux, R2DBC, Reactive MongoDB/Redis/Cassandra — RTE 모듈)
- **QueryDSL** 5.1.0 (Jakarta classifier)
- **Lombok** 1.18.48
- **로깅**: Log4j2 2.26.1, SLF4J 2.0.20, log4jdbc 1.2 (`egovframe-rte-root` 5.1.0 기준)
- **Apache Commons**: IO, DBCP2, Pool2, Lang3, BeanUtils, Net 등 버전 고정

## 🚀 시작하기

### 요구사항

- Java 17 이상
- Maven 3.6 이상

### 사용 방법

프로젝트의 `pom.xml`에서 이 프로젝트를 parent로 지정하세요:

```xml
<parent>
    <groupId>org.egovframe.boot</groupId>
    <artifactId>egovframe-boot-starter-parent</artifactId>
    <version>5.1.0</version>
</parent>
```

### 예제

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.egovframe.boot</groupId>
        <artifactId>egovframe-boot-starter-parent</artifactId>
        <version>5.1.0</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>my-egovframe-project</artifactId>
    <version>1.0.0</version>
    
    <dependencies>
        <!-- 전자정부 표준프레임워크 모듈 사용 시 버전 명시 불필요 -->
        <dependency>
            <groupId>org.egovframe.rte</groupId>
            <artifactId>egovframe-rte-ptl-mvc</artifactId>
        </dependency>
    </dependencies>
</project>
```

## 📦 포함된 주요 의존성 (`dependencyManagement`)

### 전자정부 표준프레임워크 모듈 (5.1.0)

#### Foundation Layer (FDL)
- `egovframe-rte-fdl-access` — 접근 제어
- `egovframe-rte-fdl-crypto` — 암호화
- `egovframe-rte-fdl-cmmn` — 공통 컴포넌트
- `egovframe-rte-fdl-excel` — Excel 처리
- `egovframe-rte-fdl-filehandling` — 파일 처리
- `egovframe-rte-fdl-idgnr` — ID 생성기
- `egovframe-rte-fdl-logging` — 로깅
- `egovframe-rte-fdl-property` — 프로퍼티 관리
- `egovframe-rte-fdl-reactive` — Reactive 지원
- `egovframe-rte-fdl-security` — 보안
- `egovframe-rte-fdl-string` — 문자열 처리
- `egovframe-rte-fdl-xml` — XML 처리

#### Integration Layer (ITL)
- `egovframe-rte-itl-integration` — 통합
- `egovframe-rte-itl-webservice` — 웹서비스

#### Persistence Layer (PSL)
- `egovframe-rte-psl-data-jpa` — JPA
- `egovframe-rte-psl-data-mongodb` — MongoDB
- `egovframe-rte-psl-dataaccess` — 데이터 접근
- `egovframe-rte-psl-reactive-cassandra` — Reactive Cassandra
- `egovframe-rte-psl-reactive-mongodb` — Reactive MongoDB
- `egovframe-rte-psl-reactive-r2dbc` — Reactive R2DBC
- `egovframe-rte-psl-reactive-redis` — Reactive Redis

#### Presentation Layer (PTL)
- `egovframe-rte-ptl-mvc` — Spring MVC
- `egovframe-rte-ptl-reactive` — Spring WebFlux

#### Batch
- `egovframe-rte-bat-core` — 배치 처리

#### AI (PoC)
- `egovframe-rte-ai-core` — 프레임워크 중립 AI 기반
- `egovframe-rte-ai-langchain4j` — LangChain4j 어댑터
- `egovframe-rte-ai-springai` — Spring AI 어댑터
- 위 AI 모듈이 쓰는 `hwplib` 1.1.9, `hwpxlib` 1.0.8, `pdfbox` 3.0.7, `jedis` 6.0.0 은 `egovframe-rte-root` 5.1.0 에서 함께 관리

#### Boot Starter (5.1.0)
- `egovframe-boot-starter-access` — 접근 제어
- `egovframe-boot-starter-crypto` — 암호화
- `egovframe-boot-starter-security` — 보안

### Spring 생태계

| 구성요소 | 버전 (pom.properties) |
|----------|------------------------|
| Spring Boot | 3.5.16 (parent) |
| Spring Framework | 6.2.19 |
| Spring Cloud | 2025.0.3 |
| Spring Security | 6.5.11-egov.1 |
| Spring Batch | 5.2.6 |
| Spring Data | 2025.0.13 |
| Spring Integration | 6.5.10-egov.1 |
| Spring AI | 1.1.8 (egov 빌드 5종 1.1.8-egov.1) |

### Spring AI (관리되는 아티팩트 예시)

- **egov 빌드 `1.1.8-egov.1` (BOM이 아닌 개별 관리)**: `spring-ai-commons`, `spring-ai-model`, `spring-ai-pdf-document-reader`, `spring-ai-template-st`, `spring-ai-transformers`
- 그 외는 `spring-ai-bom` **1.1.8** 기준
- `spring-ai-advisors-vector-store`, `spring-ai-client-chat`, `spring-ai-rag`
- `spring-ai-markdown-document-reader`, `spring-ai-pdf-document-reader`
- `spring-ai-starter-model-ollama`, `spring-ai-starter-model-transformers`
- `spring-ai-starter-model-chat-memory-repository-jdbc`
- `spring-ai-starter-vector-store-pgvector`, `spring-ai-starter-vector-store-redis`

### AI/ML 라이브러리

- **LangChain4J** (`langchain4j-bom` import): GA **1.18.1**, beta **1.18.1-beta28** (`langchain4j-document-parser-apache-pdfbox`, `langchain4j-easy-rag`, `langchain4j-pgvector`, `langchain4j-spring-boot-starter` 등은 BOM이 beta로 관리)
- **ONNX Runtime**: 1.20.0
- **HuggingFace Tokenizers**: 0.32.0

### 데이터베이스·Jakarta

- **PostgreSQL** (JDBC): 42.7.11
- **HSQLDB**: 2.7.4
- **Jakarta Annotation API**: 3.0.0

### 로깅

- **Log4j2**: 2.26.1 (`log4j-core`, `log4j-slf4j2-impl`)
- **SLF4J**: 2.0.20 (`jcl-over-slf4j`, `log4j-over-slf4j`)
- **log4jdbc**: 1.2

### 기타 라이브러리

- **QueryDSL**: 5.1.0 (`querydsl-apt` / `querydsl-jpa` — Jakarta classifier)
- **Lombok**: 1.18.48
- **Selenium**: 4.44.0
- **Apache Commons**: DBCP2 2.14.0, Pool2 2.13.1, IO 2.22.0, Lang3 3.20.0, BeanUtils 1.11.0, Net 3.13.0

## 🔧 버전 정보 (pom.xml 기준)

| 컴포넌트 | 버전 | pom 주석 날짜 |
|---------|------|---------------|
| Spring Boot | 3.5.16 | 2026.08.02 |
| Spring Framework | 6.2.19 | - |
| Spring Cloud | 2025.0.3 | 2026.08.02 |
| Spring Security | 6.5.11-egov.1 | - |
| Spring Batch | 5.2.6 | - |
| Spring AI | 1.1.8 (egov 5종 1.1.8-egov.1) | - |
| LangChain4J | 1.18.1 / 1.18.1-beta28 | 2026.06.10 |
| PostgreSQL (JDBC) | 42.7.11 | 2026.06.10 |
| HSQLDB | 2.7.4 | - |
| Log4j2 | 2.26.1 | - |
| SLF4J | 2.0.20 | - |
| Commons IO | 2.22.0 | - |
| Commons Lang3 | 3.20.0 | - |
| Lombok | 1.18.48 | - |
| Selenium | 4.44.0 | 2026.06.10 |
| ONNX Runtime | 1.20.0 | - |
| HuggingFace Tokenizers | 0.32.0 | - |

> Spring Framework·Security·Batch·Data·Integration, Jackson, Log4j2, SLF4J, Lombok 등은 `egovframe-rte-root` 5.1.0 BOM 값과 동일하게 정렬되어 있습니다.
>
> Spring Boot 의 `dependencyManagement` 에 직접 선언된 항목은 `egovframe-rte-root` import 보다 우선하므로, 값이 다른 항목은 Boot 속성으로 재정의해 runtime 값에 맞췄습니다: Hibernate Validator 8.0.5.Final, Lettuce 6.8.2.RELEASE, R2DBC H2 1.1.0.RELEASE, MySQL Connector/J 26.7.0, Ehcache 3.12.0. 또한 HttpClient5 는 egovframe-web-config-parent 와 같은 5.6.1 로 맞췄고, Jakarta WebSocket 은 양쪽 모두 2.1.1(Jakarta EE 10) 입니다. Jakarta Annotation · Persistence · Validation API 는 Spring Boot 3.5.16(Jakarta EE 10, Hibernate 6.6) 기준 버전(2.1.1 / 3.1.0 / 3.0.2)을 따릅니다 — `egovframe-rte-root` 의 3.0.0 / 3.2.0 / 3.1.1(EE 11 계열)로 올리면 Hibernate 6.6 의 `EntityManagerFactory` 생성이 실패합니다.

## 📚 참고 자료

- [전자정부 표준프레임워크 공식 사이트](https://www.egovframe.go.kr)
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [전자정부 표준프레임워크 개발자 센터](https://www.egovframe.go.kr/home/ntt/nttList.do?menuNo=64)

## 📄 라이선스

이 프로젝트는 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 라이선스를 따릅니다.

## 🤝 기여하기

전자정부 표준프레임워크는 공개 프로젝트입니다. 기여를 환영합니다!

1. 이 저장소를 Fork 합니다
2. 기능 브랜치를 생성합니다 (`git checkout -b feature/AmazingFeature`)
3. 변경사항을 커밋합니다 (`git commit -m 'Add some AmazingFeature'`)
4. 브랜치에 Push 합니다 (`git push origin feature/AmazingFeature`)
5. Pull Request를 생성합니다

## 📞 문의

- 전자정부 표준프레임워크 공식 사이트: https://www.egovframe.go.kr
- 이슈 트래커: GitHub Issues를 이용해주세요

---

**전자정부 표준프레임워크 Boot Starter Parent** — 전자정부 표준프레임워크 기반 Spring Boot 프로젝트를 위한 통합 의존성 관리
