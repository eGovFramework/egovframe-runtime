package org.egovframe.rte.bat.core.listener;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovChunkPreProcessor / EgovChunkPostProcessor 의 확장점이
 * 프레임워크 호출 경로에서 실제로 실행되는지 확인하는 테스트.
 *
 * <p>회귀 : 인자 없는 beforeChunk() / afterChunk() 는 ChunkListener 시그니처(ChunkContext)를 재정의하지 못해
 * 프레임워크가 호출하지 않음 (EgovJobPreProcessor·EgovStepPreProcessor 는 프레임워크 시그니처 사용으로 해당 없음).</p>
 *
 * @author 기여자
 * @version 1.0
 * @since 2026.08.19
 */
@SuppressWarnings("deprecation")
public class EgovChunkProcessorTest {

    /**
     * EgovChunkPreProcessor 를 상속해 재정의한 확장점이
     * ChunkListener#beforeChunk(ChunkContext) 호출로 실행되는지 확인
     */
    @Test
    public void testBeforeChunkIsInvokedThroughChunkListener() {
        final AtomicBoolean called = new AtomicBoolean(false);

        ChunkListener listener = new EgovChunkPreProcessor() {
            @Override
            public void beforeChunk() {
                called.set(true);
            }
        };

        // 프레임워크(TaskletStep)와 같은 경로로 호출
        listener.beforeChunk(new ChunkContext(null));

        assertTrue(called.get(), "EgovChunkPreProcessor 의 beforeChunk 확장점이 호출되지 않았다.");
    }

    /**
     * EgovChunkPostProcessor 를 상속해 재정의한 확장점이
     * ChunkListener#afterChunk(ChunkContext) 호출로 실행되는지 확인
     */
    @Test
    public void testAfterChunkIsInvokedThroughChunkListener() {
        final AtomicBoolean called = new AtomicBoolean(false);

        ChunkListener listener = new EgovChunkPostProcessor() {
            @Override
            public void afterChunk() {
                called.set(true);
            }
        };

        listener.afterChunk(new ChunkContext(null));

        assertTrue(called.get(), "EgovChunkPostProcessor 의 afterChunk 확장점이 호출되지 않았다.");
    }

}
