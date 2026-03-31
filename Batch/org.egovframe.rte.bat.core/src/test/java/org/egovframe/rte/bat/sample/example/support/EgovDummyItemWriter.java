package org.egovframe.rte.bat.sample.example.support;

import org.springframework.batch.item.ItemWriter;

import java.util.List;

/**
 * 실제 Write 하지 않고, 정해진 시간만큼 대기하는 클래스
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012. 07.25  배치실행개발팀     최초 생성
 * </pre>
 * @since 2012. 07.25
 */
public abstract class EgovDummyItemWriter implements ItemWriter<Object> {

    /**
     * 바로 Wirte 하지 않고 대기 함
     */
    public void write(List<? extends Object> item) throws Exception {
        // NO-OP
        Thread.sleep(500);
    }

}
