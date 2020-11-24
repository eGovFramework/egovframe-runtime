package org.egovframe.rte.fdl.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class Log4j2ParameterizedTest {

	Logger logger = LogManager.getLogger();
	long startTime;
	long endTime;
			
	String testStr = "Welcome To eGovFrame";
	
	
	/**
	 *  아래는 Log4j2에 추가된 Parameterized Logging의 성능을 비교하는 테스트 메소드
	 *  logger: level=TRACE, appender=Console
	 * */
	@Test
	public void testParameterizedLogging() {
		
		// String Concatenation 방식으로 로깅
		startTime = System.currentTimeMillis();
		
		// logger가 debug레벨로 활성화되었는지 확인
		if(logger.isDebugEnabled()) {
			// String Concatenation 후 debug레벨로 활성화되었는지 다시 확인
		 logger.debug("Hellow eGovFrame" + testStr);
		}
		endTime = System.currentTimeMillis();
		
		logger.debug("## String Concatation 방식 시작시간: " + startTime);
		logger.debug("## String Concatation 방식 종료시간: " + endTime);
		logger.debug("## String Concatation 방식 소요시간: " + (endTime - startTime) / 1000.0f + "초");
		
		
		// Parameterized 방식으로 로깅
		startTime = System.currentTimeMillis();
		
		// logger가 debug레벨로 활성화되었는지 확인 후 파라미터 대입
		logger.debug("Hellow eGovFrame {}", testStr);
		endTime = System.currentTimeMillis();
		
		logger.debug("## Parameterized 방식 시작시간: " + startTime);
		logger.debug("## Parameterized 방식 종료시간: " + endTime);
		logger.debug("## Parameterized 방식 소요시간: " + (endTime - startTime) / 1000.0f + "초");
	}

}
