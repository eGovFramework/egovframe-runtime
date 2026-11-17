package org.egovframe.rte.fdl.logging;

import jakarta.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egovframe.rte.fdl.logging.config.LoggingTestConfig;
import org.egovframe.rte.fdl.logging.sample.FlowTracingTestSample;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LoggingTestConfig.class)
public class Log4j2FlowTracingTest {

    /**
     * 아래는 Log4j2에 추가된 Flow Tracing과 디폴트 Marker API를 테스트하는 메소드
     * logger: level=DEBUG, appender=Console
     */
    @Resource(name = "flowTracingTestSample")
    private FlowTracingTestSample sample;

    @Test
    public void testFlowTracing() {

        // Logger 미지정 : 이름은 패키지명.클래스명 (log4j2.xml 에 없으므로 Named Hierarchy 상 "egovframework" Logger 설정 적용)
        // 로거명 "org.egovframe.rte.fdl.logging.FlowTracingTest"
        Logger logger = LogManager.getLogger();

        // 로드 출력
        logger.debug("FlowTracing Test Start");

        for (int i = 0; i < 4; i++) {
            sample.retrieveMessage();
        }

        logger.debug("FlowTracing Test End");
    }

}
