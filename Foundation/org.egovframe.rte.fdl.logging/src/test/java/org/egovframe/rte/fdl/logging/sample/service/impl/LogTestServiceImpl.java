package org.egovframe.rte.fdl.logging.sample.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egovframe.rte.fdl.logging.sample.service.LogTestService;
import org.egovframe.rte.fdl.logging.sample.service.SomeVO;
import org.springframework.stereotype.Service;

@Service("logTestService")
public class LogTestServiceImpl implements LogTestService {

    private static final Logger LOGGER = LogManager.getLogger(LogTestServiceImpl.class.getName());

    public void executeSomeLogic(SomeVO vo) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("DEBUG - LogTestServiceImpl.executeSomeLogic executed");
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("INFO - LogTestServiceImpl.executeSomeLogic executed");
        }

        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("WARN - LogTestServiceImpl.executeSomeLogic executed");
        }

        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("ERROR - LogTestServiceImpl.executeSomeLogic executed");
        }

        if (LOGGER.isFatalEnabled()) {
            LOGGER.fatal("FATAL - LogTestServiceImpl.executeSomeLogic executed");
        }
    }

}
