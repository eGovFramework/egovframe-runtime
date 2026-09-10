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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovLunarDates} 의 <b>입력 범위 검증</b> 회귀 검증.
 *
 * <p>양력·음력 날짜는 화면 입력이 그대로 들어오는 값이다. 지원 범위 밖의 연도에서 ICU 달력은
 * 예외 없이 <b>틀린 날짜</b>를 돌려주거나(±580만 년 밖: 시각을 한계값으로 고정, 64032년 이후:
 * 왕복 불일치) 내부 필드 오류를 던진다. 이 클래스는 그런 입력이 <b>즉시, 범위를 말하는 예외</b>로
 * 닫히고, 범위 안에서는 왕복이 성립함을 고정한다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.07  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
class EgovLunarDatesSecurityTest {

	@Test
	@DisplayName("지원 범위 밖의 양력 연도는 계산 전에 범위를 말하는 예외로 닫힌다")
	void 양력_범위_밖_즉시_거부() {
		for (LocalDate solar : List.of(LocalDate.of(0, 12, 31), LocalDate.of(10_000, 1, 1), LocalDate.of(67_685, 1, 1),
				LocalDate.of(10_000_000, 6, 15), LocalDate.MAX, LocalDate.MIN)) {
			IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> EgovLunarDates.toLunar(solar), solar.toString());
			assertTrue(e.getMessage().contains("1..9999"), "범위를 말해야 한다: " + e.getMessage());
		}
	}

	@Test
	@DisplayName("지원 범위 밖의 음력 연도는 계산 전에 범위를 말하는 예외로 닫힌다")
	void 음력_범위_밖_즉시_거부() {
		for (EgovLunarDate lunar : List.of(EgovLunarDate.of(0, 1, 1), EgovLunarDate.of(10_000, 1, 1),
				EgovLunarDate.of(Integer.MAX_VALUE, 1, 1), EgovLunarDate.of(Integer.MIN_VALUE, 1, 1))) {
			IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> EgovLunarDates.toSolar(lunar), lunar.toString());
			assertTrue(e.getMessage().contains("1..9999"), "범위를 말해야 한다: " + e.getMessage());
		}
	}

	@Test
	@DisplayName("범위 경계는 성립한다 — 양력 0001-01-01 과 9999-12-31, 음력 1년 1월 1일과 9999년 12월 1일")
	void 경계값_성립() {
		assertEquals(0, EgovLunarDates.toLunar(LocalDate.of(1, 1, 1)).getYear(), "양력 1년 1월은 음력으로 전년도 12월");
		assertEquals(9999, EgovLunarDates.toLunar(LocalDate.of(9999, 12, 31)).getYear());
		assertEquals(1, EgovLunarDates.toSolar(EgovLunarDate.of(1, 1, 1)).getYear());
		LocalDate lastLunarYearStart = EgovLunarDates.toSolar(EgovLunarDate.of(9999, 12, 1));
		assertTrue(lastLunarYearStart.getYear() >= 9999, "음력 9999년 12월은 양력 9999년 말~10000년 초");
	}

	@Test
	@DisplayName("범위 안에서는 양력→음력→양력 왕복이 성립한다 — 1~9999년을 7년 간격으로, 해마다 세 날짜")
	void 범위_안_왕복_표본() {
		int checked = 0;
		for (int year = 1; year <= 9999; year += 7) {
			for (LocalDate solar : List.of(LocalDate.of(year, 1, 1), LocalDate.of(year, 6, 15), LocalDate.of(year, 12, 31))) {
				EgovLunarDate lunar = EgovLunarDates.toLunar(solar);
				if (lunar.getYear() < 1 || lunar.getYear() > 9999) {
					continue; // 양력 1년 초·9999년 말은 음력 연도가 범위 밖이라 역변환 입력이 될 수 없다
				}
				assertEquals(solar, EgovLunarDates.toSolar(lunar), "왕복 불일치: " + solar + " -> " + lunar);
				checked++;
			}
		}
		assertTrue(checked > 4_000, "표본 수: " + checked);
	}

	@Test
	@DisplayName("변환 비용은 입력값에 따라 폭증하지 않는다 — 범위 양끝 1,000회가 수 초 안에 끝난다")
	void 변환_비용_상한() {
		long start = System.nanoTime();
		for (int i = 0; i < 500; i++) {
			EgovLunarDates.toLunar(LocalDate.of(9999, 1, 1).minusDays(i * 3L));
			EgovLunarDates.toLunar(LocalDate.of(1, 1, 1).plusDays(i * 3L));
		}
		long millis = (System.nanoTime() - start) / 1_000_000;
		assertTrue(millis < 10_000, "1,000회 변환에 " + millis + " ms");
	}
}
