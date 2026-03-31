package org.egovframe.rte.bat.sample.example.test;

import org.egovframe.rte.bat.config.SimpleJobLauncherConfig;
import org.egovframe.rte.bat.config.jobs.ParallelJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 멀티쓰레드로 실행하는 배치작업 테스트
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.07.31  배치실행개발팀     최초 생성
 *  </pre>
 * @since 2012. 07.31
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ParallelJob.class, SimpleJobLauncherConfig.class})
public class EgovParallelJobFunctionalTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 데이터소스 세팅
     */
    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 배치작업 테스트 전에 DB관련 작업
     */
    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("DELETE from TRADE");
        jdbcTemplate.update("DELETE from BATCH_STAGING");
    }

    /**
     * 배치작업 테스트
     */
    @Test
    public void testLaunchJob() throws Exception {
        int before = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STAGING");
        JobExecution execution = jobLauncherTestUtils.launchJob();
        int after = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STAGING");
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(after - before, execution.getStepExecutions().iterator().next().getReadCount());
    }

}
