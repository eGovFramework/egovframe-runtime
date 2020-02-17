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

import egovframework.rte.fdl.cryptography.EgovDigestService;

import org.jasypt.digest.StandardByteDigester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EgovDigestServiceImpl implements EgovDigestService {
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovDigestServiceImpl.class); // Logger 처리
	
	private static final int DEFAULT_STRONG_ITERATIONS = 1000;
	private static final int DEFAULT_STRONG_SALT_SIZE = 8;

	private String algorithm = "SHA-256"; // default
	private boolean plainDigest = false; // default

	private int strongIterations = DEFAULT_STRONG_ITERATIONS;
	private int strongSaltSizeBytes = DEFAULT_STRONG_SALT_SIZE;

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setPlainDigest(boolean plainDigest) {
		this.plainDigest = plainDigest;
	}

	public boolean isPlainDigest() {
		return plainDigest;
	}

	public byte[] digest(byte[] data) {
		StandardByteDigester digester = new StandardByteDigester();

		digester.setAlgorithm(algorithm);

		LOGGER.debug("Digest's algorithm : {}", algorithm);

		if (plainDigest) {
			digester.setIterations(1);
			digester.setSaltSizeBytes(0);
		} else {
			digester.setIterations(strongIterations);
			digester.setSaltSizeBytes(strongSaltSizeBytes);
		}

		return digester.digest(data);
	}

	public boolean matches(byte[] messageByte, byte[] digestByte) {
		StandardByteDigester digester = new StandardByteDigester();

		digester.setAlgorithm(algorithm);

		LOGGER.debug("Digest's algorithm : {}", algorithm);

		if (plainDigest) {
			digester.setIterations(1);
			digester.setSaltSizeBytes(0);
		} else {
			digester.setIterations(strongIterations);
			digester.setSaltSizeBytes(strongSaltSizeBytes);
		}

		return digester.matches(messageByte, digestByte);
	}
}
