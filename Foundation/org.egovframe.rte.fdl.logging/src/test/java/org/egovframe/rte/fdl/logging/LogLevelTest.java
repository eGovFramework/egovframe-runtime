package org.egovframe.rte.fdl.logging;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.egovframe.rte.fdl.logging.sample.LogLevelDebug;
import org.egovframe.rte.fdl.logging.sample.LogLevelError;
import org.egovframe.rte.fdl.logging.sample.LogLevelFatal;
import org.egovframe.rte.fdl.logging.sample.LogLevelInfo;
import org.egovframe.rte.fdl.logging.sample.LogLevelWarn;
import org.egovframe.rte.fdl.logging.sample.service.LogTestService;
import org.egovframe.rte.fdl.logging.sample.service.SomeVO;
import org.egovframe.rte.fdl.logging.sample.service.impl.LogTestServiceImpl;
import org.egovframe.rte.fdl.logging.util.LogFileUtil;

import javax.annotation.Resource;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-common.xml" })
public class LogLevelTest {

	@Resource(name = "logTestService")
	LogTestService logTestService;

	@Resource(name = "logLevelDebug")
	LogLevelDebug logLevelDebug;

	@Resource(name = "logLevelInfo")
	LogLevelInfo logLevelInfo;

	@Resource(name = "logLevelWarn")
	LogLevelWarn logLevelWarn;

	@Resource(name = "logLevelError")
	LogLevelError logLevelError;

	@Resource(name = "logLevelFatal")
	LogLevelFatal logLevelFatal;

	File logFile;

