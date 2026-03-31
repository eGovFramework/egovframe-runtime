package org.egovframe.rte.bat.sample.example.test;

import org.egovframe.rte.bat.config.jobs.BatchRunnerTestJob;
import org.egovframe.rte.bat.core.launch.support.EgovBatchRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertFalse;

/**
 * EgovBatchRunner클래스를 확인하기 위한 테스트
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012. 07.25  배치실행개발팀     최초 생성
 *  </pre>
 * @since 2012. 07.25
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BatchRunnerTestJob.class)
public class EgovBatchRunnerFunctionalTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovBatchRunnerFunctionalTests.class);

    @Autowired
    private EgovBatchRunner egovBatchRunner;

    /**
     * Batch Job의 시작, 정지, 재시작 기능을 테스트
     */
    @Test
    public void testStartStopResumeJob() throws Exception {
        String jobName = "batchRunnerTestJob";
        Properties jobParameters = egovBatchRunner.createUniqueJobParameters();

        // Batch Job을 시작한다.
        long executionId = egovBatchRunner.start(jobName, jobParameters);

        assertEquals(jobName, egovBatchRunner.getJobInstance(executionId).getJobName());

        // Job Parameters 확인 (Spring Batch 5.x 형식에 맞게 수정)
        String actualParameters = egovBatchRunner.getJobOperator().getParameters(executionId);
        assertTrue(actualParameters.contains("timestamp=" + jobParameters.getProperty("timestamp")));

        // Batch Job을 정지시킨다.
        stopAndCheckStatus(executionId);

        // Batch Job을 재시작한다.
        long resumedExecutionId = egovBatchRunner.restart(executionId);

        // 재시작된 Job의 Parameters 확인
        String resumedParameters = egovBatchRunner.getJobOperator().getParameters(resumedExecutionId);
        assertTrue(resumedParameters.contains("timestamp=" + jobParameters.getProperty("timestamp")));

        // Batch Job을 정지시킨다.
        stopAndCheckStatus(resumedExecutionId);
    }

    /**
     * Batch Job을 정지시키고 BatchStatus 값을 체크한다.
     */
    private void stopAndCheckStatus(long executionId) throws Exception {
        String jobName = egovBatchRunner.getJobInstance(executionId).getJobName();

        // wait to the job to get up and running
        Thread.sleep(500);

        Set<Long> runningExecutions = egovBatchRunner.getJobOperator().getRunningExecutions(jobName);

        assertTrue(runningExecutions.contains(executionId), "Wrong executions: " + runningExecutions + " expected: " + executionId);

        assertTrue(egovBatchRunner.getJobOperator().getSummary(executionId).contains(BatchStatus.STARTED.toString()),
                "Wrong summary: " + egovBatchRunner.getJobOperator().getSummary(executionId));

        egovBatchRunner.getJobOperator().stop(executionId);

        int count = 0;
        while (egovBatchRunner.getJobOperator().getRunningExecutions(jobName).contains(executionId) && count <= 20) {
            LOGGER.debug("Checking for running JobExecution: count=" + count);
            Thread.sleep(50);
            count++;
        }

        runningExecutions = egovBatchRunner.getJobOperator().getRunningExecutions(jobName);
        assertFalse("Wrong executions: " + runningExecutions + " expected: " + executionId, runningExecutions.contains(executionId));
        assertTrue(egovBatchRunner.getJobOperator().getSummary(executionId).contains(BatchStatus.STOPPED.toString()),
                "Wrong summary: " + egovBatchRunner.getJobOperator().getSummary(executionId));

        Map<Long, String> summaries = egovBatchRunner.getJobOperator().getStepExecutionSummaries(executionId);
        System.err.println(summaries);
        assertTrue(summaries.values().toString().contains(BatchStatus.STOPPED.toString()));
    }

}
