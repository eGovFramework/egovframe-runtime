package egovframework.rte.fdl.exception.handler;

import egovframework.rte.fdl.cmmn.exception.EgovBizException;
import egovframework.rte.fdl.cmmn.exception.handler.ExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OthersServiceExceptionHandler implements ExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(OthersServiceExceptionHandler.class);

	public void occur(Exception exception, String packageName) {
		LOGGER.debug(" OthersServiceExceptionHandler run...............{}", ((EgovBizException) exception).getWrappedException());

		try {
			if( exception instanceof EgovBizException){
				Exception exx = (Exception) ((EgovBizException) exception).getWrappedException();
				if(exx != null){
					exx.printStackTrace();
				}
			}

			LOGGER.debug(" sending a alert mail  is completed ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
