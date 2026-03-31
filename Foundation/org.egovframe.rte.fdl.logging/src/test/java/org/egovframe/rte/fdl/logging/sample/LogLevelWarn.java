package org.egovframe.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("logLevelWarn")
public class LogLevelWarn {

    private static final Logger LOGGER = LogManager.getLogger(LogLevelWarn.class.getName());

    public void executeSomeLogic() {
        LOGGER.warn("WARN - LogLevelWarn.executeSomeLogic executed");
    }

}
