package org.egovframe.rte.bat.sample.test;

import org.egovframe.rte.bat.config.jobs.JdbcPagingIoJob;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

/**
 * DB 의 Table을 설정된 페이지단위로 읽어서 데이터처리를 수행하는 테스트
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see
 * @since 2012. 06.27
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JdbcPagingIoJob.class)
public class EgovJdbcPagingFunctionalTests extends EgovAbstractIoSampleTests {

    private JdbcTemplate jdbcTemplate;

    /**
     * datasource 설정
     */
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 배치작업 테스트 전에 DB관련 작업
     */
    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("DELETE from CUSTOMER");
        jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES  (1, 0, 'customer1', 100000)");
        jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES  (2, 0, 'customer2', 100000)");
        jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES  (3, 0, 'customer3', 100000)");
        jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES  (4, 0, 'customer4', 100000)");
    }

    /**
     * 배치결과를 다시 읽을 때 reader 설정하는 메소드
     */
    @Override
    protected void pointReaderToOutput(ItemReader<CustomerCredit> reader) {
        JobParameters jobParameters = new JobParametersBuilder(super.getUniqueJobParameters())
                .addDouble("credit", 0.)
                .toJobParameters();

        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
        StepSynchronizationManager.close();
        StepSynchronizationManager.register(stepExecution);
    }

    /**
     * 잡파라미터를 설정하기 위한 메소드
     */
    @Override
    protected JobParameters getUniqueJobParameters() {
        return new JobParametersBuilder(super.getUniqueJobParameters())
                .addDouble("credit", 10000.)
                .toJobParameters();
    }

}
