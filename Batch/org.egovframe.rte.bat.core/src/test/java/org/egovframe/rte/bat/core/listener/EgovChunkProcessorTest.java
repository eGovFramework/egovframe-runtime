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
 * <p>두 클래스는 확장점을 인자 없는 beforeChunk() / afterChunk() 로 선언해 두었는데,
 * ChunkListener 가 호출하는 시그니처는 ChunkContext 를 받는 형태다. 그래서 인자 없는
 * 메소드는 상위 클래스를 재정의하지 못하는 별개 메소드가 되어 프레임워크가 영영
 * 호출하지 않았다. 형제인 EgovJobPreProcessor(beforeJob(JobExecution)) 나
 * EgovStepPreProcessor(beforeStep(StepExecution)) 는 프레임워크 시그니처를 그대로
 * 쓰고 있어 같은 문제가 없다.</p>
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

        // 프레임워크(TaskletStep)가 호출하는 것과 같은 경로로 호출한다.
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
