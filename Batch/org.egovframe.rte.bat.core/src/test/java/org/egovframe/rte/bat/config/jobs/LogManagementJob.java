package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.*;
import org.egovframe.rte.bat.sample.example.support.EgovErrorLogTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.WriteFailedException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.HsqlMaxValueIncrementer;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

/**
 * 에러 로그 처리 및 스킵 로직을 포함한 복합적인 배치 처리 Job
 */
@Configuration
@Import(BatchTestConfig.class)
public class LogManagementJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"stepName", TradeWriter.TOTAL_AMOUNT_KEY});
        return listener;
    }

    @Bean
    public DataFieldMaxValueIncrementer incrementer() {
        HsqlMaxValueIncrementer incrementer = new HsqlMaxValueIncrementer();
        incrementer.setDataSource(dataSource);
        incrementer.setIncrementerName("TRADE_SEQ");
        incrementer.setColumnName("ID");
        return incrementer;
    }

    @Bean
    public JdbcTradeDao jdbcTradeDao() {
        JdbcTradeDao jdbcTradeDao = new JdbcTradeDao();
        jdbcTradeDao.setDataSource(dataSource);
        jdbcTradeDao.setIncrementer(incrementer());
        return jdbcTradeDao;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Trade> fileItemReader() {
        FlatFileItemReader<Trade> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("META-INF/spring/input1.txt"));

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("ISIN", "Quantity", "Price", "Customer");

        DefaultLineMapper<Trade> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new TradeFieldSetMapper());

        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    @StepScope
    public TradeProcessor tradeProcessor() {
        return new TradeProcessor();
    }

    @Bean
    @StepScope
    public TradeWriter tradeWriter() {
        TradeWriter writer = new TradeWriter();
        writer.setDao(jdbcTradeDao());
        writer.setFailingCustomers(List.of("customer6"));
        return writer;
    }

    @Bean
    public EgovErrorLogTasklet errorLogTasklet() {
        EgovErrorLogTasklet tasklet = new EgovErrorLogTasklet();
        tasklet.setDataSource(dataSource);
        return tasklet;
    }

    @Bean
    public Step errorStep() {
        return new StepBuilder("errorPrint", jobRepository)
                .tasklet(errorLogTasklet(), transactionManager)
                .listener(errorLogTasklet())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .<Trade, Trade>chunk(3, transactionManager)
                .reader(fileItemReader())
                .processor(tradeProcessor())
                .writer(tradeWriter())
                .faultTolerant()
                .skipLimit(10)
                .skip(FlatFileParseException.class)
                .skip(WriteFailedException.class)
                .listener(promotionListener())
                .build();
    }

    @Bean
    public Job logManagementJobBean() {
        return new JobBuilder("logManagementJob", jobRepository)
                .start(step1())
                .next(errorStep())
                .build();
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
        testUtils.setJob(logManagementJobBean());
        testUtils.setJobRepository(jobRepository);
        return testUtils;
    }

}
