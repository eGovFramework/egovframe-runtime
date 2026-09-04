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
package org.egovframe.rte.ptl.mvc.filter;

import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

/**
 * HTML <b>출력 정제(Sanitizer)</b> 유틸 — 신뢰할 수 없는 HTML 에서 XSS 위험 요소만
 * 선별 제거하고 안전한 서식은 남긴다.
 *
 * <p><b>입력 필터({@link HTMLTagFilter})와의 역할 구분:</b> 입력 필터는 요청 파라미터의
 * {@code <} 를 {@code &lt;} 로 전부 인코딩하므로 HTML 서식 자체를 쓸 수 없게 된다.
 * 리치 텍스트 에디터(CKEditor 등)로 작성한 게시물 본문처럼 <b>HTML 을 허용해야 하는
 * 콘텐츠</b>는 입력 인코딩 대신 이 정제기로 위험 요소만 걸러 출력한다.
 * 두 수단은 상호 보완 관계다 — 서식이 필요 없는 필드는 입력 필터(또는
 * {@link EgovHtmlSanitizePolicy#TEXT_ONLY}), 서식이 필요한 본문은 이 정제기.</p>
 *
 * <p><b>동작 방식:</b> jsoup 의 Safelist(화이트리스트) 정제를 사용한다. 허용 목록에 없는
 * 태그·속성·프로토콜은 전부 제거되므로 {@code <script>}, 이벤트 핸들러({@code onclick} 등),
 * {@code javascript:} URL, 변형 공격({@code <scr<script>ipt>} 류)이 모두 무력화된다.
 * 정책별 허용 범위는 {@link EgovHtmlSanitizePolicy} 참조.</p>
 *
 * <p><b>의존성:</b> 이 클래스는 <b>jsoup 을 필요로 한다</b>(모듈 pom 에 optional 선언 —
 * 의존성 격리 원칙). 사용하는 애플리케이션은 {@code org.jsoup:jsoup} 을 직접 추가해야
 * 하며, 없으면 클래스 로딩 시 {@code NoClassDefFoundError} 가 발생한다.
 * ptl.mvc 의 다른 기능은 jsoup 없이 동작한다.</p>
 *
 * <p><b>사용 시점:</b> 저장 시 1회 정제(저장 데이터 자체를 안전하게) 또는 출력 직전 정제
 * 중 택일한다. 저장 시 정제가 일반적으로 우수하다 — 조회마다의 파싱 비용이 없고,
 * API 등 다른 출력 경로도 함께 보호된다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.08.28  실행환경팀     최초 생성 (A-01: 공통컴포넌트 EgovHtmlSanitizer 의 검증된
 *                            설계(상대경로 보존·style 재필터)를 차용하되, 단일 고정 정책을
 *                            용도별 정책 선택형으로 재구성 — 코드 직접 이식 아님)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @see EgovHtmlSanitizePolicy
 * @see HTMLTagFilter
 */
public final class EgovHtmlSanitizer {

	/**
	 * style 속성에서 허용하는 CSS 선언 — width/height 의 px/% 값만.
	 * 그 외 선언(position, expression, url() 등)은 클릭재킹·레거시 IE 스크립트 실행 등의
	 * 위험이 있어 전부 제거한다.
	 */
	private static final Pattern SAFE_STYLE_DECLARATION =
			Pattern.compile("^(width|height)\\s*:\\s*\\d{1,4}(px|%)?$", Pattern.CASE_INSENSITIVE);

	/**
	 * jsoup 의 프로토콜 검증은 {@code Element.absUrl()} 로 절대경로 변환이 가능해야
	 * 동작하므로, {@code /utl/web/imageSrc.do?...} 같은 컨텍스트 상대경로도 검증을
	 * 통과하도록 임의의 baseUri 를 부여한다. 정책의 {@code preserveRelativeLinks} 설정으로
	 * 실제 출력값은 절대경로로 바뀌지 않고 원본 상대경로가 그대로 유지된다.
	 */
	private static final String DUMMY_BASE_URI = "http://localhost/";

	private EgovHtmlSanitizer() {
	}

	/**
	 * 기본 정책({@link EgovHtmlSanitizePolicy#RICH_TEXT})으로 HTML 을 정제한다.
	 * 리치 텍스트 에디터로 저장한 게시물 본문을 조회 화면에 출력할 때 사용한다.
	 *
	 * @param html 신뢰할 수 없는(사용자가 입력한) 원본 HTML — null/공백 허용
	 * @return 위험 요소가 제거된 안전한 HTML (입력이 null 또는 공백이면 빈 문자열)
	 */
	public static String sanitize(String html) {
		return sanitize(html, EgovHtmlSanitizePolicy.RICH_TEXT);
	}

