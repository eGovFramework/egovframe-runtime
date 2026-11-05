package org.egovframe.rte.ai.core.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.egovframe.rte.ai.core.audit.EgovAiAudit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 실패 분류·폴백 계약 테스트 — cause 체인 순회·순환 방어·SPI 우선·키워드·문구 교체·감사 기록(스펙 ai/llm-response-handling).
 */
class EgovAiFallbackTest {

	private static final class CapturingAppender extends AbstractAppender {
		private final List<String> lines = new CopyOnWriteArrayList<>();

		CapturingAppender() {
			super("ai-audit-fallback", null, null, true, Property.EMPTY_ARRAY);
		}

		@Override
		public void append(LogEvent event) {
			lines.add(event.getMessage().getFormattedMessage());
		}
	}

	/** getCause 가 자기 자신을 돌려주는 순환 예외. */
	private static final class SelfCausedException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		SelfCausedException() {
			super("loop");
		}

		@Override
		public synchronized Throwable getCause() {
			return this;
		}
	}

	private Logger auditLogger;
	private CapturingAppender audit;

	@BeforeEach
	void attachAudit() {
		auditLogger = (Logger) LogManager.getLogger(EgovAiAudit.LOGGER_NAME);
		audit = new CapturingAppender();
		audit.start();
		auditLogger.addAppender(audit);
	}

	@AfterEach
	void detachAudit() {
		auditLogger.removeAppender(audit);
		audit.stop();
	}

	@Test
	@DisplayName("깊은 cause 의 소켓 시간 초과는 TIMEOUT, 연결 거부는 CONNECTION 으로 분류된다")
	void classifiesDeepCauses() {
		EgovAiFallback fallback = EgovAiFallback.defaults();
		RuntimeException deep = new RuntimeException("wrapper",
				new IllegalStateException("layer", new IOException("io", new SocketTimeoutException("read timed out"))));
		assertEquals(EgovAiErrorType.TIMEOUT, fallback.classify(deep));
		assertEquals(EgovAiErrorType.CONNECTION, fallback.classify(new RuntimeException("x", new ConnectException("Connection refused"))));
		assertEquals(EgovAiErrorType.GENERAL, fallback.classify(new IllegalArgumentException("bad model")));
		assertEquals(EgovAiErrorType.GENERAL, fallback.classify(null));
	}

	@Test
	@DisplayName("메시지 키워드로 토큰 한도·일시적 오류를 분류한다")
	void classifiesByKeywords() {
		EgovAiFallback fallback = EgovAiFallback.defaults();
		assertEquals(EgovAiErrorType.TOKEN_LIMIT, fallback.classify(new RuntimeException("This model's maximum context length is 4096 tokens")));
		assertEquals(EgovAiErrorType.TRANSIENT, fallback.classify(new RuntimeException("429 Too Many Requests")));
		assertEquals(EgovAiErrorType.TIMEOUT, fallback.classify(new RuntimeException("request timed out")));
		assertEquals(EgovAiErrorType.GENERAL, fallback.classify(new RuntimeException("invalid token in header")), "'token' 단어 하나로는 토큰 한도가 아니다");
	}

	@Test
	@DisplayName("자기 참조 cause 는 무한 순회 없이 일반으로 분류된다")
	void cyclicCauseTerminates() {
		assertEquals(EgovAiErrorType.GENERAL, EgovAiFallback.defaults().classify(new SelfCausedException()));
	}

	@Test
	@DisplayName("등록한 SPI 분류기가 기본 분류보다 우선한다")
	void spiClassifierTakesPrecedence() {
		EgovAiErrorClassifier frameworkClassifier = t -> t instanceof UnsupportedOperationException
				? Optional.of(EgovAiErrorType.TRANSIENT) : Optional.empty();
		EgovAiFallback fallback = EgovAiFallback.builder().classifier(frameworkClassifier).build();
		assertEquals(EgovAiErrorType.TRANSIENT, fallback.classify(new UnsupportedOperationException("rate limited by provider")));
		assertEquals(EgovAiErrorType.TIMEOUT, fallback.classify(new SocketTimeoutException()), "SPI 가 empty 면 기본 분류");
	}

	@Test
	@DisplayName("유형별 문구는 기본 제공되고 교체할 수 있으며, 폴백 문구 생성은 감사 채널에 유형·예외 클래스만 남긴다")
	void messagesAndAudit() {
		EgovAiFallback fallback = EgovAiFallback.builder()
				.message(EgovAiErrorType.TIMEOUT, "응답이 지연되고 있습니다. 민원콜센터 120 으로 문의하십시오.").build();
		assertEquals("응답이 지연되고 있습니다. 민원콜센터 120 으로 문의하십시오.", fallback.message(EgovAiErrorType.TIMEOUT));
		assertEquals(EgovAiErrorType.GENERAL.defaultMessage(), fallback.message(EgovAiErrorType.GENERAL));

		String message = fallback.message(new SocketTimeoutException("read timed out from http://user:secret@host"));
		assertTrue(message.contains("민원콜센터"));
		assertEquals(1, audit.lines.size());
		assertEquals("event=call-failure type=TIMEOUT error=SocketTimeoutException", audit.lines.get(0));
		assertFalse(audit.lines.get(0).contains("secret"));
		assertEquals(EgovAiErrorType.GENERAL.defaultMessage(), fallback.message((Throwable) null));
		assertThrows(IllegalArgumentException.class, () -> EgovAiFallback.builder().message(EgovAiErrorType.GENERAL, " "));
	}
}
