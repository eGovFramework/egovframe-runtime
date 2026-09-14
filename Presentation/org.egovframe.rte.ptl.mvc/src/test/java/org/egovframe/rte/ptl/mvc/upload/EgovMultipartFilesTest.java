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
package org.egovframe.rte.ptl.mvc.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.egovframe.rte.fdl.filehandling.upload.EgovUploadPolicy;
import org.egovframe.rte.fdl.filehandling.upload.EgovUploadRejectedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@link EgovMultipartFiles} · {@link EgovValidatingMultipartResolver} 단위 테스트.
 *
 * <p>어댑터가 <b>규칙을 복제하지 않고</b> 정책에 위임하는지, 서블릿 자료형 특유의 문제
 * (브라우저가 붙여 보내는 경로·빈 파일 항목)를 제대로 다루는지 확인한다.</p>
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
class EgovMultipartFilesTest {

	private static EgovUploadPolicy policy() {
		return EgovUploadPolicy.builder()
				.allowExtensions("png", "pdf")
				.maxFileSize(1024)
				.maxFileCount(2)
				.build();
	}

	private static MockMultipartFile file(String name, String originalFilename, int size) {
		byte[] content = new byte[size];
		return new MockMultipartFile(name, originalFilename, "application/octet-stream", content);
	}

	@Nested
	@DisplayName("파일명 처리")
	class FileName {

		@Test
		@DisplayName("브라우저가 붙여 보낸 경로를 제거하고 이름만 남긴다")
		void 경로_제거() {
			assertEquals("사진.png",
					EgovMultipartFiles.fileNameOf(file("f", "C:\\Users\\me\\사진.png", 10)));
			assertEquals("사진.png",
					EgovMultipartFiles.fileNameOf(file("f", "/home/me/사진.png", 10)));
		}

		@Test
		@DisplayName("경로가 붙어 와도 정상 파일은 통과한다")
		void 경로_붙은_정상_파일_통과() {
			assertTrue(EgovMultipartFiles.check(policy(), file("f", "C:\\tmp\\a.png", 10)).isEmpty());
		}

		@Test
		@DisplayName("파일명이 null 이면 거부")
		void 파일명_null() {
			assertEquals(java.util.Optional.of(EgovUploadPolicy.Reason.NO_FILENAME),
					EgovMultipartFiles.check(policy(), file("f", null, 10)));
		}

		@Test
		@DisplayName("파일 자체가 null 이면 거부")
		void 파일_null() {
			assertEquals(java.util.Optional.of(EgovUploadPolicy.Reason.NO_FILENAME),
					EgovMultipartFiles.check(policy(), (MultipartFile) null));
		}
	}

	@Nested
	@DisplayName("여러 파일 검증")
	class MultipleFiles {

		@Test
		@DisplayName("전부 통과하면 예외 없음")
		void 전건_통과() {
			List<MultipartFile> files = Arrays.asList(file("a", "a.png", 10), file("b", "b.pdf", 20));

			EgovMultipartFiles.validateAll(policy(), files);
			assertTrue(EgovMultipartFiles.checkAll(policy(), files).isEmpty());
		}

		@Test
		@DisplayName("하나라도 위반이면 예외")
		void 일부_위반() {
			List<MultipartFile> files = Arrays.asList(file("a", "a.png", 10), file("b", "b.exe", 20));

			EgovUploadRejectedException ex = assertThrows(EgovUploadRejectedException.class,
					() -> EgovMultipartFiles.validateAll(policy(), files));
			assertEquals(EgovUploadPolicy.Reason.EXTENSION_NOT_ALLOWED, ex.getReason());
		}

		@Test
		@DisplayName("check 는 위반한 것만 골라 돌려준다")
		void 위반_목록() {
			MultipartFile ok = file("a", "a.png", 10);
			MultipartFile bad = file("b", "b.exe", 20);

			Map<MultipartFile, EgovUploadPolicy.Reason> violations =
					EgovMultipartFiles.checkAll(policy(), Arrays.asList(ok, bad));

			assertEquals(1, violations.size());
			assertEquals(EgovUploadPolicy.Reason.EXTENSION_NOT_ALLOWED, violations.get(bad));
			assertFalse(violations.containsKey(ok));
		}

