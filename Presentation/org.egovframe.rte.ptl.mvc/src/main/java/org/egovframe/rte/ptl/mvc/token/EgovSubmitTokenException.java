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

/**
 * 중복 제출 방지 토큰 검증 실패를 알리는 unchecked 예외.
 *
 * <p>{@link EgovSubmitTokens#validateOrThrow(jakarta.servlet.http.HttpServletRequest)} 계열이
 * 던진다. 실패 사유는 {@link Reason} 으로 구분되므로, 화면에서 "중복 제출입니다"와
 * "세션이 만료됐습니다"를 다르게 안내할 수 있다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (공통컴포넌트 EgovDoubleSubmitHelper 의
 *                            raw RuntimeException 을 사유 구분 가능한 명시 예외로 재구성)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @see EgovSubmitTokens
 */
public class EgovSubmitTokenException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 검증 실패 사유.
	 */
	public enum Reason {

		/** 세션이 없거나 만료됐다 — 발급 이력 자체가 사라진 상태. */
		NO_SESSION("세션이 없거나 만료되어 제출 토큰을 확인할 수 없습니다."),

		/** 요청에 토큰 파라미터가 없다 — 화면에 토큰을 심지 않았을 가능성이 크다. */
		NO_PARAMETER("요청에 제출 토큰 파라미터가 없습니다. 화면에 토큰을 출력했는지 확인하십시오."),

		/** 토큰이 발급되지 않았거나 이미 소비됐다 — <b>중복 제출의 전형</b>. */
		TOKEN_NOT_ISSUED("발급되지 않았거나 이미 사용된 제출 토큰입니다."),

		/** 세션 토큰과 요청 토큰이 다르다. */
		TOKEN_MISMATCH("제출 토큰이 일치하지 않습니다.");

		private final String defaultMessage;

		Reason(String defaultMessage) {
			this.defaultMessage = defaultMessage;
		}

		/**
		 * 사유별 기본 메시지를 반환한다.
		 *
		 * @return 기본 메시지
		 */
		public String getDefaultMessage() {
			return defaultMessage;
		}
	}

	private final transient Reason reason;

	private final transient String tokenKey;

	/**
	 * 검증 실패 예외를 생성한다.
	 *
	 * @param reason   실패 사유 (null 불가)
	 * @param tokenKey 검증 대상 토큰 키
	 */
	public EgovSubmitTokenException(Reason reason, String tokenKey) {
		super(reason.getDefaultMessage() + " (tokenKey=" + tokenKey + ")");
		this.reason = reason;
		this.tokenKey = tokenKey;
	}

	/**
	 * 실패 사유를 반환한다.
	 *
	 * @return 실패 사유
	 */
	public Reason getReason() {
		return reason;
	}

	/**
	 * 검증 대상이던 토큰 키를 반환한다.
	 *
	 * @return 토큰 키
	 */
	public String getTokenKey() {
		return tokenKey;
	}

	/**
	 * 중복 제출로 판단되는 사유인지 여부.
	 *
	 * <p>{@link Reason#TOKEN_NOT_ISSUED} · {@link Reason#TOKEN_MISMATCH} 는 이미 처리된
	 * 요청의 재전송으로 보는 것이 타당하다. 반면 {@link Reason#NO_SESSION} 은 세션 만료,
	 * {@link Reason#NO_PARAMETER} 는 화면 구성 누락에 가깝다.</p>
	 *
	 * @return 중복 제출로 판단되면 true
	 */
	public boolean isDuplicateSubmit() {
		return reason == Reason.TOKEN_NOT_ISSUED || reason == Reason.TOKEN_MISMATCH;
	}

}
