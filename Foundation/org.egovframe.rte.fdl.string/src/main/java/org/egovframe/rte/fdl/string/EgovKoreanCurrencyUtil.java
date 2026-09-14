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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * 숫자 금액을 <b>한글 금액 표기</b>로 바꾸는 유틸 클래스.
 *
 * <p><b>NOTE:</b> 지출결의서·계약서·세금계산서 같은 공문서는 아라비아 숫자 옆에 한글 금액을 함께 적는다.
 * 숫자만 적으면 {@code 1,000} 에 획을 더해 {@code 4,000} 으로 고치는 식의 위·변조를 알아채기 어렵기
 * 때문이다. 이 유틸은 그 표기를 만든다.</p>
 *
 * <pre>
 * EgovKoreanCurrencyUtil.toKoreanReading(1234567)  → "일백이십삼만사천오백육십칠"
 * EgovKoreanCurrencyUtil.toKoreanCurrency(5000)    → "일금 오천원정"
 * EgovKoreanCurrencyUtil.toKoreanAmountMixed(1234) → "1,234원(일금 일천이백삼십사원정)"
 * </pre>
 *
 * <p><b>자리 계수가 1이어도 "일"을 적는다</b>(1,000 → "일천", 10,000 → "일만"). 말할 때는 "천"·"만" 이지만,
 * 금액 표기에서 "천"으로 적으면 앞에 글자를 덧붙여 고치기 쉬워 위·변조 방지 관례가 "일천"을 쓴다. 일상적인
 * 수 읽기가 필요한 자리에는 맞지 않는다.</p>
 *
 * <p>{@code Long.MAX_VALUE}(922경 남짓)까지 다룬다. 단위는 만·억·조·경까지 쓴다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.14  실행환경팀     최초 생성 (공통컴포넌트 EgovKoreanCurrencyUtil 의 표기 규칙을
 *                            차용하되, 자릿값을 부동소수 Math.pow 대신 정수 상수로 두고
 *                            예외 메시지를 실행환경 관례에 맞췄다)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovKoreanCurrencyUtil {

	/** 0~9 의 한글 표기. */
	private static final char[] DIGITS = { '영', '일', '이', '삼', '사', '오', '육', '칠', '팔', '구' };

	/** 네 자리 안쪽의 자릿수 이름. */
	private static final String[] SMALL_UNITS = { "", "십", "백", "천" };

	/** 네 자리마다 올라가는 큰 단위 이름. */
	private static final String[] BIG_UNITS = { "", "만", "억", "조", "경" };

	/** {@link #SMALL_UNITS} 에 맞는 자릿값. */
	private static final int[] SMALL_UNIT_VALUES = { 1, 10, 100, 1000 };

	/** 큰 단위 하나가 담는 수. */
	private static final int GROUP_SIZE = 10000;

	private EgovKoreanCurrencyUtil() {
	}

	/**
	 * 금액을 한글로 읽은 문자열로 바꾼다.
	 *
	 * @param amount 0 이상의 금액
	 * @return 한글 금액 문자열. 0 이면 {@code "영"}
	 * @throws IllegalArgumentException 금액이 음수인 경우
	 */
	public static String toKoreanReading(long amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("amount must not be negative: " + amount);
		}
		if (amount == 0) {
			return String.valueOf(DIGITS[0]);
		}

		String[] groupReadings = new String[BIG_UNITS.length];
		long remaining = amount;
		int bigUnitIndex = 0;
		while (remaining > 0 && bigUnitIndex < BIG_UNITS.length) {
			int group = (int) (remaining % GROUP_SIZE);
			if (group > 0) {
				groupReadings[bigUnitIndex] = readGroup(group) + BIG_UNITS[bigUnitIndex];
			}
			remaining /= GROUP_SIZE;
			bigUnitIndex++;
		}

		StringBuilder reading = new StringBuilder();
		for (int i = groupReadings.length - 1; i >= 0; i--) {
			if (groupReadings[i] != null) {
				reading.append(groupReadings[i]);
			}
		}
		return reading.toString();
	}

	/**
	 * 금액을 공문서 표기({@code "일금 …원정"})로 바꾼다.
	 *
	 * @param amount 0 이상의 금액
	 * @return {@code "일금 …원정"} 형식 문자열
	 * @throws IllegalArgumentException 금액이 음수인 경우
	 */
	public static String toKoreanCurrency(long amount) {
		return "일금 " + toKoreanReading(amount) + "원정";
	}

	/**
	 * 아라비아 숫자와 한글 금액을 나란히 적는다.
	 *
	 * <p>숫자 부분의 천 단위 구분자는 <b>서버 기본 로케일(Locale)과 무관하게</b> 쉼표로 고정한다. 공문서 표기라
	 * 실행 환경에 따라 {@code "1.234원"} 처럼 달라져서는 안 되기 때문이다.</p>
	 *
	 * @param amount 0 이상의 금액
	 * @return {@code "1,234원(일금 …원정)"} 형식 문자열
	 * @throws IllegalArgumentException 금액이 음수인 경우
	 */
	public static String toKoreanAmountMixed(long amount) {
		DecimalFormat format = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.KOREA));
		return format.format(amount) + "원(" + toKoreanCurrency(amount) + ")";
	}

	/**
	 * 네 자리 안쪽의 수(1~9999)를 한글로 읽는다. 위·변조 방지 표기라 자리 계수가 1이어도 "일"을 적는다.
	 */
	private static String readGroup(int group) {
		StringBuilder reading = new StringBuilder();
		for (int position = SMALL_UNIT_VALUES.length - 1; position >= 0; position--) {
			int digit = (group / SMALL_UNIT_VALUES[position]) % 10;
			if (digit != 0) {
				reading.append(DIGITS[digit]).append(SMALL_UNITS[position]);
			}
		}
		return reading.toString();
	}

}
