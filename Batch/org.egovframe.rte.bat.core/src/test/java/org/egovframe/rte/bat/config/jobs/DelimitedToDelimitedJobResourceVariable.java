package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.core.item.file.mapping.EgovDefaultLineMapper;
import org.egovframe.rte.bat.core.item.file.mapping.EgovObjectMapper;
import org.egovframe.rte.bat.core.item.file.transform.EgovDelimitedLineTokenizer;
import org.egovframe.rte.bat.core.item.file.transform.EgovFieldExtractor;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCreditIncreaseProcessor;
import org.egovframe.rte.bat.support.EgovResourceVariable;
import org.egovframe.rte.bat.support.tasklet.TaskletStepResource;
import org.egovframe.rte.bat.support.tasklet.TaskletStepResource2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
public class DelimitedToDelimitedJobResourceVariable {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job delimitedToDelimitedJob() throws Exception {
        return new JobBuilder("delimitedToDelimitedJob-ResourceVariable", jobRepository)
                .start(step1())
                .next(step2())
                .next(chunkStep())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("Rstep1", jobRepository)
                .tasklet(taskletRStep1(), transactionManager)
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("Rstep2", jobRepository)
                .tasklet(taskletRStep2(), transactionManager)
                .build();
    }

    @Bean
    public Step chunkStep() throws Exception {
        return new StepBuilder("delimitedToDelimitedStep-ResourceVariable", jobRepository)
                .<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
                .reader(itemReader(egovResourceVariable()))
                .processor(itemProcessor())
                .writer(itemWriter(egovResourceVariable()))
                .build();
    }

    @Bean
    public EgovResourceVariable egovResourceVariable() {
        EgovResourceVariable variable = new EgovResourceVariable();
        Properties props = new Properties();
        props.setProperty("input.resource", "META-INF/spring/delimited.csv");
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
        props.setProperty("writer.resource", "./target/test-outputs/csvOutput_ResourceVariable_" + timestamp + ".csv");
        variable.setPros(props);
        return variable;
    }

    @Bean
    @StepScope
    public Tasklet taskletRStep1() {
        return new TaskletStepResource();
    }

    @Bean
    @StepScope
    public Tasklet taskletRStep2() {
        return new TaskletStepResource2();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CustomerCredit> itemReader(EgovResourceVariable egovResourceVariable) throws Exception {
        FlatFileItemReader<CustomerCredit> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource((String) egovResourceVariable.getVariable("input.resource")));

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
    public ItemProcessor<CustomerCredit, CustomerCredit> itemProcessor() {
        return new CustomerCreditIncreaseProcessor();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CustomerCredit> itemWriter(EgovResourceVariable egovResourceVariable) throws Exception {
        FlatFileItemWriter<CustomerCredit> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource((String) egovResourceVariable.getVariable("writer.resource")));

        DelimitedLineAggregator<CustomerCredit> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        EgovFieldExtractor<CustomerCredit> extractor = new EgovFieldExtractor<>();
        extractor.setNames(new String[]{"name", "credit"});
        extractor.afterPropertiesSet(); // 초기화 메서드 명시적 호출
        aggregator.setFieldExtractor(extractor);

        writer.setLineAggregator(aggregator);
        return writer;
    }

}
