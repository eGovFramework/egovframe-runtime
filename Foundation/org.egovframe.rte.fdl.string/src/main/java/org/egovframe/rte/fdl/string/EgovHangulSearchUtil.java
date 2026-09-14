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
package org.egovframe.rte.fdl.string;

/**
 * 한글 <b>초성</b>을 뽑고 초성으로 검색하는 유틸 클래스.
 *
 * <p><b>NOTE:</b> 목록에서 "ㅅㅅ"만 쳐도 "삼성"이 걸리는 검색은 한국어 화면에서 흔히 기대되는 동작이지만,
 * 표준 문자열 검색으로는 되지 않는다. 완성형 한글 음절(가~힣)은 <b>초성·중성·종성이 하나의 코드 포인트로
 * 합쳐져</b> 있어 초성만 따로 비교할 수 없기 때문이다. 이 유틸은 유니코드 산술로 음절에서 초성을 뽑아
 * 그 문제를 푼다.</p>
 *
 * <pre>
 * EgovHangulSearchUtil.extractChosung("삼성전자")        → "ㅅㅅㅈㅈ"
 * EgovHangulSearchUtil.extractChosung("A동 101호")       → "Aㄷ 101ㅎ"   (한글 음절만 바뀐다)
 *
 * EgovHangulSearchUtil.matchesChosung("삼성전자", "ㅅㅅ") → true   (삼성)
 * EgovHangulSearchUtil.matchesChosung("삼성전자", "ㅅㅈ") → true   (성전 — 초성열의 연속 부분)
 * EgovHangulSearchUtil.matchesChosung("삼성전자", "ㅈㅅ") → false  (순서가 다르다)
 * </pre>
 *
 * <p><b>초성은 키보드로 치는 호환 자모(U+3131~)로 낸다.</b> 사용자가 "ㅅ"을 입력하면 그 글자가 그대로
 * 들어오므로 질의와 같은 형태여야 비교가 된다. 첫가끝(조합형) 자모 ᄀ(U+1100)이 아니다.</p>
 *
 * <p><b>계약</b> — 질의에 완성형 음절이 섞이면 그 음절의 초성으로 바꿔 비교한다. 즉 {@code "삼성"} 으로
 * 찾으면 {@code "ㅅㅅ"} 으로 찾는 것과 같아 <b>초성이 같은 다른 낱말도 걸린다</b>("사슴전자"). 정확한
 * 낱말 검색이 필요한 자리에는 이 유틸 대신 일반 문자열 검색을 쓴다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.14  실행환경팀     최초 생성 (공통컴포넌트 EgovHangulSearchUtil 의 초성 산출 방식을
 *                            차용하되, 질의 정규화를 따로 두지 않고 extractChosung 으로 합쳐
 *                            같은 규칙이 두 곳에 갈라지지 않게 했다)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovHangulSearchUtil {

	/** 완성형 한글 음절의 시작 '가'. */
	private static final char HANGUL_BASE = 0xAC00;

	/** 완성형 한글 음절의 끝 '힣'. */
	private static final char HANGUL_LAST = 0xD7A3;

	/** 중성 21자 × 종성 28자 = 초성 하나가 차지하는 음절 수. */
	private static final int SYLLABLE_BLOCK = 21 * 28;

	/** 초성 19자(호환 자모). */
	private static final char[] CHOSUNG = { 'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ',
			'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ' };

	private EgovHangulSearchUtil() {
	}

	/**
	 * 문자열의 한글 음절을 초성으로 바꾼 문자열을 돌려준다. 한글 음절이 아닌 글자는 그대로 둔다.
	 *
	 * @param text 원본 문자열
	 * @return 초성으로 바뀐 문자열. {@code text} 가 {@code null} 이면 빈 문자열
	 */
	public static String extractChosung(String text) {
		if (text == null) {
			return "";
		}
		StringBuilder chosung = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (isHangulSyllable(ch)) {
				chosung.append(CHOSUNG[(ch - HANGUL_BASE) / SYLLABLE_BLOCK]);
			} else {
				chosung.append(ch);
			}
		}
		return chosung.toString();
	}

	/**
	 * 텍스트의 초성열이 질의를 이어진 부분으로 담고 있는지 본다.
	 *
	 * <p>질의에 완성형 음절이 섞이면 그 음절의 초성으로 바꿔 비교한다.</p>
	 *
	 * @param text  대상 텍스트
	 * @param query 초성 질의(예: {@code "ㅅㅅ"})
	 * @return 초성열이 질의를 담고 있으면 {@code true}. 질의가 비어 있으면 {@code true},
	 *         {@code text} 가 {@code null} 이면 {@code false}
	 */
	public static boolean matchesChosung(String text, String query) {
		if (query == null || query.isEmpty()) {
			return true;
		}
		if (text == null) {
			return false;
		}
		return extractChosung(text).contains(extractChosung(query));
	}

	/**
	 * 완성형 한글 음절(가~힣)인지 본다.
	 *
	 * @param ch 문자
	 * @return 완성형 음절이면 {@code true}. 자모 하나(ㄱ·ㅏ)나 옛한글은 {@code false}
	 */
	public static boolean isHangulSyllable(char ch) {
		return ch >= HANGUL_BASE && ch <= HANGUL_LAST;
	}

}
