package org.egovframe.rte.bat.sample.example.test;

import org.egovframe.rte.bat.config.SimpleJobLauncherConfig;
import org.egovframe.rte.bat.config.jobs.LogManagementJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 배치 실행로그 를 확인하기 위한 테스트 (Registry와 DB)
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 07.30  배치실행개발팀    최초 생성
 *
 * </pre>
 * @since 2012. 07.30
 */

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LogManagementJob.class, SimpleJobLauncherConfig.class})
public class EgovLogManagementFunctionalTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRegistry jobRegistry;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * DataSource 세팅
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
        jdbcTemplate.update("DELETE from ERROR_LOG");
    }

    /**
     * 배치 작업 테스트
     */
    @Test
    public void testUpdateCredit() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        // Registry : Job 정보에 대한 로그기록
        assertEquals("[logManagementJob]", jobRegistry.getJobNames().toString());

        // DB : ERROR_LOG 테이블의 로그 수
        assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "ERROR_LOG"));

        // DB : ERROR_LOG 테이블의 로그 데이터
        assertEquals("3 records were skipped!", jdbcTemplate.queryForObject("SELECT MESSAGE from ERROR_LOG where JOB_NAME = ?", String.class, "logManagementJob"));
    }

}
