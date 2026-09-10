package org.egovframe.rte.fdl.string;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class EgovDateUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovDateUtilTest.class);

    /**
     * [Flow #-1] Positive Case : 특정 날짜를 인자로 받아 그 일자로부터 주어진 기간만큼 추가한 날 계산 테스트
     */
    @Test
    public void testCalcDate() {
        // Calculate Date
        assertEquals("20090330", EgovDateUtil.getCalcDateAsString("2009", "3", "20", 10, "day"));

        assertEquals("20090628", EgovDateUtil.getCalcDateAsString("2009", "3", "20", 100, "day"));

        assertEquals("20100114", EgovDateUtil.getCalcDateAsString("2009", "3", "20", 300, "day"));

        assertEquals("20100120", EgovDateUtil.getCalcDateAsString("2009", "3", "20", 10, "month"));

        assertEquals("20110320", EgovDateUtil.getCalcDateAsString("2009", "3", "20", 2, "year"));

        // Calculate Year
        assertEquals("2009", EgovDateUtil.getCalcYearAsString("2009", "3", "20", 10, "day"));

        assertEquals("2009", EgovDateUtil.getCalcYearAsString("2009", "3", "20", 100, "day"));

        assertEquals("2010", EgovDateUtil.getCalcYearAsString("2009", "3", "20", 300, "day"));

        assertEquals("2010", EgovDateUtil.getCalcYearAsString("2009", "3", "20", 10, "month"));

        assertEquals("2011", EgovDateUtil.getCalcYearAsString("2009", "3", "20", 2, "year"));

        // Calculate Month
        assertEquals("03", EgovDateUtil.getCalcMonthAsString("2009", "3", "20", 10, "day"));

        assertEquals("06", EgovDateUtil.getCalcMonthAsString("2009", "3", "20", 100, "day"));

        assertEquals("01", EgovDateUtil.getCalcMonthAsString("2009", "3", "20", 300, "day"));

        assertEquals("01", EgovDateUtil.getCalcMonthAsString("2009", "3", "20", 10, "month"));

        assertEquals("03", EgovDateUtil.getCalcMonthAsString("2009", "3", "20", 2, "year"));

        // Calculate Day
        assertEquals("30", EgovDateUtil.getCalcDayAsString("2009", "3", "20", 10, "day"));

        assertEquals("28", EgovDateUtil.getCalcDayAsString("2009", "3", "20", 100, "day"));

        assertEquals("14", EgovDateUtil.getCalcDayAsString("2009", "3", "20", 300, "day"));

        assertEquals("01", EgovDateUtil.getCalcMonthAsString("2009", "3", "20", 10, "month"));

        assertEquals("03", EgovDateUtil.getCalcMonthAsString("2009", "3", "20", 2, "year"));

        // Calculate Year
        assertEquals(2009, EgovDateUtil.getCalcYearAsInt("2009", "3", "20", 10, "day"));

        assertEquals(2009, EgovDateUtil.getCalcYearAsInt("2009", "3", "20", 100, "day"));

        assertEquals(2010, EgovDateUtil.getCalcYearAsInt("2009", "3", "20", 300, "day"));

        assertEquals(2010, EgovDateUtil.getCalcYearAsInt("2009", "3", "20", 10, "month"));

        assertEquals(2011, EgovDateUtil.getCalcYearAsInt("2009", "3", "20", 2, "year"));

        // Calculate Month
        assertEquals(3, EgovDateUtil.getCalcMonthAsInt("2009", "3", "20", 10, "day"));

        assertEquals(6, EgovDateUtil.getCalcMonthAsInt("2009", "3", "20", 100, "day"));

        assertEquals(1, EgovDateUtil.getCalcMonthAsInt("2009", "3", "20", 300, "day"));

        assertEquals(1, EgovDateUtil.getCalcMonthAsInt("2009", "3", "20", 10, "month"));

        assertEquals(3, EgovDateUtil.getCalcMonthAsInt("2009", "3", "20", 2, "year"));

        // Calculate Day
        assertEquals(30, EgovDateUtil.getCalcDayAsInt("2009", "3", "20", 10, "day"));

        assertEquals(28, EgovDateUtil.getCalcDayAsInt("2009", "3", "20", 100, "day"));

        assertEquals(14, EgovDateUtil.getCalcDayAsInt("2009", "3", "20", 300, "day"));

        assertEquals(20, EgovDateUtil.getCalcDayAsInt("2009", "3", "20", 10, "month"));

        assertEquals(20, EgovDateUtil.getCalcDayAsInt("2009", "3", "20", 2, "year"));
    }

    /**
     * [Flow #-2] Positive Case :  현재일자 조회 테스트
     */
    @Test
    public void testCurrentDate() {
        Calendar cal = Calendar.getInstance();

        assertEquals(cal.get(Calendar.YEAR), EgovDateUtil.getCurrentYearAsInt());

        assertEquals(cal.get(Calendar.MONTH) + 1, EgovDateUtil.getCurrentMonthAsInt());

        assertEquals(cal.get(Calendar.DAY_OF_MONTH), EgovDateUtil.getCurrentDayAsInt());

        assertEquals(cal.get(Calendar.HOUR_OF_DAY), EgovDateUtil.getCurrentHourAsInt());

        assertEquals(cal.get(Calendar.MINUTE), EgovDateUtil.getCurrentMinuteAsInt());

        assertEquals(EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.YEAR)), 4, '0'), EgovDateUtil.getCurrentYearAsString());

        assertEquals(EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.MONTH) + 1), 2, '0'), EgovDateUtil.getCurrentMonthAsString());

        assertEquals(EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), 2, '0'), EgovDateUtil.getCurrentDayAsString());

        assertEquals(EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)), 2, '0'), EgovDateUtil.getCurrentHourAsString());

        assertEquals(EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.MINUTE)), 2, '0'), EgovDateUtil.getCurrentMinuteAsString());

        String date = EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.YEAR)), 4, '0')
                + EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.MONTH) + 1), 2, '0')
                + EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), 2, '0');

        assertEquals(date, EgovDateUtil.getCurrentDateAsString());
    }

    /**
     * [Flow #-3] Positive Case : 해당 일자의 요일을 조회한다.
     */
    @Test
    public void testGetDayOfWeek() {
        assertEquals("일", EgovDateUtil.getDayOfWeekAsString("2009", "03", "22"));

        assertEquals("월", EgovDateUtil.getDayOfWeekAsString("2009", "03", "23"));

        assertEquals("화", EgovDateUtil.getDayOfWeekAsString("2009", "03", "24"));

        assertEquals("수", EgovDateUtil.getDayOfWeekAsString("2009", "03", "25"));

        assertEquals("목", EgovDateUtil.getDayOfWeekAsString("2009", "03", "26"));

        assertEquals("금", EgovDateUtil.getDayOfWeekAsString("2009", "03", "27"));

        assertEquals("토", EgovDateUtil.getDayOfWeekAsString("2009", "03", "28"));
    }

    /**
     * [Flow #-4] Positive Case : 나이계산, 해당년월의 마지막 일자를 조회한다.
     */
    @Test
    public void testDateUtilEtc() throws ParseException {
        // 만 나이 조회
        assertEquals(32, EgovDateUtil.getFullAge("7701011234567", "20090323"));

        // 해당년월의 마지막 일자
        assertEquals(31, EgovDateUtil.getDayCountForMonth(2009, 3));

        assertEquals(29, EgovDateUtil.getDayCountForMonth(2008, 2));

        assertEquals(28, EgovDateUtil.getDayCountForMonth(2009, 2));

        assertEquals("yyyyMMdd", EgovDateUtil.getFormatStringWithDate("20090401"));
    }

    /**
     * [Flow #-4-1] Positive Case : getFullAge() 의 세기 접두/생일 경과 분기를 고정하는 characterization 테스트.
     * 현재 동작을 그대로 명세화한다(소스 무변경).
     */
    @Test
    public void testGetFullAgeCenturyPrefix() throws ParseException {
        // 주민번호 7번째 자리 0 → 1800년도 출생 ("18" + YYMMDD)
        // birthDate=18800101, keyDate=19000101 → 0101>=0101(당일) → 1900-1880 = 20
        assertEquals(20, EgovDateUtil.getFullAge("8001010000000", "19000101"));

        // 주민번호 7번째 자리 9 → 1800년도 출생
        // birthDate=18851231, keyDate=19000101 → 0101<1231(생일 전) → 1900-1885-1 = 14
        assertEquals(14, EgovDateUtil.getFullAge("8512319000000", "19000101"));

        // 주민번호 7번째 자리 1 → 1900년도 출생
        // birthDate=19770101, keyDate=20090323 → 0323>=0101 → 2009-1977 = 32
        assertEquals(32, EgovDateUtil.getFullAge("7701011234567", "20090323"));

        // 주민번호 7번째 자리 2 → 1900년도 출생
        // birthDate=19770101, keyDate=20090323 → 0323>=0101 → 2009-1977 = 32
        assertEquals(32, EgovDateUtil.getFullAge("7701012234567", "20090323"));

        // 주민번호 7번째 자리 3 → 2000년도 출생 ("20" + YYMMDD)
        // birthDate=20050101, keyDate=20200101 → 0101>=0101(당일) → 2020-2005 = 15
        assertEquals(15, EgovDateUtil.getFullAge("0501013000000", "20200101"));

        // 주민번호 7번째 자리 4 → 2000년도 출생
        // birthDate=20071231, keyDate=20200630 → 0630<1231(생일 전) → 2020-2007-1 = 12
        assertEquals(12, EgovDateUtil.getFullAge("0712314000000", "20200630"));
    }

    /**
     * [Flow #-4-2] Negative/Boundary Case : getFullAge() 의 0 반환 경로(미인식 세기코드, keyDate null)를 고정한다.
     */
    @Test
    public void testGetFullAgeReturnsZero() throws ParseException {
        // 미인식 세기코드(5,6,7,8) → birthDate=null → 0 반환
        assertEquals(0, EgovDateUtil.getFullAge("8001015000000", "20200101"));
        assertEquals(0, EgovDateUtil.getFullAge("8001016000000", "20200101"));
        assertEquals(0, EgovDateUtil.getFullAge("8001017000000", "20200101"));
        assertEquals(0, EgovDateUtil.getFullAge("8001018000000", "20200101"));

        // keyDate == null → birthDate 유효해도 0 반환
        assertEquals(0, EgovDateUtil.getFullAge("7701011234567", null));
    }

    /**
     * [Flow #-4-3] Boundary Case : 생일 경과 여부(MMDD 비교) 경계를 고정한다.
     */
    @Test
    public void testGetFullAgeBirthdayBoundary() throws ParseException {
        // birthDate=19000615
        // keyDate=20200614 → 0614<0615(생일 전날) → 2020-1900-1 = 119
        assertEquals(119, EgovDateUtil.getFullAge("0006151000000", "20200614"));

        // keyDate=20200615 → 0615>=0615(생일 당일) → 2020-1900 = 120
        assertEquals(120, EgovDateUtil.getFullAge("0006151000000", "20200615"));

        // keyDate=20200616 → 0616>=0615(생일 다음날) → 2020-1900 = 120
        assertEquals(120, EgovDateUtil.getFullAge("0006151000000", "20200616"));
    }

    /**
     * [Flow #-4-4] Positive Case : getCurrentFullAge() 가 현재 일자로 getFullAge() 에 위임함을 확인하는 스모크 테스트.
     */
    @Test
    public void testGetCurrentFullAgeDelegation() throws ParseException {
        // 미인식 세기코드(7번째 자리 5)는 출생년도가 정해지지 않아 현재일자 기준에서도 0 을 반환한다.
        // (현재일자에 의존하지 않는 결정적 단정으로 위임 동작을 검증한다.)
        assertEquals(0, EgovDateUtil.getCurrentFullAge("8001015000000"));
    }

    /**
     * [Flow #-5] Positive Case : 시작일자와 종료일자 사이의 일수(마지막 일자 제외된 일수)
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testDayCount() throws ParseException {
        // 시작일자와 종료일자 사이의 일수(마지막 일자 제외된 일수)
        assertEquals(90, EgovDateUtil.getDayCount("20090101", "20090401"));

        assertEquals(182, EgovDateUtil.getDayCount("20081201", "20090601"));

        assertEquals(90, EgovDateUtil.getDayCountWithFormatter("20090101", "20090401", "yyyyMMdd"));

        // 형식이 틀린 경우 Exception 발생
        Class<Exception> exceptionClass = null;
        try {
            EgovDateUtil.getDayCountWithFormatter("2008/12/01", "2009/06/01", "yyyyMMdd");
        } catch (Exception e) {
            exceptionClass = (Class<Exception>) e.getClass();
        } finally {
            assertEquals(ParseException.class, exceptionClass);
        }

        assertNotSame(181, EgovDateUtil.getDayCountWithFormatter("2008/12/01", "2009/06/01", "yyyy/MM/dd"));

        assertEquals(182, EgovDateUtil.getDayCountWithFormatter("2008/12/01", "2009/06/01", "yyyy/MM/dd"));
    }

    /**
     * [Flow #-6] Positive Case : 두 일자 간의 차의 밀리초(long)값
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testTimeCount() throws ParseException {
        // 두 일자 간의 차의 밀리초(long)값
        assertEquals(86400000, EgovDateUtil.getTimeCount("20090401", "20090402"));

        assertEquals(60000, EgovDateUtil.getTimeCount("20090301000000", "20090301000100"));

        // 00시 00분 00초 ~ 01시 00분 00초
        assertEquals(3600000, EgovDateUtil.getTimeCount("20090301000000", "20090301010000"));

        // 형식이 틀린경우 Exception 발생
        Class<Exception> exceptionClass = null;
        try {
            EgovDateUtil.getTimeCount("200903010000", "20090301000100");
        } catch (Exception e) {
            exceptionClass = (Class<Exception>) e.getClass();
        } finally {
            assertEquals(ParseException.class, exceptionClass);
        }
    }

    /**
     * [Flow #-6-1] Negative Case : 인식하지 못한 길이의 일자 문자열에 대한 예외 메시지 확인
     */
    @Test
    public void testTimeCountUnknownLengthMessage() {
        // 길이가 4, 8, 12, 14, 17 중 어느 것도 아니면 형식을 정할 수 없어 ParseException 이 난다.
        ParseException e = assertThrows(ParseException.class, () -> EgovDateUtil.getTimeCount("2009040112", "2009040113"));

        // 형제인 dateFormatCheck() 과 같이 어긋난 입력값을 알려야 한다.
        assertTrue(e.getMessage().contains("2009040112"), "예외 메시지: " + e.getMessage());
    }

    /**
     * [Flow #-7] Positive Case : 시작일자와 종료일자 사이의 해당 요일이 몇번 있는지 계산한다.
     */
    @Test
    public void testDayOfWeekCount() throws Exception {
        assertEquals(5, EgovDateUtil.getDayOfWeekCount("20090301", "20090331", "월요일"));

        assertEquals(5, EgovDateUtil.getDayOfWeekCount("20090301", "20090331", "일"));

        assertEquals(4, EgovDateUtil.getDayOfWeekCount("20090301", "20090331", "토"));

        assertEquals(22, EgovDateUtil.getDayOfWeekCount("20090101", "20090531", "일"));

        assertEquals(52, EgovDateUtil.getDayOfWeekCount("20090101", "20091231", "일"));

        assertEquals(52, EgovDateUtil.getDayOfWeekCount("20090101", "20091231", "금"));

        assertEquals(52, EgovDateUtil.getDayOfWeekCount("20090101", "20091231", "토"));
    }

    /**
     * [Flow #-7-1] Negative Case : 미인식 요일이 입력되면 무한루프 대신 IllegalArgumentException 을 던진다.
     * (수정 전에는 sYoil 집합에 없는 요일이 들어오면 while 루프가 영원히 종료되지 않아 무한루프에 빠졌다.)
     * 무한루프 회귀를 막기 위해 timeout 을 건다.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testDayOfWeekCountInvalidYoil() {
        // 영문 입력(substring 후 "M")
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getDayOfWeekCount("20090301", "20090331", "Mon"));

        // length 2 입력("월요")
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getDayOfWeekCount("20090301", "20090331", "월요"));

        // 빈 문자열
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getDayOfWeekCount("20090301", "20090331", ""));

        // 오타(3자리이지만 정규화 후 미인식)
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getDayOfWeekCount("20090301", "20090331", "ABC"));

        // null
        assertThrows(IllegalArgumentException.class,
                () -> EgovDateUtil.getDayOfWeekCount("20090301", "20090331", null));
    }

    /**
     * [Flow #-8] Positive Case : 해당 문자열이 주어진 일자 형식을 준수하는지의 여부와 존재하는 날짜인지를 검사한다.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testDateFormatCheck() throws Exception {
        assertNotNull(EgovDateUtil.dateFormatCheck("20090301"));

        // 형식이 틀린경우 Exception 발생
        Class<Exception> exceptionClass = null;
        try {
            @SuppressWarnings("unused")
            Date dateFormatCheck = EgovDateUtil.dateFormatCheck("20090300");
        } catch (Exception e) {
            exceptionClass = (Class<Exception>) e.getClass();
        } finally {
            assertEquals(ParseException.class, exceptionClass);
        }

        exceptionClass = null;

        try {
            @SuppressWarnings("unused")
            Date dateFormatCheck = EgovDateUtil.dateFormatCheck("20090229");
        } catch (Exception e) {
            exceptionClass = (Class<Exception>) e.getClass();
        } finally {
            assertEquals(ParseException.class, exceptionClass);
        }

        // format 지정
        assertNotNull(EgovDateUtil.dateFormatCheck("2009/03/01", "yyyy/MM/dd"));

        assertNotNull(EgovDateUtil.dateFormatCheck("2009-03-01", "yyyy-MM-dd"));

        exceptionClass = null;

        try {
            @SuppressWarnings("unused")
            Date dateFormatCheck = EgovDateUtil.dateFormatCheck("2009/03/01");
        } catch (Exception e) {
            exceptionClass = (Class<Exception>) e.getClass();
        } finally {
            assertEquals(ParseException.class, exceptionClass);
        }
    }

}
