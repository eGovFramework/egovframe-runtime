package org.egovframe.rte.bat.config.launch;

import org.egovframe.rte.bat.core.launch.support.SayHelloService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class LaunchScheduleJCConfig {

    @Bean
    public SayHelloService sayHelloService() {
        SayHelloService service = new SayHelloService();
        service.setName("Spring Batch");
        return service;
    }

    @Bean
    public MethodInvokingJobDetailFactoryBean jobDetail(SayHelloService sayHelloService) {
        MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
        jobDetail.setTargetObject(sayHelloService);
        jobDetail.setTargetMethod("sayHello");
        return jobDetail;
    }

    @Bean
    public CronTriggerFactoryBean cronTrigger(MethodInvokingJobDetailFactoryBean jobDetail) {
        CronTriggerFactoryBean cronTrigger = new CronTriggerFactoryBean();
        cronTrigger.setJobDetail(jobDetail.getObject());
        cronTrigger.setStartDelay(5000);
        cronTrigger.setCronExpression("0/10 * * * * ?");
        return cronTrigger;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(CronTriggerFactoryBean cronTrigger) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setTriggers(cronTrigger.getObject());
        return schedulerFactory;
    }

}
