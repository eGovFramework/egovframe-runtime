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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

import egovframework.rte.fdl.cryptography.EgovARIACryptoService;
import egovframework.rte.fdl.cryptography.EgovPasswordEncoder;
import egovframework.rte.fdl.logging.util.EgovResourceReleaser;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class EgovARIACryptoServiceImpl implements EgovARIACryptoService {
	private final Base64 base64 = new Base64();

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovARIACryptoServiceImpl.class); // Logger 처리
	
	private static final int DEFAULT_BLOCKSIZE = 1024;
	private static final int BLOCKSIZE_MODULAR = 16;

	private EgovPasswordEncoder passwordEncoder;
	private int blockSize = DEFAULT_BLOCKSIZE;

	@Required
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

	public void encrypt(File srcFile, String password, File trgtFile) throws FileNotFoundException, IOException {
		FileInputStream fis = null;
		FileWriter fw = null;
		BufferedInputStream bis = null;
		BufferedWriter bw = null;

		byte[] buffer = null;

		if (passwordEncoder.checkPassword(password)) {
			ARIACipher cipher = new ARIACipher();

			cipher.setPassword(password);

			buffer = new byte[blockSize];

			LOGGER.debug("blockSize = {}", blockSize);

			try {
				fis = new FileInputStream(srcFile);
				bis = new BufferedInputStream(fis);

				fw = new FileWriter(trgtFile);
				bw = new BufferedWriter(fw);

				byte[] encrypted = null;
				int length = 0;
				long size = 0L;
				while ((length = bis.read(buffer)) >= 0) {
					if (length < blockSize) {
						byte[] tmp = new byte[length];
						System.arraycopy(buffer, 0, tmp, 0, length);
						encrypted = cipher.encrypt(tmp);
					} else {
						encrypted = cipher.encrypt(buffer);
					}
					String line;
					try {
						line = new String(base64.encode(encrypted), "US-ASCII");
					} catch (Exception ee) {
						throw new RuntimeException(ee);
					}
					bw.write(line);
					bw.newLine();
					size += length;
				}
				bw.flush();
				LOGGER.debug("processed bytes = {}", size);
			} finally {
				EgovResourceReleaser.close(fw, bw, fis, bis);
			}

		} else {
			LOGGER.error("password not matched!!!");
			throw new IllegalArgumentException("password not matched!!!");
		}
	}

	/* (non-Javadoc)
	 * @see egovframework.rte.fdl.cryptography.EgovCryptoService#decrypt(java.math.BigDecimal, java.lang.String)
	 */
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

	public void decrypt(File encryptedFile, String password, File trgtFile) throws FileNotFoundException, IOException {
		FileReader fr = null;
		FileOutputStream fos = null;
		BufferedReader br = null;
		BufferedOutputStream bos = null;

		if (passwordEncoder.checkPassword(password)) {
			ARIACipher cipher = new ARIACipher();

			cipher.setPassword(password);

			try {
				fr = new FileReader(encryptedFile);
				br = new BufferedReader(fr);

				fos = new FileOutputStream(trgtFile);
				bos = new BufferedOutputStream(fos);

				byte[] encrypted = null;
				byte[] decrypted = null;
				String line = null;

				while ((line = br.readLine()) != null) {
					try {
						encrypted = base64.decode(line.getBytes("US-ASCII"));
					} catch (Exception de) {
						throw new RuntimeException(de);
					}

					decrypted = cipher.decrypt(encrypted);

					bos.write(decrypted);
				}
				bos.flush();
			} finally {
				EgovResourceReleaser.close(fos, bos, fr, br);
			}

		} else {
			LOGGER.error("password not matched!!!");
			throw new IllegalArgumentException("password not matched!!!");
		}
	}
}
