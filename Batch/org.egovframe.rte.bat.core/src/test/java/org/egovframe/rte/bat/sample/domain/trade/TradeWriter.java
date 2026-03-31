package org.egovframe.rte.bat.sample.domain.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.annotation.AfterWrite;
import org.springframework.batch.item.*;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * TradeWriter
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
public class TradeWriter extends ItemStreamSupport implements ItemWriter<Trade> {

    public static final String TOTAL_AMOUNT_KEY = "TOTAL_AMOUNT";
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeWriter.class);
    private TradeDao dao;
    private List<String> failingCustomers = new ArrayList<String>();
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Override
    public void write(Chunk<? extends Trade> chunk) throws Exception {
        List<? extends Trade> trades = chunk.getItems();
        for (Trade trade : trades) {
            LOGGER.debug("trade :: " + trade);
            Assert.notNull(trade.getPrice(), "There must be a price to total");
            if (this.failingCustomers.contains(trade.getCustomer())) {
                throw new WriteFailedException("Something unexpected happened!");
            }
            dao.writeTrade(trade);
        }
    }

    @AfterWrite
    public void updateTotalPrice(Chunk<? extends Trade> chunk) {
        for (Trade trade : chunk.getItems()) {
            this.totalPrice = this.totalPrice.add(trade.getPrice());
        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (executionContext.containsKey(TOTAL_AMOUNT_KEY)) {
            this.totalPrice = (BigDecimal) executionContext.get(TOTAL_AMOUNT_KEY);
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {
        executionContext.put(TOTAL_AMOUNT_KEY, this.totalPrice);
        executionContext.put("stepName", "step1");
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setDao(TradeDao dao) {
        this.dao = dao;
    }

    public void setFailingCustomers(List<String> failingCustomers) {
        this.failingCustomers = failingCustomers;
    }

}
