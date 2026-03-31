# egovframe-rte-psl-data-jpa

**Spring Data JPA**와 **Hibernate**를 사용하는 영속성 확장 모듈입니다. `psl.dataaccess` 위에서 Egov 스타일 **JPA 리포지토리** 베이스를 제공합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-psl-data-jpa:5.0.0`
- **Runtime**: Java 17 / Spring Data JPA 3.x / Hibernate 6.x
- **의존**: `egovframe-rte-psl-dataaccess`

---

## 특징

- **표준 Repository**: `EgovJpaRepository`로 도메인 리포지토리 일관화
- **MyBatis·JPA 공존**: 동일 프로젝트에서 Mapper와 JPA 병행 가능(트랜잭션은 애플리케이션에서 통합)

---

## 소스 디렉터리 구조

```
org/egovframe/rte/psl/data/jpa/
└── repository/
    └── EgovJpaRepository.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovJpaRepository` | Egov 전자정부 표준에 맞춘 JPA Repository 마커/공통 인터페이스 |

엔티티·쿼리 메서드는 비즈니스 모듈에서 `JpaRepository` / `@Query` 등으로 확장합니다.

---

## 빌드

```bash
# 이 모듈 디렉터리에서 실행
mvn clean package
```

## 테스트

```bash
# 단위/통합 테스트 실행
mvn test
```

## 최소 설정 예시

```xml
<dependency>
  <groupId>org.egovframe.rte</groupId>
  <artifactId>egovframe-rte-psl-data-jpa</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- SQL 매퍼: `egovframe-rte-psl-dataaccess`
- 상위 개요: 저장소 루트 `README.md`
