package org.egovframe.rte.fdl.exception.handler;

import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.exception.handler.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OthersServiceExceptionHandler implements ExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OthersServiceExceptionHandler.class);

    public void occur(Exception exception, String packageName) {
        try {
            if (exception instanceof EgovBizException) {
                Exception exx = (Exception) ((EgovBizException) exception).getWrappedException();
                if (exx != null) {
                    exx.getMessage();
                }
            }
            LOGGER.debug("### OthersServiceExceptionHandler occur() >>> Sending a alert mail is completed");
        } catch (Exception e) {
            LOGGER.debug("### OthersServiceExceptionHandler occur() Error >>> {}", e.getMessage());
        }
    }

}
