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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

public interface EgovCryptoService {

    /**
     * 패스워드 암호화 지정.
     *
     * @param passwordEncoder
     */
    void setPasswordEncoder(EgovPasswordEncoder passwordEncoder);

    /**
     * 파일처리시 사용되는 blockSize 지정.
     *
     * @param blockSize
     */
    void setBlockSize(int blockSize);

    /**
     * 암호화 처리.
     *
     * @param data
     * @return
     * @throws Exception
     */
    byte[] encrypt(byte[] data, String password);

    /**
     * BigDecimal 암호화 처리.
     * 
     * [제거 예정]
     * 암호화 된 데이터의 반환 타입은 byte[]가 일반적이며, BigDecimal로 반환하는 것은 현실적이지 않음.
     * 또한 구현체인 EgovARIACryptoService에서도 구현되지 않으므로, 추후 전자정부프레임워크 6에서 제거 고려
     *
     * @param number
     * @return
     * @throws Exception
     */
    @Deprecated(since = "v5.0.3 FINAL", forRemoval=true)
    BigDecimal encrypt(BigDecimal number, String password);

    /**
     * 파일 암호화 처리.
     *
     * @param srcFile
     * @param trgtFile
     * @param password
     * @throws IOException
     */
    void encrypt(File srcFile, String password, File trgtFile) throws IOException;

    /**
     * 복호화 처리.
     *
     * @param encryptedData
     * @param password
     * @return
     * @throws Exception
     */
    byte[] decrypt(byte[] encryptedData, String password);

    /**
     * BigDecimal 복호화 처리. 
     * 
     * [제거 예정]
     * 암호화 된 데이터의 반환 타입은 byte[]가 일반적이며, BigDecimal로 입력받는 것은 현실적이지 않음.
     * 또한 구현체인 EgovARIACryptoService에서도 구현되지 않으므로, 추후 전자정부프레임워크 6에서 제거 고려
     *
     * @param encryptedNumber
     * @param password
     * @return
     * @throws Exception
     */
    @Deprecated(since = "v5.0.3 FINAL", forRemoval=true)
    BigDecimal decrypt(BigDecimal encryptedNumber, String password);

    /**
     * 파일 복호화 처리.
     *
     * @param encryptedFile
     * @param password
     * @param trgtFile
     * @throws IOException
     */
    void decrypt(File encryptedFile, String password, File trgtFile) throws IOException;

}
