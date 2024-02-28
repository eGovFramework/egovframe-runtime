/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * WebFlux 서비스를 위한 Sequence 생성 클래스
 *
 * <p>Desc.: WebFlux 서비스를 위한 Sequence 생성 클래스</p>
 *
 * @author ESFC
 * @since 2023.08.31
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   ESFC            최초 생성
 * </pre>
 */
public class EgovSequenceGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovSequenceGenerator.class);

    public static String generateSequence(String instance) {
        return generateHash(generateRandomString(), instance);
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
            sb.append(characters.charAt((int)(Math.random()*characters.length())));
        }
        return sb.toString();
    }

    protected static String generateHash(String data, String instance) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(instance);
            byte[] bytes = messageDigest.digest(data.getBytes(StandardCharsets.UTF_8));
            for (byte b : bytes) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("##### EgovSequenceIdGeneration Serial Number Generation NoSuchAlgorithmException");
            sb.setLength(0);
        }
        return sb.toString();
    }

}
