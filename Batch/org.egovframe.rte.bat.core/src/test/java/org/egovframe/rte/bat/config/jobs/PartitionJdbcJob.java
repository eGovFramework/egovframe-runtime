package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCreditRowMapper;
import org.egovframe.rte.bat.sample.example.support.EgovColumnRangePartitioner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * DBC 데이터베이스 테이블을 ID 컬럼 기준으로 범위를 나눠 병렬로 읽고, 각각의 결과를 CSV 파일로 출력
 */
@Configuration
@Import(BatchTestConfig.class)
public class PartitionJdbcJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Partitioner partitioner() {
        EgovColumnRangePartitioner partitioner = new EgovColumnRangePartitioner();
        partitioner.setDataSource(dataSource);
        partitioner.setTable("CUSTOMER");
        partitioner.setColumn("ID");
        return partitioner;
    }

    @Bean
    public Step partitionStep() {
        return new StepBuilder("step", jobRepository)
                .partitioner("step1", partitioner())
                .step(slaveStep())
                .gridSize(4)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job partitionJob() {
        return new JobBuilder("partitionJob", jobRepository)
                .start(partitionStep())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean
    public Step slaveStep() {
        return new StepBuilder("step1", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(5, transactionManager)
                .reader(itemReader(null, null))
                .writer(itemWriter(null))
                .listener(fileNameListener())
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<CustomerCredit> itemReader(
            @Value("#{stepExecutionContext[minValue]}") Long minId,
            @Value("#{stepExecutionContext[maxValue]}") Long maxId) {
        JdbcPagingItemReader<CustomerCredit> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(5);
        reader.setRowMapper(new CustomerCreditRowMapper());

        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("minId", minId);
        parameterValues.put("maxId", maxId);
        reader.setParameterValues(parameterValues);

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("ID, NAME, CREDIT");
        queryProvider.setFromClause("CUSTOMER");
        queryProvider.setWhereClause("ID >= :minId and ID <= :maxId");
        queryProvider.setSortKey("ID");

        try {
            reader.setQueryProvider(queryProvider.getObject());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create query provider", e);
        }
        return reader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CustomerCredit> itemWriter(
            @Value("#{stepExecutionContext[outputFile]}") String outputFilePath) {
        FlatFileItemWriter<CustomerCredit> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputFilePath));

        DelimitedLineAggregator<CustomerCredit> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<CustomerCredit> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{"id", "name", "credit"});

        aggregator.setFieldExtractor(extractor);
        writer.setLineAggregator(aggregator);
        return writer;
    }

    @Bean
    @StepScope
    public StepExecutionListener fileNameListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                ExecutionContext executionContext = stepExecution.getExecutionContext();
                String inputName = stepExecution.getStepName().replace(":", "-");
                if (!executionContext.containsKey("outputFile")) {
                    executionContext.putString("outputFile", "./target/test-outputs/partition/db/" + inputName + ".csv");
                }
            }
        };
    }

    /**
     * 테스트에서 입력 데이터를 읽기 위한 Reader (전체 데이터 읽기용)
     */
    @Bean
    public JdbcPagingItemReader<CustomerCredit> inputReader() {
        JdbcPagingItemReader<CustomerCredit> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(10);
        reader.setRowMapper(new CustomerCreditRowMapper());

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("ID, NAME, CREDIT");
        queryProvider.setFromClause("CUSTOMER");
        queryProvider.setSortKey("ID");

        try {
            reader.setQueryProvider(queryProvider.getObject());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize inputReader", e);
        }
        return reader;
    }

    /**
     * 테스트에서 출력 파일을 읽기 위한 Reader Factory
     * StepScope 제거하여 테스트에서 직접 접근 가능하도록 함
     */
    @Bean
    public FlatFileItemReader<CustomerCredit> outputTestReader() {
        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("id", "name", "credit");

        BeanWrapperFieldSetMapper<CustomerCredit> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CustomerCredit.class);

        DefaultLineMapper<CustomerCredit> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        reader.setLineMapper(lineMapper);
        return reader;
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
        testUtils.setJob(partitionJob());
        testUtils.setJobRepository(jobRepository);
        return testUtils;
    }

}
