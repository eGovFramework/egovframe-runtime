package org.egovframe.rte.fdl.cmmn.trace.handler;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.egovframe.rte.fdl.cmmn.trace.manager.DefaultTraceHandleManager;
import org.egovframe.rte.fdl.cmmn.trace.manager.TraceHandlerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 기본 TraceHandler 가 전달받은 발생 클래스와 메시지를 실제로 로그에 남기는지 검증한다.
 */
public class DefaultTraceHandlerTest {

    private static final String LOGGER_NAME = DefaultTraceHandler.class.getName();

    private CapturingAppender appender;

    @BeforeEach
    void setUp() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = context.getConfiguration();
        appender = new CapturingAppender();
        appender.start();

        LoggerConfig loggerConfig = new LoggerConfig(LOGGER_NAME, Level.DEBUG, false);
        loggerConfig.addAppender(appender, Level.DEBUG, null);
        configuration.addLogger(LOGGER_NAME, loggerConfig);
        context.updateLoggers();
    }

    @AfterEach
    void tearDown() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.getConfiguration().removeLogger(LOGGER_NAME);
        context.updateLoggers();
        appender.stop();
    }

    @Test
    public void testTodoLogsClassNameAndMessage() {
        new DefaultTraceHandler().todo(DefaultTraceHandlerTest.class, "주문 처리 시작");

        List<String> messages = appender.messages();
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains(DefaultTraceHandlerTest.class.getName()),
                "발생 클래스명이 로그에 있어야 한다: " + messages.get(0));
        assertTrue(messages.get(0).contains("주문 처리 시작"),
                "전달받은 메시지가 로그에 있어야 한다: " + messages.get(0));
        assertEquals(Level.DEBUG, appender.events().get(0).getLevel());
    }

    @Test
    public void testTodoWithNullClassDoesNotThrow() {
        assertDoesNotThrow(() -> new DefaultTraceHandler().todo(null, "클래스 없음"));

        List<String> messages = appender.messages();
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains("unknown"));
        assertTrue(messages.get(0).contains("클래스 없음"));
    }

    @Test
    public void testTodoWithNullMessageDoesNotThrow() {
        assertDoesNotThrow(() -> new DefaultTraceHandler().todo(DefaultTraceHandlerTest.class, null));

        assertEquals(1, appender.messages().size());
    }

    @Test
    public void testResolvedMessageReachesLogThroughLeaveaTrace() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("trace.message", Locale.KOREA, "trace {0} message");

        DefaultTraceHandleManager manager = new DefaultTraceHandleManager();
        manager.setPatterns(new String[] {"**"});
        manager.setHandlers(new TraceHandler[] {new DefaultTraceHandler()});
        LeaveaTrace trace = new LeaveaTrace();
        trace.setTraceHandlerServices(new TraceHandlerService[] {manager});

        trace.trace(DefaultTraceHandlerTest.class, messageSource, "trace.message", new Object[] {"resolved"}, Locale.KOREA);

        List<String> messages = appender.messages();
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains("trace resolved message"),
                "메시지 키로 해석된 메시지가 기본 핸들러의 로그에 남아야 한다: " + messages.get(0));
        assertTrue(messages.get(0).contains(DefaultTraceHandlerTest.class.getName()));
    }

    /** 로그 이벤트를 모아 두는 테스트용 Appender. */
    private static final class CapturingAppender extends AbstractAppender {

        private final List<LogEvent> events = Collections.synchronizedList(new ArrayList<>());

        private CapturingAppender() {
            super("capturing-trace", null, null, true, null);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }

        private List<LogEvent> events() {
            return new ArrayList<>(events);
        }

        private List<String> messages() {
            List<String> messages = new ArrayList<>();
            for (LogEvent event : events()) {
                messages.add(event.getMessage().getFormattedMessage());
            }
            return messages;
        }
    }
}
