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

import java.time.LocalDate;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

/**
 * 양력({@link LocalDate})과 음력({@link EgovLunarDate})을 서로 변환하는 유틸 클래스.
 *
 * <p><b>NOTE:</b> ICU4J 의 <b>단기력(Dangi) 달력</b> — 한국 전통 역법 — 으로 계산한다.
 * 중국 기준인 {@code ChineseCalendar} 가 아니다: 두 역법은 기준 자오선이 달라 합삭 시각이
 * 자정 부근인 달의 월 시작이 하루 밀리며, <b>2000~2026년 실측으로 418일이 서로 다르다</b>.
 * 한국 음력에는 한국 기준 달력을 쓴다(공통컴포넌트 원본은 ChineseCalendar 를 썼다).</p>
 *
 * <p><b>icu4j 는 optional 의존</b>이라(원칙 3) 이 클래스를 쓰는 애플리케이션만 의존을
 * 선언하면 된다 — 선언 없이 호출하면 {@code NoClassDefFoundError: com/ibm/icu/...} 가 난다.</p>
 *
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;com.ibm.icu&lt;/groupId&gt;
 *     &lt;artifactId&gt;icu4j&lt;/artifactId&gt;
 *     &lt;version&gt;77.1&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * <pre>
 * EgovLunarDates.toLunar(LocalDate.of(2025, 1, 29))          → 2025-01-01 (설날)
 * EgovLunarDates.toSolar(EgovLunarDate.of(2025, 1, 1))       → 2025-01-29
 * EgovLunarDates.toSolar(EgovLunarDate.ofLeapMonth(2025, 6, 1)) → 2025-07-25 (윤6월)
 * </pre>
 *
 * <p><b>지원 범위</b> — 입력 연도(양력·음력 모두)는 {@value #MIN_YEAR}~{@value #MAX_YEAR}년이며
 * 그 밖은 {@code IllegalArgumentException} 이다. ICU 의 천문 계산은 먼 미래에서 일관성을 잃고
 * (실측: 64032년부터 왕복 불일치와 예외가 나타난다), ±580만 년 밖에서는 달력이 시각을 조용히
 * 한계값으로 고정해 <b>틀린 날짜를 예외 없이</b> 돌려준다. 사용자 입력이 그대로 들어오는 자리에서
 * 그런 결과가 새지 않도록 범위를 명시적으로 닫는다.</p>
 *
 * <p><b>한계 고지</b> — ICU 의 천문 근사 계산이 한국천문연구원(KASI) 발표와 극단적인
 * 경계(합삭이 자정 수 초 이내)에서 다를 가능성은 이론상 남는다. 법정 공휴일 판정처럼
 * 공식 음력이 필요한 자리는 KASI 자료(공공데이터 특일 정보 API 등)를 원천으로 쓰고,
 * 이 클래스는 일반 표시 용도로 쓴다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (공통컴포넌트 EgovDateUtil 의 toLunar/toSolar
 *                            착상을 차용하되 재구성 — 원본은 길이가 8이 아닌 입력에 빈 Map 을
 *                            돌려줘 실패가 침묵했고, 반환형이 Map&lt;String,String&gt; 이라 윤달
 *                            정보가 문자열 "0/1" 로 흘렀으며, 존재하지 않는 윤달을 지정해도
 *                            다른 날짜로 미끄러졌고, 무엇보다 한국 음력에 중국 기준
 *                            ChineseCalendar 를 썼다(2000~2026 실측 418일 차이) →
 *                            단기력(Dangi)으로 교체. 코드 이식 아님)
 *  2026.09.07  실행환경팀     입력 연도 범위(1~9999) 검증 추가 — 범위 밖에서 ICU 가 틀린 날짜를
 *                            예외 없이 돌려주거나 내부 필드 오류를 던지던 것을 즉시 실패로 닫음
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovLunarDates {

	/** 지원하는 최소 연도(양력·음력 입력 공통). */
	public static final int MIN_YEAR = 1;

	/** 지원하는 최대 연도(양력·음력 입력 공통). */
	public static final int MAX_YEAR = 9999;

	/**
	 * 단기력의 {@code EXTENDED_YEAR} 와 서기 연도의 차 — 단군기원(檀紀, 기원전 2333년) 기산이다
	 * (2025년 = 단기 4358년).
	 */
	private static final int EXTENDED_YEAR_OFFSET = 2333;

	/** 하루의 밀리초. */
	private static final long MILLIS_PER_DAY = 86_400_000L;

	/** 단기력(Dangi) 달력을 지정하는 로케일. */
	private static final ULocale DANGI_LOCALE = new ULocale("ko_KR@calendar=dangi");

	private EgovLunarDates() {
	}

	/**
	 * UTC 로 고정한 단기력 달력을 만든다 — 벽시계 해석이 JVM 기본 시간대를 따르면
	 * epoch 일수 변환에서 하루가 밀린다(KST 자정 = UTC 전날 15시).
	 * 역법 자체의 천문 기준(한국 자오선)은 달력 구현에 내장되어 있어 이 설정과 무관하다.
	 */
	private static Calendar newCalendar() {
		Calendar lunarCalendar = Calendar.getInstance(DANGI_LOCALE);
		lunarCalendar.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		lunarCalendar.clear();
		return lunarCalendar;
	}

	/**
	 * 양력 날짜를 음력으로 변환한다.
	 *
	 * @param solarDate 양력 날짜(필수, 연도 {@value #MIN_YEAR}~{@value #MAX_YEAR})
	 * @return 음력 날짜(윤달 여부 포함)
	 * @throws IllegalArgumentException {@code null} 이거나 지원 범위 밖의 연도인 경우
	 */
	public static EgovLunarDate toLunar(LocalDate solarDate) {
		if (solarDate == null) {
			throw new IllegalArgumentException("solarDate must not be null");
		}
		requireSupportedYear(solarDate.getYear(), "solarDate");
		return toLunarUnchecked(solarDate);
	}

	/**
	 * 범위 검사 없는 변환 — {@link #toSolar(EgovLunarDate)} 의 역변환 대조에 쓴다
	 * (음력 {@value #MAX_YEAR}년 말은 양력 {@value #MAX_YEAR}+1 년 초에 해당하므로 대조 단계에서
	 * 범위 검사를 하면 정상 입력이 거부된다).
	 */
	private static EgovLunarDate toLunarUnchecked(LocalDate solarDate) {
		Calendar lunarCalendar = newCalendar();
		// GregorianCalendar 경유 없이 epoch 일수로 직접 지정한다(UTC 자정 = UTC 달력 해석).
		lunarCalendar.setTimeInMillis(solarDate.toEpochDay() * MILLIS_PER_DAY);

		int year = lunarCalendar.get(Calendar.EXTENDED_YEAR) - EXTENDED_YEAR_OFFSET;
		int month = lunarCalendar.get(Calendar.MONTH) + 1;
		int day = lunarCalendar.get(Calendar.DAY_OF_MONTH);
		boolean leap = lunarCalendar.get(Calendar.IS_LEAP_MONTH) == 1;

		return leap ? EgovLunarDate.ofLeapMonth(year, month, day) : EgovLunarDate.of(year, month, day);
	}

	/**
	 * 음력 날짜를 양력으로 변환한다.
	 *
	 * <p><b>존재하지 않는 날짜는 예외로 알린다</b> — 없는 윤달({@code 2026년 윤1월} 등)이나
	 * 없는 30일을 지정하면 {@code IllegalArgumentException} 이다. 원본은 이런 입력을
	 * 달력이 임의로 해석한 다른 날짜로 조용히 돌려줬다.</p>
	 *
	 * @param lunarDate 음력 날짜(필수, 연도 {@value #MIN_YEAR}~{@value #MAX_YEAR})
	 * @return 양력 날짜
	 * @throws IllegalArgumentException {@code null} 이거나 지원 범위 밖의 연도이거나
	 *                                  달력에 존재하지 않는 음력 날짜인 경우
	 */
	public static LocalDate toSolar(EgovLunarDate lunarDate) {
		if (lunarDate == null) {
			throw new IllegalArgumentException("lunarDate must not be null");
		}
		requireSupportedYear(lunarDate.getYear(), "lunarDate");
		Calendar lunarCalendar = newCalendar();
		lunarCalendar.set(Calendar.EXTENDED_YEAR, lunarDate.getYear() + EXTENDED_YEAR_OFFSET);
		lunarCalendar.set(Calendar.MONTH, lunarDate.getMonth() - 1);
		lunarCalendar.set(Calendar.DAY_OF_MONTH, lunarDate.getDay());
		lunarCalendar.set(Calendar.IS_LEAP_MONTH, lunarDate.isLeapMonth() ? 1 : 0);

		LocalDate solar = LocalDate.ofEpochDay(Math.floorDiv(lunarCalendar.getTimeInMillis(), MILLIS_PER_DAY));

		// ICU 달력은 lenient 라 없는 날짜를 다른 날로 미끄러뜨린다 — 역변환 대조로 검증한다.
		EgovLunarDate roundTrip = toLunarUnchecked(solar);
		if (!roundTrip.equals(lunarDate)) {
			throw new IllegalArgumentException(
					"lunar date does not exist in the calendar: " + lunarDate
							+ " (resolved to " + roundTrip + ")");
		}
		return solar;
	}

	private static void requireSupportedYear(int year, String name) {
		if (year < MIN_YEAR || year > MAX_YEAR) {
			throw new IllegalArgumentException(
					name + " year must be " + MIN_YEAR + ".." + MAX_YEAR + ": " + year);
		}
	}
}
