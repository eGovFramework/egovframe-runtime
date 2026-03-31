package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCreditIncreaseProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 두 개의 서로 다른 Job을 정의
 */
@Configuration
@Import(BatchTestConfig.class)
public class RepositoryJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // ===== Job 1: JDBC to JDBC =====
    @Bean
    public Step jdbcStep() {
        return new StepBuilder("jdbcCursorIoStep1", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(itemReaderDB())
                .processor(itemProcessor())
                .writer(itemWriterDB())
                .build();
    }

    @Bean
    public Job jdbcCursorIoJob() {
        return new JobBuilder("jdbcCursorIoJob", jobRepository)
                .start(jdbcStep())
                .build();
    }

    // ===== Job 2: File to File =====
    @Bean
    public Step delimitedStep() {
        return new StepBuilder("delimitedIostep1", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(itemReaderFile(null))
                .processor(itemProcessor())
                .writer(itemWriterFile())
                .build();
    }

    @Bean
    public Job delimitedIoJob() {
        return new JobBuilder("delimitedIoJob", jobRepository)
                .start(delimitedStep())
                .build();
    }

    // ===== 공통 Processor =====
    @Bean
    public ItemProcessor<CustomerCredit, CustomerCredit> itemProcessor() {
        return new CustomerCreditIncreaseProcessor();
    }

    // ===== DB Reader/Writer =====
    @Bean
    public JdbcCursorItemReader<CustomerCredit> itemReaderDB() {
        JdbcCursorItemReader<CustomerCredit> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("select ID, NAME, CREDIT from CUSTOMER");
        reader.setRowMapper(new BeanPropertyRowMapper<>(CustomerCredit.class));
        reader.setVerifyCursorPosition(true);
        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<CustomerCredit> itemWriterDB() {
        JdbcBatchItemWriter<CustomerCredit> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("UPDATE CUSTOMER set credit = :credit where id = :id");
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setAssertUpdates(true);
        return writer;
    }

    // ===== File Reader/Writer =====
    @Bean
    @StepScope
    public FlatFileItemReader<CustomerCredit> itemReaderFile(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(inputFile));

        DefaultLineMapper<CustomerCredit> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("name", "credit");

        BeanWrapperFieldSetMapper<CustomerCredit> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CustomerCredit.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CustomerCredit> itemWriterFile() {
        FlatFileItemWriter<CustomerCredit> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource("./target/test-outputs/delimitedOutput.csv"));

        DelimitedLineAggregator<CustomerCredit> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<CustomerCredit> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{"name", "credit"});

        aggregator.setFieldExtractor(extractor);
        writer.setLineAggregator(aggregator);
        return writer;
    }

}
