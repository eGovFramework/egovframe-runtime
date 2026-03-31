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
package org.egovframe.rte.fdl.reactive.idgnr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * WebFlux 서비스를 위한 Sequence 생성 클래스
 *
 * <p>Desc.: WebFlux 서비스를 위한 Sequence 생성 클래스</p>
 *
 * @author 유지보수
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   유지보수            최초 생성
 * </pre>
 * @since 2023.08.31
 */
public class EgovSequenceGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovSequenceGenerator.class);

    private static final int SALT_BYTE_LENGTH = 16;

    public static String generateSequence(String instance) {
        byte[] salt = generateSalt();
        return generateHashWithSalt(generateRandomString(), instance, salt);
    }

    protected static DateTimeFormatter localeDateTime() {
        return new DateTimeFormatterBuilder()
                .appendPattern("yyyyMMddHHmmss")
                .appendValue(ChronoField.MILLI_OF_SECOND, 3)
                .toFormatter();
    }

    protected static String generateRandomString() {
        StringBuilder sb = new StringBuilder();
        String nowDate = LocalDateTime.now().format(localeDateTime());
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        sb.append(nowDate);
        for (int i = 0; i < 5; i++) {
            sb.append(characters.charAt((int) (Math.random() * characters.length())));
        }
        return sb.toString();
    }

    /**
     * 시퀀스/ID 생성 전용 해시. 솔트를 포함하여 해시값을 생성
     * 비밀번호 저장·검증에는 사용 금지
     * 비밀번호는 BCrypt, PBKDF2, Argon2 등 전용 알고리즘(솔트+반복)을 사용 권장
     *
     * @param data     해시할 데이터
     * @param instance digest 알고리즘명 (예: SHA-256)
     * @param salt     솔트 (null이면 빈 바이트 사용)
     * @return 16진수 해시 문자열
     */
    protected static String generateHashWithSalt(String data, String instance, byte[] salt) {
        StringBuilder sb = new StringBuilder();
        // 2026.02.28 KISA 보안취약점 조치
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(instance);
            messageDigest.update(salt != null ? salt : new byte[0]);
            byte[] bytes = messageDigest.digest(data.getBytes(StandardCharsets.UTF_8));
            for (byte b : bytes) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.debug("[{}] EgovSequenceIdGeneration Serial Number Generation : {}", e.getClass().getName(), e.getMessage());
            sb.setLength(0);
        }
        return sb.toString();
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_BYTE_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * 솔트 없이 단순 해시. 시퀀스/ID 생성 이외의 용도(특히 비밀번호 저장·검증)에는 사용 금지
     * 비밀번호는 BCrypt, PBKDF2, Argon2 등 전용 API를 사용 권장
     *
     * @param data     해시할 데이터
     * @param instance digest 알고리즘명 (예: SHA-256)
     * @return 16진수 해시 문자열
     */
    protected static String generateHash(String data, String instance) {
        return generateHashWithSalt(data, instance, new byte[0]);
    }

}
