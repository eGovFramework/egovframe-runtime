package egovframework.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("logLevelWarn")
public class LogLevelWarn {

	private static Logger logger = LogManager.getLogger(LogLevelWarn.class.getName());

	public void executeSomeLogic() {
		logger.warn("WARN - LogLevelWarn.executeSomeLogic executed");
	}
}
