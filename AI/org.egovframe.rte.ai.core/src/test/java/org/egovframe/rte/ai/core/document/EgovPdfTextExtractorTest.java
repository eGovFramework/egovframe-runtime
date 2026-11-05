package org.egovframe.rte.ai.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * PDF 추출기 계약 테스트 — 페이지 단위 문서·빈 페이지 제외·페이지 번호 메타데이터·손상 파일 예외(문서는 pdfbox 로 생성).
 */
class EgovPdfTextExtractorTest {

	private static Path pdf(Path dir, String name, String... pageTexts) throws Exception {
		Path file = dir.resolve(name);
		try (PDDocument document = new PDDocument()) {
			PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
			for (String pageText : pageTexts) {
				PDPage page = new PDPage();
				document.addPage(page);
				if (pageText != null) {
					try (PDPageContentStream content = new PDPageContentStream(document, page)) {
						content.beginText();
						content.setFont(font, 12);
						content.newLineAtOffset(50, 700);
						content.showText(pageText);
						content.endText();
					}
				}
			}
			document.save(file.toFile());
		}
		return file;
	}

	@Test
	@DisplayName("3페이지 중 빈 페이지 하나를 제외한 2문서가 페이지 번호와 함께 나온다")
	void pagesBecomeDocumentsAndBlankPagesAreSkipped(@TempDir Path dir) throws Exception {
		Path file = pdf(dir, "sample report.pdf", "Page one text", null, "Page three text");
		List<EgovExtractedDocument> documents = new EgovPdfTextExtractor().extract(file);
		assertEquals(2, documents.size());
		assertEquals("pdf-sample-report_1", documents.get(0).id());
		assertEquals(1, documents.get(0).metadata().get(EgovDocumentMetadata.PAGE_NUMBER));
		assertTrue(documents.get(0).text().contains("Page one text"));
		assertEquals("pdf-sample-report_3", documents.get(1).id());
		assertEquals(3, documents.get(1).metadata().get(EgovDocumentMetadata.PAGE_NUMBER));
		assertTrue(documents.get(1).text().contains("Page three text"));
		assertEquals("pdf", documents.get(1).metadata().get(EgovDocumentMetadata.TYPE));
		assertEquals("sample report.pdf", documents.get(1).metadata().get(EgovDocumentMetadata.FILE_NAME));
	}

	@Test
	@DisplayName("모든 페이지가 비면 결과가 없고 손상 파일은 예외다")
	void blankAndCorrupt(@TempDir Path dir) throws Exception {
		Path blank = pdf(dir, "blank.pdf", (String) null);
		assertTrue(new EgovPdfTextExtractor().extract(blank).isEmpty());
		Path corrupt = dir.resolve("broken.pdf");
		Files.write(corrupt, "%PDF-1.7 broken".getBytes());
		EgovExtractionException ex = assertThrows(EgovExtractionException.class, () -> new EgovPdfTextExtractor().extract(corrupt));
		assertTrue(ex.getCause() != null);
	}

	@Test
	@DisplayName("페이지 한도를 넘는 PDF 는 본문을 읽기 전에 페이지 한도 사유로 실패한다 (R51-06 자원 예산)")
	void pageBudget(@TempDir Path dir) throws Exception {
		Path file = pdf(dir, "three.pdf", "one", "two", "three");

		EgovExtractionBudgetExceededException e = assertThrows(EgovExtractionBudgetExceededException.class,
				() -> new EgovPdfTextExtractor(EgovExtractionBudget.builder().maxPages(2).build()).extract(file));
		assertEquals(EgovExtractionBudgetExceededException.Limit.PAGES, e.getLimit());
		assertEquals(2, e.getAllowed());
		assertEquals(3, e.getActual());
		assertEquals("three.pdf", e.getFileName());

		assertEquals(3, new EgovPdfTextExtractor(EgovExtractionBudget.builder().maxPages(3).build()).extract(file).size());
		assertEquals(3, new EgovPdfTextExtractor().extract(file).size(), "기본 예산(2,000 페이지)은 통과");
	}
}
