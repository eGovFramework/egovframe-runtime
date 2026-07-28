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
package org.egovframe.rte.bat.core.item.database;

import org.egovframe.rte.bat.core.item.database.support.EgovMethodMapItemPreparedStatementSetter;
import org.egovframe.rte.bat.core.reflection.EgovReflectionSupport;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * JDBC 배치 쓰기 핫패스의 reflection/할당 최적화에 대한 결정적(외부 DB 없는) 검증.
 *
 * <pre>
 * - setter: EgovMethodMapItemPreparedStatementSetter 가 행마다 EgovReflectionSupport 를
 *   재생성하지 않고 1회 생성·재사용하는지(R->1) 및 동작 보존 검증.
 * - writer: EgovJdbcBatchItemWriter 가 sqlTypes 산출(getSqlTypeArray, getDeclaredField)을
 *   청크마다 반복하지 않고 1회 캐시하는지(N->1, 클래스별) 및 이질 item 클래스 재산출 검증.
 * </pre>
 *
 * @author 배치실행개발팀
 * @since 2026.06.30
 */
class EgovJdbcBatchWriteReflectionTest {

    private static final String[] PARAMS = {"name", "age", "id", "score", "active"};

    // ---------------------------------------------------------------------
    // Test fixtures
    // ---------------------------------------------------------------------

    /** sqlTypes(String, int, long, double, boolean) 분기를 커버하는 VO. */
    public static class SampleVo {
        private String name;
        private int age;
        private long id;
        private double score;
        private boolean active;

        public SampleVo() {
        }

        public SampleVo(String name, int age, long id, double score, boolean active) {
            this.name = name;
            this.age = age;
            this.id = id;
            this.score = score;
            this.active = active;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public long getId() {
            return id;
        }

        public double getScore() {
            return score;
        }

        public boolean getActive() {
            return active;
        }
    }

    /** 이질 item 클래스 재산출 검증용 (동일 필드 구성, 다른 클래스). */
    public static class OtherVo {
        private String name;
        private int age;
        private long id;
        private double score;
        private boolean active;

        public OtherVo() {
        }

        public OtherVo(String name, int age, long id, double score, boolean active) {
            this.name = name;
            this.age = age;
            this.id = id;
            this.score = score;
            this.active = active;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public long getId() {
            return id;
        }

        public double getScore() {
            return score;
        }

        public boolean getActive() {
            return active;
        }
    }

    /** getSqlTypeArray 호출 횟수를 세는 spy. (EgovReflectionSupport 미수정, 서브클래스 override) */
    static class CountingReflectionSupport<T> extends EgovReflectionSupport<T> {
        final AtomicInteger sqlTypeArrayCalls = new AtomicInteger();

        @Override
        public String[] getSqlTypeArray(String[] params, Object item) {
            sqlTypeArrayCalls.incrementAndGet();
            return super.getSqlTypeArray(params, item);
        }

        int getSqlTypeArrayCalls() {
            return sqlTypeArrayCalls.get();
        }
    }

    /** setXxx/addBatch/executeBatch 호출을 기록하는 가짜 PreparedStatement InvocationHandler. */
    static class FakePsHandler implements InvocationHandler {
        final List<Object[]> calls = new ArrayList<>(); // {methodName, index, value}
        int batchCount = 0;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if (name.startsWith("set") && args != null && args.length >= 1 && args[0] instanceof Integer) {
                calls.add(new Object[]{name, args[0], args.length > 1 ? args[1] : null});
                return null;
            }
            if ("addBatch".equals(name)) {
                batchCount++;
                return null;
            }
            if ("executeBatch".equals(name)) {
                int[] result = new int[batchCount];
                java.util.Arrays.fill(result, 1);
                return result;
            }
            if ("clearParameters".equals(name) || "clearBatch".equals(name)) {
                return null;
            }
            return defaultFor(method.getReturnType());
        }

        private static Object defaultFor(Class<?> returnType) {
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == void.class) {
                return null;
            }
            if (returnType == long.class) {
                return 0L;
            }
            if (returnType == double.class) {
                return 0d;
            }
            if (returnType == float.class) {
                return 0f;
            }
            if (returnType == char.class) {
                return '\0';
            }
            return 0; // int, short, byte
        }
    }

