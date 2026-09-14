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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovLunarDates} · {@link EgovLunarDate} 단위 테스트.
 *
 * <p>알려진 명절 날짜로 변환을 검증하고, <b>공통컴포넌트 원본(EgovDateUtil)의 침묵 실패</b>
 * (잘못된 입력 → 빈 Map, 없는 윤달 → 다른 날짜로 미끄러짐)를 예외 계약으로 고정한다.</p>
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
 * @since 5.1
 */
class EgovLunarDatesTest {

	@Nested
	@DisplayName("알려진 날짜 검증")
	class KnownDates {

		@Test
		@DisplayName("설날 — 2025-01-29 는 음력 2025년 1월 1일")
		void 설날_2025() {
			EgovLunarDate lunar = EgovLunarDates.toLunar(LocalDate.of(2025, 1, 29));

			assertEquals(EgovLunarDate.of(2025, 1, 1), lunar);
			assertFalse(lunar.isLeapMonth());
		}

		@Test
		@DisplayName("추석 — 2025-10-06 은 음력 2025년 8월 15일")
		void 추석_2025() {
			assertEquals(EgovLunarDate.of(2025, 8, 15),
					EgovLunarDates.toLunar(LocalDate.of(2025, 10, 6)));
		}

		@Test
		@DisplayName("음력 → 양력 — 설날 역방향")
		void 음력에서_양력() {
			assertEquals(LocalDate.of(2025, 1, 29),
					EgovLunarDates.toSolar(EgovLunarDate.of(2025, 1, 1)));
		}

		@Test
		@DisplayName("윤달 — 2025년 윤6월 1일은 양력 2025-07-25")
		void 윤달_2025_윤6월() {
			LocalDate solar = EgovLunarDates.toSolar(EgovLunarDate.ofLeapMonth(2025, 6, 1));
			assertEquals(LocalDate.of(2025, 7, 25), solar);

			EgovLunarDate back = EgovLunarDates.toLunar(solar);
			assertTrue(back.isLeapMonth(), "역변환에서 윤달 정보가 보존돼야 한다");
			assertEquals(EgovLunarDate.ofLeapMonth(2025, 6, 1), back);
		}

		@Test
		@DisplayName("한국 기준(단기력)으로 계산한다 — 중국 기준 ChineseCalendar 라면 실패한다")
		void 한국_기준_역법() {
			// 2001-04-24: 단기력(한국)은 음력 4/1, ChineseCalendar(중국)는 4/2 —
			// 합삭 시각이 자정 부근이라 월 시작이 하루 갈리는 구간이다(2000~2026 실측 418일).
			assertEquals(EgovLunarDate.of(2001, 4, 1),
					EgovLunarDates.toLunar(LocalDate.of(2001, 4, 24)));
		}

		@Test
		@DisplayName("평달 6월과 윤6월은 서로 다른 양력 날짜다 — 윤달 구분이 값 객체에 실리는 이유")
		void 평달과_윤달_구분() {
			LocalDate plain = EgovLunarDates.toSolar(EgovLunarDate.of(2025, 6, 1));
			LocalDate leap = EgovLunarDates.toSolar(EgovLunarDate.ofLeapMonth(2025, 6, 1));

			assertEquals(LocalDate.of(2025, 6, 25), plain);
			assertFalse(plain.equals(leap));
		}
	}

	@Nested
	@DisplayName("KASI 확정값 대조 — 정부 공휴일 고시 30점")
	class KasiConformance {

		/** 공휴일 고시 기준 설날(음력 1/1) 양력 날짜 — 2004~2026. */
		private static final String[] SEOLLAL = {
				"2004-01-22", "2005-02-09", "2006-01-29", "2007-02-18", "2008-02-07",
				"2009-01-26", "2010-02-14", "2011-02-03", "2012-01-23", "2013-02-10",
				"2014-01-31", "2015-02-19", "2016-02-08", "2017-01-28", "2018-02-16",
				"2019-02-05", "2020-01-25", "2021-02-12", "2022-02-01", "2023-01-22",
				"2024-02-10", "2025-01-29", "2026-02-17"};

		/** 공휴일 고시 기준 추석(음력 8/15) 양력 날짜 — 2020~2026. */
		private static final String[] CHUSEOK = {
				"2020-10-01", "2021-09-21", "2022-09-10", "2023-09-29",
				"2024-09-17", "2025-10-06", "2026-09-25"};

