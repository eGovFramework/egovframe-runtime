# org.egovframe.rte.psl.data.jpa

**영속성(Persistence) 계층 Spring Data JPA 연동 예제 모듈**입니다. **Spring Data JPA 2.7.18**과 **Hibernate 5.6.15.Final**(JPA 2.x, `javax.persistence` API)을 Spring Framework 5.3 환경에서 사용하는 방법을
테스트 코드와 설정 예제로 제공합니다. 이 모듈은 `src/main`이 없고 `src/test`만 있으며, 별도의 실행환경 클래스를 제공하지 않습니다.
빌드 결과 JAR은 의존성(`org.egovframe.rte.psl.dataaccess`, Spring Data JPA, Hibernate)을 묶어 적용 프로젝트에 전달하는 역할을 합니다. 이 모듈에 의존하는 다른 실행환경 모듈은 없습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.psl.data.jpa:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Spring Data JPA 2.7.18 / Hibernate 5.6.15.Final
- **실행환경 의존**: `org.egovframe.rte.psl.dataaccess`

---

## 특징

- **Spring Data JPA Repository 예제**: `CrudRepository`, `PagingAndSortingRepository` 상속만으로 CRUD·페이징·정렬 구현
- **쿼리 메서드 예제**: `findBy...And/Or/Between/LessThan/Like/StartingWith/In/OrderBy/IsNull/True` 등 메서드 이름 기반 쿼리 20여 종 (`UserDetailRepository`)
- **`@Query` JPQL 예제**: 위치 기반(`?1`)·이름 기반(`:name`, `@Param`) 파라미터 바인딩 (`UserRepository`)
- **복합키·연관관계 예제**: `@IdClass`(`User`/`UserPK`), `@ManyToOne`(`Department`-`Employee`)
- **XML 기반 설정 예제**: `LocalContainerEntityManagerFactoryBean` + `HibernateJpaVendorAdapter` + `JpaTransactionManager` + `<jpa:repositories>`
- **MyBatis와 공존**: `org.egovframe.rte.psl.dataaccess`에 의존하므로 같은 프로젝트에서 MyBatis Mapper와 JPA Repository를 함께 사용 가능

---

## 소스 디렉터리 구조

`src/main`은 없습니다. 테스트 소스와 설정만 있습니다.

```
src/test/java/org/egovframe/rte/psl/data/jpa/
├── CrudTest.java                 # 등록/조회/수정/삭제
├── PagingAndSortingTest.java     # PageRequest, Sort
├── QueryMethodTest.java          # 메서드 이름 기반 쿼리
├── RelationshipTest.java         # @ManyToOne 연관관계
├── SimpleTest.java               # @Query, 복합키 조회
├── domain/
│   ├── Article.java              # @Entity
│   ├── Department.java           # @Entity
│   ├── Employee.java             # @Entity @Table(name="employee"), Department @ManyToOne
│   ├── User.java                 # @Entity @IdClass(UserPK.class)
│   └── UserPK.java               # 복합키 클래스 (Serializable)
└── repository/
    ├── ArticleRepository.java        # CrudRepository<Article, Long>
    ├── DepartmentRepository.java     # CrudRepository<Department, Long> + findByDeptNameContaining
    ├── EmployeeRepository.java       # PagingAndSortingRepository<Employee, Long>
    ├── UserDetailRepository.java     # CrudRepository<User, Long> + 쿼리 메서드 모음
    └── UserRepository.java           # CrudRepository<User, UserPK> + @Query

src/test/resources/
├── META-INF/persistence.xml              # persistence-unit "jpa.sample" (내용은 주석 처리)
├── META-INF/spring/context-common.xml    # property-placeholder, component-scan
├── META-INF/spring/context-data-jpa.xml  # EntityManagerFactory, 트랜잭션, jpa:repositories
├── META-INF/spring/context-datasource.xml# jdbc:embedded-database (HSQL)
├── META-INF/spring/jdbc.properties
└── log4j2.xml
```

---

## 주요 기능

