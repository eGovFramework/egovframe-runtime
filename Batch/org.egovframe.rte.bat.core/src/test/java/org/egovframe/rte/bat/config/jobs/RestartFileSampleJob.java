package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCreditIncreaseProcessor;
import org.egovframe.rte.bat.sample.test.EgovRestartFileSampleFunctionalTests;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 파일 기반의 배치 작업이며, Job 재시작(Restartable Job) 기능 테스트를 위한 샘플
 */
@Configuration
@Import(BatchTestConfig.class)
public class RestartFileSampleJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JobLauncher jobLauncher;

    @Bean
    public Step srestartFileSampleStep1() {
        return new StepBuilder("srestartFileSampleStep1", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(5, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job restartFileSampleJobBean() {
        return new JobBuilder("restartFileSampleJob", jobRepository)
                .start(srestartFileSampleStep1())
                .build();
    }

    @Bean
    public FlatFileItemReader<CustomerCredit> itemReader() {
        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("META-INF/spring/restartFile.csv"));

        DefaultLineMapper<CustomerCredit> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("name", "credit");

        BeanWrapperFieldSetMapper<CustomerCredit> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(CustomerCredit.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);

        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    public ItemProcessor<CustomerCredit, CustomerCredit> itemProcessor() {
        return new CustomerCreditIncreaseProcessor();
    }

    @Bean
    public FlatFileItemWriter<CustomerCredit> itemWriter() {
        FlatFileItemWriter<CustomerCredit> writer = new EgovRestartFileSampleFunctionalTests.CustomerCreditFlatFileItemWriter();
        writer.setResource(outputResource());

        DelimitedLineAggregator<CustomerCredit> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<CustomerCredit> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{"name", "credit"});

        aggregator.setFieldExtractor(extractor);
        writer.setLineAggregator(aggregator);

        return writer;
    }

    @Bean
    public FileSystemResource outputResource() {
        return new FileSystemResource("target/test-outputs/restartFileOutput.csv");
    }

    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() throws Exception {
        JobLauncherTestUtils testUtils = new JobLauncherTestUtils();
        testUtils.setJob(restartFileSampleJobBean());
        testUtils.setJobRepository(jobRepository);
        testUtils.setJobLauncher(jobLauncher);
        return testUtils;
    }

}
