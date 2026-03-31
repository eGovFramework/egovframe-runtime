# egovframe-rte-fdl-reactive

**Spring WebFlux** 환경에서 쓰기 위한 소규모 **리액티브 공통** 빌딩 블록입니다. MDC(로깅 컨텍스트)와 시퀀스 생성 보조를 제공합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-fdl-reactive:5.0.0`
- **Runtime**: Java 17 / Spring Framework 6.x (`spring-webflux`)
- **의존**: `egovframe-rte-fdl-logging`

---

## 특징

- **비동기 추적**: Reactor 체인에서 MDC 값 전파·정리 (`EgovMdcContextConfig`)
- **리액티브 채번**: `EgovSequenceGenerator` — 논블로킹 파이프라인에서 시퀀스 확보 패턴

---

## 소스 디렉터리 구조

```
org/egovframe/rte/fdl/reactive/
├── idgnr/
│   └── EgovSequenceGenerator.java
└── logging/
    └── EgovMdcContextConfig.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovMdcContextConfig` | WebFlux 필터/연산자와 맞물리는 MDC 설정 |
| `EgovSequenceGenerator` | 리액티브 스택용 시퀀스 생성 유틸 |

표현 계층 WebFlux 확장은 `egovframe-rte-ptl-reactive` 모듈을 참고하세요.

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
  <artifactId>egovframe-rte-fdl-reactive</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 상위 개요: 저장소 루트 `README.md`
