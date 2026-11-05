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

import org.egovframe.boot.crypto.service.impl.aria.ARIAEngine;
import org.egovframe.boot.crypto.service.impl.aria.AnsiX923Padding;
import org.egovframe.boot.crypto.service.impl.aria.CryptoPadding;

import java.security.InvalidKeyException;

/**
 * 마스터 키로 ARIA 블록 암복호화를 수행하는 클래스.
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
public class ARIACipher {

    private static final int MASTER_KEY_MAX_LENGTH = 32;
    private static final int KEY_SIZE = 256;

    /** 마스터 키 */
    private String masterKey = null;

    /** 암호(마스터 키) 설정 */
    public void setPassword(String masterKey) {
        if (masterKey.length() > MASTER_KEY_MAX_LENGTH) {
            this.masterKey = masterKey.substring(0, MASTER_KEY_MAX_LENGTH);
        } else {
            this.masterKey = masterKey;
        }
    }

    /** 바이트 배열 암호화 */
    public byte[] encrypt(byte[] data) {
        try {
            CryptoPadding padding = new AnsiX923Padding();
            byte[] mk = padding.addPadding(masterKey.getBytes(), MASTER_KEY_MAX_LENGTH);
            ARIAEngine instance = new ARIAEngine(KEY_SIZE);
            return instance.encrypt(data, mk);
        } catch (InvalidKeyException ike) {
            throw new RuntimeException(ike);
        }
    }

    /** 바이트 배열 복호화 */
    public byte[] decrypt(byte[] encryptedData) {
        try {
            CryptoPadding padding = new AnsiX923Padding();
            byte[] mk = padding.addPadding(masterKey.getBytes(), MASTER_KEY_MAX_LENGTH);
            ARIAEngine instance = new ARIAEngine(KEY_SIZE);
            return instance.decrypt(encryptedData, mk);
        } catch (InvalidKeyException ike) {
            throw new RuntimeException(ike);
        }
    }

}
