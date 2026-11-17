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
package org.egovframe.rte.ptl.mvc.token;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 세션에 보관되는 중복 제출 방지 토큰 저장소.
 * 토큰 확인과 소비를 synchronized로 원자 처리하며, 토큰 수 상한 초과 시 가장 오래된 토큰부터 제거한다.
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 4.3.1
 * @see EgovSubmitTokens
 */
public class EgovSubmitTokenStore implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<String, String> tokens = new LinkedHashMap<>();

	private final int maxTokens;

	/**
	 * 저장소를 생성한다.
	 *
	 * @param maxTokens 세션당 보관할 최대 토큰 수 (1 이상)
	 * @throws IllegalArgumentException maxTokens 가 1 미만인 경우
	 */
	public EgovSubmitTokenStore(int maxTokens) {
		if (maxTokens < 1) {
			throw new IllegalArgumentException("maxTokens must be >= 1, but was " + maxTokens);
		}
		this.maxTokens = maxTokens;
	}

	/**
	 * 토큰을 보관하고 반환한다. 같은 키에 토큰이 있으면 새 값으로 교체한다.
	 *
	 * @param tokenKey 토큰 키
	 * @param token    발급할 토큰 값
	 * @return 보관된 토큰 값(= 인자 token)
	 */
	public synchronized String put(String tokenKey, String token) {
		tokens.remove(tokenKey);
		tokens.put(tokenKey, token);
		while (tokens.size() > maxTokens) {
			// 삽입 순서상 첫 항목이 가장 오래된 토큰
			String oldest = tokens.keySet().iterator().next();
			tokens.remove(oldest);
		}
		return token;
	}

	/**
	 * 토큰이 일치하면 제거하고 true를 반환한다(동시 요청 중 하나만 true).
	 *
	 * @param tokenKey 토큰 키
	 * @param token    요청이 제시한 토큰 값
	 * @return 일치해 소비했으면 true, 미발급이거나 불일치면 false
	 */
	public synchronized boolean consume(String tokenKey, String token) {
		String issued = tokens.get(tokenKey);
		if (issued != null && issued.equals(token)) {
			tokens.remove(tokenKey);
			return true;
		}
		return false;
	}

	/**
	 * 해당 키에 발급된 토큰이 있는지 확인한다(소비하지 않는다).
	 *
	 * @param tokenKey 토큰 키
	 * @return 발급돼 있으면 true
	 */
	public synchronized boolean isIssued(String tokenKey) {
		return tokens.containsKey(tokenKey);
	}

	/**
	 * 보관 중인 토큰을 모두 제거한다.
	 */
	public synchronized void clear() {
		tokens.clear();
	}

	/**
	 * 보관 중인 토큰 수를 반환한다.
	 *
	 * @return 토큰 수
	 */
	public synchronized int size() {
		return tokens.size();
	}

}
