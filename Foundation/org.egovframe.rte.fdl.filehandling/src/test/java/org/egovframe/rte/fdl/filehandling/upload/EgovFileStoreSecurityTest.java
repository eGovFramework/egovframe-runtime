/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.fdl.filehandling.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

/**
 * EgovFileStore 의 <b>심볼릭 링크</b> 경계 검증(CWE-59).
 *
 * <p>루트 안에 밖을 가리키는 링크가 <b>미리 심겨 있다</b>는 전제(운영 규칙 위반)에서 저장·읽기·삭제가
 * 경계 밖을 건드리지 않음을 고정한다. 저장은 무작위 저장명이라 링크를 심을 자리가 없고, 읽기·삭제는
 * 실재하는 경로의 실경로를 루트와 대조해 링크를 거부한다. 링크 생성은 Windows 에서 권한이 필요하므로
 * 링크 테스트는 POSIX 계열에서만 실행한다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.07  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
class EgovFileStoreSecurityTest {

	@TempDir
	Path tempDir;

	private Path root;
	private Path outside;
	private Path secret;

	private void plant() throws IOException {
		root = Files.createDirectories(tempDir.resolve("root"));
		outside = Files.createDirectories(tempDir.resolve("outside"));
		secret = Files.write(outside.resolve("secret.txt"), "TOP-SECRET".getBytes(StandardCharsets.UTF_8));
		link(root.resolve("flink"), secret);
		link(root.resolve("dlink"), outside);
	}

	private static void link(Path at, Path to) {
		try {
			Files.createSymbolicLink(at, to);
		} catch (UnsupportedOperationException | IOException e) {
			assumeTrue(false, "이 파일시스템에서는 심볼릭 링크를 만들 수 없다: " + e);
		}
	}

	@Test
	@DisplayName("store 는 무작위 저장명이라 링크를 미리 심을 자리가 없고, 저장 결과는 루트 실경로 안이다")
	void store_는_무작위_저장명이라_링크를_미리_심을_자리가_없다() throws IOException {
		root = Files.createDirectories(tempDir.resolve("root"));
		EgovFileStore store = EgovFileStore.builder().root("up", root).build();

		EgovStoredFile a = store.store(new ByteArrayInputStream("a".getBytes(StandardCharsets.UTF_8)), "x.txt");
		EgovStoredFile b = store.store(new ByteArrayInputStream("b".getBytes(StandardCharsets.UTF_8)), "x.txt");

		assertFalse(a.getStoredName().equals(b.getStoredName()), "같은 원본명도 다른 저장명");
		assertTrue(a.getPath().toRealPath().startsWith(root.toRealPath()));
		assertTrue(a.getStoredName().matches("[0-9a-f]{32}\\.txt"), "예측 불가 UUID 저장명");
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	@DisplayName("resolve 는 루트 안의 링크가 밖을 가리키면 거부한다 — 파일 링크와 디렉터리 링크 모두")
	void resolve_는_밖을_가리키는_링크를_거부한다() throws IOException {
		plant();
		EgovFileStore store = EgovFileStore.builder().root("up", root).build();

		assertThrows(IllegalArgumentException.class, () -> store.resolve("up", "flink"));
		assertThrows(IllegalArgumentException.class, () -> store.resolve("up", "dlink/secret.txt"));
		assertThrows(IllegalArgumentException.class, () -> store.resolve(root.resolve("flink").toString()));
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	@DisplayName("delete 는 밖을 가리키는 링크를 거부하고, 링크도 링크 대상도 건드리지 않는다")
	void delete_는_링크를_거부하고_링크_대상을_지우지_않는다() throws IOException {
		plant();
		EgovFileStore store = EgovFileStore.builder().root("up", root).build();

		assertThrows(IllegalArgumentException.class, () -> store.delete("up", "flink"));

		assertTrue(Files.exists(root.resolve("flink"), LinkOption.NOFOLLOW_LINKS), "링크는 그대로 남는다(정리는 운영자 몫)");
		assertTrue(Files.exists(secret), "링크 대상(경계 밖)은 생존");
		assertEquals("TOP-SECRET", new String(Files.readAllBytes(secret), StandardCharsets.UTF_8));
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	@DisplayName("루트 자체가 링크로 마운트된 정상 배치는 실경로 재확인에 걸리지 않는다 — 오탐 방지")
	void 루트가_링크인_정상_배치는_통과한다() throws IOException {
		Path realRoot = Files.createDirectories(tempDir.resolve("volume").resolve("upload"));
		Path linkedRoot = tempDir.resolve("root-link");
		link(linkedRoot, realRoot);
		EgovFileStore store = EgovFileStore.builder().root("up", linkedRoot).build();

		EgovStoredFile saved = store.store(new ByteArrayInputStream("ok".getBytes(StandardCharsets.UTF_8)), "ok.txt");

		Path resolved = store.resolve("up", saved.getStoredName());
		assertEquals("ok", new String(Files.readAllBytes(resolved), StandardCharsets.UTF_8));
		assertTrue(store.delete("up", saved.getStoredName()));
	}
}
