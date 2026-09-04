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
 * NIO.2({@code java.nio.file}) 기반 파일 유틸 — {@link EgovFileUtil}의 현대화 표준 구현.
 *
 * <p>기존 {@link EgovFileUtil}의 구조 결함을 해소한다:</p>
 * <ul>
 *   <li><b>전역 상태 없음</b>: {@code cd()}식 프로세스 전역 기준 디렉터리를 두지 않는다 —
 *       모든 경로는 호출 인자로 완결(스레드 안전)</li>
 *   <li><b>침묵 실패 없음</b>: 실패는 {@link UncheckedIOException}(JDK 표준 unchecked + cause 체인)으로
 *       정직하게 전파한다 — debug 로그 후 정상 복귀하던 rm/cp/mv 유형 제거</li>
 *   <li><b>문자셋 명시</b>: 텍스트 IO 기본값은 UTF-8(오버로드로 지정 가능) — 플랫폼 기본
 *       문자셋(MS949/UTF-8) 의존 제거</li>
 *   <li><b>올바른 시맨틱</b>: {@link #grepLines}는 패턴이 <b>매칭되는 라인</b>을 반환(기존
 *       {@code grep()}의 split 오동작 대체), {@link #deleteRecursively}는 깊이 무제한 재귀 삭제
 *       (기존 {@code rm()}의 1단계 한계 대체), {@link #list}는 실제 목록을 반환(기존 {@code ls()}
 *       빈 리스트 대체)</li>
 *   <li><b>경로 조작 방어</b>: {@link #resolveSecurely}로 신뢰할 수 없는 상대 경로를 기준 디렉터리
 *       내부로 강제(traversal·절대 경로·널 바이트 차단). 예외를 던질 수 없는 자리(일괄 검증·필터)에는
 *       판정형 {@link #tryResolveSecurely}·{@link #isContainedIn(Path, Path...)}를, 업로드 루트가
 *       여러 개인 배치에는 다중 허용 루트 봉쇄 {@link #requireContainedIn(Path, Path...)}를 쓴다</li>
 * </ul>
 *
 * <p>원격 파일시스템(SFTP 등)이 필요한 경우에만 Commons VFS 기반 {@link EgovFileUtil}을 계속
 * 사용하고, 로컬 파일 처리는 본 클래스를 표준으로 한다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일        수정자           수정내용
 * ----------  --------------  ---------------------------
 * 2026.08.16  실행환경 개발팀    최초 생성
 * 2026.09.01  실행환경 개발팀    판정형 경로 검증(tryResolveSecurely·isContainedIn)과
 *                             다중 허용 루트 봉쇄(requireContainedIn) 추가 — 호출부에
 *                             try-catch 를 강요해 {@code catch {}} 무시를 부르던 예외
 *                             단일형의 한계 해소
 * </pre>
 */
public final class EgovFiles {

    /** Windows 예약 장치명 — 확장자가 붙어도({@code NUL.txt}) 장치로 해석되며 대소문자를 가리지 않는다. */
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
                Files.createDirectories(targetDir.resolve(sourceDir.relativize(dir).toString()));
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
     * 파일 또는 디렉터리 트리를 <b>깊이 무제한</b>으로 삭제한다(기존 {@code EgovFileUtil.rm()}의
     * 1단계 깊이 한계 대체). 대상이 없으면 0을 반환한다.
     *
     * @return 삭제된 파일·디렉터리 수
     */
    public static long deleteRecursively(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        if (!Files.exists(path)) {
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
     * 디렉터리 직속 항목 목록을 이름순으로 반환한다(기존 {@code ls()}의 빈 리스트 반환 대체).
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
     * 정규식이 <b>매칭되는 라인</b>을 반환한다 — grep 본연의 시맨틱
     * (기존 {@code EgovFileUtil.grep()}은 패턴 기준 split 조각을 반환하는 오동작).
     * 부분 매칭(find) 기준이며, 라인 전체 매칭이 필요하면 앵커({@code ^...$})를 사용한다.
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
     * 신뢰할 수 없는 상대 경로를 기준 디렉터리 내부로 안전하게 해석한다(경로 조작 방어 — CWE-22).
     * 널 바이트·절대 경로·{@code ..} 탈출(정규화 후 기준 이탈)을 차단한다.
     *
     * <p>정규화는 어휘적(lexical)이다 — 실재하는 <b>심볼릭 링크</b>가 기준 디렉터리 밖을
     * 가리키는 경우는 걸러지지 않는다. 기준 디렉터리 안에 신뢰할 수 없는 주체가 링크를 만들 수
     * 있는 배치라면 호출 후 {@link Path#toRealPath}로 실경로를 확정해 한 번 더 확인한다.</p>
     *
     * <p>경로 구분자 해석은 <b>플랫폼을 따른다</b>. {@code ..\\escape}는 Windows 에서는 탈출로
     * 차단되지만, 역슬래시가 일반 문자인 POSIX 계열에서는 그런 이름의 <b>파일 하나</b>로
     * 해석되어 기준 디렉터리 안에 그대로 남는다(탈출은 아니다). 플랫폼 간 이식이 필요한
     * 입력은 호출 전에 구분자를 정규화한다.</p>
     *
     * <p>Windows 에서는 예약 장치명(CON·PRN·AUX·NUL·COM1~9·LPT1~9, 확장자 유무 무관)을 구성요소로 가진
     * 경로를 거부한다 — 파일이 아니라 장치로 열려 내용이 사라지거나 블로킹될 수 있다.</p>
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

    /** 기준 디렉터리 아래의 신뢰할 수 없는 구성요소에 Windows 예약 장치명이 있으면 거부한다(Windows 에서만). */
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
     * {@link #resolveSecurely(Path, String) resolveSecurely}의 <b>판정형</b> — 거부 사유가 무엇이든
     * 예외 대신 {@link Optional#empty()}를 반환한다.
     *
     * <p>일괄 검증·필터처럼 항목마다 예외를 던질 수 없는 자리를 위한 형태다. 예외형만 있으면
     * 호출부마다 try-catch 가 강제되고, 그 반복은 결국 {@code catch {}} 무시로 이어져 방어가
     * 무력해진다. 거부 원인 구분이 필요하면 예외형 {@link #resolveSecurely}를 쓴다.</p>
     *
     * <p>예외형과 달리 운영체제가 허용하지 않는 문자가 섞인 입력({@link InvalidPathException})도
     * 빈 값으로 판정한다.</p>
     *
     * <p>심볼릭 링크·경로 구분자에 대한 주의는 {@link #resolveSecurely}와 같다.</p>
     *
     * @param baseDir           기준 디렉터리(신뢰 값 — {@code null}이면 호출 코드 결함이므로 예외)
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
     * 경로가 <b>허용 루트 N개 중 하나의 하위</b>(루트 자신 포함)인지 판정한다.
     *
     * <p>업로드 저장소·이미지·문서 변환 디렉터리처럼 허용 루트가 여러 개인 배치에서, DB 등에
     * 보관된 전체 경로가 여전히 허용 범위 안인지 확인하는 용도다. 비교는 정규화 후
     * <b>경로 구성요소 단위</b>로 하므로 {@code /data-evil}이 {@code /data}의 하위로 오인되지
     * 않는다. 문자열 접두 비교가 아니다.</p>
     *
     * <p>정규화는 어휘적(lexical)이다 — 실재하는 심볼릭 링크가 루트 밖을 가리키는 경우까지
     * 봉쇄해야 하면 호출 전에 {@link Path#toRealPath}로 실경로를 확정한다.</p>
     *
     * @param candidate    검사할 경로
     * @param allowedRoots 허용 루트 목록(1개 이상 — 0개는 설정 결함이므로 예외)
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
     * {@link #isContainedIn(Path, Path...)}의 문자열 입력형 — DB에 보관된 경로 문자열을 그대로
     * 검사한다. {@code null}·빈 문자열·경로로 해석 불가한 문자열은 {@code false}로 판정한다.
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
     * {@link #isContainedIn(Path, Path...)}의 <b>봉쇄형</b> — 허용 루트 밖이면
     * {@link IllegalArgumentException}을 던지고, 안이면 정규화된 절대 경로를 반환한다.
     *
     * <p>심볼릭 링크에 대한 주의는 {@link #isContainedIn(Path, Path...)}과 같다.</p>
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
     * {@link #requireContainedIn(Path, Path...)}의 문자열 입력형. 경로로 해석 불가한 문자열은
     * 원인({@link InvalidPathException})을 연결한 {@link IllegalArgumentException}으로 거부한다.
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
            Files.createDirectories(parent);
        }
    }

}
