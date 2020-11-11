/*
 * Copyright 2006-2007 the original author or authors.
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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import egovframework.brte.sample.common.domain.trade.CustomerCredit;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * DB를 gridSize 만큼 나누어 파티셔닝으로 처리하는 내용을 테스트
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @version 1.0
 * @see
 *
 * <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 07.30  배치실행개발팀    최초 생성
 *
 * </pre>
*/

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/egovframework/batch/simple-job-launcher-context.xml", "/egovframework/batch/jobs/partitionJdbcJob.xml",
		"/egovframework/batch/job-runner-context.xml" })
public class EgovPartitionJdbcFunctionalTests implements ApplicationContextAware {

	//ItemReader
	@Autowired
	@Qualifier("inputTestReader")
	private ItemReader<CustomerCredit> inputReader;

	//배치작업을  test하기 위한 JobLauncherTestUtils
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	//ApplicationContext
	private ApplicationContext applicationContext;

	//ApplicationContext 설정
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

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

		assertTrue("Define a prototype bean called 'outputTestReader' to check the output", applicationContext.containsBeanDefinition("outputTestReader"));

		open(inputReader);
		List<CustomerCredit> inputs = new ArrayList<CustomerCredit>(getCredits(inputReader));
		close(inputReader);

		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

		@SuppressWarnings("unchecked")
		ItemReader<CustomerCredit> outputReader = (ItemReader<CustomerCredit>) applicationContext.getBean("outputTestReader");
		open(outputReader);
		List<CustomerCredit> outputs = new ArrayList<CustomerCredit>(getCredits(outputReader));
		close(outputReader);

		assertEquals(inputs.size(), outputs.size());
		int itemCount = inputs.size();
		assertTrue(itemCount > 0);

		inputs.iterator();
		for (int i = 0; i < itemCount; i++) {
			assertEquals(inputs.get(i).getCredit().intValue(), outputs.get(i).getCredit().intValue());
		}
	}

	/**
	  * 배치작업의 결과값을 set로 만드는 메소드
	  * @param reader
	  * @return Set<CustomerCredit>
	  * @throws Exception
	  */
	private Set<CustomerCredit> getCredits(ItemReader<CustomerCredit> reader) throws Exception {
		CustomerCredit credit;
		Set<CustomerCredit> result = new LinkedHashSet<CustomerCredit>();
		while ((credit = reader.read()) != null) {
			result.add(credit);
		}
		return result;

	}

	/**
	 * reader를 open하는 메소드
	 * @param reader
	 */
	private void open(ItemReader<?> reader) {
		if (reader instanceof ItemStream) {
			((ItemStream) reader).open(new ExecutionContext());
		}
	}

	/**
	 * reader를 close하는 메소드
	 * @param reader
	 */
	private void close(ItemReader<?> reader) {
		if (reader instanceof ItemStream) {
			((ItemStream) reader).close();
		}
	}

}
