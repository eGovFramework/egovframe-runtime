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

package egovframework.brte.sample.common.domain.trade;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

/**
 * JdbcTradeDao
 *
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @version 1.0
 * @see <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 07.30  배치실행개발팀    최초 생성
 *
 * </pre>
 */
public class JdbcTradeDao implements TradeDao {
	private Log log = LogFactory.getLog(JdbcTradeDao.class);
	/**
	 * template for inserting a row
	 */
	private static final String INSERT_TRADE_RECORD = "INSERT INTO TRADE (id, version, isin, quantity, price, customer) VALUES (?, 0, ?, ? ,?, ?)";

	/**
	 * handles the processing of sql query
	 */
	private JdbcTemplate jdbcTemplate;

	/**
	 * database is not expected to be setup for autoincrementation
	 */
	private DataFieldMaxValueIncrementer incrementer;

	/**
	 * @see TradeDao
	 */
	@Override
	public void writeTrade(Trade trade) {
		Long id = incrementer.nextLongValue();
		log.info("Processing: " + trade);
		jdbcTemplate.update(INSERT_TRADE_RECORD, id, trade.getIsin(), trade.getQuantity(), trade.getPrice(), trade.getCustomer());

	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void setIncrementer(DataFieldMaxValueIncrementer incrementer) {
		this.incrementer = incrementer;
	}

}
