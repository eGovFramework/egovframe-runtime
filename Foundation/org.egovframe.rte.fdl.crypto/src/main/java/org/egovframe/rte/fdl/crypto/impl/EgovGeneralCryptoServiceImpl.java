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
import org.egovframe.rte.fdl.crypto.EgovGeneralCryptoService;
import org.egovframe.rte.fdl.crypto.EgovPasswordEncoder;
import org.egovframe.rte.fdl.logging.util.EgovResourceReleaser;
import org.jasypt.encryption.pbe.StandardPBEBigDecimalEncryptor;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class EgovGeneralCryptoServiceImpl implements EgovGeneralCryptoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovGeneralCryptoServiceImpl.class);

    private final Base64 base64 = new Base64();
    private String algorithm = "PBEWithSHA1AndDESede";
    private EgovPasswordEncoder passwordEncoder;
    private int blockSize = 1024;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        LOGGER.debug("General Crypto Service's algorithm : {}", algorithm);
    }

    public void setPasswordEncoder(EgovPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        LOGGER.debug("passwordEncoder's algorithm : {}", passwordEncoder.getAlgorithm());
    }

    public void setBlockSize(int blockSize) {
        // blockSize가 0이면 encrypt(File)의 read 루프가 종료되지 않아 무한 루프에 빠지고(CWE-835),
        // 음수이면 버퍼 할당에서 NegativeArraySizeException이 발생하므로 양수만 허용한다.
        if (blockSize <= 0) {
            throw new IllegalArgumentException("blockSize must be a positive number: " + blockSize);
        }
        this.blockSize = blockSize;
    }

    public byte[] encrypt(byte[] data, String password) {
        if (passwordEncoder.checkPassword(password)) {
            StandardPBEByteEncryptor cipher = new StandardPBEByteEncryptor();
            cipher.setAlgorithm(algorithm);
            cipher.setPassword(password);
            return cipher.encrypt(data);
        } else {
            throw new IllegalArgumentException("password not matched!!!");
        }
    }

    public BigDecimal encrypt(BigDecimal number, String password) {
        if (passwordEncoder.checkPassword(password)) {
            StandardPBEBigDecimalEncryptor cipher = new StandardPBEBigDecimalEncryptor();
            cipher.setAlgorithm(algorithm);
            cipher.setPassword(password);
            return cipher.encrypt(number);
        } else {
            throw new IllegalArgumentException("password not matched!!!");
        }
    }

    public void encrypt(File srcFile, String password, File trgtFile) {
        byte[] buffer = null;
        if (passwordEncoder.checkPassword(password)) {
            StandardPBEByteEncryptor cipher = new StandardPBEByteEncryptor();
            cipher.setAlgorithm(algorithm);
            cipher.setPassword(password);
            buffer = new byte[blockSize];
            LOGGER.debug("blockSize = {}", blockSize);
            try (
                FileInputStream fis = new FileInputStream(srcFile);
                FileWriter fw = new FileWriter(trgtFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                BufferedWriter bw = new BufferedWriter(fw)
            ) {
                byte[] encrypted = null;
                int length = 0;
                long size = 0L;
                while ((length = bis.read(buffer)) >= 0) {
                    if (length < blockSize) {
                        byte[] tmp = new byte[length];
                        System.arraycopy(buffer, 0, tmp, 0, length);
                        encrypted = cipher.encrypt(tmp);
                    } else {
                        encrypted = cipher.encrypt(buffer);
                    }
                    String line = new String(base64.encode(encrypted), StandardCharsets.US_ASCII);
                    bw.write(line);
                    bw.newLine();
                    size += length;
                }
                bw.flush();
                LOGGER.debug("processed bytes = {}", size);
            } catch (IOException e) {
                ReflectionUtils.handleReflectionException(e);
            }
        } else {
            throw new IllegalArgumentException("password not matched!!!");
        }
    }

    public byte[] decrypt(byte[] encryptedData, String password) {
        if (passwordEncoder.checkPassword(password)) {
            StandardPBEByteEncryptor cipher = new StandardPBEByteEncryptor();
            cipher.setAlgorithm(algorithm);
            cipher.setPassword(password);
            return cipher.decrypt(encryptedData);
        } else {
            throw new IllegalArgumentException("password not matched!!!");
        }
    }

    public BigDecimal decrypt(BigDecimal encryptedNumber, String password) {
        if (passwordEncoder.checkPassword(password)) {
            StandardPBEBigDecimalEncryptor cipher = new StandardPBEBigDecimalEncryptor();
            cipher.setAlgorithm(algorithm);
            cipher.setPassword(password);
            return cipher.decrypt(encryptedNumber);
        } else {
            throw new IllegalArgumentException("password not matched!!!");
        }
    }

    public void decrypt(File encryptedFile, String password, File trgtFile) {
        if (passwordEncoder.checkPassword(password)) {
            StandardPBEByteEncryptor cipher = new StandardPBEByteEncryptor();
            cipher.setAlgorithm(algorithm);
            cipher.setPassword(password);
            try (
                FileReader fr = new FileReader(encryptedFile);
                FileOutputStream fos = new FileOutputStream(trgtFile);
                BufferedReader br = new BufferedReader(fr);
                BufferedOutputStream bos = new BufferedOutputStream(fos)
            ) {
                byte[] encrypted = null;
                byte[] decrypted = null;
                String line = null;
                while ((line = br.readLine()) != null) {
                    encrypted = base64.decode(line.getBytes(StandardCharsets.US_ASCII));
                    decrypted = cipher.decrypt(encrypted);
                    bos.write(decrypted);
                }
                bos.flush();
            } catch (IOException e) {
                ReflectionUtils.handleReflectionException(e);
            }
        } else {
            throw new IllegalArgumentException("password not matched!!!");
        }
    }

}
