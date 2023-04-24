package org.egovframe.rte.bat.core.item.database;

import org.egovframe.rte.bat.core.launch.support.EgovBatchRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * EgovMybatisTest JUnit Test 클래스
 *
 * @author 장동한
 * @since 2018.01.15
 * @version 1.0
 * @see
 *
 * <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2018.01.15  장동한           최초 생성
 * </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/egovframe/batch/batch-runner-context.xml", 
									"/org/egovframe/batch/context-mybatis.xml", 
									"/org/egovframe/batch/jobs/mybatisJob.xml" })
public class EgovMybatisTest {

	@Autowired
	private EgovBatchRunner egovBatchRunner;
	
	@Test
	public void testJobRun() throws Exception {
		
		String jobName = "mybatisJob";

		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
		jobParametersBuilder.addString("inputFile", "org/egovframe/data/input/delimited.csv");
		jobParametersBuilder.addLong("timestamp", new Date().getTime());
		
		String jobParameters = egovBatchRunner.convertJobParametersToString(jobParametersBuilder.toJobParameters());
		
		long executionId = egovBatchRunner.start(jobName, jobParameters);
		
		//Job Name 확인
		assertEquals(jobName, egovBatchRunner.getJobInstance(executionId).getJobName());

		//Job Parameters 확인
		assertEquals(jobParameters.toString().replaceAll(" ", ""), egovBatchRunner.getJobOperator().getParameters(executionId).toString());

		//Job 실행 결과 확인
		assertEquals(BatchStatus.COMPLETED, egovBatchRunner.getJobExecution(executionId).getStatus());

	}

}
