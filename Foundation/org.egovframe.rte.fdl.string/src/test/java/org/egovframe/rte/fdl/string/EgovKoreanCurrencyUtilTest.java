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

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovKoreanCurrencyUtil} 단위 테스트.
 *
 * <p>자리 계수 1을 "일"로 적는 위·변조 방지 표기, 만·억·조·경 단위의 자리 건너뛰기, {@code Long.MAX_VALUE}
 * 경계, 기본 로케일(Locale)이 바뀌어도 숫자 구분자가 흔들리지 않는 점을 고정한다.</p>
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
class EgovKoreanCurrencyUtilTest {

	private void underGermanLocale(Runnable body) {
		Locale original = Locale.getDefault();
		try {
			Locale.setDefault(Locale.GERMANY);
			body.run();
		} finally {
			Locale.setDefault(original);
		}
	}

	@Nested
	@DisplayName("금액을 한글로 읽는다")
	class 한글_읽기 {

		@Test
		@DisplayName("한 자리와 0")
		void 한자리() {
			assertEquals("영", EgovKoreanCurrencyUtil.toKoreanReading(0));
			assertEquals("일", EgovKoreanCurrencyUtil.toKoreanReading(1));
			assertEquals("오", EgovKoreanCurrencyUtil.toKoreanReading(5));
			assertEquals("구", EgovKoreanCurrencyUtil.toKoreanReading(9));
		}

		@Test
		@DisplayName("자리 계수가 1이어도 '일'을 적는다 (위·변조 방지 표기)")
		void 계수_일_표기() {
			assertEquals("일십", EgovKoreanCurrencyUtil.toKoreanReading(10));
			assertEquals("일백", EgovKoreanCurrencyUtil.toKoreanReading(100));
			assertEquals("일천", EgovKoreanCurrencyUtil.toKoreanReading(1000));
			assertEquals("일만", EgovKoreanCurrencyUtil.toKoreanReading(10000));
			assertEquals("일십오", EgovKoreanCurrencyUtil.toKoreanReading(15));
		}

		@Test
		@DisplayName("네 자리 안쪽을 읽는다")
		void 네자리_이하() {
			assertEquals("오천", EgovKoreanCurrencyUtil.toKoreanReading(5000));
			assertEquals("일천이백삼십사", EgovKoreanCurrencyUtil.toKoreanReading(1234));
			assertEquals("구천구백구십구", EgovKoreanCurrencyUtil.toKoreanReading(9999));
		}

		@Test
		@DisplayName("값이 0인 자리는 건너뛴다")
		void 영인_자리_생략() {
			assertEquals("일천사", EgovKoreanCurrencyUtil.toKoreanReading(1004));
			assertEquals("오천팔백칠", EgovKoreanCurrencyUtil.toKoreanReading(5807));
			assertEquals("일만일", EgovKoreanCurrencyUtil.toKoreanReading(10001));
			assertEquals("일억일", EgovKoreanCurrencyUtil.toKoreanReading(100000001L));
		}

		@Test
		@DisplayName("만·억·조·경 단위로 끊어 읽는다")
		void 큰_단위() {
			assertEquals("일백이십삼만사천오백육십칠", EgovKoreanCurrencyUtil.toKoreanReading(1234567));
			assertEquals("일백오십만", EgovKoreanCurrencyUtil.toKoreanReading(1500000));
			assertEquals("일억", EgovKoreanCurrencyUtil.toKoreanReading(100000000L));
			assertEquals("일조", EgovKoreanCurrencyUtil.toKoreanReading(1000000000000L));
			assertEquals("일경", EgovKoreanCurrencyUtil.toKoreanReading(10000000000000000L));
		}

		@Test
		@DisplayName("빈 단위를 건너뛰고 이어 붙인다")
		void 단위_건너뛰기() {
			// 1조 1원 — 억·만 자리가 통째로 비어 있다
			assertEquals("일조일", EgovKoreanCurrencyUtil.toKoreanReading(1000000000001L));
			// 1억 2345원 — 만 자리가 비어 있다
			assertEquals("일억이천삼백사십오", EgovKoreanCurrencyUtil.toKoreanReading(100002345L));
		}

		@Test
		@DisplayName("Long.MAX_VALUE 까지 다룬다")
		void 최대값() {
			assertEquals("구백이십이경삼천삼백칠십이조삼백육십팔억오천사백칠십칠만오천팔백칠",
					EgovKoreanCurrencyUtil.toKoreanReading(Long.MAX_VALUE));
		}

		@Test
		@DisplayName("음수는 거부한다")
		void 음수_거부() {
			assertThrows(IllegalArgumentException.class, () -> EgovKoreanCurrencyUtil.toKoreanReading(-1));
			assertThrows(IllegalArgumentException.class, () -> EgovKoreanCurrencyUtil.toKoreanCurrency(-1000));
			assertThrows(IllegalArgumentException.class, () -> EgovKoreanCurrencyUtil.toKoreanAmountMixed(Long.MIN_VALUE));
		}
	}

	@Nested
	@DisplayName("공문서 표기를 만든다")
	class 공문서_표기 {

		@Test
		@DisplayName("일금 …원정 형식으로 적는다")
		void 일금_원정() {
			assertEquals("일금 오천원정", EgovKoreanCurrencyUtil.toKoreanCurrency(5000));
			assertEquals("일금 일백오십만원정", EgovKoreanCurrencyUtil.toKoreanCurrency(1500000));
			assertEquals("일금 영원정", EgovKoreanCurrencyUtil.toKoreanCurrency(0));
		}

		@Test
		@DisplayName("숫자와 한글을 나란히 적는다")
		void 숫자_병기() {
			assertEquals("1,234원(일금 일천이백삼십사원정)", EgovKoreanCurrencyUtil.toKoreanAmountMixed(1234));
			assertEquals("0원(일금 영원정)", EgovKoreanCurrencyUtil.toKoreanAmountMixed(0));
			assertEquals("1,000,000원(일금 일백만원정)", EgovKoreanCurrencyUtil.toKoreanAmountMixed(1000000));
		}

		@Test
		@DisplayName("기본 로케일이 바뀌어도 천 단위 구분자는 쉼표다")
		void 로케일_무관() {
			underGermanLocale(() -> {
				assertEquals("1,234원(일금 일천이백삼십사원정)", EgovKoreanCurrencyUtil.toKoreanAmountMixed(1234));
				assertEquals("1,000,000원(일금 일백만원정)", EgovKoreanCurrencyUtil.toKoreanAmountMixed(1000000));
			});
			assertEquals("1,234원(일금 일천이백삼십사원정)", EgovKoreanCurrencyUtil.toKoreanAmountMixed(1234));
		}

		@Test
		@DisplayName("읽기·공문서·병기 표기가 서로 맞물린다")
		void 표기_일관성() {
			long amount = 98765432L;
			String reading = EgovKoreanCurrencyUtil.toKoreanReading(amount);
			assertEquals("일금 " + reading + "원정", EgovKoreanCurrencyUtil.toKoreanCurrency(amount));
			assertTrue(EgovKoreanCurrencyUtil.toKoreanAmountMixed(amount)
					.endsWith("(" + EgovKoreanCurrencyUtil.toKoreanCurrency(amount) + ")"));
		}
	}

}
