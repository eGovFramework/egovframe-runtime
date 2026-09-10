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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.egovframe.rte.ptl.mvc.cookie.EgovCookies;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 세션 만료 시각을 쿠키로 내려 주는 필터 — 화면이 세션 잔여 시간을 표시할 수 있게 한다.
 *
 * <p>매 요청마다 두 쿠키를 갱신한다. 요청이 오면 세션 유효 기간이 연장되므로, 갱신된 값을
 * 그때그때 내려 줘야 화면 카운트다운이 실제 세션과 어긋나지 않는다.</p>
 *
 * <table border="1">
 * <caption>발행 쿠키</caption>
 * <tr><th>쿠키</th><th>값</th></tr>
 * <tr><td>{@value #DEFAULT_SERVER_TIME_COOKIE}</td><td>서버 현재 시각(epoch millis)</td></tr>
 * <tr><td>{@value #DEFAULT_EXPIRY_TIME_COOKIE}</td>
 *     <td>세션 만료 예정 시각(epoch millis). 무제한 세션이면 {@code -1}</td></tr>
 * </table>
 *
 * <p><b>세션을 새로 만들지 않는다.</b> 세션이 없는 요청(정적 자원·비로그인 최초 진입)에는
 * 쿠키를 발행하지 않고 지나간다.</p>
 *
 * <p><b>{@code httpOnly} 기본값이 {@code false} 인 이유</b> — 이 쿠키는 <b>브라우저
 * 스크립트가 읽어서</b> 잔여 시간을 표시하라고 있는 것이다. {@code httpOnly=true} 로 두면
 * 스크립트가 읽지 못해 목적을 잃는다(그 경우 서버에서 값을 심어 주는 우회가 필요해진다).
 * 담기는 값은 세션 식별자가 아니라 시각 두 개뿐이라 노출 위험이 낮다. 그래도 막고 싶으면
 * {@code httpOnly} 초기화 파라미터로 켤 수 있다.</p>
 *
 * <p><b>최초 진입 화면</b>은 쿠키가 아직 브라우저에 없다(응답에 실린 쿠키는 <b>다음</b>
 * 요청부터 되돌아온다). 그 화면에서도 값이 필요하면 컨트롤러에서
 * {@link EgovSessionExpiry#from(HttpServletRequest)} 로 직접 얻어 모델에 담는다.</p>
 *
 * <p><b>web.xml 설정 예</b></p>
 * <pre>
 * &lt;filter&gt;
 *     &lt;filter-name&gt;sessionExpiryCookieFilter&lt;/filter-name&gt;
 *     &lt;filter-class&gt;org.egovframe.rte.ptl.mvc.session.EgovSessionExpiryCookieFilter&lt;/filter-class&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;excludePathPatterns&lt;/param-name&gt;
 *         &lt;param-value&gt;/css/**,/js/**,/images/**&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 *
 * <p>지원하는 초기화 파라미터: {@code serverTimeCookieName} · {@code expiryTimeCookieName} ·
 * {@code cookiePath} · {@code httpOnly} · {@code sameSite} · {@code excludePathPatterns}</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (공통컴포넌트 SessionTimeoutCookieFilter 의
 *                            설계를 차용하되 세션 강제 생성·httpOnly 목적 모순·무제한 세션
 *                            음수 계산·경로 제외 부재를 재구성 — 코드 이식 아님)
 *  2026.09.02  실행환경팀     쿠키 조립을 EgovCookies 빌더에 위임 (동작·초기화 파라미터 불변)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @see EgovSessionExpiry
 */
public class EgovSessionExpiryCookieFilter implements Filter {

	/** 서버 시각 쿠키의 기본 이름 — 공통컴포넌트와 동일하게 두어 화면 코드를 그대로 쓸 수 있다. */
	public static final String DEFAULT_SERVER_TIME_COOKIE = "egovLatestServerTime";

	/** 만료 시각 쿠키의 기본 이름 — 공통컴포넌트와 동일. */
	public static final String DEFAULT_EXPIRY_TIME_COOKIE = "egovExpireSessionTime";

	private final PathMatcher pathMatcher = new AntPathMatcher();

	private String serverTimeCookieName = DEFAULT_SERVER_TIME_COOKIE;

	private String expiryTimeCookieName = DEFAULT_EXPIRY_TIME_COOKIE;

	private String cookiePath = "/";

	private boolean httpOnly;

	private String sameSite = "Lax";

	private List<String> excludePathPatterns = Collections.emptyList();

	/**
	 * 초기화 파라미터를 읽는다. 지정하지 않은 항목은 기본값을 유지한다.
	 *
	 * @param filterConfig 필터 설정 (null 허용 — 전부 기본값)
	 */
	@Override
	public void init(FilterConfig filterConfig) {
		if (filterConfig == null) {
			return;
		}
		serverTimeCookieName = value(filterConfig, "serverTimeCookieName", serverTimeCookieName);
		expiryTimeCookieName = value(filterConfig, "expiryTimeCookieName", expiryTimeCookieName);
		cookiePath = value(filterConfig, "cookiePath", cookiePath);
		sameSite = value(filterConfig, "sameSite", sameSite);
		httpOnly = Boolean.parseBoolean(value(filterConfig, "httpOnly", String.valueOf(httpOnly)));
		// 이름·경로·SameSite 의 설정 오류는 첫 요청이 아니라 기동 시점에 드러나야 한다 — 빌더 검증을 미리 거친다
		EgovCookies.builder(serverTimeCookieName, "0").path(cookiePath).sameSite(sameSite);
		EgovCookies.builder(expiryTimeCookieName, "0").path(cookiePath).sameSite(sameSite);

		String patterns = filterConfig.getInitParameter("excludePathPatterns");
		if (patterns != null && !patterns.trim().isEmpty()) {
			List<String> parsed = new ArrayList<>();
			for (String pattern : patterns.split(",")) {
				String trimmed = pattern.trim();
				if (!trimmed.isEmpty()) {
					parsed.add(trimmed);
				}
			}
			excludePathPatterns = Collections.unmodifiableList(parsed);
		}
	}

	/**
	 * 세션이 있으면 만료 시각 쿠키를 갱신한 뒤 다음 필터로 넘긴다.
	 *
	 * @param request  요청
	 * @param response 응답
	 * @param chain    필터 체인
	 * @throws IOException      체인 처리 중 IO 오류
	 * @throws ServletException 체인 처리 중 서블릿 오류
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			if (!isExcluded(httpRequest)) {
				// 세션이 없으면 빈 Optional — 쿠키를 발행하지 않고, 세션도 만들지 않는다
				Optional<EgovSessionExpiry> expiry = EgovSessionExpiry.from(httpRequest);
				if (expiry.isPresent()) {
					writeCookies(httpRequest, (HttpServletResponse) response, expiry.get());
				}
			}
		}

		chain.doFilter(request, response);
	}

	/**
	 * 요청 경로가 제외 대상인지 판단한다.
	 *
	 * @param request 요청
	 * @return 제외 대상이면 true
	 */
	protected boolean isExcluded(HttpServletRequest request) {
		if (excludePathPatterns.isEmpty()) {
			return false;
		}
		String path = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
			path = path.substring(contextPath.length());
		}
		for (String pattern : excludePathPatterns) {
			if (pathMatcher.match(pattern, path)) {
				return true;
			}
		}
		return false;
	}

	private void writeCookies(HttpServletRequest request, HttpServletResponse response, EgovSessionExpiry expiry) {
		cookie(serverTimeCookieName, Long.toString(expiry.getServerTime())).addTo(request, response);
		cookie(expiryTimeCookieName, Long.toString(expiry.getExpiryTime())).addTo(request, response);
	}

	/**
	 * 쿠키 조립을 {@link EgovCookies} 에 위임한다(같은 조리법 두 벌 방지). Secure 는 빌더가
	 * {@code request.isSecure()} 로 자동 판정하고, {@code sameSite} 초기화 파라미터가 빈 값이면
	 * 속성을 붙이지 않는 종전 의미를 그대로 유지한다.
	 */
	private EgovCookies.Builder cookie(String name, String value) {
		return EgovCookies.builder(name, value)
				.path(cookiePath)
				.httpOnly(httpOnly)
				.sameSite(sameSite);
	}

	private static String value(FilterConfig config, String name, String defaultValue) {
		String parameter = config.getInitParameter(name);
		return (parameter == null || parameter.trim().isEmpty()) ? defaultValue : parameter.trim();
	}

	/**
	 * 서버 시각 쿠키 이름을 지정한다(Java Config 등록 시).
	 *
	 * @param serverTimeCookieName 쿠키 이름
	 */
	public void setServerTimeCookieName(String serverTimeCookieName) {
		this.serverTimeCookieName = serverTimeCookieName;
	}

	/**
	 * 만료 시각 쿠키 이름을 지정한다(Java Config 등록 시).
	 *
	 * @param expiryTimeCookieName 쿠키 이름
	 */
	public void setExpiryTimeCookieName(String expiryTimeCookieName) {
		this.expiryTimeCookieName = expiryTimeCookieName;
	}

	/**
	 * 쿠키 경로를 지정한다.
	 *
	 * @param cookiePath 경로 (기본 "/")
	 */
	public void setCookiePath(String cookiePath) {
		this.cookiePath = cookiePath;
	}

	/**
	 * {@code HttpOnly} 사용 여부를 지정한다.
	 *
	 * @param httpOnly true 면 스크립트에서 읽을 수 없다(기본 false — 클래스 설명 참조)
	 */
	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}

	/**
	 * {@code SameSite} 속성을 지정한다.
	 *
	 * @param sameSite Lax·Strict·None 등 (기본 "Lax", 빈 값이면 미설정)
	 */
	public void setSameSite(String sameSite) {
		this.sameSite = sameSite;
	}

	/**
	 * 쿠키를 발행하지 않을 경로 패턴을 지정한다(Ant 패턴).
	 *
	 * @param excludePathPatterns 패턴 목록 (null 이면 제외 없음)
	 */
	public void setExcludePathPatterns(List<String> excludePathPatterns) {
		this.excludePathPatterns = (excludePathPatterns == null)
				? Collections.emptyList()
				: Collections.unmodifiableList(new ArrayList<>(excludePathPatterns));
	}

}
