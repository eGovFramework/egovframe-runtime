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
import org.egovframe.boot.crypto.service.EgovGeneralCryptoService;
import org.jasypt.encryption.pbe.StandardPBEBigDecimalEncryptor;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import org.springframework.util.ReflectionUtils;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * Jasypt PBE 기반 일반 암복호화 서비스 구현체.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  blockSize 검증
 * </pre>
 * @since 2025.06.01
 */
@Getter
@Setter
@Slf4j
public class EgovGeneralCryptoServiceImpl implements EgovGeneralCryptoService {

    private final Base64 base64 = new Base64();
    private String algorithm = "PBEWithSHA1AndDESede";
    private int blockSize = 1024;
    private EgovPasswordEncoder passwordEncoder;

    /** 블록 크기 지정. 0 이하이면 encrypt(File) 무한 루프(CWE-835)·버퍼 할당 오류가 나므로 거부한다. */
    public void setBlockSize(int blockSize) {
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
            log.error("password not matched!!!");
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
            log.error("password not matched!!!");
            throw new IllegalArgumentException("password not matched!!!");
        }
    }

    public void encrypt(File srcFile, String password, File trgtFile) {
        FileInputStream fis = null;
        FileWriter fw = null;
        BufferedInputStream bis = null;
        BufferedWriter bw = null;
        byte[] buffer;
        if (passwordEncoder.checkPassword(password)) {
            StandardPBEByteEncryptor cipher = new StandardPBEByteEncryptor();
            cipher.setAlgorithm(algorithm);
            cipher.setPassword(password);
            buffer = new byte[blockSize];
            log.debug("blockSize = {}", blockSize);
            try {
                fis = new FileInputStream(srcFile);
                bis = new BufferedInputStream(fis);
                fw = new FileWriter(trgtFile);
                bw = new BufferedWriter(fw);
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
                log.debug("processed bytes = {}", size);
            } catch (IOException e) {
                ReflectionUtils.handleReflectionException(e);
            } finally {
                try {
                    if (bw != null) bw.close();
                } catch (IOException e) {
                    log.error("encrypt BufferedWriter close exception");
                }
                try {
                    if (fw != null) fw.close();
                } catch (IOException e) {
                    log.error("encrypt FileWriter close exception");
                }
                try {
                    if (bis != null) bis.close();
                } catch (IOException e) {
                    log.error("encrypt BufferedInputStream close exception");
                }
                try {
                    if (fis != null) fis.close();
                } catch (IOException e) {
                    log.error("encrypt FileInputStream close exception");
                }
            }
        } else {
            log.error("password not matched!!!");
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
            log.error("password not matched!!!");
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
            log.error("password not matched!!!");
            throw new IllegalArgumentException("password not matched!!!");
        }
    }

    public void decrypt(File encryptedFile, String password, File trgtFile) {
        FileReader fr = null;
        FileOutputStream fos = null;
        BufferedReader br = null;
        BufferedOutputStream bos = null;
        if (passwordEncoder.checkPassword(password)) {
            StandardPBEByteEncryptor cipher = new StandardPBEByteEncryptor();
            cipher.setAlgorithm(algorithm);
            cipher.setPassword(password);
            try {
                fr = new FileReader(encryptedFile);
                br = new BufferedReader(fr);
                fos = new FileOutputStream(trgtFile);
                bos = new BufferedOutputStream(fos);
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
            } finally {
                try {
                    if (bos != null) bos.close();
                } catch (IOException e) {
                    log.error("decrypt BufferedOutputStream close exception");
                }
                try {
                    if (fos != null) fos.close();
                } catch (IOException e) {
                    log.error("decrypt FileOutputStream close exception");
                }
                try {
                    if (br != null) br.close();
                } catch (IOException e) {
                    log.error("decrypt BufferedReader close exception");
                }
                try {
                    if (fr != null) fr.close();
                } catch (IOException e) {
                    log.error("decrypt FileReader close exception");
                }
            }
        } else {
            log.error("password not matched!!!");
            throw new IllegalArgumentException("password not matched!!!");
        }
    }

}
