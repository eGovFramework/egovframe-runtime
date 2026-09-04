package org.egovframe.rte.fdl.filehandling.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link EgovFileStore} 흐름 통합 테스트 — 저장 → 재검증 → 읽기 → 삭제를 실제 파일시스템으로 왕복한다.
 *
 */
class EgovFileStoreTest {

	@TempDir
	Path tmp;

	private static ByteArrayInputStream bytes(String s) {
		return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
	}

	private static long countFiles(Path dir) throws IOException {
		if (!Files.isDirectory(dir)) {
			return 0;
		}
		try (Stream<Path> s = Files.list(dir)) {
			return s.count();
		}
	}

	// ---------------------------------------------------------------- 루트

	@Test
	@DisplayName("① 루트 없이 만들 수 없다 (fail-closed)")
	void cannotBuildWithoutRoots() {
		assertThrows(IllegalArgumentException.class, () -> EgovFileStore.builder().build());
	}

	@Test
	@DisplayName("② 여러 루트 중 키로 고르고, 키 없으면 첫 루트가 기본")
	void selectsRootByKey() throws IOException {
		Path storeRoot = tmp.resolve("store");
		Path imageRoot = tmp.resolve("image");
		EgovFileStore store = EgovFileStore.builder().root("store", storeRoot).root("image", imageRoot).build();

		EgovStoredFile img = store.store("image", bytes("png"), "logo.png");
		EgovStoredFile doc = store.store(bytes("doc"), "report.hwp");

		assertTrue(img.getPath().startsWith(imageRoot.toAbsolutePath().normalize()));
		assertEquals("image", img.getRootKey());
		assertTrue(doc.getPath().startsWith(storeRoot.toAbsolutePath().normalize()));
		assertEquals("store", doc.getRootKey());
		assertEquals("store", store.defaultRootKey());
		assertThrows(IllegalArgumentException.class, () -> store.store("pdf", bytes("x"), "a.pdf"));
	}

	// ---------------------------------------------------------------- 저장

	@Test
	@DisplayName("③ 경로가 섞인 원본명도 루트 바로 아래 새 저장명으로 놓이고 원본명은 보존된다")
	void originalNameNeverReachesPath() throws IOException {
		Path root = tmp.resolve("store");
		EgovFileStore store = EgovFileStore.builder().root("store", root).build();

		EgovStoredFile stored = store.store(bytes("secret"), "../../etc/passwd.txt");

		assertEquals(root.toAbsolutePath().normalize(), stored.getPath().getParent());
		assertTrue(stored.getStoredName().endsWith(".txt"));
		assertFalse(stored.getStoredName().contains("passwd"));
		assertEquals("../../etc/passwd.txt", stored.getOriginalName());
		assertEquals(6, stored.getSize());
		assertEquals(1, countFiles(root));
		assertFalse(Files.exists(tmp.resolve("etc/passwd.txt")));
	}

	@Test
	@DisplayName("④ 같은 원본명을 두 번 저장해도 덮어쓰지 않는다")
	void noOverwriteOnSameOriginalName() throws IOException {
		Path root = tmp.resolve("store");
		EgovFileStore store = EgovFileStore.builder().root("store", root).build();

		EgovStoredFile a = store.store(bytes("first"), "same.txt");
		EgovStoredFile b = store.store(bytes("second"), "same.txt");

		assertNotEquals(a.getStoredName(), b.getStoredName());
		assertEquals("first", Files.readString(a.getPath()));
		assertEquals("second", Files.readString(b.getPath()));
		assertEquals(2, countFiles(root));
	}

	@Test
	@DisplayName("⑤ 루트 디렉터리가 아직 없으면 저장 시 만들어진다")
	void createsMissingRoot() throws IOException {
		Path root = tmp.resolve("not/yet/there");
		assertFalse(Files.exists(root));
		EgovFileStore store = EgovFileStore.builder().root("store", root).build();

		EgovStoredFile stored = store.store(bytes("x"), "a.txt");

		assertTrue(Files.isDirectory(root));
		assertTrue(Files.exists(stored.getPath()));
	}

	// ---------------------------------------------------------------- 정책

	@Test
	@DisplayName("⑥ 정책 확장자 위반이면 실패하고 파일이 생기지 않는다")
	void policyExtensionRejectedLeavesNothing() throws IOException {
		Path root = tmp.resolve("store");
		EgovUploadPolicy pdfOnly = EgovUploadPolicy.builder().allowExtensions("pdf").build();
		EgovFileStore store = EgovFileStore.builder().root("store", root).policy(pdfOnly).build();

		EgovUploadRejectedException ex = assertThrows(EgovUploadRejectedException.class,
				() -> store.store(bytes("MZ"), "evil.exe"));

		assertEquals(EgovUploadPolicy.Reason.EXTENSION_NOT_ALLOWED, ex.getReason());
		assertEquals(0, countFiles(root));
	}

	@Test
	@DisplayName("⑦ 크기 초과는 기록 후 드러나도 부분 파일을 남기지 않는다")
	void policySizeExceededRemovesPartial() throws IOException {
		Path root = tmp.resolve("store");
		EgovUploadPolicy small = EgovUploadPolicy.builder().allowExtensions("bin").maxFileSize(1024).build();
		EgovFileStore store = EgovFileStore.builder().root("store", root).policy(small).build();
		byte[] twoKb = new byte[2048];

		EgovUploadRejectedException ex = assertThrows(EgovUploadRejectedException.class,
				() -> store.store(new ByteArrayInputStream(twoKb), "big.bin"));

		assertEquals(EgovUploadPolicy.Reason.SIZE_EXCEEDED, ex.getReason());
		assertEquals(0, countFiles(root));

		// Path 입력은 복사 전에 걸러진다
		Path src = tmp.resolve("src.bin");
		Files.write(src, twoKb);
		assertThrows(EgovUploadRejectedException.class, () -> store.store(src, "big.bin"));
		assertEquals(0, countFiles(root));
	}

