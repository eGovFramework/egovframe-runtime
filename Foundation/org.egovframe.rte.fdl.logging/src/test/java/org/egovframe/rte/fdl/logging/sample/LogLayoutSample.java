package org.egovframe.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("logLayoutSample")
public class LogLayoutSample {

    private static final Logger LOGGER = LogManager.getLogger(LogLayoutSample.class.getName());

    public void executeSomeLogic() {
        LOGGER.debug("DEBUG - LogLayoutSample.executeSomeLogic executed");
        LOGGER.info("INFO - LogLayoutSample.executeSomeLogic executed");
        LOGGER.warn("WARN - LogLayoutSample.executeSomeLogic executed");
        LOGGER.error("ERROR - LogLayoutSample.executeSomeLogic executed");
        LOGGER.fatal("FATAL - LogLayoutSample.executeSomeLogic executed");
    }

    public Logger getTargetLogger() {
        return LogLayoutSample.LOGGER;
    }

}
