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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * 바이트 · 숫자 · 파일 암복호화 서비스 공통 인터페이스.
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
public interface EgovCryptoService {

    /** 암호화 키 검증용 패스워드 인코더 지정 */
    void setPasswordEncoder(EgovPasswordEncoder passwordEncoder);

    /** 파일 처리 블록 크기 지정(양수) */
    void setBlockSize(int blockSize);

    /** 바이트 배열 암호화 */
    byte[] encrypt(byte[] data, String password);

    /** BigDecimal 암호화 */
    BigDecimal encrypt(BigDecimal number, String password);

    /** 파일 암호화(대상 파일 덮어쓰기) */
    void encrypt(File srcFile, String password, File trgtFile) throws IOException;

    /** 바이트 배열 복호화 */
    byte[] decrypt(byte[] encryptedData, String password);

    /** BigDecimal 복호화 */
    BigDecimal decrypt(BigDecimal encryptedNumber, String password);

    /** 파일 복호화 */
    void decrypt(File encryptedFile, String password, File trgtFile) throws IOException;

}
