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
package org.egovframe.rte.ptl.mvc.context;

import java.util.Optional;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * RequestContextHolder 기반으로 현재 HTTP 요청 정보를 조회하는 요청 컨텍스트 파사드.
 * 요청 컨텍스트가 없으면(배치·비동기 스레드 등) 빈 Optional을 반환한다.
 * DispatcherServlet 밖(필터 단계 등)에서 쓰려면 RequestContextListener 등록이 필요하다.
 *
 * <pre>
 * EgovRequestContext.getRequestUri().ifPresent(uri -&gt; log.info("요청 {}", uri));
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
 */
public final class EgovRequestContext {

	private EgovRequestContext() {
	}

	/**
	 * 현재 스레드에 HTTP 요청 컨텍스트가 있는지 확인한다.
	 *
	 * @return 요청 컨텍스트가 있으면 true
	 */
	public static boolean isPresent() {
		return servletRequestAttributes().isPresent();
	}

	/**
	 * 현재 요청을 반환한다.
	 *
	 * @return 현재 요청. 요청 컨텍스트가 없으면 빈 Optional
	 */
	public static Optional<HttpServletRequest> getRequest() {
		return servletRequestAttributes().map(ServletRequestAttributes::getRequest);
	}

	/**
	 * 현재 요청을 반환하되, 없으면 예외를 던진다.
	 *
	 * @return 현재 요청
	 * @throws IllegalStateException 요청 컨텍스트가 없는 경우
	 */
	public static HttpServletRequest getRequiredRequest() {
		return getRequest().orElseThrow(() -> new IllegalStateException(
				"현재 스레드에 HTTP 요청 컨텍스트가 없습니다. "
						+ "배치·비동기 스레드이거나 RequestContextListener 가 등록되지 않았을 수 있습니다."));
	}

	/**
	 * 현재 응답을 반환한다.
	 *
	 * @return 현재 응답. 컨텍스트가 없거나 응답이 노출되지 않는 경우 빈 Optional
	 */
	public static Optional<HttpServletResponse> getResponse() {
		return servletRequestAttributes().map(ServletRequestAttributes::getResponse);
	}

	/**
	 * 현재 세션을 반환한다. 세션이 없으면 만들지 않는다.
	 *
	 * @return 현재 세션. 요청 컨텍스트나 세션이 없으면 빈 Optional
	 */
	public static Optional<HttpSession> getSession() {
		return getRequest().map(request -> request.getSession(false));
	}

	/**
	 * 현재 세션을 반환하되, 없으면 새로 만든다.
	 *
	 * @return 현재 세션(필요하면 신규 생성)
	 * @throws IllegalStateException 요청 컨텍스트가 없는 경우
	 */
	public static HttpSession getOrCreateSession() {
		return getRequiredRequest().getSession();
	}

	/**
	 * 요청 URI 를 반환한다(컨텍스트 경로 포함).
	 *
	 * @return 요청 URI. 요청 컨텍스트가 없으면 빈 Optional
	 */
	public static Optional<String> getRequestUri() {
		return getRequest().map(HttpServletRequest::getRequestURI);
	}

	/**
	 * HTTP 메서드를 반환한다.
	 *
	 * @return GET·POST 등. 요청 컨텍스트가 없으면 빈 Optional
	 */
	public static Optional<String> getMethod() {
		return getRequest().map(HttpServletRequest::getMethod);
	}

	/**
	 * TCP 연결 상대의 주소({@code getRemoteAddr()})를 반환한다.
	 * 프록시 뒤에서는 프록시 IP이며, 전달 헤더(X-Forwarded-For 등)는 해석하지 않는다.
	 *
	 * @return 원격 주소. 요청 컨텍스트가 없으면 빈 Optional
	 */
	public static Optional<String> getRemoteAddress() {
		return getRequest().map(HttpServletRequest::getRemoteAddr);
	}

	/**
	 * 요청 헤더 값을 반환한다.
	 *
	 * @param name 헤더 이름 (null 이면 빈 Optional)
	 * @return 헤더 값. 요청 컨텍스트가 없거나 해당 헤더가 없으면 빈 Optional
	 */
	public static Optional<String> getHeader(String name) {
		if (name == null) {
			return Optional.empty();
		}
		return getRequest().map(request -> request.getHeader(name));
	}

	/**
	 * 요청 파라미터 값을 반환한다.
	 *
	 * @param name 파라미터 이름 (null 이면 빈 Optional)
	 * @return 파라미터 값. 요청 컨텍스트가 없거나 해당 파라미터가 없으면 빈 Optional
	 */
	public static Optional<String> getParameter(String name) {
		if (name == null) {
			return Optional.empty();
		}
		return getRequest().map(request -> request.getParameter(name));
	}

	/**
	 * 현재 스레드의 {@link ServletRequestAttributes}를 예외 없이 조회한다.
	 */
	private static Optional<ServletRequestAttributes> servletRequestAttributes() {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		return (attributes instanceof ServletRequestAttributes)
				? Optional.of((ServletRequestAttributes) attributes)
				: Optional.empty();
	}

}
