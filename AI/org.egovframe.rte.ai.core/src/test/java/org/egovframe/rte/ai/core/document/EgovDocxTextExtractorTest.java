package org.egovframe.rte.ai.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * DOCX 추출기 계약 테스트 — 본문 추출·빈 문서 제외·손상 파일 예외(문서는 POI 로 생성).
 */
class EgovDocxTextExtractorTest {

	private static Path docx(Path dir, String name, String... paragraphs) throws Exception {
		Path file = dir.resolve(name);
		try (XWPFDocument document = new XWPFDocument(); OutputStream out = Files.newOutputStream(file)) {
			for (String paragraph : paragraphs) {
				document.createParagraph().createRun().setText(paragraph);
			}
			document.write(out);
		}
		return file;
	}

	@Test
	@DisplayName("문단 본문을 추출하고 docx 접두 식별자·메타데이터를 채운다")
	void extractsParagraphs(@TempDir Path dir) throws Exception {
		Path file = docx(dir, "정책 안내.docx", "제1조 목적", "이 규정은 정책을 정한다.");
		List<EgovExtractedDocument> documents = new EgovDocxTextExtractor().extract(file);
		assertEquals(1, documents.size());
		assertEquals("docx-정책-안내", documents.get(0).id());
		assertTrue(documents.get(0).text().contains("제1조 목적"));
		assertTrue(documents.get(0).text().contains("이 규정은 정책을 정한다."));
		assertEquals("docx", documents.get(0).metadata().get(EgovDocumentMetadata.TYPE));
	}

	@Test
	@DisplayName("본문이 없는 문서는 결과에서 제외되고 손상 파일은 예외다")
	void emptyAndCorrupt(@TempDir Path dir) throws Exception {
		Path empty = docx(dir, "empty.docx");
		assertTrue(new EgovDocxTextExtractor().extract(empty).isEmpty());
		Path corrupt = dir.resolve("broken.docx");
		Files.write(corrupt, new byte[] { 1, 2, 3 });
		EgovExtractionException ex = assertThrows(EgovExtractionException.class, () -> new EgovDocxTextExtractor().extract(corrupt));
		assertTrue(ex.getCause() != null);
	}

	// ---------------------------------------------------------------- 보안 회귀 (2026-09-07 파일 처리 보안 점검)

	private static final String DOCUMENT_ENTRY = "word/document.xml";
	private static final String SECRET = "SECRET-7f3a-LOCAL-FILE";

