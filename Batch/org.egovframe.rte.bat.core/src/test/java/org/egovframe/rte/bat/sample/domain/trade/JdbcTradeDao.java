package org.egovframe.rte.bat.sample.domain.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

import javax.sql.DataSource;

/**
 * JdbcTradeDao
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 07.30  배치실행개발팀    최초 생성
 *
 * </pre>
 * @since 2012. 07.30
 */
public class JdbcTradeDao implements TradeDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTradeDao.class);

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
        LOGGER.debug("### JdbcTradeDao writeTrade() Processing : {}", trade);
        jdbcTemplate.update(INSERT_TRADE_RECORD, id, trade.getIsin(), trade.getQuantity(), trade.getPrice(), trade.getCustomer());

    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setIncrementer(DataFieldMaxValueIncrementer incrementer) {
        this.incrementer = incrementer;
    }

}
