package org.egovframe.rte.fdl.cmmn.exception.manager;

import org.egovframe.rte.fdl.cmmn.exception.handler.ExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 예외 후처리 매니저가 발생 위치를 파라미터로 받아 처리하는 경로(run(Exception, String))와
 * 구 방식 구현체의 하위 호환을 검증한다.
 */
public class ExceptionHandlerPackageNameTest {

    /** 전달받은 (예외 메시지, packageName) 쌍을 기록하는 핸들러 — 메시지에 기대 발생 위치를 실어 보낸다 */
    static class RecordingHandler implements ExceptionHandler {
        final List<String[]> received = new CopyOnWriteArrayList<>();

        @Override
        public void occur(Exception exception, String packageName) {
            received.add(new String[] {exception.getMessage(), packageName});
        }

        long mismatches() {
            return received.stream().filter(pair -> !pair[0].equals(pair[1])).count();
        }
    }

    /** 구 방식(setPackageName 뒤 run(Exception))만 아는 커스텀 구현체 */
    @SuppressWarnings("deprecation")
    static class LegacyManager implements ExceptionHandlerService {
        private final ExceptionHandler handler;
        private String packageName;

        LegacyManager(ExceptionHandler handler) {
            this.handler = handler;
        }

        @Override
        public void setPatterns(String[] patterns) {
        }

        @Override
        public void setHandlers(ExceptionHandler[] handlers) {
        }

        @Override
        public void setPackageName(String canonicalName) {
            this.packageName = canonicalName;
        }

        @Override
        public void setException(Exception be) {
        }

        @Override
        public void setReqExpMatcher(PathMatcher pm) {
        }

        @Override
        public boolean run(Exception exception) {
            handler.occur(exception, packageName);
            return true;
        }

        @Override
        public boolean hasReqExpMatcher() {
            return true;
        }
    }

    private DefaultExceptionHandleManager newManager(RecordingHandler handler) {
        DefaultExceptionHandleManager manager = new DefaultExceptionHandleManager();
        manager.setPatterns(new String[] {"org.sample.**"});
        manager.setHandlers(new ExceptionHandler[] {handler});
        manager.setReqExpMatcher(new AntPathMatcher());
        return manager;
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testRunWithPackageNameIgnoresSingletonField() throws Exception {
        RecordingHandler handler = new RecordingHandler();
        DefaultExceptionHandleManager manager = newManager(handler);

        // 다른 요청이 남긴 것처럼 싱글톤 필드를 오염시켜도
        manager.setPackageName("org.other.PollutedService.method");
        manager.run(new Exception("org.sample.OrderService.create"), "org.sample.OrderService.create");

        assertEquals(1, handler.received.size());
        assertEquals("org.sample.OrderService.create", handler.received.get(0)[1],
                "매칭과 전달 모두 파라미터의 발생 위치를 사용해야 한다");
        assertEquals("org.other.PollutedService.method", manager.getPackageName(),
                "파라미터 경로는 싱글톤 필드를 바꾸지 않는다");
    }

    @Test
    public void testRunWithPackageNameSkipsUnmatchedPattern() throws Exception {
        RecordingHandler handler = new RecordingHandler();
        DefaultExceptionHandleManager manager = newManager(handler);

        assertTrue(manager.run(new Exception("e"), "com.unmatched.Service.method"));

        assertTrue(handler.received.isEmpty());
    }

    @Test
    public void testRunWithPackageNameReturnsFalseWithoutMatcher() throws Exception {
        RecordingHandler handler = new RecordingHandler();
        DefaultExceptionHandleManager manager = new DefaultExceptionHandleManager();
        manager.setPatterns(new String[] {"org.sample.**"});
        manager.setHandlers(new ExceptionHandler[] {handler});

        assertFalse(manager.run(new Exception("e"), "org.sample.Service.method"));
        assertTrue(handler.received.isEmpty());
    }

    @Test
    public void testConcurrentRunsDeliverEachCallsOwnPackageName() throws Exception {
        RecordingHandler handler = new RecordingHandler();
        DefaultExceptionHandleManager manager = newManager(handler);

        runConcurrently(manager, handler);
    }

    @Test
    public void testLegacyImplementationStillWorksThroughDefaultMethod() throws Exception {
        RecordingHandler handler = new RecordingHandler();
        ExceptionHandlerService legacy = new LegacyManager(handler);

        legacy.run(new Exception("org.sample.LegacyService.method"), "org.sample.LegacyService.method");

        assertEquals(1, handler.received.size());
        assertEquals("org.sample.LegacyService.method", handler.received.get(0)[1],
                "기본 구현이 구 방식으로 위임하여 커스텀 구현체가 수정 없이 동작해야 한다");
    }

    @Test
    public void testLegacyImplementationIsSerializedUnderConcurrency() throws Exception {
        RecordingHandler handler = new RecordingHandler();
        ExceptionHandlerService legacy = new LegacyManager(handler);

        runConcurrently(legacy, handler);
    }

    @Test
    public void testSubclassOverridingLegacyRunKeepsItsBehavior() throws Exception {
        RecordingHandler handler = new RecordingHandler();
        List<String> overridden = new CopyOnWriteArrayList<>();
        DefaultExceptionHandleManager subclass = new DefaultExceptionHandleManager() {
            @Override
            @SuppressWarnings("deprecation")
            public boolean run(Exception exception) throws Exception {
                overridden.add(getPackageName());
                return super.run(exception);
            }
        };
        subclass.setPatterns(new String[] {"org.sample.**"});
        subclass.setHandlers(new ExceptionHandler[] {handler});
        subclass.setReqExpMatcher(new AntPathMatcher());

        subclass.run(new Exception("org.sample.SubService.method"), "org.sample.SubService.method");

        assertEquals(List.of("org.sample.SubService.method"), overridden,
                "하위 클래스가 재정의한 run(Exception) 이 계속 호출되어야 한다");
        assertEquals(1, handler.received.size());
        assertEquals("org.sample.SubService.method", handler.received.get(0)[1]);
    }

    private static void runConcurrently(ExceptionHandlerService service, RecordingHandler handler) throws Exception {
        int threads = 4;
        int iterations = 200;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        for (int t = 0; t < threads; t++) {
            final int threadNo = t;
            executor.submit(() -> {
                ready.countDown();
                start.await();
                for (int i = 0; i < iterations; i++) {
                    String packageName = "org.sample.Svc" + threadNo + ".m" + i;
                    service.run(new Exception(packageName), packageName);
                }
                return null;
            });
        }
        ready.await();
        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        assertEquals(threads * iterations, handler.received.size());
        assertEquals(0, handler.mismatches(),
                "동시 실행에서도 각 호출의 발생 위치가 그 호출의 핸들러에 전달되어야 한다");
    }
}
