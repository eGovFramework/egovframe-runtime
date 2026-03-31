package org.egovframe.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("logLevelInfo")
public class LogLevelInfo {

    private static final Logger LOGGER = LogManager.getLogger(LogLevelInfo.class.getName());

    public void executeSomeLogic() {
        LOGGER.info("INFO - LogLevelInfo.executeSomeLogic executed");
    }

}
