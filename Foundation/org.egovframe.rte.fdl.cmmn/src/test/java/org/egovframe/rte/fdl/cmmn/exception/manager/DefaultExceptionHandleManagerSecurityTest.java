package org.egovframe.rte.fdl.cmmn.exception.manager;

import org.egovframe.rte.fdl.cmmn.exception.handler.ExceptionHandler;
import org.junit.Test;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 후처리 매니저가 발생 위치를 상태가 아닌 인자로 전달받아, 동시 요청 간 발생 위치가 섞이지 않는지 검증한다.
 */
public class DefaultExceptionHandleManagerSecurityTest {

    @Test
    public void 동시_실행해도_발생_위치가_섞이지_않는다() throws Exception {
        final List<String> mismatches = Collections.synchronizedList(new ArrayList<String>());
        DefaultExceptionHandleManager manager = new DefaultExceptionHandleManager();
        manager.setReqExpMatcher(new AntPathMatcher());
        manager.setPatterns(new String[]{"**"});
        manager.setHandlers(new ExceptionHandler[]{new ExceptionHandler() {
            @Override
            public void occur(Exception exception, String packageName) {
                if (!packageName.equals(exception.getMessage())) {
                    mismatches.add(exception.getMessage() + " != " + packageName);
                }
            }
        }});

        ExecutorService executor = Executors.newFixedThreadPool(8);
        final CountDownLatch start = new CountDownLatch(1);
        for (int i = 0; i < 2000; i++) {
            final String location = "egovframework.example.Service" + i + ".method";
            executor.submit(() -> {
                start.await();
                return manager.run(new RuntimeException(location), location);
            });
        }
        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
        assertEquals(mismatches.toString(), 0, mismatches.size());
    }

}
