package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.core.item.file.EgovIndexFileReader;
import org.egovframe.rte.bat.core.item.file.EgovIndexFileWriter;
import org.egovframe.rte.bat.core.item.file.transform.EgovFieldExtractor;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 인덱스 기반의 파일을 읽고(EgovIndexFileReader)
 * 다시 파일로 출력하는(EgovIndexFileWriter) 배치 작업
 */
@Configuration
@Import(BatchTestConfig.class)
public class DelimitedToDelimitedJobIndexReaderJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    @StepScope
    public EgovIndexFileReader<CustomerCredit> fileIndexDelimitedItemReader() {
        EgovIndexFileReader<CustomerCredit> reader = new EgovIndexFileReader<>();
        reader.setResourceLoader(resourceLoader);
        reader.setIndexResource("classpath:META-INF/spring/index/csvData_NDX(-1)");

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
    public EgovIndexFileWriter<CustomerCredit> fileIndexDelimitedItemWriter() {
        EgovIndexFileWriter<CustomerCredit> writer = new EgovIndexFileWriter<>();
        writer.setIndexResource("target/test-outputs/csvData_NDX(+1)");

        DelimitedLineAggregator<CustomerCredit> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");

        EgovFieldExtractor<CustomerCredit> fieldExtractor = new EgovFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"name", "credit"});
        try {
            fieldExtractor.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize EgovFieldExtractor", e);
        }
        lineAggregator.setFieldExtractor(fieldExtractor);

        writer.setLineAggregator(lineAggregator);
        return writer;
    }

    @Bean
    public Step delimitedToDelimitedStep() {
        return new StepBuilder("delimitedToDelimitedStep-IndexResource", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(fileIndexDelimitedItemReader())
                .writer(fileIndexDelimitedItemWriter())
                .build();
    }

    @Bean
    public Job delimitedToDelimitedJob() {
        return new JobBuilder("delimitedToDelimitedJob-IndexReaderJob", jobRepository)
                .start(delimitedToDelimitedStep())
                .build();
    }

}
