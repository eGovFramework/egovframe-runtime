package egovframework.rte.fdl.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import egovframework.rte.fdl.logging.sample.MarkerFilterTestSample;
import egovframework.rte.fdl.logging.util.LogFileUtil;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-common.xml" })
public class Log4j2FilterTest {

	/**
	 *  아래는 Log4j2에 추가된 ThresholdFilter 를 테스트하는 메소드
	 *  ThresholdFilter는 log level에 대한 임계치를 재설정한다.
	 *  logger: level=DEBUG, appender=Console, File, fileName=./logs/file/filter/ThresholdFilter
	 *  ThresholdFilter: level=ERROR
	 * */
	@Test
	public void testThresholdFilter() throws Exception {

		Logger logger = LogManager.getLogger("thresholdFilterLogger");

		// 로그 출력
		// Console Appender에는 DEBUG 레벨 이상 모든 로그 출력
		// File Appender에는 ThresholdFilter level을 ERROR 설정했으므로 ERROR 레벨 이상 로그만 출력됨
		logger.debug("ThresholdFilter Test Start");
		try {
			@SuppressWarnings("unused")
			int value = 5 / 0;
		} catch (ArithmeticException ae) {
			logger.error("An ArithmeticException have been thrown");
			logger.catching(ae);
		}
		logger.debug("ThresholdFilter Test End");
		// 하단 Console에서 로그 확인

		// File에는 error()와 catching()만 로깅됨 (ref. catching() 메소드의 레벨은 ERROR임)
		String logFileDir = "./logs/file/filter/ThresholdFilter.log";
		File logFile = new File(logFileDir);

		// 로그 확인
		// 출력 패턴 %d{HH:mm:ss.SSS} %-5level %class{36} %L %M - %m%n
		if(logFile != null) {
			assertTrue(!LogFileUtil.contains(logFileDir, "DEBUG"));
			assertEquals(2, LogFileUtil.countWords(logFileDir, "ERROR"));

			String[] tailLines = LogFileUtil.getTailLines(logFile, 32);
			assertTrue(tailLines[0].endsWith("- An ArithmeticException have been thrown"));
			// 엔트리 메서드는 메서드 명으로 로그 메세지가 출력됨 (ex. entry() - entry, exit() - exit ...)
			assertTrue(tailLines[1].endsWith("- catching"));
		}
	}


	/**
	 * 아래는 Log4j2에 추가된 DynamicThresholdFilter 를 테스트하는 메소드
	 * DynamicThresholdFilter는 ThreadContext Map의 key와 value 값에 따라 filtering을 수행한다.
	 * 따라서 value(admin1, admin2, admin3) 값에 따라 Log Level을 다르게 지정할 수 있다.
	 * logger: level=DEBUG, appender=File, fileName=./logs/file/filter/DynamicThresholdFilter
	 *  DynamicThresholdFilter: key=loginId, defaultThreshold=ERROR
	 * */
	@Test
	public void testDynamicThresholdFilter() throws Exception {

		Logger logger = LogManager.getLogger("dynamicThresholdFilterLogger");

		// key:value = loginId : admin1
		// debug 레벨 이상 로그 출력
		ThreadContext.put("loginId", "admin1");
		logger.debug("DEBUG - loginId: admin1");
		logger.info("INFO - loginId: admin1");
		logger.warn("WARN - loginId: admin1");
		logger.error("ERROR - loginId: admin1");
		logger.fatal("FATAL - loginId: admin1");

		// key:value = loginId:admin2
		// warn 레벨 이상 로그 출력
		ThreadContext.put("loginId", "admin2");
		logger.debug("DEBUG - loginId: admin2");
		logger.info("INFO - loginId: admin2");
		logger.warn("WARN - loginId: admin2");
		logger.error("ERROR - loginId: admin2");
		logger.fatal("FATAL - loginId: admin2");

		// key:value = loginId:admin3
		// defaultThreshold=ERROR 적용을 받아 error 레벨 이상 로그 출력
		ThreadContext.put("loginId", "admin3");
		logger.debug("DEBUG - loginId: admin3");
		logger.info("INFO - loginId: admin3");
		logger.warn("WARN - loginId: admin3");
		logger.error("ERROR - loginId: admin3");
		logger.fatal("FATAL - loginId: admin3");

		String logFileDir = "./logs/file/filter/DynamicThresholdFilter.log";
		File logFile = new File(logFileDir);

		// 로그 확인
		if(logFile != null) {
			int numLines = LogFileUtil.countLines(logFileDir);
			assertEquals(10,numLines);

			String[] tailLines = LogFileUtil.getTailLines(logFile, 10);
			assertTrue(tailLines[0].endsWith("DEBUG - loginId: admin1"));
			assertTrue(tailLines[1].endsWith("INFO - loginId: admin1"));
			assertTrue(tailLines[2].endsWith("WARN - loginId: admin1"));
			assertTrue(tailLines[3].endsWith("ERROR - loginId: admin1"));
			assertTrue(tailLines[4].endsWith("FATAL - loginId: admin1"));
			assertTrue(tailLines[5].endsWith("WARN - loginId: admin2"));
			assertTrue(tailLines[6].endsWith("ERROR - loginId: admin2"));
			assertTrue(tailLines[7].endsWith("FATAL - loginId: admin2"));
			assertTrue(tailLines[8].endsWith("ERROR - loginId: admin3"));
			assertTrue(tailLines[9].endsWith("FATAL - loginId: admin3"));
		}
	}


