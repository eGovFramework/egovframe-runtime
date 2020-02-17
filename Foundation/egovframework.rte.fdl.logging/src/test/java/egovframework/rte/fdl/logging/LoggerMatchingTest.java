package egovframework.rte.fdl.logging;

import static org.junit.Assert.assertTrue;

import java.io.File;

import egovframework.rte.fdl.logging.sample.LogLayoutSample;
import egovframework.rte.fdl.logging.sample.LogLevelDebug;
import egovframework.rte.fdl.logging.sample.LogLevelError;
import egovframework.rte.fdl.logging.sample.LogLevelFatal;
import egovframework.rte.fdl.logging.sample.LogLevelInfo;
import egovframework.rte.fdl.logging.sample.LogLevelWarn;
import egovframework.rte.fdl.logging.sample.LogTestSample;
import egovframework.rte.fdl.logging.util.LogFileUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-common.xml" })
public class LoggerMatchingTest {

	File logFile;

	@Before
	public void onSetUp() throws Exception {
		logFile = new File("./logs/file/sample.log");
		if (!logFile.exists()) {
			logFile.createNewFile();
		}
	}

	/**
	 * 아래는 log4j2.xml에 정의된 Logger name과 자바코드에서 호출하는 Logger Name 일치여부를 테스트하는 메소드
	 * Root Logger: level=TRACE, appender=Console
	 * egovframework Logger: level=DEBUG, appender=Console
	 * testClassLog: level=DEBUG, appender=Console
	 * logTestSampleLog: level=DEBUG, appender=Console
	 * layoutSampleLog: level=DEBUG, appender=File
	 * logLevelDebugLog: level=DEBUG, appender=File
	 * mdcLogger: level=DEBUG, appender=File
	 * */
	@Test
	public void testLoggerMatching() throws Exception {

		// Logger testClassLog = LogManager.getLogger("egovframework.rte.fdl.logging.LoggerMatchingTest"); 과 동일
		// log4j2.xml에는 "egovframework.rte.fdl.logging.LoggerMatchingTest" 이름의 Logger가 존재하지 않으므로
		// Named Hierarchy에 따라 이름이 가장 많이 매칭되는 "egovframework" 이름의 Logger설정을 따른다.
		Logger testClassLog = LogManager.getLogger(LoggerMatchingTest.class.getName()); 
		// 로그 출력
		testClassLog.debug("logger - egovframework");
		// 하단 Console에서 로그 확인
		
		
		// Logger layoutSampleLog = LogManager.getLogger("egovframework.rte.fdl.logging.sample.LogLayoutSample"); 과 동일
		// log4j2.xml에는 "egovframework.rte.fdl.logging.sample.LogLayoutSample" 이름의 Logger가 존재하므로 해당 Logger 설정을 따른다.
		Logger layoutSampleLog = LogManager.getLogger(LogLayoutSample.class.getName());		
		// 로그 출력
		layoutSampleLog.debug("logger - egovframework.rte.fdl.logging.sample.LogLayoutSample");
		// 로그 확인
		assertTrue(LogFileUtil.getLastLine(logFile).endsWith(	"logger - egovframework.rte.fdl.logging.sample.LogLayoutSample"));
		
		
		// Logger logLevelDebugLog = LogManager.getLogger("egovframework.rte.fdl.logging.sample.LogLevelDebug"); 과 동일
		// log4j2.xml에는 "egovframework.rte.fdl.logging.sample.LogLevelDebug" 이름의 Logger가 존재하므로 해당 Logger 설정을 따른다.
		Logger logLevelDebugLog = LogManager.getLogger(LogLevelDebug.class.getName());
		// 로그 출력
		logLevelDebugLog	.debug("logger - egovframework.rte.fdl.logging.sample.LogLevelDebug");
		// 로그 확인
		assertTrue(LogFileUtil.getLastLine(logFile).endsWith(	"logger - egovframework.rte.fdl.logging.sample.LogLevelDebug"));

		
		// 이하 유사
		Logger logLevelInfoLog = LogManager.getLogger(LogLevelInfo.class.getName());
		Logger logLevelWarnLog = LogManager.getLogger(LogLevelWarn.class.getName());
		Logger logLevelErrorLog = LogManager.getLogger(LogLevelError.class.getName());
		Logger logLevelFatalLog = LogManager.getLogger(LogLevelFatal.class.getName());
		// 로그 출력
		logLevelInfoLog	.info("logger - egovframework.rte.fdl.logging.sample.LogLevelInfo");
		logLevelWarnLog.warn("logger - egovframework.rte.fdl.logging.sample.LogLevelWarn");
		logLevelErrorLog.error("logger - egovframework.rte.fdl.logging.sample.LogLevelError");
		logLevelFatalLog.fatal("logger - egovframework.rte.fdl.logging.sample.LogLevelFatal");
		// 로그 확인
		String[] tailLines = LogFileUtil.getTailLines(logFile, 4);
		assertTrue(tailLines[0].endsWith("logger - egovframework.rte.fdl.logging.sample.LogLevelInfo"));
		assertTrue(tailLines[1].endsWith("logger - egovframework.rte.fdl.logging.sample.LogLevelWarn"));
		assertTrue(tailLines[2].endsWith("logger - egovframework.rte.fdl.logging.sample.LogLevelError"));
		assertTrue(tailLines[3].endsWith("logger - egovframework.rte.fdl.logging.sample.LogLevelFatal"));

		
		// Logger LogTestSample = LogManager.getLogger("egovframework.rte.fdl.logging.sample.LogTestSample"); 과 동일
		// log4j2.xml에는 "egovframework.rte.fdl.logging.sample.LogTestSample" 이름의 Logger가 존재하지 않으므로
		// Named Hierarchy에 따라 이름이 가장 많이 매칭되는 "egovframework" 이름의 Logger설정을 따른다.
		Logger logTestSampleLog = LogManager.getLogger(LogTestSample.class.getName());
		// 로그 출력
		logTestSampleLog.debug("logger - egovframework");
		// 하단 console에서 로그 확인

		
		// 직접 Logger Name ("mdcLogger")을 지정하여 Logger 객체 획득
		// log4j2.xml에 "mdcLogger" 이름의 Logger 설정이 있음
		Logger mdcLogger = LogManager.getLogger("mdcLogger");
		ThreadContext.put("class", this.getClass().getSimpleName());
		ThreadContext.put("method", "testLogMDC");
		ThreadContext.put("testKey", "test value");
		// 로그 출력 pattern: %d %5p [%c] [%X{class} %X{method} %X{testKey}] %m%n
		mdcLogger.debug("MDC test!");
		// 로그 확인
		File mdcFile = new File("./logs/file/mdcSample.log");
		assertTrue(LogFileUtil.getLastLine(mdcFile)	.endsWith("DEBUG [mdcLogger] [LoggerMatchingTest testLogMDC test value] MDC test!"));

		
		// 존재하지 않는 로거명을 지정하는 경우, Root Logger에 걸림
		Logger notExistLog = LogManager.getLogger("notExistLogger");
		// 로그 출력
		// Root Logger의 level이 error일 경우, debug("DEBUG - logger - egovframework");는 출력되지 않음
		notExistLog.debug("DEBUG - logger - egovframework");
		notExistLog.error("ERROR - logger - egovframework");
		// 하단 console에서 로그 확인
	}
}