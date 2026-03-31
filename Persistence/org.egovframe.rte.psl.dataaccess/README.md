# egovframe-rte-psl-dataaccess

**영속성(Persistence)** 계층의 핵심으로, **MyBatis**와 레거시 **iBatis(sqlMap)** 을 함께 지원합니다. `EgovAbstractMapper`·`EgovAbstractDAO`, 타입 핸들러, iBatis `SqlMapClientTemplate` 호환 계층을 제공합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-psl-dataaccess:5.0.0`
- **Runtime**: Java 17 / Spring Framework 6.x / MyBatis 3.x / Jakarta Persistence API

---

## 특징

- **이중 스택**: 신규 개발은 MyBatis, 기존은 iBatis sqlMap 유지 가능
- **표준 베이스 클래스**: DAO/Mapper 상속으로 Egov 표준 CRUD 패턴
- **타입 핸들러**: Calendar, Timestamp 문자열 등 DB-앱 타입 매핑
- **Lob 지원**: iBatis용 BLOB/CLOB 핸들러

---

## 소스 디렉터리 구조

```
org/egovframe/rte/psl/
├── dataaccess/
│   ├── EgovAbstractDAO.java
│   ├── EgovAbstractMapper.java
│   ├── mapper/
│   │   ├── EgovMapper.java
│   │   └── MapperConfigurer.java
│   ├── typehandler/
│   │   ├── CalendarTypeHandler.java
│   │   ├── CalendarMapperTypeHandler.java
│   │   └── StringTimestampTypeHandler.java
│   └── util/
│       ├── EgovMap.java
│       └── CamelUtil.java
└── orm/
    ├── ObjectRetrievalFailureException.java
    ├── ObjectOptimisticLockingFailureException.java
    └── ibatis/                    # 레거시 iBatis
        ├── SqlMapClientOperations.java
        ├── SqlMapClientTemplate.java
        ├── SqlMapClientCallback.java
        ├── SqlMapClientFactoryBean.java
        └── support/
            ├── SqlMapClientDaoSupport.java
            ├── BlobSerializableTypeHandler.java
            ├── BlobByteArrayTypeHandler.java
            ├── ClobStringTypeHandler.java
            └── AbstractLobTypeHandler.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovAbstractMapper` | MyBatis Mapper 공통 |
| `EgovAbstractDAO` | 전통 DAO 패턴 베이스 |
| `EgovMapper` | 공통 Mapper 인터페이스 |
| `SqlMapClientTemplate` | iBatis 세대 템플릿 API |
| `EgovMap` | 카멜케이스 등 편의 Map |

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
  <artifactId>egovframe-rte-psl-dataaccess</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- JPA 확장: `egovframe-rte-psl-data-jpa` (본 모듈에 의존)
- 배치: `egovframe-rte-bat-core` 의 MyBatis ItemReader/Writer
- 상위 개요: 저장소 루트 `README.md`
