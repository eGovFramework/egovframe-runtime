package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCreditIncreaseProcessor;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCreditRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * JDBC 커서 기반으로 DB 데이터를 읽고 처리한 후 DB에 다시 저장하는 배치 작업
 */
@Configuration
@Import(BatchTestConfig.class)
public class JdbcCursorIoJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private CustomerCreditRowMapper customerCreditRowMapper;

    @Autowired
    private JobLauncher jobLauncher;

    @Bean
    public JdbcCursorItemReader<CustomerCredit> itemReader() {
        JdbcCursorItemReader<CustomerCredit> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("SELECT ID, NAME, CREDIT FROM CUSTOMER");
        reader.setVerifyCursorPosition(true);
        reader.setRowMapper(customerCreditRowMapper);
        return reader;
    }

    @Bean
    public ItemProcessor<CustomerCredit, CustomerCredit> itemProcessor() {
        return new CustomerCreditIncreaseProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<CustomerCredit> itemWriter() {
        JdbcBatchItemWriter<CustomerCredit> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("UPDATE CUSTOMER SET credit = :credit WHERE id = :id");
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setAssertUpdates(true);
        return writer;
    }

    @Bean
    public Step jdbcCursorStep() {
        return new StepBuilder("jdbcCursorIoStep1", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job jdbcCursorIoJobBean() {
        return new JobBuilder("jdbcCursorIoJob", jobRepository)
                .start(jdbcCursorStep())
                .build();
    }

    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() throws Exception {
        JobLauncherTestUtils testUtils = new JobLauncherTestUtils();
        testUtils.setJob(jdbcCursorIoJobBean());
        testUtils.setJobRepository(jobRepository);
        testUtils.setJobLauncher(jobLauncher);
        return testUtils;
    }

}
