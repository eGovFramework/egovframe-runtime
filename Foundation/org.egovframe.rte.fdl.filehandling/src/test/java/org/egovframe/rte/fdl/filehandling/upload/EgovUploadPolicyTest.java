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

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovUploadPolicy} 단위 테스트.
 *
 * <p>일반 동작 검증과 함께 <b>공통컴포넌트 원본의 결함을 회귀로 고정</b>한다 —
 * 화이트리스트 미설정 시 전부 허용(fail-open), 점 없는 파일명의 확장자 오판,
 * 경로 구분자 무시, 설정 오류의 침묵 폴백. 원본 구현이라면 실패할 단언들이다.</p>
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
class EgovUploadPolicyTest {

	/** 파일명 널 바이트 — 소스에 리터럴로 두지 않는다. */
	private static final String NUL = String.valueOf((char) 0x00);

	private static EgovUploadPolicy imagePolicy() {
		return EgovUploadPolicy.builder()
				.allowExtensions("png", "jpg", "pdf")
				.maxFileSize(1024)
				.maxFileCount(3)
				.build();
	}

	@Nested
	@DisplayName("원본 결함 회귀 — 공통컴포넌트 구현이라면 실패한다")
	class LegacyDefectRegression {

		@Test
		@DisplayName("화이트리스트 없이는 정책을 만들 수 없다 (원본은 미설정 시 전부 허용)")
		void 화이트리스트_필수_fail_closed() {
			EgovUploadPolicy.Builder builder = EgovUploadPolicy.builder().maxFileSize(1024);

			IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
			assertTrue(ex.getMessage().contains("at least one allowed extension"));
		}

		@Test
		@DisplayName("점 없는 파일명은 확장자 없음으로 판정한다 (원본은 파일명 전체를 확장자로 본다)")
		void 점_없는_파일명() {
			assertEquals(Optional.of(EgovUploadPolicy.Reason.NO_EXTENSION),
					imagePolicy().check("README", 100));
		}

		@Test
		@DisplayName("경로 구분자가 든 파일명을 거부한다 (원본은 'a.b/evil' 의 확장자를 'b/evil' 로 본다)")
		void 경로_구분자_거부() {
			assertEquals(Optional.of(EgovUploadPolicy.Reason.INVALID_FILENAME),
					imagePolicy().check("dir.d/evil.png", 100));
			assertEquals(Optional.of(EgovUploadPolicy.Reason.INVALID_FILENAME),
					imagePolicy().check("dir\\evil.png", 100));
		}

		@Test
		@DisplayName("파일명의 널 바이트를 거부한다")
		void 널바이트_거부() {
			assertEquals(Optional.of(EgovUploadPolicy.Reason.INVALID_FILENAME),
					imagePolicy().check("shell.jsp" + NUL + ".png", 100));
		}

		@Test
		@DisplayName("잘못된 설정은 정책 생성 시점에 즉시 실패한다 (원본은 파싱 실패를 기본값으로 삼켰다)")
		void 잘못된_설정은_즉시_실패() {
			assertThrows(IllegalArgumentException.class,
					() -> EgovUploadPolicy.builder().maxFileSize(0));
			assertThrows(IllegalArgumentException.class,
					() -> EgovUploadPolicy.builder().maxFileSize(-1));
			assertThrows(IllegalArgumentException.class,
					() -> EgovUploadPolicy.builder().maxFileCount(0));
		}

		@Test
		@DisplayName("확장자 비교 로직이 한 곳에만 있다 — 대소문자·점·공백을 같은 규칙으로 처리")
		void 확장자_정규화_일관() {
			EgovUploadPolicy policy = EgovUploadPolicy.builder()
					.allowExtensionList(".PNG, jpg ,  .Pdf")
					.build();

			assertEquals(3, policy.getAllowedExtensions().size());
			assertTrue(policy.check("a.png", 10).isEmpty());
			assertTrue(policy.check("a.PNG", 10).isEmpty());
			assertTrue(policy.check("a.pdf", 10).isEmpty());
		}
	}

	@Nested
	@DisplayName("확장자 검증")
	class ExtensionCheck {

		@Test
		@DisplayName("허용 목록에 있으면 통과")
		void 허용_확장자() {
			assertTrue(imagePolicy().check("사진.png", 100).isEmpty());
		}

