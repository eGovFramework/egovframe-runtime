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
 *
 * <p><b>이 클래스의 존재 이유는 원자성이다.</b> 토큰 검증은 "값이 같은지 확인하고 지운다"는
 * 복합 연산이라, 확인과 삭제 사이에 다른 요청이 끼어들면 <b>둘 다 통과</b>한다 —
 * 바로 이 기능이 막으려던 상황이다. 따라서 모든 상태 변경은 이 객체를 모니터로 삼아
 * {@code synchronized} 안에서 수행한다.</p>
 *
 * <p>세션당 토큰 수는 {@link #maxTokens} 로 제한한다. 화면마다 다른 토큰 키를 쓰는 앱에서
 * 토큰이 무한정 쌓여 세션이 비대해지는 것을 막기 위함이며, 초과 시 <b>가장 오래 전에 발급된
 * 토큰부터</b> 제거한다({@link LinkedHashMap} 삽입 순서).</p>
 *
 * <p>세션 직렬화·복제(클러스터) 환경을 고려해 {@link Serializable} 이다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (원본의 비동기화 HashMap + check-then-act 를
 *                            원자 연산으로 재구성, 세션당 토큰 수 상한 도입)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
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
	 * 토큰을 발급해 보관하고 반환한다. 같은 키에 이미 토큰이 있으면 <b>새 값으로 교체</b>한다
	 * (화면을 다시 열면 이전 화면의 토큰은 무효가 된다).
	 *
	 * @param tokenKey 토큰 키
	 * @param token    발급할 토큰 값
	 * @return 보관된 토큰 값(= 인자 token)
	 */
	public synchronized String put(String tokenKey, String token) {
		tokens.remove(tokenKey);
		tokens.put(tokenKey, token);
		while (tokens.size() > maxTokens) {
			// LinkedHashMap 은 삽입 순서를 유지하므로 첫 항목이 가장 오래된 토큰이다
			String oldest = tokens.keySet().iterator().next();
			tokens.remove(oldest);
		}
		return token;
	}

	/**
	 * 토큰이 일치하면 <b>제거하고</b> true 를 반환한다 — 확인과 제거가 한 임계 구역에서
	 * 일어나므로 동시 요청 중 <b>정확히 하나만</b> true 를 받는다.
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
