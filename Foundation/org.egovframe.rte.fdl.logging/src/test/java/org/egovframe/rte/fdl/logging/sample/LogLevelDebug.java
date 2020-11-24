package org.egovframe.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("logLevelDebug")
public class LogLevelDebug {

	private static Logger logger = LogManager.getLogger(LogLevelDebug.class.getName());

	public void executeSomeLogic() {
		logger.debug("DEBUG - LogLevelDebug.executeSomeLogic executed");
	}
}