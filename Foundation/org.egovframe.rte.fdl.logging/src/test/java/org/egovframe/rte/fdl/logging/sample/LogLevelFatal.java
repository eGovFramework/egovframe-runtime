package org.egovframe.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("logLevelFatal")
public class LogLevelFatal {

    private static final Logger LOGGER = LogManager.getLogger(LogLevelFatal.class.getName());

    public void executeSomeLogic() {
        LOGGER.fatal("FATAL - LogLevelFatal.executeSomeLogic executed");
    }

}
