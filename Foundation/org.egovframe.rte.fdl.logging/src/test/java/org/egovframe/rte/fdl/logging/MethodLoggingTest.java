package org.egovframe.rte.fdl.logging;

import org.egovframe.rte.fdl.logging.sample.service.LogTestService;
import org.egovframe.rte.fdl.logging.sample.service.SomeVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath*:META-INF/spring/context-common.xml",
		"classpath*:META-INF/spring/context-aspect.xml" })
public class MethodLoggingTest {

	@Resource(name = "logTestService")
	LogTestService logTestService;

	/**
	 * 아래는 AOP 로깅처리를 테스트하는 메소드
	 * */
	@Test
	public void testAOPLoggingTest() throws Exception {
		SomeVO vo = new SomeVO();
		vo.setSomeAttr("some");

		// AOP설정에 따라 MethodParameterLoggingAspect 클래스의 beforeLog메서드 내 로그 출력이 먼저 발생

		// 아래 메소드 내에서 호출하는 logger는 log4j2.xml 에 존재하지 않으므로
		//  Named Hierarchy에 따라 이름이 가장 많이 매칭되는 "egovframework" 이름의 Logger설정을 따른다.
		// DEBUG, INFO, WARN, ERROR, FATAL 에 대한 모든 로그가 출력됨
		logTestService.executeSomeLogic(vo);
		// 하단 Console에서 로그 확인
	}
}