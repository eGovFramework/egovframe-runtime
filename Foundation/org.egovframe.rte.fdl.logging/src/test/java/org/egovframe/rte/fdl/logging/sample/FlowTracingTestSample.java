package org.egovframe.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("flowTracingTestSample")
public class FlowTracingTestSample {

    private static final Logger LOGGER = LogManager.getLogger("flowTracingLogger");

    String[] messages = new String[]{"Hello", "eGov", "Framework"};
    int num = 0;

    public void retrieveMessage() {
        LOGGER.traceEntry();
        String testMessage = "";
        try {
            testMessage = getMessage(num);
            num++;
        } catch (Exception e) {
            LOGGER.debug("[{}] FlowTracingTestSample retrieveMessage() An exception have been thrown : {}",
                    e.getClass().getName(), e.getMessage());
            LOGGER.catching(e);
        }

        LOGGER.traceExit(testMessage);
    }

    public String getMessage(int key) {
        LOGGER.traceEntry(String.valueOf(key));
        String testMessage = messages[key];
        return LOGGER.traceExit(testMessage);
    }

}
