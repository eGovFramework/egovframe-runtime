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

package egovframework.brte.sample.testcase.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.item.file.FlatFileItemWriter;

import org.springframework.batch.test.AssertFile;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import egovframework.brte.sample.common.domain.trade.CustomerCredit;

/**
 * Job 실패시 restart 되는 기능을 테스트
* @author 배치실행개발팀
* @since 2012. 06.27
* @version 1.0
* @see
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/egovframework/batch/simple-job-launcher-context.xml", "/egovframework/batch/jobs/restartFileSampleJob.xml",
		"/egovframework/batch/job-runner-context.xml" })
public class EgovRestartFileSampleFunctionalTests {

	//배치작업의 outputResource
	@Autowired
	private Resource outputResource;

	//배치작업을  test하기 위한 JobLauncherTestUtils
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	/**
	 * 배치작업 테스트
	 */
	@Test
	public void runTest() throws Exception {
		JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParameters();

		JobExecution je1 = jobLauncherTestUtils.launchJob(jobParameters);
		assertEquals(BatchStatus.FAILED, je1.getStatus());
		AssertFile.assertLineCount(10, outputResource);

		JobExecution je2 = jobLauncherTestUtils.launchJob(jobParameters);
		assertEquals(BatchStatus.COMPLETED, je2.getStatus());
		AssertFile.assertLineCount(20, outputResource);
	}

	/**
	*  restart기능 테스트를 위해 작업중간에 failed 발생시키는 writer 클래스
	* @author 배치실행개발팀
	* @since 2012. 06.27
	* @version 1.0
	* @see
	 */
	public static class CustomerCreditFlatFileItemWriter extends FlatFileItemWriter<CustomerCredit> {

		//failed를 발생시키기 위한 플래그
		private boolean failed = false;

		/**
		 * write 함
		 */
		@Override
		public void write(List<? extends CustomerCredit> arg0) throws Exception {
			for (CustomerCredit cc : arg0) {
				if (!failed && cc.getName().equals("customer13")) {
					failed = true;
					throw new RuntimeException("customer13 is failed");
				}
			}
			super.write(arg0);
		}

	}

}