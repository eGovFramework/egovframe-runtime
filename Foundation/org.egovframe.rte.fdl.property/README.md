# egovframe-rte-fdl-property

**Commons Configuration 2**와 Spring을 이용해 **프로퍼티·설정 소스**를 통합 로딩하고, 필요 시 **DB 기반 PropertySource**를 초기화합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-fdl-property:5.0.0`
- **Runtime**: Java 17 / Spring Framework 6.x
- **의존**: `egovframe-rte-fdl-cmmn`

---

## 특징

- **다중 소스 설정**: XML/properties 등 구성 파일 통합
- **DB 프로퍼티**: `DbPropertySource` 및 초기화기로 런타임/배치 환경값 주입
- **암호화 연계**: `fdl.crypto`와 함께 쓰면 민감 설정 복호화 패턴 구성 가능

---

## 소스 디렉터리 구조

```
org/egovframe/rte/fdl/property/
├── EgovPropertyService.java
├── impl/
│   └── EgovPropertyServiceImpl.java
└── db/
    ├── DbPropertySource.java
    ├── DbPropertySourceDelegate.java
    └── initializer/
        └── DBPropertySourceInitializer.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovPropertyService` / `Impl` | 설정 조회·통합 서비스 인터페이스 및 구현 |
| `DbPropertySource` | DB 테이블 등에서 key-value 형태로 프로퍼티 제공 |
| `DBPropertySourceInitializer` | Spring `Environment`에 DB 소스 등록 |

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
  <artifactId>egovframe-rte-fdl-property</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 상위 개요: 저장소 루트 `README.md`
