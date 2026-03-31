package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.GeneratingTradeItemReader;
import org.egovframe.rte.bat.sample.domain.trade.Trade;
import org.egovframe.rte.bat.sample.example.support.EgovRetrySampleItemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Retry 기능을 테스트하는 단순한 Job을 정의
 */
@Configuration
@Import(BatchTestConfig.class)
public class RetrySample {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrySample.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Step retryStep() {
        return new StepBuilder("step1", jobRepository)
                .<Trade, Trade>chunk(1, transactionManager)
                .reader(itemGenerator())
                .writer(itemWriter())
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    public Job retrySampleJob() {
        return new JobBuilder("retrySample", jobRepository)
                .start(retryStep())
                .build();
    }

    @Bean
    public GeneratingTradeItemReader itemGenerator() {
        GeneratingTradeItemReader reader = new GeneratingTradeItemReader();
        reader.setLimit(10);
        return reader;
    }

    @Bean
    public EgovRetrySampleItemWriter<Trade> itemWriter() {
        return new EgovRetrySampleItemWriter<Trade>() {
            @Override
            public void write(Chunk<? extends Trade> chunk) throws Exception {
                LOGGER.debug("### RetrySample EgovRetrySampleItemWriter()... ");
                // Chunk를 List로 변환하여 부모 클래스의 로직 호출
                super.write(chunk.getItems());
            }
        };
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
        testUtils.setJob(retrySampleJob());
        testUtils.setJobRepository(jobRepository);
        return testUtils;
    }

}
