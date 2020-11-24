package org.egovframe.rte.fdl.logging;

import org.egovframe.rte.fdl.logging.sample.FlowTracingTestSample;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-common.xml" })
public class Log4j2FlowTracingTest {

	/**
	 *  아래는 Log4j2에 추가된 Flow Tracing과 디폴트 Marker API를 테스트하는 메소드
	 *  logger: level=DEBUG, appender=Console
	 * */
	@Resource(name="flowTracingTestSample")
	FlowTracingTestSample sample;
	
	@Test
	public void testFlowTracing() {
		
		// Logger를 지정하지 않으면, Logger name이 디폴트값(패키지명.클래스명)으로 지정된다.
		Logger logger = LogManager.getLogger();  // 로거명 "org.egovframe.rte.fdl.logging.FlowTracingTest"
		// 그러나 log4j2.xml에는 디폴트값에 해당하는 Logger가 설정되어 있지 않으므로, 
		// Named Hierarchy에 따라 이름이 가장 많이 매칭되는 "egovframework" 이름의 Logger설정을 따른다.
		// System.out.println(((org.apache.logging.log4j.core.Logger)logger).getParent().getName());
		
		// 로드 출력
		logger.debug("FlowTracing Test Start");

		for(int i=0; i<4; i++) {
			sample.retrieveMessage();
		}
		
		logger.debug("FlowTracing Test End");

		
		// 결과
		// Console에는 모든 log level이 로깅 -- entry(), exit(), catching(), error() 로깅
		// File에는 등록된 MarkerFilter의 marker에 따라 출력할 로그가 filtering	
		//			출력파일명: ./logs/file/flowtracing/makerFilter_FLOW -- FLOW marker 로그 출력
		// 								./logs/file/flowtracing/makerFilter_EXCEPTION -- EXCEPTION marker 로그 출력
		
		// ref)
		// Flow Tracing API    |    Log level    |    Marker Name
		// entry()                     |    TRACE        |    ENTER 또는 FLOW
		// exit()                        |   TRACE         |    EXIT 또는 FLOW
		// catching()               |    ERROR        | CATCHING 또는 EXCEPTION
		// throwing()              | ERROR           | THROWING 또는 EXCEPTION
	}
}
