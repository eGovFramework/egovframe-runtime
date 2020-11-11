/**
 * 패드워드를 Encoder 하는 클래스.
 * 
 * <p><b>NOTE:</b> 파일 서비스를 제공하기 위해 구현한 클래스이다.</p>
 * 
 * @author 실행환경 개발팀 윤성종
 * @since 2009.06.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2017.02.28  장동한		시큐어코딩(ES)-제거되지 않고 남은 디버그 코드[CWE-489]
 *
 * </pre>
 */
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
package egovframework.rte.fdl.cryptography;

import org.jasypt.util.password.ConfigurablePasswordEncryptor;
import org.springframework.beans.factory.annotation.Required;

public class EgovPasswordEncoder {
	private String algorithm = "SHA-256"; // default
	private String hashedPassword;

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getAlgorithm() {
		return this.algorithm;
	}

	@Required
	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

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
