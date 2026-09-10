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

import org.jsoup.safety.Safelist;

/**
 * {@link EgovHtmlSanitizer} 가 사용하는 HTML 출력 정제 정책.
 *
 * <p>정책은 "어떤 태그·속성까지 안전한 것으로 보고 남길 것인가"의 허용 목록(Safelist)이다.
 * 목록에 없는 것은 전부 제거되는 <b>화이트리스트 방식</b>이므로, 새로운 공격 패턴을
 * 일일이 차단 목록에 추가할 필요가 없다.</p>
 *
 * <p>어떤 정책이든 {@code <script>}, 이벤트 핸들러 속성({@code onclick} 등),
 * {@code javascript:} 프로토콜 URL 은 허용 목록에 없으므로 항상 제거된다.
 * 정책 간 차이는 "정상적인 서식을 얼마나 남기는가"뿐이다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.08.28  실행환경팀     최초 생성 (A-01: 공통컴포넌트 EgovHtmlSanitizer 설계 차용,
 *                            단일 고정 정책을 용도별 선택형으로 재구성)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @see EgovHtmlSanitizer
 */
public enum EgovHtmlSanitizePolicy {

	/**
	 * 태그를 전부 제거하고 텍스트만 남긴다.
	 * 댓글·제목·검색어처럼 HTML 서식이 필요 없는 필드에 사용한다.
	 * (남는 텍스트는 HTML 조각으로서 안전하도록 엔티티 이스케이프된 상태다)
	 */
	TEXT_ONLY,

	/**
	 * 단순 강조 태그만 허용: {@code b, em, i, strong, u}.
	 * 링크·이미지·블록 요소는 제거된다.
	 */
	SIMPLE_TEXT,

	/**
	 * 기본 서식 허용: 문단·목록·링크 등
	 * ({@code a, b, blockquote, br, cite, code, dd, dl, dt, em, i, li, ol, p,
	 * pre, q, small, span, strike, strong, sub, sup, u, ul}).
	 * 이미지는 제거된다. 링크에는 {@code rel="nofollow"} 가 부여된다.
	 */
	BASIC,

	/**
	 * {@link #BASIC} + 이미지({@code img} 의 src·크기 속성) 허용.
	 */
	BASIC_WITH_IMAGES,

	/**
	 * 리치 텍스트 에디터(CKEditor 등) 콘텐츠용 — <b>기본 정책</b>.
	 * 표({@code table} 계열)·제목({@code h1~h6})·이미지까지 허용하고,
	 * {@code img} 의 {@code style} 속성은 width/height 선언만 남긴다
	 * ({@link EgovHtmlSanitizer} 의 style 재필터).
	 * 게시판 본문 등 에디터로 작성한 HTML 을 그대로 출력하는 화면에 사용한다.
	 */
	RICH_TEXT;

	/**
	 * 정책에 해당하는 jsoup Safelist 를 새로 만들어 반환한다.
	 *
	 * <p>Safelist 는 가변 객체이므로 호출마다 새 인스턴스를 만들어
	 * 공유 상태 변이를 차단한다. 컨텍스트 상대경로 URL
	 * (예: {@code /utl/web/imageSrc.do?...})이 절대경로로 바뀌지 않도록
	 * 모든 정책에 {@code preserveRelativeLinks} 를 적용한다.</p>
	 *
	 * @return 이 정책의 Safelist (매 호출 새 인스턴스)
	 */
	Safelist newSafelist() {
		switch (this) {
			case TEXT_ONLY:
				return Safelist.none();
			case SIMPLE_TEXT:
				return Safelist.simpleText();
			case BASIC:
				return Safelist.basic().preserveRelativeLinks(true);
			case BASIC_WITH_IMAGES:
				return Safelist.basicWithImages().preserveRelativeLinks(true);
			case RICH_TEXT:
			default:
				return Safelist.relaxed()
						.addAttributes("img", "style")
						.preserveRelativeLinks(true);
		}
	}

}
