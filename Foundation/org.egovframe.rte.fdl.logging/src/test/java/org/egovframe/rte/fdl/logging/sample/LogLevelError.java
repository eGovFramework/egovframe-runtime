package org.egovframe.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("logLevelError")
public class LogLevelError {

    private static final Logger LOGGER = LogManager.getLogger(LogLevelError.class.getName());

    public void executeSomeLogic() {
        LOGGER.error("ERROR - LogLevelError.executeSomeLogic executed");
    }

}
