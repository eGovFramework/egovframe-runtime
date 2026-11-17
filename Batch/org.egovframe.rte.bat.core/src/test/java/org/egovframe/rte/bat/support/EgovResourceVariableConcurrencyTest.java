package org.egovframe.rte.bat.support;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovResourceVariable 의 뮤텍스 일관성 테스트.
 *
 * <p>8개 메서드가 synchronized (this.map) 으로 잠금. 회귀 : setClear 가 뮤텍스 객체를 새 HashMap 으로 교체하면
 * 스레드별로 다른 모니터를 잡고 같은 맵 수정 (setPros 처럼 map.clear 제자리 비우기 필요).</p>
 *
 * @author 기여자
 * @version 1.0
 * @since 2026.08.19
 */
public class EgovResourceVariableConcurrencyTest {

    @Test
    public void setClearKeepsMutexAndTargetMapIdentical() {
        EgovResourceVariable variable = new EgovResourceVariable();
        variable.setVariable("stale", "old");

        // synchronized 메서드 진입 스레드가 보유한 모니터와 같은 객체
        Map<String, Object> mutex = variable.getVariableMap();
        synchronized (mutex) {
            variable.setClear();
            assertTrue(Thread.holdsLock(variable.getVariableMap()),
                    "setClear 후 잠금 대상과 변경 대상이 서로 다른 객체가 됐다");
        }
        assertTrue(variable.getVariableMap().isEmpty(), "setClear 후 맵이 비어 있어야 한다");
    }

    @Test
    public void writerBlockedOnMutexWritesIntoTheMapItLocked() throws InterruptedException {
        final EgovResourceVariable variable = new EgovResourceVariable();
        variable.setVariable("stale", "old");

        final Map<String, Object> lockedMap = variable.getVariableMap();
        Thread writer = new Thread(() -> variable.setVariable("written", "value"), "resource-variable-writer");

        synchronized (lockedMap) {
            writer.start();
            // 잠금 표현식만 평가, 모니터 앞 대기 상태 확인 후 비움
            awaitBlocked(writer);
            variable.setClear();
        }
        writer.join(TimeUnit.SECONDS.toMillis(10));

        assertFalse(writer.isAlive(), "쓰기 스레드가 종료되지 않았다");
        assertFalse(lockedMap.containsKey("stale"), "setClear 가 쓰기 스레드의 잠금 대상 맵을 비우지 않았다");
        assertTrue(lockedMap.containsKey("written"), "쓰기가 자신이 잠근 맵이 아닌 다른 맵에 들어갔다");
    }

    /**
     * 대상 스레드가 모니터 앞에서 대기할 때까지 기다린다. 대기 상태를 확인한 뒤에만
     * 다음 단계로 넘어가므로 검증 결과는 스케줄링에 좌우되지 않는다.
     */
    private static void awaitBlocked(Thread thread) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (thread.getState() != Thread.State.BLOCKED) {
            assertTrue(System.nanoTime() < deadline, "쓰기 스레드가 모니터 앞에서 대기하지 않았다");
            Thread.sleep(1);
        }
    }
}
