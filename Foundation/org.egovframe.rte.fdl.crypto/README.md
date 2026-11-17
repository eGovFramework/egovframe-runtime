# org.egovframe.rte.fdl.crypto

**Foundation 암복호화(Crypto) 모듈**은 국산 블록암호 **ARIA(256bit)** 기반 암복호화, Jasypt PBE 기반 일반 암복호화, Digest(해시) 생성·검증, 그리고 `globals.properties`의 DB 접속정보(URL/계정/비밀번호)를 암호화해 두고 기동 시 복호화하는 환경설정 암복호화 서비스를 제공합니다. `<egov-crypto:config>` 커스텀 네임스페이스 하나로 환경설정 암복호화 Bean 구성을 간소화할 수 있습니다. 실행환경의 다른 모듈은 이 모듈에 의존하지 않습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.fdl.crypto:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Jasypt 1.9.3 / Commons Codec 1.18.0
- **실행환경 의존**: `org.egovframe.rte.fdl.property`

> 패키지명은 모듈명과 달리 `org.egovframe.rte.fdl.cryptography` 입니다.

---

## 특징

- **ARIA 암복호화**: `EgovARIACryptoServiceImpl`이 자체 구현 `ARIAEngine`(ANSI X9.23 패딩)으로 `byte[]`·파일을 암복호화. 마스터키는 최대 32byte(256bit)까지 사용하며 초과분은 절단
- **일반(PBE) 암복호화**: `EgovGeneralCryptoServiceImpl`이 Jasypt `StandardPBEByteEncryptor`/`StandardPBEBigDecimalEncryptor`로 `byte[]`, `BigDecimal`, 파일을 암복호화. 기본 알고리즘 `PBEWithSHA1AndDESede`는 약한 조합이므로 사용 시 1회 경고 로그 출력
- **Digest**: `EgovDigestServiceImpl`이 Jasypt `StandardByteDigester`로 `digest()`/`matches()` 제공. `plainDigest=true`면 salt 없이 1회, `false`(기본)면 salt 8byte + 1000회 반복
- **패스워드 게이트**: 모든 암복호화 서비스는 `EgovPasswordEncoder.checkPassword(password)`가 `true`일 때만 동작하며, 아니면 `IllegalArgumentException("password not matched!!!")`
- **환경설정 암복호화**: `EgovEnvCryptoServiceImpl`이 `EgovPropertyService`로 `Globals.DbType`과 `Globals.{DbType}.Url/UserName/Password`를 읽고, `crypto=true`면 getter에서 복호화하여 반환
- **보안 경고**: `algorithmKey`가 기본값 `egovframe`이면 기동 시 `[EgovFramework Fatal ERROR]` 경고를 표준 오류로 출력. 시스템 속성 `egov.crypto.algorithmKey`, `egov.crypto.algorithmKeyHash`로 XML 값을 대체 가능
- `blockSize`는 양수만 허용(아니면 `IllegalArgumentException`)하며 ARIA 서비스는 16의 배수로 자동 보정

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/fdl/cryptography/
├── EgovCryptoService.java            # byte[] / BigDecimal / File 암복호화 인터페이스
├── EgovARIACryptoService.java        # extends EgovCryptoService
├── EgovGeneralCryptoService.java     # extends EgovCryptoService (+ algorithm)
├── EgovDigestService.java
├── EgovEnvCryptoService.java         # 환경설정(DB 접속정보) 암복호화
├── EgovPasswordEncoder.java          # Jasypt ConfigurablePasswordEncryptor 래퍼
├── config/
│   ├── EgovCryptoConfig.java                     # 설정 POJO
│   ├── EgovCryptoConfigBeanDefinitionParser.java # <egov-crypto:config> 파서
│   └── EgovCryptoNameHandler.java
└── impl/
    ├── ARIACipher.java                 # ARIAEngine + 패딩 래퍼 (256bit)
    ├── EgovARIACryptoServiceImpl.java
    ├── EgovGeneralCryptoServiceImpl.java
    ├── EgovDigestServiceImpl.java
    ├── EgovEnvCryptoServiceImpl.java
    └── aria/
        ├── ARIAEngine.java             # ARIA 블록암호 엔진 (128/192/256 키)
        ├── CryptoPadding.java
        └── AnsiX923Padding.java