	@Before
	public void onSetUp() throws Exception {
		// ResourceLoader resourceLoader = new DefaultResourceLoader();
		// org.springframework.core.io.Resource resource =
		// resourceLoader.getResource("file:./logs/file/sample.log");
		// logFile = resource.getFile();
		logFile = new File("./logs/file/sample.log");
		if (!logFile.exists()) {
			logFile.createNewFile();
		}
	}
	
	
	/**
	 * 아래는 자바코드의 isLevelEnabeld() 메서드와 log4j2.xml에 설정한 Log Level 관계를 테스트하는 메소드
	 * egovframework Logger: level=DEBUG, appender=Console
	 * targetLogger: level=DEBUG, appender=Console
	 * 
	 * 테스트방법
	 * 1. 자바코드로 직접 Log Level을 변경해가면서 테스트
	 * */
	@Test
	public void testLogLevelWithIsLevelEnabled() throws Exception {
		
		SomeVO vo = new SomeVO();
		vo.setSomeAttr("some");
		// 로그출력
		// 아래 메소드 내에서 호출하는 logger는 log4j2.xml 에 존재하지 않으므로
		//  Named Hierarchy에 따라 이름이 가장 많이 매칭되는 "egovframework" 이름의 Logger설정을 따른다.
		// DEBUG, INFO, WARN, ERROR, FATAL 에 대한 모든 로그가 출력됨
		logTestService.executeSomeLogic(vo);
		// 하단 Console에서 로그 확인

		
		// 지금부터 LogTestServiceImpl에 선언된 Logger의 Log Level을 변경해가며 테스트
		// Log Level이 변경된 로그는 Logger에 File Appender를 추가하여 ./logs/file/sample.log 파일에 출력하도록 함
		
		// LogTestServiceImpl에 선언된 Logger 획득
		Logger targetLogger = (Logger) LogManager.getLogger(LogTestServiceImpl.class.getName());
		assertEquals("org.egovframe.rte.fdl.logging.sample.service.impl.LogTestServiceImpl", targetLogger.getName());
		// 위 Logger Name으로 설정된 로거가 없으므로 Parent Logger인 "egovframework" 이름의 Logger 설정을 따름 
		
		Logger parentLogger = targetLogger.getParent();
		assertEquals("org.egovframe", parentLogger.getName());
		
		// targetLogger 설정 확인
		assertTrue(targetLogger.isDebugEnabled()); 
		assertEquals(Level.DEBUG, targetLogger.getLevel()); 
		assertTrue(!targetLogger.isAdditive());
		assertNotNull(targetLogger.getAppenders().get("console")); // egovframework에는 console appender만 추가되어 있음
		assertNull(targetLogger.getAppenders().get("file")); 
		
		// targetLogger의File Appender 추가
		// 이를 위해 File Appender 를 가지고 있는 debugLogger로부터 File Appender 획득
		Logger debugLogger = (Logger) LogManager.getLogger(	"org.egovframe.rte.fdl.logging.sample.LogLevelDebug");
		// debugLogger에 걸린 AppenderRef 설정 확인, "file" 이름의 File Appender가 설정되어 있음
		Map<String,Appender>appenders = debugLogger.getAppenders();
		FileAppender fileAppender = (FileAppender) appenders.get("file");
		assertNotNull(fileAppender); 
		
		// Parent Logger에 직접 추가하면 targetLogger가 참조할 수 있다.
		parentLogger.addAppender(fileAppender); 
		// targetLogger.addAppender(fileAppender); --> egovframework Logger에 추가되는 것은 아니다.
		
		// targetLogger에 File Appender가 추가됐는지 확인
		assertNotNull(targetLogger.getAppenders().get("console")); 
		assertNotNull(targetLogger.getAppenders().get("file")); 
		
		// targetLogger의 Log Level 변경 - INFO
		LogManager.getLogger("sysoutLogger").debug("==== targetLogger의 Log Level 변경 - INFO ====");
		targetLogger.setLevel(Level.INFO);
		logTestService.executeSomeLogic(vo);
		String[] tailLines = LogFileUtil.getTailLines(logFile, 4);
		assertTrue(tailLines[0].endsWith("INFO - LogTestServiceImpl.executeSomeLogic executed"));
		assertTrue(tailLines[1].endsWith("WARN - LogTestServiceImpl.executeSomeLogic executed"));
		assertTrue(tailLines[2].endsWith("ERROR - LogTestServiceImpl.executeSomeLogic executed"));
		assertTrue(tailLines[3].endsWith("FATAL - LogTestServiceImpl.executeSomeLogic executed"));

		// targetLogger의 Log Level 변경 - WARN
		LogManager.getLogger("sysoutLogger").debug("==== targetLogger의 Log Level 변경 - WARN ====");
		targetLogger.setLevel(Level.WARN);
		logTestService.executeSomeLogic(vo);
		// 마지막 라인부터 3줄 까지만 WARN으로 변경한 후 출력된 로그이지만
		// 이전 데이터 일부를 함께 가져와 새롭게 적용된 내용을 확인함
		tailLines = LogFileUtil.getTailLines(logFile, 4);
		// 이전 INFO 레벨일 때 마지막 라인 확인
		assertTrue(tailLines[0].endsWith("FATAL - LogTestServiceImpl.executeSomeLogic executed"));
		// 새로운 WARN 레벨 이후 기록된 라인 확인 - WARN 이상 로그만 나옴
		assertTrue(tailLines[1].endsWith("WARN - LogTestServiceImpl.executeSomeLogic executed"));
		assertTrue(tailLines[2].endsWith("ERROR - LogTestServiceImpl.executeSomeLogic executed"));
		assertTrue(tailLines[3].endsWith("FATAL - LogTestServiceImpl.executeSomeLogic executed"));

		// targetLogger의 Log Level 변경 - ERROR
		LogManager.getLogger("sysoutLogger").debug("==== targetLogger의 Log Level 변경 - ERROR ====");
		targetLogger.setLevel(Level.ERROR);
		logTestService.executeSomeLogic(vo);
		// 마지막 라인부터 2줄 까지만 ERROR으로 변경한 후 출력된 로그이지만
		// 이전 데이터 일부를 함께 가져와 새롭게 적용된 내용을 확인함
		tailLines = LogFileUtil.getTailLines(logFile, 4);
		// 이전 WARN 레벨일 때 마지막 2 라인 확인
		assertTrue(tailLines[0].endsWith("ERROR - LogTestServiceImpl.executeSomeLogic executed"));
		assertTrue(tailLines[1].endsWith("FATAL - LogTestServiceImpl.executeSomeLogic executed"));
		// 새로운 ERROR 레벨 이후 기록된 라인 확인 - ERROR 이상 로그만 나옴
		assertTrue(tailLines[2].endsWith("ERROR - LogTestServiceImpl.executeSomeLogic executed"));
		assertTrue(tailLines[3].endsWith("FATAL - LogTestServiceImpl.executeSomeLogic executed"));

		// targetLogger의 Log Level 변경 - FATAL
		targetLogger.setLevel(Level.FATAL);
		LogManager.getLogger("sysoutLogger").debug("==== targetLogger의 Log Level 변경 - FATAL ====");
		logTestService.executeSomeLogic(vo);
		// 마지막 라인부터 1줄만 FATAL으로 변경한 후 출력된 로그이지만
		// 이전 데이터 일부를 함께 가져와 새롭게 적용된 내용을 확인함
		tailLines = LogFileUtil.getTailLines(logFile, 4);
		// 이전 ERROR 레벨일 때 마지막 3 라인 확인
		assertTrue(tailLines[0].endsWith("FATAL - LogTestServiceImpl.executeSomeLogic executed"));
		assertTrue(tailLines[1].endsWith("ERROR - LogTestServiceImpl.executeSomeLogic executed"));
		assertTrue(tailLines[2].endsWith("FATAL - LogTestServiceImpl.executeSomeLogic executed"));
		// 새로운 FATAL 레벨 이후 기록된 라인 확인 - ERROR 이상 로그만 나옴
		assertTrue(tailLines[3].endsWith("FATAL - LogTestServiceImpl.executeSomeLogic executed"));
	}

	
	/**
	 * 아래는 자바코드의 debug() 메서드와 log4j2.xml에 설정한 Log Level(DEBUG) 관계를 테스트하는 메소드
	 * */
	@Test
	public void testLogLevelDebug() throws Exception {
		
		// executeSomeLogic() 에서 사용하는 logger의 level=DEBUG, appender=File
		logLevelDebug.executeSomeLogic();
		LogManager.getLogger("sysoutLogger").debug(	"logFile 현재 최종 라인 : " + LogFileUtil.getLastLine(logFile));
		assertTrue(LogFileUtil.getLastLine(logFile).endsWith(	"DEBUG - LogLevelDebug.executeSomeLogic executed"));
	}
	
