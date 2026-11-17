package org.egovframe.rte.ai.springai.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.egovframe.rte.ai.core.document.EgovDocumentMetadata;
import org.egovframe.rte.ai.springai.support.LogCapture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

/**
 * PII 마스킹 변환기 계약 테스트 — 위임·표식·배치당 감사 1건·값 미기록(스펙 ai/springai-document-etl 4항).
 */
class EgovPiiMaskingTransformerTest {

	private static final String RRN = "850302-1234567";

	@Test
	@DisplayName("주민번호가 든 문서는 토큰으로 치환되고 pii_masked 표식과 감사 로그(rrn=1, 값 없음)가 남는다")
	void masksAndAudits() {
		Document source = Document.builder().id("d1").text("문의자 " + RRN + " 확인 바랍니다.")
				.metadata(Map.of(EgovDocumentMetadata.ORIGINAL_ID, "d1")).build();

		try (LogCapture audit = LogCapture.audit()) {
			Document result = EgovPiiMaskingTransformer.defaults().apply(List.of(source)).get(0);

			assertFalse(result.getText().contains(RRN));
			assertTrue(result.getText().contains("[RRN]"), result.getText());
			assertEquals("d1", result.getId());
			assertEquals(Boolean.TRUE, result.getMetadata().get(EgovSpringAiMetadata.PII_MASKED));
			assertEquals("d1", result.getMetadata().get(EgovDocumentMetadata.ORIGINAL_ID));

			assertEquals(1, audit.lines().size(), audit.lines().toString());
			String line = audit.lines().get(0);
			assertTrue(line.startsWith("event=masking"), line);
			assertTrue(line.contains("rrn=1"), line);
			assertFalse(line.contains("850302"), "원문 값은 기록하지 않는다");
		}
	}

	@Test
	@DisplayName("깨끗한 문서 배치는 같은 인스턴스로 통과하고 감사는 발신되지 않는다")
	void cleanBatchPassesThrough() {
		List<Document> sources = List.of(
				Document.builder().id("a").text("전자정부 표준프레임워크 안내").build(),
				Document.builder().id("b").text("페이징 설정 방법").build(),
				Document.builder().id("c").text("관리코드 851302-1234567 참조").build());

		try (LogCapture audit = LogCapture.audit()) {
			List<Document> result = EgovPiiMaskingTransformer.defaults().apply(sources);

			for (int i = 0; i < sources.size(); i++) {
				assertSame(sources.get(i), result.get(i));
			}
			assertTrue(audit.lines().isEmpty(), audit.lines().toString());
		}
	}

	@Test
	@DisplayName("배치 안의 여러 문서 건수는 합산되어 감사 1건으로 남는다")
	void aggregatesPerBatch() {
		List<Document> sources = List.of(
				Document.builder().id("a").text("A " + RRN).build(),
				Document.builder().id("b").text("깨끗").build(),
				Document.builder().id("c").text("C " + RRN + " 와 " + RRN).build());

		try (LogCapture audit = LogCapture.audit()) {
			List<Document> result = EgovPiiMaskingTransformer.defaults().apply(sources);

			assertSame(sources.get(1), result.get(1));
			assertEquals(1, audit.lines().size());
			assertTrue(audit.lines().get(0).contains("rrn=3"), audit.lines().get(0));
			assertTrue(audit.lines().get(0).contains("total=3"), audit.lines().get(0));
		}
	}
}
