package org.egovframe.rte.bat.config.jobs;

import org.egovframe.rte.bat.sample.domain.person.PersonService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.adapter.PropertyExtractingDelegatingItemWriter;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * ItemReaderAdapter 및 PropertyExtractingDelegatingItemWriter를 사용해 외부 Bean(PersonService)의 메서드를 호출
 */
@Configuration
public class DelegatingJob {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("classpath:META-INF/testdata/testdb.sql")
                .generateUniqueName(true)
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

    @Bean
    public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setDatabaseType("HSQL");
        factory.setMaxVarCharLength(1000);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public JobExplorer jobExplorer(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public PersonService personService() {
        return new PersonService();
    }

    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor processor = new JobRegistryBeanPostProcessor();
        processor.setJobRegistry(jobRegistry);
        processor.setGroupName("testJobGroup");
        return processor;
    }

    /**
     * Reader Adapter : personService.getData()를 호출하여 데이터를 읽음
     */
    @Bean
    public ItemReader<Object> readerAdapter(PersonService personService) {
        ItemReaderAdapter<Object> adapter = new ItemReaderAdapter<>();
        adapter.setTargetObject(personService);
        adapter.setTargetMethod("getData");
        return adapter;
    }

    /**
     * Writer Adapter : firstName 및 address.city 값을 processPerson() 호출에 인자로 전달
     */
    @Bean
    public ItemWriter<Object> propertyExtractingDelegatingItemWriter(PersonService personService) {
        PropertyExtractingDelegatingItemWriter<Object> writer = new PropertyExtractingDelegatingItemWriter<>();
        writer.setTargetObject(personService);
        writer.setTargetMethod("processPerson");
        writer.setFieldsUsedAsTargetMethodArguments(new String[]{"firstName", "address.city"});
        return writer;
    }

    @Bean
    public Step delegateStep1(PersonService personService, JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("delegateStep1", jobRepository)
                .chunk(3, transactionManager)
                .reader(readerAdapter(personService))
                .writer(propertyExtractingDelegatingItemWriter(personService))
                .build();
    }

    @Bean
    public Job delegateJob(PersonService personService, JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("delegateJob", jobRepository)
                .start(delegateStep1(personService, jobRepository, transactionManager))
                .build();
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        // 테스트를 위해 동기 실행으로 변경
        // jobLauncher.setTaskExecutor(asyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public org.springframework.core.task.TaskExecutor asyncTaskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils(PersonService personService, JobRepository jobRepository, PlatformTransactionManager transactionManager, JobLauncher jobLauncher) throws Exception {
        JobLauncherTestUtils testUtils = new JobLauncherTestUtils();
        testUtils.setJobLauncher(jobLauncher);
        testUtils.setJob(delegateJob(personService, jobRepository, transactionManager));
        testUtils.setJobRepository(jobRepository);
        return testUtils;
    }

}
