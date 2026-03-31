package org.egovframe.rte.bat.config;

import org.egovframe.rte.bat.core.launch.support.EgovBatchRunner;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCreditRowMapper;
import org.egovframe.rte.bat.sample.example.support.EgovLogAdvice;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.HsqlMaxValueIncrementer;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.sql.DataSource;
import java.util.Collections;

@Configuration
@EnableAspectJAutoProxy
@EnableBatchProcessing
@PropertySource("classpath:META-INF/properties/globals.properties")
public class BatchTestConfig {

    /**
     * 내장 HSQL 데이터베이스 설정 및 초기화 SQL 실행
     */
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("classpath:META-INF/testdata/testdb.sql")
                .generateUniqueName(true)
                .build();
    }

    /**
     * 트랜잭션 매니저 등록
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

    /**
     * 트랜잭션 Advice 정의 (모든 메서드에 대해 Exception 발생 시 롤백)
     */
    @Bean
    public TransactionInterceptor txAdvice(PlatformTransactionManager transactionManager) {
        RuleBasedTransactionAttribute txAttr = new RuleBasedTransactionAttribute();
        txAttr.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
        txAttr.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        source.addTransactionalMethod("*", txAttr);

        return new TransactionInterceptor(transactionManager, source);
    }

    /**
     * HSQL ID 증가기 기본 설정
     */
    @Bean
    public DataFieldMaxValueIncrementer incrementer(DataSource dataSource) {
        HsqlMaxValueIncrementer incrementer = new HsqlMaxValueIncrementer();
        incrementer.setDataSource(dataSource);
        incrementer.setIncrementerName("DUMMY");
        incrementer.setColumnName("ID");
        return incrementer;
    }

    /**
     * AOP 포인트컷 및 트랜잭션 Advisor 설정
     */
    @Bean
    public Advisor txAdvisor(TransactionInterceptor txAdvice) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* org.egovframe.brte.sample..impl.*Impl.*(..))");
        return new DefaultPointcutAdvisor(pointcut, txAdvice);
    }

    /**
     * Spring AOP에서 빈 후처리기(BeanPostProcessor)로,
     * 등록된 Advisor 인터페이스를 구현한 빈을 찾아서 자동으로 프록시를 생성해주는 역할을 함
     * 이 과정을 통해 Advisor의 포인트컷(Pointcut)이 적용된 빈을 생성하고,
     * 해당 빈을 사용자가 직접적으로 사용하지 않고도 Advisor의 Advice가 적용된 결과를 얻을 수 있게 함
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    /**
     * JdbcTemplate 필요 시 사용
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * LobHandler 등록 (LOB 처리 시 사용)
     */
    @Bean
    public LobHandler lobHandler() {
        return new DefaultLobHandler();
    }

    /**
     * Spring Batch에서 Job 및 Step 실행 정보를 기록(persist)하고 관리하는 저장소
     * JobInstance, JobExecution, StepExecution 등을 DB에 저장
     * Job 재시작, 중단, 완료 상태 등을 관리
     */
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

    /**
     * Spring Batch의 Job 실행 트리거 역할
     */
    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }

    /**
     * Spring Batch에서 Job 이름(String) → Job 인스턴스(Job) 를 매핑해주는 컴포넌트
     * Job 객체를 저장하고 조회하거나 JobOperator나 REST API 기반 배치 실행 관리 도구에서 사용
     */
    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }

    /**
     * Spring Batch의 메타데이터 테이블(job, step 실행 이력 등)에 대한 읽기 전용 역할
     */
    @Bean
    public JobExplorer jobExplorer(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * Spring Batch에서 Job 실행, 중단, 재시작, 상태 조회 등 배치 작업을
     * 프로그래밍 방식 또는 외부에서 제어할 수 있음
     */
    @Bean
    public JobOperator jobOperator(JobExplorer jobExplorer,
                                   JobRepository jobRepository,
                                   JobRegistry jobRegistry,
                                   JobLauncher jobLauncher) {
        SimpleJobOperator operator = new SimpleJobOperator();
        operator.setJobExplorer(jobExplorer);
        operator.setJobRepository(jobRepository);
        operator.setJobRegistry(jobRegistry);
        operator.setJobLauncher(jobLauncher);
        return operator;
    }

    /**
     * 테스트 환경에서 Spring Batch Job과 Step을 실행하고 결과를 검증하는 데 사용
     */
    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() {
        return new JobLauncherTestUtils();
    }

    @Bean
    public EgovBatchRunner egovBatchRunner(JobExplorer jobExplorer, JobRepository jobRepository, JobOperator jobOperator) {
        return new EgovBatchRunner(jobOperator, jobExplorer, jobRepository);
    }

    @Bean
    public org.egovframe.rte.bat.util.ApplicationContextProvider applicationContextProvider() {
        return new org.egovframe.rte.bat.util.ApplicationContextProvider();
    }

    @Bean
    public org.egovframe.rte.bat.core.reflection.EgovReflectionSupport egovReflectionSupport() {
        return new org.egovframe.rte.bat.core.reflection.EgovReflectionSupport();
    }

    @Bean
    public EgovLogAdvice logAdvice() {
        return new EgovLogAdvice();
    }

    @Bean
    public CustomerCreditRowMapper customerCreditRowMapper() {
        return new CustomerCreditRowMapper();
    }

}
