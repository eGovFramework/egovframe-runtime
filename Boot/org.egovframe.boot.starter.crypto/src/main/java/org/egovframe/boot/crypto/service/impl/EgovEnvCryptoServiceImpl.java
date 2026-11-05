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
package org.egovframe.boot.crypto.service.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.egovframe.boot.crypto.EgovPasswordEncoder;
import org.egovframe.boot.crypto.service.EgovCryptoService;
import org.egovframe.boot.crypto.service.EgovEnvCryptoService;

import org.springframework.core.env.PropertyResolver;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 환경 설정 값 암복호화 서비스 구현체.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  UTF-8 복호화 · null 처리, DB 접속 정보 복호화 추가
 * </pre>
 * @since 2025.06.01
 */
@Getter
@Setter
@Slf4j
public class EgovEnvCryptoServiceImpl implements EgovEnvCryptoService {

    private EgovPasswordEncoder egovPasswordEncoder;
    private EgovCryptoService cryptoService;
    private String cryptoAlgorithm;
    private String cyptoAlgorithmKey;
    private String cyptoAlgorithmKeyHash;
    private int cryptoBlockSize;
    /* DB 접속 항목 조회용 (Environment 또는 cryptoPropertyLocation 파일) */
    private PropertyResolver propertyResolver;
    /* DB 접속 항목 */
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    /* DB 접속 항목 복호화 여부 */
    private boolean crypto = false;

    /** Globals.DbType 기준으로 DB 접속 항목을 읽는다. DbType 이 없으면 건너뛴다. */
    @Override
    public void init() {
        if (propertyResolver == null) {
            return;
        }
        String dbType = propertyResolver.getProperty("Globals.DbType");
        if (!StringUtils.hasText(dbType)) {
            log.debug("EgovEnvCryptoServiceImpl init() : Globals.DbType is not defined, skip database properties.");
            return;
        }
        dbType = dbType.trim();
        this.driverClassName = propertyResolver.getProperty("Globals." + dbType + ".DriverClassName");
        this.url = propertyResolver.getProperty("Globals." + dbType + ".Url");
        this.username = propertyResolver.getProperty("Globals." + dbType + ".UserName");
        this.password = propertyResolver.getProperty("Globals." + dbType + ".Password");
    }

    @Override
    public String getDriverClassName() {
        return decryptIfNeeded(driverClassName);
    }

    @Override
    public String getUrl() {
        return decryptIfNeeded(url);
    }

    @Override
    public String getUsername() {
        return decryptIfNeeded(username);
    }

    @Override
    public String getPassword() {
        return decryptIfNeeded(password);
    }

    /** crypto=true 이면 복호화, 아니면 원래 값(null 은 그대로) */
    private String decryptIfNeeded(String value) {
        if (value == null || !this.crypto) {
            return value;
        }
        return decrypt(value);
    }

    @Override
    public EgovPasswordEncoder getPasswordEncoder() {
        return egovPasswordEncoder;
    }

    @Override
    public void setPasswordEncoder(EgovPasswordEncoder passwordEncoder) {
        this.egovPasswordEncoder = passwordEncoder;
    }

    /** 설정 값 암호화(Base64 + URL 인코딩) */
    public String encrypt(String encrypt) {
        try {
            return URLEncoder.encode(new String(Base64.encodeBase64(cryptoService.encrypt(encrypt.getBytes(StandardCharsets.UTF_8), this.cyptoAlgorithmKey), false, true)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : " + e.getMessage());
        }
        return encrypt;
    }

    /** 설정 값 복호화 */
    public String decrypt(String decrypt) {
        if (decrypt == null || decrypt.isEmpty()) {
            return decrypt != null ? decrypt : "";
        }
        try {
            // 플랫폼 기본 문자셋이 아닌 UTF-8 로 복원
            return new String(cryptoService.decrypt(Base64.decodeBase64(URLDecoder.decode(decrypt, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8)), this.cyptoAlgorithmKey), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : " + e.getMessage());
        }
        return decrypt;
    }

    /** 설정 값 암호화(URL 인코딩 없음) */
    public String encryptNone(String encrypt) {
        try {
            return new String(Base64.encodeBase64(cryptoService.encrypt(encrypt.getBytes(StandardCharsets.UTF_8), this.cyptoAlgorithmKey), false, true), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : " + e.getMessage());
        }
        return encrypt;
    }

    /** 설정 값 복호화(URL 디코딩 없음) */
    public String decryptNone(String decrypt) {
        try {
            return new String(cryptoService.decrypt(Base64.decodeBase64(decrypt.getBytes(StandardCharsets.UTF_8)), this.cyptoAlgorithmKey), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : " + e.getMessage());
        }
        return decrypt;
    }

}
