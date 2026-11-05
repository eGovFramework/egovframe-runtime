package org.egovframe.rte.ai.langchain4j.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.egovframe.rte.ai.langchain4j.support.LogCapture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.langchain4j.data.document.Document;

/**
 * PII 마스킹 변환기 계약 테스트 — 위임·표식·배치당 감사 1건·단건 감사·값 미기록(스펙 ai/langchain4j-document-etl 3항).
 */
class EgovPiiMaskingTransformerTest {

	private static final String RRN = "850302-1234567";

	@Test
	@DisplayName("배치 변환 — 주민번호 문서 2건은 토큰으로 바뀌고 pii_masked 가 붙으며 감사 1건에 rrn=2, 원문은 없다")
	void batchMasksAndAuditsOnce() {
		List<Document> sources = List.of(
				Document.from("문의자 " + RRN + " 확인"),
				Document.from("깨끗한 문서"),
				Document.from("담당자 " + RRN + " 연락"));

		try (LogCapture audit = LogCapture.audit()) {
			List<Document> result = EgovPiiMaskingTransformer.defaults().transformAll(sources);

			assertFalse(result.get(0).text().contains(RRN));
			assertTrue(result.get(0).text().contains("[RRN]"));
			assertEquals("true", result.get(0).metadata().getString(EgovLangChain4jMetadata.PII_MASKED));
			assertSame(sources.get(1), result.get(1));
			assertEquals("true", result.get(2).metadata().getString(EgovLangChain4jMetadata.PII_MASKED));
			assertEquals(1, audit.lines().size(), audit.lines().toString());
			assertTrue(audit.lines().get(0).startsWith("event=masking"), audit.lines().get(0));
			assertTrue(audit.lines().get(0).contains("rrn=2"), audit.lines().get(0));
			assertFalse(audit.lines().get(0).contains("850302"));
		}
	}

	@Test
	@DisplayName("단건 변환은 그 문서 건수로 감사한다")
	void singleTransformAudits() {
		try (LogCapture audit = LogCapture.audit()) {
			Document result = EgovPiiMaskingTransformer.defaults().transform(Document.from("문의자 " + RRN));

			assertTrue(result.text().contains("[RRN]"));
			assertEquals(1, audit.lines().size());
			assertTrue(audit.lines().get(0).contains("rrn=1"));
		}
	}

	@Test
	@DisplayName("깨끗한 배치는 같은 인스턴스로 통과하고 감사는 발신되지 않는다")
	void cleanBatchPassesThrough() {
		List<Document> sources = List.of(Document.from("전자정부 표준프레임워크 안내"), Document.from("관리코드 851302-1234567 참조"));

		try (LogCapture audit = LogCapture.audit()) {
			List<Document> result = EgovPiiMaskingTransformer.defaults().transformAll(sources);

			assertSame(sources.get(0), result.get(0));
			assertSame(sources.get(1), result.get(1));
			assertTrue(audit.lines().isEmpty(), audit.lines().toString());
		}
	}
}
