package org.egovframe.rte.ai.langchain4j.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.egovframe.rte.ai.core.document.EgovDocumentMetadata;
import org.egovframe.rte.ai.core.text.EgovTextNormalizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;

/**
 * 정규화 변환기 계약 테스트(스펙 ai/langchain4j-document-etl 3항).
 */
class EgovNormalizingTransformerTest {

	@Test
	@DisplayName("HTML 태그 제거 옵션 — 본문이 바뀌면 새 문서(같은 메타데이터, content_length 갱신)")
	void stripsTags() {
		Document source = Document.from("<b>전자정부</b>",
				new Metadata().put(EgovDocumentMetadata.CONTENT_LENGTH, 12).put(EgovDocumentMetadata.FILE_NAME, "a.html"));

		Document result = EgovNormalizingTransformer.of(EgovTextNormalizer.builder().stripHtmlTags(true).build()).transform(source);

		assertEquals("전자정부", result.text());
		assertEquals(4, result.metadata().getInteger(EgovDocumentMetadata.CONTENT_LENGTH));
		assertEquals("a.html", result.metadata().getString(EgovDocumentMetadata.FILE_NAME));
	}

	@Test
	@DisplayName("기본 정규화기는 공문서 표기를 보존하고 무변경 문서는 같은 인스턴스를 돌려준다")
	void preservesOfficialNotation() {
		Document source = Document.from("「전자정부법」 제2조(定義)·행정 지원 ※ 2026.9.3.");

		assertSame(source, EgovNormalizingTransformer.defaults().transform(source));
	}
}
