package org.egovframe.rte.fdl.filehandling.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.egovframe.rte.fdl.filehandling.upload.EgovUploadPolicy.Reason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovUploadRejectedException} 단위 테스트 — 직렬화 후 필드 보존.
 */
class EgovUploadRejectedExceptionTest {

	@Test
	@DisplayName("직렬화 후 역직렬화해도 거부 사유가 보존된다")
	void 직렬화해도_거부_사유가_보존된다() throws IOException, ClassNotFoundException {
		EgovUploadRejectedException original = new EgovUploadRejectedException(Reason.EXTENSION_NOT_ALLOWED, "a.exe");

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
			out.writeObject(original);
		}

		EgovUploadRejectedException restored;
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
			restored = (EgovUploadRejectedException) in.readObject();
		}

		assertEquals(Reason.EXTENSION_NOT_ALLOWED, restored.getReason(), "거부 사유가 직렬화 후 사라지면 안 된다");
		assertEquals("a.exe", restored.getFileName(), "파일명이 직렬화 후 사라지면 안 된다");
	}

}
