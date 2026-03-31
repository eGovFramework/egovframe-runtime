package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.core.item.file.mapping.EgovDefaultLineMapper;
import org.egovframe.rte.bat.core.item.file.mapping.EgovObjectMapper;
import org.egovframe.rte.bat.core.item.file.transform.EgovDelimitedLineTokenizer;
import org.egovframe.rte.bat.core.item.file.transform.EgovFieldExtractor;
import org.egovframe.rte.bat.core.step.TaskletCreateDataStep;
import org.egovframe.rte.bat.core.step.TaskletDeleteStep;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 세 개의 스텝(데이터 생성 → 읽기/쓰기 → 파일 삭제)으로 구성된 Spring Batch Job을 정의한 XML 설정
 */
@Configuration
@Import(BatchTestConfig.class)
public class DelimitedToDelimitedJobDeleteStep {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private org.egovframe.rte.bat.core.reflection.EgovReflectionSupport egovReflectionSupport;

    /**
     * 테스트 데이터 생성 Step
     */
    @Bean
    public Step createTestDataStep() {
        return new StepBuilder("stepDelimitedToDelimitedJob-DeleteStep-CreateTestData", jobRepository)
                .tasklet(taskletCreateTestData(), transactionManager)
                .build();
    }

    /**
     * 위 createTestDataStep()에서 사용할 Tasklet Bean 정의
     */
    @Bean
    public Tasklet taskletCreateTestData() {
        return new TaskletCreateDataStep();
    }

    /**
     * 입력 파일을 읽기 위한 Reader 설정
     */
    @Bean
    public FlatFileItemReader<CustomerCredit> itemReader() {
        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("META-INF/spring/delete/csvData.csv"));

        EgovDelimitedLineTokenizer tokenizer = new EgovDelimitedLineTokenizer();
        tokenizer.setDelimiter(",");

        EgovObjectMapper<CustomerCredit> objectMapper = new EgovObjectMapper<>();
        objectMapper.setType(CustomerCredit.class);
        objectMapper.setNames(new String[]{"name", "credit"});
        // EgovReflectionSupport 수동 설정
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
     * 결과 데이터를 쓰기 위한 Writer 설정
     */
    @Bean
    public FlatFileItemWriter<CustomerCredit> itemWriter() {
        FlatFileItemWriter<CustomerCredit> writer = new FlatFileItemWriter<>();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
        writer.setResource(new FileSystemResource("./target/csvOutput_DeleteStep_" + timestamp + ".csv"));

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

    /**
     * 최종적으로 입력 파일을 삭제하는 Step
     */
    @Bean
    public Step deleteResourceStep() {
        return new StepBuilder("stepDelimitedToDelimitedJob-DeleteStep-DeleteResource", jobRepository)
                .tasklet(taskletDelete(), transactionManager)
                .build();
    }

    /**
     * 삭제용 Tasklet Bean 정의
     */
    @Bean
    public Tasklet taskletDelete() {
        TaskletDeleteStep tasklet = new TaskletDeleteStep();
        tasklet.setDirectory(new ClassPathResource("META-INF/spring/delete/"));
        return tasklet;
    }

    /**
     * FlatFileItemReader로 읽고, FlatFileItemWriter로 쓰는 Step (Delimited → Delimited 파일 변환)
     */
    @Bean
    public Step itemReaderWriterStep() {
        return new StepBuilder("stepDelimitedToDelimitedJob-DeleteStep-ItemReaderWriter", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(itemReader())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job delimitedToDelimitedJobDeleteStepBean() {
        return new JobBuilder("delimitedToDelimitedJob-DeleteStep", jobRepository)
                .start(createTestDataStep())
                .next(itemReaderWriterStep())
                .next(deleteResourceStep())
                .build();
    }

    /**
     * TaskletCreateDataStep에서 사용하는 Writer Bean
     */
    @Bean(name = "delimitedToDelimitedJob-CreateData-delimitedItemWriter")
    public FlatFileItemWriter<CustomerCredit> createDataItemWriter() {
        FlatFileItemWriter<CustomerCredit> writer = new FlatFileItemWriter<>();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
        writer.setResource(new FileSystemResource("./target/test-outputs/createData_" + timestamp + ".csv"));

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

}
