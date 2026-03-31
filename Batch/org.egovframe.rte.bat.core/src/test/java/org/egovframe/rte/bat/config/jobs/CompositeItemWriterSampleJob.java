package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.sample.domain.trade.JdbcTradeDao;
import org.egovframe.rte.bat.sample.domain.trade.Trade;
import org.egovframe.rte.bat.sample.domain.trade.TradeFieldSetMapper;
import org.egovframe.rte.bat.sample.domain.trade.TradeWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.HsqlMaxValueIncrementer;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * CompositeItemWriter를 활용하여 하나의 ItemWriter에서 여러 대상(DAO, 파일 등)으로 동시에 데이터를 출력
 */
@Configuration
@Import(BatchTestConfig.class)
public class CompositeItemWriterSampleJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public FixedLengthTokenizer fixedFileTokenizer() {
        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setNames("ISIN", "Quantity", "Price", "Customer");
        tokenizer.setColumns(new Range(1, 12), new Range(13, 15), new Range(16, 20), new Range(21, 29));
        return tokenizer;
    }

    @Bean
    public TradeFieldSetMapper fieldSetMapper() {
        return new TradeFieldSetMapper();
    }

    /**
     * 고정 길이 형식의 파일을 읽는 Reader 설정
     */
    @Bean
    public FlatFileItemReader<Trade> fileItemReader() {
        FlatFileItemReader<Trade> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("/META-INF/spring/teststream.ImportTradeDataStep.txt"));
        DefaultLineMapper<Trade> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(fixedFileTokenizer());
        lineMapper.setFieldSetMapper(fieldSetMapper());
        reader.setLineMapper(lineMapper);
        return reader;
    }

    /**
     * 데이터 유효성 검사는 생략 (간단한 구현을 위해)
     */

    /**
     * DB에 데이터를 저장하는 DAO 구성
     */
    @Bean
    public JdbcTradeDao tradeDao() {
        JdbcTradeDao dao = new JdbcTradeDao();
        dao.setDataSource(dataSource);
        dao.setIncrementer(incrementer());
        return dao;
    }

    @Bean
    public DataFieldMaxValueIncrementer incrementer() {
        HsqlMaxValueIncrementer incrementer = new HsqlMaxValueIncrementer();
        incrementer.setDataSource(dataSource);
        incrementer.setIncrementerName("TRADE_SEQ");
        incrementer.setColumnName("ID");
        return incrementer;
    }

    @Bean
    public TradeWriter tradeWriter() {
        TradeWriter writer = new TradeWriter();
        writer.setDao(tradeDao());
        return writer;
    }

    /**
     * 결과를 파일로 출력
     */
    @Bean
    public FlatFileItemWriter<Trade> fileItemWriter1() {
        FlatFileItemWriter<Trade> writer = new FlatFileItemWriter<>();
        writer.setName("fw1");
        writer.setResource(new FileSystemResource("target/test-outputs/CustomerReport1.txt"));
        writer.setLineAggregator(new PassThroughLineAggregator<>());
        return writer;
    }

    /**
     * 결과를 파일로 출력
     */
    @Bean
    public FlatFileItemWriter<Trade> fileItemWriter2() {
        FlatFileItemWriter<Trade> writer = new FlatFileItemWriter<>();
        writer.setName("fw2");
        writer.setResource(new FileSystemResource("target/test-outputs/CustomerReport2.txt"));
        writer.setLineAggregator(new PassThroughLineAggregator<>());
        return writer;
    }

    /**
     * tradeDao() 와 fileItemWriter() 를 묶어서 실행
     */
    @Bean
    public CompositeItemWriter<Trade> compositeWriter() {
        CompositeItemWriter<Trade> writer = new CompositeItemWriter<>();
        List<ItemWriter<? super Trade>> delegates = new ArrayList<>();
        delegates.add(tradeWriter());
        delegates.add(fileItemWriter1());
        delegates.add(fileItemWriter2());
        writer.setDelegates(delegates);
        return writer;
    }

    @Bean
    public Step compositeItemWriterStep() {
        return new StepBuilder("compositeItemWriterStep1", jobRepository)
                .<Trade, Trade>chunk(1, transactionManager)
                .reader(fileItemReader())
                .writer(compositeWriter())
                .stream(fileItemReader())
                .stream(fileItemWriter1())
                .stream(fileItemWriter2())
                .build();
    }

    @Bean
    public Job compositeItemWriterJob() {
        return new JobBuilder("compositeItemWriterJob", jobRepository)
                .start(compositeItemWriterStep())
                .build();
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
        testUtils.setJob(compositeItemWriterJob());
        testUtils.setJobRepository(jobRepository);
        return testUtils;
    }

}
