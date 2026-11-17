package org.egovframe.rte.ai.core.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AI 감사 채널 계약 테스트 — 채널 이름·키=값 한 줄 형식·건수만 기록·값 미기록(스펙 ai/audit-channel).
 */
class EgovAiAuditTest {

	private static final class CapturingAppender extends AbstractAppender {
		private final List<LogEvent> events = new CopyOnWriteArrayList<>();

		CapturingAppender() {
			super("ai-audit", null, null, true, Property.EMPTY_ARRAY);
		}

		@Override
		public void append(LogEvent event) {
			events.add(event.toImmutable());
		}

		String line(int index) {
			return events.get(index).getMessage().getFormattedMessage();
		}
	}

	private Logger auditLogger;
	private CapturingAppender audit;

	@BeforeEach
	void attach() {
		auditLogger = (Logger) LogManager.getLogger(EgovAiAudit.LOGGER_NAME);
		audit = new CapturingAppender();
		audit.start();
		auditLogger.addAppender(audit);
	}

	@AfterEach
	void detach() {
		auditLogger.removeAppender(audit);
		audit.stop();
	}

	@Test
	@DisplayName("채널 이름은 EGOV_AI_AUDIT 이고 가드 사건은 차단이면 WARN·허용이면 INFO 로 한 줄이다")
	void guardEvents() {
		assertEquals("EGOV_AI_AUDIT", EgovAiAudit.LOGGER_NAME);
		EgovAiAudit.guard("block", false, "이전 지시 무시");
		EgovAiAudit.guard("log", true, "line\nbreak");
		assertEquals("event=guard policy=block allowed=false pattern=이전 지시 무시", audit.line(0));
		assertEquals(Level.WARN, audit.events.get(0).getLevel());
		assertEquals("event=guard policy=log allowed=true pattern=line break", audit.line(1), "개행은 공백으로");
		assertEquals(Level.INFO, audit.events.get(1).getLevel());
	}

	@Test
	@DisplayName("마스킹 사건은 유형별 건수와 총계만 남기고 전부 0 이면 발신하지 않는다")
	void maskingEvents() {
		EgovAiAudit.masking(Map.of("RRN", 2, "CARD", 1, "SECRET", 0, "ACCOUNT", 0));
		assertEquals("event=masking account=0 card=1 rrn=2 secret=0 total=3", audit.line(0));
		EgovAiAudit.masking(Map.of("RRN", 0));
		EgovAiAudit.masking(Map.of());
		EgovAiAudit.masking(null);
		assertEquals(1, audit.events.size());
	}

	@Test
	@DisplayName("호출 실패 사건은 유형과 예외 클래스만 남긴다(메시지·값 미기록)")
	void callFailureEvents() {
		EgovAiAudit.callFailure("TIMEOUT", new java.net.SocketTimeoutException("http://user:pw@llm/api?key=abc"));
		EgovAiAudit.callFailure("GENERAL", null);
		assertEquals("event=call-failure type=TIMEOUT error=SocketTimeoutException", audit.line(0));
		assertFalse(audit.line(0).contains("key=abc"));
		assertEquals("event=call-failure type=GENERAL error=unknown", audit.line(1));
		assertTrue(audit.events.stream().allMatch(e -> e.getLoggerName().equals(EgovAiAudit.LOGGER_NAME)));
	}
}
