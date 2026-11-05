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
import org.egovframe.boot.crypto.service.EgovDigestService;
import org.jasypt.digest.StandardByteDigester;

/**
 * Jasypt 기반 Digest 서비스 구현체.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * </pre>
 * @since 2025.06.01
 */
@Getter
@Setter
public class EgovDigestServiceImpl implements EgovDigestService {

    private static final int DEFAULT_STRONG_ITERATIONS = 1000;
    private static final int DEFAULT_STRONG_SALT_SIZE = 8;
    private String algorithm;
    private boolean plainDigest;

    private StandardByteDigester byteDigester() {
        StandardByteDigester digester = new StandardByteDigester();
        digester.setAlgorithm(algorithm);
        if (plainDigest) {
            digester.setIterations(1);
            digester.setSaltSizeBytes(0); // salt 없음
        } else {
            digester.setIterations(DEFAULT_STRONG_ITERATIONS);
            digester.setSaltSizeBytes(DEFAULT_STRONG_SALT_SIZE);
        }
        return digester;
    }

    public byte[] digest(byte[] data) {
        StandardByteDigester digester = byteDigester();
        return digester.digest(data);
    }

    public boolean matches(byte[] messageByte, byte[] digestByte) {
        StandardByteDigester digester = byteDigester();
        return digester.matches(messageByte, digestByte);
    }

}