src/main/resources/
├── META-INF/spring.handlers           # egov-crypto 네임스페이스 → EgovCryptoNameHandler
├── META-INF/spring.schemas            # egov-crypto.xsd / egov-crypto-4.3.1.xsd 매핑
└── org/egovframe/rte/fdl/crypto/config/egov-crypto-4.3.1.xsd
```

---

## 주요 클래스

| 클래스 | 설명 |
|---|---|
| `EgovCryptoService` | `setPasswordEncoder`, `setBlockSize`, `encrypt/decrypt(byte[], password)`, `encrypt/decrypt(BigDecimal, password)`, `encrypt/decrypt(File src, password, File target)` |
| `EgovPasswordEncoder` | 기본 알고리즘 `SHA-256`. `encryptPassword(plain)`, `checkPassword(plain)`(설정된 `hashedPassword`와 비교), `checkPassword(plain, encrypted)`. 내부적으로 Jasypt `ConfigurablePasswordEncryptor`(`plainDigest=true`) 사용 |
| `impl.EgovARIACryptoServiceImpl` | `ARIACipher`로 ARIA 암복호화. `blockSize` 기본 1024(16의 배수로 보정). `BigDecimal` 암복호화는 `UnsupportedOperationException` |
| `impl.ARIACipher` | `setPassword(masterKey)`(최대 32byte), `encrypt(byte[])`, `decrypt(byte[])`. 키 크기 256bit |
| `impl.aria.ARIAEngine` / `CryptoPadding` / `AnsiX923Padding` | ARIA 엔진(블록 16byte)과 ANSI X9.23 패딩 구현 |
| `impl.EgovGeneralCryptoServiceImpl` | Jasypt PBE 암복호화. `algorithm` 기본 `PBEWithSHA1AndDESede`(경고 대상, `PBEWithHmacSHA256AndAES_256` 등 권장). 파일은 `blockSize` 단위로 Base64 라인으로 저장/복원 |
| `impl.EgovDigestServiceImpl` | `algorithm` 기본 `SHA-256`, `plainDigest` 기본 `false`. `digest(byte[])`, `matches(message, digest)` |
| `impl.EgovEnvCryptoServiceImpl` | `init()`에서 `Globals.DbType` 기반 접속정보 로드. `encrypt(String)`/`decrypt(String)`은 URL-safe Base64 + `URLEncoder`, `encryptNone`/`decryptNone`은 Base64만 사용. 프로퍼티: `passwordEncoder`, `cryptoService`, `cryptoConfigurer`(`EgovPropertyService`), `crypto`, `cryptoAlgorithm`, `cyptoAlgorithmKey`, `cyptoAlgorithmKeyHash`, `cryptoBlockSize` |
| `config.EgovCryptoConfig` | `<egov-crypto:config>` 속성 POJO: `initial`, `crypto`, `algorithm`, `algorithmKey`, `algorithmKeyHash`, `cryptoBlockSize`, `cryptoPropertyLocation` (+ `username`, `password`, `url`) |
| `config.EgovCryptoConfigBeanDefinitionParser` | 속성을 읽어 아래 Bean 정의 XML을 생성·로드: `egovEnvPasswordEncoderService`(`EgovPasswordEncoder`), `egovEnvARIACryptoService`(`EgovARIACryptoServiceImpl`), `egovEnvCryptoConfigurerService`(`EgovPropertyServiceImpl`, `cryptoPropertyLocation` 파일 로드), `egovEnvCryptoService`(`EgovEnvCryptoServiceImpl`, `initial="true"`면 `init-method="init"`) |
| `config.EgovCryptoNameHandler` | `config` 엘리먼트에 위 파서 등록 |

---

## 주요 의존성

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.property` | 4.3.1 | compile (`EgovPropertyService`) |
| `spring-beans` | 5.3.39-egov.1 | compile |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `commons-codec:commons-codec` | 1.18.0 | compile (Base64) |
| `commons-io:commons-io` | 2.18.0 | compile |
| `org.jasypt:jasypt` | 1.9.3 | compile (PBE, Digest, PasswordEncryptor) |
| `spring-context`, `spring-test`, `junit`, `easymock` | 5.3.39-egov.1 / 5.3.39-egov.1 / 4.13.2 / 5.2.0 | test |

버전은 루트 POM(`org.egovframe.rte.root` 4.3.1)의 `dependencyManagement`에서 관리하며 모듈 POM은 버전을 선언하지 않습니다.

