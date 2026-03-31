# egovframe-rte-ptl-reactive

**Spring WebFlux** 기반 **리액티브 표현 계층** 패키지입니다. 스테레오타입 어노테이션(`@EgovController` 등), 공통 예외 응답, **도메인 검증 어노테이션**(주민번호·사업자번호·이메일 등)을 제공합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-ptl-reactive:5.0.0`
- **Runtime**: Java 17 / Spring WebFlux / Jakarta Validation(테스트·연동)
- **의존**: `egovframe-rte-fdl-logging`

---

## 특징

- **일관된 빈 스코프**: `@EgovRestController`, `@EgovController`, `@EgovService`, `@EgovRepository`
- **API 오류 포맷**: `EgovExceptionHandler`, `EgovExceptionResponse`, `EgovErrorCode`
- **한국 업무 규칙 검증**: RRN, CRN, 전화·휴대폰, 비밀번호 규칙 등 Constraint 어노테이션

---

## 소스 디렉터리 구조

```
org/egovframe/rte/ptl/reactive/
├── annotation/
│   ├── EgovRestController.java
│   ├── EgovController.java
│   ├── EgovService.java
│   └── EgovRepository.java
├── exception/
│   ├── EgovAbstractService.java
│   ├── EgovException.java
│   ├── EgovServiceException.java
│   ├── EgovExceptionHandler.java
│   ├── EgovExceptionResponse.java
│   └── EgovErrorCode.java
└── validation/
    ├── EgovValidation.java
    ├── EgovRrnCheck.java / EgovRrnCheckValidation.java
    ├── EgovCrnCheck.java / EgovCrnCheckValidation.java
    ├── EgovEmailCheck.java, EgovPhoneCheck.java, EgovMobilePhoneCheck.java
    ├── EgovKoreanCheck.java, EgovEnglishCheck.java, EgovCnCheck.java
    ├── EgovIPCheck.java, EgovPwdCheck.java, EgovNullCheck.java
    └── ... (각 *Check + *Validation 쌍)
```

---

## 주요 기능

| 영역 | 설명 |
|------|------|
| **어노테이션** | 전자정부 스타일 컴포넌트 마킹 |
| **예외 처리** | WebFlux 환경에서 JSON 에러 바디 표준화 |
| **검증** | Bean Validation `ConstraintValidator` 구현군 |

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
  <artifactId>egovframe-rte-ptl-reactive</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 리액티브 FDL: `egovframe-rte-fdl-reactive`
- 상위 개요: 저장소 루트 `README.md`
