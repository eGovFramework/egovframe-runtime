/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.ai.core.ingest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * 문서 본문 해시 — UTF-8 바이트의 SHA-256 16진(64자). 변경 감지 키로 쓴다.
 *
 * <p>참고 샘플은 commons-codec MD5 를 썼다. 보안 결함은 아니지만(변경 감지 용도) 공공 감사에서 MD5 는 설명 비용이 들고
 * 의존이 하나 늘므로 JDK SHA-256 으로 교체했다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovContentHash {

	private static final char[] HEX = "0123456789abcdef".toCharArray();

	private EgovContentHash() {
	}

	/**
	 * SHA-256 16진 해시.
	 *
	 * @param content 본문(null 불가 — 빈 문자열은 빈 문자열의 해시)
	 */
	public static String sha256Hex(String content) {
		Objects.requireNonNull(content, "content must not be null");
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
			char[] out = new char[bytes.length * 2];
			for (int i = 0; i < bytes.length; i++) {
				int v = bytes[i] & 0xFF;
				out[i * 2] = HEX[v >>> 4];
				out[i * 2 + 1] = HEX[v & 0x0F];
			}
			return new String(out);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 을 지원하지 않는 JVM 입니다", e);
		}
	}
}
