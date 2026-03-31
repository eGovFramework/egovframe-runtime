package org.egovframe.rte.ptl.mvc.handler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan(basePackages = "org.egovframe.rte.ptl.mvc.handler")
public class HandlerTestServletConfig {

    @Bean
    public RequestMappingHandlerMapping annotationMapper() {
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
        List<Object> interceptors = new ArrayList<>();
        interceptors.add(oneInterceptor());
        interceptors.add(twoInterceptor());
        handlerMapping.setInterceptors(interceptors.toArray());
        return handlerMapping;
    }

    @Bean
    public OneInterceptor oneInterceptor() {
        return new OneInterceptor();
    }

    @Bean
    public TwoInterceptor twoInterceptor() {
        return new TwoInterceptor();
    }

}