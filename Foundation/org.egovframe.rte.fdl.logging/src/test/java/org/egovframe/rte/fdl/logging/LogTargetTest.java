package org.egovframe.rte.fdl.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egovframe.rte.fdl.logging.sample.LogLayoutSample;
import org.egovframe.rte.fdl.logging.util.LogFileUtil;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LogTargetTest {

	/**
	 * 아래는 ConsoleAppender를 테스트하는 메소드 egovframework Logger: level=DEBUG,
	 * appender=Console
	 * */
	@Test
	public void testConsoleAppender() throws Exception {

		Logger logger = LogManager.getLogger("org.egovframe");
		// 로그 출력
		logger.debug("ConsoleAppender test");
		// 하단 Console에서 로그 확인
	}

	/**
	 * 아래는 FileAppender를 테스트하는 메소드 logger: level=DEBUG, appender=File
	 * */
	@Test
	public void testFileAppender() throws Exception {

		Logger logger = LogManager.getLogger(LogLayoutSample.class.getName());

		File logFile = new File("./logs/file/sample.log");

		// 로그 출력
		logger.debug("FileAppender test");

		// 로그파일의 마지막 라인 확인
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",
				java.util.Locale.getDefault());
		assertTrue(LogFileUtil.getLastLine(logFile).contains(
				sdf.format(new Date())));
		assertTrue(LogFileUtil.getLastLine(logFile).endsWith(
				"FileAppender test"));
	}

	/**
	 * 아래는 RollingFileAppender를 테스트하는 메소드 logger: level=DEBUG,
	 * appender=RollingFile, fileName=./logs/rolling/rollingSample.log
	 * */
	@Test
	public void testRollingFileAppender() throws Exception {

		Logger logger = LogManager.getLogger("rollingLogger");

		File logFile = new File("./logs/rolling/rollingSample.log");

		// 로그 출력
		logger.debug("RollingFileAppender test");

		// 로그파일의 마지막 라인 확인
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",
				java.util.Locale.getDefault());
		assertTrue(LogFileUtil.getLastLine(logFile).contains(
				sdf.format(new Date())));
		assertTrue(LogFileUtil.getLastLine(logFile).endsWith(
				"RollingFileAppender test"));

		// RollingFile Appender 설정에 append=true(default)가 설정되어 있으므로
		// 다음 로깅 시에는 target파일을 clear하지 않고 로깅된다.
		for (int i = 0; i < 1000; i++) {
			logger.debug("RollingFileAppender loop : " + i);
		}

		// DefaultRolloverStrategy 설정에서 fileIndex를 "min(default 1)"으로 지정하였으므로
		// targetFile의 size가 1000Byte를 넘어가면 fileIndex가 1인 파일에 rolling됨

		// 생성된 파일 수를 확인해보자
		File logFileDir = new File("./logs/rolling");

		// DefaultRolloverStrategy 설정에서 생성가능한 파일 수(max)를 3으로 지정하였기에
		// target 파일을 포함하여 총 4개의 파일이 생성됨
		assertEquals(4, logFileDir.listFiles().length);
		for (File tempFile : logFileDir.listFiles()) {
			assertTrue(10000 >= tempFile.length());
		}
	}

	/**
	 * 아래는 DailyRollingFileAppender를 테스트하는 메소드 logger: level=DEBUG,
	 * appender=RollingFile, fileName=./logs/daily/dailyRollingSample.log
	 * */
	@Test
	public void testDailyRollingFileAppender() throws Exception {

		Logger logger = LogManager.getLogger("dailyLogger");

		// 최근 로그는 date pattern 이 들어가 있지 않은 기본 로그파일로 생김 (target파일)
		File logFile = new File("./logs/daily/dailyRollingSample.log");

		// 로그 출력
		logger.debug("DailyRollingFileAppender test");

		// 로그파일의 마지막 라인 확인
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
		assertTrue(LogFileUtil.getLastLine(logFile).contains(sdf.format(new Date())));
		assertTrue(LogFileUtil.getLastLine(logFile).endsWith("DailyRollingFileAppender test"));

		// history파일을 만들어보자
		// 현재 log4j2.xml 에서는 테스트 편의성을 위해 1초 단위로 로그파일을 변경하도록 설정하였으므로
		// dailyRollingSample.log 에 있는 로그가 아래 로그를 찍으면서
		// datePattern에 따라 dailyRollingSample.log.날짜로 변경됨 --> history 파일 생성
		Thread.sleep(1000);

		// 아래 최신 로그는 target파일에 출력됨
		logger.debug("DailyRollingFileAppender - file change test");

		// target파일의 마지막 라인 확인
		// 예전 로그는 date pattern 이 포함된 이전 로그 파일의 백업에서 확인할 것
		assertTrue(LogFileUtil.getLastLine(logFile).contains(sdf.format(new Date())));
		assertTrue(LogFileUtil.getLastLine(logFile).endsWith("DailyRollingFileAppender - file change test"));

		// 생성된 파일 수를 확인해보자
		File logFileDir = new File("./logs/daily");

		// 최초 실행 시 최소 파일 2개 생성
		assertTrue(2 <= logFileDir.listFiles().length);

		SimpleDateFormat logDatePattern = new SimpleDateFormat("yyyy-MM-dd-HH", java.util.Locale.getDefault());

		// 설정 상 초단위로 나누었지만 백업 파일이 만들어지는 시간인 현재 시각과 초단위의 차이가 있으므로 시간까지만 비교함
		// 예를 들어 백업이 59초에 발생하면 현재 시각과 분 단위가 달라질 수도 있으므로 시간까지만 비교함
		// 테스트 케이스가 실패하는 이유는 위의 경우에 해당하므로 ./logs/daily에 모든 파일을 삭제한 후 다시 테스트한다.
		assertTrue(logFileDir.listFiles()[1].getName().contains(logDatePattern.format(new Date())));
	}
}