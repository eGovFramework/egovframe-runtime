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
package egovframework.rte.fdl.cryptography;

import org.apache.commons.codec.binary.Base64;

import egovframework.rte.fdl.property.EgovPropertyService;

/**
 * EgovEnvCrypto 클래스
 * <Notice>
 * 	    데이터베이스 연결시 대한 항목을 암호화, 복호화
 * <Disclaimer>
 *		N/A
 *
 * @author 장동한
 * @since 2018.08.09
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2018.08.09  장동한           최초 생성
 * </pre>
 */
public interface EgovEnvCryptoService {
	
	/**
	 * 클래스 초기화 메소드.
	 * 
	 * @param N/A
	 * @return void
	 */
	public void init();

	/**
	 * EgovPasswordEncoder 클래스 setter
	 * 
	 * @param N/A
	 * @return EgovPasswordEncoder
	 */
	public EgovPasswordEncoder getPasswordEncoder();

	/**
	 * EgovPasswordEncoder 클래스 setter
	 * 
	 * @param passwordEncoder EgovPasswordEncoder클래스
	 * @return void
	 */
	public void setPasswordEncoder(EgovPasswordEncoder passwordEncoder);

	/**
	 * EgovPasswordEncoder 클래스 getter
	 * 
	 * @param N/A
	 * @return EgovCryptoService
	 */
	public EgovCryptoService getCryptoService();

	/**
	 * EgovCryptoService 클래스 setter
	 * 
	 * @param cryptoService EgovCryptoService
	 * @return void
	 */
	public void setCryptoService(EgovCryptoService cryptoService);
	
	/**
	 * EgovPropertyService 클래스 getter
	 * 
	 * @param N/A
	 * @return EgovPropertyService
	 */
	public EgovPropertyService getCryptoConfigurer();

	/**
	 * EgovPropertyService 클래스 setter
	 * 
	 * @param cryptoConfigurer EgovPropertyService클래스
	 * @return void
	 */
	public void setCryptoConfigurer(EgovPropertyService cryptoConfigurer);
	
	/**
	 * 환경설정 파일의 키값(항목)을 암호화
	 * 
	 * @param encrypt 암호화값
	 * @return String
	 */
	public String encrypt(String encrypt);
	
	/**
	 * 환경설정 파일의 키값(항목)을 복호화
	 * 
	 * @param decrypt 복호화값
	 * @return String
	 */
	public String decrypt(String decrypt);
	
	/**
	 * 환경설정 파일의 키값(항목)을 URLEncoder를 사용하지 않고 암호화
	 * 
	 * @param encrypt 암호화값
	 * @return String
	 */
	public String encryptNone(String encrypt);

	/**
	 * 환경설정 파일의 키값(항목)을 URLEncoder를 사용하지 않고 복호화
	 * 
	 * @param decrypt 복호화값
	 * @return String
	 */
	public String decryptNone(String decrypt);
	
	/**
	 * 데이터베이스 접속 항목(계정명)에 대한 setter
	 * 
	 * @param username 계정명
	 * @return void
	 */
	public void setUsername(String username);
	
	/**
	 * 데이터베이스 접속 항목(계정명)에 대한 getter
	 * 
	 * @param N/A
	 * @return String
	 */
	public String getUsername() throws Exception;

	/**
	 * 데이터베이스 접속 항목(비밀번호)에 대한 setter
	 * 
	 * @param password 비밀번호
	 * @return void
	 */
	public void setPassword(String password);
	
	/**
	 * 데이터베이스 접속 항목(비밀번호)에 대한 getter
	 * 
	 * @param N/A
	 * @return String
	 */
	public String getPassword();
	
	/**
	 * 데이터베이스 접속 항목(URL)에 대한 getter
	 * 
	 * @param url 접속경로
	 * @return void
	 */
	public void setUrl(String url);
	
	/**
	 * 데이터베이스 접속 항목(URL)에 대한 setter
	 * 
	 * @param N/A
	 * @return String
	 */
	public String getUrl();

	/**
	 * Crypto 사용여부에 대한 getter
	 * 
	 * @param N/A
	 * @return boolean
	 */
	public boolean isCrypto();

	/**
	 * Crypto 사용여부에 대한 setter
	 * 
	 * @param crypto Crypto사용여부
	 * @return void
	 */
	public void setCrypto(boolean crypto);
	
	/**
	 * Crypto 계정 알고리즘 지정에 대한 setter
	 * 
	 * @param N/A
	 * @return String
	 */
	public String getCryptoAlgorithm();
	
	/**
	 * Crypto 계정 알고리즘 지정에 대한 getter
	 * 
	 * @param cryptoAlgorithm 계정 암호화 알고리즘(MD5, SHA-1, SHA-256)
	 * @return void
	 */
	public void setCryptoAlgorithm(String cryptoAlgorithm);

	/**
	 * Crypto 계정 알고리즘키에 대한 getter
	 * 
	 * @param N/A
	 * @return String
	 */
	public String getCyptoAlgorithmKey();
	
	/**
	 * Crypto 알고리즘 키에 대한 setter
	 * 
	 * @param cyptoAlgorithmKey 알고리즘키
	 * @return void
	 */
	public void setCyptoAlgorithmKey(String cyptoAlgorithmKey);

	/**
	 * Crypto 알고리즘 키 Hash에 대한 getter
	 * 
	 * @param N/A
	 * @return String
	 */
	public String getCyptoAlgorithmKeyHash();

	/**
	 * Crypto 알고리즘 키 Hash에 대한 setter
	 * 
	 * @param cyptoAlgorithmKeyHash 알고리즘키Hash코드
	 * @return void
	 */
	public void setCyptoAlgorithmKeyHash(String cyptoAlgorithmKeyHash);

	/**
	 * Crypto 블럭 사이즈에 대한 getter
	 * 
	 * @param N/A
	 * @return int
	 */
	public int getCryptoBlockSize();

	/**
	 * Crypto 블럭 사이즈에 대한 setter
	 * 
	 * @param cryptoBlockSize 블럭사이즈
	 * @return void
	 */
	public void setCryptoBlockSize(int cryptoBlockSize);
}