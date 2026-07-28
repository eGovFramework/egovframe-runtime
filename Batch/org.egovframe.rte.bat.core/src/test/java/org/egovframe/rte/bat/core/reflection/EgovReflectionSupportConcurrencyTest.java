package org.egovframe.rte.bat.core.reflection;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovReflectionSupport 의 멀티스레드 안전성 테스트.
 *
 * <p>FieldExtractor/ItemWriter 빈이 멀티스레드 step 에서 공유되면 그 안의
 * EgovReflectionSupport 인스턴스도 공유된다. 과거 generateGetterMethodMap 은
 * methods 를 먼저 대입한 뒤 methodMap 을 채워, 다른 스레드가 부분초기화 상태를
 * 사용하다 NPE 에 노출되었다(cold-start 경합). 본 테스트로 회귀를 방지한다.</p>
 *
 * @author 기여자
 * @version 1.0
 * @since 2026.06.09
 */
public class EgovReflectionSupportConcurrencyTest {

    private static final String[] NAMES = {"name", "age"};

    @Test
    public void sharedInstanceColdStartIsThreadSafe() throws InterruptedException {
        final int threads = 32;
        final int rounds = 500;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            for (int round = 0; round < rounds; round++) {
                // 라운드마다 새 공유 인스턴스를 만들어 cold-start lazy-init 경합을 재현한다.
                final EgovReflectionSupport<SampleVo> shared = new EgovReflectionSupport<SampleVo>();
                final SampleVo item = new SampleVo("egov", 42);
                final CountDownLatch startGate = new CountDownLatch(1);
                final CountDownLatch doneGate = new CountDownLatch(threads);
                final List<Throwable> failures = new CopyOnWriteArrayList<Throwable>();
                final List<Object> names = new CopyOnWriteArrayList<Object>();
                final List<Object> ages = new CopyOnWriteArrayList<Object>();

                for (int t = 0; t < threads; t++) {
                    pool.execute(() -> {
                        try {
                            startGate.await();
                            shared.generateGetterMethodMap(NAMES, item);
                            names.add(shared.invokeGettterMethod(item, "name"));
                            ages.add(shared.invokeGettterMethod(item, "age"));
                        } catch (Throwable e) {
                            failures.add(e);
                        } finally {
                            doneGate.countDown();
                        }
                    });
                }

                startGate.countDown();
                assertTrue(doneGate.await(10, TimeUnit.SECONDS), "round " + round + " timed out");
                assertTrue(failures.isEmpty(),
                        "round " + round + " concurrent failure: " + failures);
                for (Object n : names) {
                    assertEquals("egov", n, "round " + round + " name mismatch");
                }
                for (Object a : ages) {
                    assertEquals(42, a, "round " + round + " age mismatch");
                }
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void singleThreadGetterExtractsValues() {
        EgovReflectionSupport<SampleVo> reflection = new EgovReflectionSupport<SampleVo>();
        SampleVo item = new SampleVo("egov", 42);
        reflection.generateGetterMethodMap(NAMES, item);
        List<Object> values = new ArrayList<Object>();
        for (String name : NAMES) {
            values.add(reflection.invokeGettterMethod(item, name));
        }
        assertEquals("egov", values.get(0));
        assertEquals(42, values.get(1));
    }

    /**
     * 테스트용 VO. public getter 가 reflection 으로 조회된다.
     */
    public static class SampleVo {
        private final String name;
        private final int age;

        public SampleVo(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
}
