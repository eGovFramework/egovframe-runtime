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
package org.egovframe.rte.fdl.cryptography.impl;

import org.apache.commons.codec.binary.Base64;
import org.egovframe.rte.fdl.cryptography.EgovCryptoService;
import org.egovframe.rte.fdl.cryptography.EgovEnvCryptoService;
import org.egovframe.rte.fdl.cryptography.EgovPasswordEncoder;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * EgovEnvCryptoImpl 클래스
 * <Notice>
 * 	    데이터베이스 연결시 대한 항목을 암호화, 복호화
 * <Disclaimer>
 *		N/A
 *
 * @author 장동한
 * @since 2018.08.09
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2018.08.09	장동한				최초 생성
 * </pre>
 */
public class EgovEnvCryptoServiceImpl implements EgovEnvCryptoService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovEnvCryptoServiceImpl.class);
	
	/* EgovPasswordEncoder 클래스 */
	private EgovPasswordEncoder passwordEncoder;
	
	/* EgovCryptoService 클래스 */
	private EgovCryptoService cryptoService;
	
	/* EgovPropertyService 클래스 */
	private EgovPropertyService propertyService;

	/* 데이터베이스 접속 계정명 */
	private String username = "";
	
	/* 데이터베이스 접속 패드워드 */
	private String password = "";
	
	/* 데이터베이스 접속 URL */
	private String url = "";
	
	/* crypto 사용여부 */
	private boolean crypto = false;
	
	/* crypto 알고리즘 */
	private String cryptoAlgorithm = "";
	
	/* crypto 알고리즘 키 */
	private String cyptoAlgorithmKey = "";
	
	/* crypto 알고리즘 키 Hash */
	private String cyptoAlgorithmKeyHash = "";
	
	/* crypto 알고리즘 블럭 사이즈 */
	private int cryptoBlockSize = 0;

	/**
	 * 클래스 초기화 메소드.
	 */
	public void init(){
		String sDbTyps = propertyService.getString("Globals.DbType");
		this.username = propertyService.getString("Globals."+sDbTyps+".UserName");
		this.password = propertyService.getString("Globals."+sDbTyps+".Password");
		this.url = propertyService.getString("Globals."+sDbTyps+".Url");
	}

	/**
	 * EgovPasswordEncoder 클래스 setter
	 * @return EgovPasswordEncoder
	 */
	public EgovPasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}

	/**
	 * EgovPasswordEncoder 클래스 setter
	 * @param passwordEncoder EgovPasswordEncoder클래스
	 * @return void
	 */
	public void setPasswordEncoder(EgovPasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * EgovPasswordEncoder 클래스 getter
	 * @return EgovCryptoService
	 */
	public EgovCryptoService getCryptoService() {
		return cryptoService;
	}

	/**
	 * EgovCryptoService 클래스 setter
	 * @param cryptoService EgovCryptoService
	 * @return void
	 */
	public void setCryptoService(EgovCryptoService cryptoService) {
		this.cryptoService = cryptoService;
	}
	
	/**
	 * EgovPropertyService 클래스 getter
	 * @return EgovPropertyService
	 */
	public EgovPropertyService getCryptoConfigurer() {
		return propertyService;
	}

	/**
	 * EgovPropertyService 클래스 setter
	 * @param cryptoConfigurer EgovPropertyService클래스
	 * @return void
	 */
	public void setCryptoConfigurer(EgovPropertyService cryptoConfigurer) {
		this.propertyService = cryptoConfigurer;
	}
	
	/**
	 * 환경설정 파일의 키값(항목)을 암호화
	 * @param encrypt 암호화값
	 * @return String
	 */
	public String encrypt(String encrypt){
		try {
			return URLEncoder.encode(new String(new Base64().encode((byte[])cryptoService.encrypt( encrypt.getBytes("UTF-8"), this.getCyptoAlgorithmKey()))), "UTF-8");
		} catch(IllegalArgumentException | UnsupportedEncodingException e) {
			LOGGER.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : "+ e.getMessage());
		}
		return encrypt;
	}

	/**
	 * 환경설정 파일의 키값(항목)을 복호화
	 * @param decrypt 복호화값
	 * @return String
	 */
	public String decrypt(String decrypt){
		try {
			return new String((byte[])cryptoService.decrypt(new Base64().decode(URLDecoder.decode(decrypt,"UTF-8").getBytes("UTF-8")), this.cyptoAlgorithmKey));
		} catch(IllegalArgumentException | UnsupportedEncodingException e) {
			LOGGER.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : "+ e.getMessage());
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
			return new String(new Base64().encode((byte[])cryptoService.encrypt( encrypt.getBytes("UTF-8"), this.getCyptoAlgorithmKey())));
		} catch(IllegalArgumentException | UnsupportedEncodingException e) {
			LOGGER.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : "+ e.getMessage());
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
			return new String((byte[])cryptoService.decrypt(new Base64().decode(decrypt.getBytes("UTF-8")), this.cyptoAlgorithmKey));
		} catch(IllegalArgumentException | UnsupportedEncodingException e) {
			LOGGER.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : "+ e.getMessage());
		}
		return decrypt;
	}

	/**
	 * 데이터베이스 접속 항목(계정명)에 대한 setter
	 * @param username 계정명
	 * @return void
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * 데이터베이스 접속 항목(계정명)에 대한 getter
	 * @return String
	 */
	public String getUsername() throws Exception {
		if(this.isCrypto()){
			try {
				return this.decrypt(this.username);
			} catch(IllegalArgumentException e) {
				LOGGER.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : "+ e.getMessage());
			}
		}
		return username;
	}

	/**
	 * 데이터베이스 접속 항목(비밀번호)에 대한 setter
	 * @param password 비밀번호
	 * @return void
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * 데이터베이스 접속 항목(비밀번호)에 대한 getter
	 * @return String
	 */
	public String getPassword() {
		if(this.isCrypto()){
			try {
				return this.decrypt(this.password);
			} catch(IllegalArgumentException e) {
				LOGGER.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : "+ e.getMessage());
			}
		}
		return password;
	}

	/**
	 * 데이터베이스 접속 항목(URL)에 대한 getter
	 * @param url 접속경로
	 * @return void
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * 데이터베이스 접속 항목(URL)에 대한 setter
	 * @return String
	 */
	public String getUrl() {
		if(this.isCrypto()){
			try {
				return this.decrypt(this.url);
			} catch(IllegalArgumentException e) {
				LOGGER.error("[IllegalArgumentException] Try/Catch...usingParameters Runing : "+ e.getMessage());
			}
		}
		return url;
	}

	/**
	 * Crypto 사용여부에 대한 getter
	 * @return boolean
	 */
	public boolean isCrypto() {
		return crypto;
	}

	/**
	 * Crypto 사용여부에 대한 setter
	 * @param crypto Crypto사용여부
	 * @return void
	 */
	public void setCrypto(boolean crypto) {
		this.crypto = crypto;
	}

	/**
	 * Crypto 계정 알고리즘 지정에 대한 setter
	 * @return String
	 */
	public String getCryptoAlgorithm() {
		return cryptoAlgorithm;
	}

	/**
	 * Crypto 계정 알고리즘 지정에 대한 getter
	 * @param cryptoAlgorithm 계정 암호화 알고리즘(MD5, SHA-1, SHA-256)
	 * @return void
	 */
	public void setCryptoAlgorithm(String cryptoAlgorithm) {
		this.cryptoAlgorithm = cryptoAlgorithm;
	}

	/**
	 * Crypto 계정 알고리즘키에 대한 getter
	 * @return String
	 */
	public String getCyptoAlgorithmKey() {
		return cyptoAlgorithmKey;
	}

	/**
	 * Crypto 알고리즘 키에 대한 setter
	 * @param cyptoAlgorithmKey 알고리즘키
	 * @return void
	 */
	public void setCyptoAlgorithmKey(String cyptoAlgorithmKey) {
		this.cyptoAlgorithmKey = cyptoAlgorithmKey;
	}

	/**
	 * Crypto 알고리즘 키 Hash에 대한 getter
	 * @return String
	 */
	public String getCyptoAlgorithmKeyHash() {
		return cyptoAlgorithmKeyHash;
	}

	/**
	 * Crypto 알고리즘 키 Hash에 대한 setter
	 * @param cyptoAlgorithmKeyHash 알고리즘키Hash코드
	 * @return void
	 */
	public void setCyptoAlgorithmKeyHash(String cyptoAlgorithmKeyHash) {
		this.cyptoAlgorithmKeyHash = cyptoAlgorithmKeyHash;
	}

	/**
	 * Crypto 블럭 사이즈에 대한 getter
	 * @return int
	 */
	public int getCryptoBlockSize() {
		return cryptoBlockSize;
	}

	/**
	 * Crypto 블럭 사이즈에 대한 setter
	 * 
	 * @param cryptoBlockSize 블럭사이즈
	 * @return void
	 */
	public void setCryptoBlockSize(int cryptoBlockSize) {
		this.cryptoBlockSize = cryptoBlockSize;
	}

}
