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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.egovframe.rte.ptl.mvc.cookie.EgovCookies;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 세션 만료 시각 쿠키({@value #DEFAULT_SERVER_TIME_COOKIE}, {@value #DEFAULT_EXPIRY_TIME_COOKIE})를 매 요청 갱신하는 필터.
 * 세션이 없는 요청에는 쿠키를 발행하지 않으며 세션도 만들지 않는다.
 * 화면 스크립트가 읽어야 하므로 httpOnly 기본값은 false이다.
 *
 * <p>초기화 파라미터: {@code serverTimeCookieName}, {@code expiryTimeCookieName},
 * {@code cookiePath}, {@code httpOnly}, {@code sameSite}, {@code excludePathPatterns}(Ant 패턴, 쉼표 구분)</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성
 *  2026.09.02  실행환경팀     쿠키 조립을 EgovCookies 빌더에 위임
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 4.3.1
 * @see EgovSessionExpiry
 */
public class EgovSessionExpiryCookieFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovSessionExpiryCookieFilter.class);

	/** 서버 시각 쿠키의 기본 이름(공통컴포넌트와 동일). */
	public static final String DEFAULT_SERVER_TIME_COOKIE = "egovLatestServerTime";

	/** 만료 시각 쿠키의 기본 이름(공통컴포넌트와 동일). */
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
	 * @param filterConfig 필터 설정 (null이면 모두 기본값)
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
		// 설정 오류를 기동 시점에 드러내기 위해 빌더 검증을 미리 거친다
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
				// 세션이 없으면 쿠키를 발행하지 않는다
				Optional<EgovSessionExpiry> expiry = EgovSessionExpiry.from(httpRequest);
				if (expiry.isPresent()) {
					try {
						writeCookies(httpRequest, (HttpServletResponse) response, expiry.get());
					} catch (IllegalArgumentException invalidCookieAttributes) {
						// 부가 기능이므로 쿠키 발급이 거부돼도 본 요청 처리는 계속한다
						LOGGER.warn("세션 만료 쿠키 발행 실패 — 요청은 계속 진행: {}", invalidCookieAttributes.getMessage());
					}
				}
			}
		}

		chain.doFilter(request, response);
	}

	/**
	 * 해제할 자원이 없다(Servlet 3.1 호환을 위해 명시 구현).
	 */
	@Override
	public void destroy() {
		// 해제할 자원 없음
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
	 * {@link EgovCookies} 빌더로 쿠키를 조립한다. sameSite가 빈 값이면 속성을 붙이지 않는다.
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
	 * @param httpOnly true면 스크립트에서 읽을 수 없다(기본 false)
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
