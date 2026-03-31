package org.egovframe.rte.bat.sample.example.listener;

import org.egovframe.rte.bat.core.listener.EgovChunkPreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 청크단계 이전에 호출되는 리스너 클래스
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
public class EgovSampleChunkPreProcessor extends EgovChunkPreProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovSampleChunkPreProcessor.class);

    /**
     * chunk 수행 이전에 호출되는 부분
     */
    public void beforeChunk() {
        LOGGER.debug("### EgovSampleChunkPreProcessor beforeChunk()...");
    }

}