	@Test
	@DisplayName("정책 통과 시 Path 입력도 저장되고 원본은 그대로 남는다")
	void pathSourceStoredWhenPolicyPasses() throws IOException {
		Path root = tmp.resolve("store");
		EgovUploadPolicy policy = EgovUploadPolicy.builder().allowExtensions("txt").maxFileSize(1024).build();
		EgovFileStore store = EgovFileStore.builder().root("store", root).policy(policy).build();
		Path src = tmp.resolve("src.txt");
		Files.writeString(src, "hello");

		EgovStoredFile stored = store.store(src, "hello.txt");

		assertEquals("hello", Files.readString(stored.getPath()));
		assertTrue(Files.exists(src));
		assertEquals(5, stored.getSize());
	}

	// ---------------------------------------------------------------- 재검증

	@Test
	@DisplayName("⑧ 변조된 보관 전체 경로(루트 밖)는 거부된다")
	void tamperedStoredPathRejected() throws IOException {
		Path root = tmp.resolve("store");
		Path outside = tmp.resolve("outside.txt");
		Files.writeString(outside, "x");
		EgovFileStore store = EgovFileStore.builder().root("store", root).build();

		assertThrows(IllegalArgumentException.class, () -> store.resolve(outside.toString()));
		assertThrows(IllegalArgumentException.class, () -> store.resolve("/etc/passwd"));
	}

	@Test
	@DisplayName("⑨ 저장명에 상위 이동이 섞이면 거부된다")
	void traversalInStoredNameRejected() {
		EgovFileStore store = EgovFileStore.builder().root("store", tmp.resolve("store")).build();

		assertThrows(IllegalArgumentException.class, () -> store.resolve("store", "../secret.txt"));
		assertThrows(IllegalArgumentException.class, () -> store.resolve("store", "/abs/secret.txt"));
	}

	@Test
	@DisplayName("⑩ 정상 왕복 — 루트 키 형태와 전체 경로 형태가 같은 경로·내용")
	void roundTripBothForms() throws IOException {
		Path root = tmp.resolve("store");
		EgovFileStore store = EgovFileStore.builder().root("store", root).root("image", tmp.resolve("image")).build();
		EgovStoredFile stored = store.store(bytes("payload"), "보고서.hwp");

		Path byKey = store.resolve(stored.getRootKey(), stored.getStoredName());
		Path byPath = store.resolve(stored.getPath().toString());
		Path byValue = store.resolve(stored);

		assertEquals(stored.getPath(), byKey);
		assertEquals(stored.getPath(), byPath);
		assertEquals(stored.getPath(), byValue);
		assertEquals("payload", Files.readString(byKey));
		assertTrue(stored.getStoredName().endsWith(".hwp"));
	}

	@Test
	@DisplayName("⑬ 널 바이트가 섞인 저장명·경로는 거부된다")
	void nullByteRejected() {
		EgovFileStore store = EgovFileStore.builder().root("store", tmp.resolve("store")).build();

		assertThrows(IllegalArgumentException.class, () -> store.resolve("store", "a\0.txt"));
		assertThrows(IllegalArgumentException.class, () -> store.resolve(tmp.resolve("store").toString() + "/a\0.txt"));
	}

	// ---------------------------------------------------------------- 삭제

	@Test
	@DisplayName("⑪ 루트 밖 실재 파일 삭제 요청은 거부되고 파일은 남는다")
	void deleteOutsideRootRejected() throws IOException {
		Path root = tmp.resolve("store");
		Path outside = tmp.resolve("keep-me.txt");
		Files.writeString(outside, "x");
		EgovFileStore store = EgovFileStore.builder().root("store", root).build();

		assertThrows(IllegalArgumentException.class, () -> store.delete(outside.toString()));
		assertThrows(IllegalArgumentException.class, () -> store.delete("store", "../keep-me.txt"));
		assertTrue(Files.exists(outside));
	}

	@Test
	@DisplayName("⑫ 이미 없는 파일 삭제는 false, 있으면 true")
	void deleteReportsWhetherRemoved() throws IOException {
		Path root = tmp.resolve("store");
		EgovFileStore store = EgovFileStore.builder().root("store", root).build();
		EgovStoredFile stored = store.store(bytes("x"), "a.txt");

		assertTrue(store.delete(stored));
		assertFalse(Files.exists(stored.getPath()));
		assertFalse(store.delete(stored));
		assertFalse(store.delete(stored.getRootKey(), stored.getStoredName()));
	}

	// ---------------------------------------------------------------- 기타

	@Test
	@DisplayName("keepExtension(false) 면 확장자 없는 저장명, 중복 루트 키는 거부")
	void builderOptions() throws IOException {
		Path root = tmp.resolve("store");
		EgovFileStore store = EgovFileStore.builder().root("store", root).keepExtension(false).build();

		EgovStoredFile stored = store.store(bytes("x"), "a.txt");
		assertFalse(stored.getStoredName().contains("."));

		assertThrows(IllegalArgumentException.class,
				() -> EgovFileStore.builder().root("store", root).root("store", tmp.resolve("other")));
		assertEquals(1, store.roots().size());
		assertFalse(store.policy().isPresent());
	}
}
