package org.egovframe.rte.fdl.cmmn.config;

import org.egovframe.rte.bsl.exception.web.ExceptionCase1;
import org.egovframe.rte.bsl.exception.web.ExceptionCase2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import java.util.Properties;

@Configuration
public class ExceptionResolverConfig {

    @Bean(name = "smeResovler")
    public SimpleMappingExceptionResolver simpleMappingExceptionResolver() {
        SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();

        resolver.setDefaultErrorView("common/error");

        Properties mappings = new Properties();
        mappings.setProperty(ExceptionCase1.class.getName(), "common/exceptionViewCase1");
        mappings.setProperty(ExceptionCase2.class.getName(), "common/exceptionViewCase2");

        resolver.setExceptionMappings(mappings);

        return resolver;
    }

}
