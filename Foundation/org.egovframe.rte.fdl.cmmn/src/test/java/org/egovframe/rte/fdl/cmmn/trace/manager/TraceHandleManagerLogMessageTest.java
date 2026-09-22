package org.egovframe.rte.fdl.cmmn.trace.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TraceHandleManagerLogMessageTest {

    // 진입 로그는 로거가 달린 클래스와 실행 중인 메소드를 가리켜야 한다.
    @Test
    public void testDefaultManagerLogsItsOwnEntry() {
        List<String> messages = capture(DefaultTraceHandleManager.class,
                () -> new DefaultTraceHandleManager().trace(TraceHandleManagerLogMessageTest.class, "message"));

        assertTrue(messages.contains(" DefaultTraceHandleManager.trace() "),
                "DefaultTraceHandleManager 로거가 남긴 진입 로그가 자기 클래스·메소드를 가리켜야 한다. 실제 = " + messages);
    }

    // 사용자가 상속하는 확장점의 기본 구현도 같은 규칙을 따라야 한다.
    @Test
    public void testAbstractManagerLogsItsOwnEntry() {
        List<String> messages = capture(AbstractTraceHandleManager.class,
                () -> new AbstractTraceHandleManager() {
                }.trace(TraceHandleManagerLogMessageTest.class, "message"));

        assertTrue(messages.contains(" AbstractTraceHandleManager.trace() "),
                "AbstractTraceHandleManager 로거가 남긴 진입 로그가 자기 클래스·메소드를 가리켜야 한다. 실제 = " + messages);
    }

    private List<String> capture(Class<?> loggerOwner, Runnable action) {
        List<String> messages = new ArrayList<>();
        AbstractAppender appender = new AbstractAppender("capture", null, null, true, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                messages.add(event.getMessage().getFormattedMessage());
            }
        };
        appender.start();

        Logger logger = (Logger) LogManager.getLogger(loggerOwner);
        logger.addAppender(appender);
        try {
            action.run();
        } finally {
            logger.removeAppender(appender);
            appender.stop();
        }
        return messages;
    }

}
