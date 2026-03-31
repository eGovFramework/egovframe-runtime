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

        // Logger를 지정하지 않으면, Logger name이 디폴트값(패키지명.클래스명)으로 지정된다.
        // 그러나 log4j2.xml에는 디폴트값에 해당하는 Logger가 설정되어 있지 않으므로,
        // Named Hierarchy에 따라 이름이 가장 많이 매칭되는 "egovframework" 이름의 Logger설정을 따른다.
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
