package org.egovframe.rte.ai.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.object.content.section_xml.SectionXMLFile;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.Para;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.Run;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.T;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.object.Table;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.object.table.Tc;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.object.table.Tr;
import kr.dogfoot.hwpxlib.tool.blankfilemaker.BlankFileMaker;
import kr.dogfoot.hwpxlib.tool.textextractor.TextExtractMethod;
import kr.dogfoot.hwpxlib.tool.textextractor.TextExtractor;
import kr.dogfoot.hwpxlib.writer.HWPXWriter;

/**
 * HWPX 추출기 계약 테스트 — HWP 와 같은 구분 형식으로 문단·표 구조를 보존한다(문서는 hwpxlib 로 메모리 생성).
 */
class EgovHwpxTextExtractorTest {

	private static void addText(Para para, String text) {
		Run run = para.addNewRun();
		run.charPrIDRefAnd("0");
		T t = run.addNewT();
		t.addText(text);
	}

	private static void addCell(Tr row, String text) {
		Tc cell = row.addNewTc();
		cell.createSubList();
		addText(cell.subList().addNewPara(), text);
	}

	private static HWPXFile sampleDocument() {
		HWPXFile file = BlankFileMaker.make();
		SectionXMLFile section = file.sectionXMLFileList().get(0);
		addText(section.addNewPara(), "제1조(목적) 이 규정은 수수료 기준을 정함을 목적으로 한다");
		addText(section.addNewPara(), "제2조(수수료) 수수료는 별표와 같다");
		Para tableParagraph = section.addNewPara();
		Run run = tableParagraph.addNewRun();
		run.charPrIDRefAnd("0");
		Table table = run.addNewTable();
		String[][] rows = { { "민원종류", "수수료", "처리기간" }, { "주민등록등본", "400원", "즉시" }, { "가족관계증명서", "1000원", "3일" } };
		for (String[] row : rows) {
			Tr tr = table.addNewTr();
			for (String cell : row) {
				addCell(tr, cell);
			}
		}
		return file;
	}

	@Test
	@DisplayName("문단·표 행은 개행, 셀은 구분자 — 표식이 없으면 모든 구조가 사라진다(보완의 근거)")
	void preservesStructureWithMarks() throws Exception {
		String extracted = EgovHwpxTextExtractor.Hwpxlib.structuredText(sampleDocument());
		assertTrue(extracted.contains("정함을 목적으로 한다\n제2조(수수료)"), extracted);
		assertTrue(extracted.contains("민원종류 | 수수료 | 처리기간"));
		assertTrue(extracted.contains("가족관계증명서 | 1000원 | 3일"));
		for (String line : extracted.split("\n")) {
			assertFalse(line.trim().startsWith("|"), line);
		}
		String withoutMarks = TextExtractor.extract(sampleDocument(), TextExtractMethod.InsertControlTextBetweenParagraphText, false, null);
		assertFalse(withoutMarks.contains("\n"));
		assertTrue(withoutMarks.contains("주민등록등본400원즉시"));
		assertEquals(withoutMarks, extracted.replace("\n", "").replace(" | ", ""));
	}

	@Test
	@DisplayName("파일 추출은 hwpx 접두 식별자와 공통 메타데이터를 채운다")
	void extractsFile(@TempDir Path dir) throws Exception {
		Path file = dir.resolve("수수료규정.hwpx");
		HWPXWriter.toFilepath(sampleDocument(), file.toString());

		List<EgovExtractedDocument> documents = new EgovHwpxTextExtractor().extract(file);

		assertEquals(1, documents.size());
		assertEquals("hwpx-수수료규정", documents.get(0).id());
		assertEquals("hwpx", documents.get(0).metadata().get(EgovDocumentMetadata.TYPE));
		assertTrue(documents.get(0).text().contains("주민등록등본 | 400원 | 즉시"));
	}

	@Test
	@DisplayName("손상 파일은 원인을 담은 추출 예외다")
	void corruptFileThrows(@TempDir Path dir) throws Exception {
		Path corrupt = dir.resolve("broken.hwpx");
		Files.write(corrupt, new byte[] { 0x50, 0x4B, 0x00, 0x00 });
		EgovExtractionException ex = assertThrows(EgovExtractionException.class, () -> new EgovHwpxTextExtractor().extract(corrupt));
		assertTrue(ex.getCause() != null);
	}

