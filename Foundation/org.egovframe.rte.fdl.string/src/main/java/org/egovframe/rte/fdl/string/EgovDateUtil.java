/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * 날짜를 처리하는 유틸 클래스
 * 
 * <p><b>NOTE:</b> 날짜와 관련된 여러 기능을 제공하는 유틸이다. 날짜형식 변환 및 날짜
 * 계산 등의 기능을 제공한다.</p>
 * 
 * @author 실행환경 개발팀 윤성종
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	윤성종				최초 생성
 * 2017.02.28	장동한				시큐어코딩(ES)-Null Pointer 역참조[CWE-476]
 * </pre>
 */
public class EgovDateUtil {

    protected EgovDateUtil() {
    }

    /**
     * <p>
     * 특정 날짜를 인자로 받아 그 일자로부터 주어진 기간만큼 추가한 날을 계산하여 문자열로 리턴한다.
     * </p>
     *
     * <pre>
     * String result = DateHelper.getCalcDateAsString("2004", "10", "30", 2, "day");
     * </pre>
     * <p>
     * <code>result</code>는 "20041101"의 값을 갖는다.
     * </p>
     * @param sYearPara 년도
     * @param sMonthPara 월
     * @param sDayPara 일
     * @param iTerm 기간
     * @param sGuBun 구분("day":일에 기간을 더함, "month":월에 기간을 더함, "year":년에 기간을 더함.)
     * @return "년+월+일"
     */
	public static String getCalcDateAsString(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {
		Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));

		addCalender(iTerm, sGuBun, cd);

