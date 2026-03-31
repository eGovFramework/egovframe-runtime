# egovframe-rte-fdl-crypto

`egovframe-rte-fdl-crypto`는 **대칭키 암복호화(ARIA / PBE)**, **Digest(해시)**, **환경설정(DB 접속정보 등) 암복호화 유틸**을 제공하는 라이브러리입니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-fdl-crypto:5.0.0`
- **Runtime**: Java 17 / Spring Framework 6.x

---

## 소스 디렉터리 구조

```
org/egovframe/rte/fdl/crypto/
├── config/                      # EgovCryptoConfig*, Reader, Java Config
├── EgovCryptoService.java       # 및 ARIA/General/Env/Digest·PasswordEncoder 인터페이스
├── impl/
│   ├── EgovARIACryptoServiceImpl.java
│   ├── EgovGeneralCryptoServiceImpl.java
│   ├── EgovDigestServiceImpl.java
│   ├── EgovEnvCryptoServiceImpl.java
│   ├── ARIACipher.java
│   └── aria/                    # ARIA 엔진·패딩
└── EgovPasswordEncoder.java
```

---

## 주요 제공 기능

- **ARIA 기반 암복호화**: `EgovARIACryptoService` (`EgovARIACryptoServiceImpl`)
- **General(PBE) 암복호화**: `EgovGeneralCryptoService` (`EgovGeneralCryptoServiceImpl`)
- **Digest(해시) 및 검증**: `EgovDigestService` (`EgovDigestServiceImpl`)
- **환경설정 값 암복호화(주로 DB 계정/비밀번호 등)**: `EgovEnvCryptoService` (`EgovEnvCryptoServiceImpl`)
- **패스워드 검증/인코딩**: `EgovPasswordEncoder` (Jasypt 기반)

> 이 모듈의 암복호화 서비스는 공통적으로 `EgovPasswordEncoder.checkPassword(password)`가 `true`일 때만 동작하도록 설계되어 있습니다.

---

## 클래스 구성 및 특징

### 설정/부트스트랩 (`org.egovframe.rte.fdl.crypto.config`)

- **`EgovCryptoConfig`**
  - crypto 통합 설정 POJO
  - 주요 필드:
    - `initial`, `crypto`
    - `algorithm` (Digest 및 일부 서비스에서 사용)
    - `algorithmKey`, `algorithmKeyHash`
    - `cryptoBlockSize`
    - `cryptoPropertyLocation` (환경설정 파일 위치)
    - `plainDigest` (Digest를 단순/강화 모드로 구분)

- **`EgovCryptoConfigReader`**
  - properties 파일을 읽어 `EgovCryptoConfig`로 매핑
  - 기본 설정 파일 경로(클래스패스): `egovframework/egovProps/conf/egov-crypto-config.properties`
  - 서비스에서 `Globals.CryptoConfigPath`를 주면 해당 경로를 우선 로드

- **`EgovCryptoConfiguration`**
  - (기존 XML의 대체) Java Config
  - 아래 Bean들을 구성:
    - `egovEnvPasswordEncoderService` (`EgovPasswordEncoder`)
    - `egovARIACryptoService`
    - `egovGeneralCryptoService`
    - `egovDigestService`
    - `egovEnvCryptoService` (환경설정 기반 암복호화)
  - **보안 주의**: `algorithmKey`가 기본값 `"egovframe"`이면 치명적 보안 위험 가능성을 경고 출력

### 암복호화/해시 (`org.egovframe.rte.fdl.crypto`, `...impl`)

- **`EgovCryptoService`**
  - `byte[]`, `BigDecimal`, `File` 기반 암복호화 API 정의

- **`EgovGeneralCryptoServiceImpl`**
  - Jasypt `StandardPBEByteEncryptor`, `StandardPBEBigDecimalEncryptor` 사용
  - 파일 처리 시 block 단위로 Base64 라인 저장/복원 방식 사용

- **`EgovARIACryptoServiceImpl`**
  - `ARIACipher`로 ARIA(256bit) 암복호화 수행
  - `blockSize`는 **16의 배수로 자동 보정**
  - `BigDecimal` 암복호화는 지원하지 않음(UnsupportedOperationException)

- **`ARIACipher` / `impl.aria.*`**
  - ARIA 엔진 및 padding(Ansi X9.23) 구현
  - masterKey는 최대 32byte 기준으로 사용(초과 시 절단)

- **`EgovDigestServiceImpl`**
  - Jasypt `StandardByteDigester` 기반 digest/matches 제공
  - `plainDigest=false`일 때 salt + 반복(iterations=1000, salt=8byte)로 강화 모드

- **`EgovEnvCryptoServiceImpl`**
  - `Globals.DbType`와 `Globals.{DbType}.*` 항목을 `EgovPropertyService`로 읽어옴
  - `crypto=true`이면 getter에서 복호화하여 DB 설정 값을 제공
  - `encrypt()`는 URL-safe Base64 + `URLEncoder` 조합(프로퍼티에 넣기 쉬운 형태)
  - `encryptNone()`은 URL 인코딩 없이 Base64만 사용

---

## 다른 서비스에서 사용하는 방법

### 1) 의존성 추가 (Maven)

서비스 프로젝트의 `pom.xml`에 추가합니다.

```xml
<dependency>
  <groupId>org.egovframe.rte</groupId>
  <artifactId>egovframe-rte-fdl-crypto</artifactId>
  <version>5.0.0</version>
