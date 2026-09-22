package org.egovframe.rte.fdl.cmmn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * EgovAbstractServiceImpl의 로거 생성 및 추적 로케일 전달 동작을 검증한다.
 * 
 * @author 컨티리뷰션팀 이백행
 * @since 2026-09-05
 * @version 5.0.6
 * @see
 *
 *      <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2026-09-05  이백행          [2026년 컨트리뷰션] 최초 생성
 *
 *      </pre>
 */
class EgovAbstractServiceImplTest {

	// 상위 클래스가 제공하는 로거는 실제 서비스 구현 클래스의 이름을 사용해야 한다.
	@Test
	void loggerUsesConcreteServiceClass() {
		TestService service = new TestService();

		assertEquals(TestService.class.getName(), service.logger().getName());
	}

	// 로케일을 생략한 추적 호출은 현재 스레드의 LocaleContext를 사용해야 한다.
	@Test
	void leaveaTraceUsesLocaleFromCurrentContext() {
		TestService service = new TestService();
		CapturingLeaveaTrace trace = new CapturingLeaveaTrace();
		ReflectionTestUtils.setField(service, "traceObj", trace);
		Locale locale = Locale.KOREA;
		LocaleContextHolder.setLocale(locale);

		try {
			service.trace("trace.message");

			assertEquals(locale, trace.locale);
		} finally {
			LocaleContextHolder.resetLocaleContext();
		}
	}

	// protected API를 테스트하기 위한 최소 서비스 구현체이다.
	private static class TestService extends EgovAbstractServiceImpl {

		private Logger logger() {
			return egovLogger;
		}

		private void trace(String msgKey) {
			leaveaTrace(msgKey);
		}
	}

	// LeaveaTrace에 전달된 로케일만 기록하는 테스트 대역이다.
	private static class CapturingLeaveaTrace extends LeaveaTrace {

		private Locale locale;

		@Override
		public void trace(Class<?> clazz, MessageSource messageSource, String messageKey, Object[] messageParameters,
				Locale locale, Logger logger) {
			this.locale = locale;
		}
	}
}
