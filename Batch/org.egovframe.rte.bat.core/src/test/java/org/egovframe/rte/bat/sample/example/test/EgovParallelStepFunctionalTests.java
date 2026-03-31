package org.egovframe.rte.bat.sample.example.test;

import org.egovframe.rte.bat.config.jobs.ParallelStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * parallelStep으로 배치작업을 실행하는 테스트
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
 *  </pre>
 * @since 2012. 07.31
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ParallelStep.class})
public class EgovParallelStepFunctionalTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    /**
     * 배치작업 테스트
     */
    @Test
    public void testLaunchJob() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(this.getUniqueJobParameters());
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }

    /**
     * 잡파라미터를 설정하기 위한 메소드
     */
    protected JobParameters getUniqueJobParameters() {
        return new JobParametersBuilder().addString("inputFile", "META-INF/spring/delimited.csv")
                .addString("outputFile1", "./target/test-outputs/parallelStep/delimitedOutput1.csv")
                .addString("outputFile2", "./target/test-outputs/parallelStep/delimitedOutput2.csv")
                .addString("outputFile3", "./target/test-outputs/parallelStep/delimitedOutput3.csv")
                .addString("outputFile4", "./target/test-outputs/parallelStep/delimitedOutput4.csv")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }

}
