package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.example.listener.EgovSampleChunkPostProcessor;
import org.egovframe.rte.bat.sample.example.listener.EgovSampleJobPostProcessor;
import org.egovframe.rte.bat.sample.example.listener.EgovSampleStepPostProcessor;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Job/Step/Chunk 수준의 Post Listener를 각각 설정하여, 파일 기반 배치 처리 후 특정 후처리 로직을 수행
 */
@Configuration
@Import(BatchTestConfig.class)
public class PostProcessorJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JobLauncher jobLauncher;

    @Bean
    public Step postProcessorStep1() {
        return new StepBuilder("postProcessorStep1", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(itemReader(null))
                .writer(itemWriter(null))
                .listener(chunkPostListener())
                .listener((ItemReadListener<? super CustomerCredit>) stepPostListener())
                .build();
    }

    @Bean
    public Job postProcessorJobBean() {
        return new JobBuilder("postProcessorJob", jobRepository)
                .start(postProcessorStep1())
                .listener(jobPostListener())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CustomerCredit> itemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(inputFile));

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
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
    public EgovSampleJobPostProcessor jobPostListener() {
        return new EgovSampleJobPostProcessor();
    }

    @Bean
    public EgovSampleStepPostProcessor stepPostListener() {
        return new EgovSampleStepPostProcessor();
    }

    @Bean
    public EgovSampleChunkPostProcessor chunkPostListener() {
        return new EgovSampleChunkPostProcessor();
    }

    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() throws Exception {
        JobLauncherTestUtils testUtils = new JobLauncherTestUtils();
        testUtils.setJob(postProcessorJobBean());
        testUtils.setJobRepository(jobRepository);
        testUtils.setJobLauncher(jobLauncher);
        return testUtils;
    }

}