		@Test
		@DisplayName("빈 파일 항목은 개수에서 제외한다 — 화면이 입력칸을 여러 개 두는 것이 보통이다")
		void 빈_항목_제외() {
			List<MultipartFile> files = Arrays.asList(
					file("a", "a.png", 10),
					file("b", "", 0),
					file("c", "", 0));

			assertEquals(1, EgovMultipartFiles.submittedOnly(files).size());
			EgovMultipartFiles.validateAll(policy(), files);
		}

		@Test
		@DisplayName("개수 초과는 파일별 사유가 아니라 요청 단위로 거부한다")
		void 개수_초과() {
			List<MultipartFile> files = Arrays.asList(
					file("a", "a.png", 10), file("b", "b.png", 10), file("c", "c.png", 10));

			EgovUploadRejectedException ex = assertThrows(EgovUploadRejectedException.class,
					() -> EgovMultipartFiles.validateAll(policy(), files));
			assertEquals(EgovUploadPolicy.Reason.COUNT_EXCEEDED, ex.getReason());

			Map<MultipartFile, EgovUploadPolicy.Reason> violations =
					EgovMultipartFiles.checkAll(policy(), files);
			assertEquals(EgovUploadPolicy.Reason.COUNT_EXCEEDED, violations.get(null));
		}

		@Test
		@DisplayName("null 목록은 빈 목록으로 다룬다")
		void null_목록() {
			EgovMultipartFiles.validateAll(policy(), null);
			assertTrue(EgovMultipartFiles.checkAll(policy(), null).isEmpty());
		}
	}

	@Nested
	@DisplayName("MultipartResolver 어댑터")
	class Resolver {

		@Test
		@DisplayName("정책 없이 만들 수 없다 — 설정 누락이 무검증으로 이어지지 않도록")
		void 정책_필수() {
			assertThrows(IllegalArgumentException.class,
					() -> new EgovValidatingMultipartResolver(null));
		}

		@Test
		@DisplayName("요청 전체 파일에 정책을 적용한다")
		void 요청_전체_적용() {
			EgovValidatingMultipartResolver resolver = new EgovValidatingMultipartResolver(policy());
			MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
			request.addFile(new MockMultipartFile("good", "a.png", null, "x".getBytes(StandardCharsets.UTF_8)));
			request.addFile(new MockMultipartFile("bad", "b.exe", null, "x".getBytes(StandardCharsets.UTF_8)));

			EgovUploadRejectedException ex = assertThrows(EgovUploadRejectedException.class,
					() -> resolver.applyPolicy(request));
			assertEquals(EgovUploadPolicy.Reason.EXTENSION_NOT_ALLOWED, ex.getReason());
		}

		@Test
		@DisplayName("허용된 파일만 있으면 통과한다")
		void 정상_요청_통과() {
			EgovValidatingMultipartResolver resolver = new EgovValidatingMultipartResolver(policy());
			MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
			request.addFile(new MockMultipartFile("f", "a.png", null, "x".getBytes(StandardCharsets.UTF_8)));

			resolver.applyPolicy(request);
		}

		@Test
		@DisplayName("적용 중인 정책을 확인할 수 있다")
		void 정책_조회() {
			EgovUploadPolicy p = policy();

			assertEquals(p, new EgovValidatingMultipartResolver(p).getPolicy());
		}

		@Test
		@DisplayName("거부 사유는 MultipartException 의 cause 로 전달된다")
		void 사유_전달_형태() {
			MultipartException wrapped = new MultipartException("rejected",
					new EgovUploadRejectedException(EgovUploadPolicy.Reason.SIZE_EXCEEDED, "a.png"));

			assertTrue(wrapped.getCause() instanceof EgovUploadRejectedException);
			assertEquals(EgovUploadPolicy.Reason.SIZE_EXCEEDED,
					((EgovUploadRejectedException) wrapped.getCause()).getReason());
		}

		@Test
		@DisplayName("파일이 없는 요청은 통과한다")
		void 파일_없는_요청() {
			EgovValidatingMultipartResolver resolver = new EgovValidatingMultipartResolver(policy());

			resolver.applyPolicy(new MockMultipartHttpServletRequest());
			assertTrue(EgovMultipartFiles.checkAll(policy(), Collections.emptyList()).isEmpty());
		}
	}
}
