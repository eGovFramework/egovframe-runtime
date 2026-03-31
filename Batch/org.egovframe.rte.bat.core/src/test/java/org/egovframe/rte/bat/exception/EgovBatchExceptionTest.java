package org.egovframe.rte.bat.exception;

import org.egovframe.rte.bat.config.jobs.BatchExceptionTestJob;
import org.egovframe.rte.bat.core.launch.support.EgovBatchRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovBatchException JUnit Test 클래스
 *
 * @author 장동한
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2017.12.01  장동한           최초 생성
 * </pre>
 * @since 2017.12.01
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BatchExceptionTestJob.class)
public class EgovBatchExceptionTest {

    @Autowired
    private EgovBatchRunner egovBatchRunner;

    @Test
    public void testJobRun() throws Exception {

        String jobName = "delimitedToDelimitedJob";

        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("inputFile", "META-INF/spring/delimited.csv");
        jobParametersBuilder.addLong("timestamp", new Date().getTime());

        Properties jobParameters = egovBatchRunner.convertJobParametersToString(jobParametersBuilder.toJobParameters());

        long executionId = egovBatchRunner.start(jobName, jobParameters);

        //Job Name 확인
        assertEquals(jobName, egovBatchRunner.getJobInstance(executionId).getJobName());

        //Job Parameters 확인
        String actualParameters = egovBatchRunner.getJobOperator().getParameters(executionId);
        assertTrue(actualParameters.contains("inputFile=" + jobParameters.getProperty("inputFile")));
        assertTrue(actualParameters.contains("timestamp=" + jobParameters.getProperty("timestamp")));

        //Job 실행 결과 확인
        assertEquals(BatchStatus.FAILED, egovBatchRunner.getJobExecution(executionId).getStatus());

    }

}