---

## 설정 및 사용 예시

### 1) 개별 Bean 설정 (`src/test/resources/META-INF/spring/context-crypto.xml`)

```xml
<context:property-placeholder
        location="classpath*:/META-INF/spring/crypto_config.properties,classpath*:/META-INF/spring/password.properties"/>

<bean id="passwordEncoder" class="org.egovframe.rte.fdl.cryptography.EgovPasswordEncoder">
    <property name="algorithm" value="${crypto.password.algorithm}"/>    <!-- default : SHA-256 -->
    <property name="hashedPassword" value="${crypto.hashed.password}"/>
</bean>

<bean id="ARIACryptoService" class="org.egovframe.rte.fdl.cryptography.impl.EgovARIACryptoServiceImpl">
    <property name="passwordEncoder" ref="passwordEncoder"/>
    <property name="blockSize" value="1024"/>    <!-- default : 1024 -->
</bean>

<bean id="digestService" class="org.egovframe.rte.fdl.cryptography.impl.EgovDigestServiceImpl">
    <property name="algorithm" value="SHA-256"/>    <!-- default : SHA-256 -->
    <property name="plainDigest" value="true"/>     <!-- default : false -->
</bean>

<bean id="generalCryptoService" class="org.egovframe.rte.fdl.cryptography.impl.EgovGeneralCryptoServiceImpl">
    <property name="passwordEncoder" ref="passwordEncoder"/>
    <property name="algorithm" value="PBEWithSHA1AndDESede"/>    <!-- default : PBEWithSHA1AndDESede -->
    <property name="blockSize" value="1024"/>
</bean>
```

`crypto_config.properties` 예:

```properties
# Message digest algorithm using EgovPasswordEncoder..
crypto.password.algorithm=SHA-256
# hashed password (ex: egovframe (SHA-256) => gdyYs/IZqY86VcWhT8emCYfqY1ahw2vtLG+/FzNqtrQ=)
crypto.hashed.password=gdyYs/IZqY86VcWhT8emCYfqY1ahw2vtLG+/FzNqtrQ=
```

`hashedPassword`는 `EgovPasswordEncoder.encryptPassword(평문키)` 결과이며, 서비스 호출 시 전달하는 `password`가 이 해시와 일치해야 암복호화가 수행됩니다.

### 2) 환경설정(DB 접속정보) 암복호화 네임스페이스 설정 (`src/test/resources/META-INF/spring/config/context-crypto.xml`)

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:egov-crypto="http://maven.egovframe.go.kr/schema/egov-crypto"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://maven.egovframe.go.kr/schema/egov-crypto http://maven.egovframe.go.kr/schema/egov-crypto/egov-crypto-4.3.1.xsd">

    <egov-crypto:config id="egovCryptoConfig"
        initial="false"
        crypto="true"
        algorithm="SHA-256"
        algorithmKey="egovframe"
        algorithmKeyHash="gdyYs/IZqY86VcWhT8emCYfqY1ahw2vtLG+/FzNqtrQ="
        cryptoBlockSize="1024"
        cryptoPropertyLocation="classpath:/META-INF/spring/config/globals.properties" />
