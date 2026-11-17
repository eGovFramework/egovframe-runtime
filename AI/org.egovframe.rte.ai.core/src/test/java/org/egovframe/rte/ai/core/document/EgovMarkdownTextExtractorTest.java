package org.egovframe.rte.ai.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Markdown 추출기 계약 테스트 — 본문·구조 메타데이터·빈 파일 제외·읽기 실패 예외.
 */
class EgovMarkdownTextExtractorTest {

	private final EgovMarkdownTextExtractor extractor = new EgovMarkdownTextExtractor();

	@Test
	@DisplayName("UTF-8 본문과 구조 메타데이터(헤더·코드블록·링크·이미지·줄 수)를 돌려준다")
	void extractsWithStructureMetadata(@TempDir Path dir) throws Exception {
		Path file = dir.resolve("페이징 가이드.md");
		String content = "# 페이징\n\n본문 [링크](http://a) 와 ![그림](x.png)\n\n```java\nint a;\n```\n";
		Files.writeString(file, content, StandardCharsets.UTF_8);

		List<EgovExtractedDocument> documents = extractor.extract(file);

		assertEquals(1, documents.size());
		EgovExtractedDocument document = documents.get(0);
		assertEquals("md-페이징-가이드", document.id());
		assertEquals(content, document.text());
		assertEquals("markdown", document.metadata().get(EgovDocumentMetadata.TYPE));
		assertEquals("페이징 가이드.md", document.metadata().get(EgovDocumentMetadata.FILE_NAME));
		assertEquals(document.id(), document.metadata().get(EgovDocumentMetadata.ORIGINAL_ID));
		assertEquals(content.length(), document.metadata().get(EgovDocumentMetadata.CONTENT_LENGTH));
		assertEquals(true, document.metadata().get(EgovDocumentMetadata.HAS_HEADERS));
		assertEquals(true, document.metadata().get(EgovDocumentMetadata.HAS_CODE_BLOCKS));
		assertEquals(true, document.metadata().get(EgovDocumentMetadata.HAS_LINKS));
		assertEquals(true, document.metadata().get(EgovDocumentMetadata.HAS_IMAGES));
		assertEquals(8, document.metadata().get(EgovDocumentMetadata.LINE_COUNT));
		assertTrue(extractor.supports("A.MD"));
		assertTrue(!extractor.supports("a.txt"));
	}

	@Test
	@DisplayName("링크·이미지 정규식은 적대 본문(대괄호·괄호 반복)에서도 선형이다(ReDoS, 2026-09-07 4차 점검)")
	void linkPatternsAreLinear(@TempDir Path dir) throws Exception {
		Path file = dir.resolve("hostile.md");
		String content = "[".repeat(100_000) + "\n" + "![a](" + "b".repeat(200_000) + "\n" + "[x](".repeat(50_000);
		Files.writeString(file, content, StandardCharsets.UTF_8);
		org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(java.time.Duration.ofSeconds(3), () -> extractor.extract(file));
	}

	@Test
	@DisplayName("빈 파일은 결과가 없고 읽기 실패는 예외다")
	void emptyAndMissing(@TempDir Path dir) throws Exception {
		Path empty = dir.resolve("empty.md");
		Files.writeString(empty, "  \n", StandardCharsets.UTF_8);
		assertTrue(extractor.extract(empty).isEmpty());
		EgovExtractionException ex = assertThrows(EgovExtractionException.class, () -> extractor.extract(dir.resolve("missing.md")));
		assertTrue(ex.getCause() != null);
	}
}
