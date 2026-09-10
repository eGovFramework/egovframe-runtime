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
package org.egovframe.rte.ptl.mvc.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * 안전 기본값이 붙는 쿠키 빌더 + 읽기·삭제 헬퍼.
 *
 * <p>Servlet 6 {@link Cookie} 도 Spring {@code ResponseCookie} 도 기본값이 비어 있어(SameSite 없음·
 * HttpOnly false·Secure false) 개발자가 빠뜨리면 그대로 나간다. 이 빌더는 <b>빠뜨릴 수 없는
 * 기본값</b>을 강제하고, 브라우저가 폐기하는 조합과 주입 가능한 값을 <b>생성 시점에 거부</b>한다.</p>
 *
 * <table border="1">
 *   <caption>기본값</caption>
 *   <tr><th>속성</th><th>기본</th><th>비고</th></tr>
 *   <tr><td>HttpOnly</td><td>true</td><td>{@link Builder#httpOnly(boolean)} 로 명시 해제</td></tr>
 *   <tr><td>SameSite</td><td>Lax</td><td>Strict·None 선택 가능, 빈 값이면 미설정</td></tr>
 *   <tr><td>Secure</td><td>요청이 보안 채널이면 자동</td><td>{@link Builder#secure(boolean)} 명시가 우선</td></tr>
 *   <tr><td>Path</td><td>컨텍스트 루트</td><td>요청 경로에 따라 달라지는 Servlet 기본값을 쓰지 않는다</td></tr>
 * </table>
 *
 * <pre>
 * EgovCookies.builder("theme", "dark").maxAge(Duration.ofDays(30)).addTo(request, response);
 * EgovCookies.builder("name", "홍길동").encodeValue().addTo(request, response);   // 비 ASCII 는 인코딩 선택
 * Optional&lt;String&gt; theme = EgovCookies.value(request, "theme");
 * EgovCookies.expire("theme").addTo(request, response);                        // 같은 Path 로 삭제
 * </pre>
 *
 * <p><b>거부 규칙(IAE)</b>: {@code SameSite=None} 인데 Secure 가 아님(브라우저가 폐기) · 이름이 RFC 6265
 * token 이 아님 · 이름·값에 제어 문자·CR/LF·{@code ;} 포함(헤더 주입) · 값이 cookie-octet 밖
 * 문자(공백·{@code "}·{@code ,}·{@code \}·비 ASCII)를 포함하는데 {@link Builder#encodeValue()} 를
 * 선택하지 않음. 원본(공통컴포넌트 {@code EgovSessionCookieUtil})처럼 CRLF 를 조용히 지우는
 * 정제는 하지 않는다 — 잘못된 입력은 실패가 맞다(설계 원칙 4).</p>
 *
 * <p><b>Secure 자동 판정과 프록시</b>: HTTPS 종단이 리버스 프록시에 있으면 컨테이너가 보는
 * {@code request.isSecure()} 는 false 다. 표준 경로는 forward 헤더 신뢰 설정(Boot
 * {@code server.forward-headers-strategy}, Tomcat {@code RemoteIpValve})이고, 그것이 없으면
 * {@code secure(true)} 를 명시한다. 원본처럼 무조건 true 로 두면 HTTP 개발환경에서 쿠키가
 * 저장되지 않는 침묵 미동작이 생기므로 자동이 기본이다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.02  실행환경팀     최초 생성 (안전 기본값 쿠키 빌더 — 원본 EgovSessionCookieUtil 은
 *                            SameSite 전무·Secure 하드코딩·CRLF 정제 편측·path 미설정·부재 이원화
 *                            결함에 소비처 0건이라 계약(set·get·delete)만 참고, 코드 이식 없음)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovCookies {

	/** SameSite=Lax — 기본값. 외부 링크 진입(top-level GET)에는 쿠키가 실리고 교차 사이트 POST 에는 실리지 않는다. */
	public static final String SAME_SITE_LAX = "Lax";

	/** SameSite=Strict — 외부 링크 진입에도 쿠키가 실리지 않는다(로그인 유지가 끊기는 흔한 함정). */
	public static final String SAME_SITE_STRICT = "Strict";

	/** SameSite=None — 교차 사이트 허용. 반드시 Secure 여야 한다(아니면 생성 거부). */
	public static final String SAME_SITE_NONE = "None";

	private static final String ATTR_SAME_SITE = "SameSite";
	private static final String ATTR_PARTITIONED = "Partitioned";

	private EgovCookies() {
	}

	/**
	 * 발급 빌더를 연다.
	 *
	 * @param name  쿠키 이름(RFC 6265 token)
	 * @param value 쿠키 값(cookie-octet — 비 ASCII·공백 등은 {@link Builder#encodeValue()} 필요)
	 * @throws IllegalArgumentException 이름이 token 이 아니거나 값에 제어 문자·CR/LF·{@code ;} 가 있는 경우
	 */
	public static Builder builder(String name, String value) {
		return new Builder(name, value, false);
	}

	/**
	 * 삭제 빌더를 연다 — 빈 값·{@code Max-Age=0}. 발급 때와 <b>같은 Path·Domain</b> 을 지정해야
	 * 브라우저가 같은 쿠키로 본다(기본값끼리는 자동 정합).
	 *
	 * @param name 삭제할 쿠키 이름
	 */
	public static Builder expire(String name) {
		return new Builder(name, "", true);
	}

	/**
	 * 요청에서 쿠키 값을 읽는다. 쿠키가 전혀 없거나 이름이 없으면 <b>둘 다</b> 빈 Optional —
	 * 원본의 {@code ""}/{@code null} 이원화를 단일 표현으로 교정.
	 */
	public static Optional<String> value(HttpServletRequest request, String name) {
		Objects.requireNonNull(request, "request must not be null");
		Objects.requireNonNull(name, "name must not be null");
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				return Optional.ofNullable(cookie.getValue());
			}
		}
		return Optional.empty();
	}

	/**
	 * {@link Builder#encodeValue()} 로 발급한 쿠키의 값을 URL 디코딩해 읽는다(발급의 대칭).
	 * 쿠키 값은 클라이언트가 임의로 바꿀 수 있으므로, 디코딩할 수 없는 값(깨진 퍼센트 인코딩)은
	 * 예외 대신 <b>빈 Optional</b> 로 다룬다 — 이 헬퍼가 발급한 값은 항상 디코딩된다.
	 */
	public static Optional<String> decodedValue(HttpServletRequest request, String name) {
		return value(request, name).flatMap(EgovCookies::decodeOrEmpty);
	}

	private static Optional<String> decodeOrEmpty(String encoded) {
		try {
			return Optional.of(URLDecoder.decode(encoded, StandardCharsets.UTF_8));
		} catch (IllegalArgumentException malformed) {
			return Optional.empty();
		}
	}

	/**
	 * {@link EgovCookies} 빌더 — 미지정 속성은 안전 기본값.
	 */
	public static final class Builder {

		private final String name;
		private final String value;
		private final boolean expiring;
		private boolean httpOnly = true;
		private String sameSite = SAME_SITE_LAX;
		private Boolean secure;
		private String path;
		private String domain;
		private int maxAge = -1;
		private boolean partitioned;
		private boolean encodeValue;

		private Builder(String name, String value, boolean expiring) {
			this.name = validName(name);
			this.value = validRawValue(value);
			this.expiring = expiring;
			if (expiring) {
				this.maxAge = 0;
			}
		}

		/** HttpOnly 여부(기본 true). 화면 스크립트가 읽어야 하는 쿠키만 false 로 명시한다. */
		public Builder httpOnly(boolean httpOnly) {
			this.httpOnly = httpOnly;
			return this;
		}

		/**
		 * SameSite — {@code Lax}·{@code Strict}·{@code None}(대소문자 무시, 표준 표기로 정규화).
		 * null 이나 빈 값이면 속성을 붙이지 않는다(명시 해제).
		 *
		 * @throws IllegalArgumentException 세 값 외의 문자열
		 */
		public Builder sameSite(String sameSite) {
			if (sameSite == null || sameSite.trim().isEmpty()) {
				this.sameSite = null;
				return this;
			}
			String canonical = canonicalSameSite(sameSite.trim());
			if (canonical == null) {
				throw new IllegalArgumentException("sameSite must be Lax, Strict or None: " + sameSite);
			}
			this.sameSite = canonical;
			return this;
		}

		/** Secure 명시 — 지정하면 요청 채널 자동 판정보다 우선한다. */
		public Builder secure(boolean secure) {
			this.secure = secure;
			return this;
		}

		/** Path(기본 컨텍스트 루트). 삭제 시 발급과 같은 값을 써야 한다. */
		public Builder path(String path) {
			this.path = validAttribute("path", path);
			return this;
		}

		/** Domain(기본 미설정 = 발급 호스트). */
		public Builder domain(String domain) {
			this.domain = validAttribute("domain", domain);
			return this;
		}

		/** Max-Age(초). -1 이면 세션 쿠키(기본). 삭제 빌더는 0 고정. */
		public Builder maxAge(int seconds) {
			if (expiring) {
				throw new IllegalStateException("expire() cookie has Max-Age fixed to 0");
			}
			if (seconds < -1) {
				throw new IllegalArgumentException("maxAge must be >= -1: " + seconds);
			}
			this.maxAge = seconds;
			return this;
		}

		/** Max-Age 를 기간으로 지정한다(초 단위로 내림, 최대 int 범위). */
		public Builder maxAge(Duration duration) {
			Objects.requireNonNull(duration, "duration must not be null");
			long seconds = duration.getSeconds();
			if (seconds > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("maxAge too large: " + duration);
			}
			return maxAge((int) seconds);
		}

		/** Partitioned(CHIPS) 속성을 붙인다. Secure 가 전제라 None 과 같은 규칙으로 검증된다. */
		public Builder partitioned() {
			this.partitioned = true;
			return this;
		}

		/**
		 * 값을 URL 인코딩해 발급한다(UTF-8). 비 ASCII·공백·따옴표 등 cookie-octet 밖 문자가 있을 때
		 * 선택한다. 읽을 때는 {@link EgovCookies#decodedValue(HttpServletRequest, String)}.
		 */
		public Builder encodeValue() {
			this.encodeValue = true;
			return this;
		}

		/**
		 * 쿠키를 만든다. Secure 미지정 시 {@code request.isSecure()} 로, Path 미지정 시 컨텍스트 루트로
		 * 채운다.
		 *
		 * @param request 발급 요청(자동 판정용 — null 이면 Secure=false·Path="/")
		 * @throws IllegalArgumentException None/Partitioned 인데 Secure 가 아님, 값이 cookie-octet 밖인데
		 *                                  인코딩 미선택
		 */
		public Cookie build(HttpServletRequest request) {
			String cookieValue = encodeValue
					? URLEncoder.encode(value, StandardCharsets.UTF_8)
					: requireCookieOctets(value);
			boolean effectiveSecure = (secure != null) ? secure : (request != null && request.isSecure());
			if (!effectiveSecure && SAME_SITE_NONE.equals(sameSite)) {
				throw new IllegalArgumentException("SameSite=None requires Secure — browsers reject it otherwise"
						+ " (cookie '" + name + "')");
			}
			if (!effectiveSecure && partitioned) {
				throw new IllegalArgumentException("Partitioned requires Secure (cookie '" + name + "')");
			}
			Cookie cookie = new Cookie(name, cookieValue);
			cookie.setHttpOnly(httpOnly);
			cookie.setSecure(effectiveSecure);
			cookie.setPath((path != null) ? path : contextRoot(request));
			if (domain != null) {
				cookie.setDomain(domain);
			}
			cookie.setMaxAge(maxAge);
			if (sameSite != null) {
				cookie.setAttribute(ATTR_SAME_SITE, sameSite);
			}
			if (partitioned) {
				cookie.setAttribute(ATTR_PARTITIONED, "");
			}
			return cookie;
		}

		/** {@link #build(HttpServletRequest)} 후 응답에 추가한다. */
		public Cookie addTo(HttpServletRequest request, HttpServletResponse response) {
			Objects.requireNonNull(response, "response must not be null");
			Cookie cookie = build(request);
			response.addCookie(cookie);
			return cookie;
		}

		private static String contextRoot(HttpServletRequest request) {
			if (request == null) {
				return "/";
			}
			String contextPath = request.getContextPath();
			return (contextPath == null || contextPath.isEmpty()) ? "/" : contextPath;
		}

		private static String canonicalSameSite(String value) {
			switch (value.toLowerCase(Locale.ROOT)) {
				case "lax":
					return SAME_SITE_LAX;
				case "strict":
					return SAME_SITE_STRICT;
				case "none":
					return SAME_SITE_NONE;
				default:
					return null;
			}
		}

	}

	// ---------------------------------------------------------------- 검증

	/** RFC 6265 token — 제어 문자·구분자(`()<>@,;:\"/[]?={} 공백 탭`) 제외. */
	private static String validName(String name) {
		Objects.requireNonNull(name, "name must not be null");
		if (name.isEmpty()) {
			throw new IllegalArgumentException("cookie name must not be empty");
		}
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c <= 0x20 || c >= 0x7F || "()<>@,;:\\\"/[]?={}".indexOf(c) >= 0) {
				throw new IllegalArgumentException("cookie name is not an RFC 6265 token: '" + name + "'");
			}
		}
		return name;
	}

	/** 인코딩 여부와 무관하게 절대 허용하지 않는 문자 — 제어 문자·CR/LF·{@code ;}. */
	private static String validRawValue(String value) {
		Objects.requireNonNull(value, "value must not be null");
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c < 0x20 || c == 0x7F || c == ';') {
				throw new IllegalArgumentException("cookie value contains a control character or ';' (header injection)");
			}
		}
		return value;
	}

	/** RFC 6265 cookie-octet: %x21 / %x23-2B / %x2D-3A / %x3C-5B / %x5D-7E. */
	private static String requireCookieOctets(String value) {
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			boolean ok = c == 0x21 || (c >= 0x23 && c <= 0x2B) || (c >= 0x2D && c <= 0x3A)
					|| (c >= 0x3C && c <= 0x5B) || (c >= 0x5D && c <= 0x7E);
			if (!ok) {
				throw new IllegalArgumentException("cookie value contains a character outside RFC 6265 cookie-octet"
						+ " (space, quote, comma, backslash or non-ASCII) — call encodeValue() to URL-encode it");
			}
		}
		return value;
	}

	private static String validAttribute(String attribute, String value) {
		Objects.requireNonNull(value, attribute + " must not be null");
		if (value.trim().isEmpty()) {
			throw new IllegalArgumentException(attribute + " must not be blank");
		}
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c < 0x20 || c == 0x7F || c == ';') {
				throw new IllegalArgumentException(attribute + " contains a control character or ';'");
			}
		}
		return value.trim();
	}

}
