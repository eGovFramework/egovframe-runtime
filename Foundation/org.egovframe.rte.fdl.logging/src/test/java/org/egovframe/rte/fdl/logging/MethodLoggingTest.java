package org.egovframe.rte.fdl.logging;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.logging.config.LoggingTestConfig;
import org.egovframe.rte.fdl.logging.sample.service.LogTestService;
import org.egovframe.rte.fdl.logging.sample.service.SomeVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LoggingTestConfig.class)
public class MethodLoggingTest {

    @Resource(name = "logTestService")
    LogTestService logTestService;

    /**
     * AOP 로깅처리를 테스트하는 메소드
     */
    @Test
    public void testAOPLoggingTest() throws Exception {
        SomeVO vo = new SomeVO();
        vo.setSomeAttr("some");

        // AOP설정에 따라 MethodParameterLoggingAspect 클래스의 beforeLog메서드 내 로그 출력이 먼저 발생
        // 아래 메소드의 logger 는 log4j2.xml 에 없으므로 Named Hierarchy 상 "egovframework" Logger 설정 적용
        // DEBUG, INFO, WARN, ERROR, FATAL 로그 모두 출력
        logTestService.executeSomeLogic(vo);
    }

}
