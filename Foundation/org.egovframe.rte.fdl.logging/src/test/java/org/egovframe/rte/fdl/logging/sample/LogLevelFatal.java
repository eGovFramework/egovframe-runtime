package org.egovframe.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("logLevelFatal")
public class LogLevelFatal {

	private static Logger logger = LogManager.getLogger(LogLevelFatal.class.getName());

	public void executeSomeLogic() {
		logger.fatal("FATAL - LogLevelFatal.executeSomeLogic executed");
	}
}