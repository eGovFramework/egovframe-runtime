package org.egovframe.rte.ai.core.document;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link EgovZipDocumentGuard} 직접 검증 — zip 기반 문서(hwpx·docx) 파싱 전 한도·DOCTYPE 차단.
 * (2026-09-07 보안 재점검: hwpx 추출기 경유 테스트만 있던 것을 가드 자체 단위 테스트로 보강)
 */
class EgovZipDocumentGuardTest {

	private static final String DOCTYPE_XXE =
			"<!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]><x>&e;</x>";

	private static Path zip(Path dir, String name, String[] names, byte[][] contents) throws IOException {
		Path file = dir.resolve(name);
		try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(file))) {
			for (int i = 0; i < names.length; i++) {
				out.putNextEntry(new ZipEntry(names[i]));
				if (contents[i] != null) {
					out.write(contents[i]);
				}
				out.closeEntry();
			}
		}
		return file;
	}

	private static byte[] bytes(String text, Charset charset) {
		return text.getBytes(charset);
	}

	private static byte[] filler(int length) {
		byte[] data = new byte[length];
		Arrays.fill(data, (byte) 'a');
		return data;
	}

	@Test
	@DisplayName("정상 zip 문서는 통과하고 디렉터리 엔트리는 크기에 세지 않는다")
	void wellFormedDocumentPasses(@TempDir Path dir) throws Exception {
		Path file = zip(dir, "ok.zip",
				new String[] { "Contents/", "Contents/section0.xml", "Contents/header.xml" },
				new byte[][] { null, bytes("<?xml version=\"1.0\"?><sec>DOCTYPE 라는 낱말만</sec>", StandardCharsets.UTF_8),
						bytes("<h/>", StandardCharsets.UTF_8) });
		assertDoesNotThrow(() -> EgovZipDocumentGuard.of(3, 1024, 2048).check(file));
	}

	@Test
	@DisplayName("DOCTYPE 이 읽기 청크 경계에 걸쳐 쪼개져도 탐지한다")
	void doctypeSplitAcrossReadBoundaryIsDetected(@TempDir Path dir) throws Exception {
		byte[] head = filler(64 * 1024 - 4);
		byte[] tail = bytes(DOCTYPE_XXE, StandardCharsets.UTF_8);
		byte[] content = new byte[head.length + tail.length];
		System.arraycopy(head, 0, content, 0, head.length);
		System.arraycopy(tail, 0, content, head.length, tail.length);
		Path file = zip(dir, "split.zip", new String[] { "Contents/section0.xml" }, new byte[][] { content });

		EgovExtractionException e = assertThrows(EgovExtractionException.class,
				() -> EgovZipDocumentGuard.defaults().check(file));
		assertTrue(e.getMessage().contains("DOCTYPE"), e.getMessage());
	}

	@Test
	@DisplayName("스캐너 단위: 패턴이 두 청크에 나뉘어 들어와도 이어 붙여 탐지한다")
	void scannerCarriesOverlapBetweenChunks() {
		EgovZipDocumentGuard.DoctypeScanner scanner = new EgovZipDocumentGuard.DoctypeScanner();
		byte[] first = bytes("<?xml version=\"1.0\"?>\n<!DOC", StandardCharsets.UTF_8);
		byte[] second = bytes("TYPE x><x/>", StandardCharsets.UTF_8);
		assertFalse(scanner.feed(first, first.length));
		assertTrue(scanner.feed(second, second.length));
	}

	@Test
	@DisplayName("UTF-16LE/BE 로 인코딩된 DOCTYPE 도 탐지한다")
	void doctypeInUtf16IsDetected(@TempDir Path dir) throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>" + DOCTYPE_XXE;
		byte[] le = bytes("\uFEFF" + xml, StandardCharsets.UTF_16LE);
		byte[] be = bytes("\uFEFF" + xml, StandardCharsets.UTF_16BE);
		Path fileLe = zip(dir, "le.zip", new String[] { "Contents/section0.xml" }, new byte[][] { le });
		Path fileBe = zip(dir, "be.zip", new String[] { "Contents/section0.xml" }, new byte[][] { be });

		assertThrows(EgovExtractionException.class, () -> EgovZipDocumentGuard.defaults().check(fileLe));
		assertThrows(EgovExtractionException.class, () -> EgovZipDocumentGuard.defaults().check(fileBe));
	}

	@Test
	@DisplayName("소문자 doctype 도 탐지한다")
	void lowercaseDoctypeIsDetected(@TempDir Path dir) throws Exception {
		Path file = zip(dir, "lower.zip", new String[] { "Contents/section0.xml" },
				new byte[][] { bytes("<!doctype x [<!entity e system \"file:///etc/passwd\">]><x>&e;</x>", StandardCharsets.UTF_8) });
		assertThrows(EgovExtractionException.class, () -> EgovZipDocumentGuard.defaults().check(file));
	}

	@Test
	@DisplayName("엔트리 수 한도를 넘으면 거부한다")
	void entryCountLimitIsEnforced(@TempDir Path dir) throws Exception {
		Path file = zip(dir, "many.zip", new String[] { "a.xml", "b.xml", "c.xml" },
				new byte[][] { bytes("<a/>", StandardCharsets.UTF_8), bytes("<b/>", StandardCharsets.UTF_8),
						bytes("<c/>", StandardCharsets.UTF_8) });
		EgovExtractionException e = assertThrows(EgovExtractionException.class,
				() -> EgovZipDocumentGuard.of(2, 1024, 4096).check(file));
		assertTrue(e.getMessage().contains("엔트리 수"), e.getMessage());
	}

	@Test
	@DisplayName("엔트리 해제 크기 한도를 넘으면 거부한다 — 한도와 같은 크기는 통과")
	void entryByteLimitIsEnforced(@TempDir Path dir) throws Exception {
		Path exact = zip(dir, "exact.zip", new String[] { "a.xml" }, new byte[][] { filler(1000) });
		Path over = zip(dir, "over.zip", new String[] { "a.xml" }, new byte[][] { filler(1001) });

		assertDoesNotThrow(() -> EgovZipDocumentGuard.of(10, 1000, 1 << 20).check(exact));
		EgovExtractionException e = assertThrows(EgovExtractionException.class,
				() -> EgovZipDocumentGuard.of(10, 1000, 1 << 20).check(over));
		assertTrue(e.getMessage().contains("해제 크기"), e.getMessage());
	}

	@Test
	@DisplayName("누적 해제 크기 한도를 넘으면 거부한다(zip bomb 다중 엔트리)")
	void totalByteLimitIsEnforced(@TempDir Path dir) throws Exception {
		Path file = zip(dir, "total.zip", new String[] { "a.xml", "b.xml" },
				new byte[][] { filler(1000), filler(1000) });

		assertDoesNotThrow(() -> EgovZipDocumentGuard.of(10, 1000, 2000).check(file));
		assertThrows(EgovExtractionException.class, () -> EgovZipDocumentGuard.of(10, 1000, 1999).check(file));
	}

	@Test
	@DisplayName("한도는 양수여야 한다")
	void limitsMustBePositive() {
		assertThrows(IllegalArgumentException.class, () -> EgovZipDocumentGuard.of(0, 1, 1));
		assertThrows(IllegalArgumentException.class, () -> EgovZipDocumentGuard.of(1, 0, 1));
		assertThrows(IllegalArgumentException.class, () -> EgovZipDocumentGuard.of(1, 1, -1));
	}

	@Test
	@DisplayName("zip 이 아닌 파일은 추출 예외로 감싼다")
	void nonZipIsWrapped(@TempDir Path dir) throws Exception {
		Path file = dir.resolve("plain.bin");
		Files.write(file, new byte[] { 1, 2, 3, 4 });
		assertThrows(EgovExtractionException.class, () -> EgovZipDocumentGuard.defaults().check(file));
	}
}
