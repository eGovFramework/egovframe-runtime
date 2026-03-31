package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * MethodInvokingTaskletAdapter를 활용하여 POJO의 메서드를 Tasklet으로 실행하는 배치 작업
 */
@Configuration
@Import(BatchTestConfig.class)
public class TaskLetJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .tasklet(adapter(null), transactionManager)
                .build();
    }

    @Bean
    public Job loopJobBean() {
        return new JobBuilder("loopJob", jobRepository)
                .start(step1())
                .build();
    }

    @Bean
    @StepScope
    public MethodInvokingTaskletAdapter adapter(TestBean value) {
        MethodInvokingTaskletAdapter adapter = new MethodInvokingTaskletAdapter();
        adapter.setTargetObject(value);
        adapter.setTargetMethod("execute");
        return adapter;
    }

    @Bean
    @StepScope
    public TestBean value(@Value("#{jobParameters['value']}") String injectedValue) {
        TestBean bean = new TestBean();
        bean.setValue(injectedValue);
        return bean;
    }

    @Bean
    public JobLauncher jobLauncher() throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() throws Exception {
        JobLauncherTestUtils testUtils = new JobLauncherTestUtils();
        testUtils.setJobLauncher(jobLauncher());
        testUtils.setJob(loopJobBean());
        testUtils.setJobRepository(jobRepository);
        return testUtils;
    }

    /**
     * 내부 클래스이지만 실제 환경에서는 별도 파일로 분리
     */
    public static class TestBean {
        private String value;

        public void setValue(String value) {
            this.value = value;
        }

        public void execute() {
            System.out.println("Executing with value = " + value);
        }
    }

}
