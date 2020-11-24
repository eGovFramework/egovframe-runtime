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

import java.util.Date;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 여러 방법으로 repository(대용량 데이터)를 설정하여 배치작업을 실행하는 테스트
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
 * </pre>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/org/egovframe/batch/simple-job-launcher-context.xml"
		, "/org/egovframe/batch/jobs/repositoryJob.xml"})
public class EgovRepositoryFunctionalTests {

	// 배치작업 실행하기 위한 jobLauncher
	@Autowired
	private SimpleJobLauncher jobLauncher;

	// MapJobRegistry
	@Autowired
	private MapJobRegistry jobRegistry;

	private JdbcTemplate jdbcTemplate;

	/**
	 * datasource 설정
	 *
	 * @param dataSource
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
		jdbcTemplate.update("DELETE from CUSTOMER");

		jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES  (1, 0, 'customer1', 100000)");
		jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES  (2, 0, 'customer2', 100000)");
		jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES  (3, 0, 'customer3', 100000)");
		jdbcTemplate.update("INSERT INTO CUSTOMER (ID, VERSION, NAME, CREDIT) VALUES  (4, 0, 'customer4', 100000)");

	}

	/**
	 * 배치작업 테스트
	 */
	@Test
	public void testUpdateCredit() throws Exception {

		// 1. resource가 DB인 Job 실행
		JobExecution jobExecution = jobLauncher.run(jobRegistry.getJob("jdbcCursorIoJob"), getUniqueJobParameters("jdbcCursorIoJob"));
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

		// 2. resource가 file 인 Job 실행
		jobExecution = jobLauncher.run(jobRegistry.getJob("delimitedIoJob"), getUniqueJobParameters("delimitedIoJob"));
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

	}

	/**
	 * 잡파라미터를 설정하기 위한 메소드
	 *
	 * @return jobParameters
	 */
	protected JobParameters getUniqueJobParameters(String jobName) {

		JobParametersBuilder builder = new JobParametersBuilder();
		// Job 파라미터로 등록 가능
		builder.addString("inputFile", "/org/egovframe/data/input/delimited.csv");
		builder.addParameter("timestamp", new JobParameter(new Date().getTime()));

		JobParameters jobParameters = builder.toJobParameters();
		return jobParameters;
	}

}
