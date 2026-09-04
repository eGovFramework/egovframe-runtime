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
package org.egovframe.rte.fdl.filehandling;

import java.nio.charset.StandardCharsets;

/**
 * 다운로드 응답의 {@code Content-Disposition} 헤더 값을 만든다 — RFC 6266/5987 준수.
 *
 * <p><b>NOTE:</b> 한글 파일명을 {@code filename="한글.hwp"} 로 그대로 실으면 브라우저·프록시의
 * 헤더 해석 규칙(ISO-8859-1 기준)에 따라 파일명이 깨진다. 표준 해법은 RFC 5987
 * {@code filename*=UTF-8''퍼센트인코딩} 확장 파라미터이며, 이를 해석하지 못하는 구형
 * 에이전트를 위해 ASCII 폴백 {@code filename="..."} 을 <b>병기</b>하는 것이 RFC 6266 부록 D의
 * 권고다. 이 클래스는 두 파라미터를 함께 만든다.</p>
 *
 * <pre>
 * response.setHeader("Content-Disposition", EgovContentDispositions.attachment("연차보고서.hwp"));
 * // → attachment; filename="_____.hwp"; filename*=UTF-8''%EC%97%B0%EC%B0%A8...%EC%84%9C.hwp
 *
 * response.setHeader("Content-Disposition", EgovContentDispositions.attachment("report.pdf"));
 * // → attachment; filename="report.pdf"   (ASCII 는 기존 형식 유지)
 * </pre>
 *
 * <p><b>헤더 인젝션 차단.</b> 파일명은 사용자 입력(원본 업로드명)에서 오는 값이므로,
 * CR·LF 등 제어문자는 제거하고 경로 구분자 앞부분은 잘라 <b>파일명만</b> 싣는다.
 * 인용부호·역슬래시는 quoted-string 규칙으로 이스케이프한다.</p>
 *
 * <p>Spring MVC 컨트롤러에서는 {@code org.springframework.http.ContentDisposition} 빌더를 써도
 * 되지만, 그 빌더는 비ASCII 파일명에서 ASCII 폴백을 병기하지 않으며 spring-web 의존을 요구한다.
 * 본 클래스는 서블릿·필터·뷰 등 <b>의존 없는 어느 계층에서나</b> 쓸 수 있다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (RFC 5987 파일명 인코딩을 공용 API 로 제공 —
 *                            구형 에이전트용 ASCII 폴백 병기·제어문자 제거)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovContentDispositions {

	/** RFC 5987 attr-char 중 영숫자 외에 퍼센트 인코딩 없이 쓸 수 있는 문자. */
	private static final String ATTR_CHARS = "!#$&+-.^_`|~";

	private static final char[] HEX = "0123456789ABCDEF".toCharArray();

	private EgovContentDispositions() {
	}

	/**
	 * 첨부 다운로드({@code attachment})용 헤더 값을 만든다.
	 *
	 * @param filename 내려줄 파일명(원본 업로드명 등 신뢰할 수 없는 값 허용)
	 * @return {@code Content-Disposition} 헤더에 실을 값
	 * @throws IllegalArgumentException 파일명이 {@code null}·빈 값이거나 정리 후 남는 문자가 없는 경우
	 */
	public static String attachment(String filename) {
		return build("attachment", filename);
	}

	/**
	 * 브라우저 내 표시({@code inline})용 헤더 값을 만든다.
	 *
	 * @param filename 내려줄 파일명(원본 업로드명 등 신뢰할 수 없는 값 허용)
	 * @return {@code Content-Disposition} 헤더에 실을 값
	 * @throws IllegalArgumentException 파일명이 {@code null}·빈 값이거나 정리 후 남는 문자가 없는 경우
	 */
	public static String inline(String filename) {
		return build("inline", filename);
	}

	private static String build(String disposition, String filename) {
		String name = sanitize(filename);
		StringBuilder header = new StringBuilder(disposition);
		if (isAscii(name)) {
			header.append("; filename=\"").append(escapeQuoted(name)).append('"');
		} else {
			header.append("; filename=\"").append(escapeQuoted(asciiFallback(name))).append('"');
			header.append("; filename*=UTF-8''").append(rfc5987Encode(name));
		}
		return header.toString();
	}

	/**
	 * 파일명만 남기고(경로 구분자 앞 제거) 제어문자를 걷어낸다 — 헤더 인젝션(CR/LF) 차단.
	 */
	private static String sanitize(String filename) {
		if (filename == null || filename.isEmpty()) {
			throw new IllegalArgumentException("filename must not be null or empty");
		}
		int sep = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
		String name = (sep < 0) ? filename : filename.substring(sep + 1);
		StringBuilder cleaned = new StringBuilder(name.length());
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c >= 0x20 && c != 0x7F) {
				cleaned.append(c);
			}
		}
		if (cleaned.length() == 0) {
			throw new IllegalArgumentException("filename has no usable characters: " + filename);
		}
		return cleaned.toString();
	}

	private static boolean isAscii(String value) {
		for (int i = 0; i < value.length(); i++) {
			if (value.charAt(i) > 127) {
				return false;
			}
		}
		return true;
	}

	/** quoted-string 이스케이프 — 역슬래시·인용부호. */
	private static String escapeQuoted(String value) {
		StringBuilder escaped = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '\\' || c == '"') {
				escaped.append('\\');
			}
			escaped.append(c);
		}
		return escaped.toString();
	}

	/** 구형 에이전트용 ASCII 폴백 — 비ASCII 문자를 {@code _} 로 치환한다(확장자는 통상 ASCII 라 보존된다). */
	private static String asciiFallback(String value) {
		StringBuilder fallback = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			fallback.append(c > 127 ? '_' : c);
		}
		return fallback.toString();
	}

	/** RFC 5987 — UTF-8 바이트를 attr-char 외 전부 퍼센트 인코딩한다. */
	private static String rfc5987Encode(String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		StringBuilder encoded = new StringBuilder(bytes.length * 3);
		for (byte b : bytes) {
			int c = b & 0xFF;
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
					|| ATTR_CHARS.indexOf(c) >= 0) {
				encoded.append((char) c);
			} else {
				encoded.append('%').append(HEX[c >>> 4]).append(HEX[c & 0xF]);
			}
		}
		return encoded.toString();
	}

}
