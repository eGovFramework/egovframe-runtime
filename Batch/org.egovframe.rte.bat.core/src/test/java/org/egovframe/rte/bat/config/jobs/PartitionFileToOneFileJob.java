package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;

/**
 * Partitioning 기능을 사용해 다수의 CSV 파일을 병렬 처리한 후, 하나의 단일 파일로 결과를 병합하는 작업
 */
@Configuration
@Import(BatchTestConfig.class)
public class PartitionFileToOneFileJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Partitioner partitioner() throws IOException {
        MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
        partitioner.setResources(new PathMatchingResourcePatternResolver()
                .getResources("classpath:META-INF/spring/delimited*.csv"));
        return partitioner;
    }

    @Bean
    public Step slaveStep() {
        return new StepBuilder("step1", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(3, transactionManager)
                .reader(itemReader(null))
                .writer(itemWriter(null))
                .build();
    }

    @Bean
    public Step partitionStep() throws IOException {
        return new StepBuilder("step", jobRepository)
                .partitioner("step1", partitioner())
                .step(slaveStep())
                .gridSize(4)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job partitionJob() throws IOException {
        return new JobBuilder("partitionJob", jobRepository)
                .start(partitionStep())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("partition_executor");
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CustomerCredit> itemReader(
            @Value("#{stepExecutionContext['fileName']}") Resource fileName) {
        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();
        reader.setResource(fileName);

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("name", "credit");

        BeanWrapperFieldSetMapper<CustomerCredit> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(CustomerCredit.class);

        DefaultLineMapper<CustomerCredit> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);

        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CustomerCredit> itemWriter(
            @Value("#{stepExecutionContext['fileName']}") String fileName) {
        FlatFileItemWriter writer = new FlatFileItemWriter();

        // fileName에서 파일명 추출하여 유니크한 출력 파일명 생성
        String baseFileName = "partition";
        if (fileName != null) {
            // fileName에서 실제 파일명 부분만 추출
            String[] parts = fileName.split("/");
            String actualFileName = parts[parts.length - 1];
            if (actualFileName.contains(".")) {
                baseFileName = actualFileName.substring(0, actualFileName.lastIndexOf("."));
            }
        }

        writer.setResource(new FileSystemResource("./target/test-outputs/partition/file/" + baseFileName + "_output.csv"));

        DelimitedLineAggregator<CustomerCredit> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<CustomerCredit> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{"name", "credit"});
        aggregator.setFieldExtractor(extractor);

        writer.setLineAggregator(aggregator);
        return writer;
    }

    /**
     * 테스트에서 입력 데이터를 읽기 위한 Reader (모든 입력 파일 읽기용)
     */
    @Bean
    public MultiResourceItemReader<CustomerCredit> inputReader() throws IOException {
        MultiResourceItemReader<CustomerCredit> reader = new MultiResourceItemReader<>();
        reader.setResources(new PathMatchingResourcePatternResolver()
                .getResources("classpath:META-INF/spring/delimited*.csv"));

        FlatFileItemReader<CustomerCredit> delegate = new FlatFileItemReader<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("name", "credit");

        BeanWrapperFieldSetMapper<CustomerCredit> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(CustomerCredit.class);

        DefaultLineMapper<CustomerCredit> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);

        delegate.setLineMapper(lineMapper);
        reader.setDelegate(delegate);
        return reader;
    }

    /**
     * 테스트에서 출력 파일을 읽기 위한 Reader.
     * prototype이므로 getBean() 시점에 출력 경로를 다시 조회하여 Job 실행 후 생성된 파일을 읽는다.
     */
    @Bean
    @Scope(value = "prototype", proxyMode = ScopedProxyMode.NO)
    public MultiResourceItemReader<CustomerCredit> outputTestReader() {
        MultiResourceItemReader<CustomerCredit> reader = new MultiResourceItemReader<>();

        try {
            // 파티션 작업으로 생성된 모든 출력 파일들을 읽음 (_output.csv 패턴)
            reader.setResources(new PathMatchingResourcePatternResolver()
                    .getResources("file:./target/test-outputs/partition/file/*_output.csv"));
        } catch (IOException e) {
            // 파일이 없는 경우 빈 배열 설정
            reader.setResources(new Resource[0]);
        }

        FlatFileItemReader<CustomerCredit> delegate = new FlatFileItemReader<>();
        delegate.setStrict(false); // 파일이 없어도 오류가 발생하지 않도록 설정

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("name", "credit");

        BeanWrapperFieldSetMapper<CustomerCredit> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(CustomerCredit.class);

        DefaultLineMapper<CustomerCredit> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);

        delegate.setLineMapper(lineMapper);
        reader.setDelegate(delegate);
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
