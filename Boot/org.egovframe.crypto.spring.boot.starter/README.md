# org.egovframe.crypto.spring.boot.starter

**Boot Starter 암복호화 모듈** 은 Spring Boot 2.7.18 자동구성(auto-configuration)으로 ARIA 블록암호 기반 암복호화, Jasypt PBE 암복호화, Digest(해시),
환경설정 값(DB 접속정보 등) 암복호화 서비스를 Bean으로 등록하는 스타터입니다.
Foundation 레이어의 `org.egovframe.rte.fdl.crypto`(Spring XML 네임스페이스 설정 방식)와 같은 서비스 구성을 `application.yml` 속성만으로 제공하며,
`fdl.crypto` 모듈에 의존하지 않고 `org.egovframe.boot.crypto` 패키지에 서비스 인터페이스·구현·ARIA 엔진을 자체 포함합니다. 다른 실행환경 모듈이 이 모듈에 의존하지는 않습니다.

- **Artifact**: `org.egovframe.boot:org.egovframe.crypto.spring.boot.starter:4.3.1`
- **Runtime**: Java 1.8 / Spring Boot 2.7.18 / Spring Framework 5.3.39-egov.1 / Jasypt 1.9.3
- **실행환경 의존**: 없음 (`org.egovframe.rte.*` 모듈을 의존하지 않음)
- **부모 POM**: 실행환경 루트 POM `org.egovframe.rte:org.egovframe.rte.root:4.3.1` (`spring-boot-starter-parent`가 아님)

---

## 특징

- `META-INF/spring.factories`로 등록되는 Spring Boot 2.7 방식 자동구성 (`EgovCryptoConfiguration`). 조건 어노테이션 없이 클래스패스에 있으면 항상 적용
- `org.egovframe.crypto.*` 속성(`@ConfigurationProperties`)으로 알고리즘, 키, 키 해시, 블록 크기, Digest 방식을 지정
- ARIA 256비트 키 암복호화 (`EgovARIACryptoServiceImpl`, ANSI X.923 패딩, 마스터 키 최대 32자)
- Jasypt `StandardPBEByteEncryptor`/`StandardPBEBigDecimalEncryptor` 기반 범용 암복호화 (`EgovGeneralCryptoServiceImpl`, 기본 알고리즘 `PBEWithSHA1AndDESede`)
- Jasypt `StandardByteDigester` 기반 Digest 생성·검증 (`EgovDigestServiceImpl`)
- 환경설정 값 암복호화 (`EgovEnvCryptoServiceImpl`: ARIA + URL-safe Base64 + `URLEncoder`)
- 모든 암복호화 서비스는 호출 시 전달된 패스워드가 `algorithm-key-hash`와 일치할 때(`EgovPasswordEncoder.checkPassword`)만 동작

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/boot/crypto/
├── EgovCryptoConfiguration.java          # 자동구성 클래스 (서비스 Bean 5종 + MessageSource)
├── EgovCryptoProperties.java             # @ConfigurationProperties(prefix="org.egovframe.crypto")
├── EgovPasswordEncoder.java              # Jasypt ConfigurablePasswordEncryptor 기반 패스워드 해시·검증
└── service/
    ├── EgovCryptoService.java            # byte[]/BigDecimal/File 암복호화 공통 인터페이스
    ├── EgovARIACryptoService.java        # ARIA 암복호화 인터페이스 (EgovCryptoService 상속)
    ├── EgovGeneralCryptoService.java     # PBE 암복호화 인터페이스 (알고리즘 지정 가능)
    ├── EgovDigestService.java            # Digest 인터페이스
    ├── EgovEnvCryptoService.java         # 환경설정 값 암복호화 인터페이스
    └── impl/
        ├── EgovARIACryptoServiceImpl.java
        ├── EgovGeneralCryptoServiceImpl.java
        ├── EgovDigestServiceImpl.java
        ├── EgovEnvCryptoServiceImpl.java
        ├── ARIACipher.java               # ARIAEngine 래퍼 (마스터 키 패딩, 256비트)
        └── aria/
            ├── ARIAEngine.java           # ARIA 블록암호 엔진
            ├── AnsiX923Padding.java      # ANSI X.923 패딩 구현
            └── CryptoPadding.java        # 패딩 인터페이스

