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
package org.egovframe.rte.fdl.crypto;

import org.egovframe.rte.fdl.property.EgovPropertyService;

/**
 * EgovEnvCrypto 클래스
 * 데이터베이스 연결시 대한 항목을 암호화, 복호화
 * 개정이력(Modification Information)
 * <p>
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2018.08.09	장동한				최초 생성
 */
public interface EgovEnvCryptoService {

    /**
     * 클래스 초기화 메소드.
     */
    void init();

    /**
     * EgovPasswordEncoder 클래스 setter
     */
    EgovPasswordEncoder getPasswordEncoder();

    /**
     * EgovPasswordEncoder 클래스 setter
     */
    void setPasswordEncoder(EgovPasswordEncoder passwordEncoder);

    /**
     * EgovPasswordEncoder 클래스 getter
     */
    EgovCryptoService getCryptoService();

    /**
     * EgovCryptoService 클래스 setter
     */
    void setCryptoService(EgovCryptoService cryptoService);

    /**
     * EgovPropertyService 클래스 getter
     */
    EgovPropertyService getCryptoConfigurer();

    /**
     * EgovPropertyService 클래스 setter
     */
    void setCryptoConfigurer(EgovPropertyService cryptoConfigurer);

    /**
     * 환경설정 파일의 키값(항목)을 암호화
     */
    String encrypt(String encrypt);

    /**
     * 환경설정 파일의 키값(항목)을 복호화
     */
    String decrypt(String decrypt);

    /**
     * 환경설정 파일의 키값(항목)을 URLEncoder를 사용하지 않고 암호화
     */
    String encryptNone(String encrypt);

    /**
     * 환경설정 파일의 키값(항목)을 URLEncoder를 사용하지 않고 복호화
     */
    String decryptNone(String decrypt);

    /**
     * 데이터베이스 접속 항목(DriverClassName)에 대한 setter
     */
    String getDriverClassName();

    /**
     * 데이터베이스 접속 항목(DriverClassName)에 대한 getter
     */
    void setDriverClassName(String driverClassName);

    /**
     * 데이터베이스 접속 항목(URL)에 대한 setter
     */
    String getUrl();

    /**
     * 데이터베이스 접속 항목(URL)에 대한 getter
     */
    void setUrl(String url);

    /**
     * 데이터베이스 접속 항목(계정명)에 대한 getter
     */
    String getUsername() throws Exception;

    /**
     * 데이터베이스 접속 항목(계정명)에 대한 setter
     */
    void setUsername(String username);

    /**
     * 데이터베이스 접속 항목(비밀번호)에 대한 getter
     */
    String getPassword();

    /**
     * 데이터베이스 접속 항목(비밀번호)에 대한 setter
     */
    void setPassword(String password);

    /**
     * Crypto 사용여부에 대한 getter
     */
    boolean isCrypto();

    /**
     * Crypto 사용여부에 대한 setter
     */
    void setCrypto(boolean crypto);

    /**
     * Crypto 계정 알고리즘 지정에 대한 setter
     */
    String getCryptoAlgorithm();

    /**
     * Crypto 계정 알고리즘 지정에 대한 getter
     */
    void setCryptoAlgorithm(String cryptoAlgorithm);

    /**
     * Crypto 계정 알고리즘키에 대한 getter
     */
    String getCryptoAlgorithmKey();

    /**
     * Crypto 알고리즘 키에 대한 setter
     */
    void setCryptoAlgorithmKey(String cryptoAlgorithmKey);

    /**
     * Crypto 알고리즘 키 Hash에 대한 getter
     */
    String getCryptoAlgorithmKeyHash();

    /**
     * Crypto 알고리즘 키 Hash에 대한 setter
     */
    void setCryptoAlgorithmKeyHash(String cryptoAlgorithmKeyHash);

    /**
     * Crypto 블럭 사이즈에 대한 getter
     */
    int getCryptoBlockSize();

    /**
     * Crypto 블럭 사이즈에 대한 setter
     */
    void setCryptoBlockSize(int cryptoBlockSize);

}
