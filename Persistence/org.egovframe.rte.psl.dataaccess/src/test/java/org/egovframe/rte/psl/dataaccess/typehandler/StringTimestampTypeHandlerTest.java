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
package org.egovframe.rte.psl.dataaccess.typehandler;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link StringTimestampTypeHandler} 단위 테스트.
 * <p>
 * iBatis 의 TypeHandler 는 싱글톤으로 공유되어 여러 스레드에서 동시에 호출된다.
 * 과거 구현은 {@code static SimpleDateFormat} 을 공유하여 thread-safe 하지 않았고,
 * 동시 호출 시 잘못된 결과나 예외(NumberFormatException, ArrayIndexOutOfBoundsException 등)를
 * 유발할 수 있었다. 본 테스트는 단일 인스턴스를 다수 스레드가 동시에 사용해도
 * 결과가 정확하고 예외가 없음을 검증한다.
 * </p>
 */
public class StringTimestampTypeHandlerTest {

    private static final String FORMATTED = "20240102030405";
    private static final Timestamp EXPECTED_TS = Timestamp.valueOf("2024-01-02 03:04:05");

    /** 지정한 Timestamp 를 반환하는 ResultGetter 프록시 (필요 메서드만 구현). */
    private ResultGetter resultGetter(final Timestamp ts) {
        return (ResultGetter) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ResultGetter.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "wasNull":
                            return Boolean.FALSE;
                        case "getTimestamp":
                            return ts;
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    /** setTimestamp 호출값을 캡처하는 ParameterSetter 프록시 (필요 메서드만 구현). */
    private ParameterSetter capturingSetter(final AtomicReference<Timestamp> captured) {
        return (ParameterSetter) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ParameterSetter.class},
                (proxy, method, args) -> {
                    if ("setTimestamp".equals(method.getName()) && args != null && args.length == 1) {
                        captured.set((Timestamp) args[0]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    @Test
    public void roundTripPreservesValue() throws Exception {
        StringTimestampTypeHandler handler = new StringTimestampTypeHandler();

        // Timestamp -> String
        assertEquals(FORMATTED, handler.getResult(resultGetter(EXPECTED_TS)));

        // String -> Timestamp
        AtomicReference<Timestamp> captured = new AtomicReference<>();
        handler.setParameter(capturingSetter(captured), FORMATTED);
        assertEquals(EXPECTED_TS, captured.get());
    }

    @Test
    public void sharedInstanceIsThreadSafe() throws Exception {
        final StringTimestampTypeHandler handler = new StringTimestampTypeHandler();
        final int threads = 16;
        final int iterations = 500;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        final CountDownLatch ready = new CountDownLatch(threads);
        final CountDownLatch start = new CountDownLatch(1);
        final List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        for (int t = 0; t < threads; t++) {
            pool.execute(() -> {
                ready.countDown();
                try {
                    start.await();
                    for (int i = 0; i < iterations; i++) {
                        // format 경로
                        Object formatted = handler.getResult(resultGetter(EXPECTED_TS));
                        if (!FORMATTED.equals(formatted)) {
                            failures.add(new AssertionError("getResult=" + formatted));
                        }
                        // parse 경로
                        AtomicReference<Timestamp> captured = new AtomicReference<>();
                        handler.setParameter(capturingSetter(captured), FORMATTED);
                        if (!EXPECTED_TS.equals(captured.get())) {
                            failures.add(new AssertionError("setParameter=" + captured.get()));
                        }
                    }
                } catch (Throwable e) {
                    failures.add(e);
                }
            });
        }

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS), "동시성 테스트 시간 초과");
        assertTrue(failures.isEmpty(), "동시 호출 중 오류 발생: " + failures);
    }
}
