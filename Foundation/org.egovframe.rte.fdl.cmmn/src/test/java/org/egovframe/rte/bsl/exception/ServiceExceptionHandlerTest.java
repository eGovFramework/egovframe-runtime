package org.egovframe.rte.bsl.exception;

import jakarta.annotation.Resource;

import java.util.Locale;
import org.egovframe.rte.fdl.cmmn.aspect.ExceptionTransfer;
import org.egovframe.rte.fdl.cmmn.config.CmmnTestConfig;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CmmnTestConfig.class)
public class ServiceExceptionHandlerTest {

    @Resource(name = "helloService")
    private HelloService helloService;

    @Resource(name = "otherService")
    private HelloService otherService;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * processException() 은 LocaleContextHolder.getLocale() 로 메시지를 해석하며,
     * 미지정 시 JVM 기본 로케일(Locale)로 폴백한다. 아래 단언은 한국어 메시지를 기대하므로
     * 실행 환경의 기본 로케일에 좌우되지 않도록 스레드 범위로 고정한다.
     */
    @BeforeEach
    public void pinLocale() {
        LocaleContextHolder.setLocale(Locale.KOREA);
    }

    @AfterEach
    public void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    public void testBizUnCheckedException() throws Exception {
        String name = "world";
        String resultStr = helloService.sayHello(name);
        assertEquals("Hello world", resultStr);
        LeaveaTrace tmpTrace = (LeaveaTrace) applicationContext.getBean("leaveaTrace");
        assertEquals(1, tmpTrace.countOfTheTraceHandlerService());
    }

    @Test
    public void testBizException() {
        // wrapped Exception 존재하는 경우
        try {
            helloService.updateMethod();
        } catch (Exception be) {
            System.out.println("### testBizException >>> " + be.getMessage());
            assertInstanceOf(EgovBizException.class, be);
            assertInstanceOf(ArithmeticException.class, ((EgovBizException) be).getWrappedException());
            assertEquals("해당 데이터가 없습니다.", be.getMessage());
            ExceptionTransfer etfer = (ExceptionTransfer) applicationContext.getBean("exceptionTransfer");
            assertEquals(2, etfer.countOfTheExceptionHandlerService());
        }
    }

    @Test
    public void testBiz2Exception() {
        // wrapped Exception 존재하지 않는 경우
        try {
            otherService.updateMethod();
        } catch (Exception be) {
            System.out.println("### testBiz2Exception >>> " + be.getMessage());
            assertInstanceOf(EgovBizException.class, be);
            assertEquals("해당 데이터가 없습니다.", be.getMessage());
            ExceptionTransfer etfer = (ExceptionTransfer) applicationContext.getBean("exceptionTransfer");
            assertEquals(2, etfer.countOfTheExceptionHandlerService());
        }
    }

}
