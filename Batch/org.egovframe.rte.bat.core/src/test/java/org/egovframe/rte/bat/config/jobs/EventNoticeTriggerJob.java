package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.example.event.EgovEmailEventNoticeTrigger;
import org.egovframe.rte.bat.sample.example.listener.EgovEventNoticeCallProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
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
 * 파일을 입력으로 받아 처리하고 출력하며,
 * Step의 실행 중에 이벤트(예: 이메일, SMS 발송)를 수행하는 리스너를 활용하는 알림 기반 배치 작업
 */
@Configuration
@Import(BatchTestConfig.class)
public class EventNoticeTriggerJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Step eventNoticeTriggerStep() {
        return new StepBuilder("eventNoticeTriggerStep1", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(itemReader(null))
                .writer(itemWriter(null))
                .listener(eventNoticeListener())
                .build();
    }

    @Bean
    public Job eventNoticeTriggerJobBean() {
        return new JobBuilder("eventNoticeTriggerJob", jobRepository)
                .start(eventNoticeTriggerStep())
                .build();
    }

    @Bean
    public StepExecutionListener eventNoticeListener() {
        return new EgovEventNoticeCallProcessor();  // 리스너에서 이메일 또는 SMS 트리거 호출
    }

    @Bean(name = "emailEventNoticeTrigger")
    public EgovEmailEventNoticeTrigger emailEventNoticeTrigger() {
        return new EgovEmailEventNoticeTrigger();  // 이메일 전송 로직
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

        BeanWrapperFieldExtractor<CustomerCredit> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"name", "credit"});

        aggregator.setFieldExtractor(fieldExtractor);
        writer.setLineAggregator(aggregator);
        return writer;
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
        testUtils.setJob(eventNoticeTriggerJobBean());
        testUtils.setJobRepository(jobRepository);
        return testUtils;
    }

}
