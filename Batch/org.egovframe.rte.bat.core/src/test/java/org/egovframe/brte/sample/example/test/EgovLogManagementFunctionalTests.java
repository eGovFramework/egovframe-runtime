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

import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * 배치 실행로그 를 확인하기 위한 테스트 (Registry와 DB)
 *
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 07.30  배치실행개발팀    최초 생성
 *
 * </pre>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/org/egovframe/batch/simple-job-launcher-context.xml"
		, "/org/egovframe/batch/jobs/logManagementJob.xml"
		, "/org/egovframe/batch/job-runner-context.xml"})
public class EgovLogManagementFunctionalTests {

	// 배치작업을 test하기 위한 JobLauncherTestUtils
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	// Job 정보가 저장되어 있는 JobRegistry
	@Autowired
	private JobRegistry jobRegistry;

	private JdbcTemplate jdbcTemplate;

	/**
	 * DataSource 세팅
	 *
	 */
	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * 배치작업 테스트 전에 DB관련 작업
	 */
	@Before
	public void setUp() {
		jdbcTemplate.update("DELETE from ERROR_LOG");
	}

	/**
	 * 배치 작업 테스트
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateCredit() throws Exception {
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		// Registry : Job 정보에 대한 로그기록
		assertEquals("[logManagementJob]", jobRegistry.getJobNames().toString());

		// DB : ERROR_LOG 테이블의 로그 수
		assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "ERROR_LOG"));

		// DB : ERROR_LOG 테이블의 로그 데이터
		assertEquals("3 records were skipped!", jdbcTemplate.queryForObject("SELECT MESSAGE from ERROR_LOG where JOB_NAME = ?", String.class, "logManagementJob"));
	}

}
