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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.egovframe.rte.fdl.filehandling.EgovFiles;

/**
 * 허용 루트 위에서 업로드 파일의 저장·재검증·삭제를 담당하는 참조 구현.
 * 저장명은 새로 생성하고(확장자만 보존) 원본 이름은 경로에 쓰지 않는다.
 * 모든 경로는 허용 루트 내부로 제한하며, 읽기·삭제 시 실경로로 재확인한다(심볼릭 링크 우회 방지).
 *
 * <pre>
 * EgovFileStore store = EgovFileStore.builder().root("store", Paths.get("/data/upload")).policy(uploadPolicy).build();
 * EgovStoredFile stored = store.store(file.getInputStream(), file.getOriginalFilename());
 * Path verified = store.resolve(attach.getRootKey(), attach.getStoredName());
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.02  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 4.3.1
 */
public final class EgovFileStore {

	/** 크기 기록 전 선행 검사에 쓰는 이름·확장자 정책 사유. */
	private static final Set<EgovUploadPolicy.Reason> NAME_REASONS = Collections.unmodifiableSet(EnumSet.of(
			EgovUploadPolicy.Reason.NO_FILENAME, EgovUploadPolicy.Reason.INVALID_FILENAME,
			EgovUploadPolicy.Reason.NO_EXTENSION, EgovUploadPolicy.Reason.EXTENSION_NOT_ALLOWED));

	private final Map<String, Path> roots;
	private final String defaultRootKey;
	private final EgovUploadPolicy policy;
	private final boolean keepExtension;

	private EgovFileStore(Builder builder) {
		if (builder.roots.isEmpty()) {
			throw new IllegalArgumentException("at least one root directory is required (fail-closed)");
		}
		this.roots = Collections.unmodifiableMap(new LinkedHashMap<>(builder.roots));
		this.defaultRootKey = roots.keySet().iterator().next();
		this.policy = builder.policy;
		this.keepExtension = builder.keepExtension;
	}

	public static Builder builder() {
		return new Builder();
	}

	// ------------------------------------------------------------------ store

	/** 기본(첫 번째) 루트에 저장한다. 스트림은 끝까지 읽되 닫지 않는다(호출자 소유). */
	public EgovStoredFile store(InputStream source, String originalName) {
		return store(defaultRootKey, source, originalName);
	}

	/** 지정 루트에 저장한다. 스트림은 끝까지 읽되 닫지 않는다(호출자 소유). */
	public EgovStoredFile store(String rootKey, InputStream source, String originalName) {
		Objects.requireNonNull(source, "source must not be null");
		Path root = rootOf(rootKey);
		precheckName(originalName);
		Path target = prepareTarget(root, originalName);
		long size;
		try {
			size = Files.copy(source, target);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to store uploaded file into " + root, e);
		}
		validateSizeOrDelete(originalName, size, target);
		return new EgovStoredFile(rootKey, target.getFileName().toString(), target, originalName, size);
	}

	/** 기본(첫 번째) 루트에 기존 파일을 복사해 저장한다. 원본 파일은 그대로 둔다. */
	public EgovStoredFile store(Path source, String originalName) {
		return store(defaultRootKey, source, originalName);
	}

	/** 지정 루트에 기존 파일을 복사해 저장한다. 크기는 복사 전에 정책으로 거른다. */
	public EgovStoredFile store(String rootKey, Path source, String originalName) {
		Objects.requireNonNull(source, "source must not be null");
		Path root = rootOf(rootKey);
		long declared;
		try {
			declared = Files.size(source);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to read size of " + source, e);
		}
		if (policy != null) {
			policy.validate(originalName, declared);
		}
		Path target = prepareTarget(root, originalName);
		long size;
		try {
			Files.copy(source, target);
			size = Files.size(target);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to store file " + source + " into " + root, e);
		}
		validateSizeOrDelete(originalName, size, target);
		return new EgovStoredFile(rootKey, target.getFileName().toString(), target, originalName, size);
	}

	// ---------------------------------------------------------------- resolve

	/** 루트 키와 저장명으로 읽기 경로를 확정한다. 루트 밖이면 {@link IllegalArgumentException}. */
	public Path resolve(String rootKey, String storedName) {
		return requireRealPathInside(EgovFiles.resolveSecurely(rootOf(rootKey), storedName));
	}

	/** 보관된 전체 경로를 허용 루트 기준으로 재검증한다(호환용). 루트 밖이면 {@link IllegalArgumentException}. */
	public Path resolve(String storedPath) {
		return requireRealPathInside(EgovFiles.requireContainedIn(storedPath, rootArray()));
	}

	/** 저장 결과의 경로가 여전히 허용 루트 안인지(실경로 포함) 확인해 돌려준다. */
	public Path resolve(EgovStoredFile stored) {
		Objects.requireNonNull(stored, "stored must not be null");
		return requireRealPathInside(EgovFiles.requireContainedIn(stored.getPath(), rootArray()));
	}

	// ----------------------------------------------------------------- delete