    private static PreparedStatement newFakePs(FakePsHandler handler) {
        return (PreparedStatement) Proxy.newProxyInstance(
                FakePsHandler.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class}, handler);
    }

    private static void assertSampleRow(List<Object[]> calls, int base, SampleVo vo) {
        assertEquals("setString", calls.get(base)[0]);
        assertEquals(1, calls.get(base)[1]);
        assertEquals(vo.getName(), calls.get(base)[2]);

        assertEquals("setInt", calls.get(base + 1)[0]);
        assertEquals(2, calls.get(base + 1)[1]);
        assertEquals(vo.getAge(), calls.get(base + 1)[2]);

        assertEquals("setLong", calls.get(base + 2)[0]);
        assertEquals(3, calls.get(base + 2)[1]);
        assertEquals(vo.getId(), calls.get(base + 2)[2]);

        assertEquals("setDouble", calls.get(base + 3)[0]);
        assertEquals(4, calls.get(base + 3)[1]);
        assertEquals(vo.getScore(), calls.get(base + 3)[2]);

        assertEquals("setBoolean", calls.get(base + 4)[0]);
        assertEquals(5, calls.get(base + 4)[1]);
        assertEquals(vo.getActive(), calls.get(base + 4)[2]);
    }

    // ---------------------------------------------------------------------
    // Test A: setter EgovReflectionSupport 할당 hoist (R -> 1) + 동작 보존
    // ---------------------------------------------------------------------

    @Test
    void setterReusesSingleReflectorAcrossRows() throws Exception {
        SampleVo item = new SampleVo("kim", 30, 1001L, 88.5, true);

        EgovReflectionSupport<SampleVo> support = new EgovReflectionSupport<>();
        support.generateGetterMethodMap(PARAMS, item);
        Map<String, Method> methodMap = support.getMethodMap();
        String[] sqlTypes = support.getSqlTypeArray(PARAMS, item);

        EgovMethodMapItemPreparedStatementSetter<SampleVo> setter =
                new EgovMethodMapItemPreparedStatementSetter<>();

        Field reflectorField = EgovMethodMapItemPreparedStatementSetter.class.getDeclaredField("reflector");
        reflectorField.setAccessible(true);

        Object firstReflector = null;
        int rows = 5;
        for (int row = 0; row < rows; row++) {
            FakePsHandler handler = new FakePsHandler();
            PreparedStatement ps = newFakePs(handler);

            setter.setValues(item, ps, PARAMS, sqlTypes, methodMap);

            // R -> 1: 행마다 새로 생성하지 않고 동일 인스턴스를 재사용
            Object current = reflectorField.get(setter);
            assertNotNull(current, "reflector 필드가 1회 생성되어 있어야 한다");
            if (firstReflector == null) {
                firstReflector = current;
            } else {
                assertSame(firstReflector, current, "reflector 는 행마다 재사용되어야 한다");
            }

            // 동작 보존: ps.setXxx 호출 인자/순서가 동일
            assertEquals(PARAMS.length, handler.calls.size());
            assertSampleRow(handler.calls, 0, item);
        }
    }

    // ---------------------------------------------------------------------
    // Test B: writer sqlTypes 캐시 (N -> 1, 클래스별) + 이질 item 재산출 + 동작 보존
    // ---------------------------------------------------------------------

    @Test
    void writerCachesSqlTypesAcrossChunksAndRecomputesOnClassChange() throws Exception {
        EgovJdbcBatchItemWriter<Object> writer = new EgovJdbcBatchItemWriter<>();
        writer.setSql("insert into sample (name, age, id, score, active) values (?, ?, ?, ?, ?)");
        writer.setParams(PARAMS);
        writer.setItemPreparedStatementSetter(new EgovMethodMapItemPreparedStatementSetter<>());

        final FakePsHandler[] lastHandler = new FakePsHandler[1];
        JdbcTemplate fakeTemplate = new JdbcTemplate() {
            @Override
            public <X> X execute(String sql, PreparedStatementCallback<X> action) {
                FakePsHandler handler = new FakePsHandler();
                lastHandler[0] = handler;
                try {
                    return action.doInPreparedStatement(newFakePs(handler));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        writer.setSimpleJdbcTemplate(fakeTemplate);
        writer.afterPropertiesSet();

        // afterPropertiesSet 가 생성한 reflector 를 카운팅 spy 로 교체
        CountingReflectionSupport<Object> counting = new CountingReflectionSupport<>();
        Field reflectorField = EgovJdbcBatchItemWriter.class.getDeclaredField("reflector");
        reflectorField.setAccessible(true);
        reflectorField.set(writer, counting);

        // 동질 item K(=3) 청크 처리 -> getSqlTypeArray 는 1회만 (N -> 1)
        SampleVo a = new SampleVo("a", 1, 10L, 1.5, true);
        SampleVo b = new SampleVo("b", 2, 20L, 2.5, false);
        int chunks = 3;
        for (int c = 0; c < chunks; c++) {
            writer.write(new Chunk<>(List.of(a, b)));
        }
        assertEquals(1, counting.getSqlTypeArrayCalls(),
                "동질 청크 K개 처리 시 getSqlTypeArray 는 1회만 호출되어야 한다");

        // 동작 보존: 마지막 청크의 ps 세팅이 item 별 기대 시퀀스와 일치
        assertEquals(2 * PARAMS.length, lastHandler[0].calls.size());
        assertSampleRow(lastHandler[0].calls, 0, a);
        assertSampleRow(lastHandler[0].calls, PARAMS.length, b);

        // 캐시 holder 확인 (itemClass + sqlTypes 가 한 쌍으로 발행됨)
        assertNotNull(sqlTypeCacheHolder(writer));
        assertEquals(SampleVo.class, cachedItemClass(writer));

        // 이질 item 클래스 -> 재산출. (메서드맵은 클래스당 1회 생성 정책이므로
        //  사전 정책과 무관하게 캐시 무효화만 검증하기 위해 fresh spy 로 교체)
        CountingReflectionSupport<Object> counting2 = new CountingReflectionSupport<>();
        reflectorField.set(writer, counting2);

        OtherVo o = new OtherVo("o", 9, 90L, 9.5, true);
        writer.write(new Chunk<>(List.of(o)));

        assertEquals(1, counting2.getSqlTypeArrayCalls(),
                "item 클래스가 바뀌면 sqlTypes 를 재산출해야 한다");
        assertEquals(OtherVo.class, cachedItemClass(writer));
    }

    // ---------------------------------------------------------------------
    // Test C: setParams 호출 시 sqlTypes 캐시 무효화
    // ---------------------------------------------------------------------

    @Test
    void setParamsInvalidatesSqlTypeCache() throws Exception {
        EgovJdbcBatchItemWriter<Object> writer = new EgovJdbcBatchItemWriter<>();
        writer.setSql("insert into sample (name, age, id, score, active) values (?, ?, ?, ?, ?)");
        writer.setParams(PARAMS);
        writer.setItemPreparedStatementSetter(new EgovMethodMapItemPreparedStatementSetter<>());
        writer.setSimpleJdbcTemplate(new JdbcTemplate() {
            @Override
            public <X> X execute(String sql, PreparedStatementCallback<X> action) {
                try {
                    return action.doInPreparedStatement(newFakePs(new FakePsHandler()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        writer.afterPropertiesSet();

        writer.write(new Chunk<>(List.of(new SampleVo("a", 1, 10L, 1.5, true))));
        assertNotNull(sqlTypeCacheHolder(writer), "최초 write 후 캐시가 채워져야 한다");

        // params 변경 -> 캐시 무효화(stale 방지)
        writer.setParams(new String[]{"name", "age"});
        assertNull(sqlTypeCacheHolder(writer), "setParams 호출 시 캐시는 무효화되어야 한다");
    }

    // ---------------------------------------------------------------------
    // Test D: 멀티스레드 동시 청크에서 sqlTypes torn-read 부재 (회귀 가드)
    // 서로 다른 item 클래스의 청크를 두 스레드가 인터리빙하며 write 해도,
    // 각 호출이 받는 sqlTypes 는 항상 자기 item 클래스의 것과 일치해야 한다.
    // ---------------------------------------------------------------------

    @Test
    void concurrentChunksOfDifferentClassesNeverTearSqlTypes() throws Exception {
        // 클래스별로 식별 가능한 sqlTypes 를 돌려주고, 산출-발행 사이 창을 넓히는 stub reflector.
        // (실 reflection 의 methodMap/클래스당 제약과 무관하게 캐시 원자성만 격리 검증)
        EgovReflectionSupport<Object> stub = new EgovReflectionSupport<>() {
            @Override
            public String[] getSqlTypeArray(String[] params, Object item) {
                String tag = item.getClass().getSimpleName();
                Thread.yield(); // 산출-발행 사이 인터리빙 유도(torn-read 창 확대)
                String[] types = new String[params.length];
                java.util.Arrays.fill(types, tag);
                return types;
            }
        };

        // getter 를 실제로 호출하지 않고 (item.getClass, sqlTypes) 쌍만 기록하는 setter.
        final List<Object[]> observed = java.util.Collections.synchronizedList(new ArrayList<>());
        org.egovframe.rte.bat.core.item.database.support.EgovItemPreparedStatementSetter<Object> recordingSetter =
                new org.egovframe.rte.bat.core.item.database.support.EgovItemPreparedStatementSetter<>() {
                    @Override
                    public void setValues(Object item, PreparedStatement ps, String[] params, String[] sqlTypes, Map<String, Method> methodMap) {
                        observed.add(new Object[]{item.getClass(), sqlTypes});
                    }
                };

        EgovJdbcBatchItemWriter<Object> writer = new EgovJdbcBatchItemWriter<>();
        writer.setSql("insert into sample (name) values (?)");
        writer.setParams(new String[]{"name"});
        writer.setItemPreparedStatementSetter(recordingSetter);
        writer.setSimpleJdbcTemplate(new JdbcTemplate() {
            @Override
            public <X> X execute(String sql, PreparedStatementCallback<X> action) {
                try {
                    return action.doInPreparedStatement(newFakePs(new FakePsHandler()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        writer.afterPropertiesSet();

        Field reflectorField = EgovJdbcBatchItemWriter.class.getDeclaredField("reflector");
        reflectorField.setAccessible(true);
        reflectorField.set(writer, stub);

        int rounds = 300;
        java.util.concurrent.CyclicBarrier barrier = new java.util.concurrent.CyclicBarrier(2);
        java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();

        Runnable sampleWorker = worker(writer, new SampleVo("s", 1, 1L, 1.0, true), rounds, barrier, error);
        Runnable otherWorker = worker(writer, new OtherVo("o", 2, 2L, 2.0, false), rounds, barrier, error);

        Thread t1 = new Thread(sampleWorker, "sample-writer");
        Thread t2 = new Thread(otherWorker, "other-writer");
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertNull(error.get(), "동시 write 중 예외가 없어야 한다: " + error.get());

        // 모든 호출에서 item 클래스와 sqlTypes 가 일치(torn-read 부재).
        Object[][] snapshot = observed.toArray(new Object[0][]);
        assertEquals(2 * rounds, snapshot.length, "두 스레드의 모든 write 가 기록되어야 한다");
        for (Object[] pair : snapshot) {
            Class<?> itemClass = (Class<?>) pair[0];
            String[] sqlTypes = (String[]) pair[1];
            assertEquals(itemClass.getSimpleName(), sqlTypes[0],
                    "item 클래스와 sqlTypes 가 어긋남(torn-read): " + itemClass.getSimpleName() + " vs " + sqlTypes[0]);
        }
    }

    private static Runnable worker(EgovJdbcBatchItemWriter<Object> writer, Object item, int rounds,
                                   java.util.concurrent.CyclicBarrier barrier,
                                   java.util.concurrent.atomic.AtomicReference<Throwable> error) {
        return () -> {
            try {
                for (int i = 0; i < rounds; i++) {
                    barrier.await(); // 라운드마다 두 스레드를 정렬시켜 인터리빙 강제
                    writer.write(new Chunk<>(List.of(item)));
                }
            } catch (Throwable t) {
                error.compareAndSet(null, t);
            }
        };
    }

    // ---------------------------------------------------------------------
    // 캐시 holder 리플렉션 helper
    // ---------------------------------------------------------------------

    private static Object sqlTypeCacheHolder(EgovJdbcBatchItemWriter<?> writer) throws Exception {
        Field cacheField = EgovJdbcBatchItemWriter.class.getDeclaredField("sqlTypeCache");
        cacheField.setAccessible(true);
        return cacheField.get(writer);
    }

    private static Class<?> cachedItemClass(EgovJdbcBatchItemWriter<?> writer) throws Exception {
        Object holder = sqlTypeCacheHolder(writer);
        if (holder == null) {
            return null;
        }
        Field itemClassField = holder.getClass().getDeclaredField("itemClass");
        itemClassField.setAccessible(true);
        return (Class<?>) itemClassField.get(holder);
    }
}
