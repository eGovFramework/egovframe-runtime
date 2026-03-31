package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.example.listener.EgovSampleChunkPreProcessor;
import org.egovframe.rte.bat.sample.example.listener.EgovSampleJobPreProcessor;
import org.egovframe.rte.bat.sample.example.listener.EgovSampleStepPreProcessor;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Job/Step/Chunk 수준에서 작업 시작 전 실행되는 Listener를 사용하는 배치 작업
 */
@Configuration
@Import(BatchTestConfig.class)
public class PreProcessorJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Step preProcessorStep1() {
        return new StepBuilder("preProcessorStep1", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(itemReader(null))
                .writer(itemWriter(null))
                .listener(chunkPreListener())
                .listener((ItemReadListener<? super CustomerCredit>) stepPreListener())
                .build();
    }

    @Bean
    public Job preProcessorJobBean() {
        return new JobBuilder("preProcessorJob", jobRepository)
                .start(preProcessorStep1())
                .listener(jobPreListener())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CustomerCredit> itemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {

        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(inputFile));

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("name", "credit");

        BeanWrapperFieldSetMapper<CustomerCredit> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CustomerCredit.class);

        DefaultLineMapper<CustomerCredit> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CustomerCredit> itemWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile) {

        FlatFileItemWriter<CustomerCredit> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputFile));

        DelimitedLineAggregator<CustomerCredit> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<CustomerCredit> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{"name", "credit"});

        aggregator.setFieldExtractor(extractor);
        writer.setLineAggregator(aggregator);

        return writer;
    }

    @Bean
    public EgovSampleJobPreProcessor jobPreListener() {
        return new EgovSampleJobPreProcessor();
    }

    @Bean
    public EgovSampleStepPreProcessor stepPreListener() {
        return new EgovSampleStepPreProcessor();
    }

    @Bean
    public EgovSampleChunkPreProcessor chunkPreListener() {
        return new EgovSampleChunkPreProcessor();
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
        testUtils.setJob(preProcessorJobBean());
        testUtils.setJobRepository(jobRepository);
        return testUtils;
    }

}
