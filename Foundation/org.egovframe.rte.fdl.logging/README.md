# egovframe-rte-fdl-logging

다른 런타임 모듈이 공통으로 사용하는 **로깅 기반** 라이브러리입니다. **Apache Log4j 2**와 **SLF4J** 브리지를 중심으로 연동 리소스 정리·DB 커넥션 팩토리 유틸을 제공합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-fdl-logging:5.0.0`
- **Runtime**: Java 17 / Spring Framework 6.x

---

## 특징

- **Log4j2 + SLF4J**: `log4j-core`, `log4j-slf4j2-impl`, `jcl-over-slf4j`, `log4j-over-slf4j`
- **경량 헬퍼**: JDK 로거 래핑, 리소스 해제 유틸
- **선택적 DB 연동**: 로그/모니터링용 `ConnectionFactory` 패턴 (`EgovConnectionFactory`)

---

## 소스 디렉터리 구조

```
org/egovframe/rte/fdl/logging/
├── db/
│   └── EgovConnectionFactory.java
└── util/
    ├── EgovJdkLogger.java
    └── EgovResourceReleaser.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovJdkLogger` | `java.util.logging`과의 연계 보조 |
| `EgovResourceReleaser` | AutoCloseable 등 리소스 안전 해제 |
| `EgovConnectionFactory` | DataSource/Connection 획득 추상화 (로깅·진단 시나리오) |

> 대부분의 로그 출력 정책은 애플리케이션의 `log4j2.xml` 등 설정으로 제어합니다.

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
  <artifactId>egovframe-rte-fdl-logging</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 상위 개요: 저장소 루트 `README.md`
