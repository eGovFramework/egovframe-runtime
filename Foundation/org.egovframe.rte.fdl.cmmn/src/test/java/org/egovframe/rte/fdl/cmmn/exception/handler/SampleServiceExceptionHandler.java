package org.egovframe.rte.fdl.cmmn.exception.handler;

import jakarta.annotation.Resource;
import org.egovframe.rte.mail.SimpleSSLMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception 발생시 조건 매칭후 메일을 발송한다.
 */
public class SampleServiceExceptionHandler implements ExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleServiceExceptionHandler.class);

    @Resource(name = "egovSampleSSLMailSender")
    private SimpleSSLMail mailSender;

    public void occur(Exception ex, String packageName) {
        try {
            mailSender.send("[Exception Handler Subject]", ex.getMessage() + " occur !!! find the exception problem ");
            LOGGER.debug("### SampleServiceExceptionHandler >>> Sending a alert mail is completed");
        } catch (Exception e) {
            LOGGER.debug("[{}] SampleServiceExceptionHandler occur() : {}", e.getClass().getName(), e.getMessage());
        }
    }

}
