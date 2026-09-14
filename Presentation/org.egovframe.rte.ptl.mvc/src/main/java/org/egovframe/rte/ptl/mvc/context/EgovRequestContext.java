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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 현재 HTTP 요청 정보를 어디서나 꺼낼 수 있게 하는 <b>요청 컨텍스트 파사드</b>.
 *
 * <p>서비스 계층처럼 {@code HttpServletRequest} 를 인자로 받지 않는 자리에서 요청 정보가
 * 필요할 때, {@code RequestContextHolder} 를 직접 다루는 보일러플레이트를 대신한다.</p>
 *
 * <pre>
 * // 직접 다루면 — 매번 반복되고, 컨텍스트가 없을 때의 처리가 제각각이 된다
 * RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
 * if (attrs instanceof ServletRequestAttributes) {
 *     HttpServletRequest request = ((ServletRequestAttributes) attrs).getRequest();
 *     ...
 * }
 *
 * // 파사드
 * EgovRequestContext.getRequestUri().ifPresent(uri -&gt; log.info("요청 {}", uri));
 * </pre>
 *
 * <p><b>요청 컨텍스트가 없을 수 있다.</b> 배치·스케줄러·비동기 스레드에서는 당연히 없고,
 * 웹 요청이라도 별도 스레드로 넘어가면 사라진다. 그래서 조회 메서드는 모두
 * {@link Optional} 을 돌려주며, <b>없는 것은 예외 상황이 아니다</b>.
 * 반드시 있어야 하는 자리라면 {@link #getRequiredRequest()} 를 쓴다.</p>
 *
 * <p><b>가능하면 요청을 인자로 받는 편이 낫다.</b> 이 파사드는 스레드로컬에 의존하므로
 * 비동기 처리에서 값이 사라지고, 테스트에서도 별도 준비가 필요하다.
 * 컨트롤러·필터처럼 요청을 손에 쥘 수 있는 자리에서는 그대로 넘겨 쓰는 것이 명확하다.</p>
 *
 * <p><b>전제</b> — 서블릿 컨테이너에서 {@code RequestContextHolder} 가 채워져 있어야 한다.
 * Spring MVC 의 {@code DispatcherServlet} 이 처리하는 요청은 자동으로 채워지지만,
 * 그 밖의 경로(필터 단계 등)에서도 쓰려면 {@code RequestContextListener} 등록이 필요하다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (A-05: 공통컴포넌트 EgovHttpRequestHelper 의 설계를
 *                            차용하되 예외 기반 판정·세션 강제 생성·프록시 미대응 IP 이름을
 *                            Optional 계약과 정직한 명명으로 재구성 — 코드 이식 아님)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovRequestContext {

	private EgovRequestContext() {
	}

	/**
	 * 현재 스레드에 HTTP 요청 컨텍스트가 있는지 확인한다.
	 *
	 * <p>예외를 던지거나 잡지 않고 판정한다 — 배치·비동기 스레드에서 이 메서드를 호출하는 것은
	 * 정상적인 사용이므로 예외를 흐름 제어에 쓰지 않는다.</p>
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
	 * <p>요청이 반드시 있어야 하는 자리(웹 전용 컴포넌트)에서만 쓴다.</p>
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
	 * 현재 세션을 반환한다 — <b>없으면 만들지 않는다.</b>
	 *
	 * <p>단순 조회 때문에 세션이 생기면 비로그인 방문자에게도 세션이 할당되어 자원이 낭비된다.
	 * 세션이 필요해서 만들어야 한다면 {@link #getOrCreateSession()} 을 명시적으로 쓴다.</p>
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
	 * <b>TCP 연결 상대의 주소</b>를 반환한다({@code getRemoteAddr()}).
	 *
	 * <p><b>이것은 "클라이언트 IP" 가 아닐 수 있다.</b> L4·리버스 프록시·CDN 뒤에 배포하면
	 * 이 값은 전부 <b>프록시의 IP</b> 로 고정된다. 접속 기록·감사 로그에 실제 사용자 IP 가
	 * 필요하다면 {@code X-Forwarded-For} 같은 헤더를 봐야 하는데, 그 헤더는 클라이언트가
	 * 임의로 넣을 수 있으므로 <b>신뢰할 프록시를 정해 두고 그 경우에만 받아들이는</b>
	 * 신뢰 경계 설계가 함께 필요하다.</p>
	 *
	 * <p>메서드 이름을 {@code getClientIp} 로 두지 않은 이유가 그것이다 — 프록시 뒤에서는
	 * 이름이 사실과 달라진다. 신뢰 경계를 갖춘 클라이언트 IP 해석은 별도 과제(C-04)에서 다룬다.</p>
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
	 * 현재 스레드의 {@link ServletRequestAttributes} 를 찾는다.
	 *
	 * <p>{@code RequestContextHolder.currentRequestAttributes()} 는 없을 때 예외를 던지므로,
	 * 예외 없이 판정 가능한 {@code getRequestAttributes()} 를 쓴다. 서블릿 환경이 아닌
	 * {@link RequestAttributes} 구현이 올 수도 있어 타입도 함께 확인한다.</p>
	 */
	private static Optional<ServletRequestAttributes> servletRequestAttributes() {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		return (attributes instanceof ServletRequestAttributes)
				? Optional.of((ServletRequestAttributes) attributes)
				: Optional.empty();
	}

}
