package egovframework.rte.fdl.logging.sample.aop;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;

public class MethodParameterLoggingAspect {
	public void beforeLog(JoinPoint thisJoinPoint) {
		Class<?> clazz = thisJoinPoint.getTarget().getClass();
		String className = thisJoinPoint.getTarget().getClass().getName();
		String methodName = thisJoinPoint.getSignature().getName();

		StringBuffer buf = new StringBuffer();
		buf.append("\n== MethodParameterLoggingAspect : [" + className + "."
				+ methodName + "()] ==");
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
