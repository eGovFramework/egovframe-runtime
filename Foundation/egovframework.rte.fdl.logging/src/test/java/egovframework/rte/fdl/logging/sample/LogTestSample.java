package egovframework.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("logTestSample")
public class LogTestSample {

	private static Logger logger = LogManager.getLogger(LogTestSample.class.getName());

	public void executeSomeLogic() {
		logger.debug("DEBUG - LogTestSample.executeSomeLogic executed");
		logger.info("INFO - LogTestSample.executeSomeLogic executed");
		logger.warn("WARN - LogTestSample.executeSomeLogic executed");
		logger.error("ERROR - LogTestSample.executeSomeLogic executed");
		logger.fatal("FATAL - LogTestSample.executeSomeLogic executed");
	}

	public Logger getTargetLogger() {
		return LogTestSample.logger;
	}
}
