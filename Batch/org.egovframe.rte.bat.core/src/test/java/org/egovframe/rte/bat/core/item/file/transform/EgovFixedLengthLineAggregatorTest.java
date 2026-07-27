/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.bat.core.item.file.transform;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EgovFixedLengthLineAggregator} 단위 테스트.
 *
 * <p>고정길이 라인 집계의 우측 패딩 동작과, 동일 인스턴스가 여러 스레드에서 공유될 때의
 * thread-safety(멀티스레드 step / 공유 빈 환경)를 검증한다.</p>
 */
public class EgovFixedLengthLineAggregatorTest {

    private EgovFixedLengthLineAggregator<Object> aggregator(int... ranges) {
        EgovFixedLengthLineAggregator<Object> agg = new EgovFixedLengthLineAggregator<>();
        agg.setFieldRanges(ranges);
        return agg;
    }

    @Test
    public void padsShortValuesToFieldWidth() {
        // "ab" -> 폭 10, "cd" -> 폭 10, 우측 공백 패딩
        String out = aggregator(10, 10).doAggregate(new Object[]{"ab", "cd"});
        assertEquals("ab        cd        ", out);
        assertEquals(20, out.length());
    }

    @Test
    public void keepsExactWidthValuesUnchanged() {
        String out = aggregator(3, 2).doAggregate(new Object[]{"abc", "de"});
        assertEquals("abcde", out);
    }

    @Test
    public void usesCustomPaddingCharacter() {
        EgovFixedLengthLineAggregator<Object> agg = aggregator(5);
        agg.setPadding('0');
        assertEquals("ab000", agg.doAggregate(new Object[]{"ab"}));
    }

    @Test
    public void padsBeyondLegacyChunkBoundary() {
        // 과거 구현의 PADDING_LISTSIZE(100) 분기 경계를 넘는 폭도 동일하게 패딩되어야 한다.
        String out = aggregator(250).doAggregate(new Object[]{"x"});
        assertEquals(250, out.length());
        assertEquals('x', out.charAt(0));
        assertEquals(" ".repeat(249), out.substring(1));
    }

    @Test
    public void throwsWhenValueLongerThanFieldRange() {
        assertThrows(IllegalStateException.class,
                () -> aggregator(2).doAggregate(new Object[]{"toolong"}));
    }

    @Test
    public void sharedInstanceIsThreadSafe() throws Exception {
        int threads = 32;
        int rounds = 500;
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> wrong = new ConcurrentLinkedQueue<>();
        String expected = "ab        cd        "; // width 20

        for (int r = 0; r < rounds && errors.isEmpty() && wrong.isEmpty(); r++) {
            // 매 라운드 fresh 인스턴스 → cold-start 동시 진입(초기화 경합) 노출
            EgovFixedLengthLineAggregator<Object> agg = aggregator(10, 10);
            Object[] fields = {"ab", "cd"};

            ExecutorService pool = Executors.newFixedThreadPool(threads);
            CountDownLatch gate = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    try {
                        gate.await();
                        String out = agg.doAggregate(fields);
                        if (!expected.equals(out)) {
                            wrong.add(out);
                        }
                    } catch (Throwable e) {
                        errors.add(e);
                    } finally {
                        done.countDown();
                    }
                });
            }
            gate.countDown();
            done.await(10, TimeUnit.SECONDS);
            pool.shutdownNow();
        }

        assertTrue(errors.isEmpty(),
                "concurrent aggregate threw: " + (errors.isEmpty() ? "" : errors.peek()));
        assertTrue(wrong.isEmpty(),
                "concurrent aggregate produced wrong output, e.g.: " + (wrong.isEmpty() ? "" : "[" + wrong.peek() + "]"));
    }
}
