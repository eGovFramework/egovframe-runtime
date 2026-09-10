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
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovLocaleFormats} 단위 테스트.
 *
 * <p>로케일을 <b>명시 지정</b>해 검증한다 — JVM 기본 로케일에 기대면 실행 환경에 따라
 * 결과가 갈려 테스트가 흔들린다.</p>
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
class EgovLocaleFormatsTest {

	@Nested
	@DisplayName("숫자·통화·백분율")
	class Numbers {

		@Test
		@DisplayName("자릿수 구분")
		void 숫자() {
			assertEquals("1,234,567.89", EgovLocaleFormats.number(1234567.89, Locale.KOREA));
			assertEquals("1.234.567,89", EgovLocaleFormats.number(1234567.89, Locale.GERMANY));
		}

		@Test
		@DisplayName("통화 — 기호·자릿수가 로케일을 따른다")
		void 통화() {
			assertEquals("₩50,000", EgovLocaleFormats.currency(50000, Locale.KOREA));
			assertEquals("$50,000.00", EgovLocaleFormats.currency(50000, Locale.US));
		}

		@Test
		@DisplayName("백분율 — 값은 비율이다")
		void 백분율() {
			assertEquals("76%", EgovLocaleFormats.percent(0.756, Locale.KOREA));
			assertEquals("100%", EgovLocaleFormats.percent(1, Locale.KOREA));
		}
	}

	@Nested
	@DisplayName("날짜·일시")
	class Dates {

		private final LocalDate date = LocalDate.of(2026, 9, 1);

		@Test
		@DisplayName("한국어 관례")
		void 한국어() {
			assertEquals("2026년 9월 1일", EgovLocaleFormats.date(date, FormatStyle.LONG, Locale.KOREA));
			assertEquals("2026. 9. 1.", EgovLocaleFormats.date(date, FormatStyle.MEDIUM, Locale.KOREA));
		}

		@Test
		@DisplayName("영어 관례 — 같은 값, 다른 표기")
		void 영어() {
			assertEquals("Sep 1, 2026", EgovLocaleFormats.date(date, FormatStyle.MEDIUM, Locale.US));
		}

		@Test
		@DisplayName("일시")
		void 일시() {
			String formatted = EgovLocaleFormats.dateTime(
					LocalDateTime.of(2026, 9, 1, 14, 30, 0), FormatStyle.MEDIUM, Locale.KOREA);

			assertTrue(formatted.contains("2026"), "실제 값: " + formatted);
			assertTrue(formatted.contains("2:30") || formatted.contains("14:30"), "실제 값: " + formatted);
		}
	}

	@Nested
	@DisplayName("계약")
	class Contract {

		@Test
		@DisplayName("null 값은 빈 문자열 — 화면을 깨뜨리지 않는다")
		void null_값() {
			assertEquals("", EgovLocaleFormats.number(null, Locale.KOREA));
			assertEquals("", EgovLocaleFormats.currency(null, Locale.KOREA));
			assertEquals("", EgovLocaleFormats.percent(null, Locale.KOREA));
			assertEquals("", EgovLocaleFormats.date(null, FormatStyle.MEDIUM, Locale.KOREA));
			assertEquals("", EgovLocaleFormats.dateTime(null, FormatStyle.MEDIUM, Locale.KOREA));
		}

		@Test
		@DisplayName("로케일·스타일 인자가 null 이면 즉시 실패 — 값 null 과 구분한다")
		void null_인자() {
			assertThrows(IllegalArgumentException.class,
					() -> EgovLocaleFormats.number(1, null));
			assertThrows(IllegalArgumentException.class,
					() -> EgovLocaleFormats.date(LocalDate.now(), null, Locale.KOREA));
		}
	}
}