src/main/resources/
├── META-INF/spring.factories             # EnableAutoConfiguration 등록
├── messages.properties                   # MessageSource 기본 메시지
├── messages_ko_KR.properties
└── messages_en_US.properties
```

`src/test`는 없습니다.

---

## 자동구성

### 등록 방식

`src/main/resources/META-INF/spring.factories` (Spring Boot 2.7 방식. `AutoConfiguration.imports` 파일은 사용하지 않음)

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.egovframe.boot.crypto.EgovCryptoConfiguration
```

### 활성화 조건

`EgovCryptoConfiguration`에는 `@ConditionalOnProperty` 등 조건 어노테이션이 없습니다. 스타터가 클래스패스에 있으면 항상 적용되며,
`@EnableConfigurationProperties(EgovCryptoProperties.class)`로 속성 클래스를 활성화합니다.

### 생성되는 Bean

| Bean 이름 | 타입 | 설명 |
|---|---|---|
| `egovEnvPasswordEncoderService` | `EgovPasswordEncoder` | `algorithm`, `algorithm-key-hash`로 초기화. 다른 서비스의 패스워드 검증에 사용 |
| `egovEnvARIACryptoService` | `EgovARIACryptoServiceImpl` | ARIA 암복호화. `crypto-block-size`를 파일 처리 버퍼 크기로 사용(16의 배수로 보정) |
| `egovEnvCryptoService` | `EgovEnvCryptoServiceImpl` | 환경설정 값 암복호화. `algorithm-key`를 ARIA 패스워드로 사용 |
| `egovGeneralCryptoService` | `EgovGeneralCryptoServiceImpl` | Jasypt PBE 암복호화 (기본 `PBEWithSHA1AndDESede`, `setAlgorithm()`으로 변경) |
| `egovDigestService` | `EgovDigestServiceImpl` | `algorithm`, `plain-digest`로 초기화한 Digest 서비스 |
| `reloadableResourceBundleMessageSource` | `ReloadableResourceBundleMessageSource` | basename `classpath:messages`, UTF-8 |

---

## 설정 속성

prefix: `org.egovframe.crypto` (`EgovCryptoProperties`). 속성 키는 Spring Boot relaxed binding을 따르므로 kebab-case와 필드명(camelCase) 모두 사용할 수 있습니다.

| 속성 키 (`org.egovframe.crypto.` 생략) | 필드 | 타입 | 기본값 | 설명 |
|---|---|---|---|---|
| `initial` | `initial` | boolean | `true` | 선언만 되어 있고 현재 자동구성에서 사용하지 않음 |
| `crypto` | `crypto` | boolean | `true` | `EgovEnvCryptoServiceImpl.isCrypto()`로 보관. 스타터 내부에서 암호화를 끄는 용도로 쓰지는 않으며 적용 프로젝트에서 판단용 |
| `algorithm` | `algorithm` | String | 없음 | 패스워드 해시·Digest 알고리즘 (`EgovEnvCryptoService` Javadoc 예: `MD5`, `SHA-1`, `SHA-256`) |
| `algorithm-key` | `algorithmKey` | String | 없음 | 환경설정 암복호화에 사용할 키(패스워드). ARIA 마스터 키는 최대 32자까지 사용 |
| `algorithm-key-hash` | `algorithmKeyHash` | String | 없음 | `algorithm-key`를 `algorithm`으로 해시한 값. `EgovPasswordEncoder.encryptPassword(algorithmKey)` 결과를 넣음 |
| `crypto-block-size` | `cryptoBlockSize` | int | `1024` | 파일 암복호화 버퍼 크기 |
| `plain-digest` | `plainDigest` | boolean | `true` | `true`: 반복 1회·Salt 없음, `false`: 반복 1000회·Salt 8바이트 (`EgovDigestServiceImpl`) |
| `crypto-db-driver` | `cryptoDBDriver` | String | `""` | DB 드라이버 클래스. `EgovEnvCryptoServiceImpl` 필드에 보관만 하며 스타터가 DB에 접속하지는 않음 |
| `crypto-db-url` | `cryptoDBUrl` | String | `""` | DB 접속 URL (보관용) |
| `crypto-db-username` | `cryptoDBUsername` | String | `""` | DB 계정 (보관용) |
| `crypto-db-password` | `cryptoDBPassword` | String | `""` | DB 패스워드 (보관용) |

