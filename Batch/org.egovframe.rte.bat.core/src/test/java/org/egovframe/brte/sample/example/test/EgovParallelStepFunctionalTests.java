/*
 * Copyright 2006-2007 the original author or authors.
 *
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * parallelStep으로 배치작업을 실행하는 테스트
 * 
 * @author 배치실행개발팀
 * @since 2012. 07.31
 * @version 1.0
 * @see <pre>
 * 
 * 개정이력(Modification Information)
 *   
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.07.31  배치실행개발팀     최초 생성
 *  </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/org/egovframe/batch/simple-job-launcher-context.xml"
		, "/org/egovframe/batch/jobs/parallelStep.xml"
		, "/org/egovframe/batch/job-runner-context.xml"})
public class EgovParallelStepFunctionalTests {

	//배치작업을  test하기 위한 JobLauncherTestUtils
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	/**
	 * 배치작업 테스트
	 */
	@Test
	public void testLaunchJob() throws Exception {
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(this.getUniqueJobParameters());
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		// /target/test-outputs/parallelStep폴더의 output파일 확인
	}

	/**
	 * 잡파라미터를 설정하기 위한 메소드 
	 * @return jobParameters
	 */
	protected JobParameters getUniqueJobParameters() {
		return new JobParametersBuilder().addString("inputFile", "/org/egovframe/data/input/delimited.csv")
				.addString("outputFile1", "file:./target/test-outputs/parallelStep/delimitedOutput1.csv")
				.addString("outputFile2", "file:./target/test-outputs/parallelStep/delimitedOutput2.csv")
				.addString("outputFile3", "file:./target/test-outputs/parallelStep/delimitedOutput3.csv")
				.addString("outputFile4", "file:./target/test-outputs/parallelStep/delimitedOutput4.csv")
				.addParameter("timestamp", new JobParameter(new Date().getTime()))
				.toJobParameters();
	}

}
