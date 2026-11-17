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
package org.egovframe.rte.ptl.mvc.session;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 서버 시각과 세션 만료 예정 시각을 담는 값 객체.
 * 무제한 세션이면 만료 시각은 {@link #UNLIMITED}(-1)이며, 쿠키가 없는 최초 화면에서는 컨트롤러에서 직접 조회한다.
 *
 * <pre>
 * EgovSessionExpiry.from(request).ifPresent(expiry -&gt; model.addAttribute("sessionExpiry", expiry));
 * </pre>
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
 * @see EgovSessionExpiryCookieFilter
 */
public final class EgovSessionExpiry {

	/** 무제한 세션임을 나타내는 만료 시각 값. */
	public static final long UNLIMITED = -1L;

	private final long serverTime;

	private final long expiryTime;

	private final int maxInactiveIntervalSeconds;

	private EgovSessionExpiry(long serverTime, long expiryTime, int maxInactiveIntervalSeconds) {
		this.serverTime = serverTime;
		this.expiryTime = expiryTime;
		this.maxInactiveIntervalSeconds = maxInactiveIntervalSeconds;
	}

	/**
	 * 현재 요청의 세션에서 만료 정보를 얻는다. 세션을 새로 만들지 않는다.
	 *
	 * @param request 현재 요청 (null 불가)
	 * @return 만료 정보. 세션이 없으면 빈 Optional
	 * @throws IllegalArgumentException request 가 null 인 경우
	 */
	public static Optional<EgovSessionExpiry> from(HttpServletRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("request must not be null");
		}
		HttpSession session = request.getSession(false);
		if (session == null) {
			return Optional.empty();
		}
		return Optional.of(of(session, System.currentTimeMillis()));
	}

	/**
	 * 지정한 세션과 기준 시각으로 만료 정보를 만든다.
	 *
	 * @param session    대상 세션 (null 불가)
	 * @param serverTime 기준 서버 시각(epoch millis)
	 * @return 만료 정보
	 * @throws IllegalArgumentException session 이 null 인 경우
	 */
	public static EgovSessionExpiry of(HttpSession session, long serverTime) {
		if (session == null) {
			throw new IllegalArgumentException("session must not be null");
		}
		int interval = session.getMaxInactiveInterval();
		// 0 이하이면 만료 없음(서블릿 규약). int 넘침 방지를 위해 long으로 계산한다.
		long expiry = (interval <= 0) ? UNLIMITED : serverTime + (long) interval * 1000L;
		return new EgovSessionExpiry(serverTime, expiry, interval);
	}

	/**
	 * 기준 서버 시각(epoch millis)을 반환한다.
	 *
	 * @return 서버 시각
	 */
	public long getServerTime() {
		return serverTime;
	}

	/**
	 * 세션 만료 예정 시각(epoch millis)을 반환한다.
	 *
	 * @return 만료 시각, 무제한 세션이면 {@link #UNLIMITED}
	 */
	public long getExpiryTime() {
		return expiryTime;
	}

	/**
	 * 세션 비활성 유지 시간(초)을 반환한다.
	 *
	 * @return {@code HttpSession.getMaxInactiveInterval()} 값
	 */
	public int getMaxInactiveIntervalSeconds() {
		return maxInactiveIntervalSeconds;
	}

	/**
	 * 만료가 없는 세션인지 여부.
	 *
	 * @return 무제한이면 true
	 */
	public boolean isUnlimited() {
		return expiryTime == UNLIMITED;
	}

	/**
	 * 기준 서버 시각으로부터의 잔여 시간(ms)을 반환한다.
	 *
	 * @return 잔여 밀리초. 무제한이면 {@link #UNLIMITED}
	 */
	public long getRemainingMillis() {
		return isUnlimited() ? UNLIMITED : expiryTime - serverTime;
	}

	/**
	 * 지정 시각 기준으로 이미 만료됐는지 판단한다.
	 *
	 * @param currentTime 판단 기준 시각(epoch millis)
	 * @return 만료됐으면 true. 무제한 세션은 항상 false
	 */
	public boolean isExpiredAt(long currentTime) {
		return !isUnlimited() && currentTime >= expiryTime;
	}

	@Override
	public String toString() {
		return "EgovSessionExpiry[serverTime=" + serverTime + ", expiryTime=" + expiryTime
				+ ", maxInactiveIntervalSeconds=" + maxInactiveIntervalSeconds + "]";
	}

}
