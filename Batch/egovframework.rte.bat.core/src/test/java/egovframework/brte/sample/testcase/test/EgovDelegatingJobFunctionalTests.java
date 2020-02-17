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
import static org.junit.Assert.assertTrue;
import egovframework.brte.sample.common.domain.person.PersonService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
*  기존에 작성된 클래스를 ItemReader나 ItemWriter의 역할로 배치작업을 수행하는 테스트
*
* @author 배치실행개발팀
* @since 2012. 06.27
* @version 1.0
* @see
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/egovframework/batch/simple-job-launcher-context.xml", "/egovframework/batch/jobs/delegatingJob.xml",
		"/egovframework/batch/job-runner-context.xml" })
public class EgovDelegatingJobFunctionalTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovDelegatingJobFunctionalTests.class);

	//배치작업을  test하기 위한 JobLauncherTestUtils
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	//배치작업에 사용된 기존 업무 클래스
	@Autowired
	private PersonService personService;

	/**
	 * 배치작업 테스트
	 */
	@Test
	public void testLaunchJob() throws Exception {

		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		assertTrue(personService.getReturnedCount() > 0);
		assertEquals(personService.getReturnedCount(), personService.getReceivedCount());
		LOGGER.info(">>>>>>>>> readCount = {}", personService.getReturnedCount());
		LOGGER.info(">>>>>>>>> writeCount = {}", personService.getReceivedCount());
	}

}
