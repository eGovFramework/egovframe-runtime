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
package egovframework.rte.fdl.cryptography.impl;

import java.security.InvalidKeyException;

import egovframework.rte.fdl.cryptography.impl.aria.ARIAEngine;
import egovframework.rte.fdl.cryptography.impl.aria.AnsiX923Padding;
import egovframework.rte.fdl.cryptography.impl.aria.CryptoPadding;

public class ARIACipher {
	private static final int MASTER_KEY_MAX_LENGTH = 32;
	private static final int KEY_SIZE = 256;
	
	/** 마스터 키 */
	private String masterKey = null;

	/**
	 * 암호 설정.
	 *
	 * @param masterKey 암호문자열
	 */
	public void setPassword(String masterKey) {	
		if (masterKey.length() > MASTER_KEY_MAX_LENGTH) {
			this.masterKey = masterKey.substring(0, MASTER_KEY_MAX_LENGTH);
		} else {
			this.masterKey = masterKey;
		}
	}

	/**
	 * 바이트 배열 리턴 암호화
	 *
	 * @param data 암호화할 바이트배열
	 * @return 암호화된 바이트배열
	 */
	public byte[] encrypt(byte[] data) {
		try {
			CryptoPadding padding = new AnsiX923Padding();

			byte[] mk = padding.addPadding(masterKey.getBytes(), MASTER_KEY_MAX_LENGTH);

			ARIAEngine instance = new ARIAEngine(KEY_SIZE);

			return instance.encrypt(data, mk);
		} catch (InvalidKeyException ike) {
			throw new RuntimeException(ike);
		}
	}

	/**
	 * 복호화 배열 리턴 암호화.
	 *
	 * @param encryptedData 복호화할 데이타 바이트배열
	 * @return 복호화된 바이트배열
	 */
	public byte[] decrypt(byte[] encryptedData) {
		try {
			CryptoPadding padding = new AnsiX923Padding();

			byte[] mk = padding.addPadding(masterKey.getBytes(), MASTER_KEY_MAX_LENGTH);

			ARIAEngine instance = new ARIAEngine(KEY_SIZE);

			return instance.decrypt(encryptedData, mk);
		} catch (InvalidKeyException ike) {
			throw new RuntimeException(ike);
		}
	}
}