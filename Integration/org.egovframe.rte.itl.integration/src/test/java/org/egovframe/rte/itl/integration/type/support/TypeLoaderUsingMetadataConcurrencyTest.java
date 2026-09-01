package org.egovframe.rte.itl.integration.type.support;

import org.egovframe.rte.itl.integration.type.Type;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TypeLoaderUsingMetadata} 의 동시 {@code getType} 호출에 대한 캐시 안전성을 검증한다.
 *
 * <p>{@code typePool} 은 싱글톤 빈에서 여러 스레드가 {@code getType} 으로 공유하는데, 이전
 * 구현은 동기화 없는 {@code HashMap} 을 검사 후 갱신해 콜드 캐시에 동시 진입하면 맵 손상이나
 * 스레드별로 서로 다른 인스턴스 반환이 발생할 수 있었다. {@code ConcurrentHashMap} 과
 * {@code putIfAbsent} 적용 후, 동시 호출이 예외 없이 동일한 정본 인스턴스를 반환함을 검증한다.</p>
 */
class TypeLoaderUsingMetadataConcurrencyTest {

    @Test
    @DisplayName("동일 타입을 동시에 load해도 예외 없이 같은 정본 인스턴스를 반환한다")
    void concurrentGetTypeReturnsCanonicalInstance() throws Exception {
        // boolean[] 는 리스트-원시 타입이라 RecordTypeDefinitionDao 없이 캐싱 경로를 탄다.
        TypeLoaderUsingMetadata loader = new TypeLoaderUsingMetadata();

        int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        List<Type> results = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        try {
            for (int i = 0; i < threads; i++) {
                pool.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        results.add(loader.getType("boolean[]"));
                    } catch (Throwable t) {
                        errors.add(t);
                    } finally {
                        done.countDown();
                    }
                });
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            assertTrue(done.await(5, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }

        assertTrue(errors.isEmpty(), "동시 getType 중 예외가 발생하지 않아야 한다: " + errors);
        assertEquals(threads, results.size());

        Type canonical = loader.getType("boolean[]");
        for (Type t : results) {
            assertSame(canonical, t, "모든 스레드가 캐시된 동일 인스턴스를 반환해야 한다");
        }
    }
}
