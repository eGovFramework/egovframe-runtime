package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.core.item.file.mapping.EgovDefaultLineMapper;
import org.egovframe.rte.bat.core.item.file.mapping.EgovObjectMapper;
import org.egovframe.rte.bat.core.item.file.transform.EgovDelimitedLineTokenizer;
import org.egovframe.rte.bat.core.item.file.transform.EgovFieldExtractor;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCreditIncreaseProcessor;
import org.egovframe.rte.bat.support.EgovJobVariableListener;
import org.egovframe.rte.bat.support.tasklet.TaskletJob;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
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
public class DelimitedToDelimitedJobJobVariable {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * JobVariable 설정을 위한 Listener
     */
    @Bean
    public JobExecutionListener egovJobVariableListener() {
        EgovJobVariableListener listener = new EgovJobVariableListener();
        Properties props = new Properties();
        props.setProperty("JobVariableKey1", "JobVariableValue1");
        props.setProperty("JobVariableKey2", "JobVariableValue2");
        props.setProperty("JobVariableKey3", "JobVariableValue3");
        listener.setPros(props);
        return listener;
    }

    @Bean
    @StepScope
    public Tasklet taskletJob(
            @Value("#{jobExecutionContext['JobVariableKey1']}") String jobVariable) {
        TaskletJob tasklet = new TaskletJob();
        tasklet.setJobVariable(jobVariable);
        return tasklet;
    }

    @Bean
    public Step taskletStep() {
        return new StepBuilder("step1", jobRepository)
                .tasklet(taskletJob(null), transactionManager) // jobVariable는 StepScope에서 주입됨
                .build();
    }

    @Bean
    public Step chunkStep() throws Exception {
        return new StepBuilder("delimitedToDelimitedStep-JobVariable", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(delimitedItemReader(null)) // inputFile은 StepScope에서 주입됨
                .processor(itemProcessor())
                .writer(delimitedItemWriter())
                .build();
    }

    @Bean
    public Job delimitedToDelimitedJob() throws Exception {
        return new JobBuilder("delimitedToDelimitedJob-JobVariable", jobRepository)
                .listener(egovJobVariableListener())
                .start(taskletStep())
                .next(chunkStep())
                .build();
    }

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

    @Bean
    @StepScope
    public CustomerCreditIncreaseProcessor itemProcessor() {
        return new CustomerCreditIncreaseProcessor();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CustomerCredit> delimitedItemWriter() throws Exception {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
        String outputPath = "./target/test-outputs/csvOutput_JobVariable_" + timestamp + ".csv";

        FlatFileItemWriter<CustomerCredit> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputPath));

        DelimitedLineAggregator<CustomerCredit> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        EgovFieldExtractor extractor = new EgovFieldExtractor();
        extractor.setNames(new String[]{"name", "credit"});
        extractor.afterPropertiesSet(); // 초기화 메서드 명시적 호출
        aggregator.setFieldExtractor(extractor);

        writer.setLineAggregator(aggregator);
        return writer;
    }

}
