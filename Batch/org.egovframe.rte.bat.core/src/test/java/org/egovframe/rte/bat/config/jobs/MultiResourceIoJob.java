package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCreditIncreaseProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.MultiResourceItemWriter;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;

/**
 * 여러 개의 입력 파일을 순차적으로 읽고, 여러 개의 출력 파일로 분할하여 쓰는 멀티 리소스 배치 작업
 */
@Configuration
@Import(BatchTestConfig.class)
public class MultiResourceIoJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Step multiResourceStep() throws Exception {
        return new StepBuilder("multiResourceIoStep1", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(multiResourceReader(null))
                .processor(itemProcessor())
                .writer(multiResourceWriter(null))
                .build();
    }

    @Bean
    public Job multiResourceIoJobBean() throws Exception {
        return new JobBuilder("multiResourceIoJob", jobRepository)
                .start(multiResourceStep())
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
        testUtils.setJob(multiResourceIoJobBean());
        testUtils.setJobRepository(jobRepository);
        return testUtils;
    }

    @Bean
    @StepScope
    public MultiResourceItemReader<CustomerCredit> multiResourceReader(
            @Value("#{jobParameters['input.file.path']}") String inputFilePattern) throws Exception {

        FlatFileItemReader<CustomerCredit> delegate = new FlatFileItemReader<>();
        DefaultLineMapper<CustomerCredit> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("name", "credit");

        BeanWrapperFieldSetMapper<CustomerCredit> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CustomerCredit.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        delegate.setLineMapper(lineMapper);

        // 파일 패턴에 따라 리소스 처리 방식 결정
        Resource[] inputFiles;
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        if (inputFilePattern != null && inputFilePattern.startsWith("./target/test-outputs/")) {
            // 출력 파일을 다시 읽는 경우 (pointReaderToOutput)는 file: 접두어 추가
            inputFiles = resolver.getResources("file:" + inputFilePattern);
            // 파일이 없는 경우 빈 배열 반환
            if (inputFiles.length == 0) {
                inputFiles = new Resource[0];
            }
        } else {
            // 일반적인 경우는 classpath: 접두어 추가
            if (inputFilePattern != null && !inputFilePattern.startsWith("classpath:")) {
                inputFilePattern = "classpath:" + inputFilePattern;
            }
            inputFiles = resolver.getResources(inputFilePattern);
        }

        MultiResourceItemReader<CustomerCredit> reader = new MultiResourceItemReader<>();
        reader.setResources(inputFiles);
        reader.setDelegate(delegate);
        return reader;
    }

    @Bean
    public ItemProcessor<CustomerCredit, CustomerCredit> itemProcessor() {
        return new CustomerCreditIncreaseProcessor();
    }

    @Bean
    @StepScope
    public MultiResourceItemWriter<CustomerCredit> multiResourceWriter(
            @Value("#{jobParameters['output.file.path']}") String outputBasePath) {

        // 출력 디렉토리가 존재하지 않으면 생성
        if (outputBasePath != null) {
            File outputFile = new File(outputBasePath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
        }

        FlatFileItemWriter<CustomerCredit> delegate = new FlatFileItemWriter<>();
        DelimitedLineAggregator<CustomerCredit> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<CustomerCredit> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{"name", "credit"});
        aggregator.setFieldExtractor(extractor);
        delegate.setLineAggregator(aggregator);

        MultiResourceItemWriter<CustomerCredit> writer = new MultiResourceItemWriter<>();
        writer.setDelegate(delegate);
        writer.setResource(new FileSystemResource(outputBasePath));
        writer.setItemCountLimitPerResource(6);
        return writer;
    }

}
