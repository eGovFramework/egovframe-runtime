package org.egovframe.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("logTestSample")
public class LogTestSample {

    private static Logger LOGGER = LogManager.getLogger(LogTestSample.class.getName());

    public void executeSomeLogic() {
        LOGGER.debug("DEBUG - LogTestSample.executeSomeLogic executed");
        LOGGER.info("INFO - LogTestSample.executeSomeLogic executed");
        LOGGER.warn("WARN - LogTestSample.executeSomeLogic executed");
        LOGGER.error("ERROR - LogTestSample.executeSomeLogic executed");
        LOGGER.fatal("FATAL - LogTestSample.executeSomeLogic executed");
    }

    public Logger getTargetLogger() {
        return LogTestSample.LOGGER;
    }

}