	// ---------------------------------------------------------------- 보안 회귀 (2026-09-07 파일 처리 보안 점검)

	private static final String SECTION_ENTRY = "Contents/section0.xml";
	private static final String SECRET = "SECRET-7f3a-LOCAL-FILE";

	/** 정상 문서를 origin.hwpx 로 쓰고 본문 섹션 XML 원문을 돌려준다. */
	private static String originalSectionXml(Path dir) throws Exception {
		Path original = dir.resolve("origin.hwpx");
		HWPXWriter.toFilepath(sampleDocument(), original.toString());
		try (ZipFile zip = new ZipFile(original.toFile());
				InputStream in = zip.getInputStream(zip.getEntry(SECTION_ENTRY))) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/** origin.hwpx 의 본문 섹션만 주어진 XML 로 바꿔치기한 사본 — 공격자가 만든 HWPX 를 모사한다. */
	private static Path withSection(Path dir, String name, String sectionXml) throws Exception {
		Path original = dir.resolve("origin.hwpx");
		Path tampered = dir.resolve(name);
		try (ZipFile zip = new ZipFile(original.toFile());
				ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(tampered))) {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				out.putNextEntry(new ZipEntry(entry.getName()));
				if (SECTION_ENTRY.equals(entry.getName())) {
					out.write(sectionXml.getBytes(StandardCharsets.UTF_8));
				} else if (!entry.isDirectory()) {
					try (InputStream in = zip.getInputStream(entry)) {
						in.transferTo(out);
					}
				}
				out.closeEntry();
			}
		}
		return tampered;
	}

	/** XML 선언 바로 뒤에 DOCTYPE 을 끼워 넣는다. */
	private static String withDoctype(String xml, String doctype) {
		int prologEnd = xml.indexOf("?>");
		return prologEnd < 0 ? doctype + xml : xml.substring(0, prologEnd + 2) + doctype + xml.substring(prologEnd + 2);
	}

	@Test
	@DisplayName("DOCTYPE 외부 엔티티가 든 HWPX 는 파싱 전에 거부된다 — hwpxlib 기본 SAX 파서의 XXE 노출 차단")
	void doctypeEntityRejected(@TempDir Path dir) throws Exception {
		Path secret = dir.resolve("secret.txt");
		Files.writeString(secret, SECRET);
		String original = originalSectionXml(dir);
		String doctype = "<!DOCTYPE hs:sec [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]>";
		String tampered = withDoctype(original, doctype).replace("별표와 같다", "&xxe;");
		assertTrue(tampered.contains("&xxe;"), "본문에 엔티티 참조가 들어가야 한다");
		Path evil = withSection(dir, "evil.hwpx", tampered);

		try {
			List<EgovExtractedDocument> documents = new EgovHwpxTextExtractor().extract(evil);
			String text = documents.isEmpty() ? "" : documents.get(0).text();
			fail("DOCTYPE 문서를 거부하지 않았다 — 외부 엔티티가 해석되어 로컬 파일 내용이 본문에 실렸는가: "
					+ text.contains(SECRET));
		} catch (EgovExtractionException expected) {
			assertTrue(expected.getMessage().contains("DOCTYPE"), expected.getMessage());
		}
	}

	@Test
	@DisplayName("압축 해제 크기가 한도를 넘는 엔트리는 파싱 전에 거부된다 — zip bomb 방어")
	void oversizedEntryRejected(@TempDir Path dir) throws Exception {
		String original = originalSectionXml(dir);
		String padded = original.replace("별표와 같다", "별표와 같다" + " ".repeat(3 * 1024 * 1024));
		Path bomb = withSection(dir, "bomb.hwpx", padded);
		EgovHwpxTextExtractor limited = new EgovHwpxTextExtractor(
				EgovZipDocumentGuard.of(1000, 1024 * 1024, 8L * 1024 * 1024));

		EgovExtractionException ex = assertThrows(EgovExtractionException.class, () -> limited.extract(bomb));
		assertTrue(ex.getMessage().contains("한도"), ex.getMessage());
	}
}
