# egovframe-boot-starter-crypto

전자정부 표준프레임워크의 Spring Boot 암호화 스타터 라이브러리입니다.

## 📋 개요

`egovframe-boot-starter-crypto`는 Spring Boot 애플리케이션에서 암호화 기능을 쉽게 사용할 수 있도록 제공하는 자동 구성(Auto Configuration) 스타터입니다. ARIA 암호화 알고리즘을 포함한 다양한 암호화 서비스를 제공합니다.

## ✨ 주요 기능

- **ARIA 암호화 서비스**: 한국 국가 표준 암호화 알고리즘인 ARIA를 사용한 바이트 · 숫자 · 파일 암호화/복호화
- **일반 암호화 서비스**: PBE(Jasypt) 기반 범용 암호화/복호화
- **환경설정 값 암호화**: 설정 파일의 민감한 값 암호화/복호화, **DB 접속 정보(Globals.DbType 등) 자동 복호화**
- **Digest 서비스**: 해시 알고리즘을 사용한 메시지 다이제스트 생성 및 검증
- **패스워드 인코더**: 암호화 키(비밀번호) 해시 생성 및 검증

## 🔧 요구사항

- Java 17 이상
- Spring Boot 3.5.16 이상 (`egovframe-boot-starter-parent` 5.1.0 기준)

## 📦 설치

### egovframe-boot-starter-parent 사용 시

`egovframe-boot-starter-parent` 5.1.0 을 parent 로 사용하면 이 스타터가 **자동으로 포함**됩니다(access · crypto · logging · security 4종). 별도 의존성 선언이 필요 없습니다.

### Maven

```xml
<dependency>
    <groupId>org.egovframe.boot</groupId>
    <artifactId>egovframe-boot-starter-crypto</artifactId>
    <version>5.1.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'org.egovframe.boot:egovframe-boot-starter-crypto:5.1.0'
```

## ⚙️ 설정

이 스타터는 **활성화 조건 없이** 항상 아래 Bean 을 등록합니다(설정이 없어도 기동은 실패하지 않습니다). 실제로 암호화 기능을 쓰려면 `application.yml` 또는 `application.properties` 에 다음 설정을 추가하세요.

### application.yml

```yaml
org:
  egovframe:
    crypto:
      crypto: true
      algorithm: SHA-256
      algorithm-key: your-secure-key
      algorithm-key-hash: <algorithm-key 의 해시값>
      crypto-block-size: 1024
      plain-digest: false
      # 선택 : 암호화된 DB 접속 정보가 있는 파일
      crypto-property-location: classpath:/egovframework/egovProps/globals.properties
```

### application.properties

```properties
org.egovframe.crypto.crypto=true
org.egovframe.crypto.algorithm=SHA-256
org.egovframe.crypto.algorithm-key=your-secure-key
org.egovframe.crypto.algorithm-key-hash=<algorithm-key 의 해시값>
org.egovframe.crypto.crypto-block-size=1024
org.egovframe.crypto.plain-digest=false
```

### 설정 속성 설명

| 속성 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `algorithm` | String | - | 패스워드 인코더 · Digest 서비스의 해시 알고리즘 (예: SHA-256) |
| `algorithm-key` | String | - | ARIA 암호화 키(비밀번호). **"egovframe" 사용 금지** |
| `algorithm-key-hash` | String | - | `algorithm-key` 의 해시값. 암호화/복호화 시 키 검증에 사용 |
| `crypto-block-size` | int | `1024` | 파일 암호화 블록 크기. **0 이하이면 기동 실패**, ARIA 는 16의 배수로 올림 |
| `plain-digest` | boolean | `false` | Digest 서비스에서 salt · 반복 없는 단순 해시 사용 여부 |
| `crypto` | boolean | `true` | `EgovEnvCryptoService` 의 DB 접속 정보(`getUrl()` 등)를 **복호화하여** 반환할지 여부. 평문을 쓰면 `false` |
| `crypto-property-location` | String | - | DB 접속 정보를 읽을 프로퍼티 파일 위치(UTF-8). 미지정 시 Spring Environment 에서 읽음 |
| `initial` | boolean | `true` | Foundation(egovframe-rte-fdl-crypto) 설정 호환용 항목. 현재 동작에 영향 없음 |

