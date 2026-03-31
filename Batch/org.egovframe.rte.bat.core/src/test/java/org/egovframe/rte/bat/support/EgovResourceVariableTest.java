package org.egovframe.rte.bat.support;

import org.egovframe.rte.bat.config.jobs.DelimitedToDelimitedJobResourceVariable;
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
 * EgovResourceVariable JUnit Test 클래스
 *
 * @author 장동한
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2017.12.08  장동한           최초 생성
 * </pre>
 * @since 2017.12.08
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DelimitedToDelimitedJobResourceVariable.class)
public class EgovResourceVariableTest {

    @Autowired
    private EgovBatchRunner egovBatchRunner;

    @Autowired
    private EgovResourceVariable egovResourceVariable;

    @Test
    public void testJobRun() throws Exception {

        String jobName = "delimitedToDelimitedJob-ResourceVariable";

        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addLong("timestamp", new Date().getTime());

        Properties jobParameters = egovBatchRunner.convertJobParametersToString(jobParametersBuilder.toJobParameters());

        long executionId = egovBatchRunner.start(jobName, jobParameters);

        //Job Name 확인
        assertEquals(jobName, egovBatchRunner.getJobInstance(executionId).getJobName());

        //Job Parameters 확인
        String actualParameters = egovBatchRunner.getJobOperator().getParameters(executionId);
        assertTrue(actualParameters.contains("timestamp=" + jobParameters.getProperty("timestamp")));

        //Resource Variable 확인
        assertEquals("META-INF/spring/delimited.csv", egovResourceVariable.getVariable("input.resource"));

        //Job 실행 결과 확인
        assertEquals(BatchStatus.COMPLETED, egovBatchRunner.getJobExecution(executionId).getStatus());

    }

}