	/** 정상 DOCX 의 본문 XML 만 주어진 내용으로 바꿔치기한 사본을 만든다. */
	private static Path withDocumentXml(Path normal, Path tampered, String documentXml) throws Exception {
		try (ZipFile zip = new ZipFile(normal.toFile());
				ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(tampered))) {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				out.putNextEntry(new ZipEntry(entry.getName()));
				if (DOCUMENT_ENTRY.equals(entry.getName())) {
					out.write(documentXml.getBytes(StandardCharsets.UTF_8));
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

	@Test
	@DisplayName("DOCTYPE 외부 엔티티가 든 DOCX 는 본문에 로컬 파일 내용이 실리지 않는다 — POI 의 DOCTYPE 거부 회귀 고정")
	void doctypeEntityIsNotResolved(@TempDir Path dir) throws Exception {
		Path secret = dir.resolve("secret.txt");
		Files.writeString(secret, SECRET);
		Path normal = docx(dir, "normal.docx", "수수료는 별표와 같다");
		String documentXml;
		try (ZipFile zip = new ZipFile(normal.toFile());
				InputStream in = zip.getInputStream(zip.getEntry(DOCUMENT_ENTRY))) {
			documentXml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		int prologEnd = documentXml.indexOf("?>");
		String doctype = "<!DOCTYPE w:document [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]>";
		String tampered = (prologEnd < 0 ? doctype + documentXml
				: documentXml.substring(0, prologEnd + 2) + doctype + documentXml.substring(prologEnd + 2))
				.replace("별표와 같다", "&xxe;");
		assertTrue(tampered.contains("&xxe;"));
		Path evil = withDocumentXml(normal, dir.resolve("evil.docx"), tampered);

		try {
			List<EgovExtractedDocument> documents = new EgovDocxTextExtractor().extract(evil);
			String text = documents.isEmpty() ? "" : documents.get(0).text();
			assertFalse(text.contains(SECRET), "외부 엔티티가 해석되어 로컬 파일 내용이 본문에 실렸다");
		} catch (EgovExtractionException expected) {
			// 2026-09-18 부터는 zip 가드가 파싱 전에 DOCTYPE 을 거부한다(원인 없는 가드 예외). POI 의 거부(원인 있음)도 안전한 결과다
			assertTrue(expected.getMessage().contains("DOCTYPE") || expected.getCause() != null, expected.getMessage());
		}
	}

	// ---------------------------------------------------------------- 자원 예산 (R51-06, 2026-09-18 — 보안점검 1부 4장 #5)

	private static Path zip(Path dir, String name, String[] names, byte[][] contents) throws Exception {
		Path file = dir.resolve(name);
		try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(file))) {
			for (int i = 0; i < names.length; i++) {
				out.putNextEntry(new ZipEntry(names[i]));
				out.write(contents[i]);
				out.closeEntry();
			}
		}
		return file;
	}

	@Test
	@DisplayName("DOCX 도 HWPX 와 같은 zip 가드를 파싱 전에 받는다 — 압축 폭탄은 예산의 가드 사유로 실패")
	void zipGuardAppliesToDocx(@TempDir Path dir) throws Exception {
		byte[] filler = new byte[10 * 1024];
		java.util.Arrays.fill(filler, (byte) 'a');
		Path bomb = zip(dir, "bomb.docx", new String[] { "[Content_Types].xml", "word/document.xml" },
				new byte[][] { "<x/>".getBytes(StandardCharsets.UTF_8), filler });
		EgovExtractionBudget tight = EgovExtractionBudget.builder()
				.zipGuard(EgovZipDocumentGuard.of(100, 1024, 1 << 20)).build();

		EgovExtractionBudgetExceededException e = assertThrows(EgovExtractionBudgetExceededException.class,
				() -> new EgovDocxTextExtractor(tight).extract(bomb));
		assertEquals(EgovExtractionBudgetExceededException.Limit.ZIP_BYTES, e.getLimit());
		assertEquals(1024, e.getAllowed());

		assertEquals(1, new EgovDocxTextExtractor().extract(docx(dir, "ok.docx", "본문")).size(), "정상 DOCX 는 기본 예산 통과");
	}

	@Test
	@DisplayName("같은 zip 구조 위반(엔트리 수 초과)은 HWPX 와 DOCX 에서 같은 사유로 실패한다")
	void hwpxAndDocxShareTheGuardVerdict(@TempDir Path dir) throws Exception {
		String[] names = { "a.xml", "b.xml", "c.xml" };
		byte[][] contents = { "<a/>".getBytes(StandardCharsets.UTF_8), "<b/>".getBytes(StandardCharsets.UTF_8),
				"<c/>".getBytes(StandardCharsets.UTF_8) };
		Path manyDocx = zip(dir, "many.docx", names, contents);
		Path manyHwpx = zip(dir, "many.hwpx", names, contents);
		EgovExtractionBudget twoEntries = EgovExtractionBudget.builder()
				.zipGuard(EgovZipDocumentGuard.of(2, 1024, 4096)).build();

		EgovExtractionBudgetExceededException docx = assertThrows(EgovExtractionBudgetExceededException.class,
				() -> new EgovDocxTextExtractor(twoEntries).extract(manyDocx));
		EgovExtractionBudgetExceededException hwpx = assertThrows(EgovExtractionBudgetExceededException.class,
				() -> new EgovHwpxTextExtractor(twoEntries).extract(manyHwpx));

		assertEquals(EgovExtractionBudgetExceededException.Limit.ZIP_ENTRIES, docx.getLimit());
		assertEquals(docx.getLimit(), hwpx.getLimit());
		assertEquals(docx.getAllowed(), hwpx.getAllowed());
	}
}
