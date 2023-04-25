package org.egovframe.rte.fdl.logging;

import org.egovframe.rte.fdl.logging.sample.LogLayoutSample;
import org.egovframe.rte.fdl.logging.util.LogFileUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-common.xml" })
public class Slf4JLoggerTest {

	/**
	 * 아래는 SLF4J와 Log4j를 함께 사용하는 테스트 메소드
	 * slf4jLogger: level=DEBUG, appender=Console
	 * slf4jFileLogger: level=DEBUG, appender=File
	 * */
	@Test
	public void testSlf4jLogger() throws Exception {

		// log4j2.xml에서 Logger name이 "org.egovframe"인 Logger 인스턴스 생성
		Logger slf4jLogger = LoggerFactory.getLogger("org.egovframe");
		
		// Parameterized 방식으로 로그 출력 - 변수1개
		String arg = "some argument";
		slf4jLogger.debug("Slf4jLoggerTest - {}", arg);
		// 하단 Console에서 로그 확인
		
		
		// log4j2.xml에서 Logger name이 "org.egovframe.rte.~.LogLayoutSample"인 Logger 인스턴스 생성
		Logger slf4jFileLogger = LoggerFactory.getLogger(LogLayoutSample.class);
		
		// Parameterized 방식으로 로그 출력 - Object 타입
		Object[] arguments = new Object[3];
		arguments[0] = "1st";
		arguments[1] = Integer.valueOf("2");
		arguments[2] = new Date().toString();
		
		slf4jFileLogger.debug("Slf4jLoggerTest - {} {} {}", arguments);
		
		File logFile = new File("./logs/file/sample.log");
		
		// 로그 확인
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
		assertTrue(LogFileUtil.getLastLine(logFile).contains(sdf.format(new Date())));
		assertTrue(LogFileUtil.getLastLine(logFile).contains("Slf4jLoggerTest - 1st 2 "+ (new Date()).toString().substring(0, 16)));
	}
}