	/**
	 * 지정 정책으로 HTML 을 정제한다.
	 *
	 * @param html   신뢰할 수 없는 원본 HTML — null/공백 허용
	 * @param policy 허용 범위 정책 (null 이면 {@link EgovHtmlSanitizePolicy#RICH_TEXT})
	 * @return 위험 요소가 제거된 안전한 HTML (입력이 null 또는 공백이면 빈 문자열)
	 */
	public static String sanitize(String html, EgovHtmlSanitizePolicy policy) {
		EgovHtmlSanitizePolicy effective = (policy != null) ? policy : EgovHtmlSanitizePolicy.RICH_TEXT;
		return sanitize(html, effective.newSafelist());
	}

	/**
	 * 사용자 정의 Safelist 로 HTML 을 정제한다 — 표준 정책으로 부족할 때의 확장점.
	 *
	 * <p>커스텀 Safelist 를 쓰더라도 style 속성 재필터(width/height 만 허용)는
	 * 동일하게 적용된다. Safelist 는 style 속성값(CSS) 자체의 위험성을 검증하지
	 * 않기 때문이다.</p>
	 *
	 * @param safelist jsoup 허용 목록 (null 불가)
	 * @param html     신뢰할 수 없는 원본 HTML — null/공백 허용
	 * @return 위험 요소가 제거된 안전한 HTML (입력이 null 또는 공백이면 빈 문자열)
	 * @throws IllegalArgumentException safelist 가 null 인 경우
	 */
	public static String sanitize(String html, Safelist safelist) {
		if (safelist == null) {
			throw new IllegalArgumentException("safelist must not be null");
		}
		if (html == null || html.trim().isEmpty()) {
			return "";
		}
		String cleaned = Jsoup.clean(html, DUMMY_BASE_URI, safelist);
		return sanitizeStyleAttributes(cleaned);
	}

	/**
	 * 태그를 전부 제거하고 텍스트만 남긴다 —
	 * {@code sanitize(html, TEXT_ONLY)} 의 편의 메서드.
	 * 댓글·제목·검색어처럼 HTML 서식이 필요 없는 값에 사용한다.
	 *
	 * @param html 신뢰할 수 없는 원본 HTML — null/공백 허용
	 * @return 태그가 제거된 텍스트(HTML 조각으로 안전하도록 엔티티 이스케이프됨).
	 *         입력이 null 또는 공백이면 빈 문자열
	 */
	public static String stripToText(String html) {
		return sanitize(html, EgovHtmlSanitizePolicy.TEXT_ONLY);
	}

	/**
	 * Safelist 는 style 속성값(CSS) 자체의 위험성은 검증하지 않으므로,
	 * 1차 정제 결과물에서 style 속성을 width/height 선언만 남도록 다시 필터링한다.
	 */
	private static String sanitizeStyleAttributes(String cleanedHtml) {
		// 정제 결과에 style 속성이 없으면 재파싱을 생략한다 (jsoup 출력은 style="..." 형태)
		if (!cleanedHtml.contains("style=")) {
			return cleanedHtml;
		}

		Document doc = Jsoup.parseBodyFragment(cleanedHtml);
		doc.outputSettings().prettyPrint(false);

		for (Element element : doc.select("[style]")) {
			String safeStyle = filterStyle(element.attr("style"));
			if (safeStyle == null) {
				element.removeAttr("style");
			} else {
				element.attr("style", safeStyle);
			}
		}

		return doc.body().html();
	}

	/**
	 * style 속성값에서 허용 선언(width/height)만 남긴다.
	 *
	 * @return 허용 선언만 남긴 값, 남는 것이 없으면 null
	 */
	private static String filterStyle(String style) {
		if (style == null || style.trim().isEmpty()) {
			return null;
		}

		StringBuilder safeStyle = new StringBuilder();
		for (String declaration : style.split(";")) {
			String trimmed = declaration.trim();
			if (SAFE_STYLE_DECLARATION.matcher(trimmed).matches()) {
				if (safeStyle.length() > 0) {
					safeStyle.append("; ");
				}
				safeStyle.append(trimmed);
			}
		}

		return safeStyle.length() > 0 ? safeStyle.toString() : null;
	}

}
