# 표준프레임워크 실행환경 4.1.0

![java](https://img.shields.io/badge/java-007396?style=for-the-badge&logo=JAVA&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

## 실행환경 소개

표준프레임워크 실행환경은 전자정부 사업에서 개발하는 업무 프로그램의 실행에 필요한 공통모듈 등 업무 프로그램 개발 시 화면, 서버 프로그램, 데이터 처리, 배치 처리 등의 필수적인 기능을 패턴화하여 미리 구현해 둔 라이브러리 코드 묶음임

### 실행환경 특징

- 실행환경 사용 시 [필수적인 서비스]들과 타 서비스에 [의존성이 높은 서비스]들을 [핵심 서비스]로 제공
- 실행환경 사용에 필수적이지 않은 서비스들은 [선택 서비스]로 분류하여 각 사업에서 선택적으로 적용할 수 있도록 제공
- 핵심 서비스는 기본적으로 적용되며 선택 서비스는 각 사업의 필요에 맞게 개발환경을 통해 선택적으로 적용 가능

### 실행환경 4.1.0 버전의 주요 특징

- Spring Boot 2.7.0 지원
- Spring Framework 5.3.20 버전으로 업그레이드
- Spring Security 5.7.1 버전으로 업그레이드
- Spring WebFlux 표준화 기반 구성을 위한 [WebFlux 적용 예제](https://www.egovframe.go.kr/home/sub.do?menuNo=37)<img width="912" alt="11" src="https://user-images.githubusercontent.com/51683963/230555013-4e5f2c3a-808a-42e0-89bd-6a783d20345a.png">


## 실행환경 구성

```
org.egovframe.rte
  ├─Batch
  │  └─org.egovframe.rte.bat.core
  ├─Foundation
  │  ├─org.egovframe.rte.fdl.access
  │  ├─org.egovframe.rte.fdl.cmmn
  │  ├─org.egovframe.rte.fdl.crypto
  │  ├─org.egovframe.rte.fdl.excel
  │  ├─org.egovframe.rte.fdl.filehandling
  │  ├─org.egovframe.rte.fdl.idgnr
  │  ├─org.egovframe.rte.fdl.logging
  │  ├─org.egovframe.rte.fdl.property
  │  ├─org.egovframe.rte.fdl.security
  │  ├─org.egovframe.rte.fdl.string
  │  └─org.egovframe.rte.fdl.xml
  ├─Integration
  │  ├─org.egovframe.rte.itl.integration
  │  └─org.egovframe.rte.itl.webservice
  ├─Persistence
  │  ├─org.egovframe.rte.psl.data.jpa
  │  ├─org.egovframe.rte.psl.data.mongodb
  │  └─org.egovframe.rte.psl.data.dataaccess
  └─Presentation
     ├─org.egovframe.rte.ptl.mvc
     └─spring-modules-validation
```

### 실행환경 구성 설명

- `org.egovframe.rte.bat.core` : 대용량 데이터 처리 지원을 위해 작업수행 및 결과 관리 및 스케줄링 관리 기능을 제공
- `org.egovframe.rte.fdl.access` : Session 방식으로 접근제어 권한관리 설정을 간소화할 수 있는 방법을 제공
- `org.egovframe.rte.fdl.cmmn` : 공통으로 사용되는 업무 흐름제어, 에러 처리 등의 기능을 제공
- `org.egovframe.rte.fdl.crypto` : ARIA 블록암호 알고리즘 기반 암복호화 설정을 간소화할 수 있는 방법을 제공
- `org.egovframe.rte.fdl.excel` : 엑셀파일을 구성하거나 서버에 업로드 다운로드 할 수 있는 기능을 제공
- `org.egovframe.rte.fdl.filehandling` : 파일을 구성하거나 서버에 업로드 다운로드 할 수 있는 기능을 제공
- `org.egovframe.rte.fdl.idgnr` : 시스템을 개발할 때 필요한 유일한 ID를 생성할 수 있는 기능을 제공
- `org.egovframe.rte.fdl.logging` : 로그 처리와 관련된 기능을 제공
- `org.egovframe.rte.fdl.property` : 설정을 구성하거나 관리하는 기능을 제공
- `org.egovframe.rte.fdl.security` : 스프링 시큐리티를 기반으로 한 접근제어 권한관리 설정을 간소화할 수 있는 방법을 제공
- `org.egovframe.rte.fdl.string` : 시스템을 개발할 때 필요한 문자열 데이터를 다루기 위해 다양한 기능을 제공
- `org.egovframe.rte.fdl.xml` : XML 파일을 다루기 위해 다양한 기능을 제공
- `org.egovframe.rte.itl.integration` : 전자정부 표준프레임워크 기반의 시스템이 타 시스템과의 연계를 위해 사용하는 인터페이스 표준을 정의
- `org.egovframe.rte.itl.webservice` : 전자정부 표준프레임워크 Integration 서비스에 따라 웹서비스를 요청하고 제공하기 위한 라이브러리
- `org.egovframe.rte.psl.data.jpa` : Spring JPA 연동 예제 제공
- `org.egovframe.rte.psl.data.mongodb` : Spring과 MongoDB 연동 예제 제공
- `org.egovframe.rte.psl.data.dataaccess` : 데이터베이스 연결, 데이터처리, 트랜잭션 관리 기능을 제공
- `org.egovframe.rte.ptl.mvc` : 화면 처리를 위한 기능을 제공
- `spring-modules-validation` : Form 데이터의 유효성을 검증하기 위한 라이브러리

## 실행환경 구동 방법

1. 개발환경 Eclipse IDE 를 실행함
2. Eclipse IDE 메뉴에서 File>Import… 를 클릭하여 프로젝트를 가져옴
3. 실행환경의 각 서비스는 모듈 형식으로 제공되므로 각 서비스별로 JUnit을 이용하여 구성한 테스트케이스를 통해 설정이나 기능을 점검함![9](https://user-images.githubusercontent.com/51683963/230547024-f68cb39e-540f-4d83-a922-4a80c912194d.jpg)

## 참조

1. [실행환경 위키가이드](https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.1)
2. [개발환경 다운로드](https://www.egovframe.go.kr/home/sub.do?menuNo=94)
3. [실행환경 다운로드](https://www.egovframe.go.kr/home/sub.do?menuNo=92)
