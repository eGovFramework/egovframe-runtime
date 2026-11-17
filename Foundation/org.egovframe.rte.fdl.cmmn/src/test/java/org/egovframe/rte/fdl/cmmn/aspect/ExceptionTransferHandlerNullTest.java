package org.egovframe.rte.fdl.cmmn.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.exception.manager.ExceptionHandlerService;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExceptionTransferHandlerNullTest {

    // exceptionHandlerService 미주입 구성에서도 발생한 업무 예외 그대로 전달
    @Test
    public void testTransferBizExceptionWithoutHandlerService() {
        ExceptionTransfer transfer = new ExceptionTransfer();
        EgovBizException be = new EgovBizException("해당 데이터가 없습니다.");

        Exception thrown = assertThrows(EgovBizException.class, () -> transfer.transfer(joinPoint(), be));

        assertSame(be, thrown);
    }

    // 빈 배열 주입 구성과 결과 동일
    @Test
    public void testTransferBizExceptionWithEmptyHandlerService() {
        ExceptionTransfer transfer = new ExceptionTransfer();
        transfer.setExceptionHandlerService(new ExceptionHandlerService[]{});
        EgovBizException be = new EgovBizException("해당 데이터가 없습니다.");

        Exception thrown = assertThrows(EgovBizException.class, () -> transfer.transfer(joinPoint(), be));

        assertSame(be, thrown);
    }

    // RuntimeException 도 같은 후처리 경로 — 원본 그대로 전달
    @Test
    public void testTransferRuntimeExceptionWithoutHandlerService() {
        ExceptionTransfer transfer = new ExceptionTransfer();
        RuntimeException re = new IllegalStateException("runtime exception");

        Exception thrown = assertThrows(IllegalStateException.class, () -> transfer.transfer(joinPoint(), re));

        assertSame(re, thrown);
    }

    private JoinPoint joinPoint() {
        Signature signature = createNiceMock(Signature.class);
        expect(signature.getName()).andStubReturn("updateMethod");
        JoinPoint joinPoint = createNiceMock(JoinPoint.class);
        expect(joinPoint.getTarget()).andStubReturn(new Object());
        expect(joinPoint.getSignature()).andStubReturn(signature);
        replay(signature, joinPoint);
        return joinPoint;
    }

}
