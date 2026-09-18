package org.egovframe.rte.ptl.mvc.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.egovframe.rte.ptl.mvc.token.EgovSubmitTokenException.Reason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovSubmitTokenException} 단위 테스트 — 직렬화 후 필드 보존.
 */
class EgovSubmitTokenExceptionTest {

	@Test
	@DisplayName("직렬화 후 역직렬화해도 실패 사유와 tokenKey 가 보존된다")
	void 직렬화해도_실패_사유와_tokenKey가_보존된다() throws IOException, ClassNotFoundException {
		EgovSubmitTokenException original = new EgovSubmitTokenException(Reason.TOKEN_MISMATCH, "orderForm");

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
			out.writeObject(original);
		}

		EgovSubmitTokenException restored;
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
			restored = (EgovSubmitTokenException) in.readObject();
		}

		assertEquals(Reason.TOKEN_MISMATCH, restored.getReason(), "실패 사유가 직렬화 후 사라지면 안 된다");
		assertTrue(restored.isDuplicateSubmit(), "복원된 예외도 중복 제출로 판정돼야 한다");
		assertEquals("orderForm", restored.getTokenKey(), "tokenKey 가 직렬화 후 사라지면 안 된다");
	}

}
