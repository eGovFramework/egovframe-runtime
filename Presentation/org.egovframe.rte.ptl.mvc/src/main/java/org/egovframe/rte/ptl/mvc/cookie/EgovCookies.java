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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * 안전 기본값(HttpOnly, SameSite=Lax, 컨텍스트 루트 Path, 보안 채널이면 Secure)을 적용하는 쿠키 빌더 및 읽기·삭제 헬퍼.
 * SameSite=None+비 Secure 조합과 제어 문자·CR/LF·{@code ;} 포함 입력은 생성 시점에 거부한다(헤더 주입 방지).
 *
 * <pre>
 * EgovCookies.builder("theme", "dark").maxAge(Duration.ofDays(30)).addTo(request, response);
 * Optional&lt;String&gt; theme = EgovCookies.value(request, "theme");
 * EgovCookies.expire("theme").addTo(request, response);
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.02  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 4.3.1
 */
public final class EgovCookies {

	/** SameSite=Lax (기본값). */
	public static final String SAME_SITE_LAX = "Lax";

	/** SameSite=Strict. */
	public static final String SAME_SITE_STRICT = "Strict";

	/** SameSite=None (Secure 필수). */
	public static final String SAME_SITE_NONE = "None";

	private static final String ATTR_PARTITIONED = "Partitioned";

	private EgovCookies() {
	}

	/**
	 * 발급 빌더를 연다.
	 *
	 * @param name  쿠키 이름(RFC 6265 token)
	 * @param value 쿠키 값(비 ASCII·공백 등은 {@link Builder#encodeValue()} 필요)
	 * @throws IllegalArgumentException 이름이 token 이 아니거나 값에 제어 문자·CR/LF·{@code ;} 가 있는 경우
	 */
	public static Builder builder(String name, String value) {
		return new Builder(name, value, false);
	}

	/**
	 * 삭제 빌더(빈 값, {@code Max-Age=0})를 연다. 발급 때와 같은 Path·Domain을 지정해야 한다.
	 *
	 * @param name 삭제할 쿠키 이름
	 */
	public static Builder expire(String name) {
		return new Builder(name, "", true);
	}

	/**
	 * 요청에서 쿠키 값을 읽는다. 쿠키가 없으면 빈 Optional을 반환한다.
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
	 * {@link Builder#encodeValue()}로 발급한 쿠키 값을 URL 디코딩해 읽는다. 디코딩할 수 없으면 빈 Optional.
	 */
	public static Optional<String> decodedValue(HttpServletRequest request, String name) {
		return value(request, name).flatMap(EgovCookies::decodeOrEmpty);
	}

	private static Optional<String> decodeOrEmpty(String encoded) {
		try {
			return Optional.of(URLDecoder.decode(encoded, StandardCharsets.UTF_8.name()));
		} catch (IllegalArgumentException | UnsupportedEncodingException malformed) {
			return Optional.empty();
		}
	}

	/** {@link EgovCookies} 빌더. 미지정 속성은 안전 기본값을 쓴다. */
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
		 * SameSite({@code Lax}·{@code Strict}·{@code None}, 대소문자 무시)를 지정한다. null·빈 값이면 속성을 붙이지 않는다.
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

		/** Secure 여부를 지정한다(요청 채널 자동 판정보다 우선). */
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
		 * 값을 UTF-8로 URL 인코딩해 발급한다. 읽을 때는 {@link EgovCookies#decodedValue(HttpServletRequest, String)}를 쓴다.
		 */
		public Builder encodeValue() {
			this.encodeValue = true;
			return this;
		}

		/**
		 * 쿠키를 만든다. Secure 미지정 시 {@code request.isSecure()}, Path 미지정 시 컨텍스트 루트를 쓴다.
		 *
		 * @param request 발급 요청(null이면 Secure=false, Path="/")
		 * @throws IllegalArgumentException None/Partitioned인데 Secure가 아니거나, cookie-octet 밖 값을 인코딩하지 않은 경우
		 */
		public ResponseCookie build(HttpServletRequest request) {
			String cookieValue = encodeValue
					? urlEncode(value)
					: requireCookieOctets(value);
			boolean effectiveSecure = (secure != null) ? secure : (request != null && request.isSecure());
			if (!effectiveSecure && SAME_SITE_NONE.equals(sameSite)) {
				throw new IllegalArgumentException("SameSite=None requires Secure — browsers reject it otherwise"
						+ " (cookie '" + name + "')");
			}
			if (!effectiveSecure && partitioned) {
				throw new IllegalArgumentException("Partitioned requires Secure (cookie '" + name + "')");
			}
			ResponseCookie.ResponseCookieBuilder cookie = ResponseCookie.from(name, cookieValue)
					.httpOnly(httpOnly)
					.secure(effectiveSecure)
					.path((path != null) ? path : contextRoot(request))
					.maxAge(maxAge);
			if (domain != null) {
				cookie.domain(domain);
			}
			if (sameSite != null) {
				cookie.sameSite(sameSite);
			}
			return cookie.build();
		}

		/**
		 * {@code Set-Cookie} 헤더 값을 만든다(Partitioned 속성 포함).
		 *
		 * @param request 발급 요청(자동 판정용)
		 */
		public String toHeaderValue(HttpServletRequest request) {
			String header = build(request).toString();
			return partitioned ? header + "; " + ATTR_PARTITIONED : header;
		}

		/** {@link #build(HttpServletRequest)} 후 응답에 {@code Set-Cookie} 헤더로 추가한다. */
		public ResponseCookie addTo(HttpServletRequest request, HttpServletResponse response) {
			Objects.requireNonNull(response, "response must not be null");
			ResponseCookie cookie = build(request);
			String header = cookie.toString();
			response.addHeader(HttpHeaders.SET_COOKIE, partitioned ? header + "; " + ATTR_PARTITIONED : header);
			return cookie;
		}

		private static String urlEncode(String raw) {
			try {
				return URLEncoder.encode(raw, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
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

	/** 이름이 RFC 6265 token인지 검증한다. */
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

	/** 제어 문자·CR/LF·{@code ;}가 포함된 값을 거부한다. */
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
