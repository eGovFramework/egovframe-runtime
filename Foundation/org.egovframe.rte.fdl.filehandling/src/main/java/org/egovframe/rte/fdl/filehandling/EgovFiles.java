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
package org.egovframe.rte.fdl.filehandling;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * NIO.2({@code java.nio.file}) 기반 로컬 파일 유틸리티.
 * 전역 상태 없이 실패는 {@link UncheckedIOException}으로 전파하며, 텍스트 IO 기본 문자셋은 UTF-8이다.
 * 신뢰할 수 없는 경로는 {@link #resolveSecurely}로 기준 디렉터리 내부로 제한한다(경로 조작 방지).
 *
 * @author 실행환경 개발팀
 * @since 4.3.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일        수정자           수정내용
 * ----------  --------------  ---------------------------
 * 2026.08.16  실행환경 개발팀    최초 생성
 * 2026.09.01  실행환경 개발팀    판정형 경로 검증·다중 허용 루트 검사·createDirectories 추가
 * </pre>
 */
public final class EgovFiles {

    /** Windows 예약 장치명(확장자 포함, 대소문자 무시). */
    private static final Pattern WINDOWS_RESERVED_NAME =
            Pattern.compile("(?i)^(CON|PRN|AUX|NUL|COM[1-9¹²³]|LPT[1-9¹²³])(\\..*)?$");

    private static final boolean WINDOWS = "\\".equals(FileSystems.getDefault().getSeparator());


    private EgovFiles() {
    }

    // ---------------------------------------------------------------- 텍스트 IO

    /**
     * 파일 전체를 UTF-8 문자열로 읽는다.
     */
    public static String readString(Path file) {
        return readString(file, StandardCharsets.UTF_8);
    }

    /**
     * 파일 전체를 지정한 문자셋의 문자열로 읽는다.
     */
    public static String readString(Path file, Charset charset) {
        Objects.requireNonNull(file, "file must not be null");
        try {
            return new String(Files.readAllBytes(file), charset(charset));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file: " + file, e);
        }
    }

    /**
     * 파일을 라인 리스트로 읽는다(UTF-8).
     */
    public static List<String> readLines(Path file) {
        return readLines(file, StandardCharsets.UTF_8);
    }

    /**
     * 파일을 지정한 문자셋의 라인 리스트로 읽는다.
     */
    public static List<String> readLines(Path file, Charset charset) {
        Objects.requireNonNull(file, "file must not be null");
        try {
            return Files.readAllLines(file, charset(charset));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read lines: " + file, e);
        }
    }

    /**
     * 문자열을 UTF-8로 파일에 쓴다(기존 내용 대체). 부모 디렉터리가 없으면 생성한다.
     */
    public static void writeString(Path file, CharSequence content) {
        writeString(file, content, StandardCharsets.UTF_8);
    }

    /**
     * 문자열을 지정한 문자셋으로 파일에 쓴다(기존 내용 대체). 부모 디렉터리가 없으면 생성한다.
     */
    public static void writeString(Path file, CharSequence content, Charset charset) {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(content, "content must not be null");
        try {
            createParentDirectories(file);
            Files.write(file, content.toString().getBytes(charset(charset)));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write file: " + file, e);
        }
    }

    // ---------------------------------------------------------------- 복사/이동/삭제

    /**
     * 파일 또는 디렉터리(재귀)를 복사한다. 대상의 기존 파일은 대체한다.
     */
    public static void copy(Path source, Path target) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");
        try {
            if (Files.isDirectory(source)) {
                copyDirectory(source, target);
            } else {
                createParentDirectories(target);
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to copy " + source + " -> " + target, e);
        }
    }

    private static void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                createDirectories(targetDir.resolve(sourceDir.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, targetDir.resolve(sourceDir.relativize(file).toString()),
                        StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 파일 또는 디렉터리를 이동한다. 가능하면 원자적 이동을 시도하고,
     * 파일시스템이 지원하지 않으면 일반 이동(대체 허용)으로 폴백한다.
     */
    public static void move(Path source, Path target) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");
        try {
            createParentDirectories(target);
            try {
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to move " + source + " -> " + target, e);
        }
    }

    /**
     * 파일 또는 디렉터리 트리를 재귀 삭제한다. 대상이 없으면 0을 반환한다.
     *
     * @return 삭제된 파일·디렉터리 수
     */
    public static long deleteRecursively(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        // 깨진 심볼릭 링크도 삭제 대상이 되도록 NOFOLLOW_LINKS로 확인
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            return 0;
        }
        try (Stream<Path> walk = Files.walk(path)) {
            List<Path> targets = walk.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            for (Path target : targets) {
                Files.delete(target);
            }
            return targets.size();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete recursively: " + path, e);
        }
    }

    // ---------------------------------------------------------------- 조회

    /**
     * 디렉터리 직속 항목 목록을 이름순으로 반환한다.
     */
    public static List<Path> list(Path dir) {
        Objects.requireNonNull(dir, "dir must not be null");
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.sorted().collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list directory: " + dir, e);
        }
    }

    /**
     * 디렉터리 하위 전체(파일만)를 재귀로 반환한다.
     */
    public static List<Path> listRecursively(Path dir) {
        Objects.requireNonNull(dir, "dir must not be null");
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(Files::isRegularFile).sorted().collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to walk directory: " + dir, e);
        }
    }

    /**
     * 정규식이 부분 매칭(find)되는 라인을 반환한다.
     */
    public static List<String> grepLines(Path file, String regex, Charset charset) {
        Objects.requireNonNull(regex, "regex must not be null");
        Pattern pattern = Pattern.compile(regex);
        List<String> matched = new ArrayList<>();
        for (String line : readLines(file, charset)) {
            if (pattern.matcher(line).find()) {
                matched.add(line);
            }
        }
        return matched;
    }

    /**
     * UTF-8 기준 {@link #grepLines(Path, String, Charset)}.
     */
    public static List<String> grepLines(Path file, String regex) {
        return grepLines(file, regex, StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------- 기타

    /**
     * 파일이 없으면 생성하고, 있으면 최종 수정 시각을 현재로 갱신한다.
     *
     * @return 갱신된 최종 수정 시각(epoch millis)
     */
    public static long touch(Path file) {
        Objects.requireNonNull(file, "file must not be null");
        try {
            createParentDirectories(file);
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
            long now = System.currentTimeMillis();
            Files.setLastModifiedTime(file, FileTime.fromMillis(now));
            return now;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to touch file: " + file, e);
        }
    }

    /**
     * 신뢰할 수 없는 상대 경로를 기준 디렉터리 내부로 해석한다(경로 조작 방지, CWE-22).
     * 널 바이트·절대 경로·{@code ..} 탈출과 Windows 예약 장치명을 차단한다.
     * 정규화는 어휘적이므로 심볼릭 링크는 필요 시 {@link Path#toRealPath}로 재확인한다.
     *
     * @param baseDir           기준 디렉터리(신뢰 값)
     * @param untrustedRelative 사용자 입력 등 신뢰할 수 없는 상대 경로
     * @return 기준 디렉터리 내부로 확정된 절대 경로
     * @throws IllegalArgumentException 기준 디렉터리를 벗어나는 경로이거나 Windows 예약 장치명을 포함하는 경우
     */
    public static Path resolveSecurely(Path baseDir, String untrustedRelative) {
        Objects.requireNonNull(baseDir, "baseDir must not be null");
        if (untrustedRelative == null || untrustedRelative.isEmpty()) {
            throw new IllegalArgumentException("path must not be null or empty");
        }
        if (untrustedRelative.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("path must not contain null byte");
        }
        Path base = baseDir.toAbsolutePath().normalize();
        Path resolved = base.resolve(untrustedRelative).normalize();
        if (!resolved.startsWith(base)) {
            throw new IllegalArgumentException(
                    "path escapes the base directory (path traversal blocked): " + untrustedRelative);
        }
        rejectWindowsReservedNames(base, resolved, untrustedRelative);
        return resolved;
    }

    /** 기준 디렉터리 아래 구성요소에 Windows 예약 장치명이 있으면 거부한다(Windows 한정). */
    private static void rejectWindowsReservedNames(Path base, Path resolved, String original) {
        if (hasWindowsReservedName(base, resolved)) {
            throw new IllegalArgumentException(
                    "path contains a Windows reserved device name (device access blocked): " + original);
        }
    }

    private static boolean hasWindowsReservedName(Path base, Path resolved) {
        if (!WINDOWS || resolved.getNameCount() <= base.getNameCount()) {
            return false;
        }
        for (Path component : resolved.subpath(base.getNameCount(), resolved.getNameCount())) {
            if (WINDOWS_RESERVED_NAME.matcher(component.toString()).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@link #resolveSecurely(Path, String)}의 판정형. 거부 시 예외 대신 {@link Optional#empty()}를 반환한다.
     *
     * @param baseDir           기준 디렉터리(신뢰 값, {@code null}이면 예외)
     * @param untrustedRelative 사용자 입력 등 신뢰할 수 없는 상대 경로
     * @return 기준 디렉터리 내부로 확정된 절대 경로, 거부되면 빈 값
     */
    public static Optional<Path> tryResolveSecurely(Path baseDir, String untrustedRelative) {
        Objects.requireNonNull(baseDir, "baseDir must not be null");
        if (untrustedRelative == null || untrustedRelative.isEmpty()
                || untrustedRelative.indexOf('\0') >= 0) {
            return Optional.empty();
        }
        try {
            Path base = baseDir.toAbsolutePath().normalize();
            Path resolved = base.resolve(untrustedRelative).normalize();
            if (!resolved.startsWith(base) || hasWindowsReservedName(base, resolved)) {
                return Optional.empty();
            }
            return Optional.of(resolved);
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
    }

    /**
     * 경로가 허용 루트 중 하나의 하위(루트 자신 포함)인지 경로 구성요소 단위로 판정한다.
     *
     * @param candidate    검사할 경로
     * @param allowedRoots 허용 루트 목록(1개 이상)
     * @return 허용 루트 중 하나의 하위이면 {@code true}
     * @throws IllegalArgumentException 허용 루트가 비어 있는 경우(fail-closed)
     */
    public static boolean isContainedIn(Path candidate, Path... allowedRoots) {
        Objects.requireNonNull(candidate, "candidate must not be null");
        requireRoots(allowedRoots);
        Path normalized = candidate.toAbsolutePath().normalize();
        for (Path root : allowedRoots) {
            if (normalized.startsWith(root.toAbsolutePath().normalize())) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@link #isContainedIn(Path, Path...)}의 문자열 입력형. 해석 불가한 문자열은 {@code false}.
     *
     * @param candidatePath 검사할 경로 문자열(신뢰할 수 없는 값)
     * @param allowedRoots  허용 루트 목록(1개 이상)
     * @return 허용 루트 중 하나의 하위이면 {@code true}
     * @throws IllegalArgumentException 허용 루트가 비어 있는 경우(fail-closed)
     */
    public static boolean isContainedIn(String candidatePath, Path... allowedRoots) {
        requireRoots(allowedRoots);
        if (candidatePath == null || candidatePath.isEmpty()) {
            return false;
        }
        Path parsed;
        try {
            parsed = Paths.get(candidatePath);
        } catch (InvalidPathException e) {
            return false;
        }
        return isContainedIn(parsed, allowedRoots);
    }

    /**
     * {@link #isContainedIn(Path, Path...)}의 예외형. 허용 루트 밖이면 {@link IllegalArgumentException}.
     *
     * @param candidate    검사할 경로
     * @param allowedRoots 허용 루트 목록(1개 이상)
     * @return 정규화된 절대 경로
     * @throws IllegalArgumentException 허용 루트 밖이거나 허용 루트가 비어 있는 경우
     */
    public static Path requireContainedIn(Path candidate, Path... allowedRoots) {
        if (!isContainedIn(candidate, allowedRoots)) {
            throw new IllegalArgumentException(
                    "path is outside every allowed root (containment blocked): " + candidate);
        }
        return candidate.toAbsolutePath().normalize();
    }

    /**
     * {@link #requireContainedIn(Path, Path...)}의 문자열 입력형.
     *
     * @param candidatePath 검사할 경로 문자열(신뢰할 수 없는 값)
     * @param allowedRoots  허용 루트 목록(1개 이상)
     * @return 정규화된 절대 경로
     * @throws IllegalArgumentException 허용 루트 밖·해석 불가·허용 루트가 비어 있는 경우
     */
    public static Path requireContainedIn(String candidatePath, Path... allowedRoots) {
        requireRoots(allowedRoots);
        if (candidatePath == null || candidatePath.isEmpty()) {
            throw new IllegalArgumentException("path must not be null or empty");
        }
        Path parsed;
        try {
            parsed = Paths.get(candidatePath);
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("path is not a valid file system path: " + candidatePath, e);
        }
        return requireContainedIn(parsed, allowedRoots);
    }

    private static void requireRoots(Path[] allowedRoots) {
        if (allowedRoots == null || allowedRoots.length == 0) {
            throw new IllegalArgumentException("at least one allowed root is required");
        }
        for (Path root : allowedRoots) {
            Objects.requireNonNull(root, "allowed root must not be null");
        }
    }

    /**
     * 파일명에서 확장자를 반환한다(없으면 빈 문자열, 구분자 뒤 이름만 검사).
     */
    public static String getExtension(String filename) {
        String name = fileNameOnly(filename);
        int dot = name.lastIndexOf('.');
        return (dot < 0) ? "" : name.substring(dot + 1);
    }

    /**
     * 파일명에서 확장자를 제외한 이름을 반환한다.
     */
    public static String getBaseName(String filename) {
        String name = fileNameOnly(filename);
        int dot = name.lastIndexOf('.');
        return (dot < 0) ? name : name.substring(0, dot);
    }

    private static String fileNameOnly(String filename) {
        if (filename == null) {
            return "";
        }
        int sep = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        return (sep < 0) ? filename : filename.substring(sep + 1);
    }

    private static Charset charset(Charset charset) {
        return (charset != null) ? charset : StandardCharsets.UTF_8;
    }

    private static void createParentDirectories(Path file) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            createDirectories(parent);
        }
    }

    /**
     * 디렉터리를 상위 포함 생성한다. 이미 디렉터리(링크 포함)이면 그대로 둔다.
     *
     * @param dir 만들 디렉터리
     * @return 전달받은 경로
     * @throws IOException 생성 실패(디렉터리가 아닌 파일이 이미 있는 경우 포함)
     */
    public static Path createDirectories(Path dir) throws IOException {
        Objects.requireNonNull(dir, "dir must not be null");
        if (Files.isDirectory(dir)) {
            return dir;
        }
        return Files.createDirectories(dir);
    }

}
