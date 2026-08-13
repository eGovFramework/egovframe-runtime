package org.egovframe.rte.itl.integration.support;

import org.egovframe.rte.itl.integration.EgovIntegrationMessage;
import org.egovframe.rte.itl.integration.EgovIntegrationMessageHeader.ResultCode;
import org.egovframe.rte.itl.integration.message.simple.SimpleMessage;
import org.egovframe.rte.itl.integration.message.simple.SimpleMessageHeader;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 같은 연계 서비스 인스턴스에 대해 동시에 두 건의 sendSync 호출이 들어왔을 때,
 * 락을 기다리다 타임아웃한 두 번째 호출이 이후에도 doSend를 시도하지 않음을 확인한다.
 * (수정 전에는 이 두 번째 호출의 스레드가 interrupt를 무시하고 계속 락을 기다리다,
 * 첫 번째 호출이 끝난 뒤 아무도 기다리지 않는 상태에서 뒤늦게 doSend를 실행했다.)
 */
public class MessageSenderLockContentionTest {

    private static final long SLOW_SERVICE_TIME = 3000L;
    private static final long SHORT_TIMEOUT = 300L;

    @Test
    public void secondCallerNeverSendsAfterTimingOutOnTheLock() throws Exception {
        CountDownLatch firstCallEnteredCriticalSection = new CountDownLatch(1);
        AtomicBoolean secondActuallyRan = new AtomicBoolean(false);

        RecordingService service = new RecordingService("lockContentionTest", 10_000L,
                firstCallEnteredCriticalSection, secondActuallyRan);

        EgovIntegrationMessage firstRequest = service.createRequestMessage();
        EgovIntegrationMessage secondRequest = service.createRequestMessage();

        Thread firstCaller = new Thread(() -> service.sendSync(firstRequest, 10_000L));
        firstCaller.start();
        assertTrue(firstCallEnteredCriticalSection.await(2, TimeUnit.SECONDS),
                "first caller should have entered doSend within 2 seconds");
        Thread.sleep(50); // 첫 호출이 확실히 락을 쥔 상태로 만들기 위한 여유

        long before = System.currentTimeMillis();
        EgovIntegrationMessage secondResponse = service.sendSync(secondRequest, SHORT_TIMEOUT);
        long elapsed = System.currentTimeMillis() - before;

        assertEquals(ResultCode.TIME_OUT, secondResponse.getHeader().getResultCode());
        assertTrue(elapsed < SLOW_SERVICE_TIME,
                "second call should time out on the lock wait (" + SHORT_TIMEOUT
                        + "ms), not on the slow send (" + SLOW_SERVICE_TIME + "ms); took " + elapsed + "ms");
        assertFalse(secondActuallyRan.get(),
                "doSend must not have been attempted yet when TIME_OUT is returned");

        firstCaller.join(5000);
        // 첫 호출이 끝나 락이 풀린 뒤에도, 두 번째 호출은 이미 인터럽트되어 doSend를
        // 다시 시도하지 않아야 한다 (뒤늦은 "유령" 전송이 없어야 한다).
        Thread.sleep(SLOW_SERVICE_TIME);
        assertFalse(secondActuallyRan.get(),
                "a TIME_OUT response must not be followed by a belated (phantom) send");
    }

    private static class RecordingService extends AbstractService {
        private final CountDownLatch firstCallEnteredCriticalSection;
        private final AtomicBoolean secondActuallyRan;
        private volatile boolean firstCallSeen = false;

        RecordingService(String id, long defaultTimeout,
                          CountDownLatch firstCallEnteredCriticalSection,
                          AtomicBoolean secondActuallyRan) {
            super(id, defaultTimeout);
            this.firstCallEnteredCriticalSection = firstCallEnteredCriticalSection;
            this.secondActuallyRan = secondActuallyRan;
        }

        @Override
        protected EgovIntegrationMessage doSend(EgovIntegrationMessage requestMessage) {
            if (!firstCallSeen) {
                firstCallSeen = true;
                firstCallEnteredCriticalSection.countDown();
                try {
                    Thread.sleep(SLOW_SERVICE_TIME);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                secondActuallyRan.set(true);
            }
            return requestMessage;
        }

        public EgovIntegrationMessage createRequestMessage() {
            return new SimpleMessage(new SimpleMessageHeader());
        }
    }
}
