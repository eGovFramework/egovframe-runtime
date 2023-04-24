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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;

/**
 * 멀티쓰레드로 실행하는 배치작업 테스트
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
@ContextConfiguration(locations={
		"/org/egovframe/batch/simple-job-launcher-context.xml"
		, "/org/egovframe/batch/jobs/parallelJob.xml"
		, "/org/egovframe/batch/job-runner-context.xml"})
public class EgovParallelJobFunctionalTests {

	//배치작업을  test하기 위한 JobLauncherTestUtils
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	//DB 사용을 위한  SimpleJdbcTemplate
	private JdbcTemplate jdbcTemplate;

	/**
	 * 데이터소스 세팅
	 * @param dataSource
	 */
	@Autowired
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	* 배치작업 테스트 전에 DB관련 작업
	*/
	@Before
	public void setUp() {
		jdbcTemplate.update("DELETE from TRADE");
		jdbcTemplate.update("DELETE from BATCH_STAGING");
	}

	/**
	 * 배치작업 테스트
	 */
	@Test
	public void testLaunchJob() throws Exception {
		int before = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STAGING");
		JobExecution execution = jobLauncherTestUtils.launchJob();
		int after = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STAGING");
		assertEquals(BatchStatus.COMPLETED, execution.getStatus());
		assertEquals(after - before, execution.getStepExecutions().iterator().next().getReadCount());
	}

}
