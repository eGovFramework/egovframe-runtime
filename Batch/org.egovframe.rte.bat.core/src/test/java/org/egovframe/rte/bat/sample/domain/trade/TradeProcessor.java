package org.egovframe.rte.bat.sample.domain.trade;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.validator.ValidationException;

/**
 * TradeProcessor
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
public class TradeProcessor implements ItemProcessor<Trade, Trade> {

    private int failure = -1;
    private int index = 0;
    private Trade failedItem = null;

    /**
     * Public setter for the the index on which failure should occur.
     *
     * @param failure the failure to set
     */
    public void setValidationFailure(int failure) {
        this.failure = failure;
    }

    public Trade process(Trade item) throws Exception {
        if ((failedItem == null && index++ == failure) || (failedItem != null && failedItem.equals(item))) {
            failedItem = item;
            throw new ValidationException("Some bad data for " + failedItem);
        }
        return item;
    }

}
