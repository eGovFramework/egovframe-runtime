package org.egovframe.rte.fdl.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.egovframe.rte.fdl.logging.sample.LogLayoutSample;
import org.egovframe.rte.fdl.logging.util.LogFileUtil;

import javax.annotation.Resource;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-common.xml" })
public class LogLayoutTest {

	@Resource(name = "logLayoutSample")
	LogLayoutSample logLayoutSample;

	File logFile;

	@Before
	public void onSetUp() throws Exception {
		logFile = new File("./logs/file/sample.log");

	}

	
	/**
	 * 아래는 Log Layout을 테스트하는 메소드
	 * targetLogger: level=DEBUG, appender=File, layout=PatternLayout
	 * 
	 * 테스트 수행 순서
	 * 1. 특정 Logger의 Appender를 획득하고, 해당 Appender의 Layout 객체를 획득
	 * 2. 획득한 Layout에 등록된 기존 pattern을 출력하여 확인
	 * 3. 획득한 Layout의 pattern을 변경해가며 확인
	 * */
	@Test
	public void testLogLayout() throws Exception {

		Logger targetLogger = (Logger) logLayoutSample.getTargetLogger();
		assertEquals("org.egovframe.rte.fdl.logging.sample.LogLayoutSample", targetLogger.getName());
		
		// targetLogger 설정 확인
		assertTrue(targetLogger.isDebugEnabled()); // true
		assertEquals(Level.DEBUG, targetLogger.getLevel()); // true
		assertTrue(!targetLogger.isAdditive()); // true - 해당 로거 설정에 additivity=false이므로 targetLogger.isAdditive()=false

		// targetLogger에 걸린 AppenderRef 설정 확인, "file" 이름의 File Appender가 설정되어 있음
		 Map<String,Appender>appenders = targetLogger.getAppenders();
		 FileAppender fileAppender = (FileAppender) appenders.get("file");
		assertNotNull(fileAppender); // true

		// 해당 로거의 File Appender 의 Layout 설정 확인, PatternLayout이 등록되어 있음
		assertEquals("org.apache.logging.log4j.core.layout.PatternLayout", fileAppender.getLayout().getClass().getName());
		PatternLayout layout = (PatternLayout) fileAppender.getLayout();
		assertEquals("%d %5p [%c] %m%n", layout.getConversionPattern());

		// 로그 출력
		logLayoutSample.executeSomeLogic();

		// ref.)
		// http://logging.apache.org/log4j/2.x/manual/layouts.html#PatternLayout
		// 주요 Conversion Character
		// d (date)
		// p (priority = log level)
		// c (Category)
		// m (사용자 메시지)
		// n (platform dependent line separator)
		// C (Class - 느림)
		// M (Method - 느림)
		// L (line number - 느림)
		// t (thread)
		// r (기동 후 경과시간)
		// X (ThreadContext 객체의 특정 property 출력을 위해 - ex. %X{key} )
		
		// 위에서 출력한 로그 확인 테스트 (Layout의 pattern 변경 전)
		String[] tailLines = LogFileUtil.getTailLines(logFile, 5);
		// check - %d
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", java.util.Locale.getDefault());
		assertTrue(sdf.parse(tailLines[0].substring(0, 23)) instanceof Date);
		assertTrue(sdf.parse(tailLines[1].substring(0, 23)) instanceof Date);
		assertTrue(sdf.parse(tailLines[2].substring(0, 23)) instanceof Date);
		assertTrue(sdf.parse(tailLines[3].substring(0, 23)) instanceof Date);
		assertTrue(sdf.parse(tailLines[4].substring(0, 23)) instanceof Date);
		// check - %5p - cf.)%-5p 와 같이 -를 기술하면 right pad 됨!
		assertEquals("DEBUG", tailLines[0].substring(24, 29));
		assertEquals(" INFO", tailLines[1].substring(24, 29));
		assertEquals(" WARN", tailLines[2].substring(24, 29));
		assertEquals("ERROR", tailLines[3].substring(24, 29));
		assertEquals("FATAL", tailLines[4].substring(24, 29));
		// check - [%c]
		assertTrue(tailLines[0].contains("[org.egovframe.rte.fdl.logging.sample.LogLayoutSample]"));
		assertTrue(tailLines[1].contains("[org.egovframe.rte.fdl.logging.sample.LogLayoutSample]"));
		assertTrue(tailLines[2].contains("[org.egovframe.rte.fdl.logging.sample.LogLayoutSample]"));
		assertTrue(tailLines[3].contains("[org.egovframe.rte.fdl.logging.sample.LogLayoutSample]"));
		assertTrue(tailLines[4].contains("[org.egovframe.rte.fdl.logging.sample.LogLayoutSample]"));
		// check - %m
		assertTrue(tailLines[0].endsWith("DEBUG - LogLayoutSample.executeSomeLogic executed"));
		assertTrue(tailLines[1].endsWith("INFO - LogLayoutSample.executeSomeLogic executed"));
		assertTrue(tailLines[2].endsWith("WARN - LogLayoutSample.executeSomeLogic executed"));
		assertTrue(tailLines[3].endsWith("ERROR - LogLayoutSample.executeSomeLogic executed"));
		assertTrue(tailLines[4].endsWith("FATAL - LogLayoutSample.executeSomeLogic executed"));

		// TODO 2.0 정식버전으로 업데이트된 이후 setConversionPattern 메소드 삭제됨
		// Layout 변경 테스트
//		 layout.setConversionPattern("%d %5p [%c] %C %M %L %t %r %m%n");

		// 로그 재출력
		logLayoutSample.executeSomeLogic();

		// 위에서 출력한 로그 확인 테스트 (Layout의 pattern 변경 후)
		// 로그파일에 기록된 마지막 5행을 다시 가져옴
		tailLines = LogFileUtil.getTailLines(logFile, 5);

		// 위의 %d %5p [%c] 및 마지막 %m%n 의 출력은 동일
		// thread 명이 main 으로만 나올 때도 있고 main [client-10] 과 같이 나오는 경우도 있음
		String patternStr = "^(.*) ([a-zA-Z]+) \\[(org\\.egovframe\\.rte\\.fdl\\.logging\\.sample\\.LogLayoutSample)\\] (.*)$";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(tailLines[0]);
		boolean isMatch = matcher.matches();
		assertTrue(isMatch);

		LogManager.getLogger("sysoutLogger").debug("== tailLines[0] ==");
		LogManager.getLogger("sysoutLogger").debug("%d : " + matcher.group(1));
		LogManager.getLogger("sysoutLogger").debug("%5p : " + matcher.group(2));
		LogManager.getLogger("sysoutLogger").debug("[%c] : " + matcher.group(3));
		LogManager.getLogger("sysoutLogger").debug("%m%n : " + matcher.group(4));

		matcher = pattern.matcher(tailLines[1]);
		isMatch = matcher.matches();
		assertTrue(isMatch);

		LogManager.getLogger("sysoutLogger").debug("== tailLines[1] ==");
		LogManager.getLogger("sysoutLogger").debug("%d : " + matcher.group(1));
		LogManager.getLogger("sysoutLogger").debug("%5p : " + matcher.group(2));
		LogManager.getLogger("sysoutLogger").debug("[%c] : " + matcher.group(3));
		LogManager.getLogger("sysoutLogger").debug("%m%n : " + matcher.group(4));

		matcher = pattern.matcher(tailLines[2]);
		isMatch = matcher.matches();
		assertTrue(isMatch);

		LogManager.getLogger("sysoutLogger").debug("== tailLines[2] ==");
		LogManager.getLogger("sysoutLogger").debug("%d : " + matcher.group(1));
		LogManager.getLogger("sysoutLogger").debug("%5p : " + matcher.group(2));
		LogManager.getLogger("sysoutLogger").debug("[%c] : " + matcher.group(3));
		LogManager.getLogger("sysoutLogger").debug("%m%n : " + matcher.group(4));

		matcher = pattern.matcher(tailLines[3]);
		isMatch = matcher.matches();
		assertTrue(isMatch);

		LogManager.getLogger("sysoutLogger").debug("== tailLines[3] ==");
		LogManager.getLogger("sysoutLogger").debug("%d : " + matcher.group(1));
		LogManager.getLogger("sysoutLogger").debug("%5p : " + matcher.group(2));
		LogManager.getLogger("sysoutLogger").debug("[%c] : " + matcher.group(3));
		LogManager.getLogger("sysoutLogger").debug("%m%n : " + matcher.group(4));

		matcher = pattern.matcher(tailLines[4]);
		isMatch = matcher.matches();
		assertTrue(isMatch);

		LogManager.getLogger("sysoutLogger").debug("== tailLines[4] ==");
		LogManager.getLogger("sysoutLogger").debug("%d : " + matcher.group(1));
		LogManager.getLogger("sysoutLogger").debug("%5p : " + matcher.group(2));
		LogManager.getLogger("sysoutLogger").debug("[%c] : " + matcher.group(3));
		LogManager.getLogger("sysoutLogger").debug("%m%n : " + matcher.group(4));
	}

