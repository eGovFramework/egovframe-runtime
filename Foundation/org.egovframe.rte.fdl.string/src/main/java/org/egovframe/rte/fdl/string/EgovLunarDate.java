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

import java.util.Objects;

/**
 * 음력 날짜를 담는 <b>불변</b> 값 객체 — 연·월·일에 <b>윤달 여부</b>가 더해진다.
 *
 * <p><b>NOTE:</b> 음력은 같은 연·월·일이 평달과 윤달로 두 번 있을 수 있어 세 필드만으로는
 * 날짜가 특정되지 않는다. 윤달 여부까지 가진 전용 타입을 둔 이유다 — 문자열이나
 * {@code Map} 으로 나르면 이 정보가 어디선가 빠진다.</p>
 *
 * <p>변환은 {@link EgovLunarDates} 가 한다.</p>
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
public final class EgovLunarDate {

	private final int year;

	private final int month;

	private final int day;

	private final boolean leapMonth;

	private EgovLunarDate(int year, int month, int day, boolean leapMonth) {
		if (month < 1 || month > 12) {
			throw new IllegalArgumentException("month must be 1..12: " + month);
		}
		if (day < 1 || day > 30) {
			throw new IllegalArgumentException("lunar day must be 1..30: " + day);
		}
		this.year = year;
		this.month = month;
		this.day = day;
		this.leapMonth = leapMonth;
	}

	/**
	 * 평달 음력 날짜를 만든다.
	 *
	 * @param year  음력 연도
	 * @param month 음력 월(1~12)
	 * @param day   음력 일(1~30)
	 * @return 음력 날짜
	 */
	public static EgovLunarDate of(int year, int month, int day) {
		return new EgovLunarDate(year, month, day, false);
	}

	/**
	 * 윤달 음력 날짜를 만든다.
	 *
	 * @param year  음력 연도
	 * @param month 음력 월(1~12)
	 * @param day   음력 일(1~30)
	 * @return 윤달 날짜
	 */
	public static EgovLunarDate ofLeapMonth(int year, int month, int day) {
		return new EgovLunarDate(year, month, day, true);
	}

	/**
	 * 음력 연도를 반환한다.
	 *
	 * @return 연도
	 */
	public int getYear() {
		return year;
	}

	/**
	 * 음력 월을 반환한다(1~12).
	 *
	 * @return 월
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * 음력 일을 반환한다(1~30).
	 *
	 * @return 일
	 */
	public int getDay() {
		return day;
	}

	/**
	 * 윤달 여부를 반환한다.
	 *
	 * @return 윤달이면 {@code true}
	 */
	public boolean isLeapMonth() {
		return leapMonth;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EgovLunarDate)) {
			return false;
		}
		EgovLunarDate other = (EgovLunarDate) obj;
		return year == other.year && month == other.month
				&& day == other.day && leapMonth == other.leapMonth;
	}

	@Override
	public int hashCode() {
		return Objects.hash(year, month, day, leapMonth);
	}

	/**
	 * {@code 2026-01-15} · 윤달이면 {@code 2026-01-15(윤)} 형태로 표기한다.
	 */
	@Override
	public String toString() {
		return String.format("%04d-%02d-%02d%s", year, month, day, leapMonth ? "(윤)" : "");
	}
}
