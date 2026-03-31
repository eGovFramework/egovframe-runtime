package org.egovframe.rte.bat.sample.example.listener;

import org.egovframe.rte.bat.core.listener.EgovChunkPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 청크단계 이후에 호출되는 프로세서 클래스
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.06.27  배치실행개발팀     최초 생성
 * </pre>
 * @since 2012.06.27
 */

public class EgovSampleChunkPostProcessor extends EgovChunkPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovSampleChunkPostProcessor.class);

    /**
     * chunk 단계 수행 이후에 호출되는 부분
     */
    public void afterChunk() {
        LOGGER.debug("### EgovSampleChunkPostProcessor afterChunk()...");
    }

}