	/** 검증 뒤 삭제한다. 삭제했으면 true, 파일이 없으면 false. */
	public boolean delete(String rootKey, String storedName) {
		return deleteVerified(resolve(rootKey, storedName));
	}

	/** 보관된 전체 경로를 검증 뒤 삭제한다. */
	public boolean delete(String storedPath) {
		return deleteVerified(resolve(storedPath));
	}

	/** 저장 결과를 검증 뒤 삭제한다. */
	public boolean delete(EgovStoredFile stored) {
		return deleteVerified(resolve(stored));
	}

	// ------------------------------------------------------------------ query

	/** 등록된 허용 루트(키 → 정규화된 절대 경로, 등록 순서). */
	public Map<String, Path> roots() {
		return roots;
	}

	/** 기본 루트 키(첫 번째로 등록한 루트). */
	public String defaultRootKey() {
		return defaultRootKey;
	}

	/** 지정된 정책(없으면 빈 값). */
	public Optional<EgovUploadPolicy> policy() {
		return Optional.ofNullable(policy);
	}

	// --------------------------------------------------------------- internal

	/** 실재하는 경로는 실경로가 허용 루트 안인지 재확인한다(심볼릭 링크 우회 방지). */
	private Path requireRealPathInside(Path lexical) {
		if (!Files.exists(lexical, LinkOption.NOFOLLOW_LINKS)) {
			return lexical;
		}
		if (Files.isSymbolicLink(lexical)) {
			throw new IllegalArgumentException(
					"stored path is a symbolic link (link traversal blocked): " + lexical.getFileName());
		}
		try {
			Path real = lexical.toRealPath();
			for (Path root : roots.values()) {
				if (Files.isDirectory(root) && real.startsWith(root.toRealPath())) {
					return lexical;
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to resolve the real path of " + lexical.getFileName(), e);
		}
		throw new IllegalArgumentException(
				"stored path resolves outside the allowed roots (link traversal blocked): " + lexical.getFileName());
	}

	private Path rootOf(String rootKey) {
		Path root = roots.get(rootKey);
		if (root == null) {
			throw new IllegalArgumentException(
					"unknown root key: " + rootKey + " (registered: " + roots.keySet() + ")");
		}
		return root;
	}

	private Path[] rootArray() {
		return roots.values().toArray(new Path[0]);
	}

	/** 기록 전 이름·확장자만 정책으로 검사한다. */
	private void precheckName(String originalName) {
		if (policy == null) {
			return;
		}
		policy.check(originalName, 1L).filter(NAME_REASONS::contains).ifPresent(reason -> {
			throw new EgovUploadRejectedException(reason, originalName);
		});
	}

	private Path prepareTarget(Path root, String originalName) {
		String storedName = keepExtension
				? EgovStoredFileNames.generate(originalName)
				: EgovStoredFileNames.generateWithoutExtension();
		Path target = EgovFiles.resolveSecurely(root, storedName);
		try {
			EgovFiles.createDirectories(root);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to prepare root directory " + root, e);
		}
		return target;
	}

	private void validateSizeOrDelete(String originalName, long size, Path target) {
		if (policy == null) {
			return;
		}
		Optional<EgovUploadPolicy.Reason> reason = policy.check(originalName, size);
		if (reason.isPresent()) {
			try {
				Files.deleteIfExists(target);
			} catch (IOException e) {
				throw new UncheckedIOException("Rejected by policy (" + reason.get()
						+ ") but failed to remove partial file " + target, e);
			}
			throw new EgovUploadRejectedException(reason.get(), originalName);
		}
	}

	private static boolean deleteVerified(Path verified) {
		try {
			return Files.deleteIfExists(verified);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to delete " + verified, e);
		}
	}

	// ---------------------------------------------------------------- builder

	/** {@link EgovFileStore} 빌더. 루트는 등록 순서를 유지하며 첫 번째가 기본 루트다. */
	public static final class Builder {
		private final Map<String, Path> roots = new LinkedHashMap<>();
		private EgovUploadPolicy policy;
		private boolean keepExtension = true;

		private Builder() {
		}

		/** 허용 루트를 등록한다. 키가 중복되면 예외. */
		public Builder root(String key, Path directory) {
			if (key == null || key.trim().isEmpty()) {
				throw new IllegalArgumentException("root key must not be null or empty");
			}
			Objects.requireNonNull(directory, "root directory must not be null");
			if (roots.containsKey(key)) {
				throw new IllegalArgumentException("duplicate root key: " + key);
			}
			roots.put(key, directory.toAbsolutePath().normalize());
			return this;
		}

		/** 이름·크기 검증을 위임할 정책(선택). 미지정이면 검증하지 않는다. */
		public Builder policy(EgovUploadPolicy policy) {
			this.policy = policy;
			return this;
		}

		/** 저장명에 원본 확장자를 보존할지(기본 true). false 면 확장자 없는 저장명. */
		public Builder keepExtension(boolean keepExtension) {
			this.keepExtension = keepExtension;
			return this;
		}

		public EgovFileStore build() {
			return new EgovFileStore(this);
		}
	}
}
