/*
 * Copyright 2008-2019 MOIS(Ministry of the Interior and Safety).
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

public interface EgovEnvCryptoService {

	/**
	 * EgovPasswordEncoder 클래스 setter
	 *
	 * @param
	 * @return EgovPasswordEncoder
	 */
	EgovPasswordEncoder getPasswordEncoder();

	/**
	 * EgovPasswordEncoder 클래스 setter
	 *
	 * @param passwordEncoder EgovPasswordEncoder클래스
	 * @return void
	 */
	void setPasswordEncoder(EgovPasswordEncoder passwordEncoder);

	/**
	 * EgovPasswordEncoder 클래스 getter
	 *
	 * @param
	 * @return EgovCryptoService
	 */
	EgovCryptoService getCryptoService();

	/**
	 * EgovCryptoService 클래스 setter
	 *
	 * @param cryptoService EgovCryptoService
	 * @return void
	 */
	void setCryptoService(EgovCryptoService cryptoService);

	/**
	 * 환경설정 파일의 키값(항목)을 암호화
	 *
	 * @param encrypt 암호화값
	 * @return String
	 */
	String encrypt(String encrypt);

	/**
	 * 환경설정 파일의 키값(항목)을 복호화
	 *
	 * @param decrypt 복호화값
	 * @return String
	 */
	String decrypt(String decrypt);

	/**
	 * 환경설정 파일의 키값(항목)을 URLEncoder를 사용하지 않고 암호화
	 *
	 * @param encrypt 암호화값
	 * @return String
	 */
	String encryptNone(String encrypt);

	/**
	 * 환경설정 파일의 키값(항목)을 URLEncoder를 사용하지 않고 복호화
	 *
	 * @param decrypt 복호화값
	 * @return String
	 */
	String decryptNone(String decrypt);

	/**
	 * Crypto 사용여부에 대한 getter
	 *
	 * @param
	 * @return boolean
	 */
	boolean isCrypto();

	/**
	 * Crypto 사용여부에 대한 setter
	 *
	 * @param crypto Crypto사용여부
	 * @return void
	 */
	void setCrypto(boolean crypto);

	/**
	 * Crypto 계정 알고리즘 지정에 대한 setter
	 *
	 * @param
	 * @return String
	 */
	String getCryptoAlgorithm();

	/**
	 * Crypto 계정 알고리즘 지정에 대한 getter
	 *
	 * @param cryptoAlgorithm 계정 암호화 알고리즘(MD5, SHA-1, SHA-256)
	 * @return void
	 */
	void setCryptoAlgorithm(String cryptoAlgorithm);

	/**
	 * Crypto 계정 알고리즘키에 대한 getter
	 *
	 * @param
	 * @return String
	 */
	String getCyptoAlgorithmKey();

	/**
	 * Crypto 알고리즘 키에 대한 setter
	 *
	 * @param cyptoAlgorithmKey 알고리즘키
	 * @return void
	 */
	void setCyptoAlgorithmKey(String cyptoAlgorithmKey);

	/**
	 * Crypto 알고리즘 키 Hash에 대한 getter
	 *
	 * @param
	 * @return String
	 */
	String getCyptoAlgorithmKeyHash();

	/**
	 * Crypto 알고리즘 키 Hash에 대한 setter
	 *
	 * @param cyptoAlgorithmKeyHash 알고리즘키Hash코드
	 * @return void
	 */
	void setCyptoAlgorithmKeyHash(String cyptoAlgorithmKeyHash);

	/**
	 * Crypto 블럭 사이즈에 대한 getter
	 *
	 * @param
	 * @return int
	 */
	int getCryptoBlockSize();

	/**
	 * Crypto 블럭 사이즈에 대한 setter
	 *
	 * @param cryptoBlockSize 블럭사이즈
	 * @return void
	 */
	void setCryptoBlockSize(int cryptoBlockSize);

}