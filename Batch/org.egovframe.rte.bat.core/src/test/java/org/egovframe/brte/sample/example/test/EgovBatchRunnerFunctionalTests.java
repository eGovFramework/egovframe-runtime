/*
 * Copyright 2006-2007 the original author or authors. *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.brte.sample.example.test;

import org.egovframe.rte.bat.core.launch.support.EgovBatchRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * EgovBatchRunner클래스를 확인하기 위한 테스트
 * 
 * @author 배치실행개발팀
 * @since 2012. 07.25
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *   
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012. 07.25  배치실행개발팀     최초 생성
 *  </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/org/egovframe/batch/batch-runner-context.xml"
		, "/org/egovframe/batch/jobs/batchRunnerTestJob.xml"})
public class EgovBatchRunnerFunctionalTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovBatchRunnerFunctionalTests.class);

	//EgovBatchRunner클래스 
	@Autowired
	private EgovBatchRunner egovBatchRunner;

	/**
	 * Batch Job의 시작, 정지, 재시작 기능을 테스트
	 */
	@Test
	public void testStartStopResumeJob() throws Exception {

		String jobName = "batchRunnerTestJob";
		String jobParameters = egovBatchRunner.createUniqueJobParameters();

		// Batch Job을 시작한다.
		long executionId = egovBatchRunner.start(jobName, jobParameters);

		assertEquals(jobName, egovBatchRunner.getJobInstance(executionId).getJobName());
		assertEquals(jobParameters.toString(), egovBatchRunner.getJobOperator().getParameters(executionId));

		// Batch Job을 정지시킨다.
		stopAndCheckStatus(executionId);

		// Batch Job을 재시작한다.
		long resumedExecutionId = egovBatchRunner.restart(executionId);
		assertEquals(jobParameters.toString(), egovBatchRunner.getJobOperator().getParameters(resumedExecutionId));

		// Batch Job을 정지시킨다. 
		stopAndCheckStatus(resumedExecutionId);

	}

	/**
	 * Batch Job을 정지시키고 BatchStatus 값을 체크한다.
	 * 
	 * @param executionId : JobExecutionID
	 */
	private void stopAndCheckStatus(long executionId) throws Exception {

		String jobName = egovBatchRunner.getJobInstance(executionId).getJobName();

		// wait to the job to get up and running	
		Thread.sleep(1000);

		Set<Long> runningExecutions = egovBatchRunner.getJobOperator().getRunningExecutions(jobName);
		assertTrue("Wrong executions: " + runningExecutions + " expected: " + executionId, runningExecutions.contains(executionId));
		assertTrue("Wrong summary: " + egovBatchRunner.getJobOperator().getSummary(executionId),
				egovBatchRunner.getJobOperator().getSummary(executionId).contains(BatchStatus.STARTED.toString()));

		egovBatchRunner.getJobOperator().stop(executionId);

		int count = 0;
		while (egovBatchRunner.getJobOperator().getRunningExecutions(jobName).contains(executionId) && count <= 10) {
			LOGGER.info("Checking for running JobExecution: count=" + count);
			Thread.sleep(100);
			count++;
		}

		runningExecutions = egovBatchRunner.getJobOperator().getRunningExecutions(jobName);
		assertFalse("Wrong executions: " + runningExecutions + " expected: " + executionId, runningExecutions.contains(executionId));
		assertTrue("Wrong summary: " + egovBatchRunner.getJobOperator().getSummary(executionId),
				egovBatchRunner.getJobOperator().getSummary(executionId).contains(BatchStatus.STOPPED.toString()));

		// there is just a single step in the test job
		Map<Long, String> summaries = egovBatchRunner.getJobOperator().getStepExecutionSummaries(executionId);
		System.err.println(summaries);
		assertTrue(summaries.values().toString().contains(BatchStatus.STOPPED.toString()));
	}

}
