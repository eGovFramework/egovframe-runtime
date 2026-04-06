# 전자정부 표준프레임워크 실행환경 (eGovFrame Runtime)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Framework](https://img.shields.io/badge/Spring%20Framework-6.2.11-brightgreen.svg)](https://spring.io/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)

전자정부 표준프레임워크의 런타임 컴포넌트를 제공하는 멀티 모듈 Maven 프로젝트입니다.

## 📋 목차

- [소개](#소개)
- [주요 기능](#주요-기능)
- [프로젝트 구조](#프로젝트-구조)
- [요구사항](#요구사항)
- [빌드 및 설치](#빌드-및-설치)
- [라이브러리별 특징 및 기능](#라이브러리별-특징-및-기능)
- [사용 예시](#사용-예시)
- [라이선스](#라이선스)
- [참고 자료](#참고-자료)
- [기여하기](#기여하기)
- [문의](#문의)

## 소개

전자정부 표준프레임워크 런타임은 전자정부 시스템 개발을 위한 표준화된 컴포넌트와 유틸리티를 제공합니다. 이 프로젝트는 공공기관의 정보시스템 구축 시 재사용 가능한 공통 컴포넌트를 제공하여 개발 생산성 향상과 유지보수 비용 절감을 목표로 합니다.

### 주요 특징

- **계층화된 아키텍처**: Foundation, Integration, Persistence, Presentation 레이어로 구성
- **Spring Framework 기반**: Spring 6.2.11 기반의 최신 기술 스택 적용
- **Java 17 지원**: 최신 Java 기능 활용
- **멀티 모듈 구조**: 필요한 모듈만 선택적으로 사용 가능
- **리액티브 프로그래밍 지원**: Spring WebFlux 기반 리액티브 모듈 제공

## 주요 기능

### Foundation Layer (기반 레이어)
- **공통 서비스**: 예외 처리, 로깅, 메일 발송
- **보안**: 인증 및 권한 관리
- **암호화**: 데이터 암호화 및 해시 처리
- **파일 처리**: 파일 업로드/다운로드 관리
- **Excel 처리**: Excel 파일 읽기/쓰기
- **ID 생성**: 유일한 ID 생성 서비스
- **속성 관리**: 설정 파일 관리
- **XML 처리**: XML 파싱 및 변환
- **문자열 처리**: 문자열 유틸리티
- **리액티브 지원**: 리액티브 프로그래밍 기반 컴포넌트

### Integration Layer (연계 레이어)
- **시스템 연계**: 다양한 시스템 간 연계 서비스
- **웹 서비스**: SOAP 기반 웹 서비스 지원

### Persistence Layer (영속성 레이어)
- **JPA**: Java Persistence API 지원
- **MyBatis**: MyBatis 기반 데이터 접근
- **MongoDB**: NoSQL 데이터베이스 지원
- **리액티브 데이터베이스**: 
  - R2DBC (Reactive Relational Database Connectivity)
  - Reactive MongoDB
  - Reactive Cassandra
  - Reactive Redis

### Presentation Layer (표현 레이어)
- **Spring MVC**: 전통적인 MVC 패턴 지원
- **Spring WebFlux**: 리액티브 웹 애플리케이션 지원

### Batch Layer (배치 레이어)
- **Spring Batch**: 대용량 데이터 처리 배치 작업 지원

## 프로젝트 구조

```
egovframe-runtime/
├── Batch/
│   └── org.egovframe.rte.bat.core/          # 배치 처리
├── Foundation/
│   ├── org.egovframe.rte.fdl.access/        # 접근 제어
│   ├── org.egovframe.rte.fdl.cmmn/          # 공통 서비스
│   ├── org.egovframe.rte.fdl.crypto/        # 암호화
│   ├── org.egovframe.rte.fdl.excel/         # Excel 처리
│   ├── org.egovframe.rte.fdl.filehandling/  # 파일 처리
│   ├── org.egovframe.rte.fdl.idgnr/         # ID 생성
│   ├── org.egovframe.rte.fdl.logging/       # 로깅
│   ├── org.egovframe.rte.fdl.property/      # 속성 관리
│   ├── org.egovframe.rte.fdl.reactive/      # 리액티브 지원
│   ├── org.egovframe.rte.fdl.security/      # 보안
│   ├── org.egovframe.rte.fdl.string/        # 문자열 처리
│   └── org.egovframe.rte.fdl.xml/           # XML 처리
├── Integration/
│   ├── org.egovframe.rte.itl.integration/   # 시스템 연계
│   └── org.egovframe.rte.itl.webservice/    # 웹 서비스
├── Persistence/
│   ├── org.egovframe.rte.psl.data.jpa/      # JPA
│   ├── org.egovframe.rte.psl.data.mongodb/  # MongoDB
│   ├── org.egovframe.rte.psl.dataaccess/    # 데이터 접근
│   ├── org.egovframe.rte.psl.reactive.cassandra/  # Reactive Cassandra
│   ├── org.egovframe.rte.psl.reactive.mongodb/   # Reactive MongoDB
│   ├── org.egovframe.rte.psl.reactive.r2dbc/     # Reactive R2DBC
│   └── org.egovframe.rte.psl.reactive.redis/     # Reactive Redis
├── Presentation/
│   ├── org.egovframe.rte.ptl.mvc/           # Spring MVC
│   └── org.egovframe.rte.ptl.reactive/      # Spring WebFlux
└── pom.xml                                   # 루트 POM
```

## 요구사항

- **Java**: JDK 17 이상
- **Maven**: 3.6 이상
- **Spring Framework**: 6.2.11
- **빌드 도구**: Maven

## 빌드 및 설치

### 전체 프로젝트 빌드

```bash
# 프로젝트 클론 (실제 저장소 URL로 변경)
git clone <repository-url>
cd <repository-directory>

# 전체 프로젝트 빌드 및 설치
mvn clean install
```

### 특정 모듈만 빌드

```bash
# 특정 모듈 빌드 예시
cd Foundation/org.egovframe.rte.fdl.cmmn
mvn clean install
```

### 테스트 실행

```bash
# 전체 테스트 실행
mvn test

# 특정 모듈 테스트 실행
cd Foundation/org.egovframe.rte.fdl.cmmn
mvn test
```

### Maven 저장소에 배포

프로젝트는 전자정부 프레임워크 공식 Maven 저장소를 사용합니다:

```xml
<repositories>
    <repository>
        <id>egovframe</id>
        <url>https://maven.egovframe.go.kr/maven/</url>
    </repository>
</repositories>
```

## 라이브러리별 특징 및 기능

버전 **5.0.0** 기준 Maven 좌표는 `org.egovframe.rte:<artifactId>:5.0.0` 형태입니다. 상세 사용법은 일부 모듈(`/Foundation/.../README.md`)에 별도 문서가 있습니다.

### 요약 표

| 계층 | 디렉터리(모듈) | Artifact ID | 한 줄 설명 |
|------|----------------|-------------|------------|
| Foundation | `fdl.logging` | `egovframe-rte-fdl-logging` | Log4j2·SLF4J 기반 공통 로깅 스택 |
| Foundation | `fdl.cmmn` | `egovframe-rte-fdl-cmmn` | 서비스·트랜잭션·AOP 등 공통 기반 |
| Foundation | `fdl.string` | `egovframe-rte-fdl-string` | 문자열·인코딩 등 유틸리티 |
| Foundation | `fdl.filehandling` | `egovframe-rte-fdl-filehandling` | Commons VFS2 기반 파일 처리 |
| Foundation | `fdl.property` | `egovframe-rte-fdl-property` | Commons Configuration2 기반 설정 로딩 |
| Foundation | `fdl.crypto` | `egovframe-rte-fdl-crypto` | Jasypt 등 암·복호화·보안 속성 연동 |
| Foundation | `fdl.idgnr` | `egovframe-rte-fdl-idgnr` | 시퀀스·테이블·UUID 등 ID 채번 |
| Foundation | `fdl.xml` | `egovframe-rte-fdl-xml` | Spring OXM 계열 XML 바인딩·처리 |
| Foundation | `fdl.access` | `egovframe-rte-fdl-access` | DB 매핑 기반 URL 접근제어 인터셉터 |
| Foundation | `fdl.security` | `egovframe-rte-fdl-security` | Spring Security·태그 라이브러리 연계 |
| Foundation | `fdl.reactive` | `egovframe-rte-fdl-reactive` | WebFlux·리액티브 공통 기반 |
| Foundation | `fdl.excel` | `egovframe-rte-fdl-excel` | Apache POI·jxls 엑셀 입출력 |
| Integration | `itl.integration` | `egovframe-rte-itl-integration` | Hibernate·ORM 연계·테스트 지원 |
| Integration | `itl.webservice` | `egovframe-rte-itl-webservice` | Apache CXF JAX-WS(SOAP) 웹서비스 |
| Persistence | `psl.dataaccess` | `egovframe-rte-psl-dataaccess` | iBatis·MyBatis SQL 매퍼 접근 |
| Persistence | `psl.data.jpa` | `egovframe-rte-psl-data-jpa` | Spring Data JPA·Hibernate |
| Persistence | `psl.data.mongodb` | `egovframe-rte-psl-data-mongodb` | Spring Data MongoDB(동기) |
| Persistence | `psl.reactive.r2dbc` | `egovframe-rte-psl-reactive-r2dbc` | Spring Data R2DBC(리액티브 SQL) |
| Persistence | `psl.reactive.mongodb` | `egovframe-rte-psl-reactive-mongodb` | MongoDB 리액티브 드라이버 연동 |
| Persistence | `psl.reactive.cassandra` | `egovframe-rte-psl-reactive-cassandra` | Spring Data Cassandra 리액티브 |
| Persistence | `psl.reactive.redis` | `egovframe-rte-psl-reactive-redis` | Spring Data Redis·Lettuce 리액티브 |
| Presentation | `ptl.mvc` | `egovframe-rte-ptl-mvc` | Spring MVC 뷰·검증·태그 확장 |
| Presentation | `ptl.reactive` | `egovframe-rte-ptl-reactive` | WebFlux 기반 표현 계층·검증 |
| Batch | `bat.core` | `egovframe-rte-bat-core` | Spring Batch 확장 Item·리스너·실행기 |

---

### Foundation Layer (`Foundation/`)

#### `egovframe-rte-fdl-logging`

- **특징**: 다른 FDL·PSL 모듈이 공통으로 의존하는 최하위 로깅 계층. Apache Log4j2와 SLF4J 브리지를 사용합니다.
- **기능**: 애플리케이션 전역 로그 설정의 기준, commons-logging 대체 방향과의 정합.

#### `egovframe-rte-fdl-cmmn`

- **특징**: 표준 프레임워크 서비스 추상화, 트랜잭션·JDBC 연동의 허브 역할.
- **기능**: 공통 예외·서비스 베이스 클래스, 메일 등 지원 컴포넌트(`spring-context-support`, `spring-jdbc`, `spring-tx`), AspectJ 기반 횡단 관심사. `fdl.logging` 모듈에 의존합니다.

#### `egovframe-rte-fdl-string`

- **특징**: 가벼운 문자열·바이트 처리 보조. Spring `ApplicationContext`와 함께 쓰이는 유틸 성격.
- **기능**: 인코딩·포맷 등 공통 문자열 연산(Commons Codec 연계).

#### `egovframe-rte-fdl-filehandling`

- **특징**: 물리·논리 경로를 추상화한 파일 접근(Apache Commons VFS2).
- **기능**: 업로드·다운로드·경로 통합 처리에 필요한 파일 유틸. `fdl.string`에 의존.

#### `egovframe-rte-fdl-property`

- **특징**: XML·properties 등 다중 소스 설정을 다루기 위한 계층. `fdl.cmmn` 기반.
- **기능**: `commons-configuration2`로 환경별 프로퍼티 로드·참조. `fdl.crypto`에서 보안 속성 복호화 등과 맞물릴 수 있음.

#### `egovframe-rte-fdl-crypto`

- **특징**: 설정·저장 데이터 보호를 위한 암호화 유틸. `fdl.property`에 의존.
- **기능**: Jasypt 기반 암·복호화, Jackson과 연계한 속성 직렬화 등.

#### `egovframe-rte-fdl-idgnr`

- **특징**: 도메인 키·채번 테이블·시퀀스와 연동하는 ID 생성 전략.
- **기능**: `EgovTableIdGnrServiceImpl`, `EgovSequenceIdGnrServiceImpl`, `EgovUUIdGnrServiceImpl` 등 전략 패턴 채번, `fdl.cmmn` 연동.

#### `egovframe-rte-fdl-xml`

- **특징**: Spring `spring-context-support`(OXM) 중심의 XML ↔ 객체 매핑.
- **기능**: 레거시 연계·설계서류 대응용 XML 처리, EE API는 `provided`로 범위 제한.

#### `egovframe-rte-fdl-access`

- **특징**: DB에 정의된 권한·URL 패턴을 읽어 Spring MVC `HandlerInterceptor`로 인증·인가.
- **기능**: `EgovAccessInterceptor`, 설정 Reader·Java Config, `RequestMappingHandlerMapping` 후처리로 인터셉터 우선순위 주입. 자세한 동작은 모듈 내 README 참고.

#### `egovframe-rte-fdl-security`

- **특징**: Spring Security 6.x와 MVC 연동(설정·태그).
- **기능**: 권한 표현·URL 보호와 연계한 확장, `fdl.string` 의존.

#### `egovframe-rte-fdl-reactive`

- **특징**: Servlet 스택과 분리된 WebFlux 기반 공통 레이어.
- **기능**: `spring-webflux`·`spring-context` 기반 리액티브 빈·설정 패턴 지원. `fdl.logging` 의존.

#### `egovframe-rte-fdl-excel`

- **특징**: 대용량 표 데이터의 업로드·다운로드·양식 처리.
- **기능**: Apache POI, jxls-poi 템플릿, `fdl.cmmn`·`fdl.filehandling`·`psl.dataaccess`와 결합한 DB 연동 엑셀 처리.

---

### Integration Layer (`Integration/`)

#### `egovframe-rte-itl-integration`

- **특징**: 외부 시스템·ORM과의 연계를 위한 스프링 ORM·Hibernate 코어 결합.
- **기능**: `spring-orm`, Hibernate, DbUnit 기반 테스트 픽스처. 상위 연계 모듈의 베이스.

#### `egovframe-rte-itl-webservice`

- **특징**: SOAP(JAX-WS) 서버·클라이언트 구성을 Apache CXF로 표준화.
- **기능**: `itl.integration` 위에 `cxf-rt-frontend-jaxws`, HTTP Jetty 트랜스포트, `jakarta.xml.ws-api` 연동.

---

### Persistence Layer (`Persistence/`)

#### `egovframe-rte-psl-dataaccess`

- **특징**: MyBatis 중심의 전통적 SQL 매핑 + 레거시 iBatis(sqlmap) 호환.
- **기능**: `mybatis`, `mybatis-spring`, iBatis sqlmap, Spring `ORM`·`JPA` API와 공존 가능한 데이터 소스 추상화.

#### `egovframe-rte-psl-data-jpa`

- **특징**: 도메인 모델 중심 영속성. `psl.dataaccess` 위에 JPA 스택을 올린 구조.
- **기능**: Spring Data JPA, Hibernate 구현체, 트랜잭션 경계와 Egov 표준 리포지토리 패턴 연계.

#### `egovframe-rte-psl-data-mongodb`

- **특징**: 문서 저장소 동기 API.
- **기능**: Spring Data MongoDB, 공식 MongoDB 동기 드라이버, 웹·트랜잭션 모듈과 함께 사용.

#### `egovframe-rte-psl-reactive-r2dbc`

- **특징**: 논블로킹 관계형 DB 접근(R2DBC).
- **기능**: Spring Data R2DBC, Project Reactor, H2 R2DBC 등 테스트 의존. `fdl.cmmn`과 조합.

#### `egovframe-rte-psl-reactive-mongodb`

- **특징**: MongoDB Reactive Streams 기반.
- **기능**: Spring Data MongoDB + Reactor, 비동기 문서 CRUD.

#### `egovframe-rte-psl-reactive-cassandra`

- **특징**: 컬럼 패밀리 DB의 리액티브 액세스.
- **기능**: Spring Data Cassandra, Cassandra Java 드라이버, WebFlux·트랜잭션과 병행.

#### `egovframe-rte-psl-reactive-redis`

- **특징**: 캐시·세션·토큰 저장소 등 인메모리 데이터 리액티브 접근.
- **기능**: Spring Data Redis, Lettuce 비동기 클라이언트, Jackson 직렬화.

---

### Presentation Layer (`Presentation/`)

#### `egovframe-rte-ptl-mvc`

- **특징**: Servlet 기반 Spring MVC 표현 계층 표준 확장.
- **기능**: `spring-webmvc`, Jakarta Servlet/JSP API, Commons BeanUtils·Validator, ANTLR 등 뷰·입력 검증 보조. `fdl.cmmn` 의존.

#### `egovframe-rte-ptl-reactive`

- **특징**: WebFlux 스택의 컨트롤러·필터·메시지 코덱 공통.
- **기능**: `spring-webflux`, Bean Validation(Hibernate Validator) 테스트 스택, `fdl.logging` 연동.

---

### Batch Layer (`Batch/`)

#### `egovframe-rte-bat-core`

- **특징**: Spring Batch 5.x를 확장한 공공 배치 패턴(파일·DB·복합 Reader/Writer).
- **기능**: 고정/가변 길이·구분자 파일, 인덱스 파일, MyBatis 페이징 Reader·BatchWriter, 청크·잡 전후 리스너, 스케줄·커맨드라인 실행기, `psl.dataaccess`·`fdl.cmmn`과 연계.

---

## 사용 예시

### Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>egovframe-rte-fdl-cmmn</artifactId>
    <version>5.0.0</version>
</dependency>
```

### 공통 서비스 사용 예시

```java
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service
public class SampleService extends EgovAbstractServiceImpl {
    // 공통 서비스 기능 사용
}
```

## 라이선스

이 프로젝트는 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt) 라이선스를 따릅니다.

```
Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 참고 자료

- [전자정부 표준프레임워크 공식 사이트](https://www.egovframe.go.kr)
- [전자정부 표준프레임워크 가이드](https://www.egovframe.go.kr/docs)
- [Spring Framework 공식 문서](https://spring.io/projects/spring-framework)
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)

## 기여하기

프로젝트에 기여하고 싶으시다면 다음을 참고해주세요:

1. 이슈를 먼저 생성하여 변경사항을 논의해주세요
2. Fork 후 브랜치를 생성하여 작업해주세요
3. 테스트를 작성하고 모든 테스트가 통과하는지 확인해주세요
4. Pull Request를 생성해주세요

## 문의

프로젝트에 대한 문의사항이 있으시면 다음을 통해 연락해주세요:

- 이슈 트래커: 사용 중인 저장소의 `Issues` 페이지를 이용하세요.
- 전자정부 표준프레임워크 공식 사이트: https://www.egovframe.go.kr

---

**전자정부 표준프레임워크 런타임 5.0.0** - 공공기관 정보시스템 개발을 위한 표준화된 컴포넌트 제공
