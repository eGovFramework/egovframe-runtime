package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCreditIncreaseProcessor;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCreditRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * JdbcPagingItemReader를 이용해 페이지 단위로 DB 데이터를 읽고 처리한 후 업데이트하는 배차 작업
 */
@Configuration
@Import(BatchTestConfig.class)
public class JdbcPagingIoJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;


    @Bean
    public CustomerCreditRowMapper customerCreditRowMapper() {
        return new CustomerCreditRowMapper();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<CustomerCredit> itemReader(
            @Value("#{jobParameters['credit']}") Integer credit) throws Exception {
        SqlPagingQueryProviderFactoryBean queryProviderFactory = new SqlPagingQueryProviderFactoryBean();
        queryProviderFactory.setDataSource(dataSource);
        queryProviderFactory.setSelectClause("SELECT NAME, ID, CREDIT");
        queryProviderFactory.setFromClause("FROM CUSTOMER");
        queryProviderFactory.setWhereClause("WHERE CREDIT > :credit");
        queryProviderFactory.setSortKey("ID");

        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("credit", credit);
        parameterValues.put("statusCode", "PE");
        parameterValues.put("type", "COLLECTION"); // 주석 처리 가능: where 절엔 사용되지 않음

        JdbcPagingItemReader<CustomerCredit> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setQueryProvider(queryProviderFactory.getObject());
        reader.setRowMapper(customerCreditRowMapper());
        reader.setPageSize(2);
        reader.setParameterValues(parameterValues);
        reader.afterPropertiesSet();
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
    public Step jdbcPagingStep() throws Exception {
        return new StepBuilder("jdbcPagingIoStep1", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(itemReader(null))
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job jdbcPagingIoJobBean() throws Exception {
        return new JobBuilder("jdbcPagingIoJob", jobRepository)
                .start(jdbcPagingStep())
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
        testUtils.setJob(jdbcPagingIoJobBean());
        testUtils.setJobRepository(jobRepository);
        return testUtils;
    }

}
