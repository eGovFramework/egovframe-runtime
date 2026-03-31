# egovframe-rte-fdl-string

문자열·날짜·숫자·객체 관련 **공통 유틸리티**를 제공합니다. Spring `ApplicationContext`와 함께 사용되는 경량 FDL 모듈입니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-fdl-string:5.0.0`
- **Runtime**: Java 17 / Spring Framework 6.x
- **의존**: `egovframe-rte-fdl-logging`, `commons-codec`

---

## 특징

- **순수 유틸 성격**: 도메인 의존 없이 포맷·검증·변환 보조
- **다른 FDL과 결합**: `fdl.filehandling`, `fdl.security` 등에서 하위 의존

---

## 소스 디렉터리 구조

```
org/egovframe/rte/fdl/string/
├── EgovStringUtil.java
├── EgovDateUtil.java
├── EgovNumericUtil.java
└── EgovObjectUtil.java
```

---

## 주요 기능

| 클래스 | 용도 |
|--------|------|
| `EgovStringUtil` | null-safe 문자열 조작, trim, split, 인코딩 보조 등 |
| `EgovDateUtil` | 날짜/시간 포맷·계산 |
| `EgovNumericUtil` | 숫자 파싱·검증 |
| `EgovObjectUtil` | 객체 비교·toString 등 공통 처리 |

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
  <artifactId>egovframe-rte-fdl-string</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 상위 개요: 저장소 루트 `README.md`
