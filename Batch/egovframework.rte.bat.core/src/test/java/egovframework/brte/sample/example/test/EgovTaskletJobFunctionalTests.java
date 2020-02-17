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

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Job의 실행중 배치작업 외 단순 처리가 필요한 작업의 처리 기능을 테스트 
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
@ContextConfiguration(locations = { "/egovframework/batch/simple-job-launcher-context.xml", "/egovframework/batch/jobs/taskletJob.xml",
		"/egovframework/batch/job-runner-context.xml" })
public class EgovTaskletJobFunctionalTests {

	//배치작업을  test하기 위한 JobLauncherTestUtils
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	/**
	 * 배치작업 테스트
	 */
	@Test
	public void testLaunchJob() throws Exception {
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(new JobParametersBuilder().addString("value", "foo")
				.addParameter("timestamp", new JobParameter(new Date().getTime())).toJobParameters());
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}

	/**
	 *  단순 처리가 필요한 작업 클래스
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
	public static class TestBean {
		//String value
		private String value;

		/**
		 * value 설정
		 */
		public void setValue(String value) {
			this.value = value;
		}

		/**
		 * value의 값과 "foo" 값 비교
		 */
		public void execute() {
			assertEquals("foo", value);
		}
	}

}
