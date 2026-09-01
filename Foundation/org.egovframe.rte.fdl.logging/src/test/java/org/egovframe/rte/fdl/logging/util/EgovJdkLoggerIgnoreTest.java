package org.egovframe.rte.fdl.logging.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ignore 로 넘긴 메시지가 기본 설정에서 기록되지 않는지 확인한다.
 *
 * <p>Level.OFF 는 임계값으로 쓸 때만 "끄기"이고, 레코드 수준으로 쓰면 intValue 가
 * Integer.MAX_VALUE 라 어떤 로거 임계값도 걸러내지 못한다. 기록이 불필요하다고 넘긴 메시지가
 * 오히려 가장 높은 심각도로 남는다.</p>
 */
class EgovJdkLoggerIgnoreTest {

    private final AtomicInteger emitted = new AtomicInteger();
    private Logger ignoreLogger;
    private Handler counter;

    @BeforeEach
    void setUp() {
        counter = new Handler() {
            @Override
            public void publish(LogRecord record) {
                emitted.incrementAndGet();
            }

            @Override
            public void flush() {
                // 세는 것이 전부라 할 일이 없다
            }

            @Override
            public void close() {
                // 세는 것이 전부라 할 일이 없다
            }
        };
        counter.setLevel(Level.ALL);

        ignoreLogger = Logger.getLogger("ignore");
        ignoreLogger.setUseParentHandlers(false);
        ignoreLogger.addHandler(counter);
    }

    @AfterEach
    void tearDown() {
        ignoreLogger.removeHandler(counter);
        ignoreLogger.setUseParentHandlers(true);
    }

    @Test
    void testIgnoreDoesNotEmitUnderDefaultThreshold() {
        ignoreLogger.setLevel(Level.INFO);

        EgovJdkLogger.ignore("기록이 불필요한 메시지");
        EgovJdkLogger.ignore("기록이 불필요한 메시지", new IllegalStateException("무시 대상"));

        assertEquals(0, emitted.get(), "ignore 로 넘긴 메시지는 기본 임계값에서 기록되지 않아야 한다");
    }

    @Test
    void testIgnoreIsQuieterThanDebug() {
        ignoreLogger.setLevel(Level.FINEST);

        EgovJdkLogger.ignore("기록이 불필요한 메시지");

        assertEquals(0, emitted.get(), "ignore 는 debug 를 켠 수준에서도 조용해야 한다");
    }
}