⚠️ **보안 경고**: `algorithm-key` 를 "egovframe" 으로 두면 기동 시 표준 오류에 경고가 출력됩니다. 반드시 다른 키로 변경하세요.

### algorithm-key-hash 생성

```java
EgovPasswordEncoder encoder = new EgovPasswordEncoder();
encoder.setAlgorithm("SHA-256");                       // algorithm 설정값과 동일하게
String hash = encoder.encryptPassword("your-secure-key"); // → algorithm-key-hash 에 설정
```

## 🗄️ DB 접속 정보 복호화

`EgovEnvCryptoService` 는 기동 시 다음 키를 읽어 두고, `crypto=true` 이면 getter 호출 시 복호화하여 반환합니다.

| 키 | getter |
|----|--------|
| `Globals.DbType` | (접두어 결정) |
| `Globals.{DbType}.DriverClassName` | `getDriverClassName()` |
| `Globals.{DbType}.Url` | `getUrl()` |
| `Globals.{DbType}.UserName` | `getUsername()` |
| `Globals.{DbType}.Password` | `getPassword()` |

- 조회 순서 : `crypto-property-location` 파일 → 파일에 없는 키는 Spring Environment(`application.properties`/`yml`, 환경 변수 등)
- `Globals.DbType` 이 없으면 DB 접속 정보를 사용하지 않는 것으로 보고 getter 는 `null` 을 반환합니다.
- `crypto-property-location` 을 지정했는데 파일이 없으면 기동이 실패합니다.

```properties
Globals.DbType=oracle
Globals.oracle.DriverClassName=<envCryptoService.encrypt("oracle.jdbc.OracleDriver") 결과>
Globals.oracle.Url=<암호화 값>
Globals.oracle.UserName=<암호화 값>
Globals.oracle.Password=<암호화 값>
```

```java
@Bean
public DataSource dataSource(EgovEnvCryptoService envCryptoService) {
    HikariDataSource ds = new HikariDataSource();
    ds.setDriverClassName(envCryptoService.getDriverClassName());
    ds.setJdbcUrl(envCryptoService.getUrl());
    ds.setUsername(envCryptoService.getUsername());
    ds.setPassword(envCryptoService.getPassword());
    return ds;
}
```

## 🚀 사용 예제

### ARIA 암호화 서비스

```java
@Autowired
private EgovARIACryptoService ariaCryptoService;

// 바이트 · 숫자
byte[] encrypted = ariaCryptoService.encrypt("plaintext".getBytes(StandardCharsets.UTF_8), "your-secure-key");
byte[] decrypted = ariaCryptoService.decrypt(encrypted, "your-secure-key");
BigDecimal encryptedNumber = ariaCryptoService.encrypt(new BigDecimal("123.45"), "your-secure-key");

// 파일 (대상 파일은 덮어씀)
ariaCryptoService.encrypt(new File("plain.txt"), "your-secure-key", new File("plain.enc"));
ariaCryptoService.decrypt(new File("plain.enc"), "your-secure-key", new File("plain.dec"));
```

전달한 비밀번호가 `algorithm-key-hash` 와 일치하지 않으면 `IllegalArgumentException("password not matched!!!")` 이 발생합니다.

### 환경설정 값 암호화 서비스

```java
@Autowired
private EgovEnvCryptoService envCryptoService;

String encrypted = envCryptoService.encrypt("sensitive-data");          // URL 인코딩 포함
String decrypted = envCryptoService.decrypt(encrypted);

String encryptedNone = envCryptoService.encryptNone("sensitive-data");  // URL 인코딩 없음
String decryptedNone = envCryptoService.decryptNone(encryptedNone);
```

