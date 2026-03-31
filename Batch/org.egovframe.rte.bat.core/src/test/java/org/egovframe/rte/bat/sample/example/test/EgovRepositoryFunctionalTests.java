package org.egovframe.rte.bat.sample.example.test;

import org.egovframe.rte.bat.config.SimpleJobLauncherConfig;
import org.egovframe.rte.bat.config.jobs.RepositoryJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 여러 방법으로 repository(대용량 데이터)를 설정하여 배치작업을 실행하는 테스트
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 *
 * 개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.07.31  배치실행개발팀     최초 생성
 * </pre>
 * @since 2012. 07.31
 */

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RepositoryJob.class, SimpleJobLauncherConfig.class})
public class EgovRepositoryFunctionalTests {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job jdbcCursorIoJob;

    @Autowired
    private Job delimitedIoJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 배치작업 테스트 전에 DB관련 작업
     */
    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("DELETE FROM CUSTOMER");
        jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES (1, 0, 'customer1', 100000)");
        jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES (2, 0, 'customer2', 100000)");
        jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES (3, 0, 'customer3', 100000)");
        jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES (4, 0, 'customer4', 100000)");
    }

    /**
     * 배치작업 테스트
     */
    @Test
    public void testUpdateCredit() throws Exception {
        JobExecution jobExecution = jobLauncher.run(jdbcCursorIoJob, getUniqueJobParameters());
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        jobExecution = jobLauncher.run(delimitedIoJob, getUniqueJobParameters());
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }

    /**
     * 잡파라미터를 설정하기 위한 메소드
     */
    private JobParameters getUniqueJobParameters() {
        return new JobParametersBuilder()
                .addString("inputFile", "src/test/resources/META-INF/spring/delimited.csv")
                .addLong("timestamp", new Date().getTime())
                .toJobParameters();
    }

}
