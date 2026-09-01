package org.egovframe.rte.fdl.cmmn;

import org.egovframe.rte.fdl.cmmn.exception.BaseException;
import org.egovframe.rte.fdl.cmmn.exception.BaseRuntimeException;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * 생성자로 전달한 원인 예외가 getWrappedException() 으로 조회되는지 확인한다.
 */
public class WrappedExceptionRetentionTest {

    private static final String MESSAGE_KEY = "error.wrapped.msg";

    private StaticMessageSource messageSource() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage(MESSAGE_KEY, Locale.getDefault(), "wrapped message");
        return messageSource;
    }

    @Test
    public void baseExceptionRetainsWrappedExceptionWithDefaultMessage() {
        Exception wrapped = new IllegalStateException("TEST BaseException");
        BaseException be = new BaseException("message", wrapped);

        assertEquals("message", be.getMessage());
        assertSame(wrapped, be.getCause());
        assertSame(wrapped, be.getWrappedException());
    }

    @Test
    public void baseExceptionRetainsWrappedExceptionWithMessageSource() {
        Exception wrapped = new IllegalStateException("TEST BaseException");
        BaseException be = new BaseException(messageSource(), MESSAGE_KEY, wrapped);

        assertEquals("wrapped message", be.getMessage());
        assertSame(wrapped, be.getCause());
        assertSame(wrapped, be.getWrappedException());
    }

    @Test
    public void baseRuntimeExceptionRetainsWrappedExceptionWithDefaultMessage() {
        Exception wrapped = new IllegalStateException("TEST BaseRuntimeException");
        BaseRuntimeException bre = new BaseRuntimeException("message", wrapped);

        assertEquals("message", bre.getMessage());
        assertSame(wrapped, bre.getCause());
        assertSame(wrapped, bre.getWrappedException());
    }

    @Test
    public void baseRuntimeExceptionRetainsWrappedExceptionWithMessageSource() {
        Exception wrapped = new IllegalStateException("TEST BaseRuntimeException");
        BaseRuntimeException bre = new BaseRuntimeException(messageSource(), MESSAGE_KEY, wrapped);

        assertEquals("wrapped message", bre.getMessage());
        assertSame(wrapped, bre.getCause());
        assertSame(wrapped, bre.getWrappedException());
    }

    @Test
    public void fdlExceptionRetainsWrappedExceptionWithDefaultMessage() {
        Exception wrapped = new IllegalStateException("TEST FdlException");
        FdlException fe = new FdlException("message", wrapped);

        assertEquals("message", fe.getMessage());
        assertSame(wrapped, fe.getCause());
        assertSame(wrapped, fe.getWrappedException());
    }

    @Test
    public void fdlExceptionRetainsWrappedExceptionWithMessageSource() {
        Exception wrapped = new IllegalStateException("TEST FdlException");
        FdlException fe = new FdlException(messageSource(), MESSAGE_KEY, wrapped);

        assertEquals("wrapped message", fe.getMessage());
        assertSame(wrapped, fe.getCause());
        assertSame(wrapped, fe.getWrappedException());
    }

    @Test
    public void wrappedExceptionIsNullWhenNotSupplied() {
        BaseException be = new BaseException("message");
        BaseRuntimeException bre = new BaseRuntimeException("message");
        FdlException fe = new FdlException("message");

        assertNull(be.getWrappedException());
        assertNull(bre.getWrappedException());
        assertNull(fe.getWrappedException());
    }
}
