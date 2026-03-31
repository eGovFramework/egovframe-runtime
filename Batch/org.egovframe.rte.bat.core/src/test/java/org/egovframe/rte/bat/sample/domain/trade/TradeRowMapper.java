package org.egovframe.rte.bat.sample.domain.trade;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TradeRowMapper
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012. 07.25  배치실행개발팀     최초 생성
 *  </pre>
 * @since 2012. 07.25
 */
public class TradeRowMapper implements RowMapper<Object> {

    public static final int ISIN_COLUMN = 1;
    public static final int QUANTITY_COLUMN = 2;
    public static final int PRICE_COLUMN = 3;
    public static final int CUSTOMER_COLUMN = 4;
    public static final int ID_COLUMN = 5;
    public static final int VERSION_COLUMN = 6;

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        Trade trade = new Trade(rs.getLong(ID_COLUMN));
        trade.setIsin(rs.getString(ISIN_COLUMN));
        trade.setQuantity(rs.getLong(QUANTITY_COLUMN));
        trade.setPrice(rs.getBigDecimal(PRICE_COLUMN));
        trade.setCustomer(rs.getString(CUSTOMER_COLUMN));
        trade.setVersion(rs.getInt(VERSION_COLUMN));
        return trade;
    }

}
