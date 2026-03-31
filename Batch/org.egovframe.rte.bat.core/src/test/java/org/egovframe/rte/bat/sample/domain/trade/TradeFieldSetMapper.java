package org.egovframe.rte.bat.sample.domain.trade;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

/**
 * TradeFieldSetMapper
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
public class TradeFieldSetMapper implements FieldSetMapper<Trade> {

    public static final int ISIN_COLUMN = 0;
    public static final int QUANTITY_COLUMN = 1;
    public static final int PRICE_COLUMN = 2;
    public static final int CUSTOMER_COLUMN = 3;

    public Trade mapFieldSet(FieldSet fieldSet) {
        Trade trade = new Trade();
        trade.setIsin(fieldSet.readString(ISIN_COLUMN));
        trade.setQuantity(fieldSet.readLong(QUANTITY_COLUMN));
        trade.setPrice(fieldSet.readBigDecimal(PRICE_COLUMN));
        trade.setCustomer(fieldSet.readString(CUSTOMER_COLUMN));
        return trade;
    }

}
