package org.egovframe.rte.bat.sample.example.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

/**
 * 재시도가 일어나는지 확인하기 위해, 특정 구간동안 에러를 생성하는 Writer 클래스
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
public abstract class EgovRetrySampleItemWriter<T> implements ItemWriter<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovRetrySampleItemWriter.class);

    //write한 데이터라인수
    private int counter = 0;

    /**
     * write하다가 retry 함
     */
    public void write(List<? extends T> items) throws Exception {

        LOGGER.debug("Writing... " + items);

        int current = counter;
        counter += items.size();
        if (current < 3 && (counter >= 2 || counter >= 3)) {
            /*
             * 자료가 총 3라인이 쓰여지기전에 (current 0~2 까지) counter가 2,3 일 경우 (2번)
             * RuntimeException 을 내어 retry 하도록 만듦
             * retry Limit 이 3 이므로, 총 2번의 재시도를 넘어감 (최초 try 1번 포함)
             * retry Limit 이 2 라면, counter 가 3일때  RuntimeException 에러 남
             */
            throw new RuntimeException("Temporary error");
        }
    }

    /**
     * write 한  데이터라인 수를 가져온다.
     *
     * @return
     */
    public int getCounter() {
        return counter;
    }

}
