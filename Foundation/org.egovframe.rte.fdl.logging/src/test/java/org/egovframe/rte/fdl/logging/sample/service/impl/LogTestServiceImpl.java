package org.egovframe.rte.fdl.logging.sample.service.impl;

import org.egovframe.rte.fdl.logging.sample.service.LogTestService;
import org.egovframe.rte.fdl.logging.sample.service.SomeVO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service("logTestService")
public class LogTestServiceImpl implements LogTestService {

	private static Logger logger = LogManager.getLogger(LogTestServiceImpl.class.getName());
	
	public void executeSomeLogic(SomeVO vo) {
		if (logger.isDebugEnabled()) {
			logger.debug("DEBUG - LogTestServiceImpl.executeSomeLogic executed");
		}

		if (logger.isInfoEnabled()) {
			logger.info("INFO - LogTestServiceImpl.executeSomeLogic executed");
		}

		if (logger.isWarnEnabled()) {
			logger.warn("WARN - LogTestServiceImpl.executeSomeLogic executed");
		}

		if (logger.isErrorEnabled()) {
			logger.error("ERROR - LogTestServiceImpl.executeSomeLogic executed");
		}

		if (logger.isFatalEnabled()) {
			logger.fatal("FATAL - LogTestServiceImpl.executeSomeLogic executed");
		}
	}
}
