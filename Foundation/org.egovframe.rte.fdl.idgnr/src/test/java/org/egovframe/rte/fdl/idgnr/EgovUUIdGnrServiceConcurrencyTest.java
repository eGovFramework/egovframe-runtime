package org.egovframe.rte.fdl.idgnr;

import org.egovframe.rte.fdl.idgnr.impl.EgovUUIdGnrServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * UUId Generation Service 동시성 Test 클래스
 *
 * Mac Address 세팅 경로에서 UUID 동시 생성 시 중복이 발생하지 않고,
 * 기존 UUID 구조와 hostId 정보가 유지되는지 검증한다.
 */
public class EgovUUIdGnrServiceConcurrencyTest {

    /** 테스트 반복 횟수 */
    private static final int TEST_COUNT = 4000;
    /** 동시 스레드 수 */
    private static final int TEST_THREAD = 16;

    /**
     * Mac Address 세팅 후 동시 생성 테스트
     */
    @Test
    @Timeout(30)
    public void testUUIdGenerationInThread() throws Exception {
        EgovUUIdGnrServiceImpl service = new EgovUUIdGnrServiceImpl();
        service.setAddress("00:00:F0:79:19:5B");

        int totalIds = TEST_COUNT * TEST_THREAD;
        CyclicBarrier barrier = new CyclicBarrier(TEST_THREAD);
        ExecutorService executorService = Executors.newFixedThreadPool(TEST_THREAD);
        List<Future<List<String>>> futures = new ArrayList<>();

        for (int i = 0; i < TEST_THREAD; i++) {
            futures.add(executorService.submit(() -> {
                List<String> ids = new ArrayList<>(TEST_COUNT);
                barrier.await();
                for (int j = 0; j < TEST_COUNT; j++) {
                    ids.add(service.getNextStringId());
                }
                return ids;
            }));
        }

        List<String> ids = new ArrayList<>(totalIds);
        for (Future<List<String>> future : futures) {
            ids.addAll(future.get());
        }

        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        int distinct = new HashSet<>(ids).size();
        assertEquals(totalIds, distinct,
                "동시 생성된 UUID에 중복이 존재. total=" + totalIds + ", distinct=" + distinct + ", duplicates=" + (totalIds - distinct));
    }

    /**
     * Mac Address 세팅 후 UUID 구조 유지 테스트
     */
    @Test
    public void testUUIdGenerationFormat() throws Exception {
        EgovUUIdGnrServiceImpl service = new EgovUUIdGnrServiceImpl();
        service.setAddress("00:00:F0:79:19:5B");

        String firstId = service.getNextStringId();
        String secondId = service.getNextStringId();
        UUID uuid = UUID.fromString(firstId);

        assertEquals(1, uuid.version());
        assertEquals(0x0000F079195BL, uuid.getLeastSignificantBits() & 0xFFFFFFFFFFFFL);
        assertNotEquals(firstId, secondId);
    }

}
