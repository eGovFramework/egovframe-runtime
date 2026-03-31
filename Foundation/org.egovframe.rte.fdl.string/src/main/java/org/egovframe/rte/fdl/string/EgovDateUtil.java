/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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
 * 날짜와 관련된 여러 기능을 제공하는 유틸이다. 날짜형식 변환 및 날짜 계산 등의 기능을 제공한다.
 * <p>
 * 개정이력(Modification Information)
 * <p>
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	윤성종            최초 생성
 * 2017.02.28	장동한            시큐어코딩(ES)-Null Pointer 역참조[CWE-476]
 * 2023.08.31   유지보수            코드 리팩토링(addCalendar(), Contribution 반영)
 */
public class EgovDateUtil {

    protected EgovDateUtil() {
    }

    /**
     * 특정 날짜를 인자로 받아 그 일자로부터 주어진 기간만큼 추가한 날을 계산하여 문자열로 리턴한다.
     */
    public static String getCalcDateAsString(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {
        Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));
        addCalendar(iTerm, sGuBun, cd);
        return getFormalYear(cd) + getFormalMonth(cd) + getFormalDay(cd);
    }

    /**
     * 특정 날짜를 인자로 받아 그 일자로부터 주어진 기간만큼 추가한 날을 계산하여 년을 리턴한다.
     */
    public static String getCalcYearAsString(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {
        Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));
        addCalendar(iTerm, sGuBun, cd);
        return getFormalYear(cd);
    }

    /**
     * 특정 날짜를 인자로 받아 그 일자로부터 주어진 기간만큼 추가한 날을 계산하여 월을 리턴한다.
     */
    public static String getCalcMonthAsString(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {
        Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));
        addCalendar(iTerm, sGuBun, cd);
        return getFormalMonth(cd);
    }

    /**
     * 특정 날짜를 인자로 받아 그 일자로부터 주어진 기간만큼 추가한 날을 계산하여 일을 리턴한다.
     */
    public static String getCalcDayAsString(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {
        Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));
        addCalendar(iTerm, sGuBun, cd);
        return getFormalDay(cd);
    }

    /**
     * 특정 날짜를 인자로 받아 그 일자로부터 주어진 기간만큼 추가한 날을 계산하여 년을 리턴한다.
     */
    public static int getCalcYearAsInt(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {
        Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));
        addCalendar(iTerm, sGuBun, cd);
        return cd.get(Calendar.YEAR);
    }

    /**
     * 특정 날짜를 인자로 받아 그 일자로부터 주어일 기간만큼 추가한 날을 계산하여 월을 리턴한다.
     */
    public static int getCalcMonthAsInt(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {
        Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));
        addCalendar(iTerm, sGuBun, cd);
        return cd.get(Calendar.MONTH) + 1;
    }

    /**
     * 특정 날짜를 인자로 받아 그 일자로부터 주어일 기간만큼 추가한 날을 계산하여 일을 리턴한다.
     */
    public static int getCalcDayAsInt(String sYearPara, String sMonthPara, String sDayPara, int iTerm, String sGuBun) {
        Calendar cd = new GregorianCalendar(Integer.parseInt(sYearPara), Integer.parseInt(sMonthPara) - 1, Integer.parseInt(sDayPara));
        addCalendar(iTerm, sGuBun, cd);
        return cd.get(Calendar.DAY_OF_MONTH);
    }

    private static void addCalendar(int iTerm, String sGuBun, Calendar cd) {
        if (EgovStringUtil.equals(sGuBun, "day")) {
            cd.add(Calendar.DATE, iTerm);
        } else if (EgovStringUtil.equals(sGuBun, "month")) {
            cd.add(Calendar.MONTH, iTerm);
        } else if (EgovStringUtil.equals(sGuBun, "year")) {
            cd.add(Calendar.YEAR, iTerm);
        }
    }

    /**
     * 현재 연도값을 리턴
     */
    public static int getCurrentYearAsInt() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return cd.get(Calendar.YEAR);
    }

    /**
     * 현재 월을 리턴
     */
    public static int getCurrentMonthAsInt() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return cd.get(Calendar.MONTH) + 1;
    }

    /**
     * 현재 일을 리턴
     */
    public static int getCurrentDayAsInt() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return cd.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 현재 시간을 리턴
     */
    public static int getCurrentHourAsInt() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return cd.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 현재 분을 리턴
     */
    public static int getCurrentMinuteAsInt() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return cd.get(Calendar.MINUTE);
    }

    /**
     * 현재 초를 리턴
     */
    public static int getCurrentMilliSecondAsInt() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return cd.get(Calendar.MILLISECOND);
    }

    /**
     * 현재 년도를 YYYY 형태로 리턴
     */
    public static String getCurrentYearAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalYear(cd);
    }

    /**
     * 현재 월을 MM 형태로 리턴
     */
    public static String getCurrentMonthAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalMonth(cd);
    }

    /**
     * 현재 일을 DD 형태로 리턴
     */
    public static String getCurrentDayAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalDay(cd);
    }

    /**
     * 현재 시간을 HH 형태로 리턴
     */
    public static String getCurrentHourAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalHour(cd);
    }

    /**
     * 현재 분을 mm 형태로 리턴
     */
    public static String getCurrentMinuteAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalMin(cd);
    }

    /**
     * 현재 초를 ss 형태로 리턴
     */
    public static String getCurrentSecondAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalSec(cd);
    }

    /**
     * 현재 밀리초를 sss 형태로 리턴
     */
    public static String getCurrentMilliSecondAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalMSec(cd);
    }

    /**
     * 현재 날짜를 년월일을 합쳐서 String으로 리턴하는 메소드
     */
    public static String getCurrentDateAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalYear(cd) + getFormalMonth(cd) + getFormalDay(cd);
    }

    /**
     * 현재 시간을 시분초를 합쳐서 String으로 리턴하는 메소드
     */
    public static String getCurrentTimeAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalHour(cd) + getFormalMin(cd) + getFormalSec(cd);
    }

    /**
     * 현재 날짜와 시간을 년월일시분초를 합쳐서 String으로 리턴하는 메소드
     */
    public static String getCurrentDateTimeAsString() {
        Calendar cd = new GregorianCalendar(Locale.KOREA);
        return getFormalYear(cd) + getFormalMonth(cd) + getFormalDay(cd) + getFormalHour(cd) + getFormalMin(cd) + getFormalSec(cd);
    }

    /**
     * 해당 년,월,일을 받아 요일을 리턴하는 메소드
     */
    public static String getDayOfWeekAsString(String sYear, String sMonth, String sDay) {
        Calendar cd = new GregorianCalendar(Integer.parseInt(sYear), Integer.parseInt(sMonth) - 1, Integer.parseInt(sDay));
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.KOREA);    // "EEE" - Day in Week
        Date d1 = cd.getTime();
        return sdf.format(d1);
    }

    /**
     * 해당 대상자에 대해 기준일자에서의 만 나이를 구한다.
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
        if (keyDate != null && birthDate != null) {
            // 생일이 안지났을때 기준일자 년에서 생일년을 빼고 1년을 더뺀다.
            if (Integer.parseInt(keyDate.substring(4, 8)) < Integer.parseInt(birthDate.substring(4, 8))) {
                return Integer.parseInt(keyDate.substring(0, 4)) - Integer.parseInt(birthDate.substring(0, 4)) - 1;
                // 생일이 지났을때 기준일자 년에서 생일년을 뺀다.
            } else {
                return Integer.parseInt(keyDate.substring(0, 4)) - Integer.parseInt(birthDate.substring(0, 4));
            }
        } else {
            return 0;
        }
    }

    /**
     * 주민번호를 넘겨 현재 시점의 만 나이를 구한다.
     */
    public static int getCurrentFullAge(String socialNo) {
        String sCurrentDate = getCurrentYearAsString() + getCurrentMonthAsString() + getCurrentDayAsString();
        return getFullAge(socialNo, sCurrentDate);
    }

    /**
     * 해당 년의 특정월의 일자를 구한다.
     */
    public static int getDayCountForMonth(int year, int month) {
        int[] DOMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}; // 평년
        int[] lDOMonth = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}; // 윤년

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
     * 8자리로된(yyyyMMdd) 시작일자와 종료일자 사이의 일수를 구함.
     */
    public static int getDayCount(String from, String to) throws ParseException {
        return getDayCountWithFormatter(from, to, "yyyyMMdd");
    }

    /**
     * 해당 문자열이 "yyyyMMdd" 형식에 합당한지 여부를 판단하여 합당하면 Date 객체를 리턴한다.
     */
    public static Date dateFormatCheck(String source) throws ParseException {
        return dateFormatCheck(source, "yyyyMMdd");
    }

    /**
     * 해당 문자열이 주어진 일자 형식을 준수하는지 여부를 검사한다.
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
     * 정해진 일자 형식을 기반으로 시작일자와 종료일자 사이의 일자를 구한다.
     */
    public static int getDayCountWithFormatter(String from, String to, String format) throws ParseException {
        long duration = getTimeCount(from, to, format);
        return (int) (duration / (1000 * 60 * 60 * 24));
    }

    /**
     * DATE 문자열을 이용한 format string을 생성
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
     * yyyyMMddHHmmssSSS와 같은 Format 문자열 없이 시작 일자 시간, 끝 일자 시간을 구한다.
     */
    public static long getTimeCount(String from, String to) throws ParseException {
        String format = getFormatStringWithDate(from);
        return getTimeCount(from, to, format);
    }

    /**
     * 정해진 일자 형식을 기반으로 시작일자와 종료일자 사이의 일자를 구한다.
     */
    public static long getTimeCount(String from, String to, String format) throws ParseException {
        Date d1 = dateFormatCheck(from, format);
        Date d2 = dateFormatCheck(to, format);
        return d2.getTime() - d1.getTime();
    }

    /**
     * 시작일자와 종료일자 사이의 해당 요일이 몇번 있는지 계산한다.
     */
    public static int getDayOfWeekCount(String from, String to, String yoil) throws ParseException {
        int first = 0; // from 날짜로 부터 며칠 후에 해당 요일인지에 대한
        int count = 0; // 해당 요일이 반복된 횟수
        String[] sYoil = {"일", "월", "화", "수", "목", "금", "토"};

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
     * 년도 표시를 네자리로 형식화 한다.
     */
    private static String getFormalYear(Calendar cd) {
        return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.YEAR)), 4, '0');
    }

    /**
     * 월(Month) 표시를 두자리로 형식화 한다.
     */
    private static String getFormalMonth(Calendar cd) {
        return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.MONTH) + 1), 2, '0');
    }

    /**
     * 일(Day) 표시를 두자리로 형식화 한다.
     */
    private static String getFormalDay(Calendar cd) {
        return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.DAY_OF_MONTH)), 2, '0');
    }

    /**
     * 시간(Hour) 표시를 두자리로 형식화 한다.
     */
    private static String getFormalHour(Calendar cd) {
        return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.HOUR_OF_DAY)), 2, '0');
    }

    /**
     * 분(Minute) 표시를 두자리로 형식화 한다.
     */
    private static String getFormalMin(Calendar cd) {
        return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.MINUTE)), 2, '0');
    }

    /**
     * 초(sec) 표시를 두자리로 형식화 한다.
     */
    private static String getFormalSec(Calendar cd) {
        return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.SECOND)), 2, '0');
    }

    /**
     * 밀리초(millisec) 표시를 세자리로 형식화 한다.
     */
    private static String getFormalMSec(Calendar cd) {
        return EgovStringUtil.lPad(Integer.toString(cd.get(Calendar.MILLISECOND)), 3, '0');
    }

    /**
     * Date -> String
     */
    public static String toString(Date date, String format, Locale locale) {
        if (EgovStringUtil.isNull(format)) {
            format = "yyyy-MM-dd HH:mm:ss";
        }

        if (EgovObjectUtil.isNull(locale)) {
            locale = java.util.Locale.KOREA;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
        return sdf.format(date);
    }

}
