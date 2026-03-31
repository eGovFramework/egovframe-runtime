package org.egovframe.rte.bat.config.jobs;

import org.apache.ibatis.session.SqlSessionFactory;
import org.egovframe.rte.bat.config.BatchTestConfig;
import org.egovframe.rte.bat.config.MybatisConfig;
import org.egovframe.rte.bat.core.item.database.EgovMyBatisBatchItemWriter;
import org.egovframe.rte.bat.core.item.database.EgovMyBatisPagingItemReader;
import org.egovframe.rte.bat.core.item.file.transform.EgovFieldExtractor;
import org.egovframe.rte.bat.core.tasklet.DummyTasklet;
import org.egovframe.rte.bat.mapper.EmpProcessor;
import org.egovframe.rte.bat.mapper.EmpVO;
import org.egovframe.rte.bat.support.EgovJobVariableListener;
import org.egovframe.rte.bat.support.EgovResourceVariable;
import org.egovframe.rte.bat.support.EgovStepVariableListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
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
import java.util.Properties;

/**
 * Spring Batch + MyBatis 연동을 기반으로,
 * 다양한 데이터 입력/출력 자원(SQL, 파일 등)을 다루며,
 * Job/Step/Resource 변수와 복합적인 처리 흐름을 갖는 배치 작업
 */
@Configuration
@Import({BatchTestConfig.class, MybatisConfig.class})
public class MybatisJob {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Bean
    public EgovJobVariableListener egovJobVariable() {
        EgovJobVariableListener l = new EgovJobVariableListener();
        Properties props = new Properties();
        props.put("JobVariableKey1", "JobVariableValue1");
        props.put("JobVariableKey2", "JobVariableValue2");
        props.put("JobVariableKey3", "JobVariableValue3");
        l.setPros(props);
        return l;
    }

    @Bean
    public EgovStepVariableListener egovStepVariable() {
        EgovStepVariableListener l = new EgovStepVariableListener();
        Properties props = new Properties();
        props.put("StepVariableKey1", "StepVariableValue1");
        props.put("StepVariableKey2", "StepVariableValue2");
        props.put("StepVariableKey3", "StepVariableValue3");
        l.setPros(props);
        return l;
    }

    @Bean
    public EgovResourceVariable egovResourceVariable() {
        EgovResourceVariable v = new EgovResourceVariable();
        Properties props = new Properties();
        props.put("empNo", "7499");
        props.put("empName", "ALLEN");
        props.put("empNoR", "7499");
        props.put("empNameR", "ALLEN");
        props.put("mgrR", "7777");
        v.setPros(props);
        return v;
    }

    @Bean
    public Step dummyStep() {
        return new StepBuilder("sampleStep", jobRepository)
                .tasklet(new DummyTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step mybatisToDbStep() {
        return new StepBuilder("mybatisJobStep1", jobRepository)
                .<EmpVO, EmpVO>chunk(2, transactionManager)
                .reader(mybatisItemReader())
                .processor(itemProcessor())
                .writer(mybatisItemWriter())
                .build();
    }

    @Bean
    public Step mybatisToFileStep() {
        return new StepBuilder("mybatisJobStep2", jobRepository)
                .<EmpVO, EmpVO>chunk(2, transactionManager)
                .reader(mybatisItemReader())
                .processor(itemProcessor())
                .writer(delimitedItemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return new JobBuilder("mybatisJob", jobRepository)
                .start(dummyStep())
                .next(mybatisToDbStep())
                .next(mybatisToFileStep())
                .build();
    }

    @Bean
    @StepScope
    public EgovMyBatisPagingItemReader<EmpVO> mybatisItemReader() {
        EgovMyBatisPagingItemReader<EmpVO> reader = new EgovMyBatisPagingItemReader<>();
        reader.setSqlSessionFactory(sqlSessionFactory);
        reader.setQueryId("EmpMapper.selectEmpList");
        reader.setPageSize(10);
        reader.setResourceVariable(egovResourceVariable());
        reader.setJobVariable(egovJobVariable());
        reader.setStepVariable(egovStepVariable());
        return reader;
    }

    @Bean
    public ItemProcessor<EmpVO, EmpVO> itemProcessor() {
        return new EmpProcessor();
    }

    @Bean
    @StepScope
    public EgovMyBatisBatchItemWriter<EmpVO> mybatisItemWriter() {
        EgovMyBatisBatchItemWriter<EmpVO> writer = new EgovMyBatisBatchItemWriter<>();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setStatementId("EmpMapper.updateEmp1");
        writer.setResourceVariable(egovResourceVariable());
        writer.setJobVariable(egovJobVariable());
        writer.setStepVariable(egovStepVariable());
        return writer;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<EmpVO> delimitedItemWriter() {
        FlatFileItemWriter<EmpVO> writer = new FlatFileItemWriter<>();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
        writer.setResource(new FileSystemResource("target/test-outputs/csvOutput_" + timestamp + ".txt"));

        DelimitedLineAggregator<EmpVO> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        EgovFieldExtractor<EmpVO> extractor = new EgovFieldExtractor<>();
        extractor.setNames(new String[]{"empNo", "empName", "job", "mgr", "hireDate", "sal", "comm", "deptNo"});
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
