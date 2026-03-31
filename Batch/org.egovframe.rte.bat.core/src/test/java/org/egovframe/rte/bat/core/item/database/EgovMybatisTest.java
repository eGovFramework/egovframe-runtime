package org.egovframe.rte.bat.core.item.database;

import org.egovframe.rte.bat.config.jobs.MybatisJob;
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
 * EgovMybatisTest JUnit Test 클래스
 *
 * @author 장동한
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2018.01.15  장동한           최초 생성
 * </pre>
 * @since 2018.01.15
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MybatisJob.class)
public class EgovMybatisTest {

    @Autowired
    private EgovBatchRunner egovBatchRunner;

    @Test
    public void testJobRun() throws Exception {

        String jobName = "mybatisJob";

        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("inputFile", "org/egovframe/data/input/delimited.csv");
        jobParametersBuilder.addLong("timestamp", new Date().getTime());

        Properties jobParameters = egovBatchRunner.convertJobParametersToString(jobParametersBuilder.toJobParameters());

        long executionId = egovBatchRunner.start(jobName, jobParameters);

        //Job Name 확인
        assertEquals(jobName, egovBatchRunner.getJobInstance(executionId).getJobName());

        //Job Parameters 확인
        String actualParameters = egovBatchRunner.getJobOperator().getParameters(executionId);
        assertTrue(actualParameters.contains("inputFile=org/egovframe/data/input/delimited.csv"));
        assertTrue(actualParameters.contains("timestamp="));

        //Job 실행 결과 확인
        assertEquals(BatchStatus.COMPLETED, egovBatchRunner.getJobExecution(executionId).getStatus());

    }

}
