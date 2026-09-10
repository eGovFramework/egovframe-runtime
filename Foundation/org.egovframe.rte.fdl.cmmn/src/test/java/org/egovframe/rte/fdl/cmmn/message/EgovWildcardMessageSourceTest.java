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

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovWildcardMessageSource} 단위 테스트.
 *
 * <p>일반 동작 검증과 함께 <b>공통컴포넌트 원본의 결함을 회귀로 고정</b>한다 —
 * 로케일 접미가 없는 파일에서 나던 예외, 파일명의 밑줄을 모두 로케일 구분자로 보던 문제,
 * 입출력 오류를 debug 로그로 삼키던 문제.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (A-23)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
class EgovWildcardMessageSourceTest {

	private EgovWildcardMessageSource messageSource;

	@BeforeEach
	void setUp() {
		messageSource = new EgovWildcardMessageSource();
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setFallbackToSystemLocale(false);
	}

	@Nested
	@DisplayName("원본 결함 회귀 — 공통컴포넌트 구현이라면 실패한다")
	class LegacyDefectRegression {

		@Test
		@DisplayName("로케일 접미가 없는 파일에서 예외가 나지 않는다 (원본은 substring(0, -1))")
		void 로케일_없는_파일() {
			// common.properties 처럼 접미가 없는 파일이 섞여 있어도 확장이 끝까지 진행된다.
			messageSource.setBasenames("classpath*:/egov-wildcard-test/*.properties");

			assertEquals("default", messageSource.getMessage("greeting", null, Locale.FRENCH));
		}

		@Test
		@DisplayName("파일명의 밑줄을 로케일로 오인하지 않는다 (원본은 user_info_ko → user)")
		void 밑줄_포함_파일명_보존() {
			List<String> basenames = messageSource.expand("classpath*:/egov-wildcard-test/*.properties");

			assertTrue(basenames.contains("classpath:/egov-wildcard-test/user_info"),
					"실제 값: " + basenames);
			assertFalse(basenames.contains("classpath:/egov-wildcard-test/user"),
					"user 로 잘리면 메시지를 찾지 못한다");
		}

		@Test
		@DisplayName("해석할 수 없는 패턴은 조용히 넘어가지 않는다 (원본은 debug 로그로 삼켰다)")
		void 입출력_오류_비침묵() {
			// 존재하지 않는 스킴은 IOException 을 유발한다.
			assertThrows(UncheckedIOException.class,
					() -> messageSource.setBasenames("nosuchscheme://*.properties"));
		}
	}

	@Nested
	@DisplayName("패턴 확장")
	class Expansion {

		@Test
		@DisplayName("로케일별 파일이 하나의 basename 으로 합쳐진다")
		void 로케일_병합() {
			List<String> basenames = messageSource.expand("classpath*:/egov-wildcard-test/*.properties");

			long commonCount = basenames.stream()
					.filter(b -> b.equals("classpath:/egov-wildcard-test/common")).count();
			assertEquals(1, commonCount, "common_ko·common_en·common 이 하나로 모여야 한다");
		}

		@Test
		@DisplayName("하위 디렉터리까지 훑는다")
		void 하위_디렉터리() {
			List<String> basenames = messageSource.expand("classpath*:/egov-wildcard-test/**/*.properties");

			assertTrue(basenames.contains("classpath:/egov-wildcard-test/sub/board"),
					"실제 값: " + basenames);
		}

		@Test
		@DisplayName("확장 결과로 실제 메시지를 찾을 수 있다")
		void 메시지_조회() {
			messageSource.setBasenames("classpath*:/egov-wildcard-test/**/*.properties");

			assertEquals("안녕하세요", messageSource.getMessage("greeting", null, Locale.KOREAN));
			assertEquals("Hello", messageSource.getMessage("greeting", null, Locale.ENGLISH));
			assertEquals("게시판", messageSource.getMessage("board.title", null, Locale.KOREAN));
			assertEquals("이름", messageSource.getMessage("user.name", null, Locale.KOREAN));
		}

		@Test
		@DisplayName("접두 디렉터리 이름이 하위 경로에 반복돼도 메시지가 유실되지 않는다")
		void 접두_디렉터리_이름_반복() {
			// dupdir/sub/dupdir/nested.properties — 상대 경로를 마지막 일치로 구하면 안쪽 dupdir 을 잡아
			// basename 이 classpath:/dupdir/nested 로 틀어지고 메시지가 조용히 사라진다
			messageSource.setBasenames("classpath*:/dupdir/**/*.properties");

			assertEquals("nested-ok", messageSource.getMessage("nested.key", null, Locale.ROOT));
		}

		@Test
		@DisplayName("패턴이 아닌 basename 은 그대로 전달된다 — 기존 설정과 섞어 쓸 수 있다")
		void 일반_basename_공존() {
			messageSource.setBasenames(
					"classpath:/message/egovframework-fdl-exception-message",
					"classpath*:/egov-wildcard-test/*.properties");

			assertEquals("안녕하세요", messageSource.getMessage("greeting", null, Locale.KOREAN));
		}

		@Test
		@DisplayName("일치하는 리소스가 없으면 빈 목록")
		void 매칭_없음() {
			assertTrue(messageSource.expand("classpath*:/egov-nothing-here/*.properties").isEmpty());
		}

		@Test
		@DisplayName("null·빈 항목은 건너뛴다")
		void null_항목() {
			messageSource.setBasenames(null, "", "  ", "classpath*:/egov-wildcard-test/*.properties");

			assertEquals("안녕하세요", messageSource.getMessage("greeting", null, Locale.KOREAN));
		}
	}

	@Nested
	@DisplayName("로케일 접미 판별")
	class LocaleSuffix {

		@Test
		@DisplayName("언어 코드 형태만 제거한다")
		void 언어_코드() {
			assertEquals("common", messageSource.stripLocaleSuffix("common_ko"));
			assertEquals("common", messageSource.stripLocaleSuffix("common_en"));
			assertEquals("common", messageSource.stripLocaleSuffix("common_ko_KR"));
			assertEquals("common", messageSource.stripLocaleSuffix("common_zh_Hans_CN"));
		}

		@Test
		@DisplayName("로케일이 아닌 밑줄은 보존한다")
		void 비로케일_밑줄() {
			assertEquals("user_info", messageSource.stripLocaleSuffix("user_info"));
			assertEquals("board_list", messageSource.stripLocaleSuffix("board_list"));
			assertEquals("user_info", messageSource.stripLocaleSuffix("user_info_ko"));
		}

		@Test
		@DisplayName("접미가 없으면 그대로")
		void 접미_없음() {
			assertEquals("messages", messageSource.stripLocaleSuffix("messages"));
		}
	}

	@Nested
	@DisplayName("접두 디렉터리 추출")
	class FixedDirectory {

		@Test
		@DisplayName("와일드카드 직전까지의 디렉터리를 잘라낸다")
		void 기본() {
			assertEquals("/messages/", messageSource.fixedDirectoryOf("classpath*:/messages/*.properties"));
			assertEquals("/messages/", messageSource.fixedDirectoryOf("classpath*:/messages/**/*.properties"));
			assertEquals("/msg/sub/", messageSource.fixedDirectoryOf("classpath*:/msg/sub/*.properties"));
		}

		@Test
		@DisplayName("선행 슬래시가 없어도 붙여 준다")
		void 슬래시_보정() {
			assertEquals("/messages/", messageSource.fixedDirectoryOf("classpath*:messages/*.properties"));
		}
	}
}
