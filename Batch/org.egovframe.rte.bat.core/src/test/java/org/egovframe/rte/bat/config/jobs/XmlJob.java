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
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
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

import java.math.BigDecimal;

/**
 * XML 파일을 읽고, 처리한 후, 다시 XML로 출력하는 배치 작업
 */
@Configuration
@Import(BatchTestConfig.class)
public class XmlJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Step xmlStep1() {
        return new StepBuilder("xmlStep1", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(itemReader(null))
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job xmlJobBean() {
        return new JobBuilder("xmlJob", jobRepository)
                .start(xmlStep1())
                .build();
    }


    @Bean
    @StepScope
    public FlatFileItemReader<CustomerCredit> itemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();

        // 출력 파일을 다시 읽는 경우는 FileSystemResource, 일반적인 경우는 ClassPathResource
        if (inputFile != null && inputFile.startsWith("./target/test-outputs/")) {
            reader.setResource(new FileSystemResource(inputFile));
        } else {
            reader.setResource(new ClassPathResource(inputFile));
        }

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("id", "name", "credit");
        tokenizer.setDelimiter(",");

        DefaultLineMapper<CustomerCredit> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> {
            CustomerCredit customer = new CustomerCredit();
            customer.setId(fieldSet.readInt("id"));
            customer.setName(fieldSet.readString("name"));
            customer.setCredit(new BigDecimal(fieldSet.readString("credit")));
            return customer;
        });

        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    public ItemProcessor<CustomerCredit, CustomerCredit> itemProcessor() {
        return new CustomerCreditIncreaseProcessor();
    }

    @Bean
    public FlatFileItemWriter<CustomerCredit> itemWriter() {
        FlatFileItemWriter<CustomerCredit> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource("target/test-outputs/output.csv"));

        DelimitedLineAggregator<CustomerCredit> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");
        aggregator.setFieldExtractor(item -> new Object[]{
                item.getId(),
                item.getName(),
                item.getCredit()
        });

        writer.setLineAggregator(aggregator);
        writer.setShouldDeleteIfExists(true);
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
        testUtils.setJob(xmlJobBean());
        testUtils.setJobRepository(jobRepository);
        return testUtils;
    }

}