이 모듈은 `additional-spring-configuration-metadata.json`을 제공하지 않습니다. `spring-boot-configuration-processor`가 optional 의존성으로 선언되어 있으나
루트 POM의 `maven-compiler-plugin`이 `annotationProcessorPaths`에 Lombok만 지정하므로 빌드 시 `spring-configuration-metadata.json`은 생성되지 않습니다.

---

## 주요 클래스

| 클래스 | 설명 |
|---|---|
| `EgovPasswordEncoder` | Jasypt `ConfigurablePasswordEncryptor`(plainDigest) 기반. `encryptPassword(plain)`, `checkPassword(plain)`(설정된 `hashedPassword`와 비교), `checkPassword(plain, encrypted)` |
| `EgovCryptoService` | `encrypt/decrypt(byte[], password)`, `encrypt/decrypt(BigDecimal, password)`, `encrypt/decrypt(File, password, File)` |
| `EgovARIACryptoServiceImpl` | `ARIACipher`로 byte[]·파일 암복호화. 파일은 Base64 문자열로 변환 후 ARIA 암호화하여 Base64로 저장. `BigDecimal`은 `UnsupportedOperationException` |
| `EgovGeneralCryptoServiceImpl` | Jasypt PBE. byte[], `BigDecimal`, 파일(블록 단위 Base64 줄 저장) 지원. `algorithm` 기본 `PBEWithSHA1AndDESede` |
| `EgovDigestServiceImpl` | `digest(byte[])`, `matches(message, digest)`. `plainDigest` 여부에 따라 반복 횟수·Salt 크기 결정 |
| `EgovEnvCryptoServiceImpl` | `encrypt(String)`: ARIA 암호화 → URL-safe Base64 → `URLEncoder`, `decrypt(String)`: 역순. `encryptNone/decryptNone`은 `URLEncoder` 없이 처리. 예외 시 입력값을 그대로 반환 |
| `ARIACipher` | 마스터 키를 ANSI X.923 패딩으로 32바이트로 맞춰 `ARIAEngine(256)`으로 암복호화 |
| `ARIAEngine`, `AnsiX923Padding`, `CryptoPadding` | ARIA 블록암호 엔진과 패딩 구현 |

---

## 설정 및 사용 예시

`application.yml` (아래 키는 모두 `EgovCryptoProperties` 필드에 실제 존재하는 키입니다)

```yaml
org:
  egovframe:
    crypto:
      crypto: true
      algorithm: SHA-256
      algorithm-key: egovframe
      algorithm-key-hash: "<EgovPasswordEncoder.encryptPassword(algorithm-key) 결과>"
      crypto-block-size: 1024
      plain-digest: true
```

`algorithm-key-hash` 값은 `EgovPasswordEncoder`로 생성합니다.

```java
EgovPasswordEncoder encoder = new EgovPasswordEncoder();
encoder.setAlgorithm("SHA-256");
String hash = encoder.encryptPassword("egovframe");   // 이 값을 algorithm-key-hash에 설정
```

환경설정 값 암복호화

```java
@Autowired
@Qualifier("egovEnvCryptoService")
private EgovEnvCryptoService cryptoService;

String enc = cryptoService.encrypt("jdbc-password");   // algorithm-key로 ARIA 암호화 + URL-safe Base64 + URLEncoder
String dec = cryptoService.decrypt(enc);
```

ARIA / Digest 직접 사용

