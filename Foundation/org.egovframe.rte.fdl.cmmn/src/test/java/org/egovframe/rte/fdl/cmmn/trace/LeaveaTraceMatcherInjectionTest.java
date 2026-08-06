package org.egovframe.rte.fdl.cmmn.trace;

import org.egovframe.rte.fdl.cmmn.trace.handler.TraceHandler;
import org.egovframe.rte.fdl.cmmn.trace.manager.DefaultTraceHandleManager;
import org.egovframe.rte.fdl.cmmn.trace.manager.TraceHandlerService;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.util.AntPathMatcher;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LeaveaTraceMatcherInjectionTest {

    // matcher가 주입되지 않은 매니저에는 LeaveaTrace 의 기본 matcher 를 주입해야 후처리 핸들러가 실행된다.
    @Test
    public void testInjectDefaultMatcherWhenManagerHasNone() {
        LeaveaTrace trace = new LeaveaTrace();
        SpyTraceHandler handler = new SpyTraceHandler();
        DefaultTraceHandleManager manager = new DefaultTraceHandleManager();
        manager.setPatterns(new String[]{"**"});
        manager.setHandlers(new TraceHandler[]{handler});
        trace.setTraceHandlerServices(new TraceHandlerService[]{manager});

        trace.trace(LeaveaTraceMatcherInjectionTest.class, messageSource(), "trace.message", new Object[]{"test"}, Locale.KOREA, null);

        assertEquals(1, handler.callCount);
        assertEquals("trace test message", handler.lastMessage);
    }

    // 사용자가 명시 주입한 matcher 는 기본 matcher 로 덮어쓰지 않는다.
    @Test
    public void testKeepUserDefinedMatcher() {
        LeaveaTrace trace = new LeaveaTrace();
        SpyTraceHandler handler = new SpyTraceHandler();
        RecordingPathMatcher customPathMatcher = new RecordingPathMatcher();
        DefaultTraceHandleManager manager = new DefaultTraceHandleManager();
        manager.setPatterns(new String[]{"**"});
        manager.setHandlers(new TraceHandler[]{handler});
        manager.setReqExpMatcher(customPathMatcher);
        trace.setTraceHandlerServices(new TraceHandlerService[]{manager});

        trace.trace(LeaveaTraceMatcherInjectionTest.class, messageSource(), "trace.message", new Object[]{"custom"}, Locale.KOREA, null);

        assertTrue(customPathMatcher.matchCalled);
        assertEquals(1, handler.callCount);
        assertEquals("trace custom message", handler.lastMessage);
    }

    // 패턴이 일치하지 않으면 matcher 주입 여부와 무관하게 핸들러를 호출하지 않는다.
    @Test
    public void testHandlerNotCalledWhenPatternMismatch() {
        LeaveaTrace trace = new LeaveaTrace();
        SpyTraceHandler handler = new SpyTraceHandler();
        DefaultTraceHandleManager manager = new DefaultTraceHandleManager();
        manager.setPatterns(new String[]{"com.example.*"});
        manager.setHandlers(new TraceHandler[]{handler});
        trace.setTraceHandlerServices(new TraceHandlerService[]{manager});

        trace.trace(LeaveaTraceMatcherInjectionTest.class, messageSource(), "trace.message", new Object[]{"miss"}, Locale.KOREA, null);

        assertEquals(0, handler.callCount);
    }

    private StaticMessageSource messageSource() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("trace.message", Locale.KOREA, "trace {0} message");
        return messageSource;
    }

    private static class SpyTraceHandler implements TraceHandler {

        private int callCount;
        private String lastMessage;

        @Override
        public void todo(Class<?> clazz, String message) {
            callCount++;
            lastMessage = message;
        }

    }

    private static class RecordingPathMatcher extends AntPathMatcher {

        private boolean matchCalled;

        @Override
        public boolean match(String pattern, String path) {
            matchCalled = true;
            return super.match(pattern, path);
        }

    }

}
