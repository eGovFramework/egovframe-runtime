package org.egovframe.rte.bat.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ObjectUtils;

public class ApplicationContextProvider implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContextProvider.class);

    private static ApplicationContext applicationContext;

    public ApplicationContextProvider() {
    }

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public static <T> T getBean(String beanName, Class<T> requiredType) {
        return applicationContext.getBean(beanName, requiredType);
    }

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (ObjectUtils.isEmpty(context)) {
            LOGGER.debug("### ApplicationContextProvider fail set ApplicationContext ");
        }
        applicationContext = context;
    }

}
