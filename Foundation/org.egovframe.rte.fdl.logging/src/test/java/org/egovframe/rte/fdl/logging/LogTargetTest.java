package org.egovframe.rte.fdl.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egovframe.rte.fdl.logging.sample.LogLayoutSample;
import org.egovframe.rte.fdl.logging.util.LogFileUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class LogTargetTest {

    @Test
    public void testConsoleAppender() throws Exception {
        Logger logger = LogManager.getLogger("org.egovframe");
        logger.debug("ConsoleAppender test");
    }

    @Test
    public void testFileAppender() throws Exception {
        Logger logger = LogManager.getLogger(LogLayoutSample.class.getName());
        File logFile = new File("./logs/file/sample.log");

        logger.debug("FileAppender test");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        assertTrue(LogFileUtil.getLastLine(logFile).contains(sdf.format(new Date())));
        assertTrue(LogFileUtil.getLastLine(logFile).endsWith("FileAppender test"));
    }

    @Test
    public void testRollingFileAppender() throws Exception {
        Logger logger = LogManager.getLogger("rollingLogger");
        File logFile = new File("./logs/rolling/rollingSample.log");

        logger.debug("RollingFileAppender test");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        assertTrue(LogFileUtil.getLastLine(logFile).contains(sdf.format(new Date())));
        assertTrue(LogFileUtil.getLastLine(logFile).endsWith("RollingFileAppender test"));

        for (int i = 0; i < 1000; i++) {
            logger.debug("RollingFileAppender loop : " + i);
        }

        File logFileDir = new File("./logs/rolling");
        assertEquals(4, logFileDir.listFiles().length);
        for (File tempFile : logFileDir.listFiles()) {
            assertTrue(10000 >= tempFile.length());
        }
    }

    @Test
    public void testDailyRollingFileAppender() throws Exception {
        Logger logger = LogManager.getLogger("dailyLogger");

        logger.debug("DailyRollingFileAppender test");

        File logFile = new File("./logs/daily/dailyRollingSample.log");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        assertTrue(LogFileUtil.getLastLine(logFile).contains(sdf.format(new Date())));
        assertTrue(LogFileUtil.getLastLine(logFile).endsWith("DailyRollingFileAppender test"));

        Thread.sleep(1000);
        logger.debug("DailyRollingFileAppender - file change test");

        assertTrue(LogFileUtil.getLastLine(logFile).contains(sdf.format(new Date())));
        assertTrue(LogFileUtil.getLastLine(logFile).endsWith("DailyRollingFileAppender - file change test"));

        File logFileDir = new File("./logs/daily");
        File[] files = logFileDir.listFiles();
        assertNotNull(files);
        assertTrue(files.length >= 2, "daily 디렉터리에 롤링된 파일을 포함해 2개 이상의 파일이 있어야 함");

        SimpleDateFormat logDatePattern = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String todayStr = logDatePattern.format(new Date());
        boolean hasTodayRolledFile = false;
        for (File f : files) {
            if (f.getName().contains(todayStr)) {
                hasTodayRolledFile = true;
                break;
            }
        }
        assertTrue(hasTodayRolledFile, "오늘 날짜(" + todayStr + ")를 포함한 롤링 파일이 하나 이상 있어야 함");
    }
}