```java
@Autowired
@Qualifier("egovEnvARIACryptoService")
private EgovARIACryptoService ariaService;

@Autowired
@Qualifier("egovDigestService")
private EgovDigestService digestService;

byte[] encrypted = ariaService.encrypt("data".getBytes(StandardCharsets.UTF_8), "egovframe"); // 패스워드는 algorithm-key와 같아야 함
byte[] decrypted = ariaService.decrypt(encrypted, "egovframe");

byte[] digest = digestService.digest("data".getBytes(StandardCharsets.UTF_8));
boolean ok = digestService.matches("data".getBytes(StandardCharsets.UTF_8), digest);
```

암복호화 메서드에 전달하는 패스워드가 `algorithm-key-hash`와 일치하지 않으면 `IllegalArgumentException("password not matched!!!")`이 발생합니다.

---

## 주요 의존성

모듈 POM은 버전을 선언하지 않으며 루트 POM `dependencyManagement`에서 관리합니다.
Spring Boot는 `spring-boot-dependencies` BOM 전체가 아니라 아래 아티팩트만 개별 관리하고, Spring Framework는 루트 POM의 `spring-framework-bom`(5.3.39-egov.1)으로 맞춥니다.

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.springframework.boot:spring-boot-autoconfigure` | 2.7.18 | compile |
| `org.springframework.boot:spring-boot-autoconfigure-processor` | 2.7.18 | optional |
| `org.springframework.boot:spring-boot-configuration-processor` | 2.7.18 | optional |
| `org.springframework.boot:spring-boot-starter-web` | 2.7.18 | compile (`spring-webmvc` 5.3.39-egov.1) |
| `commons-codec:commons-codec` | 1.18.0 | compile (Base64) |
| `commons-io:commons-io` | 2.18.0 | compile (파일 읽기·쓰기) |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `org.jasypt:jasypt` | 1.9.3 | compile (PBE, Digest, PasswordEncryptor) |
| `org.projectlombok:lombok` | 1.18.36 | provided |

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Boot/org.egovframe.crypto.spring.boot.starter -am
```

결과물: `target/org.egovframe.crypto.spring.boot.starter-4.3.1.jar`, `-sources.jar`, `-javadoc.jar`

## 테스트

이 모듈에는 `src/test`가 없어 실행되는 테스트가 없습니다. `mvn test -pl Boot/org.egovframe.crypto.spring.boot.starter`는 컴파일만 수행합니다.

---

## Maven 의존성 추가

Spring Boot 적용 프로젝트는 [org.egovframe.boot.starter.parent](../../Parent/org.egovframe.boot.starter.parent/README.md) 4.3.1을 부모 POM으로 상속하고
스타터를 버전 없이 추가합니다. 부모 POM이 실행환경 루트 POM을 BOM으로 가져오므로 이 스타터(4.3.1)와 Spring 포크 버전이 함께 적용됩니다.

```xml
<parent>
    <groupId>org.egovframe.boot</groupId>
    <artifactId>org.egovframe.boot.starter.parent</artifactId>
    <version>4.3.1</version>
</parent>

<dependencies>
    <dependency>
        <groupId>org.egovframe.boot</groupId>
        <artifactId>org.egovframe.crypto.spring.boot.starter</artifactId>
    </dependency>
</dependencies>
```

부모 POM을 상속하지 않는 경우 `<version>4.3.1</version>`을 명시합니다.
`-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소를 프로젝트 `pom.xml` 또는 `settings.xml`에 등록해야 합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 부모 POM: [org.egovframe.boot.starter.parent](../../Parent/org.egovframe.boot.starter.parent/README.md)
- 같은 기능의 Foundation 모듈(Spring XML 설정 방식): [org.egovframe.rte.fdl.crypto](../../Foundation/org.egovframe.rte.fdl.crypto/README.md)
- 관련 스타터: [org.egovframe.access.spring.boot.starter](../org.egovframe.access.spring.boot.starter/README.md), [org.egovframe.security.spring.boot.starter](../org.egovframe.security.spring.boot.starter/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
