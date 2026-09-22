package org.egovframe.rte.fdl.cmmn;

import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class EgovBizExceptionTest {

    @Test
    public void testGetCauseWithDefaultMessage() {
        Exception wrapped = new IllegalStateException("TEST EgovBizException");
        EgovBizException be = new EgovBizException("message", wrapped);

        assertEquals("message", be.getMessage());
        assertEquals("TEST EgovBizException", be.getCause().getMessage());
        assertSame(wrapped, be.getWrappedException());
    }

    @Test
    public void testGetCauseWithMessageSource() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("error.biz.msg1", Locale.getDefault(), "message1");
        Exception wrapped = new IllegalStateException("TEST EgovBizException");
        EgovBizException be = new EgovBizException(messageSource, "error.biz.msg1", wrapped);

        assertEquals("message1", be.getMessage());
        assertEquals("TEST EgovBizException", be.getCause().getMessage());
        assertSame(wrapped, be.getWrappedException());
    }

    @Test
    public void testDefaultMessageWithoutArgument() {
        EgovBizException be = new EgovBizException();

        assertEquals("EgovBizException without message", be.getMessage());
    }

}
