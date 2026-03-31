# egovframe-rte-fdl-idgnr

업무 키·일련번호 등 **고유 ID 채번**을 표준화합니다. DB 시퀀스·채번 테이블·UUID·전략 패턴을 지원합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-fdl-idgnr:5.0.0`
- **Runtime**: Java 17 / Spring Framework 6.x
- **의존**: `egovframe-rte-fdl-cmmn`

---

## 특징

- **다중 채번 방식**: 시퀀스, 테이블 블록, UUID
- **전략 패턴**: `EgovIdGnrStrategy`로 규칙 교체 가능
- **Spring 연동**: `AbstractIdGnrService` 계열로 Bean 주입·트랜잭션과 결합

---

## 소스 디렉터리 구조

```
org/egovframe/rte/fdl/idgnr/
├── EgovIdGnrService.java
├── EgovIdGnrStrategy.java
└── impl/
    ├── AbstractIdGnrService.java
    ├── AbstractDataIdGnrService.java
    ├── AbstractDataBlockIdGnrService.java
    ├── EgovSequenceIdGnrServiceImpl.java
    ├── EgovTableIdGnrServiceImpl.java
    ├── EgovUUIdGnrServiceImpl.java
    ├── Base64.java
    └── strategy/
        └── EgovIdGnrStrategyImpl.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovSequenceIdGnrServiceImpl` | DB 시퀀스 기반 채번 |
| `EgovTableIdGnrServiceImpl` | 채번 테이블(블록 단위 포함) |
| `EgovUUIdGnrServiceImpl` | UUID 생성 |
| `EgovIdGnrStrategy` / `EgovIdGnrStrategyImpl` | 채번 정책 커스터마이징 |

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
  <artifactId>egovframe-rte-fdl-idgnr</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 상위 개요: 저장소 루트 `README.md`
