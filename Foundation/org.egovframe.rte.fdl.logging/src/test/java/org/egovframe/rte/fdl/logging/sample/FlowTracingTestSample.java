package org.egovframe.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("flowTracingTestSample")
public class FlowTracingTestSample {

	// logger: level=TRACE, appender=Console, File
	private static Logger logger = LogManager.getLogger("flowTracingLogger");

	private String[] messages = new String[] { "Hello", "eGov", "Framework" };
	int num = 0;
	
	public String retrieveMessage() {
		logger.entry();
		String testMsg = "";
		try {
			testMsg = getMessage(num);
			num++;
		} catch (Exception ex) {
			logger.error("An exception have been thrown");
			logger.catching(ex);
		}

		return logger.exit(testMsg);
	}

	public String getMessage(int key) {
		logger.entry(key);
		String value = messages[key];
		return logger.exit(value);
	}

}
