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

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * 로케일에 맞는 숫자·통화·백분율·날짜 형식화 유틸 클래스.
 *
 * <p><b>NOTE:</b> JDK 의 {@link NumberFormat}·{@link DateTimeFormatter} 를 얇게 감싼 것이다 —
 * 인스턴스 생성·스타일 상수·스레드 안전성({@code NumberFormat} 은 공유 불가)의 잔손질을
 * 줄이는 것이 전부이며, 그 이상을 감추지 않는다. 형식을 세밀히 제어할 자리는 JDK API 를
 * 직접 쓴다.</p>
 *
 * <pre>
 * EgovLocaleFormats.number(1234567.89)                    → "1,234,567.89"
 * EgovLocaleFormats.currency(50000, Locale.KOREA)         → "₩50,000"
 * EgovLocaleFormats.percent(0.756)                        → "76%"
 * EgovLocaleFormats.date(LocalDate.now(), FormatStyle.LONG, Locale.KOREA)
 *                                                         → "2026년 9월 1일"
 * </pre>
 *
 * <p><b>계약</b> — 값이 {@code null} 이면 빈 문자열을 돌려준다(화면 표시용 — 예외로 화면을
 * 깨뜨리지 않는다). 로케일 인자를 생략하면 JVM 기본 로케일이다.</p>
 *
 * <p>날짜는 {@code java.time} 만 받는다 — 공통컴포넌트 원본(java.util.Date 기반)에서 옮겨올
 * 때는 {@code date.toInstant().atZone(zone).toLocalDate()} 로 변환해 넘긴다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (공통컴포넌트 EgovNumberFormat·EgovDateFormat 의
 *                            역할을 차용하되 java.time 기반으로 재작성 — 원본은 Date·int
 *                            스타일 상수 기반이었고 통화·백분율까지 오버로드 22개로 흩어져
 *                            있었다. 코드 이식 아님)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovLocaleFormats {

	private EgovLocaleFormats() {
	}

	/**
	 * 자릿수 구분이 든 숫자 형식({@code 1,234,567.89})으로 만든다 — JVM 기본 로케일.
	 *
	 * @param value 값({@code null} 이면 빈 문자열)
	 * @return 형식화된 문자열
	 */
	public static String number(Number value) {
		return number(value, Locale.getDefault());
	}

	/**
	 * 자릿수 구분이 든 숫자 형식으로 만든다.
	 *
	 * @param value  값({@code null} 이면 빈 문자열)
	 * @param locale 로케일(필수)
	 * @return 형식화된 문자열
	 */
	public static String number(Number value, Locale locale) {
		return format(value, NumberFormat.getNumberInstance(requireLocale(locale)));
	}

	/**
	 * 통화 형식({@code ₩50,000})으로 만든다 — JVM 기본 로케일.
	 *
	 * @param value 값({@code null} 이면 빈 문자열)
	 * @return 형식화된 문자열
	 */
	public static String currency(Number value) {
		return currency(value, Locale.getDefault());
	}

	/**
	 * 통화 형식으로 만든다 — 통화 기호·자릿수가 로케일을 따른다.
	 *
	 * @param value  값({@code null} 이면 빈 문자열)
	 * @param locale 로케일(필수)
	 * @return 형식화된 문자열
	 */
	public static String currency(Number value, Locale locale) {
		return format(value, NumberFormat.getCurrencyInstance(requireLocale(locale)));
	}

	/**
	 * 백분율 형식({@code 0.756} → {@code 76%})으로 만든다 — JVM 기본 로케일.
	 *
	 * <p>값은 <b>비율</b>이다 — {@code 76} 을 넘기면 {@code 7,600%} 가 된다.</p>
	 *
	 * @param ratio 비율({@code null} 이면 빈 문자열)
	 * @return 형식화된 문자열
	 */
	public static String percent(Number ratio) {
		return percent(ratio, Locale.getDefault());
	}

	/**
	 * 백분율 형식으로 만든다.
	 *
	 * @param ratio  비율({@code null} 이면 빈 문자열)
	 * @param locale 로케일(필수)
	 * @return 형식화된 문자열
	 */
	public static String percent(Number ratio, Locale locale) {
		return format(ratio, NumberFormat.getPercentInstance(requireLocale(locale)));
	}

	/**
	 * 로케일 관례의 날짜 문자열로 만든다 — JVM 기본 로케일 · {@link FormatStyle#MEDIUM}.
	 *
	 * @param date 날짜({@code null} 이면 빈 문자열)
	 * @return 형식화된 문자열
	 */
	public static String date(LocalDate date) {
		return date(date, FormatStyle.MEDIUM, Locale.getDefault());
	}

	/**
	 * 로케일 관례의 날짜 문자열로 만든다({@code 2026년 9월 1일} · {@code Sep 1, 2026}).
	 *
	 * @param date   날짜({@code null} 이면 빈 문자열)
	 * @param style  길이({@link FormatStyle#SHORT}~{@link FormatStyle#FULL}, 필수)
	 * @param locale 로케일(필수)
	 * @return 형식화된 문자열
	 */
	public static String date(LocalDate date, FormatStyle style, Locale locale) {
		if (date == null) {
			return "";
		}
		requireStyle(style);
		return date.format(DateTimeFormatter.ofLocalizedDate(style).withLocale(requireLocale(locale)));
	}

	/**
	 * 로케일 관례의 일시 문자열로 만든다 — JVM 기본 로케일 · {@link FormatStyle#MEDIUM}.
	 *
	 * @param dateTime 일시({@code null} 이면 빈 문자열)
	 * @return 형식화된 문자열
	 */
	public static String dateTime(LocalDateTime dateTime) {
		return dateTime(dateTime, FormatStyle.MEDIUM, Locale.getDefault());
	}

	/**
	 * 로케일 관례의 일시 문자열로 만든다.
	 *
	 * <p>{@link FormatStyle#LONG}·{@link FormatStyle#FULL} 은 시간대 표기가 필요해
	 * {@link LocalDateTime} 으로는 형식화할 수 없다 — SHORT·MEDIUM 을 쓴다.</p>
	 *
	 * @param dateTime 일시({@code null} 이면 빈 문자열)
	 * @param style    길이({@link FormatStyle#SHORT} 또는 {@link FormatStyle#MEDIUM}, 필수)
	 * @param locale   로케일(필수)
	 * @return 형식화된 문자열
	 */
	public static String dateTime(LocalDateTime dateTime, FormatStyle style, Locale locale) {
		if (dateTime == null) {
			return "";
		}
		requireStyle(style);
		return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(style).withLocale(requireLocale(locale)));
	}

	private static String format(Number value, NumberFormat numberFormat) {
		// NumberFormat 은 스레드 안전하지 않다 — 매번 새 인스턴스를 받아 공유 문제를 차단한다.
		return (value == null) ? "" : numberFormat.format(value);
	}

	private static Locale requireLocale(Locale locale) {
		if (locale == null) {
			throw new IllegalArgumentException("locale must not be null");
		}
		return locale;
	}

	private static void requireStyle(FormatStyle style) {
		if (style == null) {
			throw new IllegalArgumentException("style must not be null");
		}
	}
}
