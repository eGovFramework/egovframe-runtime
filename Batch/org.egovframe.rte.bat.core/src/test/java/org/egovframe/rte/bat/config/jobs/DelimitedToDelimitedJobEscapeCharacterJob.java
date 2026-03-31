package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.core.item.file.mapping.EgovDefaultLineMapper;
import org.egovframe.rte.bat.core.item.file.mapping.EgovObjectMapper;
import org.egovframe.rte.bat.core.item.file.transform.EgovEscapableDelimitedLineTokenizer;
import org.egovframe.rte.bat.core.item.file.transform.EgovFieldExtractor;
import org.egovframe.rte.bat.domain.CustomerCredit;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Spring Batch를 기반으로 Delimited 형식의 파일을 읽고 처리한 후 다시 Delimited 형식으로 출력하는 설정
 */
@Configuration
@Import(BatchTestConfig.class)
public class DelimitedToDelimitedJobEscapeCharacterJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * 입력 파일을 읽는 FlatFileItemReader Bean 정의.
     * EgovEscapableDelimitedLineTokenizer를 사용해 Escape 문자 지원
     */
    @Bean
    @StepScope
    public FlatFileItemReader<CustomerCredit> delimitedItemReader() {
        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource("./src/test/resources/org/egovframe/data/input/csvDataEscapeCharacter.csv"));

        EgovEscapableDelimitedLineTokenizer tokenizer = new EgovEscapableDelimitedLineTokenizer();
        tokenizer.setDelimiter(",");

        EgovObjectMapper<CustomerCredit> objectMapper = new EgovObjectMapper<>();
        objectMapper.setType(CustomerCredit.class);
        objectMapper.setNames(new String[]{"name", "credit"});
        try {
            objectMapper.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize EgovObjectMapper", e);
        }

        EgovDefaultLineMapper lineMapper = new EgovDefaultLineMapper();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setObjectMapper(objectMapper);

        reader.setLineMapper(lineMapper);
        return reader;
    }

    /**
     * 데이터를 CSV 형식으로 출력
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<CustomerCredit> delimitedItemWriter() {
        FlatFileItemWriter<CustomerCredit> writer = new FlatFileItemWriter<>();

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
        writer.setResource(new FileSystemResource("./target/csvOutput_EscapeCharacter_" + timestamp + ".csv"));
        writer.setShouldDeleteIfExists(true);

        DelimitedLineAggregator<CustomerCredit> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        EgovFieldExtractor<CustomerCredit> extractor = new EgovFieldExtractor<>();
        extractor.setNames(new String[]{"name", "credit"});
        try {
            extractor.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize EgovFieldExtractor", e);
        }
        aggregator.setFieldExtractor(extractor);

        writer.setLineAggregator(aggregator);
        return writer;
    }

    @Bean
    public Step delimitedToDelimitedStep() {
        return new StepBuilder("delimitedToDelimitedStep-EscapeCharacter", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(1000, transactionManager)
                .reader(delimitedItemReader())
                .writer(delimitedItemWriter())
                .build();
    }

    @Bean
    public Job delimitedToDelimitedJob() {
        return new JobBuilder("delimitedToDelimitedJob-EscapeCharacterJob", jobRepository)
                .start(delimitedToDelimitedStep())
                .build();
    }

}
