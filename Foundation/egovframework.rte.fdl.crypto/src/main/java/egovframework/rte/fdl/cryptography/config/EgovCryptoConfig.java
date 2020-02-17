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
package egovframework.rte.fdl.cryptography.config;

/**
 * EgovCryptoConfig 클래스
 * <Notice>
 * 	    Definition에 대한 내역 저장
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
public class EgovCryptoConfig  {

	/* Definition 계정명 */
	private String username = "";
	
	/* Definition 패드워드 */
	private String password = "";
	
	/* Definition url */
	private String url = "";
	
	/* Definition crypto여부 */
	private boolean crypto = false;
	
	/* Definition 초기화여부 */
	private boolean initial = false;
	
	/* Definition 알고리즘 */
	private String algorithm = "";
	
	/* Definition 알고리즘키 */
	private String algorithmKey = "";
	
	/* Definition 알고리즘키Hash */
	private String algorithmKeyHash = "";
	
	/* Definition 환경설정 Property 경로 */
	private String cryptoPropertyLocation = "";
	
	/**
	 * 환경설정 Property 경로 getter
	 * 
	 * @param N/A
	 * @return String
	 */
	public String getCryptoPropertyLocation() {
		return cryptoPropertyLocation;
	}

	/**
	 * 환경설정 Property 경로 setter
	 * 
	 * @param cryptoPropertyLocation 환경설정Property경로
	 * @return void
	 */
	public void setCryptoPropertyLocation(String cryptoPropertyLocation) {
		this.cryptoPropertyLocation = cryptoPropertyLocation;
	}

	/* Definition 알고리즘 블럭 사이즈 */
	private int cryptoBlockSize = 0;
	
	/**
	 * 데이터베이스 접속 항목(계정명)에 대한 getter
	 * 
	 * @param N/A
	 * @return String
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Definition 항목(계정명)에 대한 setter
	 * 
	 * @param username 계정명
	 * @return void
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * 데이터베이스 접속 항목(비밀번호)에 대한 getter
	 * 
	 * @param N/A
	 * @return String
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 데이터베이스 접속 항목(비밀번호)에 대한 setter
	 * 
	 * @param password 비밀번호
	 * @return void
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 데이터베이스 접속 항목(URL)에 대한 setter
	 * 
	 * @param N/A
	 * @return String
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * 데이터베이스 접속 항목(URL)에 대한 getter
	 * 
	 * @param url 접속경로
	 * @return void
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Crypto 사용여부에 대한 getter
	 * 
	 * @param N/A
	 * @return boolean
	 */
	public boolean isCrypto() {
		return crypto;
	}

	/**
	 * Crypto 사용여부에 대한 setter
	 * 
	 * @param crypto Crypto사용여부
	 * @return void
	 */
	public void setCrypto(boolean crypto) {
		this.crypto = crypto;
	}
	
	/**
	 * 초기화 여부에 대한 getter
	 * 
	 * @param N/A
	 * @return boolean
	 */
	public boolean isInitial() {
		return initial;
	}

	/**
	 * 초기화 여부에 대한 setter
	 * 
	 * @param initial 초기화여부
	 * @return void
	 */
	public void setInitial(boolean initial) {
		this.initial = initial;
	}

	/**
	 * Crypto 계정 알고리즘 지정에 대한 getter
	 * 
	 * @param cryptoAlgorithm 계정 암호화 알고리즘(MD5, SHA-1, SHA-256)
	 * @return void
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * Crypto 계정 알고리즘 지정에 대한 setter
	 * 
	 * @param N/A
	 * @return String
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * Crypto 계정 알고리즘키에 대한 getter
	 * 
	 * @param N/A
	 * @return String
	 */
	public String getAlgorithmKey() {
		return algorithmKey;
	}

	/**
	 * Crypto 알고리즘 키에 대한 setter
	 * 
	 * @param cyptoAlgorithmKey 알고리즘키
	 * @return void
	 */
	public void setAlgorithmKey(String algorithmKey) {
		this.algorithmKey = algorithmKey;
	}

	/**
	 * Crypto 알고리즘 키 Hash에 대한 getter
	 * 
	 * @param N/A
	 * @return String
	 */
	public String getAlgorithmKeyHash() {
		return algorithmKeyHash;
	}

	/**
	 * Crypto 알고리즘 키 Hash에 대한 setter
	 * 
	 * @param cyptoAlgorithmKeyHash 알고리즘키Hash코드
	 * @return void
	 */
	public void setAlgorithmKeyHash(String algorithmKeyHash) {
		this.algorithmKeyHash = algorithmKeyHash;
	}

	/**
	 * Crypto 블럭 사이즈에 대한 getter
	 * 
	 * @param N/A
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
