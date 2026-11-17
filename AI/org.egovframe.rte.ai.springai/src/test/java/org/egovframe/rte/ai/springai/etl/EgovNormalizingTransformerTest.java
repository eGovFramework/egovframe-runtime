package org.egovframe.rte.ai.springai.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import java.util.Map;

import org.egovframe.rte.ai.core.document.EgovDocumentMetadata;
import org.egovframe.rte.ai.core.text.EgovTextNormalizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

/**
 * 정규화 변환기 계약 테스트(스펙 ai/springai-document-etl 3항).
 */
class EgovNormalizingTransformerTest {

	@Test
	@DisplayName("HTML 태그 제거 옵션 — 본문이 바뀌면 같은 id 로 재생성되고 content_length 가 갱신된다")
	void stripsTagsAndUpdatesLength() {
		EgovNormalizingTransformer transformer = EgovNormalizingTransformer.of(EgovTextNormalizer.builder().stripHtmlTags(true).build());
		Document source = Document.builder().id("d1").text("<b>전자정부</b>")
				.metadata(Map.of(EgovDocumentMetadata.CONTENT_LENGTH, 12, EgovDocumentMetadata.FILE_NAME, "a.html")).build();

		Document result = transformer.apply(List.of(source)).get(0);

		assertEquals("전자정부", result.getText());
		assertEquals("d1", result.getId());
		assertEquals(4, result.getMetadata().get(EgovDocumentMetadata.CONTENT_LENGTH));
		assertEquals("a.html", result.getMetadata().get(EgovDocumentMetadata.FILE_NAME));
	}

	@Test
	@DisplayName("기본 정규화기는 공문서 표기를 보존하고 무변경 문서는 같은 인스턴스를 통과시킨다")
	void preservesOfficialNotation() {
		Document source = Document.builder().id("d2").text("「전자정부법」 제2조(定義)·행정 지원 ※ 2026.9.3.").build();

		Document result = EgovNormalizingTransformer.defaults().apply(List.of(source)).get(0);

		assertSame(source, result);
	}

	@Test
	@DisplayName("개행 정리 옵션 — 빈 줄 연속은 정리되고 바뀐 문서만 재생성된다")
	void collapsesBlankLinesOnlyWhenChanged() {
		Document changed = Document.builder().id("c").text("첫 단락\n\n\n\n둘째 단락").build();
		Document unchanged = Document.builder().id("u").text("첫 단락\n\n둘째 단락").build();

		List<Document> result = EgovNormalizingTransformer.of(EgovTextNormalizer.builder().normalizeNewlines(true).build())
				.apply(List.of(changed, unchanged));

		assertEquals("첫 단락\n\n둘째 단락", result.get(0).getText());
		assertEquals("c", result.get(0).getId());
		assertSame(unchanged, result.get(1));
	}
}