	/**
	 * 아래는 자바코드의 info() 메서드와 log4j2.xml에 설정한 Log Level(INFO) 관계를 테스트하는 메소드
	 * */
	@Test
	public void testLogLevelInfo() throws Exception {
		
		// executeSomeLogic() 에서 사용하는 logger의 level=INFO, appender=File
		logLevelInfo.executeSomeLogic();
		LogManager.getLogger("sysoutLogger").debug(	"logFile 현재 최종 라인 : " + LogFileUtil.getLastLine(logFile));
		assertTrue(LogFileUtil.getLastLine(logFile).endsWith(	"INFO - LogLevelInfo.executeSomeLogic executed"));
	}

	/**
	 * 아래는 자바코드의 warn() 메서드와 log4j2.xml에 설정한 Log Level(WARN) 관계를 테스트하는 메소드
	 * */
	@Test
	public void testLogLevelWarn() throws Exception {
		
		// executeSomeLogic() 에서 사용하는 logger의 level=WARN, appender=File
		logLevelWarn.executeSomeLogic();
		LogManager.getLogger("sysoutLogger").debug(	"logFile 현재 최종 라인 : " + LogFileUtil.getLastLine(logFile));
		assertTrue(LogFileUtil.getLastLine(logFile).endsWith(	"WARN - LogLevelWarn.executeSomeLogic executed"));
	}

	/**
	 * 아래는 자바코드의 error() 메서드와 log4j2.xml에 설정한 Log Level(ERROR) 관계를 테스트하는 메소드
	 * */
	@Test
	public void testLogLevelError() throws Exception {
		
		// executeSomeLogic() 에서 사용하는 logger의 level=ERROR, appender=File
		logLevelError.executeSomeLogic();
		LogManager.getLogger("sysoutLogger").debug(	"logFile 현재 최종 라인 : " + LogFileUtil.getLastLine(logFile));
		assertTrue(LogFileUtil.getLastLine(logFile).endsWith(	"ERROR - LogLevelError.executeSomeLogic executed"));
	}

	/**
	 * 아래는 자바코드의 fatal() 메서드와 log4j2.xml에 설정한 Log Level(FATAL) 관계를 테스트하는 메소드
	 * */
	@Test
	public void testLogLevelFatal() throws Exception {
		
		// executeSomeLogic() 에서 사용하는 logger의 level=FATAL, appender=File
		logLevelFatal.executeSomeLogic();
		LogManager.getLogger("sysoutLogger").debug(	"logFile 현재 최종 라인 : " + LogFileUtil.getLastLine(logFile));
		assertTrue(LogFileUtil.getLastLine(logFile).endsWith(	"FATAL - LogLevelFatal.executeSomeLogic executed"));
	}
}