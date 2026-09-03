package org.egovframe.rte.fdl.reactive.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EgovMdcContextConfigTest {

    private final EgovMdcContextConfig config = new EgovMdcContextConfig();

    /** 시퀀스의 상류 절반이 도는, 재사용되는 스레드 */
    private Scheduler worker;

    /** publishOn 으로 하류 절반을 넘겨받는 스레드 */
    private Scheduler consumer;

    @BeforeEach
    public void setUp() {
        worker = Schedulers.newSingle("mdc-worker");
        consumer = Schedulers.newSingle("mdc-consumer");
        config.contextOperatorHook();
        MDC.clear();
    }

    @AfterEach
    public void tearDown() {
        config.cleanupHook();
        worker.dispose();
        consumer.dispose();
        MDC.clear();
    }

    /**
     * userId=A 를 worker 스레드의 MDC 에 남긴 뒤, 같은 worker 스레드에서 값 없이 완료되는
     * 시퀀스를 구독한다. 두 번째 시퀀스의 종료 콜백은 자신의 Context 값인 B 를 봐야 한다.
     */
    @Test
    public void completionOfNextSequenceSeesItsOwnContext() {
        Mono.just("a").hide()
                .subscribeOn(worker)
                .publishOn(consumer)
                .contextWrite(context -> context.put("userId", "A"))
                .block();

        AtomicReference<String> seen = new AtomicReference<>();
        Mono.empty()
                .subscribeOn(worker)
                .doOnTerminate(() -> seen.set(MDC.get("userId")))
                .contextWrite(context -> context.put("userId", "B"))
                .block();

        assertEquals("B", seen.get());
    }

    /**
     * 두 번째 시퀀스가 값 없이 에러로 끝나는 경우도 마찬가지다.
     */
    @Test
    public void errorOfNextSequenceSeesItsOwnContext() {
        Mono.just("a").hide()
                .subscribeOn(worker)
                .publishOn(consumer)
                .contextWrite(context -> context.put("userId", "A"))
                .block();

        AtomicReference<String> seen = new AtomicReference<>();
        Mono<Object> error = Mono.error(new IllegalStateException("boom")).hide()
                .subscribeOn(worker)
                .doOnTerminate(() -> seen.set(MDC.get("userId")))
                .contextWrite(context -> context.put("userId", "B"));
        assertThrows(IllegalStateException.class, error::block);

        assertEquals("B", seen.get());
    }

}