</dependency>
```

### 2) 설정 파일 준비 (`egov-crypto-config.properties`)

기본 탐색 경로는 클래스패스의 `egovframework/egovProps/conf/egov-crypto-config.properties` 입니다.

예시:

```properties
id=egovCryptoConfig

# 초기화 관련 플래그(서비스 구성에 따라 사용)
initial=true

# 환경설정(DB 계정 등) 복호화 사용 여부
crypto=true

# Digest 알고리즘 및 PasswordEncoder 알고리즘 힌트
algorithm=SHA-256

# 반드시 변경 권장: 기본값 "egovframe"는 위험 경고 대상
algorithmKey=change-this-key

# passwordEncoder.checkPassword()에 사용되는 기준(해시된 패스워드)
algorithmKeyHash=REPLACE_WITH_HASHED_PASSWORD

# 파일 처리 blockSize (ARIA는 16의 배수로 보정됨)
cryptoBlockSize=1024

# EgovEnvCryptoService가 읽을 프로퍼티 파일 위치
cryptoPropertyLocation=classpath:/egovframework/egovProps/globals.properties

# Digest 강화모드 여부(false면 salt/iteration 적용)
plainDigest=false
```

### 3) 서비스 설정에 `Globals.CryptoConfigPath` 지정

서비스에서 `Globals.CryptoConfigPath`를 지정하면 해당 경로의 설정 파일을 우선 로드합니다.

```properties
Globals.CryptoConfigPath=egovframework/egovProps/conf/egov-crypto-config.properties
```

경로는 다음 형태를 지원합니다.

- `classpath:...`
- `file:/absolute/path/...`
- 상대경로(기본적으로 classpath로 해석)

### 4) 스프링 컨텍스트에 설정 클래스 등록

서비스(Spring Boot/Spring)에서 `EgovCryptoConfiguration`이 로딩되도록 구성합니다.

```java
@Import(org.egovframe.rte.fdl.crypto.config.EgovCryptoConfiguration.class)
public class CryptoConfig {
}
```

그러면 아래 Bean 이름으로 주입해서 사용할 수 있습니다.

- `egovARIACryptoService`
- `egovGeneralCryptoService`
- `egovDigestService`
- `egovEnvCryptoService`
- `egovEnvPasswordEncoderService`

---

## 사용 예시

### ARIA 암복호화 (`egovARIACryptoService`)

```java
@Autowired @Qualifier("egovARIACryptoService")
private org.egovframe.rte.fdl.crypto.EgovARIACryptoService ariaCrypto;

public String encryptToBase64(String plain, String password) {
  byte[] enc = ariaCrypto.encrypt(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8), password);
  return java.util.Base64.getEncoder().encodeToString(enc);
}

public String decryptFromBase64(String encBase64, String password) {
  byte[] enc = java.util.Base64.getDecoder().decode(encBase64);
  byte[] dec = ariaCrypto.decrypt(enc, password);
  return new String(dec, java.nio.charset.StandardCharsets.UTF_8);
}
```

### General(PBE) 암복호화 (`egovGeneralCryptoService`)

```java
@Autowired @Qualifier("egovGeneralCryptoService")
private org.egovframe.rte.fdl.crypto.EgovGeneralCryptoService generalCrypto;

public byte[] encryptBytes(byte[] data, String password) {
  return generalCrypto.encrypt(data, password);
}
```

### Digest(해시) 생성/검증 (`egovDigestService`)

```java
@Autowired @Qualifier("egovDigestService")
private org.egovframe.rte.fdl.crypto.EgovDigestService digestService;

public boolean verify(byte[] message, byte[] digest) {
  return digestService.matches(message, digest);
}
```

### DB 접속정보(환경설정) 암복호화 (`egovEnvCryptoService`)

`EgovEnvCryptoServiceImpl.init()`는 `cryptoPropertyLocation`으로 지정된 프로퍼티에서 아래 키들을 읽습니다.

- `Globals.DbType`
- `Globals.{DbType}.DriverClassName`
- `Globals.{DbType}.Url`
- `Globals.{DbType}.UserName`
- `Globals.{DbType}.Password`

서비스에서 `crypto=true`로 설정하면, `getUrl()`, `getUsername()`, `getPassword()` 호출 시 복호화된 값을 반환합니다.

---

## 보안 주의사항

- **`algorithmKey`는 반드시 변경**하세요. 기본값 `"egovframe"`는 보안 위험이 커서 라이브러리에서 경고를 출력합니다.
- `algorithmKeyHash`는 `EgovPasswordEncoder.checkPassword()`에 사용되므로, 서비스에서 관리하는 **신뢰 가능한 값**으로 설정해야 합니다.

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
  <artifactId>egovframe-rte-fdl-crypto</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 라이선스

Apache License 2.0

