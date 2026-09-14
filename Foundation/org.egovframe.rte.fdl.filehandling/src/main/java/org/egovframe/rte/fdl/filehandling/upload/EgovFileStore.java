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
 * 신뢰 루트 위에서 업로드 파일의 <b>저장·재검증·삭제</b>를 한 번에 맡는 참조 구현.
 *
 * <p><b>NOTE:</b> 안전 경로 부품(저장명 {@link EgovStoredFileNames}, 경로 확정
 * {@link EgovFiles#resolveSecurely(Path, String)}·{@link EgovFiles#requireContainedIn(String, Path...)},
 * 정책 {@link EgovUploadPolicy})을 <b>올바른 순서로 조립</b>한 것이며 새 규칙을 두지 않는다.
 * 개발자가 매번 네 부품을 직접 엮지 않도록 하는 것이 목적이다. 웹 타입에 의존하지 않으므로
 * 컨트롤러·서비스·배치·리액티브 어디서나 같은 조립을 쓴다.</p>
 *
 * <pre>
 * EgovFileStore store = EgovFileStore.builder()
 *         .root("store", Paths.get("/data/upload"))        // 첫 루트 = 기본 루트
 *         .root("image", Paths.get("/data/image"))
 *         .policy(uploadPolicy)                             // 선택 — 이름·크기 검증 위임
 *         .build();
 *
 * // 업로드
 * EgovStoredFile stored = store.store(file.getInputStream(), file.getOriginalFilename());
 * // → DB 에는 stored.getRootKey()·getStoredName()·getOriginalName() 을 보관
 *
 * // 다운로드 — 읽는 시점에 다시 검증한다
 * Path verified = store.resolve(attach.getRootKey(), attach.getStoredName());
 * response.setHeader("Content-Disposition", EgovContentDispositions.attachment(attach.getOriginalName()));
 * Files.copy(verified, response.getOutputStream());
 * </pre>
 *
 * <p><b>허용 루트는 설정에서 오는 신뢰 값이어야 한다.</b> 루트가 하나도 없으면 만들 수 없다
 * (fail-closed). 저장·해석·삭제의 모든 경로는 루트 중 하나의 내부여야 하며, 벗어나면
 * {@link IllegalArgumentException} 이다.</p>
 *
 * <p><b>원본 이름은 경로에 쓰이지 않는다.</b> 저장명은 새로 만들고(확장자만 보존) 원본 이름은
 * 결과 값으로 돌려줄 뿐이다. 따라서 {@code ../../etc/passwd.txt} 같은 이름이 와도 루트 바로
 * 아래에 {@code <무작위>.txt} 로 놓인다.</p>
 *
 * <p><b>정책은 선택이며, 지정하지 않으면 이름·크기를 검증하지 않는다.</b> 웹 계층에서
 * {@code ptl.mvc} 어댑터로 이미 검증했다면 생략할 수 있지만, 서비스 계층 스토어에도 정책을
 * 붙여 두면 두 번 검증되어도 무해하고 우회 경로가 막힌다. 정책이 있으면 크기는 <b>실제로 기록된
 * 바이트 수</b>로 검증하며(선언 크기는 믿지 않는다), 위반이면 방금 쓴 파일을 지우고
 * {@link EgovUploadRejectedException} 을 던진다 — 부분 저장물은 남지 않는다.</p>
 *
 * <p><b>재검증 두 형태.</b> {@link #resolve(String, String)}(루트 키 + 저장명)가 권장 형태이고,
 * {@link #resolve(String)}(보관된 전체 경로)는 전체 경로를 DB 컬럼에 보관해 온 관행을 이전 기간
 * 동안 수용하기 위한 것이다. 두 형태 모두 루트 밖이면 거부한다. 재검증은 경로 포함 여부만
 * 판정하며 파일 존재 여부는 호출자가 본다.</p>
 *
 * <p><b>한계.</b> 저장 시점의 경로 검증은 어휘적(lexical) 정규화 기반이지만 저장명이 무작위라 링크를 미리 심을
 * 자리가 없고, 읽기·삭제 시점에는 실재하는 경로의 <b>실경로</b>가 루트 안인지 다시 확인하므로 루트 안에
 * 심긴 심볼릭 링크를 따라 밖을 읽거나 지우지 않는다. 저장은 루트 바로 아래 평면 배치이며, 하위
 * 디렉터리 단위로 나누려면 그 디렉터리를 별도 루트로 등록한다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.02  실행환경팀     최초 생성 (공통컴포넌트
 *                            EgovFileMngUtil.parseFileInf 의 조립 역할을 차용하되
 *                            문자 삭제형 경로 방어·시각 기반 저장명·다운로드 시 DB 경로
 *                            무검증을 부품 위임으로 재구성 — 코드 이식 아님)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovFileStore {

	/** 이름·확장자만으로 판정되는 정책 사유 — 크기를 모르는 시점의 선행 검사에 쓴다. */
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

	/**
	 * 루트 키 + 저장명으로 읽기 경로를 확정한다(권장 형태). 루트 밖이거나, 실재하는 경로가 심볼릭 링크를
	 * 거쳐 루트 밖을 가리키면 {@link IllegalArgumentException}.
	 */
	public Path resolve(String rootKey, String storedName) {
		return requireRealPathInside(EgovFiles.resolveSecurely(rootOf(rootKey), storedName));
	}

	/**
	 * 보관된 전체 경로 문자열을 허용 루트 전체에 대해 재검증한다(이전 기간 호환 형태).
	 * 루트 밖·해석 불가·널 바이트이거나, 실재하는 경로가 심볼릭 링크를 거쳐 루트 밖을 가리키면
	 * {@link IllegalArgumentException}.
	 */
	public Path resolve(String storedPath) {
		return requireRealPathInside(EgovFiles.requireContainedIn(storedPath, rootArray()));
	}

	/** 저장 결과의 경로가 여전히 허용 루트 안인지(실경로 포함) 확인해 돌려준다. */
	public Path resolve(EgovStoredFile stored) {
		Objects.requireNonNull(stored, "stored must not be null");
		return requireRealPathInside(EgovFiles.requireContainedIn(stored.getPath(), rootArray()));
	}

	// ----------------------------------------------------------------- delete

	/** 검증 뒤 삭제한다. 지운 파일이 있으면 true, 이미 없으면 false. 루트 밖이거나 링크를 거치면 예외. */
	public boolean delete(String rootKey, String storedName) {
		return deleteVerified(resolve(rootKey, storedName));
	}

	/** 보관된 전체 경로를 검증 뒤 삭제한다. 루트 밖이면 예외이며 파일시스템에 영향이 없다. */
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

	/**
	 * 어휘적 봉쇄를 통과한 경로가 <b>실재</b>하면 실경로로도 허용 루트 안인지 재확인한다 — 루트 안에 미리 심긴
	 * 심볼릭 링크를 따라 밖을 읽거나 지우는 일을 막는다. 아직 없는 경로는 어휘적 검사로 충분하다.
	 */
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

	/** 크기를 모르는 시점의 선행 검사 — 이름·확장자 사유만 정책에 묻는다(크기 사유는 기록 후 판정). */
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
			Files.createDirectories(root);
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

		/** 허용 루트를 등록한다. 같은 키를 다시 등록하면 예외(설정 중복은 실수다). */
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
