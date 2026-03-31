/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.fdl.crypto.impl;

import org.apache.commons.codec.binary.Base64;
import org.egovframe.rte.fdl.crypto.EgovCryptoService;
import org.egovframe.rte.fdl.crypto.EgovEnvCryptoService;
import org.egovframe.rte.fdl.crypto.EgovPasswordEncoder;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * EgovEnvCryptoImpl 클래스
 * 데이터베이스 연결시 대한 항목을 암호화, 복호화
 * <p>
 * 개정이력(Modification Information)
 * <p>
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2018.08.09	장동한			최초 생성
 * 2024.08.22   kiboomhan		URL Safe 암호화 방식 적용
 */
public class EgovEnvCryptoServiceImpl implements EgovEnvCryptoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovEnvCryptoServiceImpl.class);

    /* EgovPasswordEncoder 클래스 */
    private EgovPasswordEncoder passwordEncoder;

    /* EgovCryptoService 클래스 */
    private EgovCryptoService cryptoService;

    /* EgovPropertyService 클래스 */
    private EgovPropertyService propertyService;

    /* 데이터베이스 드라이브명 */
    private String driverClassName;

    /* 데이터베이스 접속 URL */
    private String url;

    /* 데이터베이스 접속 계정명 */
    private String username;

    /* 데이터베이스 접속 패드워드 */
    private String password;

    /* crypto 사용여부 */
    private boolean crypto = false;

    /* crypto 알고리즘 */
    private String cryptoAlgorithm;

    /* crypto 알고리즘 키 */
    private String cryptoAlgorithmKey;

    /* crypto 알고리즘 키 Hash */
    private String cryptoAlgorithmKeyHash;

    /* crypto 알고리즘 블럭 사이즈 */
    private int cryptoBlockSize = 0;

    /**
     * 클래스 초기화 메소드.
     */
    public void init() {
        String dbType = this.propertyService.getString("Globals.DbType");
        this.driverClassName = this.propertyService.getString("Globals." + dbType + ".DriverClassName");
        this.url = this.propertyService.getString("Globals." + dbType + ".Url");
        this.username = this.propertyService.getString("Globals." + dbType + ".UserName");
        this.password = this.propertyService.getString("Globals." + dbType + ".Password");
    }

    /**
     * 환경설정 파일의 키값(항목)을 암호화
     *
     * @param encrypt 암호화값
     * @return String
     */
    public String encrypt(String encrypt) {
        try {
            return URLEncoder.encode(
                    new String(
                            Base64.encodeBase64(
                                    cryptoService.encrypt(
                                            encrypt.getBytes(StandardCharsets.UTF_8), this.getCryptoAlgorithmKey()
                                    ), false, true
                            ),
                            StandardCharsets.UTF_8
                    ), StandardCharsets.UTF_8
            );
        } catch (IllegalArgumentException e) {
            LOGGER.debug("[{}] EgovEnvCryptoServiceImpl encrypt() : {}", e.getClass().getName(), e.getMessage());
        }
        return encrypt;
    }

    /**
     * 환경설정 파일의 키값(항목)을 복호화
     *
     * @param decrypt 복호화값
     * @return String
     */
    public String decrypt(String decrypt) {
        if (decrypt == null || decrypt.isEmpty()) {
            return decrypt != null ? decrypt : "";
        }
        try {
            return new String(
                    cryptoService.decrypt(
                            Base64.decodeBase64(
                                    URLDecoder.decode(decrypt, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8)
                            ), this.cryptoAlgorithmKey
                    ),
                    StandardCharsets.UTF_8
            );
        } catch (IllegalArgumentException e) {
            LOGGER.debug("[{}] EgovEnvCryptoServiceImpl decrypt() : {}", e.getClass().getName(), e.getMessage());
        }
        return decrypt;
    }

    /**
     * 환경설정 파일의 키값(항목)을 URLEncoder를 사용하지 않고 암호화
     *
     * @param encrypt 암호화값
     * @return String
     */
    public String encryptNone(String encrypt) {
        try {
            return new String(
                    Base64.encodeBase64(
                            cryptoService.encrypt(
                                    encrypt.getBytes(StandardCharsets.UTF_8), this.getCryptoAlgorithmKey()
                            ), false, true
                    ), StandardCharsets.UTF_8
            );
        } catch (IllegalArgumentException e) {
            LOGGER.debug("[{}] EgovEnvCryptoServiceImpl encryptNone() : {}", e.getClass().getName(), e.getMessage());
        }
        return encrypt;
    }

    /**
     * 환경설정 파일의 키값(항목)을 URLEncoder를 사용하지 않고 복호화
     *
     * @param decrypt 복호화값
     * @return String
     */
    public String decryptNone(String decrypt) {
        try {
            return new String(
                    cryptoService.decrypt(
                            Base64.decodeBase64(
                                    decrypt.getBytes(StandardCharsets.UTF_8)
                            ), this.cryptoAlgorithmKey
                    ),
                    StandardCharsets.UTF_8
            );
        } catch (IllegalArgumentException e) {
            LOGGER.debug("[{}] EgovEnvCryptoServiceImpl decryptNone() : {}", e.getClass().getName(), e.getMessage());
        }
        return decrypt;
    }

    /**
     * EgovPasswordEncoder 클래스 setter
     *
     * @return EgovPasswordEncoder
     */
    public EgovPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    /**
     * EgovPasswordEncoder 클래스 setter
     *
     * @param passwordEncoder EgovPasswordEncoder클래스
     */
    public void setPasswordEncoder(EgovPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * EgovPasswordEncoder 클래스 getter
     *
     * @return EgovCryptoService
     */
    public EgovCryptoService getCryptoService() {
        return cryptoService;
    }

    /**
     * EgovCryptoService 클래스 setter
     *
     * @param cryptoService EgovCryptoService
     */
    public void setCryptoService(EgovCryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    /**
     * EgovPropertyService 클래스 getter
     *
     * @return EgovPropertyService
     */
    public EgovPropertyService getCryptoConfigurer() {
        return propertyService;
    }

    /**
     * EgovPropertyService 클래스 setter
     *
     * @param cryptoConfigurer EgovPropertyService클래스
     */
    public void setCryptoConfigurer(EgovPropertyService cryptoConfigurer) {
        this.propertyService = cryptoConfigurer;
    }

    /**
     * 데이터베이스 드라이브명(DriverClassName)에 대한 setter
     *
     * @return String
     */
    public String getDriverClassName() {
        if (this.isCrypto()) {
            try {
                return this.decrypt(this.driverClassName);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("[{}] EgovEnvCryptoServiceImpl driverClassName() : {}", e.getClass().getName(), e.getMessage());
            }
        }
        return driverClassName;
    }

    /**
     * 데이터베이스 드라이브명(DriverClassName)에 대한 getter
     *
     * @param driverClassName 드라이브명
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    /**
     * 데이터베이스 접속 항목(URL)에 대한 setter
     *
     * @return String
     */
    public String getUrl() {
        if (this.isCrypto()) {
            try {
                return this.decrypt(this.url);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("[{}] EgovEnvCryptoServiceImpl url() : {}", e.getClass().getName(), e.getMessage());
            }
        }
        return url;
    }

    /**
     * 데이터베이스 접속 항목(URL)에 대한 getter
     *
     * @param url 접속경로
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 데이터베이스 접속 항목(계정명)에 대한 getter
     *
     * @return String
     */
    public String getUsername() {
        if (this.isCrypto()) {
            try {
                return this.decrypt(this.username);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("[{}] EgovEnvCryptoServiceImpl username() : {}", e.getClass().getName(), e.getMessage());
            }
        }
        return username;
    }

    /**
     * 데이터베이스 접속 항목(계정명)에 대한 setter
     *
     * @param username 계정명
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 데이터베이스 접속 항목(비밀번호)에 대한 getter
     *
     * @return String
     */
    public String getPassword() {
        if (this.isCrypto()) {
            try {
                return this.decrypt(this.password);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("[{}] EgovEnvCryptoServiceImpl password() : {}", e.getClass().getName(), e.getMessage());
            }
        }
        return password;
    }

    /**
     * 데이터베이스 접속 항목(비밀번호)에 대한 setter
     *
     * @param password 비밀번호
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Crypto 사용여부에 대한 getter
     *
     * @return boolean
     */
    public boolean isCrypto() {
        return crypto;
    }

    /**
     * Crypto 사용여부에 대한 setter
     *
     * @param crypto Crypto사용여부
     */
    public void setCrypto(boolean crypto) {
        this.crypto = crypto;
    }

    /**
     * Crypto 계정 알고리즘 지정에 대한 setter
     *
     * @return String
     */
    public String getCryptoAlgorithm() {
        return cryptoAlgorithm;
    }

    /**
     * Crypto 계정 알고리즘 지정에 대한 getter
     *
     * @param cryptoAlgorithm 계정 암호화 알고리즘(MD5, SHA-1, SHA-256)
     */
    public void setCryptoAlgorithm(String cryptoAlgorithm) {
        this.cryptoAlgorithm = cryptoAlgorithm;
    }

    /**
     * Crypto 계정 알고리즘키에 대한 getter
     *
     * @return String
     */
    public String getCryptoAlgorithmKey() {
        return cryptoAlgorithmKey;
    }

    /**
     * Crypto 알고리즘 키에 대한 setter
     *
     * @param cryptoAlgorithmKey 알고리즘키
     */
    public void setCryptoAlgorithmKey(String cryptoAlgorithmKey) {
        this.cryptoAlgorithmKey = cryptoAlgorithmKey;
    }

    /**
     * Crypto 알고리즘 키 Hash에 대한 getter
     *
     * @return String
     */
    public String getCryptoAlgorithmKeyHash() {
        return cryptoAlgorithmKeyHash;
    }

    /**
     * Crypto 알고리즘 키 Hash에 대한 setter
     *
     * @param cryptoAlgorithmKeyHsah 알고리즘키Hash코드
     */
    public void setCryptoAlgorithmKeyHash(String cryptoAlgorithmKeyHsah) {
        this.cryptoAlgorithmKeyHash = cryptoAlgorithmKeyHsah;
    }

    /**
     * Crypto 블럭 사이즈에 대한 getter
     *
     * @return int
     */
    public int getCryptoBlockSize() {
        return cryptoBlockSize;
    }

    /**
     * Crypto 블럭 사이즈에 대한 setter
     *
     * @param cryptoBlockSize 블럭사이즈
     */
    public void setCryptoBlockSize(int cryptoBlockSize) {
        this.cryptoBlockSize = cryptoBlockSize;
    }

}
