package org.egovframe.rte.fdl.filehandling;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * VFS 초기화 실패 가드와 rm/cp/mv 의 실패 로그, deprecated API 의 기존 동작 유지를 검증한다.
 */
@SuppressWarnings("deprecation")
public class EgovFileUtilInitGuardTest {

    private static final String LOGGER_NAME = EgovFileUtil.class.getName();

    @TempDir
    Path tempDir;

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
    public void testCallsAfterFailedInitializationThrowIllegalStateInsteadOfNpe() throws Exception {
        Object savedManager = readStatic("manager");
        try {
            writeStatic("manager", null);

            IllegalStateException e = assertThrows(IllegalStateException.class, () -> EgovFileUtil.rm("nothing.txt"));
            assertTrue(e.getMessage().contains("not initialized"), e.getMessage());
            assertTrue(e.getMessage().contains("EgovFiles"), "대체 경로를 안내해야 한다: " + e.getMessage());
            assertThrows(IllegalStateException.class, () -> EgovFileUtil.touch("nothing.txt"));
            assertThrows(IllegalStateException.class, () -> EgovFileUtil.cp("a.txt", "b.txt"));
            assertThrows(IllegalStateException.class, () -> EgovFileUtil.mv("a.txt", "b.txt"));
        } finally {
            writeStatic("manager", savedManager);
        }
    }

    @Test
    public void testMissingBaseDirectoryIsReportedExplicitly() throws Exception {
        Object savedBasefile = readStatic("basefile");
        try {
            writeStatic("basefile", null);

            IllegalStateException e = assertThrows(IllegalStateException.class, EgovFileUtil::pwd);
            assertTrue(e.getMessage().contains("base directory"), e.getMessage());
        } finally {
            writeStatic("basefile", savedBasefile);
        }
    }

    @Test
    public void testCopyFailureIsLoggedAtWarnWithoutException() {
        String source = tempDir.resolve("missing-source.txt").toAbsolutePath().toString();
        String target = tempDir.resolve("copied.txt").toAbsolutePath().toString();

        assertDoesNotThrow(() -> EgovFileUtil.cp(source, target), "기존 계약: 실패해도 예외를 던지지 않는다");

        assertFalse(Files.exists(tempDir.resolve("copied.txt")));
        assertTrue(appender.hasWarnContaining("EgovFileUtil.cp failed"), "실패가 WARN 으로 남아야 한다: " + appender.messages());
    }

    @Test
    public void testMoveFailureIsLoggedAtWarnWithoutException() {
        String source = tempDir.resolve("missing-source.txt").toAbsolutePath().toString();
        String target = tempDir.resolve("moved.txt").toAbsolutePath().toString();

        assertDoesNotThrow(() -> EgovFileUtil.mv(source, target), "기존 계약: 실패해도 예외를 던지지 않는다");

        assertFalse(Files.exists(tempDir.resolve("moved.txt")));
        assertTrue(appender.hasWarnContaining("EgovFileUtil.mv failed"), "실패가 WARN 으로 남아야 한다: " + appender.messages());
    }

    @Test
    public void testDeprecatedApisKeepTheirBehavior() throws Exception {
        // grep 은 패턴에 맞는 줄이 아니라 패턴으로 나눈 조각을 돌려준다(기존 동작 유지)
        List<String> fragments = EgovFileUtil.grep(new Object[] {"a1b22c"}, "\\d{1,2}");
        assertEquals(List.of("[a", "b", "c]"), fragments);

        // ls 는 항상 빈 리스트를 돌려준다(기존 동작 유지)
        Path dir = Files.createDirectory(tempDir.resolve("ls-dir"));
        Files.writeString(dir.resolve("one.txt"), "1");
        List<?> listed = new EgovFileUtil().ls(new String[] {"ls", dir.toAbsolutePath().toString()});
        assertTrue(listed.isEmpty());
    }

    private static Object readStatic(String name) throws Exception {
        Field field = EgovFileUtil.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private static void writeStatic(String name, Object value) throws Exception {
        Field field = EgovFileUtil.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    /** 로그 이벤트를 모아 두는 테스트용 Appender. */
    private static final class CapturingAppender extends AbstractAppender {

        private final List<LogEvent> events = Collections.synchronizedList(new ArrayList<>());

        private CapturingAppender() {
            super("capturing-fileutil", null, null, true, null);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }

        private boolean hasWarnContaining(String text) {
            synchronized (events) {
                for (LogEvent event : events) {
                    if (event.getLevel() == Level.WARN && event.getMessage().getFormattedMessage().contains(text)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private List<String> messages() {
            List<String> messages = new ArrayList<>();
            synchronized (events) {
                for (LogEvent event : events) {
                    messages.add(event.getLevel() + " " + event.getMessage().getFormattedMessage());
                }
            }
            return messages;
        }
    }
}
