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

import java.util.UUID;

import org.egovframe.rte.ptl.mvc.token.EgovSubmitTokenException.Reason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * <b>중복 제출 방지 토큰</b>(멱등성 토큰) 유틸 — 같은 요청이 두 번 처리되는 것을 막는다.
 *
 * <p><b>CSRF 토큰과 다르다.</b> CSRF 토큰은 "제3자 사이트가 보낸 요청인가"를 가리고
 * 세션 동안 재사용되지만, 제출 토큰은 <b>"이미 처리한 요청인가"</b>를 가리며
 * <b>1회 사용 후 소비</b>된다. 새로고침·따닥 클릭·뒤로가기 후 재전송으로 생기는
 * 이중 등록·이중 결제를 막는 용도이며, CSRF 방어(Spring Security)를 대체하지 않는다.</p>
 *
 * <p><b>사용 흐름</b> — 화면을 그릴 때 발급하고, 처리 직전에 검증한다.</p>
 * <pre>
 * // ① 폼 화면
 * model.addAttribute("submitToken", EgovSubmitTokens.issue(request));
 *
 * // ② 처리 컨트롤러 — 실패 시 사유가 담긴 예외
 * EgovSubmitTokens.validateOrThrow(request);
 * orderService.register(vo);
 * return "redirect:/orders";   // PRG 로 새로고침 재전송까지 차단
 * </pre>
 *
 * <p>JSP 는 {@code <ui:submitToken/>} 태그로 hidden input 을 바로 출력할 수 있고,
 * Thymeleaf 등 다른 뷰는 위처럼 모델에 담아 쓰면 된다 — <b>이 클래스는 뷰 기술에
 * 의존하지 않는다.</b></p>
 *
 * <p><b>토큰 키</b>는 화면(폼)을 구분한다. 한 세션에서 여러 폼을 동시에 열 수 있으므로
 * 폼마다 다른 키를 주면 서로 간섭하지 않는다. 지정하지 않으면 {@link #DEFAULT_TOKEN_KEY}.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (공통컴포넌트 EgovDoubleSubmitHelper·
 *                            DoubleSubmitTag 의 설계를 차용하되 원자적 검증·뷰 비종속·
 *                            명시 예외·토큰 상한으로 재구성 — 코드 이식 아님)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @see EgovSubmitTokenException
 * @see EgovSubmitTokenStore
 */
