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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 세션 만료 시각 정보 — 서버 시각과 만료 예정 시각을 함께 담는 값 객체.
 *
 * <p>세션 잔여 시간을 화면에 표시하려면 <b>서버 시각도 함께</b> 필요하다. 클라이언트 시계는
 * 서버와 어긋나 있을 수 있으므로, 남은 시간은 "만료 시각 − <b>서버</b> 시각"으로 계산한 뒤
 * 그 시점부터 브라우저에서 흘려보내야 한다.</p>
 *
 * <p><b>쿠키를 거치지 않고 바로 쓸 수 있다.</b> {@link EgovSessionExpiryCookieFilter} 가
 * 쿠키로도 실어 보내지만, 쿠키는 <b>다음 요청부터</b> 브라우저가 되돌려 주므로 최초 진입
 * 화면에서는 값이 비어 있다. 컨트롤러에서 이 클래스로 값을 직접 얻어 모델에 담으면
 * 그 문제가 없다.</p>
 *
 * <pre>
 * EgovSessionExpiry.from(request)
 *         .ifPresent(expiry -&gt; model.addAttribute("sessionExpiry", expiry));
 * </pre>
 *
 * <p><b>무제한 세션</b>({@code maxInactiveInterval} 이 0 이하)이면 {@link #getExpiryTime()} 이
 * {@link #UNLIMITED}(-1)를 반환한다. 화면은 이 값을 보고 카운트다운을 하지 않으면 된다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (공통컴포넌트 SessionTimeoutCookieFilter 가
 *                            필터 안에 계산을 묻어 두어 쿠키를 거쳐야만 값을 얻을 수 있던
 *                            구조를, 서버에서 바로 조회 가능한 값 객체로 분리)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
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
	 * 현재 요청의 세션에서 만료 정보를 얻는다.
	 *
	 * <p><b>세션을 새로 만들지 않는다</b>({@code getSession(false)}) — 정적 자원 요청처럼
	 * 세션이 필요 없는 요청 때문에 세션이 생기면 서버 자원이 낭비된다.</p>
	 *
	 * @param request 현재 요청 (null 불가)
	 * @return 만료 정보. <b>세션이 없으면 빈 Optional</b>
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
		// interval 이 0 이하이면 만료 없음(서블릿 규약). 곱셈은 long 으로 — int 연산은
		// 약 24.8일을 넘기면 넘침이 생기고, 음수일 때는 과거 시각이 되어 즉시 만료로 오판된다.
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