	/**
	 * 아래는 MDC형태 로그 출력을 테스트하는 메소드
	 * testLogger: level=DEBUG, appender=File
	 * sysoutLogger: level=DEBUG, appender=Console
	 * */
	@Test
	public void testLogMDC() throws Exception {

		// mdc log file
		File mdcFile = new File("./logs/file/mdcSample.log");
		if (!mdcFile.exists()) {
			mdcFile.createNewFile();
		}

		// MDC (mapped diagnostic context) test --> log4j2에서 Thread Context Map으로 교체됨
		// ThreadContext 객체에 key, value값을 put
		ThreadContext.put("class", this.getClass().getSimpleName());
		ThreadContext.put("method", "testLogMDC");
		ThreadContext.put("testKey", "test value");

		// class 패턴이 아닌 명시적인 로그명을 지정 - 일반 로그 처리 시에는 class 패턴을 사용하는 것을 추천
		Logger testLogger = (Logger) LogManager.getLogger("mdcLogger");
		// 로그 출력
		// 파일에 로그 출력 패턴 %d %5p [%c] [%X{class} %X{method} %X{testKey}] %m%n
		testLogger.debug("MDC test!");
		
		
		String lastLine = LogFileUtil.getLastLine(mdcFile);
		// 로그 출력
		// 콘솔에 로그 출력 패턴 %d %5p [%c] [%X{class} %X{method} %X{testKey}] %m%n
		LogManager.getLogger("sysoutLogger").debug(lastLine);
		// 로그 확인
		String patternStr = "^(.*\\]) \\[([a-zA-Z]+) ([a-zA-Z]+) ([a-zA-Z| ]+)\\] (.*)$";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(lastLine);
		boolean isMatch = matcher.matches();
		assertTrue(isMatch);

		LogManager.getLogger("sysoutLogger").debug(lastLine);
		LogManager.getLogger("sysoutLogger").debug(	"%d %5p [%c] : " + matcher.group(1));
		LogManager.getLogger("sysoutLogger").debug(	"%X{class} : " + matcher.group(2));
		LogManager.getLogger("sysoutLogger").debug(	"%X{method} : " + matcher.group(3));
		LogManager.getLogger("sysoutLogger").debug(	"%X{testKey} : " + matcher.group(4));
		LogManager.getLogger("sysoutLogger").debug("%m%n : " + matcher.group(5));
	}
}
