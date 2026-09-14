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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovHangulSearchUtil} 단위 테스트.
 *
 * <p>초성 산출의 유니코드 경계와 검색 계약을 고정한다 — 겹자음·종성이 있는 음절, 음절 범위의 양 끝,
 * 자모 하나는 음절이 아니라는 점, 질의에 완성형 음절이 섞였을 때의 정규화.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.14  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
class EgovHangulSearchUtilTest {

	@Nested
	@DisplayName("음절에서 초성을 뽑는다")
	class 초성_추출 {

		@Test
		@DisplayName("한글 음절을 초성으로 바꾼다")
		void 기본_추출() {
			assertEquals("ㅅㅅㅈㅈ", EgovHangulSearchUtil.extractChosung("삼성전자"));
			assertEquals("ㄱㄴㄷ", EgovHangulSearchUtil.extractChosung("가나다"));
			assertEquals("ㅎㄱㄷ", EgovHangulSearchUtil.extractChosung("홍길동"));
		}

		@Test
		@DisplayName("겹자음 초성과 종성이 있는 음절도 초성만 뽑는다")
		void 겹자음과_종성() {
			assertEquals("ㄲ", EgovHangulSearchUtil.extractChosung("까"));
			assertEquals("ㄸㅃㅆㅉ", EgovHangulSearchUtil.extractChosung("따빠싸짜"));
			assertEquals("ㄱ", EgovHangulSearchUtil.extractChosung("강"));
			assertEquals("ㄱ", EgovHangulSearchUtil.extractChosung("값"));
		}

		@Test
		@DisplayName("한글 음절이 아닌 글자는 그대로 둔다")
		void 비한글_유지() {
			assertEquals("Aㄷ 101ㅎ", EgovHangulSearchUtil.extractChosung("A동 101호"));
			assertEquals("ABC123", EgovHangulSearchUtil.extractChosung("ABC123"));
			assertEquals("ㄱㄴ", EgovHangulSearchUtil.extractChosung("ㄱㄴ"));
			assertEquals("ㅏㅑ", EgovHangulSearchUtil.extractChosung("ㅏㅑ"));
			assertEquals("漢字", EgovHangulSearchUtil.extractChosung("漢字"));
		}

		@Test
		@DisplayName("음절 범위의 양 끝을 정확히 처리한다")
		void 유니코드_경계() {
			assertEquals("ㄱ", EgovHangulSearchUtil.extractChosung("가"));
			assertEquals("ㅎ", EgovHangulSearchUtil.extractChosung("힣"));
			assertEquals("꯿", EgovHangulSearchUtil.extractChosung("꯿"));
			assertEquals("힤", EgovHangulSearchUtil.extractChosung("힤"));
		}

		@Test
		@DisplayName("null 과 빈 문자열은 빈 문자열이다")
		void 널과_빈값() {
			assertEquals("", EgovHangulSearchUtil.extractChosung(null));
			assertEquals("", EgovHangulSearchUtil.extractChosung(""));
		}
	}

	@Nested
	@DisplayName("초성으로 검색한다")
	class 초성_검색 {

		@Test
		@DisplayName("초성열의 이어진 부분이면 찾는다")
		void 연속_부분_일치() {
			assertTrue(EgovHangulSearchUtil.matchesChosung("삼성전자", "ㅅㅅ"));
			assertTrue(EgovHangulSearchUtil.matchesChosung("삼성전자", "ㅅㅈ"));
			assertTrue(EgovHangulSearchUtil.matchesChosung("삼성전자", "ㅈㅈ"));
			assertTrue(EgovHangulSearchUtil.matchesChosung("삼성전자", "ㅅㅅㅈㅈ"));
		}

