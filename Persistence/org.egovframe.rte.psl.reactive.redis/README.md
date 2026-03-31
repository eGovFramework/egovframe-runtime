# egovframe-rte-psl-reactive-redis

**Spring Data Redis**와 **Lettuce**를 이용한 **리액티브 Redis** 연동 모듈입니다. 캐시·세션·토큰 저장 등 인메모리 데이터를 논블로킹으로 다룹니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-psl-reactive-redis:5.0.0`
- **Runtime**: Java 17 / Spring Data Redis / Lettuce / Jackson(직렬화)
- **의존**: `egovframe-rte-fdl-cmmn`

---

## 특징

- Lettuce **비동기/리액티브** API
- `EgovRedisConfiguration`으로 커넥션 팩토리·템플릿 구성 표준화

---

## 소스 디렉터리 구조

```
org/egovframe/rte/psl/reactive/redis/
├── connect/
│   └── EgovRedisConfiguration.java
└── repository/
    └── EgovRedisRepository.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovRedisConfiguration` | Redis Standalone/Cluster 등 Bean 정의 |
| `EgovRedisRepository` | Redis 접근 리포지토리 베이스 |

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
  <artifactId>egovframe-rte-psl-reactive-redis</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 상위 개요: 저장소 루트 `README.md`