		@Test
		@DisplayName("허용 목록에 없으면 거부")
		void 불허_확장자() {
			assertEquals(Optional.of(EgovUploadPolicy.Reason.EXTENSION_NOT_ALLOWED),
					imagePolicy().check("shell.jsp", 100));
		}

		@Test
		@DisplayName("이중 확장자는 마지막 확장자로 판정한다 — 서버의 실행 설정이 함께 잠겨야 한다")
		void 이중_확장자() {
			assertTrue(imagePolicy().check("shell.jsp.png", 100).isEmpty());
		}

		@Test
		@DisplayName("한글 파일명도 정상 처리")
		void 한글_파일명() {
			assertTrue(imagePolicy().check("2026년 사업계획.pdf", 100).isEmpty());
		}

		@Test
		@DisplayName("확장자가 없으면 거부")
		void 확장자_없음() {
			assertEquals(Optional.of(EgovUploadPolicy.Reason.NO_EXTENSION),
					imagePolicy().check("noext.", 100));
		}
	}

	@Nested
	@DisplayName("크기·개수 검증")
	class SizeAndCountCheck {

		@Test
		@DisplayName("한도 이하는 통과, 초과는 거부")
		void 크기_경계() {
			assertTrue(imagePolicy().check("a.png", 1024).isEmpty());
			assertEquals(Optional.of(EgovUploadPolicy.Reason.SIZE_EXCEEDED),
					imagePolicy().check("a.png", 1025));
		}

		@Test
		@DisplayName("0 바이트 파일은 기본 거부, 옵션으로 허용")
		void 빈_파일() {
			assertEquals(Optional.of(EgovUploadPolicy.Reason.EMPTY_FILE),
					imagePolicy().check("a.png", 0));

			EgovUploadPolicy allowEmpty = EgovUploadPolicy.builder()
					.allowExtensions("png").allowEmptyFile(true).build();
			assertTrue(allowEmpty.check("a.png", 0).isEmpty());
		}

		@Test
		@DisplayName("음수 크기는 호출부 오류로 거부")
		void 음수_크기() {
			assertEquals(Optional.of(EgovUploadPolicy.Reason.INVALID_SIZE),
					imagePolicy().check("a.png", -1));
		}

		@Test
		@DisplayName("개수 한도 경계")
		void 개수_경계() {
			assertTrue(imagePolicy().checkCount(3).isEmpty());
			assertEquals(Optional.of(EgovUploadPolicy.Reason.COUNT_EXCEEDED),
					imagePolicy().checkCount(4));
		}

		@Test
		@DisplayName("개수 제한을 두지 않을 수 있다")
		void 개수_무제한() {
			EgovUploadPolicy policy = EgovUploadPolicy.builder().allowExtensions("png").build();

			assertEquals(EgovUploadPolicy.UNLIMITED_COUNT, policy.getMaxFileCount());
			assertTrue(policy.checkCount(1000).isEmpty());
		}
	}

	@Nested
	@DisplayName("판정형과 예외형")
	class CheckAndValidate {

		@Test
		@DisplayName("check 는 예외를 던지지 않는다")
		void check_는_무예외() {
			assertFalse(imagePolicy().check(null, 100).isEmpty());
			assertFalse(imagePolicy().check("a.exe", 100).isEmpty());
		}

		@Test
		@DisplayName("validate 는 사유를 담은 예외를 던진다")
		void validate_는_사유_포함() {
			EgovUploadRejectedException ex = assertThrows(EgovUploadRejectedException.class,
					() -> imagePolicy().validate("shell.jsp", 100));

			assertEquals(EgovUploadPolicy.Reason.EXTENSION_NOT_ALLOWED, ex.getReason());
			assertEquals("shell.jsp", ex.getFileName());
		}

		@Test
		@DisplayName("예외 메시지에 파일명을 넣지 않는다 — 사용자 입력이 로그·응답에 그대로 실리지 않도록")
		void 예외_메시지에_파일명_없음() {
			EgovUploadRejectedException ex = assertThrows(EgovUploadRejectedException.class,
					() -> imagePolicy().validate("<script>evil</script>.jsp", 100));

			assertFalse(ex.getMessage().contains("script"));
		}

		@Test
		@DisplayName("정책은 불변이다")
		void 정책_불변() {
			EgovUploadPolicy policy = imagePolicy();

			assertThrows(UnsupportedOperationException.class,
					() -> policy.getAllowedExtensions().add("exe"));
		}
	}
}