public final class EgovSubmitTokens {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovSubmitTokens.class);

	/** 토큰을 담는 요청 파라미터(및 hidden input) 이름. */
	public static final String PARAMETER_NAME = "egovSubmitToken";

	/** 토큰 저장소를 보관하는 세션 속성 이름. */
	public static final String SESSION_ATTRIBUTE_NAME =
			"org.egovframe.rte.ptl.mvc.token.EgovSubmitTokens.STORE";

	/** 토큰 키를 지정하지 않았을 때 쓰는 기본 키. */
	public static final String DEFAULT_TOKEN_KEY = "DEFAULT";

	/** 세션당 보관하는 최대 토큰 수 — 초과 시 가장 오래된 토큰부터 제거된다. */
	public static final int MAX_TOKENS_PER_SESSION = 32;

	private EgovSubmitTokens() {
	}

	/**
	 * 기본 키로 토큰을 발급한다. 세션이 없으면 생성한다.
	 *
	 * @param request 현재 요청 (null 불가)
	 * @return 발급된 토큰 값
	 */
	public static String issue(HttpServletRequest request) {
		return issue(request, DEFAULT_TOKEN_KEY);
	}

	/**
	 * 지정한 키로 토큰을 발급한다. 세션이 없으면 생성하며, 같은 키에 이미 토큰이 있으면
	 * 새 값으로 교체한다.
	 *
	 * @param request  현재 요청 (null 불가)
	 * @param tokenKey 토큰 키 (null·공백이면 {@link #DEFAULT_TOKEN_KEY})
	 * @return 발급된 토큰 값
	 * @throws IllegalArgumentException request 가 null 인 경우
	 */
	public static String issue(HttpServletRequest request, String tokenKey) {
		assertRequest(request);
		String key = normalizeKey(tokenKey);
		String token = UUID.randomUUID().toString();

		HttpSession session = request.getSession();
		// 저장소 최초 생성이 요청 간에 겹칠 수 있으므로 세션을 모니터로 잡는다.
		// 이후 토큰 조작은 저장소 자신이 동기화한다.
		EgovSubmitTokenStore store;
		synchronized (session) {
			Object attribute = session.getAttribute(SESSION_ATTRIBUTE_NAME);
			if (attribute instanceof EgovSubmitTokenStore) {
				store = (EgovSubmitTokenStore) attribute;
			} else {
				store = new EgovSubmitTokenStore(MAX_TOKENS_PER_SESSION);
				session.setAttribute(SESSION_ATTRIBUTE_NAME, store);
			}
		}

		store.put(key, token);
		LOGGER.debug("제출 토큰 발급 — tokenKey={}", key);
		return token;
	}

	/**
	 * 기본 키로 토큰을 검증한다. 성공하면 토큰은 <b>소비되어 재사용할 수 없다</b>.
	 *
	 * @param request 현재 요청 (null 불가)
	 * @return 유효한 요청이면 true, 중복 제출·미발급·세션 부재면 false
	 */
	public static boolean validate(HttpServletRequest request) {
		return validate(request, DEFAULT_TOKEN_KEY);
	}

	/**
	 * 지정한 키로 토큰을 검증한다. 성공하면 토큰은 <b>소비되어 재사용할 수 없다</b>.
	 *
	 * <p>확인과 소비가 한 임계 구역에서 일어나므로, 같은 토큰으로 <b>동시에</b> 들어온
	 * 요청 중 정확히 하나만 true 를 받는다.</p>
	 *
	 * @param request  현재 요청 (null 불가)
	 * @param tokenKey 토큰 키 (null·공백이면 {@link #DEFAULT_TOKEN_KEY})
	 * @return 유효한 요청이면 true
	 * @throws IllegalArgumentException request 가 null 인 경우
	 */
	public static boolean validate(HttpServletRequest request, String tokenKey) {
		return resolveFailure(request, tokenKey) == null;
	}

	/**
	 * 기본 키로 토큰을 검증하고, 실패하면 사유가 담긴 예외를 던진다.
	 *
	 * @param request 현재 요청 (null 불가)
	 * @throws EgovSubmitTokenException 검증에 실패한 경우
	 */
	public static void validateOrThrow(HttpServletRequest request) {
		validateOrThrow(request, DEFAULT_TOKEN_KEY);
	}

	/**
	 * 지정한 키로 토큰을 검증하고, 실패하면 사유가 담긴 예외를 던진다.
	 *
	 * @param request  현재 요청 (null 불가)
	 * @param tokenKey 토큰 키 (null·공백이면 {@link #DEFAULT_TOKEN_KEY})
	 * @throws EgovSubmitTokenException 검증에 실패한 경우
	 * @throws IllegalArgumentException request 가 null 인 경우
	 */
	public static void validateOrThrow(HttpServletRequest request, String tokenKey) {
		Reason failure = resolveFailure(request, tokenKey);
		if (failure != null) {
			throw new EgovSubmitTokenException(failure, normalizeKey(tokenKey));
		}
	}

	/**
	 * 세션에 보관된 제출 토큰을 모두 제거한다(로그아웃·세션 정리 시).
	 * 세션이 없으면 아무 일도 하지 않는다.
	 *
	 * @param request 현재 요청 (null 불가)
	 * @throws IllegalArgumentException request 가 null 인 경우
	 */
	public static void reset(HttpServletRequest request) {
		assertRequest(request);
		EgovSubmitTokenStore store = findStore(request);
		if (store != null) {
			store.clear();
		}
	}

	/**
	 * 검증을 수행하고 실패 사유를 반환한다.
	 *
	 * @return 성공이면 null, 실패면 사유
	 */
	private static Reason resolveFailure(HttpServletRequest request, String tokenKey) {
		assertRequest(request);
		String key = normalizeKey(tokenKey);

		EgovSubmitTokenStore store = findStore(request);
		if (store == null) {
			LOGGER.debug("제출 토큰 검증 실패(세션·저장소 없음) — tokenKey={}", key);
			return Reason.NO_SESSION;
		}

		String parameter = request.getParameter(PARAMETER_NAME);
		if (parameter == null || parameter.isEmpty()) {
			LOGGER.debug("제출 토큰 검증 실패(파라미터 없음) — tokenKey={}", key);
			return Reason.NO_PARAMETER;
		}

		// 확인과 소비가 원자적으로 일어난다 — 동시 요청 중 하나만 통과
		if (store.consume(key, parameter)) {
			LOGGER.debug("제출 토큰 검증 성공·소비 — tokenKey={}", key);
			return null;
		}

		// 미발급(이미 소비 포함)과 불일치를 구분해 알린다
		Reason reason = store.isIssued(key) ? Reason.TOKEN_MISMATCH : Reason.TOKEN_NOT_ISSUED;
		LOGGER.debug("제출 토큰 검증 실패({}) — tokenKey={}", reason, key);
		return reason;
	}

	/**
	 * 세션에서 저장소를 찾는다. 세션을 새로 만들지 않는다.
	 *
	 * @return 저장소, 없으면 null
	 */
	private static EgovSubmitTokenStore findStore(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}
		Object attribute = session.getAttribute(SESSION_ATTRIBUTE_NAME);
		return (attribute instanceof EgovSubmitTokenStore) ? (EgovSubmitTokenStore) attribute : null;
	}

	private static String normalizeKey(String tokenKey) {
		return (tokenKey == null || tokenKey.trim().isEmpty()) ? DEFAULT_TOKEN_KEY : tokenKey;
	}

	private static void assertRequest(HttpServletRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("request must not be null");
		}
	}

}
