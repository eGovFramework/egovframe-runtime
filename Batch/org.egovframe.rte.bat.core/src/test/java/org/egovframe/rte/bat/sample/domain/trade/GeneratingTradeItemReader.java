package org.egovframe.rte.bat.sample.domain.trade;

import org.springframework.batch.item.ItemReader;

import java.math.BigDecimal;

/**
 * 원천소스에서 read 하는 것이 아닌, items 를 사용자 설정만큼 생성하는 클래스
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see * <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 07.30  배치실행개발팀    최초 생성
 *
 * </pre>
 * @since 2012. 07.30
 */
public class GeneratingTradeItemReader implements ItemReader<Trade> {

    private int limit = 1;
    private int counter = 0;

    public Trade read() throws Exception {
        if (counter < limit) {
            counter++;
            return new Trade("isin" + counter, counter, new BigDecimal(counter), "customer" + counter);
        }
        return null;
    }

    public int getCounter() {
        return counter;
    }

    public int getLimit() {
        return limit;
    }

    /**
     * @param limit number of items that will be generated
     *              (null returned on consecutive calls).
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void resetCounter() {
        this.counter = 0;
    }

}