		@Test
		@DisplayName("설날 23년치가 전부 음력 1월 1일이다 — icu4j 업그레이드 시 정합 감시")
		void 설날_전수() {
			for (String solar : SEOLLAL) {
				EgovLunarDate lunar = EgovLunarDates.toLunar(LocalDate.parse(solar));
				assertEquals(EgovLunarDate.of(lunar.getYear(), 1, 1), lunar, "설날 불일치: " + solar);
			}
		}

		@Test
		@DisplayName("추석 7년치가 전부 음력 8월 15일이다")
		void 추석_전수() {
			for (String solar : CHUSEOK) {
				EgovLunarDate lunar = EgovLunarDates.toLunar(LocalDate.parse(solar));
				assertEquals(EgovLunarDate.of(lunar.getYear(), 8, 15), lunar, "추석 불일치: " + solar);
			}
		}

		@Test
		@DisplayName("2017년은 한국 윤5월이다 — 중국력(윤6월)이라면 실패한다")
		void 이천십칠년_윤오월() {
			// 한·중 윤달 위치가 다른 해로 알려진 사례 — 계통 오차가 아니라 역법 차이의 크기를 보여준다.
			EgovLunarDate june25 = EgovLunarDates.toLunar(LocalDate.of(2017, 6, 25));
			assertTrue(june25.isLeapMonth(), "2017-06-25 는 윤5월 구간이어야 한다: " + june25);
			assertEquals(5, june25.getMonth());
		}
	}

	@Nested
	@DisplayName("왕복 일관성")
	class RoundTrip {

		@Test
		@DisplayName("1년치 매일 왕복 변환이 원본과 일치한다")
		void 일년_왕복() {
			LocalDate date = LocalDate.of(2025, 1, 1);
			for (int i = 0; i < 365; i++) {
				EgovLunarDate lunar = EgovLunarDates.toLunar(date);
				assertEquals(date, EgovLunarDates.toSolar(lunar), "왕복 불일치: " + date);
				date = date.plusDays(1);
			}
		}
	}

	@Nested
	@DisplayName("원본 결함 회귀 — 침묵 실패를 예외 계약으로")
	class LegacyDefectRegression {

		@Test
		@DisplayName("존재하지 않는 윤달은 예외다 (원본은 다른 날짜로 조용히 미끄러졌다)")
		void 없는_윤달() {
			// 2026년에는 윤1월이 없다.
			assertThrows(IllegalArgumentException.class,
					() -> EgovLunarDates.toSolar(EgovLunarDate.ofLeapMonth(2026, 1, 1)));
		}

		@Test
		@DisplayName("존재하지 않는 30일은 예외다")
		void 없는_30일() {
			// 2025년 음력 4월은 29일까지다(작은달).
			assertThrows(IllegalArgumentException.class,
					() -> EgovLunarDates.toSolar(EgovLunarDate.of(2025, 4, 30)));
		}

		@Test
		@DisplayName("null 입력은 즉시 예외다 (원본은 빈 Map 을 돌려줬다)")
		void null_입력() {
			assertThrows(IllegalArgumentException.class, () -> EgovLunarDates.toLunar(null));
			assertThrows(IllegalArgumentException.class, () -> EgovLunarDates.toSolar(null));
		}

		@Test
		@DisplayName("값 객체 자체도 범위를 검증한다")
		void 값_객체_검증() {
			assertThrows(IllegalArgumentException.class, () -> EgovLunarDate.of(2025, 13, 1));
			assertThrows(IllegalArgumentException.class, () -> EgovLunarDate.of(2025, 1, 31));
			assertThrows(IllegalArgumentException.class, () -> EgovLunarDate.of(2025, 0, 1));
		}
	}

	@Nested
	@DisplayName("값 객체")
	class ValueObject {

		@Test
		@DisplayName("동일성은 연·월·일·윤달 네 필드")
		void 동일성() {
			assertEquals(EgovLunarDate.of(2025, 6, 1), EgovLunarDate.of(2025, 6, 1));
			assertFalse(EgovLunarDate.of(2025, 6, 1).equals(EgovLunarDate.ofLeapMonth(2025, 6, 1)));
		}

		@Test
		@DisplayName("표기 — 윤달은 (윤) 이 붙는다")
		void 표기() {
			assertEquals("2025-06-01", EgovLunarDate.of(2025, 6, 1).toString());
			assertEquals("2025-06-01(윤)", EgovLunarDate.ofLeapMonth(2025, 6, 1).toString());
		}
	}
}
