package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.core.item.file.mapping.EgovDefaultLineMapper;
import org.egovframe.rte.bat.core.item.file.mapping.EgovObjectMapper;
import org.egovframe.rte.bat.core.item.file.transform.EgovDelimitedLineTokenizer;
import org.egovframe.rte.bat.core.item.file.transform.EgovFieldExtractor;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCreditIncreaseProcessor;
import org.egovframe.rte.bat.support.EgovStepVariableListener;
import org.egovframe.rte.bat.support.tasklet.TaskletStep;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
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
import java.util.Properties;

/**
 * Spring Batch를 기반으로 Delimited 형식의 파일을 읽고 처리한 후 다시 Delimited 형식으로 출력하는 설정
 */
@Configuration
@Import(BatchTestConfig.class)
public class DelimitedToDelimitedJobStepVariable {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public StepExecutionListener egovStepVariableListener() {
        EgovStepVariableListener listener = new EgovStepVariableListener();
        Properties pros = new Properties();
        pros.setProperty("StepVariableKey1", "StepVariableValue1");
        pros.setProperty("StepVariableKey2", "StepVariableValue2");
        pros.setProperty("StepVariableKey3", "StepVariableValue3");
        listener.setPros(pros);
        return listener;
    }

    @Bean
    @StepScope
    public Tasklet taskletStep(
            @Value("#{stepExecutionContext['StepVariableKey1']}") String stepVariable) {
        TaskletStep tasklet = new TaskletStep();
        tasklet.setStepVariable(stepVariable);
        return tasklet;
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .tasklet(taskletStep(null), transactionManager)
                .listener(egovStepVariableListener())
                .build();
    }

    @Bean
    public Step delimitedToDelimitedStep() throws Exception {
        return new StepBuilder("delimitedToDelimitedStep-StepVariable", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(itemReader(null))
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job delimitedToDelimitedJob() throws Exception {
        return new JobBuilder("delimitedToDelimitedJob-StepVariable", jobRepository)
                .start(step1())
                .next(delimitedToDelimitedStep())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CustomerCredit> itemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) throws Exception {
        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(inputFile));

        EgovDelimitedLineTokenizer tokenizer = new EgovDelimitedLineTokenizer();
        tokenizer.setDelimiter(",");

        EgovObjectMapper<CustomerCredit> mapper = new EgovObjectMapper<>();
        mapper.setType(CustomerCredit.class);
        mapper.setNames(new String[]{"name", "credit"});
        mapper.afterPropertiesSet();

        EgovDefaultLineMapper lineMapper = new EgovDefaultLineMapper();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setObjectMapper(mapper);

        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    @StepScope
    public ItemProcessor<CustomerCredit, CustomerCredit> itemProcessor() {
        return new CustomerCreditIncreaseProcessor();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CustomerCredit> itemWriter() throws Exception {
        FlatFileItemWriter<CustomerCredit> writer = new FlatFileItemWriter<>();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
        writer.setResource(new FileSystemResource("target/test-outputs/csvOutput_StepVariable_" + timestamp + ".csv"));

        DelimitedLineAggregator<CustomerCredit> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        EgovFieldExtractor<CustomerCredit> fieldExtractor = new EgovFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"name", "credit"});
        fieldExtractor.afterPropertiesSet();

        aggregator.setFieldExtractor(fieldExtractor);
        writer.setLineAggregator(aggregator);
        return writer;
    }

}
