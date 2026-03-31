package org.egovframe.rte.fdl.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.egovframe.rte.fdl.logging.config.LoggingTestConfig;
import org.egovframe.rte.fdl.logging.sample.*;
import org.egovframe.rte.fdl.logging.util.LogFileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LoggingTestConfig.class)
public class LoggerMatchingTest {

    private File logFile;

    @BeforeEach
    public void onSetUp() throws Exception {
        logFile = new File("./logs/file/sample.log");
        if (!logFile.exists()) {
            logFile.createNewFile();
        }
    }

    @Test
    public void testLoggerMatching() throws Exception {
        Logger testClassLog = LogManager.getLogger(LoggerMatchingTest.class.getName());
        testClassLog.debug("logger - org.egovframe");

        Logger layoutSampleLog = LogManager.getLogger(LogLayoutSample.class.getName());
        layoutSampleLog.debug("logger - LogLayoutSample");
        assertTrue(LogFileUtil.getLastLine(logFile).endsWith("logger - LogLayoutSample"));

        Logger logLevelDebugLog = LogManager.getLogger(LogLevelDebug.class.getName());
        logLevelDebugLog.debug("logger - LogLevelDebug");
        assertTrue(LogFileUtil.getLastLine(logFile).endsWith("logger - LogLevelDebug"));

        Logger logLevelInfoLog = LogManager.getLogger(LogLevelInfo.class.getName());
        Logger logLevelWarnLog = LogManager.getLogger(LogLevelWarn.class.getName());
        Logger logLevelErrorLog = LogManager.getLogger(LogLevelError.class.getName());
        Logger logLevelFatalLog = LogManager.getLogger(LogLevelFatal.class.getName());

        logLevelInfoLog.info("logger - LogLevelInfo");
        logLevelWarnLog.warn("logger - LogLevelWarn");
        logLevelErrorLog.error("logger - LogLevelError");
        logLevelFatalLog.fatal("logger - LogLevelFatal");

        String[] tailLines = LogFileUtil.getTailLines(logFile, 4);
        assertTrue(tailLines[0].endsWith("logger - LogLevelInfo"));
        assertTrue(tailLines[1].endsWith("logger - LogLevelWarn"));
        assertTrue(tailLines[2].endsWith("logger - LogLevelError"));
        assertTrue(tailLines[3].endsWith("logger - LogLevelFatal"));

        Logger logTestSampleLog = LogManager.getLogger(LogTestSample.class.getName());
        logTestSampleLog.debug("logger - org.egovframe");

        Logger mdcLogger = LogManager.getLogger("mdcLogger");
        ThreadContext.put("class", this.getClass().getSimpleName());
        ThreadContext.put("method", "testLogMDC");
        ThreadContext.put("testKey", "test value");
        mdcLogger.debug("MDC test!");

        File mdcFile = new File("./logs/file/mdcSample.log");
        assertTrue(LogFileUtil.getLastLine(mdcFile).endsWith("DEBUG [mdcLogger] [LoggerMatchingTest testLogMDC test value] MDC test!"));

        Logger notExistLog = LogManager.getLogger("notExistLogger");
        notExistLog.debug("DEBUG - logger - org.egovframe");
        notExistLog.error("ERROR - logger - org.egovframe");
    }

}
