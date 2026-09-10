/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.fdl.cmmn.exception.handler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovLoggingExceptionHandler} 단위 테스트.
 *
 * <p>핵심은 <b>로거 이름이 발생 위치(packageName)로 잡히는지</b>다 — 공통컴포넌트 원본은
 * 핸들러 클래스 이름으로 로거를 고정해 업무별 로그 제어가 불가능했다. 실제 로그 이벤트를
 * 붙잡아 확인한다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
class EgovLoggingExceptionHandlerTest {

	private static final String TARGET_PACKAGE = "org.example.board.service";

	private EgovLoggingExceptionHandler handler;

	private CapturingAppender appender;

	private LoggerConfig loggerConfig;

	@BeforeEach
	void setUp() {
		handler = new EgovLoggingExceptionHandler();

		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration configuration = context.getConfiguration();
		appender = new CapturingAppender();
		appender.start();

		loggerConfig = new LoggerConfig(TARGET_PACKAGE, Level.DEBUG, false);
		loggerConfig.addAppender(appender, Level.DEBUG, null);
		configuration.addLogger(TARGET_PACKAGE, loggerConfig);
		context.updateLoggers();
	}

	@AfterEach
	void tearDown() {
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		context.getConfiguration().removeLogger(TARGET_PACKAGE);
		context.updateLoggers();
		appender.stop();
	}

	@Nested
	@DisplayName("원본 결함 회귀 — 공통컴포넌트 구현이라면 실패한다")
	class LegacyDefectRegression {

		@Test
		@DisplayName("로거 이름이 발생 위치가 된다 (원본은 핸들러 클래스로 고정)")
		void 로거_이름은_발생위치() {
			handler.occur(new IllegalStateException("실패"), TARGET_PACKAGE);

			List<LogEvent> events = appender.events();
			assertEquals(1, events.size(), "해당 패키지 로거로 기록돼야 잡힌다");
			assertEquals(TARGET_PACKAGE, events.get(0).getLoggerName());
		}

		@Test
		@DisplayName("발생 위치별로 로그 레벨을 조정할 수 있다")
		void 패키지별_레벨_제어() {
			loggerConfig.setLevel(Level.OFF);
			((LoggerContext) LogManager.getContext(false)).updateLoggers();

			handler.occur(new IllegalStateException("실패"), TARGET_PACKAGE);

			assertTrue(appender.events().isEmpty(), "OFF 로 내리면 기록되지 않아야 한다");
		}
	}

	@Nested
	@DisplayName("기록 동작")
	class Logging {

		@Test
		@DisplayName("예외가 스택트레이스와 함께 실린다")
		void 예외_전달() {
			IllegalStateException cause = new IllegalStateException("실패");

			handler.occur(cause, TARGET_PACKAGE);

			LogEvent event = appender.events().get(0);
			assertEquals(cause, event.getThrown());
			assertEquals(TARGET_PACKAGE, event.getMessage().getFormattedMessage());
		}

		@Test
		@DisplayName("기본 레벨은 ERROR")
		void 기본_레벨() {
			assertEquals(EgovLoggingExceptionHandler.Level.ERROR, handler.getLogLevel());

			handler.occur(new IllegalStateException(), TARGET_PACKAGE);

			assertEquals(Level.ERROR, appender.events().get(0).getLevel());
		}

		@Test
		@DisplayName("레벨을 낮출 수 있다 — 업무 예외는 WARN 이 적절할 때가 있다")
		void 레벨_변경() {
			handler.setLogLevel(EgovLoggingExceptionHandler.Level.WARN);

			handler.occur(new IllegalStateException(), TARGET_PACKAGE);

			assertEquals(Level.WARN, appender.events().get(0).getLevel());
		}

		@Test
		@DisplayName("레벨에 null 을 주면 ERROR 로 되돌린다")
		void 레벨_null() {
			handler.setLogLevel(EgovLoggingExceptionHandler.Level.DEBUG);
			handler.setLogLevel(null);

			assertEquals(EgovLoggingExceptionHandler.Level.ERROR, handler.getLogLevel());
		}
	}

	@Nested
	@DisplayName("방어적 계약")
	class Defensive {

		@Test
		@DisplayName("인자가 null 이어도 예외를 던지지 않는다 — 예외 처리 중에 또 터지면 원인이 묻힌다")
		void null_인자() {
			assertDoesNotThrow(() -> handler.occur(null, null));
			assertDoesNotThrow(() -> handler.occur(new IllegalStateException(), null));
			assertDoesNotThrow(() -> handler.occur(null, TARGET_PACKAGE));
		}

		@Test
		@DisplayName("packageName 이 비면 핸들러 이름으로 기록한다")
		void 빈_패키지명() {
			assertDoesNotThrow(() -> handler.occur(new IllegalStateException(), "   "));
			assertTrue(appender.events().isEmpty(), "대상 패키지 로거로는 가지 않는다");
		}
	}

	/** 로그 이벤트를 모아 두는 테스트용 Appender. */
	private static final class CapturingAppender extends AbstractAppender {

		private final List<LogEvent> events = Collections.synchronizedList(new ArrayList<>());

		private CapturingAppender() {
			super("capturing", null, null, true, null);
		}

		@Override
		public void append(LogEvent event) {
			events.add(event.toImmutable());
		}

		private List<LogEvent> events() {
			return new ArrayList<>(events);
		}
	}
}
