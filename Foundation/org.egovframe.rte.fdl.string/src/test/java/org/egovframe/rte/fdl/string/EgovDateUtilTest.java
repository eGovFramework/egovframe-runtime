package org.egovframe.rte.fdl.string;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author sjyoon
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/context-*.xml" })
public class EgovDateUtilTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovDateUtilTest.class);

	@Before
    public void onSetUp() throws Exception {

		LOGGER.debug("###### EgovNumericUtilTest.onSetUp START ######");

		LOGGER.debug("###### EgovNumericUtilTest.onSetUp END ######");

    }

    @After
    public void onTearDown() throws Exception {

    	LOGGER.debug("###### EgovNumericUtilTest.onTearDown START ######");

    	LOGGER.debug("###### EgovNumericUtilTest.onTearDown END ######");
    }

	/**
	 * [Flow #-1] Positive Case : 특정 날짜를 인자로 받아 그 일자로부터 주어진 기간만큼 추가한 날 계산 테스트
	 * @throws Exception
	 */
	@Test
	public void testCalcDate() throws Exception {

		//////////////////////////////////////////////////////////////////////////////
		// String type return

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

		//////////////////////////////////////////////////////////////////////////////
		// Integer type return

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
	 * @throws Exception
	 */
	@Test
	public void testCurrentDate() throws Exception {

		Calendar cal = Calendar.getInstance();

		//int currentAsInt = 0;

		// type integer
		assertEquals(cal.get(Calendar.YEAR), EgovDateUtil.getCurrentYearAsInt());

		assertEquals(cal.get(Calendar.MONTH) + 1, EgovDateUtil.getCurrentMonthAsInt());

		assertEquals(cal.get(Calendar.DAY_OF_MONTH), EgovDateUtil.getCurrentDayAsInt());

		assertEquals(cal.get(Calendar.HOUR_OF_DAY), EgovDateUtil.getCurrentHourAsInt());

		assertEquals(cal.get(Calendar.MINUTE), EgovDateUtil.getCurrentMinuteAsInt());

		LOGGER.debug("DEBUG : {}", EgovDateUtil.getCurrentMilliSecondAsInt());


		// type integer
		assertEquals(EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.YEAR)), 4, '0'), EgovDateUtil.getCurrentYearAsString());

		assertEquals(EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.MONTH) + 1), 2, '0'), EgovDateUtil.getCurrentMonthAsString());

		assertEquals(EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), 2, '0'), EgovDateUtil.getCurrentDayAsString());

		assertEquals(EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)), 2, '0'), EgovDateUtil.getCurrentHourAsString());

		assertEquals(EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.MINUTE)), 2, '0'), EgovDateUtil.getCurrentMinuteAsString());

		LOGGER.debug(EgovDateUtil.getCurrentSecondAsString());

		LOGGER.debug(EgovDateUtil.getCurrentMilliSecondAsString());


		String date = EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.YEAR)), 4, '0')
				+ EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.MONTH) + 1), 2, '0')
				+ EgovStringUtil.lPad(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), 2, '0');

		assertEquals(date, EgovDateUtil.getCurrentDateAsString());

		LOGGER.debug(EgovDateUtil.getCurrentTimeAsString());

		LOGGER.debug(EgovDateUtil.getCurrentDateTimeAsString());


	}

	/**
	 * [Flow #-3] Positive Case : 해당 일자의 요일을 조회한다.
	 * @throws Exception
	 */
	@Test
	public void testGetDayOfWeek() throws Exception {

		//String dateAsString = "0";

		// 요일 조회
		assertEquals("일", EgovDateUtil.getDayOfWeekAsString("2009", "03", "22"));

		assertEquals("월", EgovDateUtil.getDayOfWeekAsString("2009", "03", "23"));

		assertEquals("화", EgovDateUtil.getDayOfWeekAsString("2009", "03", "24"));

		assertEquals("수", EgovDateUtil.getDayOfWeekAsString("2009", "03", "25"));

		assertEquals("목", EgovDateUtil.getDayOfWeekAsString("2009", "03", "26"));

		assertEquals("금", EgovDateUtil.getDayOfWeekAsString("2009", "03", "27"));

		assertEquals("토", EgovDateUtil.getDayOfWeekAsString("2009", "03", "28"));
	}

	/**
	 * [Flow #-4] Positive Case :
	 * @throws Exception
	 */
	@Test
	public void testDateUtilEtc() throws Exception {

		// 만 나이 조회
		assertEquals(32, EgovDateUtil.getFullAge("7701011234567", "20090323"));

		LOGGER.debug("DEBUG : {}", EgovDateUtil.getCurrentFullAge("7701011234567"));

		// 해당년월의 마지막 일자
		assertEquals(31, EgovDateUtil.getDayCountForMonth(2009, 3));

		assertEquals(29, EgovDateUtil.getDayCountForMonth(2008, 2));

		assertEquals(28, EgovDateUtil.getDayCountForMonth(2009, 2));


		//
		assertEquals("yyyyMMdd", EgovDateUtil.getFormatStringWithDate("20090401"));

	}

	/**
	 * [Flow #-5] Positive Case :
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testDayCount() throws Exception {

		// 시작일자와 종료일자 사이의 일수(마지막 일자 제외된 일수)
		assertEquals(90, EgovDateUtil.getDayCount("20090101", "20090401"));

		assertEquals(182, EgovDateUtil.getDayCount("20081201", "20090601"));

		assertEquals(90, EgovDateUtil.getDayCountWithFormatter("20090101", "20090401", "yyyyMMdd"));

		// 형식이 틀린경우 Exception 발생
		Class<Exception> exceptionClass = null;
		try {
			LOGGER.debug("DEBUG : {}", EgovDateUtil.getDayCountWithFormatter("2008/12/01", "2009/06/01", "yyyyMMdd"));
		} catch (Exception e) {
			LOGGER.error("### Exception : {}", e.toString());
			exceptionClass = (Class<Exception>) e.getClass();
		} finally {
			assertEquals(ParseException.class, exceptionClass);
		}

		assertNotSame(181, EgovDateUtil.getDayCountWithFormatter("2008/12/01", "2009/06/01", "yyyy/MM/dd"));

		assertEquals(182, EgovDateUtil.getDayCountWithFormatter("2008/12/01", "2009/06/01", "yyyy/MM/dd"));

	}

	/**
	 * [Flow #-6] Positive Case :
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testTimeCount() throws Exception {

		// 두 일자 간의 차의 밀리초(long)값
		assertEquals(86400000, EgovDateUtil.getTimeCount("20090401", "20090402"));

		assertEquals(60000, EgovDateUtil.getTimeCount("20090301000000", "20090301000100"));

		// 00시 00분 00초 ~ 01시 00분 00초
		assertEquals(3600000, EgovDateUtil.getTimeCount("20090301000000", "20090301010000"));

		// 형식이 틀린경우 Exception 발생
		Class<Exception> exceptionClass = null;
		try {
			LOGGER.debug("DEBUG : {}", EgovDateUtil.getTimeCount("200903010000", "20090301000100"));
		} catch (Exception e) {
			LOGGER.error("### Exception : {}", e.toString());
			exceptionClass = (Class<Exception>) e.getClass();
		} finally {
			assertEquals(ParseException.class, exceptionClass);
		}

	}

	/**
	 * [Flow #-7] Positive Case : 시작일자와 종료일자 사이의 해당 요일이 몇번 있는지 계산한다.
	 * @throws Exception
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
	 * [Flow #-8] Positive Case : 해당 문자열이 주어진 일자 형식을 준수하는지의 여부와 존재하는 날짜인지를 검사한다.
	 * @throws Exception
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
			LOGGER.error("### Exception : {}", e.toString());
			exceptionClass = (Class<Exception>) e.getClass();
		} finally {
			assertEquals(ParseException.class, exceptionClass);
		}

		exceptionClass = null;

		try {
			@SuppressWarnings("unused")
			Date dateFormatCheck = EgovDateUtil.dateFormatCheck("20090229");
		} catch (Exception e) {
			LOGGER.error("### Exception : {}", e.toString());
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
			LOGGER.error("### Exception : {}", e.toString());
			exceptionClass = (Class<Exception>) e.getClass();
		} finally {
			assertEquals(ParseException.class, exceptionClass);
		}
	}

}
