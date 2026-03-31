package org.egovframe.rte.bat.sample.domain.trade;

import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

/**
 * CustomerCreditIncreaseProcessor
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
public class CustomerCreditIncreaseProcessor implements ItemProcessor<CustomerCredit, CustomerCredit> {

    /**
     * 증가할 수
     */
    public static final BigDecimal FIXED_AMOUNT = new BigDecimal("5");

    /**
     * FIXED_AMOUNT만큼 증가 시킨 후 return
     */
    public CustomerCredit process(CustomerCredit item) {
        return item.increaseCreditBy(FIXED_AMOUNT);
    }

}
