package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.*;
import org.egovframe.rte.bat.sample.example.support.EgovLogAdvice;
import org.egovframe.rte.bat.sample.example.support.EgovStagingItemProcessor;
import org.egovframe.rte.bat.sample.example.support.EgovStagingItemReader;
import org.egovframe.rte.bat.sample.example.support.EgovStagingItemWriter;
import org.springframework.aop.Advisor;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.adapter.AfterReturningAdviceInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.HsqlMaxValueIncrementer;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 병렬(TaskExecutor 기반) 처리와 유효성 검증을 포함한 2단계 배치 작업
 */
@Configuration
@Import(BatchTestConfig.class)
public class ParallelJob {

    @Autowired
    private JobRepository jobRepository;


    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Step stagingStep() {
        return new StepBuilder("staging", jobRepository)
                .<Trade, Trade>chunk(2, transactionManager)
                .reader(fileItemReader())
                .writer(stagingItemWriter())
                .build();
    }

    @Bean
    public Step loadingStep() {
        return new StepBuilder("loading", jobRepository)
                .<Trade, Trade>chunk(3, transactionManager)
                .reader(stagingReader())
                .processor(stagingProcessor())
                .writer(tradeWriter())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job parallelJobBean() {
        return new JobBuilder("parallelJob", jobRepository)
                .start(stagingStep())
                .next(loadingStep())
                .build();
    }

    @Bean
    public ItemReader<Trade> fileItemReader() {
        FlatFileItemReader<Trade> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("META-INF/spring/teststream.ImportTradeDataStep.txt"));

        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setNames("ISIN", "Quantity", "Price", "Customer");
        tokenizer.setColumns(new Range(1, 12), new Range(13, 15), new Range(16, 20), new Range(21, 29));

        DefaultLineMapper<Trade> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new TradeFieldSetMapper());

        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    public ItemWriter<Trade> stagingItemWriter() {
        EgovStagingItemWriter<Trade> writer = new EgovStagingItemWriter<Trade>() {
            @Override
            public void write(Chunk<? extends Trade> chunk) throws Exception {
                write(chunk.getItems());
            }
        };
        writer.setDataSource(dataSource);
        writer.setIncrementer(incrementer1());
        return writer;
    }

    @Bean
    public ItemReader<Trade> stagingReader() {
        EgovStagingItemReader reader = new EgovStagingItemReader();
        reader.setDataSource(dataSource);
        return reader;
    }

    @Bean
    public ItemProcessor<Trade, Trade> stagingProcessor() {
        EgovStagingItemProcessor processor = new EgovStagingItemProcessor();
        processor.setDataSource(dataSource);
        return processor;
    }

    @Bean
    public ItemWriter<Trade> tradeWriter() {
        TradeWriter writer = new TradeWriter();
        writer.setDao(tradeDao());
        return writer;
    }

    @Bean
    public TradeDao tradeDao() {
        JdbcTradeDao dao = new JdbcTradeDao();
        dao.setDataSource(dataSource);
        dao.setIncrementer(incrementer2());
        return dao;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public DataFieldMaxValueIncrementer incrementer1() {
        HsqlMaxValueIncrementer incrementer = new HsqlMaxValueIncrementer();
        incrementer.setDataSource(dataSource);
        incrementer.setIncrementerName("BATCH_STAGING_SEQ");
        incrementer.setColumnName("ID");
        return incrementer;
    }

    @Bean
    public DataFieldMaxValueIncrementer incrementer2() {
        HsqlMaxValueIncrementer incrementer = new HsqlMaxValueIncrementer();
        incrementer.setDataSource(dataSource);
        incrementer.setIncrementerName("TRADE_SEQ");
        incrementer.setColumnName("ID");
        return incrementer;
    }

    @Bean
    public EgovLogAdvice egovLogAdvice() {
        return new EgovLogAdvice();
    }

    @Bean
    public Advisor loggingAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* org.springframework.batch.item.ItemWriter+.write(..))");
        return new DefaultPointcutAdvisor(pointcut, new AfterReturningAdviceInterceptor((AfterReturningAdvice) egovLogAdvice()));
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
        testUtils.setJob(parallelJobBean());
        testUtils.setJobRepository(jobRepository);
        return testUtils;
    }

}
