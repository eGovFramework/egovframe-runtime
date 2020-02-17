package egovframework.rte.fdl.cmmn.aspectj;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
@Aspect
public class SampleAspect {
	private static final Logger LOGGER = LoggerFactory.getLogger(SampleAspect.class);
	
	//@Pointcut("execution(public * *(..))")
    @Before("execution(public * *(..))")
    //@Pointcut("execution(public * *(..))")
    public void beforeTargetMethod(JoinPoint thisJoinPoint) {
    	
  	  Class clazz = thisJoinPoint.getTarget().getClass();
      String className = thisJoinPoint.getTarget().getClass().getSimpleName();
      String methodName = thisJoinPoint.getSignature().getName();
      
    	LOGGER.debug("@@Before !!! execution("+className+" "+methodName+"())");
    }
    //@Pointcut("execution(* getOrderDescription(..))")
//    @Pointcut("execution(public * *(..))")
//    public void targetMethod() {
//    	LOGGER.debug("@Pointcut !!! execution(* getOrderDescription())");
//    	
//    }
    
    @Before("execution(public * *(..))")
    public void Pointcut(JoinPoint thisJoinPoint) {
    	
    	  Class clazz = thisJoinPoint.getTarget().getClass();
          String className = thisJoinPoint.getTarget().getClass().getSimpleName();
          String methodName = thisJoinPoint.getSignature().getName();;
      
    	LOGGER.debug("@Pointcut !!! execution("+className+" "+methodName+"())");
        // pointcut annotation 값을 참조하기 위한 dummy method
    }
    
    @Around("execution(public * *(..))")
    public Object aroundTargetMethod(ProceedingJoinPoint thisJoinPoint) throws Throwable {
    	
    	  Class clazz = thisJoinPoint.getTarget().getClass();
          String className = thisJoinPoint.getTarget().getClass().getSimpleName();
          String methodName = thisJoinPoint.getSignature().getName();
          
    	LOGGER.debug("@Around !!! execution("+className+" "+methodName+"())");
    	
    	return thisJoinPoint.proceed();
    }
}