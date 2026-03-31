package org.egovframe.rte.bat.sample.example.support;

import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

/**
 * 로그틀을 제공하는 클래스
 *
 * @author 배치실행개발팀
 * @see <pre>
 * == 개정이력(Modification Information) ==
 * 수정일               수정자               수정내용
 * ------      --------     ---------------------------
 * 2012. 07.30  배치실행개발팀    최초 생성
 * </pre>
 * @since 2012. 07.30
 */
public class EgovLogAdvice implements AfterReturningAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovLogAdvice.class);

    /**
     * 기본 로그틀 제공
     */
    public void doBasicLogging(JoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        StringBuffer output = new StringBuffer();

        output.append(pjp.getTarget().getClass().getName()).append(": ");
        output.append(pjp.toShortString()).append(": ");

        for (Object arg : args) {
            output.append(arg).append(" ");
        }

        LOGGER.debug("Basic: " + output);
    }

    /**
     * processed: item 로그틀 제공
     */
    public void doStronglyTypedLogging(Object item) {
        LOGGER.debug("Processed: " + item);
    }

    /**
     * AfterReturningAdvice 인터페이스 구현
     */
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        LOGGER.debug("After returning - Method: " + method.getName() + ", Return value: " + returnValue);
        if (args != null && args.length > 0) {
            LOGGER.debug("Processed items: " + args[0]);
        }
    }

}
