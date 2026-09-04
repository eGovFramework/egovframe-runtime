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
package org.egovframe.rte.fdl.cmmn.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.NoSuchMessageException;

/**
 * {@link EgovWildcardMessageSource} 의 <b>신뢰할 수 없는 입력</b>(메시지 인자·코드·로케일(Locale))에 대한 계약 고정.
 *
 * <p>메시지 인자와 코드는 화면 파라미터에서, 로케일은 {@code Accept-Language} 에서 그대로 오기 쉽다.
 * 인자는 값으로만 쓰이고 패턴으로 해석되지 않아야 하며, 모르는 코드가 응답에 반사되지 않아야 하고,
 * 어떤 로케일에도 예외 없이 기본 메시지로 응답해야 한다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.07  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
class EgovWildcardMessageSourceSecurityTest {

	private EgovWildcardMessageSource messageSource;

	@BeforeEach
	void setUp() {
		messageSource = new EgovWildcardMessageSource();
		messageSource.setDefaultEncoding("UTF-8");
		// 실행 환경의 기본 로케일에 따라 결과가 달라지지 않도록 시스템 로케일 폴백을 끈다
		messageSource.setFallbackToSystemLocale(false);
		messageSource.setBasenames("classpath*:/egov-wildcard-security-test/*.properties");
	}

	@Test
	@DisplayName("인자는 값으로만 쓰이고 MessageFormat 패턴으로 해석되지 않는다")
	void 인자는_패턴이_아니다() {
		for (String hostile : List.of("{1}", "'", "{", "{0}", "</script><script>alert(1)</script>")) {
			String message = messageSource.getMessage("hello", new Object[] {hostile}, Locale.KOREA);
			assertTrue(message.startsWith(hostile + " 님"), "인자가 그대로 값으로 들어가야 한다: " + message);
		}
		assertEquals("It's X", messageSource.getMessage("quote", new Object[] {"X"}, Locale.ENGLISH));
	}

	@Test
	@DisplayName("모르는 코드는 기본 설정에서 반사되지 않는다 — 예외이거나 호출자가 준 기본값이다")
	void 모르는_코드는_반사되지_않는다() {
		String hostileCode = "<script>alert(1)</script>";

		assertThrows(NoSuchMessageException.class, () -> messageSource.getMessage(hostileCode, null, Locale.KOREA));
		assertThrows(NoSuchMessageException.class, () -> messageSource.getMessage("../../../etc/passwd", null, Locale.KOREA));
		assertEquals("기본값", messageSource.getMessage(hostileCode, null, "기본값", Locale.KOREA));
		assertEquals("기본값", messageSource.getMessage("x".repeat(5000), null, "기본값", Locale.KOREA));
	}

	@Test
	@DisplayName("비정상 로케일에도 예외 없이 기본 메시지로 응답한다")
	void 비정상_로케일() {
		List<Locale> hostile = List.of(
				new Locale("x".repeat(300)),
				Locale.forLanguageTag("aa-Latn-XX-1234-x-yz"),
				new Locale("../../../leak"),
				Locale.forLanguageTag("../../x"),
				Locale.ROOT);
		for (Locale locale : hostile) {
			String message = messageSource.getMessage("plain", null, locale);
			assertEquals("no-args", message, "기본 메시지: " + locale);
		}
		assertFalse(messageSource.getMessage("plain", null, Locale.KOREA).isEmpty());
	}
}
