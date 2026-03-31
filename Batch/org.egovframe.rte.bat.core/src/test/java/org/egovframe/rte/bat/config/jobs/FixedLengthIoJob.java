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
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 고정 길이(고정 폭) 포맷의 파일을 읽고 처리한 후 동일한 고정 길이 포맷으로 출력하는 배치 작업
 */
@Configuration
@Import(BatchTestConfig.class)
public class FixedLengthIoJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    @StepScope
    public FlatFileItemReader<CustomerCredit> itemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();
        // 출력 파일을 다시 읽는 경우 (pointReaderToOutput)는 FileSystemResource 사용
        if (inputFile != null && inputFile.startsWith("./target/test-outputs/")) {
            reader.setResource(new FileSystemResource(inputFile));
        } else {
            reader.setResource(new ClassPathResource(inputFile));
        }

        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setNames("name", "credit");
        tokenizer.setColumns(new Range(1, 9), new Range(10, 11));

        BeanWrapperFieldSetMapper<CustomerCredit> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CustomerCredit.class);

        DefaultLineMapper<CustomerCredit> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    public ItemProcessor<CustomerCredit, CustomerCredit> itemProcessor() {
        return new CustomerCreditIncreaseProcessor();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CustomerCredit> itemWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile) {
        FlatFileItemWriter<CustomerCredit> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputFile));

        FormatterLineAggregator<CustomerCredit> aggregator = new FormatterLineAggregator<>();
        aggregator.setFormat("%-9s%-2.0f");

        BeanWrapperFieldExtractor<CustomerCredit> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{"name", "credit"});

        aggregator.setFieldExtractor(extractor);
        writer.setLineAggregator(aggregator);
        return writer;
    }

    @Bean
    public Step fixedLengthStep() {
        return new StepBuilder("fixedLengthIostep1", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(itemReader(null))
                .processor(itemProcessor())
                .writer(itemWriter(null))
                .build();
    }

    @Bean
    public Job fixedLengthIoJobBean() {
        return new JobBuilder("fixedLengthIoJob", jobRepository)
                .start(fixedLengthStep())
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
        testUtils.setJob(fixedLengthIoJobBean());
        testUtils.setJobRepository(jobRepository);
        return testUtils;
    }

}
