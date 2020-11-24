package org.egovframe.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("logLevelError")
public class LogLevelError {

	private static Logger logger = LogManager.getLogger(LogLevelError.class.getName());
	
	public void executeSomeLogic() {
		logger.error("ERROR - LogLevelError.executeSomeLogic executed");
	}
}
