package org.egovframe.rte.bat.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextProvider implements ApplicationContextAware {
	private static ApplicationContext applicationContext;
	private Logger logger = LoggerFactory
			.getLogger(ApplicationContextProvider.class);

	public ApplicationContextProvider() {
		logger.info("init ApplicationContextProvider");
	}

	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		logger.info("set ApplicationContextProvider");
		if (context == null) logger.info("ApplicationContextProvider fail set context ~!");
		applicationContext = context;
	}

	public static Object getBean(String beanName) {
		return applicationContext.getBean(beanName);
	}

	public static <T> T getBean(String beanName, Class<T> requiredType) {
		return applicationContext.getBean(beanName, requiredType);
	}
}


