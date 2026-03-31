package org.egovframe.rte.fdl.logging;

import jakarta.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.egovframe.rte.fdl.logging.config.LoggingTestConfig;
import org.egovframe.rte.fdl.logging.sample.MarkerFilterTestSample;
import org.egovframe.rte.fdl.logging.util.LogFileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ObjectUtils;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LoggingTestConfig.class)
public class Log4j2FilterTest {

    @Resource(name = "markerFilterTestService")
    MarkerFilterTestSample markerFilterTestSample;

    @Test
    public void testThresholdFilter() throws Exception {
        Logger LOGGER = LogManager.getLogger("thresholdFilterLogger");
        LOGGER.debug("### Log4j2FilterTest testThresholdFilter() Start");

        try {
            @SuppressWarnings("unused")
            int value = 5 / 0;
        } catch (ArithmeticException e) {
            LOGGER.error("An ArithmeticException have been thrown");
            LOGGER.catching(e);
        }

        LOGGER.debug("### Log4j2FilterTest testThresholdFilter() End");

        String logFileDir = "./logs/file/filter/ThresholdFilter.log";
        File logFile = new File(logFileDir);

        if (!ObjectUtils.isEmpty(logFile)) {
            assertFalse(LogFileUtil.contains(logFileDir, "DEBUG"));
            assertEquals(2, LogFileUtil.countWords(logFileDir, "ERROR"));

            String[] tailLines = LogFileUtil.getTailLines(logFile, LogFileUtil.countLines(logFileDir));

            assertTrue(tailLines[0].endsWith("- An ArithmeticException have been thrown"));
            assertTrue(tailLines[1].endsWith("- Catching"));
        }
    }

    @Test
    public void testDynamicThresholdFilter() throws Exception {
        Logger LOGGER = LogManager.getLogger("dynamicThresholdFilterLogger");

        ThreadContext.put("loginId", "admin1");
        LOGGER.debug("DEBUG - loginId: admin1");
        LOGGER.info("INFO - loginId: admin1");
        LOGGER.warn("WARN - loginId: admin1");
        LOGGER.error("ERROR - loginId: admin1");
        LOGGER.fatal("FATAL - loginId: admin1");

        ThreadContext.put("loginId", "admin2");
        LOGGER.debug("DEBUG - loginId: admin2");
        LOGGER.info("INFO - loginId: admin2");
        LOGGER.warn("WARN - loginId: admin2");
        LOGGER.error("ERROR - loginId: admin2");
        LOGGER.fatal("FATAL - loginId: admin2");

        ThreadContext.put("loginId", "admin3");
        LOGGER.debug("DEBUG - loginId: admin3");
        LOGGER.info("INFO - loginId: admin3");
        LOGGER.warn("WARN - loginId: admin3");
        LOGGER.error("ERROR - loginId: admin3");
        LOGGER.fatal("FATAL - loginId: admin3");

        String logFileDir = "./logs/file/filter/DynamicThresholdFilter.log";
        File logFile = new File(logFileDir);

        if (!ObjectUtils.isEmpty(logFile)) {
            int numLines = LogFileUtil.countLines(logFileDir);
            assertEquals(10, numLines);

            String[] tailLines = LogFileUtil.getTailLines(logFile, 10);
            assertTrue(tailLines[0].endsWith("DEBUG - loginId: admin1"));
            assertTrue(tailLines[1].endsWith("INFO - loginId: admin1"));
            assertTrue(tailLines[2].endsWith("WARN - loginId: admin1"));
            assertTrue(tailLines[3].endsWith("ERROR - loginId: admin1"));
            assertTrue(tailLines[4].endsWith("FATAL - loginId: admin1"));
            assertTrue(tailLines[5].endsWith("WARN - loginId: admin2"));
            assertTrue(tailLines[6].endsWith("ERROR - loginId: admin2"));
            assertTrue(tailLines[7].endsWith("FATAL - loginId: admin2"));
            assertTrue(tailLines[8].endsWith("ERROR - loginId: admin3"));
            assertTrue(tailLines[9].endsWith("FATAL - loginId: admin3"));
        }
    }

    @Test
    public void testMarkerFilter() throws Exception {
        String userId = "egov";

        markerFilterTestSample.doSelectUser(userId);
        markerFilterTestSample.doInsertUser(userId);
        markerFilterTestSample.doUpdateUser(userId);
        markerFilterTestSample.doDeleteUser(userId);

        String logFileDir = "./logs/file/filter/MarkerFilter.log";
        File logFile = new File(logFileDir);

        if (!ObjectUtils.isEmpty(logFile)) {
            Boolean printSELECT = LogFileUtil.contains(logFileDir, "SELECT[ SQL ]");
            Boolean printINSERT = LogFileUtil.contains(logFileDir, "INSERT[ SQL ]");
            Boolean printUPDATE = LogFileUtil.contains(logFileDir, "UPDATE[ SQL ]");
            Boolean printDELETE = LogFileUtil.contains(logFileDir, "DELETE[ SQL ]");

            assertEquals(false, printSELECT);
            assertEquals(true, printINSERT);
            assertEquals(false, printUPDATE);
            assertEquals(false, printDELETE);
        }
    }

    @Test
    public void testRegexFilter() throws Exception {
        Logger LOGGER = LogManager.getLogger("regexFilterLogger");
        LOGGER.debug("RegexFilterTest Start");

        LOGGER.debug("DEBUG - RegexFilter Test !!");
        LOGGER.info("INFO - RegexFilter Test !!");
        LOGGER.warn("WARN - RegexFilterTest !!");
        LOGGER.error("ERROR - RegexFilter Test !!");
        LOGGER.fatal("FATAL - RegexFilter Test !!");
        LOGGER.debug("RegexFilterTest End");

        File logFile = new File("./logs/file/filter/RegexFilter.log");
        String[] tailLines = LogFileUtil.getTailLines(logFile, 4);

        assertTrue(tailLines[0].endsWith("DEBUG - RegexFilter Test !!"));
        assertTrue(tailLines[1].endsWith("INFO - RegexFilter Test !!"));
        assertTrue(tailLines[2].endsWith("ERROR - RegexFilter Test !!"));
        assertTrue(tailLines[3].endsWith("FATAL - RegexFilter Test !!"));
    }

}
