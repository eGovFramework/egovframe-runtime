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
package org.egovframe.rte.fdl.cryptography;

import java.io.File;
import java.io.FileNotFoundException;
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
	 * @param number
	 * @return
	 * @throws Exception
	 */
	BigDecimal encrypt(BigDecimal number, String password);

	/**
	 * 파일 암호화 처리.
	 * 
	 * @param srcFile
	 * @param trgtFile
	 * @param password
	 * @throws Exception
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
	 * @param encryptedNumber
	 * @param password
	 * @return
	 * @throws Exception
	 */
	BigDecimal decrypt(BigDecimal encryptedNumber, String password);

	/**
	 * 파일 복호화 처리.
	 * 
	 * @param encryptedFile
	 * @param password
	 * @param trgtFile
	 * @throws Exception
	 */
	void decrypt(File encryptedFile, String password, File trgtFile) throws IOException;

}
