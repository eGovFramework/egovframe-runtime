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

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import egovframework.brte.sample.common.domain.trade.Trade;

/**
* 서로 다른 유형의 업무 처리 가능을 확인하는 예제를 테스트
* @author 배치실행개발팀
* @since 2012. 06.27
* @version 1.0
* @see
*/

@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/egovframework/batch/simple-job-launcher-context.xml", "/egovframework/batch/jobs/compositeItemWriterSampleJob.xml",
		"/egovframework/batch/job-runner-context.xml" })
public class EgovCompositeItemWriterSampleFunctionalTests {

	//Trade 테이블의 데이트를 가져오는 쿼리
	private static final String GET_TRADES = "SELECT isin, quantity, price, customer FROM TRADE order by isin";

	//파일에 예상되는 결과값
	private static final String EXPECTED_OUTPUT_FILE = "Trade: [isin=UK21341EAH41,quantity=211,price=31.11,customer=customer1]"
			+ "Trade: [isin=UK21341EAH42,quantity=212,price=32.11,customer=customer2]" + "Trade: [isin=UK21341EAH43,quantity=213,price=33.11,customer=customer3]"
			+ "Trade: [isin=UK21341EAH44,quantity=214,price=34.11,customer=customer4]" + "Trade: [isin=UK21341EAH45,quantity=215,price=35.11,customer=customer5]";
	//DB 사용을 위한  SimpleJdbcTemplate
	private JdbcTemplate simpleJdbcTemplate;

	//배치작업을  test하기 위한 JobLauncherTestUtils
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	/**
	 * 배치작업을 할 DB 설정
	 * @param dataSource
	 */
	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new JdbcTemplate(dataSource);
	}

	/** 
	 * 배치작업 테스트
	 * @throws Exception
	 */
	@Test
	public void testJobLaunch() throws Exception {

		simpleJdbcTemplate.update("DELETE from TRADE");
		int before = simpleJdbcTemplate.queryForObject("SELECT COUNT(*) from TRADE", new Object[]{}, Integer.class);

		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		checkOutputFile("target/test-outputs/CustomerReport1.txt");
		checkOutputFile("target/test-outputs/CustomerReport2.txt");
		checkOutputTable(before);

	}

	/**
	 * 배치작업 후 Trade 테이블의 데이터 확인
	 * @param 배치작업 전 Trade의 데이터 갯수
	 */
	@SuppressWarnings("unchecked")
	private void checkOutputTable(int before) {
		final List<Trade> trades = new ArrayList<Trade>() {

			private static final long serialVersionUID = 1L;

			{
				add(new Trade("UK21341EAH41", 211, new BigDecimal("31.11"), "customer1"));
				add(new Trade("UK21341EAH42", 212, new BigDecimal("32.11"), "customer2"));
				add(new Trade("UK21341EAH43", 213, new BigDecimal("33.11"), "customer3"));
				add(new Trade("UK21341EAH44", 214, new BigDecimal("34.11"), "customer4"));
				add(new Trade("UK21341EAH45", 215, new BigDecimal("35.11"), "customer5"));
			}
		};

		int after = simpleJdbcTemplate.queryForObject("SELECT COUNT(*) from TRADE", new Object[]{}, Integer.class);

		assertEquals(before + 5, after);

		
		simpleJdbcTemplate.query(GET_TRADES,
				new Object[] {},
				new RowMapper<Object>() {
					private int activeRow = 0;
					@Override
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Trade trade = trades.get(activeRow++);
						
						assertEquals(trade.getIsin(), rs.getString(1));
						assertEquals(trade.getQuantity(), rs.getLong(2));
						assertEquals(trade.getPrice(), rs.getBigDecimal(3));
						assertEquals(trade.getCustomer(), rs.getString(4));
						return trade;
					}
				}
		);

	}

	/**
	 * 배치작업 후 file의 데이터 확인
	 * @param fileName
	 * @throws IOException
	 */
	private void checkOutputFile(String fileName) throws IOException {
		
		List<String> outputLines = IOUtils.readLines(new FileInputStream(fileName));

		String output = "";
		for (String line : outputLines) {
			output += line;
		}

		assertEquals(EXPECTED_OUTPUT_FILE, output);
	}

}
