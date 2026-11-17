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
package org.egovframe.boot.crypto.service.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.egovframe.boot.crypto.EgovPasswordEncoder;
import org.egovframe.boot.crypto.service.EgovCryptoService;
import org.egovframe.boot.crypto.service.EgovEnvCryptoService;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
@Slf4j
public class EgovEnvCryptoServiceImpl implements EgovEnvCryptoService {

	/* EgovPasswordEncoder 클래스 */
	private EgovPasswordEncoder egovPasswordEncoder;
	/* EgovCryptoService 클래스 */
	private EgovCryptoService cryptoService;
	/* crypto 사용여부 */
	private boolean crypto;
	/* 데이터베이스 Drive Class */
	private String cryptoDBDriver;
	/* 데이터베이스 접속 URL */
	private String cryptoDBUrl;
	/* 데이터베이스 접속 계정명 */
	private String cryptoDBUsername;
	/* 데이터베이스 접속 패드워드 */
	private String cryptoDBPassword;
	/* crypto 알고리즘 */
	private String cryptoAlgorithm;
	/* crypto 알고리즘 키 */
	private String cyptoAlgorithmKey;
	/* crypto 알고리즘 키 Hash */
	private String cyptoAlgorithmKeyHash;
	/* crypto 알고리즘 블럭 사이즈 */
	private int cryptoBlockSize;

	@Override
	public EgovPasswordEncoder getPasswordEncoder() {
		return egovPasswordEncoder;
	}

	@Override
	public void setPasswordEncoder(EgovPasswordEncoder passwordEncoder) {
		this.egovPasswordEncoder = passwordEncoder;
	}

	/**
	 * 환경설정 파일의 키값(항목)을 암호화
	 * @param encrypt 암호화값
	 * @return String
	 */
	public String encrypt(String encrypt){
		try {
			return URLEncoder.encode(new String(Base64.encodeBase64(cryptoService.encrypt(encrypt.getBytes(StandardCharsets.UTF_8), this.cyptoAlgorithmKey), false, true)), "UTF-8");
		} catch(IllegalArgumentException | UnsupportedEncodingException e) {
			log.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : "+ e.getMessage());
		}
		return encrypt;
	}

	/**
	 * 환경설정 파일의 키값(항목)을 복호화
	 * @param decrypt 복호화값
	 * @return String
	 */
	public String decrypt(String decrypt) {
		try {
			return new String(cryptoService.decrypt(Base64.decodeBase64(URLDecoder.decode(decrypt,"UTF-8").getBytes(StandardCharsets.UTF_8)), this.cyptoAlgorithmKey));
		} catch(IllegalArgumentException | UnsupportedEncodingException e) {
			log.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : "+ e.getMessage());
        }
        return decrypt;
	}

	/**
	 * 환경설정 파일의 키값(항목)을 URLEncoder를 사용하지 않고 암호화
	 * @param encrypt 암호화값
	 * @return String
	 */
	public String encryptNone(String encrypt){
		try {
			return new String(Base64.encodeBase64(cryptoService.encrypt(encrypt.getBytes(StandardCharsets.UTF_8), this.cyptoAlgorithmKey), false, true), StandardCharsets.UTF_8);
		} catch(IllegalArgumentException e) {
			log.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : "+ e.getMessage());
		}
		return encrypt;
	}

	/**
	 * 환경설정 파일의 키값(항목)을 URLEncoder를 사용하지 않고 복호화
	 * @param decrypt 복호화값
	 * @return String
	 */
	public String decryptNone(String decrypt){
		try {
			return new String(cryptoService.decrypt(Base64.decodeBase64(decrypt.getBytes(StandardCharsets.UTF_8)), this.cyptoAlgorithmKey));
		} catch(IllegalArgumentException e) {
			log.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : "+ e.getMessage());
		}
		return decrypt;
	}

}