| 구분 | 테스트 클래스 | 설명 |
|---|---|---|
| 기본 CRUD | `CrudTest` | `CrudRepository`의 `save`/`findById`/`findAll`/`count`/`delete` 검증 |
| 페이징·정렬 | `PagingAndSortingTest` | `PagingAndSortingRepository`에 `PageRequest`/`Sort` 전달 |
| 쿼리 메서드 | `QueryMethodTest` | `UserDetailRepository`의 메서드 이름 기반 파생 쿼리 검증 |
| 연관관계 | `RelationshipTest` | `Employee` → `Department` `@ManyToOne(optional=false)` 매핑 |
| JPQL / 복합키 | `SimpleTest` | `UserRepository`의 `@Query`, `UserPK` 복합키 사용 |

모든 테스트는 `@ContextConfiguration(locations = "classpath*:META-INF/spring/context-*.xml")`로 테스트 설정을 로드합니다.

---

## 주요 의존성

모듈 POM은 버전을 선언하지 않으며 루트 POM `dependencyManagement`의 버전이 적용됩니다.

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.psl.dataaccess` | 4.3.1 | compile (Spring, MyBatis, iBatis 전이) |
| `org.springframework.data:spring-data-jpa` | 2.7.18 | compile (spring-core/beans/context/aop/tx/orm, slf4j, aspectjrt 제외 → 실행환경 버전 사용) |
| `org.hibernate:hibernate-entitymanager` | 5.6.15.Final | runtime (JPA 구현체, `javax.persistence` API) |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `org.apache.commons:commons-dbcp2` | 2.13.0 | compile |
| `org.springframework:spring-test`, `junit`, `easymock` | 5.3.39-egov.1 / 4.13.2 / 5.2.0 | test |
| `org.hsqldb:hsqldb` (`jdk8` 분류자), `com.mysql:mysql-connector-j` | 2.7.3 / 8.4.0 | test |

---

## 설정 및 사용 예시

`src/test/resources/META-INF/spring/context-data-jpa.xml`의 설정입니다.

```xml
<beans xmlns:jpa="http://www.springframework.org/schema/data/jpa" ...>

    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="dataSource"/>
        <property name="packagesToScan" value="org.egovframe.rte.psl.data.jpa.domain"/>
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter">
                <property name="database" value="${jpa.database}"/>
                <property name="generateDdl" value="${jpa.generateDdl}"/>
                <property name="showSql" value="${jpa.showSql}"/>
            </bean>
        </property>
        <property name="jpaProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.H2Dialect</prop>
                <prop key="hibernate.hbm2ddl.auto">create-drop</prop>
                <prop key="hibernate.id.new_generator_mappings">true</prop>
            </props>
        </property>
    </bean>

    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager">
        <property name="entityManagerFactory" ref="entityManagerFactory"/>
    </bean>

    <bean class="org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor"/>

    <jpa:repositories base-package="org.egovframe.rte.psl.data.jpa.repository"/>
</beans>
```

`context-datasource.xml`은 `<jdbc:embedded-database id="dataSource" type="HSQL"/>`로 인메모리 HSQLDB를 만들고,
`jdbc.properties`는 `jpa.database=H2`, `jpa.generateDdl=true`, `jpa.showSql=true`를 지정합니다.

Repository와 엔티티 예시(`src/test/java`):

```java
@Entity
@IdClass(UserPK.class)
public class User { @Id ... }

public interface UserRepository extends CrudRepository<User, UserPK> {
    User findByUsername(String username);
    List<User> findByLastname(String lastname);

    @Query("select u from User u where u.firstname = ?1")
    List<User> findByFirstname(String firstname);

    @Query("select u from User u where u.firstname = :name or u.lastname = :name")
    List<User> findByFirstnameOrLastname(@Param("name") String name);
}

public interface EmployeeRepository extends PagingAndSortingRepository<Employee, Long> {
}
```

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Persistence/org.egovframe.rte.psl.data.jpa -am
```

## 테스트

```bash
mvn test -pl Persistence/org.egovframe.rte.psl.data.jpa
```

테스트는 Spring `jdbc:embedded-database`(HSQLDB 인메모리)만 사용하므로 외부 서버가 필요 없습니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.psl.data.jpa</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>` 생략 가능.
`-egov.1` 포크 아티팩트(Spring Framework, Spring Data Commons 2.7.18-egov.1)를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록 필요.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.psl.dataaccess](../org.egovframe.rte.psl.dataaccess/README.md), [org.egovframe.rte.psl.reactive.r2dbc](../org.egovframe.rte.psl.reactive.r2dbc/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
