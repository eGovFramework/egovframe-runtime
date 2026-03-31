package org.egovframe.rte.bat.sample.example.listener;

import org.egovframe.rte.bat.core.listener.EgovStepPreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;

/**
 * 스텝단계 이전에 호출되는 리스너 클래스
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 * 	개정이력(Modification Information)
 *
 * 	수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.06.27  배치실행개발팀     최초 생성
 * </pre>
 * @since 2012.06.27
 */
public class EgovSampleStepPreProcessor<T, S> extends EgovStepPreProcessor<T, S> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovSampleStepPreProcessor.class);

    /**
     * Step 수행 이전에 호출되는 부분
     */
    public void beforeStep(StepExecution stepExecution) {
        LOGGER.debug("### EgovSampleStepPreProcessor beforeStep() Start " + stepExecution.getStepName());
    }

}
