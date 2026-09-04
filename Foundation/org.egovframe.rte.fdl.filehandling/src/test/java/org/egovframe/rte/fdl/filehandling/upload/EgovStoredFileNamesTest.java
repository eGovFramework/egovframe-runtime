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
package org.egovframe.rte.fdl.filehandling.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovStoredFileNames} 단위 테스트.
 *
 * <p>일반 동작 검증과 함께 <b>공통컴포넌트 원본(EgovFileMngUtil·EgovStringUtil 의
 * getTimeStamp)의 결함을 회귀로 고정</b>한다 — 12시간제로 오전·오후를 구분하지 못하는 문제,
 * 시각만으로 이름을 만들어 동시 업로드가 덮어써지는 문제, 이름이 예측 가능한 문제.</p>
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
class EgovStoredFileNamesTest {

	/** 파일명 널 바이트 — 소스에 리터럴로 두지 않는다. */
	private static final String NUL = String.valueOf((char) 0x00);

	@Nested
	@DisplayName("원본 결함 회귀 — 공통컴포넌트 getTimeStamp 라면 실패한다")
	class LegacyDefectRegression {

		@Test
		@DisplayName("연속 생성해도 이름이 겹치지 않는다 (원본은 같은 밀리초면 같은 이름 → 덮어쓰기)")
		void 대량_생성_충돌_없음() {
			Set<String> names = new HashSet<>();
			for (int i = 0; i < 10000; i++) {
				names.add(EgovStoredFileNames.generate("a.png"));
			}
			assertEquals(10000, names.size(), "생성한 수만큼 서로 다른 이름이어야 한다");
		}

		@Test
		@DisplayName("이름에 시각이 드러나지 않는다 (원본은 yyyyMMddhhmmssSSS 라 추측이 가능했다)")
		void 시각_비노출() {
			String stored = EgovStoredFileNames.generate("a.png");
			String base = stored.substring(0, stored.indexOf('.'));
			String year = String.valueOf(java.time.Year.now().getValue());

			assertFalse(base.startsWith(year), "이름이 연도로 시작하면 시각 기반이라는 뜻이다");
			assertEquals(32, base.length());
		}

		@Test
		@DisplayName("12시간제 충돌이 구조적으로 불가능하다 — 오전·오후가 같은 이름이 될 수 없다")
		void 오전_오후_구분_불필요() {
			// 원본은 hh(1~12) 를 써서 09:00 과 21:00 이 같은 문자열이 됐다.
			// 저장명이 시각에 의존하지 않으므로 이 문제 자체가 성립하지 않는다.
			Set<String> names = new HashSet<>();
			for (int i = 0; i < 100; i++) {
				names.add(EgovStoredFileNames.generateWithoutExtension());
			}
			assertEquals(100, names.size());
		}

		@Test
		@DisplayName("원본 파일명이 저장명에 남지 않는다 — 경로·특수문자가 섞여 들어갈 여지가 없다")
		void 원본명_비반영() {
			String stored = EgovStoredFileNames.generate("../../etc/passwd.png");

			assertFalse(stored.contains(".."));
			assertFalse(stored.contains("/"));
			assertFalse(stored.contains("passwd"));
			assertTrue(stored.endsWith(".png"));
		}
	}

	@Nested
	@DisplayName("저장명 생성")
	class Generate {

		@Test
		@DisplayName("하이픈 없는 32자 + 확장자")
		void 기본_형식() {
			String stored = EgovStoredFileNames.generate("보고서.pdf");

			assertTrue(stored.matches("[0-9a-f]{32}\\.pdf"), "실제 값: " + stored);
		}

		@Test
		@DisplayName("확장자가 없으면 붙이지 않는다")
		void 확장자_없는_원본() {
			String stored = EgovStoredFileNames.generate("README");

			assertTrue(stored.matches("[0-9a-f]{32}"), "실제 값: " + stored);
		}

		@Test
		@DisplayName("null·빈 문자열도 예외 없이 처리한다")
		void null_허용() {
			assertTrue(EgovStoredFileNames.generate(null).matches("[0-9a-f]{32}"));
			assertTrue(EgovStoredFileNames.generate("").matches("[0-9a-f]{32}"));
		}

		@Test
		@DisplayName("확장자 없는 형태를 따로 만들 수 있다")
		void 확장자_제외_생성() {
			assertTrue(EgovStoredFileNames.generateWithoutExtension().matches("[0-9a-f]{32}"));
		}

		@Test
		@DisplayName("UUID 를 주입하면 그 값이 쓰인다 — UUIDv7 로 시간 정렬이 가능하다")
		void uuid_주입() {
			UUID uuid = UUID.fromString("018f1a2b-3c4d-7e5f-8a9b-0c1d2e3f4a5b");

			assertEquals("018f1a2b3c4d7e5f8a9b0c1d2e3f4a5b.pdf",
					EgovStoredFileNames.generate("a.pdf", uuid));
		}

		@Test
		@DisplayName("UUID 가 null 이면 즉시 실패한다")
		void uuid_null_거부() {
			assertThrows(NullPointerException.class,
					() -> EgovStoredFileNames.generate("a.pdf", null));
		}
	}

	@Nested
	@DisplayName("확장자 정규화")
	class ExtensionNormalization {

		@Test
		@DisplayName("대문자는 소문자로")
		void 소문자화() {
			assertEquals("pdf", EgovStoredFileNames.extensionOf("보고서.PDF"));
			assertTrue(EgovStoredFileNames.generate("a.PNG").endsWith(".png"));
		}

		@Test
		@DisplayName("경로가 붙어 있어도 확장자만 뽑는다")
		void 경로_제거() {
			assertEquals("png", EgovStoredFileNames.extensionOf("C:\\tmp\\a.png"));
			assertEquals("png", EgovStoredFileNames.extensionOf("/home/me/a.png"));
		}

		@Test
		@DisplayName("영숫자가 아닌 문자가 섞이면 확장자로 인정하지 않는다")
		void 비영숫자_거부() {
			assertEquals("", EgovStoredFileNames.extensionOf("a.p g"));
			assertEquals("", EgovStoredFileNames.extensionOf("a.pn" + NUL + "g"));
			assertEquals("", EgovStoredFileNames.extensionOf("a.p\"g"));
		}

		@Test
		@DisplayName("비정상적으로 긴 확장자는 인정하지 않는다")
		void 과도한_길이_거부() {
			String longExt = "a".repeat(21);

			assertEquals("", EgovStoredFileNames.extensionOf("file." + longExt));
			assertEquals("a".repeat(20), EgovStoredFileNames.extensionOf("file." + "a".repeat(20)));
		}

		@Test
		@DisplayName("이중 확장자는 마지막만 쓴다")
		void 이중_확장자() {
			assertEquals("png", EgovStoredFileNames.extensionOf("shell.jsp.png"));
		}

		@Test
		@DisplayName("점으로 끝나면 확장자가 없다")
		void 점으로_끝남() {
			assertEquals("", EgovStoredFileNames.extensionOf("noext."));
		}

		@Test
		@DisplayName("한글 확장자는 인정하지 않는다 — 저장명은 ASCII 로 유지한다")
		void 한글_확장자_거부() {
			assertEquals("", EgovStoredFileNames.extensionOf("파일.한글"));
		}
	}
}
