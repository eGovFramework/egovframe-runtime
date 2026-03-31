package org.egovframe.rte.bat.core.step;

import org.egovframe.rte.bat.config.jobs.DelimitedToDelimitedJobDeleteStep;
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

/**
 * EgovBatchException JUnit Test 클래스
 *
 * @author 신용호
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2017.11.29  신용호           최초 생성
 * </pre>
 * @since 2017.11.29
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DelimitedToDelimitedJobDeleteStep.class)
public class EgovDeleteStepTest {

    @Autowired
    private EgovBatchRunner egovBatchRunner;

    @Test
    public void testJobRun() throws Exception {

        String jobName = "delimitedToDelimitedJob-DeleteStep";

        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("inputFile", "META-INF/spring/delimited.csv");
        jobParametersBuilder.addLong("timestamp", new Date().getTime());

        Properties jobParameters = egovBatchRunner.convertJobParametersToString(jobParametersBuilder.toJobParameters());

        long executionId = egovBatchRunner.start(jobName, jobParameters);

        //Job Name 확인
        assertEquals(jobName, egovBatchRunner.getJobInstance(executionId).getJobName());

        //Job Parameters 확인
        //assertEquals(jobParameters.toString().replaceAll(" ", ""), egovBatchRunner.getJobOperator().getParameters(executionId).toString());

        //Job 실행 결과 확인
        assertEquals(BatchStatus.COMPLETED, egovBatchRunner.getJobExecution(executionId).getStatus());

    }

}
