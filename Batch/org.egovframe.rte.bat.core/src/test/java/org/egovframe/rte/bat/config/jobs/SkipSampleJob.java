package org.egovframe.rte.bat.config.jobs;

import io.micrometer.core.instrument.config.validate.ValidationException;
import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.Trade;
import org.egovframe.rte.bat.sample.domain.trade.TradeProcessor;
import org.egovframe.rte.bat.sample.domain.trade.TradeRowMapper;
import org.egovframe.rte.bat.sample.example.listener.EgovSkipCheckingListener;
import org.egovframe.rte.bat.sample.example.support.EgovErrorLogTasklet;
import org.egovframe.rte.bat.sample.example.support.EgovItemTrackingTradeItemWriter;
import org.egovframe.rte.bat.sample.example.support.EgovSkipCheckingDecider;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.WriteFailedException;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * skip 및 decider 기능을 포괄적으로 사용하는 복합적인 Job 구성
 */
@Configuration
@Import(BatchTestConfig.class)
public class SkipSampleJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .<Trade, Trade>chunk(3, transactionManager)
                .reader(tradeSqlItemReader())
                .processor(tradeProcessor())
                .writer(tradeItemWriter())
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skip(WriteFailedException.class)
                .skipLimit(10)
                .listener(new EgovSkipCheckingListener())
                .listener(promotionListener())
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("step2", jobRepository)
                .<Trade, Trade>chunk(2, transactionManager)
                .reader(tradeSqlItemReader())
                .processor(tradeProcessorFailure())
                .writer(tradeItemWriter())
                .faultTolerant()
                .skip(ValidationException.class)
                .skip(IOException.class)
                .noRollback(ValidationException.class)
                .skipLimit(10)
                .listener(new EgovSkipCheckingListener())
                .listener(promotionListener())
                .build();
    }

    @Bean
    public Step errorPrint1() {
        return new StepBuilder("errorPrint1", jobRepository)
                .tasklet(errorLogTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step errorPrint2() {
        return new StepBuilder("errorPrint2", jobRepository)
                .tasklet(errorLogTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Job skipJob() {
        return new JobBuilder("skipJob", jobRepository)
                .start(step1())
                .on("COMPLETED WITH SKIPS").to(errorPrint1())
                .from(step1())
                .on("*").to(step2())
                .from(step2())
                .next(skipCheckingDecider())
                .on("COMPLETED WITH SKIPS").to(errorPrint2())
                .from(skipCheckingDecider()).on("*").end()
                .build()
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<Trade> tradeSqlItemReader() {
        JdbcCursorItemReader<Trade> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("SELECT isin, quantity, price, customer, id, version from TRADE order by id");
        reader.setVerifyCursorPosition(true);
        reader.setRowMapper(tradeRowMapper());
        return reader;
    }

    @Bean
    @StepScope
    public TradeProcessor tradeProcessor() {
        return new TradeProcessor();
    }

    @Bean
    @StepScope
    public TradeProcessor tradeProcessorFailure() {
        TradeProcessor tradeProcessor = new TradeProcessor();
        tradeProcessor.setValidationFailure(7);
        return tradeProcessor;
    }

    @Bean
    @StepScope
    public EgovItemTrackingTradeItemWriter tradeItemWriter() {
        EgovItemTrackingTradeItemWriter writer = new EgovItemTrackingTradeItemWriter();
        writer.setDataSource(dataSource);
        writer.setWriteFailureISIN("UK21341EAH47");
        return writer;
    }

    @Bean
    public RowMapper tradeRowMapper() {
        return new TradeRowMapper();
    }

    @Bean
    @StepScope
    public Tasklet errorLogTasklet() {
        EgovErrorLogTasklet tasklet = new EgovErrorLogTasklet();
        tasklet.setDataSource(dataSource);
        return tasklet;
    }

    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"stepName", "TOTAL_AMOUNT_KEY"});
        return listener;
    }

    @Bean
    public JobExecutionDecider skipCheckingDecider() {
        return new EgovSkipCheckingDecider();
    }

}
