package org.egovframe.rte.fdl.cmmn;

import org.egovframe.rte.fdl.cmmn.config.FdlExceptionConfig;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = FdlExceptionConfig.class)
public class FdlExceptionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FdlExceptionTest.class);

    @Autowired
    private FdlExceptionTestService fdlExceptionTestService;

    @Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;

    @Test
    public void TestThrowFdlException() {
        LOGGER.debug("### TestThrowFdlException Start ");

        LOGGER.debug("### TestThrowFdlException #1 Start ");
        try {
            fdlExceptionTestService.testFdlException(1);
        } catch (FdlException fe) {
            assertEquals("FdlException without message", fe.getMessage());
        }
        LOGGER.debug("### TestThrowFdlException #1 End ");

        LOGGER.debug("### TestThrowFdlException #2 Start ");
        try {
            fdlExceptionTestService.testFdlException(2);
        } catch (FdlException fe) {
            assertEquals("message", fe.getMessage());
        }
        LOGGER.debug("### TestThrowFdlException #2 End ");

        LOGGER.debug("### TestThrowFdlException #3 Start ");
        try {
            fdlExceptionTestService.testFdlException(3);
        } catch (FdlException fe) {
            assertEquals("message", fe.getMessage());
            assertEquals("TEST FdlException", fe.getCause().getMessage());
        }
        LOGGER.debug("### TestThrowFdlException #3 End ");

        LOGGER.debug("### TestThrowFdlException #4 Start ");
        try {
            fdlExceptionTestService.testFdlException(4);
        } catch (FdlException fe) {
            assertEquals("message 1", fe.getMessage());
            assertEquals("TEST FdlException", fe.getCause().getMessage());
        }
        LOGGER.debug("### TestThrowFdlException #4 End ");

        LOGGER.debug("### TestThrowFdlException #5 Start ");
        try {
            fdlExceptionTestService.testFdlException(5);
        } catch (FdlException fe) {
            assertEquals("error.fdl.msg1", fe.getMessageKey());
            assertEquals("message1", fe.getMessage());
        }
        LOGGER.debug("### TestThrowFdlException #5 End ");

        LOGGER.debug("### TestThrowFdlException #6 Start ");
        try {
            fdlExceptionTestService.testFdlException(6);
        } catch (FdlException fe) {
            assertEquals("error.fdl.msg2", fe.getMessageKey());
            assertEquals("message2", fe.getMessage());
            assertEquals("TEST FdlException", fe.getCause().getMessage());
        }
        LOGGER.debug("### TestThrowFdlException #6 End ");

        LOGGER.debug("### TestThrowFdlException #7 Start ");
        try {
            fdlExceptionTestService.testFdlException(7);
        } catch (FdlException fe) {
            assertEquals("error.fdl.msg3", fe.getMessageKey());
            assertEquals("메세지3", fe.getMessage());
            assertEquals("TEST FdlException", fe.getCause().getMessage());
        }
        LOGGER.debug("### TestThrowFdlException #7 End ");

        LOGGER.debug("### TestThrowFdlException #8 Start ");
        try {
            fdlExceptionTestService.testFdlException(8);
        } catch (FdlException fe) {
            assertEquals("error.fdl.msg4", fe.getMessageKey());
            assertEquals("메세지4 파라미터 추가", fe.getMessage());
            assertEquals("TEST FdlException", fe.getCause().getMessage());
        }
        LOGGER.debug("### TestThrowFdlException #8 End ");

        LOGGER.debug("### TestThrowFdlException #9 Start ");
        try {
            fdlExceptionTestService.testFdlException(9);
        } catch (FdlException fe) {
            assertEquals("error.fdl.msg5", fe.getMessageKey());
            assertEquals("message5 parameter", fe.getMessage());
            assertEquals("TEST FdlException", fe.getCause().getMessage());
        }
        LOGGER.debug("### TestThrowFdlException #9 End ");

        LOGGER.debug("### TestThrowFdlException #10 Start ");
        try {
            fdlExceptionTestService.testFdlException(10);
        } catch (FdlException fe) {
            assertEquals("error.fdl.msg66", fe.getMessageKey());
            assertEquals("default message", fe.getMessage());
            assertEquals("TEST FdlException", fe.getCause().getMessage());
        }
        LOGGER.debug("### TestThrowFdlException #10 End ");

        LOGGER.debug("### TestThrowFdlException #11 Start ");
        try {
            fdlExceptionTestService.testFdlException(11);
        } catch (FdlException fe) {
            assertEquals("error.fdl.msg6", fe.getMessageKey());
            assertEquals("message6 parameter", fe.getMessage());
            assertEquals("TEST FdlException", fe.getCause().getMessage());
        }
        LOGGER.debug("### TestThrowFdlException #11 End ");

        LOGGER.debug("### TestThrowFdlException #12 Start ");
        try {
            fdlExceptionTestService.testFdlException(12);
        } catch (FdlException fe) {
            assertEquals("error.fdl.msg7", fe.getMessageKey());
            assertEquals("메세지7 파라미터", fe.getMessage());
            assertEquals("TEST FdlException", fe.getCause().getMessage());
        }
        LOGGER.debug("### TestThrowFdlException #12 End ");

        LOGGER.debug("### TestThrowFdlException End ");
    }

}
