package org.egovframe.rte.fdl.cmmn.config;

import org.egovframe.rte.bsl.exception.HelloServiceImpl;
import org.egovframe.rte.fdl.cmmn.aspect.ExceptionTransfer;
import org.egovframe.rte.fdl.cmmn.exception.manager.DefaultExceptionHandleManager;
import org.egovframe.rte.fdl.cmmn.exception.manager.ExceptionHandlerService;
import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.egovframe.rte.fdl.cmmn.trace.handler.DefaultTraceHandler;
import org.egovframe.rte.fdl.cmmn.trace.manager.DefaultTraceHandleManager;
import org.egovframe.rte.fdl.exception.handler.EgovServiceExceptionHandler;
import org.egovframe.rte.fdl.exception.handler.OthersServiceExceptionHandler;
import org.egovframe.rte.mail.SimpleSSLMail;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.AntPathMatcher;

@Configuration
@EnableAspectJAutoProxy
public class CmmnTestConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/message-common");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(60);
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean
    public AntPathMatcher antPathMater() {
        return new AntPathMatcher();
    }

    @Bean
    public DefaultTraceHandler defaultTraceHandler() {
        return new DefaultTraceHandler();
    }

    @Bean
    public DefaultTraceHandleManager traceHandlerService() {
        DefaultTraceHandleManager manager = new DefaultTraceHandleManager();
        manager.setPatterns(new String[]{"*"});
        manager.setHandlers(new DefaultTraceHandler[]{defaultTraceHandler()});
        manager.setReqExpMatcher(antPathMater());
        return manager;
    }

    @Bean
    public LeaveaTrace leaveaTrace() {
        LeaveaTrace trace = new LeaveaTrace();
        trace.setTraceHandlerServices(new DefaultTraceHandleManager[]{traceHandlerService()});
        return trace;
    }

    @Bean
    public EgovServiceExceptionHandler egovHandler() {
        return new EgovServiceExceptionHandler();
    }

    @Bean
    public OthersServiceExceptionHandler otherHandler() {
        return new OthersServiceExceptionHandler();
    }

    @Bean
    public DefaultExceptionHandleManager defaultExceptionHandleManager() {
        DefaultExceptionHandleManager manager = new DefaultExceptionHandleManager();
        manager.setPatterns(new String[]{"**exception.*"});
        manager.setHandlers(new EgovServiceExceptionHandler[]{egovHandler()});
        return manager;
    }

    @Bean
    public DefaultExceptionHandleManager otherExceptionHandleManager() {
        DefaultExceptionHandleManager manager = new DefaultExceptionHandleManager();
        manager.setPatterns(new String[]{"**exception.*"});
        manager.setHandlers(new OthersServiceExceptionHandler[]{otherHandler()});
        return manager;
    }

    @Bean
    public ExceptionTransfer exceptionTransfer() {
        ExceptionTransfer transfer = new ExceptionTransfer();
        transfer.setExceptionHandlerService(new ExceptionHandlerService[]{
                defaultExceptionHandleManager(), otherExceptionHandleManager()
        });
        return transfer;
    }

    @Bean
    public SimpleSSLMail otherSSLMailSender() {
        SimpleSSLMail mail = new SimpleSSLMail();
        mail.setHost("smtp.gmail.com");
        mail.setPort(465);
        mail.setUsername("egovframe@gmail.com");
        mail.setPassword("egovframe1q2w3e");
        mail.setReceivers(new String[]{"user@gmail.com"});
        return mail;
    }

    @Bean(name = "helloService")
    public HelloServiceImpl helloService() {
        return new HelloServiceImpl();
    }

    @Bean(name = "otherService")
    public HelloServiceImpl otherService() {
        return new HelloServiceImpl();
    }

}
