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
package org.egovframe.rte.fdl.filehandling.archive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


import org.egovframe.rte.fdl.filehandling.EgovFiles;

/**
 * 디렉터리 zip 생성·해제 유틸리티.
 * 엔트리 경로는 대상 디렉터리 내부로 제한(Zip Slip 방지)하고, 엔트리 수·누적 크기는 {@link EgovExtractLimits}로 제한한다.
 * JDK 표준({@code java.util.zip})만 사용하며 tar 형식은 제공하지 않는다.
 *
 * <pre>
 * EgovArchives.zipDirectory(dataDir, backupDir.resolve("data.zip"));
 * EgovArchives.unzip(zipFile, restoreDir);                              // UTF-8, 기본 한도
 * EgovArchives.unzip(zipFile, restoreDir, Charset.forName("MS949"));    // 구형 윈도우 zip
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.10.25  실행환경팀     최초 생성 (5.1.0 기능 이식)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 4.3.1
 */
public final class EgovArchives {

	private static final int COPY_BUFFER_SIZE = 64 * 1024;

	private EgovArchives() {
	}

	// ---------------------------------------------------------------- 생성

	/**
	 * 디렉터리 하위 전체(빈 디렉터리 포함)를 zip으로 만든다. 엔트리 이름은 상대 경로(UTF-8)로 기록한다.
	 *
	 * @param sourceDir 아카이브할 디렉터리
	 * @param zipFile   만들 zip 파일(부모 디렉터리는 자동 생성, 원본 디렉터리 밖이어야 함)
	 * @return 기록한 엔트리 수
	 * @throws IllegalArgumentException 원본이 디렉터리가 아니거나 비었거나, zip 파일이 원본 내부인 경우
	 * @throws UncheckedIOException     입출력 실패(부분 생성된 zip 파일은 삭제 시도)
	 */
	public static long zipDirectory(Path sourceDir, Path zipFile) {
		Path source = requireSourceDirectory(sourceDir, zipFile);
		List<Path> entries = collectEntries(source);
		try {
			createParentDirectories(zipFile);
			try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile), StandardCharsets.UTF_8)) {
				for (Path path : entries) {
					boolean directory = Files.isDirectory(path);
					zos.putNextEntry(new ZipEntry(entryName(source, path, directory)));
					if (!directory) {
						Files.copy(path, zos);
					}
					zos.closeEntry();
				}
			}
			return entries.size();
		} catch (IOException e) {
			throw cleanupAndWrap("Failed to create zip archive: " + zipFile, zipFile, e);
		}
	}

	// ---------------------------------------------------------------- 해제

	/**
	 * zip 을 대상 디렉터리에 해제한다(UTF-8 엔트리 이름·기본 한도).
	 *
	 * @return 처리한 엔트리 수
	 * @see #unzip(Path, Path, Charset, EgovExtractLimits)
	 */
	public static long unzip(Path zipFile, Path targetDir) {
		return unzip(zipFile, targetDir, StandardCharsets.UTF_8, EgovExtractLimits.defaults());
	}

	/**
	 * 엔트리 이름 문자셋을 지정해 zip을 해제한다(기본 한도). 구형 윈도우 zip은 MS949를 지정한다.
	 *
	 * @return 처리한 엔트리 수
	 * @see #unzip(Path, Path, Charset, EgovExtractLimits)
	 */
	public static long unzip(Path zipFile, Path targetDir, Charset entryNameCharset) {
		return unzip(zipFile, targetDir, entryNameCharset, EgovExtractLimits.defaults());
	}

	/**
	 * zip을 대상 디렉터리에 해제한다. 엔트리는 대상 디렉터리 내부로 제한하며 한도 초과 시 즉시 중단한다.
	 * 기존 파일은 대체한다.
	 *
	 * @param zipFile          해제할 zip 파일
	 * @param targetDir        해제 대상 디렉터리(없으면 생성)
	 * @param entryNameCharset 엔트리 이름 문자셋(쓰기 표준은 UTF-8)
	 * @param limits           해제 한도
	 * @return 처리한 엔트리 수(빈 아카이브는 0)
	 * @throws IllegalArgumentException 탈출·절대 경로 엔트리, 링크를 거치는 경로, 한도 초과, 해석 불가 엔트리 이름
	 * @throws UncheckedIOException     입출력 실패·zip 형식 오류
	 */
	public static long unzip(Path zipFile, Path targetDir, Charset entryNameCharset, EgovExtractLimits limits) {
		Objects.requireNonNull(zipFile, "zipFile must not be null");
		Objects.requireNonNull(targetDir, "targetDir must not be null");
		Objects.requireNonNull(entryNameCharset, "entryNameCharset must not be null");
		Objects.requireNonNull(limits, "limits must not be null");
		long entryCount = 0;
		long totalBytes = 0;
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile), entryNameCharset)) {
			EgovFiles.createDirectories(targetDir);
			Path targetDirReal = targetDir.toRealPath();
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				entryCount++;
				checkEntryCount(entryCount, limits, zipFile);
				Path target = resolveEntryTarget(targetDir, entry.getName(), zipFile);
				requireRealPathInside(target, targetDir, targetDirReal, entry.getName(), zipFile);
				if (entry.isDirectory()) {
					EgovFiles.createDirectories(target);
				} else {
					EgovFiles.createDirectories(target.getParent());
					totalBytes = copyLimited(zis, target, totalBytes, limits, zipFile);
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to extract zip archive: " + zipFile, e);
		}
		return entryCount;
	}

	// ---------------------------------------------------------------- 내부

	private static Path requireSourceDirectory(Path sourceDir, Path archiveFile) {
		Objects.requireNonNull(sourceDir, "sourceDir must not be null");
		Objects.requireNonNull(archiveFile, "archiveFile must not be null");
		Path source = sourceDir.toAbsolutePath().normalize();
		if (!Files.isDirectory(source)) {
			throw new IllegalArgumentException("source is not an existing directory: " + sourceDir);
		}
		if (EgovFiles.isContainedIn(archiveFile, source)) {
			throw new IllegalArgumentException(
					"archive file must not be inside the source directory: " + archiveFile);
		}
		return source;
	}

	/** 아카이브할 파일과 하위 디렉터리를 수집한다. 원본이 비었으면 예외. */
	private static List<Path> collectEntries(Path source) {
		List<Path> entries;
		try (Stream<Path> walk = Files.walk(source)) {
			entries = walk.filter(path -> !path.equals(source)).sorted().collect(Collectors.toList());
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to scan source directory: " + source, e);
		}
		if (entries.isEmpty()) {
			throw new IllegalArgumentException("source directory is empty, nothing to archive: " + source);
		}
		return entries;
	}

	/** 원본 디렉터리 기준 상대 경로 엔트리 이름({@code /} 구분, 디렉터리는 {@code /} 접미). */
	private static String entryName(Path source, Path path, boolean directory) {
		String relative = source.relativize(path).toString().replace('\\', '/');
		return directory ? relative + "/" : relative;
	}

	/** 엔트리 이름을 대상 디렉터리 내부 경로로 확정한다(Zip Slip 방지). */
	private static Path resolveEntryTarget(Path targetDir, String rawEntryName, Path archive) {
		String name = rawEntryName.replace('\\', '/');
		try {
			return EgovFiles.resolveSecurely(targetDir, name);
		} catch (InvalidPathException e) {
			throw new IllegalArgumentException(
					"archive entry name is not a valid path: " + rawEntryName + " in " + archive, e);
		}
	}

	/** 쓰기 직전 실경로가 대상 디렉터리 안인지 재확인한다(심볼릭 링크 우회 방지). */
	private static void requireRealPathInside(Path target, Path targetDir, Path targetDirReal,
			String entryName, Path archive) throws IOException {
		Path base = targetDir.toAbsolutePath().normalize();
		Path probe = target;
		while (probe != null && !probe.equals(base) && !Files.exists(probe, LinkOption.NOFOLLOW_LINKS)) {
			probe = probe.getParent();
		}
		if (probe == null || probe.equals(base)) {
			return;
		}
		if (Files.isSymbolicLink(probe)) {
			throw new IllegalArgumentException("archive entry path goes through a symbolic link "
					+ "(link traversal blocked): " + entryName + " in " + archive);
		}
		if (!probe.toRealPath().startsWith(targetDirReal)) {
			throw new IllegalArgumentException("archive entry resolves outside the target directory "
					+ "(link traversal blocked): " + entryName + " in " + archive);
		}
	}

	private static void checkEntryCount(long entryCount, EgovExtractLimits limits, Path archive) {
		if (entryCount > limits.getMaxEntries()) {
			throw new IllegalArgumentException("archive exceeds the entry count limit ("
					+ limits.getMaxEntries() + " entries): " + archive);
		}
	}

	/** 엔트리 내용을 복사한다. 실제 기록 바이트가 한도를 넘으면 부분 파일을 지우고 예외를 던진다. */
	private static long copyLimited(InputStream in, Path target, long totalSoFar,
			EgovExtractLimits limits, Path archive) throws IOException {
		long total = totalSoFar;
		byte[] buffer = new byte[COPY_BUFFER_SIZE];
		try (OutputStream out = Files.newOutputStream(target)) {
			int read;
			while ((read = in.read(buffer)) != -1) {
				total += read;
				if (total > limits.getMaxTotalBytes()) {
					throw new IllegalArgumentException("archive exceeds the total extracted size limit ("
							+ limits.getMaxTotalBytes() + " bytes): " + archive);
				}
				out.write(buffer, 0, read);
			}
		} catch (IllegalArgumentException limitExceeded) {
			// 한도 초과 엔트리의 부분 파일 삭제
			Files.deleteIfExists(target);
			throw limitExceeded;
		}
		return total;
	}

	private static void createParentDirectories(Path file) throws IOException {
		Path parent = file.toAbsolutePath().getParent();
		if (parent != null) {
			EgovFiles.createDirectories(parent);
		}
	}

	private static UncheckedIOException cleanupAndWrap(String message, Path archiveFile, IOException cause) {
		try {
			Files.deleteIfExists(archiveFile);
		} catch (IOException cleanupFailure) {
			cause.addSuppressed(cleanupFailure);
		}
		return new UncheckedIOException(message, cause);
	}

}
