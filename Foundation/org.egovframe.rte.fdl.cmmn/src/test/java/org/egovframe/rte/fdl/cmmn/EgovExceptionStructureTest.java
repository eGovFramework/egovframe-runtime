package org.egovframe.rte.fdl.cmmn;

import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizRuntimeException;
import org.egovframe.rte.fdl.cmmn.exception.EgovErrorMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 구조화 오류 메시지와 선언적 예외 헬퍼를 검증한다 — reason/solution 해석, cause 체인 연결, 미정의 키의 폴백.
 */
public class EgovExceptionStructureTest {

    /** 테스트용 업무 서비스 */
    static class OrderService extends EgovAbstractServiceImpl {
        void orderChecked(Throwable cause) throws EgovBizException {
            throwBizException("fail.biz.stock", new Object[] {99}, cause);
        }

        void orderUnchecked(Throwable cause) {
            throw newBizRuntimeException("fail.biz.stock", new Object[] {99}, cause);
        }

        void orderWithoutArgs() throws EgovBizException {
            throwBizException("fail.biz.stock");
        }
    }

    private StaticMessageSource messageSource;
    private OrderService service;

    @BeforeEach
    public void setUp() {
        messageSource = new StaticMessageSource();
        messageSource.addMessage("fail.biz.stock", Locale.KOREAN, "재고가 부족합니다.");
        messageSource.addMessage("fail.biz.stock.reason", Locale.KOREAN, "요청 수량 {0}이 보유 재고를 초과했습니다.");
        messageSource.addMessage("fail.biz.stock.solution", Locale.KOREAN, "수량을 줄여 다시 시도하십시오.");
        service = new OrderService();
        ReflectionTestUtils.setField(service, "messageSource", messageSource);
        LocaleContextHolder.setLocale(Locale.KOREAN);
    }

    @AfterEach
    public void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    public void testThrowBizExceptionCarriesStructuredMessageAndCause() {
        IllegalStateException cause = new IllegalStateException("stock=1");

        EgovBizException ex = assertThrows(EgovBizException.class, () -> service.orderChecked(cause));

        assertEquals("재고가 부족합니다.", ex.getMessage());
        assertEquals("fail.biz.stock", ex.getMessageKey());
        EgovErrorMessage errorMessage = ex.getErrorMessage();
        assertEquals("fail.biz.stock", errorMessage.getCode());
        assertEquals("요청 수량 99이 보유 재고를 초과했습니다.", errorMessage.getReason());
        assertEquals("수량을 줄여 다시 시도하십시오.", errorMessage.getSolution());
        assertSame(cause, ex.getCause(), "원인 예외가 cause 체인에 연결되어야 한다");
        assertSame(cause, ex.getWrappedException());
    }

    @Test
    public void testThrowBizExceptionWithoutArgsOrCause() {
        EgovBizException ex = assertThrows(EgovBizException.class, () -> service.orderWithoutArgs());

        assertEquals("재고가 부족합니다.", ex.getMessage());
        assertNull(ex.getCause());
        assertNull(ex.getWrappedException());
        assertEquals("수량을 줄여 다시 시도하십시오.", ex.getErrorMessage().getSolution());
    }

    @Test
    public void testNewBizRuntimeExceptionIsUncheckedWithStructure() {
        IllegalStateException cause = new IllegalStateException("stock=1");

        EgovBizRuntimeException ex = assertThrows(EgovBizRuntimeException.class, () -> service.orderUnchecked(cause));

        assertEquals("재고가 부족합니다.", ex.getMessage());
        assertEquals("fail.biz.stock", ex.getMessageKey());
        assertEquals("요청 수량 99이 보유 재고를 초과했습니다.", ex.getErrorMessage().getReason());
        assertSame(cause, ex.getCause());
        assertSame(cause, ex.getWrappedException());
    }

    @Test
    public void testStructuredConstructorAndPlainMessageConstructor() {
        EgovErrorMessage errorMessage = new EgovErrorMessage("fail.biz.custom", "사용자 메시지", "원인", "조치");
        IllegalArgumentException cause = new IllegalArgumentException("bad");

        EgovBizException checked = new EgovBizException(errorMessage, cause);
        assertEquals("사용자 메시지", checked.getMessage());
        assertEquals("fail.biz.custom", checked.getMessageKey());
        assertSame(errorMessage, checked.getErrorMessage());
        assertSame(cause, checked.getCause());

        EgovBizRuntimeException plain = new EgovBizRuntimeException("단순 메시지", cause);
        assertEquals("단순 메시지", plain.getMessage());
        assertNull(plain.getErrorMessage());
        assertSame(cause, plain.getCause());

        assertNull(new EgovBizException("기존 생성자").getErrorMessage(), "기존 생성자로 만들면 구조화 메시지는 없다");
    }

    @Test
    public void testErrorMessageResolveFallbacks() {
        EgovErrorMessage resolved = EgovErrorMessage.resolve(messageSource, "no.such.code", null, Locale.KOREAN);

        assertEquals("no.such.code", resolved.getUserMessage(), "사용자 메시지가 없으면 코드로 폴백한다");
        assertNull(resolved.getReason());
        assertNull(resolved.getSolution());
        assertTrue(resolved.toString().contains("no.such.code"));

        EgovErrorMessage full = EgovErrorMessage.resolve(messageSource, "fail.biz.stock", new Object[] {3}, Locale.KOREAN);
        assertTrue(full.toString().contains("reason:"), full.toString());
        assertTrue(full.toString().contains("solution:"), full.toString());
        assertTrue(full.toString().contains("요청 수량 3이"), full.toString());
    }
}
