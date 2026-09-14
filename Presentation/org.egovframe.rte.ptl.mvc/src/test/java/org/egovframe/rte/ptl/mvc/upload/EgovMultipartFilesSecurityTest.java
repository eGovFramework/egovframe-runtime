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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.egovframe.rte.fdl.filehandling.upload.EgovUploadPolicy;
import org.egovframe.rte.fdl.filehandling.upload.EgovUploadRejectedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.mock.web.MockPart;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@link EgovMultipartFiles} · {@link EgovValidatingMultipartResolver} 의 <b>정책 우회</b> 회귀 검증.
 *
 * <p>정책 자체({@link EgovUploadPolicy})의 확장자 우회 배터리는 정책 쪽 테스트가 맡는다. 이 클래스는
 * <b>웹 계층 어댑터가 정책을 건너뛰는 틈</b>을 막는다 — 크기 0 이라는 이유로 정책에 보이지 않는 파일,
 * content-type 에 기대는 판정, 경로가 섞인 파일명, 거부 메시지의 파일명 반사.</p>
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
class EgovMultipartFilesSecurityTest {

	private static final String BS = String.valueOf((char) 92);
	private static final String NUL = String.valueOf((char) 0);

	private static EgovUploadPolicy imagePolicy() {
		return EgovUploadPolicy.builder().allowExtensionList(".png,.jpg,.pdf").maxFileSize(1_000_000L).maxFileCount(2).build();
	}

	private static MockMultipartFile file(String name, int bytes, String contentType) {
		return new MockMultipartFile("files", name, contentType, new byte[bytes]);
	}

	@Test
	@DisplayName("파일명이 있는 0바이트 파일은 정책 판정을 받는다 — 크기 0 이라는 이유로 확장자 검사를 건너뛰지 않는다")
	void 파일명_있는_0바이트_파일은_정책_판정() {
		MockMultipartFile emptyJsp = file("shell.jsp", 0, "application/octet-stream");

		EgovUploadRejectedException rejected = assertThrows(EgovUploadRejectedException.class,
				() -> EgovMultipartFiles.validateAll(imagePolicy(), List.of(emptyJsp)));
		assertEquals(EgovUploadPolicy.Reason.EMPTY_FILE, rejected.getReason());

		Map<MultipartFile, EgovUploadPolicy.Reason> violations = EgovMultipartFiles.checkAll(imagePolicy(), List.of(emptyJsp));
		assertEquals(EgovUploadPolicy.Reason.EMPTY_FILE, violations.get(emptyJsp));

		// 빈 파일을 허용하는 정책이라도 확장자는 여전히 검사한다
		EgovUploadPolicy emptyAllowed = EgovUploadPolicy.builder().allowExtensionList(".png,.pdf").allowEmptyFile(true).build();
		assertEquals(Optional.of(EgovUploadPolicy.Reason.EXTENSION_NOT_ALLOWED),
				EgovMultipartFiles.checkAll(emptyAllowed, List.of(emptyJsp)).values().stream().findFirst());
		EgovMultipartFiles.validateAll(emptyAllowed, List.of(file("note.pdf", 0, null)));
	}

	@Test
	@DisplayName("채우지 않은 입력칸(파일명 없음·0바이트)은 여전히 개수와 검증에서 제외된다")
	void 파일명_없는_빈_항목은_제외() {
		List<MultipartFile> files = Arrays.asList(file("", 0, null), file("  ", 0, null), file("photo.png", 3, null));

		assertEquals(1, EgovMultipartFiles.submittedOnly(files).size());
		EgovMultipartFiles.validateAll(imagePolicy(), files);
		assertTrue(EgovMultipartFiles.checkAll(imagePolicy(), files).isEmpty());
	}

	@Test
	@DisplayName("개수 상한은 파일명이 있는 0바이트 항목으로 우회되지 않는다")
	void 개수_상한_우회_불가() {
		List<MultipartFile> files = Arrays.asList(file("a.png", 1, null), file("b.png", 1, null),
				file("c.png", 0, null), file("d.png", 0, null), file("e.png", 0, null));

		EgovUploadRejectedException rejected = assertThrows(EgovUploadRejectedException.class,
				() -> EgovMultipartFiles.validateAll(imagePolicy(), files));
		assertEquals(EgovUploadPolicy.Reason.COUNT_EXCEEDED, rejected.getReason());
	}

	@Test
	@DisplayName("리졸버는 0바이트 실행형 파일명을 컨트롤러 앞에서 거부하고, 예외 메시지에 파일명을 싣지 않는다")
	void 리졸버_0바이트_실행형_거부() {
		EgovValidatingMultipartResolver resolver = new EgovValidatingMultipartResolver(imagePolicy());
		MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
		request.setMethod("POST");
		request.setContentType("multipart/form-data; boundary=boundary");
		request.addPart(new MockPart("files", "secret-shell.jsp", new byte[0]));
		request.addPart(new MockPart("files", "ok.png", new byte[] {1, 2, 3}));

		MultipartException failure = assertThrows(MultipartException.class, () -> resolver.resolveMultipart(request));

		assertTrue(failure.getCause() instanceof EgovUploadRejectedException, String.valueOf(failure.getCause()));
		assertEquals(EgovUploadPolicy.Reason.EMPTY_FILE, ((EgovUploadRejectedException) failure.getCause()).getReason());
		assertFalse(failure.getMessage().contains("secret-shell"), failure.getMessage());
		assertFalse(failure.getCause().getMessage().contains("secret-shell"), failure.getCause().getMessage());
	}

	@Test
	@DisplayName("content-type 은 판정에 쓰이지 않는다 — 위장된 형식 헤더로 허용되거나 거부되지 않는다")
	void content_type_비의존() {
		assertEquals(Optional.of(EgovUploadPolicy.Reason.EXTENSION_NOT_ALLOWED),
				EgovMultipartFiles.check(imagePolicy(), file("shell.jsp", 10, "image/png")));
		assertEquals(Optional.empty(), EgovMultipartFiles.check(imagePolicy(), file("photo.png", 10, "application/x-jsp")));
	}

	@Test
	@DisplayName("경로가 섞인 파일명은 이름만 남는다 — 구분자 앞부분과 드라이브 접두를 떼고, 널 바이트는 정책이 거부한다")
	void 경로_정규화() {
		assertEquals("x.png", EgovMultipartFiles.fileNameOf(file("../../etc/x.png", 1, null)));
		assertEquals("x.png", EgovMultipartFiles.fileNameOf(file("dir" + BS + "x.png", 1, null)));
		assertEquals("x.png", EgovMultipartFiles.fileNameOf(file("C:" + BS + "Users" + BS + "me" + BS + "x.png", 1, null)));
		assertEquals("x.png", EgovMultipartFiles.fileNameOf(file("C:x.png", 1, null)));
		assertEquals("x.png", EgovMultipartFiles.fileNameOf(file("x.png.", 1, null)));
		assertEquals("", EgovMultipartFiles.fileNameOf(file("C:", 1, null)));

		assertEquals(Optional.of(EgovUploadPolicy.Reason.INVALID_FILENAME),
				EgovMultipartFiles.check(imagePolicy(), file("shell.jsp" + NUL + ".png", 1, null)));
		assertTrue(EgovMultipartFiles.check(imagePolicy(), file("..", 1, null)).isPresent());
		assertTrue(EgovMultipartFiles.check(imagePolicy(), file(".htaccess", 1, null)).isPresent());
	}

}
