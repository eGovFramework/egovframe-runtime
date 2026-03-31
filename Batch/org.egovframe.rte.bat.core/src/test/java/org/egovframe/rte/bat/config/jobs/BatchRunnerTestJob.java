package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.GeneratingTradeItemReader;
import org.egovframe.rte.bat.sample.domain.trade.Trade;
import org.egovframe.rte.bat.sample.example.support.EgovDummyItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 테스트 및 무한 루프 환경에서 Shutdown 테스트를 위한 Job 정의
 */
@Configuration
@Import(BatchTestConfig.class)
public class BatchRunnerTestJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * 병렬/비동기 실행을 위한 실행기 구성
     */
    @Bean
    public JobLauncher jobLauncher() {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return jobLauncher;
    }

    /**
     * 100만 건의 Trade 데이터를 생성할 Reader 정의
     */
    @Bean
    public ItemReader<Trade> reader() {
        GeneratingTradeItemReader reader = new GeneratingTradeItemReader();
        reader.setLimit(1000000);
        return reader;
    }

    /**
     * 500ms 지연 처리를 포함한 Dummy Writer 설정
     */
    @Bean
    public EgovDummyItemWriter writer() {
        return new EgovDummyItemWriter() {
            @Override
            public void write(Chunk<?> chunk) throws Exception {
                Thread.sleep(200);
            }
        };
    }

    /**
     * Reader/Writer를 바탕으로 chunk(1000) 단위의 Step 구성
     */
    @Bean
    public Step infiniteStep() {
        return new StepBuilder("infiniteStep", jobRepository)
                .<Trade, Trade>chunk(1000, transactionManager)
                .reader(reader())
                .writer(writer())
                .build();
    }

    /**
     * infiniteStep()을 시작으로 하는 Job 빌드
     */
    @Bean
    public Job batchJob() {
        return new JobBuilder("batchRunnerTestJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(infiniteStep())
                .build();
    }

}