</beans>
```

| 속성 | 기본값 | 설명 |
|---|---|---|
| `initial` | `false` | `true`면 `egovEnvCryptoService`에 `init-method="init"`을 붙여 기동 시 `Globals.{DbType}.*` 값을 로드 |
| `crypto` | `false` | DB 접속정보 암호화 사용 여부 |
| `algorithm` | `SHA-256` | `EgovPasswordEncoder` 해시 알고리즘 |
| `algorithmKey` | - | 암호화 키(평문). 시스템 속성 `egov.crypto.algorithmKey`가 있으면 우선 |
| `algorithmKeyHash` | - | `algorithmKey`의 해시. 시스템 속성 `egov.crypto.algorithmKeyHash`가 있으면 우선 |
| `cryptoBlockSize` | `1024` | ARIA 서비스 블록 크기 |
| `cryptoPropertyLocation` | - | `Globals.*` 프로퍼티 파일 위치 |

등록되는 Bean: `egovEnvPasswordEncoderService`, `egovEnvARIACryptoService`, `egovEnvCryptoConfigurerService`, `egovEnvCryptoService`.

`globals.properties`에는 암호화된 값을 넣습니다 (테스트 리소스 기준).

```properties
Globals.DbType=mysql
Globals.mysql.DriverClassName=net.sf.log4jdbc.DriverSpy
Globals.mysql.Url=IPnSE1%2BSpbvetos96I7vdc6frKxds8ybFrhFUode%2FkT6RJ66jEavU%2FHiSB84A%2B29%2BGU4L03ieIdMkDGgQUTiActDGFm9Z0ZDW%2Fq7ehHFhibcUqubKTmZCR68yBfNiBZw
Globals.mysql.UserName=wmvgRSfWM2HgY35IqGDPGA%3D%3D
Globals.mysql.Password=WWA%2FTxd61%2F6v7C3WbXsDPw%3D%3D
Globals.db.Crypto=true
Globals.db.CryptoAlgorithm=SHA-256
Globals.db.CryptoAlgorithmKey=egovframe
Globals.db.CryptoAlgorithmKeyHash=gdyYs/IZqY86VcWhT8emCYfqY1ahw2vtLG+/FzNqtrQ=
Globals.db.CryptoBlockSize=1028
```

네임스페이스 대신 `crypto-config.xml`처럼 `EgovPropertyServiceImpl` + SpEL(`#{egovPropertyConfigurer.getString('Globals.db.CryptoAlgorithmKey')}`)로 `EgovEnvCryptoServiceImpl`을 직접 정의할 수도 있습니다.

### 3) Java 사용 예

```java
@Resource(name = "ARIACryptoService")
private EgovCryptoService cryptoService;

@Resource(name = "digestService")
private EgovDigestService digestService;

// ARIA 암복호화 (password 는 passwordEncoder.hashedPassword 와 일치하는 평문키)
byte[] enc = cryptoService.encrypt("평문".getBytes(StandardCharsets.UTF_8), "egovframe");
byte[] dec = cryptoService.decrypt(enc, "egovframe");

// 파일 암복호화
cryptoService.encrypt(new File("src.txt"), "egovframe", new File("src.enc"));
cryptoService.decrypt(new File("src.enc"), "egovframe", new File("src.dec"));

// Digest 생성/검증
byte[] digest = digestService.digest("message".getBytes(StandardCharsets.UTF_8));
boolean ok = digestService.matches("message".getBytes(StandardCharsets.UTF_8), digest);
```

암호화된 환경설정 값을 만들 때는 `EgovPasswordEncoder.encryptPassword(algorithmKey)`로 해시를 구한 뒤 `EgovARIACryptoServiceImpl`로 암호화하고 Base64·URL 인코딩합니다 (`src/test/java/.../EgovEnvCryptoAdminTest.java`의 `main()` 참고).

### 4) 보안 주의사항

- `algorithmKey`는 반드시 `egovframe`이 아닌 값으로 변경하십시오. 기본값이면 기동 시 치명적 보안 경고가 출력됩니다.
- 키와 해시를 XML에 두지 않으려면 JVM 시스템 속성 `-Degov.crypto.algorithmKey=...`, `-Degov.crypto.algorithmKeyHash=...`를 사용하십시오.
- `EgovGeneralCryptoServiceImpl`의 기본 알고리즘 `PBEWithSHA1AndDESede`는 신규 적용 시 더 강한 PBE 알고리즘으로 교체하십시오.

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Foundation/org.egovframe.rte.fdl.crypto -am
```

## 테스트

```bash
mvn test -pl Foundation/org.egovframe.rte.fdl.crypto
```

테스트는 클래스패스 리소스(`src/test/resources/META-INF/spring/**`, `vector/ARIA-*_(ECB)_*.txt` KAT/MCT/MMT 벡터)만 사용하며 외부 서버가 필요 없습니다. `EgovEnvCryptoAdminTest`, `EgovEnvCryptoUserTest`는 `main()` 기반 유틸리티로 surefire가 실행하지 않습니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.fdl.crypto</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>`을 생략할 수 있습니다. `-egov.1` 포크 아티팩트(Spring Framework 등)를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록이 필요합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.property](../org.egovframe.rte.fdl.property/README.md), [org.egovframe.crypto.spring.boot.starter](../../Boot/org.egovframe.crypto.spring.boot.starter/README.md) (Spring Boot 자동구성 ARIA 스타터)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
