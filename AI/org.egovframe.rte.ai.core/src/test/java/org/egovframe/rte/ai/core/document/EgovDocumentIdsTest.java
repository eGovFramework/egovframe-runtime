package org.egovframe.rte.ai.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 안정 식별자 계약 테스트(스펙 ai/document-text-extraction 2항).
 */
class EgovDocumentIdsTest {

	@Test
	@DisplayName("형식 접두 + 정제 파일명(확장자 제거·위험 문자 제거·공백은 -) + 페이지")
	void ids() {
		assertEquals("hwp-2026-공고안내", EgovDocumentIds.of("hwp", "2026 공고:안내?.hwp"));
		assertEquals("pdf-report_3", EgovDocumentIds.of("pdf", "report.pdf", 3));
		assertEquals("md-guide.v2", EgovDocumentIds.of("md", "guide.v2.md"));
		assertEquals("docx-정책", EgovDocumentIds.of("docx", "C:\\upload\\정책.docx"));
		assertEquals(EgovDocumentIds.of("hwp", "a.hwp"), EgovDocumentIds.of("hwp", "a.hwp"), "실행마다 같은 값");
	}

	@Test
	@DisplayName("정제 결과가 비거나 형식·페이지가 잘못되면 거부된다")
	void validation() {
		assertThrows(IllegalArgumentException.class, () -> EgovDocumentIds.sanitize("???.hwp"));
		assertThrows(IllegalArgumentException.class, () -> EgovDocumentIds.of("", "a.hwp"));
		assertThrows(IllegalArgumentException.class, () -> EgovDocumentIds.of("pdf", "a.pdf", 0));
		assertThrows(NullPointerException.class, () -> EgovDocumentIds.sanitize(null));
	}
}
