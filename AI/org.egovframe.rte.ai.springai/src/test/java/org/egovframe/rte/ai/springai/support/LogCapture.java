package org.egovframe.rte.ai.springai.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.egovframe.rte.ai.core.audit.EgovAiAudit;

/**
 * log4j 로거 캡처 — 감사 채널({@link #audit()})이나 클래스 로거의 메시지를 모은다. try-with-resources 로 붙였다 뗀다.
 */
public final class LogCapture implements AutoCloseable {

	private final Logger logger;
	private final AbstractAppender appender;
	private final List<LogEvent> events = new CopyOnWriteArrayList<>();

	public LogCapture(String loggerName) {
		this.logger = (Logger) LogManager.getLogger(loggerName);
		this.appender = new AbstractAppender("springai-capture-" + loggerName, null, null, true, Property.EMPTY_ARRAY) {
			@Override
			public void append(LogEvent event) {
				events.add(event.toImmutable());
			}
		};
		appender.start();
		logger.addAppender(appender);
	}

	public static LogCapture audit() {
		return new LogCapture(EgovAiAudit.LOGGER_NAME);
	}

	public static LogCapture of(Class<?> type) {
		return new LogCapture(type.getName());
	}

	public List<String> lines() {
		return events.stream().map(event -> event.getMessage().getFormattedMessage()).toList();
	}

	public List<LogEvent> events() {
		return events;
	}

	@Override
	public void close() {
		logger.removeAppender(appender);
		appender.stop();
	}
}
