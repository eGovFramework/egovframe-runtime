package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.core.item.file.mapping.EgovDefaultLineMapper;
import org.egovframe.rte.bat.core.item.file.mapping.EgovObjectMapper;
import org.egovframe.rte.bat.core.item.file.transform.EgovDelimitedLineTokenizer;
import org.egovframe.rte.bat.core.item.file.transform.EgovFieldExtractor;
import org.egovframe.rte.bat.exception.CustomerCreditIncreaseProcessor;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Spring Batch를 기반으로 Delimited 형식의 파일을 읽고 처리한 후 다시 Delimited 형식으로 출력하는 설정
 */
@Configuration
@Import(BatchTestConfig.class)
public class BatchExceptionTestJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * Job을 정의하며, 실행할 Step(delimitedToDelimitedStep)을 설정
     */
    @Bean
    public Job delimitedToDelimitedJob() throws Exception {
        return new JobBuilder("delimitedToDelimitedJob", jobRepository)
                .start(delimitedToDelimitedStep())
                .build();
    }

    /**
     * 하나의 Step을 정의하며, Reader, Processor, Writer를 연결하여 처리 흐름을 만듬
     */
    @Bean
    public Step delimitedToDelimitedStep() throws Exception {
        return new StepBuilder("delimitedToDelimitedStep", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(delimitedItemReader(null))  // job parameter 바인딩
                .processor(itemProcessor())
                .writer(delimitedItemWriter())
                .build();
    }

    /**
     * CSV(또는 Delimited) 형식의 파일을 읽어 CustomerCredit 객체로 변환
     */
    @Bean
    @StepScope
    public FlatFileItemReader<CustomerCredit> delimitedItemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) throws Exception {
        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(inputFile));

        EgovDelimitedLineTokenizer tokenizer = new EgovDelimitedLineTokenizer();
        tokenizer.setDelimiter(",");

        EgovObjectMapper<CustomerCredit> objectMapper = new EgovObjectMapper<>();
        objectMapper.setType(CustomerCredit.class);
        objectMapper.setNames(new String[]{"name", "credit"});
        objectMapper.afterPropertiesSet(); // 초기화 메서드 명시적 호출

        EgovDefaultLineMapper lineMapper = new EgovDefaultLineMapper();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setObjectMapper(objectMapper);

        reader.setLineMapper(lineMapper);
        return reader;
    }

    /**
     * 읽어온 데이터를 가공
     */
    @Bean
    public ItemProcessor<CustomerCredit, CustomerCredit> itemProcessor() {
        return new CustomerCreditIncreaseProcessor();
    }

    /**
     * 처리된 데이터를 다시 Delimited 형식으로 파일에 기록
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<CustomerCredit> delimitedItemWriter() {
        FlatFileItemWriter<CustomerCredit> writer = new FlatFileItemWriter<>();

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
        writer.setResource(new FileSystemResource("target/test-outputs/csvOutput_" + timestamp + ".txt"));

        DelimitedLineAggregator<CustomerCredit> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        EgovFieldExtractor<CustomerCredit> extractor = new EgovFieldExtractor<>();
        extractor.setNames(new String[]{"name", "credit"});
        aggregator.setFieldExtractor(extractor);

        writer.setLineAggregator(aggregator);
        return writer;
    }

}
