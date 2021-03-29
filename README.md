# 표준프레임워크 실행환경
[Contribution guidelines for this project](README.md)
## 형상 제외 대상

- eclipse 프로젝트 관련 파일 : .project, .classpath, .settings/*.*
- output 파일 : target/**/*.*

## maven build command

mvn -Dmaven.test.skip=true source:jar install

mvn -Dencoding=UTF-8 javadoc:javadoc

mvn -Dmaven.test.skip=true -Dencoding=UTF-8 clean install source:jar

mvn -Dmaven.test.skip=true -Dencoding=UTF-8 clean install javadoc:jar

# tobe release note 
* 2018-06-12 : 표준프레임워크 README 정리
* 2018-06-12 : (egovframework.rte.fdl.cmmn) Spring Framework 4.3.16 > 4.3.22(OSS 버전 업그레이드
* 2018-06-12 : (egovframework.rte.fdl.cmmn) org.aspectj.aspectjweaver / org.spectj.aspectjrt 1.8.0 > 1.8.14
* 2018-06-12 : (egovframework.rte.fdl.logging) org.apache.loggin.log4j 2.10.0 > 2.11.2
* 2018-06-12 : (egovframework.rte.fdl.logging) org.slf4j 1.7.25 > 1.7.26
* 2018-06-12 : (egovframework.rte.fdl.security) Spring Security 4.2.5 > 4.2.11(OSS 버전 업그레이드)
* 2018-06-12 : (egovframework.rte.fdl.security) org.aspectj.aspectjweaver 1.8.0 > 1.8.4
* 2018-06-12 : (egovframework.rte.fdl.xml) org.aspectj.aspectjweaver 1.8.0 > 1.8.4
* 2018-06-12 : (egovframework.rte.itl.integration) org.hibernate 4.3.11.Final > 5.0.12.Final
* 2018-06-12 : (egovframework.rte.itl.webservice) org.hibernate 4.3.11.Final > 5.0.12.Final
* 2018-06-12 : (egovframework.rte.psl.data.jpa) spring-data-jpa 1.10.18 > 1.11.18
* 2018-06-12 : (egovframework.rte.psl.data.jpa) hibernate 4.3.11.Final > 5.0.12.Final
* 2018-06-12 : (egovframework.rte.psl.data.mongodb) spring-data-mongodb 1.7.2 > 1.10.18
* 2018-06-12 : (egovframework.rte.psl.dataaccess) Mybatis 3.4.1 > 3.4.6
* 2018-06-12 : (egovframework.rte.psl.dataaccess) mybatis-spring 1.3.0 > 1.3.2
* 2018-06-12 : (egovframework.rte.ptl.mvc)  javax.servlet.jsp-api 2.3.1 > 2.3.3
* 2018-06-12 : 표준프레임워크 소스 최종 정리
* 2018-04-12 : 표준프레임워크 반영(소스정리 GIT 반영)(Ver. 3.8)

# before release note
* 2018-05-15 : 표준프레임워크 최종 반영(소스정리 GIT 반영)(Ver. 3.7)
* 2017-05-10 : spring 4.2.4 반영
* 2017-05-02 : spring batch 3.0.6 반영
* 2013-03-19 : 초기 등록 (Ver. 2.5)
* 2013-03-22 : (egovframework.rte.ptl.mvc, 2.6) 패스워드 검증 관련 validation 메소드 추가
* 2013-03-25 : (egovframework.rte.fdl.idgnr, 2.6) Table ID Generation 서비스 테이블 필드 property 처리 (설정 가능)
* 2013-03-29 : (egovframework.rte.fdl.cmmn, 2.6) 버전 변경 2.5.0 -> 2.6.0 (idgnr에서 참조)
* 2013-04-01 : (egovframework.rte.bat.core, 2.6) 패키지 변경 및 EgovSchedulerRunner loop 기능
* 2013-04-02 : (spring-modules-validation, 0.9) JBoss tld 문제 해결 (기존 spring-modules-validation에서 groupId만 egovframework.rte로 변경하여 tld 파일만 수정)
* 2013-04-05 : (egovframework.rte.psl.dataaccess, 2.6) MyBatis 추가
* 2013-05-20 : (egovframework.rte.psl.data.jpa, 2.6) Spring Data JPA 신규 추가
* 2013-05-28 : (egovframework.rte.fdl.excel, 2.6) Apache POI 3.9, jxls 1.0.2 업그레이드
* 2013-05-29 : (egovframework.rte.fdl.excel, 2.6) mapBeanName 처리 방식 추가 (bean 활용)
* 2013-05-29 : 실행환경 전체 2.6 반영

* 2013-09-04 : (egovframework.rte.fdl.idgnr, 2.6.1) 내부 트랜잭션 분리
* 2013-11-21 : (egovframework.rte.psl.dataaccess, 2.6.1) EgovAbstractMapper listWithPaging() 메소드 오류 수정

* 2013-12-30 : 실행환경 전체 2.7 반영
* 2014-07-24 : 실행환경 전체 3.1 반영
* 2014-08-22 : (egovframework.rte.fdl.idgnr) Sequence 오류 수정

이하 release note 참조
* 2014-09-03 : (egovframework.rte.fdl.excel) EgovExcelUtil 수식 반환 값이 문자열 아니면 숫자여서 예외처리
----
* 2015-05-01 : spring 4.0.9 반영
* 2015-06-02 : 엑셀다운로드 라이브러리 버그 (파일명 변경불가) - AbstractPOIExcelView 버그 수정
* 2015-08-18 : 실행환경 전체 3.5 반영 

* 2015-09-03 : 실행환경 3.5 몽고(egovframework.rte.psl.data.mongodb) 버그 개선
    - maven dependency spring 3.2 > 3.5 적용
    - spring-data-mongodb 1.7.2.RELEAS 적용
    - Junit Test 관련 mongodb 설정 내역 변경 
    - Junit Test 테스트 MongoOperationsTest를 인증/비인증 테스트 가능 하도록 분리
      (인증[MongoOperationsAuthTest]/비인증[MongoOperationsAnonymousTest])
 * 2015-09-10 : egovframework.rte.fdl.cmmn.profiles.SpringXmlProfileTest 해당 Junit 에러 수정
 * 2015-09-15 : (전체)Junit 테스트 관련 spring 3.5 관련 DTD > spring 4.0 관련 DTD 변경
 * 2015-09-20 : Spring 4.0.9 관련 업그래이드 실행환경 3.5.1 패치 버전 작업
     > egovframework.rte.psl.data.mongodb : spring-data-mongodb 1.7.2.RELEAS 업그레이드 
     > egovframework.rte.psl.data.jpa     : spring-data-jpa 1.8.2.RELEASE 업그레이드
 * 2015-10-15 : (egovframework.rte.fdl.idgnr) EgovSequenceIdGnrService, EgovSequenceIdGnrServiceImpl Database JDBC Close 관련 보안 코드 적용
     > 오픈커뮤니티 버그 리포팅 수정 요 Resultset is not closed 에러 관련 수정
---- 
 * 2015-03-13 : 실행환경 전체 3.6 반영 (createChecksum 추가)
 * 2015-03-13 : spring 4.1.2 반영
 * 2015-03-15 : hibernate 4.3.11 반영
 * 2015-03-15 : Ehcache 2.6.11 반영
 * 2015-03-15 : log4j 2.5 반영
 * 2015-03-15 : mybatis 3.3.0 반영
 * 2015-03-15 : mybatis-spring 1.2.3 반영
 * 2015-03-15 : egovframework.rte.psl.data.jpa > spring 4.0.9 pom 충돌 대응 정리
 * 2015-03-15 : egovframework.rte.bat.core 팩키지 deprecated 관련 처리 
			 - org.springframework.scheduling.quartz.SimpleTriggerBean(Spring 4.1.x삭제됨 junit 수정)
			- org.springframework.scheduling.quartz.SimpleTriggerBean >  org.springframework.scheduling.quartz.CronTriggerFactoryBean
			 - egovframework.rte.bat.core/src/test/resources/META-INF/spring/launch/context-scheduler.xml 수정

# 위키 작성 문법
```
https://help.github.com/articles/basic-writing-and-formatting-syntax/
https://about.gitlab.com/handbook/product/technical-writing/markdown-guide/
```

 