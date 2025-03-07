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
package org.egovframe.rte.fdl.cryptography.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.egovframe.rte.fdl.cryptography.EgovARIACryptoService;
import org.egovframe.rte.fdl.cryptography.EgovPasswordEncoder;
import org.egovframe.rte.fdl.logging.util.EgovResourceReleaser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class EgovARIACryptoServiceImpl implements EgovARIACryptoService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovARIACryptoServiceImpl.class);

	private static final int BLOCKSIZE_MODULAR = 16;
	private EgovPasswordEncoder passwordEncoder;
	private int blockSize = 1024;

	public void setPasswordEncoder(EgovPasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
		LOGGER.debug("passwordEncoder's algorithm : {}", passwordEncoder.getAlgorithm());
	}

	public void setBlockSize(int blockSize) {
		if (blockSize % BLOCKSIZE_MODULAR != 0) {
			blockSize += (BLOCKSIZE_MODULAR - blockSize % BLOCKSIZE_MODULAR);
		}
		this.blockSize = blockSize;
	}

	public BigDecimal encrypt(BigDecimal number, String password) {
		throw new UnsupportedOperationException("Unsupported method.. (ARIA Cryptography service doesn't support BigDecimal en/decryption)");
	}

	public byte[] encrypt(byte[] data, String password) {
		if (passwordEncoder.checkPassword(password)) {
			ARIACipher cipher = new ARIACipher();
			cipher.setPassword(password);
			return cipher.encrypt(data);
		} else {
			LOGGER.error("password not matched!!!");
			throw new IllegalArgumentException("password not matched!!!");
		}
	}

	public void encrypt(File srcFile, String password, File trgtFile) {
		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;
		String fileString = new String();
		byte[] buffer = null;
		if (passwordEncoder.checkPassword(password)) {
			ARIACipher cipher = new ARIACipher();
			cipher.setPassword(password);
			try {
				fis = new FileInputStream(srcFile);
				baos = new ByteArrayOutputStream();
				buffer = new byte[blockSize];
				LOGGER.debug("blockSize = {}", blockSize);
				int len = 0;
				while ((len = fis.read(buffer)) != -1) {
					if (len < blockSize) {
						byte[] tmp = new byte[len];
						System.arraycopy(buffer, 0, tmp, 0, len);
						baos.write(tmp, 0, len);
					} else {
						baos.write(buffer, 0, len);
					}
				}
				byte[] fileArray = baos.toByteArray();
				fileString = new String(Base64.encodeBase64(fileArray));
				byte[] enc = cipher.encrypt(fileString.getBytes(StandardCharsets.UTF_8));
				String encString = Base64.encodeBase64String(enc);
				FileUtils.writeStringToFile(trgtFile, encString, "UTF-8", true);
			} catch (IOException e) {
				ReflectionUtils.handleReflectionException(e);
			} finally {
				EgovResourceReleaser.close(fis, baos);
			}
		} else {
			LOGGER.error("password not matched!!!");
			throw new IllegalArgumentException("password not matched!!!");
		}
	}

	public BigDecimal decrypt(BigDecimal encryptedNumber, String password) {
		throw new UnsupportedOperationException("Unsupported method.. (ARIA Cryptography service doesn't support BigDecimal en/decryption)");
	}

	public byte[] decrypt(byte[] encryptedData, String password) {
		if (passwordEncoder.checkPassword(password)) {
			ARIACipher cipher = new ARIACipher();
			cipher.setPassword(password);
			return cipher.decrypt(encryptedData);
		} else {
			LOGGER.error("password not matched!!!");
			throw new IllegalArgumentException("password not matched!!!");
		}
	}

	public void decrypt(File encryptedFile, String password, File trgtFile) {
		if (passwordEncoder.checkPassword(password)) {
			ARIACipher cipher = new ARIACipher();
			cipher.setPassword(password);
			try {
				String readEncString = FileUtils.readFileToString(encryptedFile, "UTF-8");
				byte[] decEnc = Base64.decodeBase64(readEncString);
				byte[] dec = cipher.decrypt(decEnc);
				String decBase64String = new String(dec, StandardCharsets.UTF_8);
				byte[] data = Base64.decodeBase64(decBase64String);
				FileUtils.writeByteArrayToFile(trgtFile, data);
			} catch (IOException e) {
				ReflectionUtils.handleReflectionException(e);
			}
		} else {
			LOGGER.error("password not matched!!!");
			throw new IllegalArgumentException("password not matched!!!");
		}
	}

}
