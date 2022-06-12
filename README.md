![image](https://user-images.githubusercontent.com/1613812/125195363-365a7d00-e290-11eb-92b5-6cfd5266962e.png)

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]

<!-- ABOUT THE PROJECT -->
# 표준프레임워크 실행환경

전자정부 표준프레임워크 - 실행환경

## 프로젝트 설명

표준프레임워크 실행환경은 응용SW의 구성기반이 되며 응용SW실행 시 필요한 기본 기능을 제공하는 환경을 의미한다. 즉 전자정부 업무 구현을 목적으로 개발된 프로그램이 사용자가 의도하는 대로 정상적으로 실행될 수 있도록 지원하는 재사용 가능한 서버 실행 모듈, SW구조의 집합을 의미 한다.

<!-- GETTING STARTED -->
## 시작하기

표준프레임워크 실행환경은 메이븐(Maven)을 빌드 도구로 사용하고 있습니다.

### 사용 환경

* Maven 3+
* Java JDK 1.8+

### 빌드하기

#### 컴파일

메이븐 프로젝트로 구성되어 있으면 빌드하려면 다음 명령을 사용합니다

```
$ mvn clean install
```

#### 패키징

다음 명령을 사용하여 패키징 작업을 수행할 수 있습니다. 

```
$ mvn clean package
```

#### 테스트

__단위 테스트__

다음 명령을 사용하면 프로젝트의 단위 테스트를 수행할 수 있습니다.

```
$ mvn test
```

### 사용하기

표준프레임워크 실행환경은 pom.xml에 다음 설정을 추가하여 사용할 수 있습니다. 

#### 실행환경 의존성 추가

필수 항목은 반드시 추가해야 표준프레임워크 호환성을 유지할 수 있습니다. 

``` xml

<properties>
    <spring.maven.artifact.version>5.3.6.RELEASE</spring.maven.artifact.version>
    <org.egovframe.rte.version>4.0.0</org.egovframe.rte.version>
</properties>

...

<dependency>
    <groupId>egovframework.rte</groupId>
    <artifactId>egovframework.rte.fdl.cmmn</artifactId>
    <version>${egovframework.rte.version}</version>
</dependency>

<dependency>
    <groupId>egovframework.rte</groupId>
    <artifactId>egovframework.rte.ptl.mvc</artifactId>
    <version>${egovframework.rte.version}</version>
</dependency>

<dependency>
    <groupId>egovframework.rte</groupId>
    <artifactId>egovframework.rte.psl.dataaccess</artifactId>
    <version>${egovframework.rte.version}</version>
</dependency>

<dependency>
    <groupId>egovframework.rte</groupId>
    <artifactId>egovframework.rte.fdl.logging</artifactId>
    <version>${egovframework.rte.version}</version>
</dependency>

<!-- OPTIONAL -->
<dependency>
    <groupId>egovframework.rte</groupId>
    <artifactId>egovframework.rte.fdl.idgnr</artifactId>
    <version>${egovframework.rte.version}</version>
</dependency>

<!-- OPTIONAL -->
<dependency>
    <groupId>egovframework.rte</groupId>
    <artifactId>egovframework.rte.fdl.property</artifactId>
    <version>${egovframework.rte.version}</version>
</dependency>

...

```

## 버전 이력

* v4.0.0 FINAL
    * Spring Boot 2.4.5 지원
    * 실행환경 groupId 와 artifactId 변경 (egovframework.rte -> org.egovframe.rte)
    * 실행환경 오픈소스 버전 업그레이드 (Spring Framework 4.3.25 -> 5.3.6)
    * 실행환경 오픈소스 버전 업그레이드 (Spring Security 4.3.13 -> 5.4.6)
    * 실행환경 오픈소스 버전 업그레이드 (Spring Batch 3.0.10 -> 4.3.2)
* 4.0.0 beta
    * Various bug fixes and optimizations
    * See [commit change]() or See [release history]()
* 4.0.0 alpha
    * Initial Release

<!-- CONTRIBUTING -->
## 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b eGovFramework/egovframe-runtime`)
3. Commit your Changes (`git commit -m 'Add some egovframe-runtime'`)
4. Push to the Branch (`git push origin eGovFramework/egovframe-runtime`)
5. Open a Pull Request

## 도움말

표준프레임워크 묻고답하기 : https://www.egovframe.go.kr/home/sub.do?menuNo=69

## 라이선스

표준프레임워크 실행환경은 [Apache License](https://www.apache.org/licenses/LICENSE-2.0) 2.0에 따라 릴리스됩니다.

## 연락처

<!-- MARKDOWN LINKS & IMAGES -->

[contributors-shield]: https://img.shields.io/github/contributors/eGovFramework/egovframe-runtime.svg?style=flat-square
[contributors-url]: https://github.com/eGovFramework/egovframe-runtime/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/eGovFramework/egovframe-runtime.svg?style=flat-square
[forks-url]: https://github.com/eGovFramework/egovframe-runtime/network/members
[stars-shield]: https://img.shields.io/github/stars/eGovFramework/egovframe-runtime.svg?style=flat-square
[stars-url]: https://github.com/eGovFramework/egovframe-runtime/stargazers
[issues-shield]: https://img.shields.io/github/issues/eGovFramework/egovframe-runtime.svg?style=flat-square
[issues-url]: https://github.com/eGovFramework/egovframe-runtime/issues
[license-shield]: https://img.shields.io/github/license/eGovFramework/egovframe-runtime.svg?style=flat-square
[license-url]: https://github.com/eGovFramework/egovframe-runtime/blob/master/LICENSE.txt
