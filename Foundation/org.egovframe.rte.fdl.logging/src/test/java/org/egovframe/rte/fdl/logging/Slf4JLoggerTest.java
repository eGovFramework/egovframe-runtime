package org.egovframe.rte.fdl.logging;

import org.egovframe.rte.fdl.logging.config.LoggingTestConfig;
import org.egovframe.rte.fdl.logging.sample.LogLayoutSample;
import org.egovframe.rte.fdl.logging.util.LogFileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LoggingTestConfig.class)
public class Slf4JLoggerTest {

    /**
     * 아래는 SLF4J와 Log4j를 함께 사용하는 테스트 메소드
     * slf4jLogger: level=DEBUG, appender=Console
     * slf4jFileLogger: level=DEBUG, appender=File
     */
    @Test
    public void testSlf4jLogger() throws Exception {
        Logger slf4jLogger = LoggerFactory.getLogger("org.egovframe");
        String arg = "some argument";
        slf4jLogger.debug("Slf4jLoggerTest - {}", arg);

        Logger slf4jFileLogger = LoggerFactory.getLogger(LogLayoutSample.class);

        Object[] arguments = new Object[3];
        arguments[0] = "1st";
        arguments[1] = Integer.valueOf("2");
        arguments[2] = new Date().toString();

        slf4jFileLogger.debug("Slf4jLoggerTest - {} {} {}", arguments);

        File logFile = new File("./logs/file/sample.log");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        String logLine = LogFileUtil.getLastLine(logFile);

        assertTrue(logLine.contains(sdf.format(new Date())));
        assertTrue(logLine.contains("Slf4jLoggerTest - 1st 2 " + (new Date()).toString().substring(0, 16)));
    }

}
