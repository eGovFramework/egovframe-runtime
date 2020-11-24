package org.egovframe.rte.fdl.logging;

import static org.junit.Assert.assertEquals;

import org.egovframe.rte.fdl.logging.sample.LogTestSample;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-common.xml" })
public class LoggerHierarchyTest {

	@Resource(name = "logTestSample")
	LogTestSample logTestSample;
	
	/**
	 * 아래는 Logger Hierarchy를 테스트하는 메소드
	 * egovframework Logger: level=DEBUG, appender=Console
	 * targetLogger: level=DEBUG, appender=Console
	 * */
	@Test
	public void testLoggerHierarchy() throws Exception {

		// Logger targetLogger = LogManager.getLogger("LogTestSample"); 과 동일
		// log4j2.xml에는 "LogTestSample" 이름의 Logger가 존재하지 않으므로
		// Named Hierarchy에 따라 이름이 가장 많이 매칭되는 "egovframework" 이름의 Logger설정을 따른다.
		Logger targetLogger = (Logger) logTestSample.getTargetLogger();
		assertEquals("org.egovframe.rte.fdl.logging.sample.LogTestSample", targetLogger.getName());
		
		// targetLogger의 ParentLogger가 org.egovframe 로거인지 확인
		Logger parentLogger = targetLogger.getParent();
		Logger egovLogger = (Logger) LogManager.getLogger("org.egovframe");
		assertEquals(egovLogger.getName(), parentLogger.getName());
		
		assertEquals(targetLogger.getLevel(), egovLogger.getLevel());
		assertEquals(targetLogger.getAppenders(), egovLogger.getAppenders());
		assertEquals(targetLogger.getLevel(), egovLogger.getLevel());
		assertEquals(targetLogger.isAdditive(), egovLogger.isAdditive());  // getAdditivity() - log4j1.x

		// 로그 출력
		// 현재 org.egovframe 로그 레벨이 log4j2.xml 에 DEBUG 로 정의돼 있으므로
		// 해당 로거 설정을 따르는 logTestSample 의 메서드를 실행하면
		// DEBUG, INFO, WARN, ERROR, FATAL 에 대한 모든 로그가 출력됨
		logTestSample.executeSomeLogic();
		// 하단 Console에서 로그 확인
	}
}
