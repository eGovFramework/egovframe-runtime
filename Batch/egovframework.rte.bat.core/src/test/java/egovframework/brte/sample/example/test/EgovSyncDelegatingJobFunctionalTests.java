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

package egovframework.brte.sample.example.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import egovframework.brte.sample.common.domain.person.PersonService;

/**
 * JobLauncher 설정을 통한 동기/비동기 처리기능을 테스트  
 * 
 * @author 배치실행개발팀
 * @since 2012. 07.31
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *   
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.07.31  배치실행개발팀     최초 생성
 *  </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/egovframework/batch/sync-job-launcher-context.xml", "/egovframework/batch/jobs/delegatingJob.xml",
		"/egovframework/batch/job-runner-context.xml" })
public class EgovSyncDelegatingJobFunctionalTests {

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
		JobExecution jobExecution = null;
		try {
			jobExecution = jobLauncherTestUtils.launchJob();
			//Async 로 수행되는 경우 Exit Status는 UNKNOWN으로 설정 됨
			assertEquals("UNKNOWN", jobExecution.getExitStatus().getExitCode());
			Thread.sleep(4000);

		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		assertTrue(personService.getReturnedCount() > 0);
		assertEquals(personService.getReturnedCount(), personService.getReceivedCount());
		assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

	}

}