- 복호화 결과는 플랫폼 기본 문자셋과 무관하게 UTF-8 로 복원됩니다.
- `decrypt(null)` / `decrypt("")` 은 빈 문자열을 반환합니다.

### Digest 서비스

```java
@Autowired
private EgovDigestService digestService;

byte[] digest = digestService.digest("message".getBytes(StandardCharsets.UTF_8));
boolean matches = digestService.matches("message".getBytes(StandardCharsets.UTF_8), digest);
```

### 패스워드 인코더

```java
@Autowired
@Qualifier("egovEnvPasswordEncoderService")
private EgovPasswordEncoder passwordEncoder;

String encryptedPassword = passwordEncoder.encryptPassword("plainPassword");
boolean isValid = passwordEncoder.checkPassword("plainPassword", encryptedPassword);
```

## 📂 패키지 구성

| 패키지 | 내용 |
|--------|------|
| `org.egovframe.boot.crypto` | 암호화 자동 구성(`EgovCryptoConfiguration`), 설정 프로퍼티(`org.egovframe.crypto.*`), 암호화 키 검증용 비밀번호 인코더 |
| `org.egovframe.boot.crypto.service` | 암복호화(ARIA·일반 PBE)·해시(Digest)·환경 설정 값 암호화 및 DB 접속 정보 복호화 서비스 인터페이스 |
| `org.egovframe.boot.crypto.service.impl` | 암복호화·해시·환경 설정 암호화 서비스 구현체와 ARIA 암호화 처리 클래스 |
| `org.egovframe.boot.crypto.service.impl.aria` | ARIA 블록 암호 엔진과 패딩(ANSI X.923) 처리 클래스 |

## 📚 제공되는 Bean

| Bean 이름 | 타입 | 설명 |
|-----------|------|------|
| `egovEnvPasswordEncoderService` | `EgovPasswordEncoder` | 패스워드 인코더 |
| `egovARIACryptoService` | `EgovARIACryptoServiceImpl` | ARIA 암호화 서비스 |
| `egovEnvCryptoService` | `EgovEnvCryptoServiceImpl` | 환경설정 값 암호화 · DB 접속 정보 복호화 |
| `egovGeneralCryptoService` | `EgovGeneralCryptoServiceImpl` | 일반(PBE) 암호화 서비스 |
| `egovDigestService` | `EgovDigestServiceImpl` | Digest 서비스 |
| `reloadableResourceBundleMessageSource` | `ReloadableResourceBundleMessageSource` | 스타터 메시지(`classpath:messages/message-crypto*.properties`) |

## 🔐 보안 고려사항

1. **알고리즘 키 변경**: 알고리즘 키 "egovframe"은 반드시 변경해야 합니다.
2. **키 관리**: 암호화 키는 안전하게 관리하고, 소스 코드에 하드코딩하지 마세요. 환경 변수나 외부 설정 서비스를 사용하는 것을 권장합니다.
3. **로그**: 이 스타터는 평문 데이터나 비밀번호를 로그에 남기지 않습니다. 애플리케이션 코드에서도 복호화 결과를 로그로 출력하지 마세요.
4. **일반 암호화 기본 알고리즘**: `EgovGeneralCryptoServiceImpl` 의 기본 알고리즘 `PBEWithSHA1AndDESede` 는 약한 조합입니다. 신규 개발 시 `setAlgorithm("PBEWithHmacSHA256AndAES_256")` 등 강한 알고리즘을 지정하세요.

## 📄 라이선스

이 프로젝트는 Apache License 2.0 하에 배포됩니다.

```
Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 🔗 관련 링크

- [전자정부 표준프레임워크 공식 사이트](https://www.egovframe.go.kr)
- [암호화 서비스 가이드](https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte5.0:fdl:crypto)

## 📝 버전 정보

- **현재 버전**: 5.1.0
- **Java 버전**: 17
- **Spring Boot 버전**: 3.5.16
- **Last Updated**: 2026.10.23

## 🤝 기여

이슈 및 개선 사항은 GitHub Issues를 통해 제출해 주세요.