		return getFormalYear(cd) + getFormalMonth(cd) + getFormalDay(cd);
    }

    /**
     * <p>
     * 특정 날짜를 인자로 받아 그 일자로부터 주어진 기간만큼 추가한 날을 계산하여 년을 리턴한다.
     * </p>
     *
     * <pre>
     * String result=DateHelper.getCalcYearAsString("2004","12","30",2,"day");
     * </pre>
     *
     * <P>
     * result는 "2005"의 값을 갖는다.
     * </p>
     * @param sYearPara 년도
     * @param sMonthPara 월
     * @param sDayPara 일
     * @param iTerm 기간
     * @param sGuBun 구분("day":일에 기간을 더함, "month":월에 기간을 더함, "year":년에 기간을 더함.)
     * @return 년(年)
     */
	public static String getCalcYearAsString(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {

		Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));

		addCalender(iTerm, sGuBun, cd);

		return getFormalYear(cd);
	}

    /**
     * <p>
     * 특정 날짜를 인자로 받아 그 일자로부터 주어진 기간만큼 추가한 날을 계산하여 월을 리턴한다.
     * </p>
     *
     * <pre>
     * String result = DateHelper.getCalcMonthAsString("2004", "12", "30", 2, "day");
     * </pre>
     * <p>
     * <code>result</code>는 "01"의 값을 갖는다.
     * </p>
     * @param sYearPara 년도
     * @param sMonthPara 월
     * @param sDayPara 일
     * @param iTerm 기간
     * @param sGuBun 구분 ("day":일에 기간을 더함, "month":월에 기간을 더함, "year":년에 기간을 더함.)
     * @return 월(月)
     */
	public static String getCalcMonthAsString(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {
		Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));

		addCalender(iTerm, sGuBun, cd);

		return getFormalMonth(cd);
	}

    /**
     * <p>
     * 특정 날짜를 인자로 받아 그 일자로부터 주어진 기간만큼 추가한 날을 계산하여 일을 리턴한다.
     * </p>
     *
     * <pre>
     * String result = DateHelper.getCalcDayAsString("2004", "12", "30", 3, "day");
     * </pre>
     * <p>
     * <code>result</code>는 "02"의 값을 갖는다.
     * </p>
     * @param sYearPara 년도
     * @param sMonthPara 월
     * @param sDayPara 일
     * @param iTerm 기간
     * @param sGuBun 구분("day":일에 기간을 더함, "month":월에 기간을 더함, "year":년에 기간을 더함.)
     * @return 일(日)
     */
	public static String getCalcDayAsString(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {
		Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));

		addCalender(iTerm, sGuBun, cd);

		return getFormalDay(cd);
	}

    /**
     * <p>
     * 특정 날짜를 인자로 받아 그 일자로부터 주어진 기간만큼 추가한 날을 계산하여 년을 리턴한다.
     * </p>
     *
     * <pre>
     * int result = DateHelper.getCalcYearAsInt("2004", "12", "30", 3, "day");
     * </pre>
     * <p>
     * <code>result</code>는 2005의 값을 갖는다.
     * </p>
     * @param sYearPara 년도
     * @param sMonthPara 월
     * @param sDayPara 일
     * @param iTerm 기간
     * @param sGuBun 구분("day":일에 기간을 더함, "month":월에 기간을 더함, "year":년에 기간을 더함.)
     * @return 년(年)
     */
	public static int getCalcYearAsInt(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {
		Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));

		addCalender(iTerm, sGuBun, cd);

		return cd.get(Calendar.YEAR);
	}

    /**
     * <p>
     * 특정 날짜를 인자로 받아 그 일자로부터 주어일 기간만큼 추가한 날을 계산하여 월을 리턴한다.
     * </p>
     *
     * <pre>
     * int result = DateHelper.getCalcMonthAsInt("2004", "12", "30", 3, "day");
     * </pre>
     * <p>
     * <code>result</code>는 1의 값을 갖는다.
     * </p>
     * @param sYearPara 년도
     * @param sMonthPara 월
     * @param sDayPara 일
     * @param iTerm 기간
     * @param sGuBun 구분("day":일에 기간을 더함, "month":월에 기간을 더함, "year":년에 기간을 더함.)
     * @return 월(月)
     */
	public static int getCalcMonthAsInt(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {
		Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));

		addCalender(iTerm, sGuBun, cd);

		return cd.get(Calendar.MONTH) + 1;
	}

    /**
     * <p>
     * 특정 날짜를 인자로 받아 그 일자로부터 주어일 기간만큼 추가한 날을 계산하여 일을 리턴한다.
     * </p>
     *
     * <pre>
     * int result = DateHelper.getCalcDayAsInt("2004", "12", "30", 3, "day");
     * </pre>
     * <p>
     * <code>result</code>는 2의 값을 갖는다.
     * </p>
     * @param sYearPara 년도
     * @param sMonthPara 월
     * @param sDayPara 일
     * @param iTerm 기간
     * @param sGuBun 구분("day":일에 기간을 더함, "month":월에 기간을 더함, "year":년에 기간을 더함.)
     * @return 일(日)
     */
	public static int getCalcDayAsInt(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {
		Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));

		addCalender(iTerm, sGuBun, cd);

		return cd.get(Calendar.DAY_OF_MONTH);
	}

    /**
	 * sGuBun의 종류에 따라 년/월/일 캘린더에 추가
	 * @param iTerm
	 * @param sGuBun
	 * @param cd
	 */
    private static void addCalender(int iTerm, String sGuBun, Calendar cd) {
		if (EgovStringUtil.equals(sGuBun, "day")) {
			cd.add(Calendar.DATE, iTerm);
		} else if (EgovStringUtil.equals(sGuBun, "month")) {
			cd.add(Calendar.MONTH, iTerm);
		} else if (EgovStringUtil.equals(sGuBun, "year")) {
			cd.add(Calendar.YEAR, iTerm);
		}
	}

    /**
     * <p>
     * 현재 연도값을 리턴
     * </p>
     * @return 년(年)
     */
    public static int getCurrentYearAsInt() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return cd.get(Calendar.YEAR);
    }

    /**
     * <p>
     * 현재 월을 리턴
     * </p>
     * @return 월(月)
     */
    public static int getCurrentMonthAsInt() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return cd.get(Calendar.MONTH) + 1;
    }

    /**
     * <p>
     * 현재 일을 리턴
     * </p>
     * @return 일(日)
     */
    public static int getCurrentDayAsInt() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return cd.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * <p>
     * 현재 시간을 리턴
     * </p>
     * @return 시(時)
     */
    public static int getCurrentHourAsInt() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return cd.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * <p>
     * 현재 분을 리턴
     * </p>
     * @return 분(分)
     */
    public static int getCurrentMinuteAsInt() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return cd.get(Calendar.MINUTE);
    }

    /**
     * <p>
     * 현재 초를 리턴
     * </p>
     * @return 밀리초
     */
    public static int getCurrentMilliSecondAsInt() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return cd.get(Calendar.MILLISECOND);
    }

    /**
     * <p>
     * 현재 년도를 YYYY 형태로 리턴
     * </p>
     * @return 년도(YYYY)
     */
    public static String getCurrentYearAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalYear(cd);
    }

    /**
     * <P>
     * 현재 월을 MM 형태로 리턴
     * </p>
     * @return 월(MM)
     */
    public static String getCurrentMonthAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalMonth(cd);
    }

    /**
     * <p>
     * 현재 일을 DD 형태로 리턴
     * </p>
     * @return 일(DD)
     */
    public static String getCurrentDayAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalDay(cd);
    }

    /**
     * <p>
     * 현재 시간을 HH 형태로 리턴
     * </p>
     * @return 시간(HH)
     */
    public static String getCurrentHourAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalHour(cd);
    }

    /**
     * <p>
     * 현재 분을 mm 형태로 리턴
     * </p>
     * @return 분(mm)
     */
    public static String getCurrentMinuteAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalMin(cd);
    }

    /**
     * <p>
     * 현재 초를 ss 형태로 리턴
     * </p>
     * @return 초(ss)
     */
    public static String getCurrentSecondAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalSec(cd);
    }

    /**
     * <p>
     * 현재 밀리초를 sss 형태로 리턴
     * </p>
     * @return 밀리초(sss)
     */
    public static String getCurrentMilliSecondAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalMSec(cd);
    }

    /**
     * <p>
     * 현재 날짜를 년월일을 합쳐서 String으로 리턴하는 메소드
     * </p>
     * @return 년+월+일 값
     */
    public static String getCurrentDateAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalYear(cd) + getFormalMonth(cd) + getFormalDay(cd);
    }

    /**
     * <p>
     * 현재 시간을 시분초를 합쳐서 String으로 리턴하는 메소드
     * </p>
     * @return 시+분+초 값
     */
    public static String getCurrentTimeAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalHour(cd) + getFormalMin(cd) + getFormalSec(cd);
    }

    /**
     * <p>
     * 현재 날짜와 시간을 년월일시분초를 합쳐서 String으로 리턴하는 메소드
     * </p>
     * @return 년+월+일+시+분+초 값
     */
    public static String getCurrentDateTimeAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalYear(cd) + getFormalMonth(cd) + getFormalDay(cd) + getFormalHour(cd) + getFormalMin(cd) + getFormalSec(cd);
    }

    /**
     * <p>
     * 해당 년,월,일을 받아 요일을 리턴하는 메소드
     * </p>
     * @param sYear 년도
     * @param sMonth 월
     * @param sDay 일
     * @return 요일(한글)
     */
	public static String getDayOfWeekAsString(String sYear, String sMonth, String sDay) {
		Calendar cd = new GregorianCalendar(Integer.parseInt(sYear), Integer.parseInt(sMonth) - 1, Integer.parseInt(sDay));
		SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.KOREA);	// "EEE" - Day in Week
        Date d1 = cd.getTime();
        return sdf.format(d1);
    }

    /**
     * <p>
     * 해당 대상자에 대해 기준일자에서의 만 나이를 구한다.
     * </p>
     *
     * <pre>
     * int age = DateHelper.getFullAge("7701011234567", "20041021");
     * </pre>
     * <p>
     * <code>age</code>는 27의 값을 갖는다.
     * </p>
     * @param socialNo 주민번호 13자리
     * @param keyDate 기준일자 8자리
     * @return 만 나이
     */
	public static int getFullAge(String socialNo, String keyDate) {
		String birthDate = null;

		// 주민번호 7번째 자리가 0 또는 9 라면 1800년도 출생이다.
		if (EgovStringUtil.equals(EgovStringUtil.toSubString(socialNo, 6, 7), "0") || EgovStringUtil.equals(EgovStringUtil.toSubString(socialNo, 6, 7), "9")) {
			birthDate = "18" + socialNo.substring(0, 6);
		}

		// 주민번호 7번째 자리가 1 또는 2 라면 1900년도 출생이다.
		else if (EgovStringUtil.equals(EgovStringUtil.toSubString(socialNo, 6, 7), "1") || EgovStringUtil.equals(EgovStringUtil.toSubString(socialNo, 6, 7), "2")) {
			birthDate = "19" + socialNo.substring(0, 6);
		}

		// 주민번호 7번째 자리가 3 또는 4 라면 2000년도 출생이다.
		else if (EgovStringUtil.equals(EgovStringUtil.toSubString(socialNo, 6, 7), "3") || EgovStringUtil.equals(EgovStringUtil.toSubString(socialNo, 6, 7), "4")) {
			birthDate = "20" + socialNo.substring(0, 6);
		}

		//2017.02.28 장동한 시큐어코딩(ES)-Null Pointer 역참조[CWE-476]
		if(keyDate != null && birthDate != null){
			// 생일이 안지났을때 기준일자 년에서 생일년을 빼고 1년을 더뺀다.
			if (Integer.parseInt(keyDate.substring(4, 8)) < Integer.parseInt(birthDate.substring(4, 8))) {
				return Integer.parseInt(keyDate.substring(0, 4)) - Integer.parseInt(birthDate.substring(0, 4)) - 1;
			// 생일이 지났을때 기준일자 년에서 생일년을 뺀다.
			} else {
				return Integer.parseInt(keyDate.substring(0, 4)) - Integer.parseInt(birthDate.substring(0, 4));
			}
		}else{
			return 0;
		}
	}

    /**
     * <p>
     * 주민번호를 넘겨 현재 시점의 만 나이를 구한다.
     * </p>
     * @param socialNo 주민번호 6자리
     * @return 만 나이
     */
    public static int getCurrentFullAge(String socialNo) {
        // 현재일자를 구한다.
		String sCurrentDate = getCurrentYearAsString() + getCurrentMonthAsString() + getCurrentDayAsString();
        return getFullAge(socialNo, sCurrentDate);
    }

    /**
     * <p>
     * 해당 년의 특정월의 일자를 구한다.
     * </p>
     * @param year 년도4자리
     * @param month 월 1자리 또는 2자리
     * @return 특정월의 일자
     */
    public static int getDayCountForMonth(int year, int month) {
        int[] DOMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 }; // 평년
        int[] lDOMonth = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 }; // 윤년

		if ((year % 4) == 0) {
			if ((year % 100) == 0 && (year % 400) != 0) {
				return DOMonth[month - 1];
			}
			return lDOMonth[month - 1];
		} else {
			return DOMonth[month - 1];
		}
    }

    /**
     * <p>
     * 8자리로된(yyyyMMdd) 시작일자와 종료일자 사이의 일수를 구함.
     * </p>
     * @param from 8자리로된(yyyyMMdd)시작일자
     * @param to 8자리로된(yyyyMMdd)종료일자
     * @return 날짜 형식이 맞고, 존재하는 날짜일 때 2개 일자 사이의 일수 리턴
     * @throws ParseException 형식이 잘못 되었거나 존재하지 않는 날짜인 경우 발생함
     */
    public static int getDayCount(String from, String to) throws ParseException {
        return getDayCountWithFormatter(from, to, "yyyyMMdd");
    }

    /**
     * <p>
     * 해당 문자열이 "yyyyMMdd" 형식에 합당한지 여부를 판단하여 합당하면 Date
     * 객체를 리턴한다.
     * </p>
     * @param source 대상 문자열
     * @return "yyyyMMdd" 형식에 맞는 Date 객체를 리턴한다.
     * @throws ParseException 형식이 잘못 되었거나 존재하지 않는 날짜인 경우 발생함
     */
    public static Date dateFormatCheck(String source) throws ParseException {
        return dateFormatCheck(source, "yyyyMMdd");
    }

    /**
     * <p>
     * 해당 문자열이 주어진 일자 형식을 준수하는지 여부를 검사한다.
     * </p>
     * @param source 검사할 대상 문자열
     * @param format Date 형식의 표현. 예) "yyyy-MM-dd".
     * @return 형식에 합당하는 경우 Date 객체를 리턴한다.
     * @throws ParseException 형식이 잘못 되었거나 존재하지 않는 날짜인 경우 발생함
     */
	public static Date dateFormatCheck(String source, String format) throws ParseException {
		if (source == null) {
			throw new ParseException("date string to check is null", 0);
		}

		if (format == null) {
			throw new ParseException("format string to check date is null", 0);
		}

		SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.KOREA);
		Date date = null;
		try {
			date = formatter.parse(source);
		} catch (ParseException e) {
			throw new ParseException(" wrong date:\"" + source + "\" with format \"" + format + "\"", 0);
		}

		if (!formatter.format(date).equals(source)) {
			throw new ParseException("Out of bound date:\"" + source + "\" with format \"" + format + "\"", 0);
		}

		return date;
	}

    /**
     * <p>
     * 정해진 일자 형식을 기반으로 시작일자와 종료일자 사이의 일자를 구한다.
     * </p>
     * @param from 시작 일자
     * @param to 종료 일자
     * @return 날짜 형식이 맞고, 존재하는 날짜일 때 2개 일자 사이의 일수를 리턴
     * @throws ParseException 형식이 잘못 되었거나 존재하지 않는 날짜인 경우 발생함
     * @see #getTimeCount(String, String, String)
     */
	public static int getDayCountWithFormatter(String from, String to, String format) throws ParseException {
		long duration = getTimeCount(from, to, format);
		return (int) (duration / (1000 * 60 * 60 * 24));
	}

    /**
     * <p>
     * DATE 문자열을 이용한 format string을 생성
     * </p>
     * @param date Date 문자열
     * @return Java.text.DateFormat 부분의 정규 표현 문자열
     * @throws ParseException 형식이 잘못 되었거나 존재하지 않는 날짜인 경우 발생함
     */
	protected static String getFormatStringWithDate(String date) throws ParseException {
		String format = null;

		if (date.length() == 4) {
			format = "HHmm";
		} else if (date.length() == 8) {
			format = "yyyyMMdd";
		} else if (date.length() == 12) {
			format = "yyyyMMddHHmm";
		} else if (date.length() == 14) {
			format = "yyyyMMddHHmmss";
		} else if (date.length() == 17) {
			format = "yyyyMMddHHmmssSSS";
		} else {
			throw new ParseException(" wrong date format!:\"" + format + "\"", 0);
		}

		return format;
	}

    /**
     * <p>
     * <code>yyyyMMddHHmmssSSS</code> 와 같은 Format 문자열 없이 시작 일자 시간, 끝 일자 시간을
     * </p>
     * @param from 시작일자
     * @param to 끝일자
     * @return 두 일자 간의 차의 밀리초(long)값
     * @throws ParseException 형식이 잘못 되었거나 존재하지 않는 날짜인 경우 발생함
     * @see #getFormatStringWithDate(String)
     */
	public static long getTimeCount(String from, String to) throws ParseException {
		String format = getFormatStringWithDate(from);
		return getTimeCount(from, to, format);
	}

    /**
     * <p>
     * 정해진 일자 형식을 기반으로 시작일자와 종료일자 사이의 일자를 구한다.
     * </p>
     *
     * <pre>
     * Symbol		Meaning		 				Presentation			Example
     * -----------------------------------------------------------------------------
     * G			era designator				(Text)					AD
     * y			year						(Number)				1996
     * M			month in year				(Text & Number)			July & 07
     * d			day in month				(Number)				10
     * h			hour in am/pm (1~12)		(Number)				12
     * H			hour in day (0~23)			(Number)				0
     * m			minute in hour				(Number)				30
     * s			second in minute			(Number)				55
     * S			millisecond					(Number)				978
     * E			day in week					(Text)					Tuesday
     * D			day in year					(Number)				189
     * F			day of week in month		(Number)				2 (2nd Wed in July)
     * w			week in year				(Number)				27
     * W			week in month				(Number)				2
     * a			am/pm marker				(Text)					PM
     * k			hour in day (1~24)			(Number)				24
     * K			hour in am/pm (0~11)		(Number)				0
     * z			time zone					(Text)					Pacific Standard Time
     * '			escape for text				(Delimiter)
     * ''			single quote				(Literal)					 '
     * </pre>
     * @param from 시작 일자
     * @param to 종료 일자
     * @param format
     * @return 날짜 형식이 맞고, 존재하는 날짜일 때 2개 일자 사이의 일수를 리턴
     * @throws ParseException 형식이 잘못 되었거나 존재하지 않는 날짜인 경우 발생함
     */
	public static long getTimeCount(String from, String to, String format) throws ParseException {
		Date d1 = dateFormatCheck(from, format);
		Date d2 = dateFormatCheck(to, format);
		long duration = d2.getTime() - d1.getTime();
		return duration;
	}

    /**
     * <p>
     * 시작일자와 종료일자 사이의 해당 요일이 몇번 있는지 계산한다.
     * </p>
     * @param from 시작 일자
     * @param to 종료 일자
     * @param yoil 요일
     * @return 날짜 형식이 맞고, 존재하는 날짜일 때 2개 일자 사이의 일자 리턴
     * @throws ParseException 발생. 형식이 잘못 되었거나 존재하지 않는 날짜
     */
	public static int getDayOfWeekCount(String from, String to, String yoil) throws ParseException {
		int first = 0; // from 날짜로 부터 며칠 후에 해당 요일인지에 대한
		int count = 0; // 해당 요일이 반복된 횟수
		String[] sYoil = { "일", "월", "화", "수", "목", "금", "토" };

		// 두 일자 사이의 날 수
		int betweenDays = getDayCount(from, to);

		// 첫번째 일자에 대한 요일
		Calendar cd = new GregorianCalendar(Integer.parseInt(from.substring(0, 4)), Integer.parseInt(from.substring(4, 6)) - 1, Integer.parseInt(from.substring(6, 8)));
		int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK);

		// 요일이 3자리이면 첫자리만 취한다.
		if (yoil.length() == 3) {
			yoil = yoil.substring(0, 1);
		}

		while (!sYoil[(dayOfWeek - 1) % 7].equals(yoil)) {
			dayOfWeek += 1;
			first++;
		}

		if ((betweenDays - first) < 0) {
			return 0;
		} else {
			count++;
		}

		count += (betweenDays - first) / 7;

		return count;
	}

    /**
     * <p>
     * 년도 표시를 네자리로 형식화 한다.
     * </p>
     * @param cd 년도를 포함하는 <strong>Calendar</strong> 오브젝트
     * @return 네자리로 형식화된 년도
     */
	private static String getFormalYear(Calendar cd) {
		return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.YEAR)), 4, '0');
	}

    /**
     * <p>
     * 월(Month) 표시를 두자리로 형식화 한다.
     * </p>
     * @param cd  월을 포함하는 <strong>Calendar</strong> 오브젝트
     * @return 두자리로 형식화된 월
     */
	private static String getFormalMonth(Calendar cd) {
		return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.MONTH) + 1), 2, '0');
	}

    /**
     * <p>
     * 일(Day) 표시를 두자리로 형식화 한다.
     * </p>
     * @param cd 일자를 포함하는 <strong>Calendar</strong> 오브젝트
     * @return 두자리로 형식화된 일
     */
	private static String getFormalDay(Calendar cd) {
		return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.DAY_OF_MONTH)), 2, '0');
	}

    /**
     * <p>
     * 시간(Hour) 표시를 두자리로 형식화 한다.
     * </p>
     * @param cd 시간을 포함하는 <strong>Calendar</strong> 오브젝트
     * @return 두자리로 형식화된 시간
     */
	private static String getFormalHour(Calendar cd) {
		return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.HOUR_OF_DAY)), 2, '0');
	}

    /**
     * <p>
     * 분(Minute) 표시를 두자리로 형식화 한다.
     * </p>
     * @param cd 분을 포함하는 <strong>Calendar</strong> 오브젝트
     * @return 두자리로 형식화된 분
     */
	private static String getFormalMin(Calendar cd) {
		return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.MINUTE)), 2, '0');
	}

    /**
     * <p>
     * 초(sec) 표시를 두자리로 형식화 한다.
     * </p>
     * @param cd 초를 포함하는 <strong>Calendar</strong> 오브젝트
     * @return 두자리로 형식화된 초
     */
	private static String getFormalSec(Calendar cd) {
		return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.SECOND)), 2, '0');
	}

    /**
     * <p>
     * 밀리초(millisec) 표시를 세자리로 형식화 한다.
     * </p>
     * @param cd 밀리초를 포함하는 <strong>Calendar</strong> 오브젝트
     * @return 세자리로 형식화된 밀리초
     */
	private static String getFormalMSec(Calendar cd) {
		return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.MILLISECOND)), 3, '0');
	}

    /**
     * <p>
     * Date -> String
     * </p>
     * @param date Date which you want to change.
     * @return String The Date string. Type, yyyyMMdd HH:mm:ss.
     */
	public static String toString(Date date, String format, Locale locale) {
		if (EgovStringUtil.isNull(format)) {
			format = "yyyy-MM-dd HH:mm:ss";
		}

		if (EgovObjectUtil.isNull(locale)) {
			locale = java.util.Locale.KOREA;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
		String tmp = sdf.format(date);
		return tmp;
	}

}
