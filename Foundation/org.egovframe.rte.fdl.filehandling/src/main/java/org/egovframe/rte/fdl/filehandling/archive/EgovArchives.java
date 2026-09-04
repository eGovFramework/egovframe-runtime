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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import org.egovframe.rte.fdl.filehandling.EgovFiles;

/**
 * 디렉터리 아카이브 생성·해제(zip/tar) — 경로 조작(Zip Slip)과 압축 폭탄을 봉쇄한 표준 구현.
 *
 * <p><b>NOTE:</b> 아카이브 해제는 <b>아카이브 안의 문자열이 파일시스템 경로가 되는</b> 지점이라
 * 경로 조작 공격(Zip Slip, CWE-22)의 표적이 된다. 모든 엔트리는
 * {@link EgovFiles#resolveSecurely(Path, String)}로 대상 디렉터리 내부임이 확정된 뒤에만
 * 만들어지며, 탈출·절대 경로·널 바이트 엔트리는 {@link IllegalArgumentException}으로 거부된다.
 * 엔트리 수·누적 크기는 {@link EgovExtractLimits}로 제한한다(기본 한도 적용).</p>
 *
 * <pre>
 * long entries = EgovArchives.zipDirectory(dataDir, backupDir.resolve("data.zip"));
 *
 * EgovArchives.unzip(zipFile, restoreDir);                                  // UTF-8·기본 한도
 * EgovArchives.unzip(zipFile, restoreDir, Charset.forName("MS949"));        // 구형 윈도우 zip
 * EgovArchives.untar(tarFile, restoreDir, EgovExtractLimits.unlimited());   // 신뢰 가능한 대용량 백업
 * </pre>
 *
 * <p><b>형식.</b> zip 은 JDK 표준({@code java.util.zip})만 쓰므로 추가 의존이 없다. 엔트리 이름은
 * 쓰기 시 UTF-8 로 기록하고, 읽기는 UTF-8 기본에 문자셋 오버로드를 제공한다(구형 윈도우·알집이
 * 만든 zip 은 엔트리 이름이 MS949 인 경우가 많다). tar 는 Apache commons-compress 를 쓰며 이
 * 모듈에 <b>optional</b> 의존으로 선언되어 있다 — tar 메서드를 쓰는 모듈은 자기 pom 에
 * {@code org.apache.commons:commons-compress} 를 직접 선언해야 한다(zip 만 쓰면 불필요).
 * tar 쓰기는 POSIX(pax) 확장 헤더를 켜서 긴 이름·비ASCII 이름을 표준 방식으로 기록한다.</p>
 *
 * <p><b>계약.</b> 생성 대상이 빈 디렉터리이면 조용히 빈 결과를 만드는 대신
 * {@link IllegalArgumentException}으로 정직하게 실패한다. 해제는 기존 파일을 대체하며, 실패
 * 시점까지 만든 파일은 남는다 — 신뢰 경계를 넘는 아카이브는 임시 디렉터리에 풀어 검증 후
 * {@link EgovFiles#move}로 옮기는 구성을 권장한다. tar 의 링크·디바이스 엔트리는 거부한다
 * (링크를 따라 대상 디렉터리 밖에 쓰는 고전적 우회 차단). zip·tar 모두 쓰기 직전에 실경로를
 * 재확인하므로, 대상 디렉터리에 미리 심긴 심볼릭 링크를 거치는 엔트리도 거부한다.</p>
 *
 * <p>공통컴포넌트 {@code EgovFileCmprs}·{@code BackupJob}의 결함을 해소한 재구성이다 —
 * 원본은 ① Zip Slip 방어가 블랙리스트 문자열 치환({@code filePathBlackList}) 한 줄이고
 * ② 이름과 달리 압축 생성 기능이 없으며({@code EgovFileCmprs}) ③ 아카이브 엔트리 이름에
 * <b>서버 절대 경로를 그대로 기록</b>하고({@code BackupJob}) ④ 빈 디렉터리를 만나면 예외 없이
 * {@code false}를 반환했다. 설계 목적만 차용하고 구현은 전량 재작성했다(코드 이식 아님).</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (압축·해제 표준 구현 —
 *                            Zip Slip 방어를 EgovFiles 안전 경로 해석 기반으로 재작성)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovArchives {

	private static final int COPY_BUFFER_SIZE = 64 * 1024;

	private EgovArchives() {
	}

	// ---------------------------------------------------------------- 생성

	/**
	 * 디렉터리 하위 전체(빈 하위 디렉터리 포함)를 zip 으로 만든다. 엔트리 이름은 원본 디렉터리
	 * 기준 <b>상대 경로</b>이며 UTF-8 로 기록한다.
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

	/**
	 * 디렉터리 하위 전체(빈 하위 디렉터리 포함)를 tar 로 만든다. POSIX(pax) 확장 헤더로
	 * 긴 이름·비ASCII 이름을 기록한다.
	 *
	 * <p>commons-compress(optional 의존)가 클래스패스에 있어야 한다.</p>
	 *
	 * @param sourceDir 아카이브할 디렉터리
	 * @param tarFile   만들 tar 파일(부모 디렉터리는 자동 생성, 원본 디렉터리 밖이어야 함)
	 * @return 기록한 엔트리 수
	 * @throws IllegalArgumentException 원본이 디렉터리가 아니거나 비었거나, tar 파일이 원본 내부인 경우
	 * @throws UncheckedIOException     입출력 실패(부분 생성된 tar 파일은 삭제 시도)
	 */
	public static long tarDirectory(Path sourceDir, Path tarFile) {
		Path source = requireSourceDirectory(sourceDir, tarFile);
		List<Path> entries = collectEntries(source);
		try {
			createParentDirectories(tarFile);
			try (TarArchiveOutputStream tos =
					new TarArchiveOutputStream(Files.newOutputStream(tarFile), StandardCharsets.UTF_8.name())) {
				tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
				tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
				tos.setAddPaxHeadersForNonAsciiNames(true);
				for (Path path : entries) {
					boolean directory = Files.isDirectory(path);
					TarArchiveEntry entry = new TarArchiveEntry(path, entryName(source, path, directory));
					tos.putArchiveEntry(entry);
					if (!directory) {
						Files.copy(path, tos);
					}
					tos.closeArchiveEntry();
				}
			}
			return entries.size();
		} catch (IOException e) {
			throw cleanupAndWrap("Failed to create tar archive: " + tarFile, tarFile, e);
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
	 * 엔트리 이름 문자셋을 지정해 zip 을 해제한다(기본 한도). 구형 윈도우·알집이 만든 zip 은
	 * {@code Charset.forName("MS949")} 를 지정해야 한글 파일명이 깨지지 않는다.
	 *
	 * @return 처리한 엔트리 수
	 * @see #unzip(Path, Path, Charset, EgovExtractLimits)
	 */
	public static long unzip(Path zipFile, Path targetDir, Charset entryNameCharset) {
		return unzip(zipFile, targetDir, entryNameCharset, EgovExtractLimits.defaults());
	}

	/**
	 * zip 을 대상 디렉터리에 해제한다 — 모든 엔트리는 대상 디렉터리 내부로 확정된 뒤에만
	 * 만들어지고(Zip Slip 봉쇄), 실제 엔트리 수·실제 기록 바이트가 한도를 넘으면 쓰던 파일을 지우고 즉시 중단한다.
	 * 쓰기 직전에는 실경로를 재확인해, 대상 디렉터리 안에 미리 심긴 심볼릭 링크를 따라 밖에 쓰는 일을 막는다.
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
			Files.createDirectories(targetDir);
			Path targetDirReal = targetDir.toRealPath();
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				entryCount++;
				checkEntryCount(entryCount, limits, zipFile);
				Path target = resolveEntryTarget(targetDir, entry.getName(), zipFile);
				requireRealPathInside(target, targetDir, targetDirReal, entry.getName(), zipFile);
				if (entry.isDirectory()) {
					Files.createDirectories(target);
				} else {
					Files.createDirectories(target.getParent());
					totalBytes = copyLimited(zis, target, totalBytes, limits, zipFile);
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to extract zip archive: " + zipFile, e);
		}
		return entryCount;
	}

	/**
	 * tar 를 대상 디렉터리에 해제한다(기본 한도).
	 *
	 * @return 처리한 엔트리 수
	 * @see #untar(Path, Path, EgovExtractLimits)
	 */
	public static long untar(Path tarFile, Path targetDir) {
		return untar(tarFile, targetDir, EgovExtractLimits.defaults());
	}

	/**
	 * tar 를 대상 디렉터리에 해제한다 — Zip Slip 봉쇄·한도 적용은 {@code unzip} 과 같고,
	 * 심볼릭 링크·하드 링크·디바이스 엔트리는 거부한다(링크를 따라 대상 밖에 쓰는 우회 차단).
	 *
	 * <p>commons-compress(optional 의존)가 클래스패스에 있어야 한다.</p>
	 *
	 * @param tarFile   해제할 tar 파일
	 * @param targetDir 해제 대상 디렉터리(없으면 생성)
	 * @param limits    해제 한도
	 * @return 처리한 엔트리 수(빈 아카이브는 0)
	 * @throws IllegalArgumentException 탈출·절대 경로 엔트리, 링크·디바이스 엔트리, 링크를 거치는 경로, 한도 초과
	 * @throws UncheckedIOException     입출력 실패·tar 형식 오류
	 */
	public static long untar(Path tarFile, Path targetDir, EgovExtractLimits limits) {
		Objects.requireNonNull(tarFile, "tarFile must not be null");
		Objects.requireNonNull(targetDir, "targetDir must not be null");
		Objects.requireNonNull(limits, "limits must not be null");
		long entryCount = 0;
		long totalBytes = 0;
		try (TarArchiveInputStream tis =
				new TarArchiveInputStream(Files.newInputStream(tarFile), StandardCharsets.UTF_8.name())) {
			Files.createDirectories(targetDir);
			Path targetDirReal = targetDir.toRealPath();
			TarArchiveEntry entry;
			while ((entry = tis.getNextEntry()) != null) {
				entryCount++;
				checkEntryCount(entryCount, limits, tarFile);
				if (entry.isSymbolicLink() || entry.isLink()) {
					throw new IllegalArgumentException(
							"link entry is not allowed (link extraction blocked): " + entry.getName() + " in " + tarFile);
				}
				if (entry.isCharacterDevice() || entry.isBlockDevice() || entry.isFIFO()) {
					throw new IllegalArgumentException(
							"device/fifo entry is not allowed: " + entry.getName() + " in " + tarFile);
				}
				Path target = resolveEntryTarget(targetDir, entry.getName(), tarFile);
				requireRealPathInside(target, targetDir, targetDirReal, entry.getName(), tarFile);
				if (entry.isDirectory()) {
					Files.createDirectories(target);
				} else {
					Files.createDirectories(target.getParent());
					totalBytes = copyLimited(tis, target, totalBytes, limits, tarFile);
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to extract tar archive: " + tarFile, e);
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

	/**
	 * 아카이브할 엔트리를 수집한다 — 파일과 <b>모든 하위 디렉터리</b>(빈 디렉터리 보존).
	 * 빈 원본은 조용한 빈 결과 대신 예외로 정직하게 실패한다.
	 */
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

	/** 원본 디렉터리 기준 상대 경로 엔트리 이름 — 구분자는 {@code /}, 디렉터리는 {@code /} 접미. */
	private static String entryName(Path source, Path path, boolean directory) {
		String relative = source.relativize(path).toString().replace('\\', '/');
		return directory ? relative + "/" : relative;
	}

	/** 엔트리 이름을 대상 디렉터리 내부로 확정한다 — Zip Slip 봉쇄 지점. */
	private static Path resolveEntryTarget(Path targetDir, String rawEntryName, Path archive) {
		String name = rawEntryName.replace('\\', '/');
		try {
			return EgovFiles.resolveSecurely(targetDir, name);
		} catch (InvalidPathException e) {
			throw new IllegalArgumentException(
					"archive entry name is not a valid path: " + rawEntryName + " in " + archive, e);
		}
	}

	/**
	 * 쓰기 직전 <b>실경로</b> 재확인 — 어휘적 봉쇄를 통과한 경로라도 대상 디렉터리 안에 미리 심긴 심볼릭 링크를
	 * 거치면 밖에 쓰게 된다. 이미 존재하는 가장 깊은 구성요소가 링크이거나 그 실경로가 대상 디렉터리 실경로
	 * 밖이면 거부한다. 아직 없는 구성요소는 이 확인 뒤에 직접 만들므로 링크일 수 없다.
	 */
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

	/**
	 * 현재 엔트리 내용을 복사한다 — 헤더의 크기 선언이 아니라 <b>실제 기록 바이트</b>에 한도를 적용한다.
	 * 한도를 넘으면 쓰던 파일을 지우고 예외를 던진다(거부한 엔트리의 부분 파일을 남기지 않는다).
	 */
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
			// 한도 초과로 거부한 엔트리의 부분 파일은 남기지 않는다(앞서 정상 해제된 항목은 그대로 둔다)
			Files.deleteIfExists(target);
			throw limitExceeded;
		}
		return total;
	}

	private static void createParentDirectories(Path file) throws IOException {
		Path parent = file.toAbsolutePath().getParent();
		if (parent != null) {
			Files.createDirectories(parent);
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
