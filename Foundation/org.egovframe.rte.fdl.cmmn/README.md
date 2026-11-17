# egovframe-rte-fdl-cmmn

**Foundation 공통(Common)** 레이어입니다. 서비스 베이스 클래스, 예외/예외 처리 연계, 호출 추적(Trace) AOP를 제공하며 다른 FDL·표준 컴포넌트가 자주 의존하는 허브 역할을 합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-fdl-cmmn:5.1.0`
- **Runtime**: Java 17 / Spring Framework 6.2
- **의존**: `egovframe-rte-fdl-logging`

---

## 특징

- **서비스 추상화**: `EgovAbstractServiceImpl` 등 공통 서비스 패턴
- **예외 체계**: 비즈니스·기반 예외, 핸들러 매니저로 일관 처리
- **횡단 관심사**: `ExceptionTransfer` 등 AspectJ 기반 예외 전파/변환
- **추적(Trace)**: LeaveaTrace, TraceHandler / Manager 계층

---

## 소스 디렉터리 구조

```
org/egovframe/rte/fdl/cmmn/
├── EgovAbstractServiceImpl.java
├── aspect/
│   └── ExceptionTransfer.java
├── code/
│   ├── EgovCode.java
│   ├── EgovCodeCache.java
│   ├── EgovCodeLoader.java
│   ├── EgovInMemoryCodeLoader.java
│   └── EgovJdbcCodeLoader.java
├── exception/
│   ├── BaseException.java
│   ├── BaseRuntimeException.java
│   ├── EgovBizException.java
│   ├── EgovBizRuntimeException.java
│   ├── EgovErrorMessage.java
│   ├── FdlException.java
│   ├── handler/
│   │   ├── ExceptionHandler.java
│   │   └── EgovLoggingExceptionHandler.java
│   └── manager/
│       ├── AbstractExceptionHandleManager.java
│       ├── DefaultExceptionHandleManager.java
│       └── ExceptionHandlerService.java
├── message/
│   ├── EgovDbMessageSource.java
│   └── EgovWildcardMessageSource.java
└── trace/
    ├── LeaveaTrace.java
    ├── handler/
    │   ├── TraceHandler.java
    │   └── DefaultTraceHandler.java
    └── manager/
        ├── AbstractTraceHandleManager.java
        ├── DefaultTraceHandleManager.java
        └── TraceHandlerService.java
```

---

## 주요 기능

| 구분 | 클래스/역할 |
|------|-------------|
| 서비스 템플릿 | `EgovAbstractServiceImpl` |
| 예외 | `EgovBizException`, `EgovBizRuntimeException`(unchecked), `FdlException`, `BaseException` 계열, `EgovErrorMessage`(구조화 오류 메시지) |
| 예외 처리 매니저 | `AbstractExceptionHandleManager`, `DefaultExceptionHandleManager`, `ExceptionHandlerService`, `EgovLoggingExceptionHandler`(기본 로깅 핸들러) |
| 공통코드 | `EgovCodeCache`(불변 스냅숏 캐시·`reload()`), `EgovCodeLoader` SPI, `EgovJdbcCodeLoader`, `EgovInMemoryCodeLoader` |
| 메시지 소스 | `EgovWildcardMessageSource`(basename 와일드카드), `EgovDbMessageSource`(DB 메시지) |
| AOP | `ExceptionTransfer` |
| 추적 | `LeaveaTrace`, `TraceHandler`, `DefaultTraceHandleManager` 등 |

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
  <artifactId>egovframe-rte-fdl-cmmn</artifactId>
  <version>5.1.0</version>
</dependency>
```

---

## 참고

- 상위 개요: 저장소 루트 `README.md`