	/**
	 * 아래는 Log4j2에 추가된 MarkerFilter 를 테스트하는 메소드
	 * MarkerFilter는 코드에 지정한 Marker 종류에 따라 로깅여부를 결정할 수 있다.
	 *  MarkerFilter: marker=INSERT
	 *  fileName=./logs/file/filter/MarkerFilter.log
	 * */
	@Resource(name="markerFilterTestService")
	MarkerFilterTestSample markerFilterTestSample;

	@Test
	public void testMarkerFilter() throws Exception {

		String userId = "egov";

		// 로그 출력
		// MarkerFilter의 marker 속성값을 "INSERT" 로 지정해놓았기 때문에
		// "INSERT" 이름의 Marker를 파라미터로 하는 로그만 출력됨

		// "SELECT" Marker를 파라미터로 로그출력하는 메서드
		markerFilterTestSample.doSelectUser(userId);
		// "INSERT" Marker를 파라미터로 로그출력하는 메서드
		// 아래 메서드 내에 INSERT 마커 로그만 출력됨
		markerFilterTestSample.doInsertUser(userId);
		// "UPDATE" Marker를 파라미터로 로그출력하는 메서드
		markerFilterTestSample.doUpdateUser(userId);
		// "DELETE" Marker를 파라미터로 로그출력하는 메서드
		markerFilterTestSample.doDeleteUser(userId);

		// 로그 확인
		String logFileDir = "./logs/file/filter/MarkerFilter.log";
		File logFile = new File(logFileDir);
		if(logFile != null) {
			// PatternLayout의 %marker 패턴은 Marker명[ Parent Marker명 ] 으로 로깅됨
			Boolean printSELECT= LogFileUtil.contains(logFileDir, "SELECT[ SQL ]");
			Boolean printINSERT= LogFileUtil.contains(logFileDir, "INSERT[ SQL ]");
			Boolean printUPDATE= LogFileUtil.contains(logFileDir, "UPDATE[ SQL ]");
			Boolean printDELETE= LogFileUtil.contains(logFileDir, "DELETE[ SQL ]");

			assertEquals(false, printSELECT);
			assertEquals(true, printINSERT);
			assertEquals(false, printUPDATE);
			assertEquals(false, printDELETE);
		}
	}


	/**
	 * 아래는 Log4j2에 추가된 RegexFilter 를 테스트하는 메소드
	 * RegexFilter는 표현식에 따라 출력할 로그를 결정한다.
	 *  logger: level=DEBUG, appender=Console, fileName=./logs/file/filter/RegexFilter.log
	 *  RegexFilter: regex=".* Test .*"
	 * */
	@Test
	public void testRegexFilter() throws Exception {

		Logger logger = LogManager.getLogger("regexFilterLogger");
		logger.debug("RegexFilterTest Start");

		// 로그 출력
		logger.debug("DEBUG - RegexFilter Test !!");
		logger.info("INFO - RegexFilter Test !!");
		logger.warn("WARN - RegexFilterTest !!");
		logger.error("ERROR - RegexFilter Test !!");
		logger.fatal("FATAL - RegexFilter Test !!");
		logger.debug("RegexFilterTest End");

		// 로그 확인
		File logFile = new File("./logs/file/filter/RegexFilter.log");
		String[] tailLines = LogFileUtil.getTailLines(logFile, 4);

		assertTrue(tailLines[0].endsWith("DEBUG - RegexFilter Test !!"));
		assertTrue(tailLines[1].endsWith("INFO - RegexFilter Test !!"));
		assertTrue(tailLines[2].endsWith("ERROR - RegexFilter Test !!"));
		assertTrue(tailLines[3].endsWith("FATAL - RegexFilter Test !!"));
	}
}