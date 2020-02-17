package egovframework.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("logLayoutSample")
public class LogLayoutSample {

	private static Logger logger = LogManager.getLogger(LogLayoutSample.class	.getName());

	public void executeSomeLogic() {
		logger.debug("DEBUG - LogLayoutSample.executeSomeLogic executed");
		logger.info("INFO - LogLayoutSample.executeSomeLogic executed");
		logger.warn("WARN - LogLayoutSample.executeSomeLogic executed");
		logger.error("ERROR - LogLayoutSample.executeSomeLogic executed");
		logger.fatal("FATAL - LogLayoutSample.executeSomeLogic executed");
	}

	public Logger getTargetLogger() {
		return LogLayoutSample.logger;
	}

}
