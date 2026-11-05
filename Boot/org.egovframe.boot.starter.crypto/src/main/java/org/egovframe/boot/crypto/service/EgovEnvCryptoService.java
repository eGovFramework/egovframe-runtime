/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.boot.crypto.service;

import org.egovframe.boot.crypto.EgovPasswordEncoder;

/**
 * 환경 설정 값 암복호화 및 DB 접속 정보 복호화 서비스.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  DB 접속 정보(Globals.DbType 등) 복호화 API 추가
 * </pre>
 * @since 2025.06.01
 */
public interface EgovEnvCryptoService {

    /** 암호화 키 검증용 패스워드 인코더 */
    EgovPasswordEncoder getPasswordEncoder();

    /** 패스워드 인코더 지정 */
    void setPasswordEncoder(EgovPasswordEncoder passwordEncoder);

    /** 실제 암복호화를 수행하는 서비스(ARIA) */
    EgovCryptoService getCryptoService();

    /** 암복호화 서비스 지정 */
    void setCryptoService(EgovCryptoService cryptoService);

    /** 설정 값 암호화(Base64 + URL 인코딩) */
    String encrypt(String encrypt);

    /** 설정 값 복호화(UTF-8). null · 빈 값은 빈 문자열 반환 */
    String decrypt(String decrypt);

    /** 설정 값 암호화(Base64, URL 인코딩 없음) */
    String encryptNone(String encrypt);

    /** 설정 값 복호화(URL 디코딩 없음, UTF-8) */
    String decryptNone(String decrypt);

    /** 패스워드 인코더 해시 알고리즘 */
    String getCryptoAlgorithm();

    /** 해시 알고리즘 지정(MD5, SHA-1, SHA-256 등) */
    void setCryptoAlgorithm(String cryptoAlgorithm);

    /** 암호화 키 */
    String getCyptoAlgorithmKey();

    /** 암호화 키 지정 */
    void setCyptoAlgorithmKey(String cyptoAlgorithmKey);

    /** 암호화 키 해시 */
    String getCyptoAlgorithmKeyHash();

    /** 암호화 키 해시 지정 */
    void setCyptoAlgorithmKeyHash(String cyptoAlgorithmKeyHash);

    /** 블록 크기 */
    int getCryptoBlockSize();

    /** 블록 크기 지정 */
    void setCryptoBlockSize(int cryptoBlockSize);

    /** Globals.DbType 기준으로 DB 접속 항목(DriverClassName/Url/UserName/Password)을 읽는다 */
    void init();

    /** DB 드라이버 클래스명(crypto=true 이면 복호화) */
    String getDriverClassName();

    void setDriverClassName(String driverClassName);

    /** DB URL(crypto=true 이면 복호화) */
    String getUrl();

    void setUrl(String url);

    /** DB 계정명(crypto=true 이면 복호화) */
    String getUsername();

    void setUsername(String username);

    /** DB 비밀번호(crypto=true 이면 복호화) */
    String getPassword();

    void setPassword(String password);

    /** DB 접속 항목 복호화 여부 */
    boolean isCrypto();

    void setCrypto(boolean crypto);

}