		@Test
		@DisplayName("순서가 다르거나 이어지지 않으면 찾지 못한다")
		void 불일치() {
			assertFalse(EgovHangulSearchUtil.matchesChosung("삼성전자", "ㅈㅅ"));
			assertFalse(EgovHangulSearchUtil.matchesChosung("삼성전자", "ㅅㅅㅅ"));
			assertFalse(EgovHangulSearchUtil.matchesChosung("삼성전자", "ㅅㅈㅅ"));
			assertFalse(EgovHangulSearchUtil.matchesChosung("삼성전자", "ㄱ"));
		}

		@Test
		@DisplayName("질의에 완성형 음절이 섞이면 초성으로 바꿔 비교한다")
		void 질의_정규화() {
			assertTrue(EgovHangulSearchUtil.matchesChosung("삼성전자", "삼성"));
			assertTrue(EgovHangulSearchUtil.matchesChosung("삼성전자", "ㅅ성"));
			assertTrue(EgovHangulSearchUtil.matchesChosung("사슴전자", "삼성"));
		}

		@Test
		@DisplayName("질의가 비면 참, 대상이 null 이면 거짓이다")
		void 경계_입력() {
			assertTrue(EgovHangulSearchUtil.matchesChosung("삼성전자", null));
			assertTrue(EgovHangulSearchUtil.matchesChosung("삼성전자", ""));
			assertTrue(EgovHangulSearchUtil.matchesChosung(null, ""));
			assertFalse(EgovHangulSearchUtil.matchesChosung(null, "ㅅ"));
		}

		@Test
		@DisplayName("한글이 아닌 글자도 그대로 비교한다")
		void 비한글_검색() {
			assertTrue(EgovHangulSearchUtil.matchesChosung("A동 101호", "Aㄷ"));
			assertTrue(EgovHangulSearchUtil.matchesChosung("A동 101호", "101"));
			assertFalse(EgovHangulSearchUtil.matchesChosung("A동 101호", "B"));
		}
	}

	@Nested
	@DisplayName("음절 여부를 가린다")
	class 음절_판정 {

		@Test
		@DisplayName("완성형 음절만 참이다")
		void 음절만_참() {
			assertTrue(EgovHangulSearchUtil.isHangulSyllable('가'));
			assertTrue(EgovHangulSearchUtil.isHangulSyllable('힣'));
			assertTrue(EgovHangulSearchUtil.isHangulSyllable('뷁'));
			assertFalse(EgovHangulSearchUtil.isHangulSyllable('ㄱ'));
			assertFalse(EgovHangulSearchUtil.isHangulSyllable('ㅏ'));
			assertFalse(EgovHangulSearchUtil.isHangulSyllable('A'));
			assertFalse(EgovHangulSearchUtil.isHangulSyllable('漢'));
		}
	}

	@Nested
	@DisplayName("실제 쓰임 — 목록 필터링")
	class 목록_필터링 {

		private static final List<String> 기관목록 = List.of("행정안전부", "국세청", "조달청", "기획재정부", "고용노동부");

		private List<String> 초성으로_거르기(String query) {
			return 기관목록.stream()
					.filter(name -> EgovHangulSearchUtil.matchesChosung(name, query))
					.collect(Collectors.toList());
		}

		@Test
		@DisplayName("초성 질의로 기관명을 걸러낸다")
		void 기관명_필터() {
			assertEquals(List.of("국세청"), 초성으로_거르기("ㄱㅅㅊ"));
			assertEquals(List.of("고용노동부"), 초성으로_거르기("ㄱㅇ"));
			assertEquals(List.of("기획재정부"), 초성으로_거르기("ㄱㅎ"));
			// 앞에서만 찾지 않는다 — 기획재정부(ㄱㅎㅈㅈㅂ)의 가운데에도 "ㅎㅈ"가 있다
			assertEquals(List.of("행정안전부", "기획재정부"), 초성으로_거르기("ㅎㅈ"));
			assertEquals(List.of("국세청", "조달청"), 초성으로_거르기("ㅊ"));
			assertEquals(List.of(), 초성으로_거르기("ㅋㅋ"));
			assertEquals(기관목록, 초성으로_거르기(""));
		}
	}

}
