package org.egovframe.rte.ai.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.ParagraphListInterface;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.control.ControlType;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.tool.blankfilemaker.BlankFileMaker;
import kr.dogfoot.hwplib.tool.textextractor.TextExtractMethod;
import kr.dogfoot.hwplib.tool.textextractor.TextExtractor;
import kr.dogfoot.hwplib.writer.HWPWriter;

/**
 * HWP 추출기 계약 테스트 — 문단·표 구조 보존(스펙 ai/document-text-extraction 1항). 문서는 hwplib 로 메모리에서 만들어
 * 파일로 써서 검증하므로 바이너리 fixture 를 저장소에 두지 않는다(참고 샘플의 검증 케이스 이식).
 */
class EgovHwpTextExtractorTest {

	private static void addParagraph(ParagraphListInterface list, String text) throws Exception {
		Paragraph paragraph = list.addNewParagraph();
		paragraph.createText();
		paragraph.getText().addString(text);
	}

	/** 문단 2개와 3×3 표 하나. */
	private static HWPFile sampleDocument() throws Exception {
		HWPFile file = BlankFileMaker.make();
		Section section = file.getBodyText().getSectionList().get(0);
		addParagraph(section, "제1조(목적) 이 규정은 수수료 기준을 정함을 목적으로 한다");
		addParagraph(section, "제2조(수수료) 수수료는 별표와 같다");
		Paragraph tableParagraph = section.addNewParagraph();
		tableParagraph.createText();
		tableParagraph.getText().addExtendCharForTable();
		ControlTable table = (ControlTable) tableParagraph.addNewControl(ControlType.Table);
		String[][] rows = { { "민원종류", "수수료", "처리기간" }, { "주민등록등본", "400원", "즉시" }, { "가족관계증명서", "1000원", "3일" } };
		for (String[] row : rows) {
			Row tableRow = table.addNewRow();
			for (String cell : row) {
				Cell tableCell = tableRow.addNewCell();
				addParagraph(tableCell.getParagraphList(), cell);
			}
		}
		return file;
	}

	private static String stripControl(String text) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) >= ' ') {
				out.append(text.charAt(i));
			}
		}
		return out.toString();
	}

	@Test
	@DisplayName("문단은 개행, 표는 행마다 개행·셀은 구분자로 이어지고 구분자는 행 맨 앞에 붙지 않는다")
	void preservesParagraphAndTableStructure() throws Exception {
		String extracted = EgovHwpTextExtractor.Hwplib.structuredText(sampleDocument());
		assertTrue(extracted.contains("정함을 목적으로 한다\n제2조(수수료)"), extracted);
		assertTrue(extracted.contains("민원종류 | 수수료 | 처리기간"));
		assertTrue(extracted.contains("주민등록등본 | 400원 | 즉시"));
		assertTrue(extracted.contains("가족관계증명서 | 1000원 | 3일"));
		for (String line : extracted.split("\n")) {
			assertFalse(line.trim().startsWith("|"), line);
		}
		// hwplib 기본 추출은 행 관계를 잃는다(보완의 근거) — 글자 자체는 같다
		String plain = stripControl(TextExtractor.extract(sampleDocument(), TextExtractMethod.AppendControlTextAfterParagraphText));
		assertTrue(plain.contains("주민등록등본400원즉시"));
		assertEquals(plain, extracted.replace("\n", "").replace(" | ", ""));
	}

	@Test
	@DisplayName("파일 추출은 안정 식별자·공통 메타데이터를 채우고 같은 파일에 같은 값을 준다")
	void extractsFileWithIdAndMetadata(@TempDir Path dir) throws Exception {
		Path file = dir.resolve("2026 수수료 규정.hwp");
		HWPWriter.toFile(sampleDocument(), file.toString());
		EgovHwpTextExtractor extractor = new EgovHwpTextExtractor();

		List<EgovExtractedDocument> documents = extractor.extract(file);

		assertEquals(1, documents.size());
		EgovExtractedDocument document = documents.get(0);
		assertEquals("hwp-2026-수수료-규정", document.id());
		assertTrue(document.text().contains("주민등록등본 | 400원 | 즉시"));
		assertEquals("hwp", document.metadata().get(EgovDocumentMetadata.TYPE));
		assertEquals("2026 수수료 규정.hwp", document.metadata().get(EgovDocumentMetadata.SOURCE));
		assertEquals(document.id(), document.metadata().get(EgovDocumentMetadata.ORIGINAL_ID));
		assertEquals(document.text().length(), document.metadata().get(EgovDocumentMetadata.CONTENT_LENGTH));
		assertEquals(document.id(), extractor.extract(file).get(0).id());
	}

	@Test
	@DisplayName("손상 파일은 원인을 담은 추출 예외다")
	void corruptFileThrows(@TempDir Path dir) throws Exception {
		Path corrupt = dir.resolve("broken.hwp");
		Files.write(corrupt, new byte[] { 0x11, 0x22, 0x33, 0x44 });
		EgovExtractionException ex = assertThrows(EgovExtractionException.class, () -> new EgovHwpTextExtractor().extract(corrupt));
		assertTrue(ex.getMessage().contains("broken.hwp"));
		assertTrue(ex.getCause() != null);
	}
}
