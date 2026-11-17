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
package org.egovframe.boot.crypto;

import lombok.Getter;
import lombok.Setter;
import org.jasypt.util.password.ConfigurablePasswordEncryptor;

@Getter
@Setter
public class EgovPasswordEncoder {

	private String algorithm;
	private String hashedPassword;

	public String encryptPassword(String plainPassword) {
		ConfigurablePasswordEncryptor encoder = new ConfigurablePasswordEncryptor();
		encoder.setAlgorithm(this.algorithm);
		encoder.setPlainDigest(true);
		return encoder.encryptPassword(plainPassword);
	}

	public boolean checkPassword(String plainPassword) {
		ConfigurablePasswordEncryptor encoder = new ConfigurablePasswordEncryptor();
		encoder.setAlgorithm(this.algorithm);
		encoder.setPlainDigest(true);
		return encoder.checkPassword(plainPassword, this.hashedPassword);
	}

	public boolean checkPassword(String plainPassword, String encryptedPassword) {
		ConfigurablePasswordEncryptor encoder = new ConfigurablePasswordEncryptor();
		encoder.setAlgorithm(this.algorithm);
		encoder.setPlainDigest(true);
		return encoder.checkPassword(plainPassword, encryptedPassword);
	}

}
