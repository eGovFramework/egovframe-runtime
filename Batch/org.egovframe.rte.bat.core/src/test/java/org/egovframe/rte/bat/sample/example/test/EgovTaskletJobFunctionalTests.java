package org.egovframe.rte.bat.sample.example.test;

import org.egovframe.rte.bat.config.SimpleJobLauncherConfig;
import org.egovframe.rte.bat.config.jobs.TaskLetJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Job의 실행중 배치작업 외 단순 처리가 필요한 작업의 처리 기능을 테스트
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
@ContextConfiguration(classes = {TaskLetJob.class, SimpleJobLauncherConfig.class})
public class EgovTaskletJobFunctionalTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    /**
     * 배치작업 테스트
     */
    @Test
    public void testLaunchJob() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(
                new JobParametersBuilder()
                        .addString("value", "foo")
                        .addLong("timestamp", System.currentTimeMillis())
                        .toJobParameters()
        );
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }

    /**
     * 단순 처리가 필요한 작업 클래스
     */
    public static class TestBean {
        private String value;

        /**
         * value 설정
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * value의 값과 "foo" 값 비교
         */
        public void execute() {
            assertEquals("foo", value);
        }
    }

}
