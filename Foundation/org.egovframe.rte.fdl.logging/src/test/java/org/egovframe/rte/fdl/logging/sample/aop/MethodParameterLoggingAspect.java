package org.egovframe.rte.fdl.logging.sample.aop;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodParameterLoggingAspect {

    @Before("execution(* org.egovframe.rte.fdl.logging.sample..impl.*Impl.*(..))")
    public void beforeLog(JoinPoint thisJoinPoint) {
        Class<?> clazz = thisJoinPoint.getTarget().getClass();
        String className = thisJoinPoint.getTarget().getClass().getName();
        String methodName = thisJoinPoint.getSignature().getName();

        StringBuilder buf = new StringBuilder();
        buf.append("\n== MethodParameterLoggingAspect : [").append(className).append(".").append(methodName).append("()] ==");
        Object[] arguments = thisJoinPoint.getArgs();
        int argCount = 0;
        for (Object obj : arguments) {
            buf.append("\n - arg ");
            buf.append(argCount++);
            buf.append(" : ");
            buf.append(ToStringBuilder.reflectionToString(obj));
        }

        Logger logger = LogManager.getLogger(clazz);
        if (logger.isDebugEnabled())
            logger.debug(buf.toString());
    }

}
