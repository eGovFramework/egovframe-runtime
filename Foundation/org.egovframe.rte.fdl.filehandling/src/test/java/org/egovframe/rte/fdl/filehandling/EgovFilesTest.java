package org.egovframe.rte.fdl.filehandling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovFiles(NIO.2 무상태 파일 유틸) 검증.
 */
public class EgovFilesTest {

    @TempDir
    Path tempDir;

    @Test
    public void 한글_텍스트_UTF8_왕복_읽기쓰기() {
        Path file = tempDir.resolve("한글.txt");
        EgovFiles.writeString(file, "전자정부 표준프레임워크\n파일 처리 현대화");
        assertEquals("전자정부 표준프레임워크\n파일 처리 현대화", EgovFiles.readString(file));
    }

    @Test
    public void 쓰기_시_부모_디렉터리_자동_생성() {
        Path file = tempDir.resolve("a/b/c/deep.txt");
        EgovFiles.writeString(file, "deep");
        assertEquals("deep", EgovFiles.readString(file));
    }

    @Test
    public void 지정_문자셋_읽기쓰기() {
        Path file = tempDir.resolve("euckr.txt");
        EgovFiles.writeString(file, "고정길이 전문", java.nio.charset.Charset.forName("EUC-KR"));
        assertEquals("고정길이 전문", EgovFiles.readString(file, java.nio.charset.Charset.forName("EUC-KR")));
    }

    @Test
    public void 파일_복사와_디렉터리_재귀_복사() throws Exception {
        Path srcDir = tempDir.resolve("src");
        Files.createDirectories(srcDir.resolve("sub/subsub"));
        EgovFiles.writeString(srcDir.resolve("root.txt"), "r");
        EgovFiles.writeString(srcDir.resolve("sub/subsub/leaf.txt"), "l");

        Path destDir = tempDir.resolve("dest");
        EgovFiles.copy(srcDir, destDir);

        assertEquals("r", EgovFiles.readString(destDir.resolve("root.txt")));
        assertEquals("l", EgovFiles.readString(destDir.resolve("sub/subsub/leaf.txt")));
        assertTrue(Files.exists(srcDir.resolve("root.txt")), "복사는 원본을 보존한다");
    }

    @Test
    public void 이동은_원본을_제거한다() {
        Path src = tempDir.resolve("move-src.txt");
        Path dest = tempDir.resolve("moved/move-dest.txt");
        EgovFiles.writeString(src, "mv");
        EgovFiles.move(src, dest);
        assertFalse(Files.exists(src));
        assertEquals("mv", EgovFiles.readString(dest));
    }

    @Test
    public void 깊이_3단계_트리도_전부_삭제된다_기존_rm의_1단계_한계_해소() throws Exception {
        Path root = tempDir.resolve("tree");
        Files.createDirectories(root.resolve("d1/d2"));
        EgovFiles.writeString(root.resolve("f0.txt"), "0");
        EgovFiles.writeString(root.resolve("d1/f1.txt"), "1");
        EgovFiles.writeString(root.resolve("d1/d2/f2.txt"), "2");

        long deleted = EgovFiles.deleteRecursively(root);

        assertEquals(6, deleted); // root, d1, d2, f0, f1, f2
        assertFalse(Files.exists(root));
    }

    @Test
    public void 없는_경로_삭제는_0을_반환한다() {
        assertEquals(0, EgovFiles.deleteRecursively(tempDir.resolve("ghost")));
    }

    @Test
    public void 목록은_실제_항목을_이름순으로_반환한다_기존_ls_빈_리스트_해소() throws Exception {
        Files.createDirectories(tempDir.resolve("listdir/sub"));
        EgovFiles.writeString(tempDir.resolve("listdir/b.txt"), "b");
        EgovFiles.writeString(tempDir.resolve("listdir/a.txt"), "a");
        EgovFiles.writeString(tempDir.resolve("listdir/sub/c.txt"), "c");

        List<Path> shallow = EgovFiles.list(tempDir.resolve("listdir"));
        assertEquals(3, shallow.size());
        assertEquals("a.txt", shallow.get(0).getFileName().toString());

        List<Path> deep = EgovFiles.listRecursively(tempDir.resolve("listdir"));
        assertEquals(3, deep.size()); // 파일만(a, b, sub/c)
    }

    @Test
    public void grepLines는_매칭되는_라인만_반환한다_기존_grep_split_오동작_해소() {
        Path file = tempDir.resolve("grep.txt");
        EgovFiles.writeString(file, "alpha ERROR one\nbeta info\ngamma ERROR two\ndelta warn");

        List<String> matched = EgovFiles.grepLines(file, "ERROR");

        assertEquals(2, matched.size());
        assertEquals("alpha ERROR one", matched.get(0));
        assertEquals("gamma ERROR two", matched.get(1));
    }

    @Test
    public void touch는_파일을_만들고_수정시각을_갱신한다() throws Exception {
        Path file = tempDir.resolve("touch.txt");
        long stamped = EgovFiles.touch(file);
        assertTrue(Files.exists(file));
        assertTrue(stamped > 0);
    }

    @Test
    public void 경로_조작은_차단된다() {
        Path base = tempDir.resolve("base");
        assertEquals(base.toAbsolutePath().normalize().resolve("ok/file.txt"),
                EgovFiles.resolveSecurely(base, "ok/file.txt"));
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.resolveSecurely(base, "../escape.txt"));
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.resolveSecurely(base, "a/../../escape.txt"));
        assertThrows(IllegalArgumentException.class,
                () -> EgovFiles.resolveSecurely(base, tempDir.resolve("abs.txt").toAbsolutePath().toString()));
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.resolveSecurely(base, "bad\0name"));
    }

    @Test
    public void 실패는_UncheckedIOException으로_정직하게_전파된다_침묵_실패_해소() {
        assertThrows(UncheckedIOException.class, () -> EgovFiles.readString(tempDir.resolve("missing.txt")));
        assertThrows(UncheckedIOException.class,
                () -> EgovFiles.copy(tempDir.resolve("missing-src.txt"), tempDir.resolve("t.txt")));
        assertThrows(UncheckedIOException.class,
                () -> EgovFiles.move(tempDir.resolve("missing-src.txt"), tempDir.resolve("t.txt")));
    }

    @Test
    public void 확장자와_기본이름_추출() {
        assertEquals("xlsx", EgovFiles.getExtension("dir/보고서.최종.xlsx"));
        assertEquals("보고서.최종", EgovFiles.getBaseName("dir\\보고서.최종.xlsx"));
        assertEquals("", EgovFiles.getExtension("noext"));
        assertEquals("noext", EgovFiles.getBaseName("noext"));
    }

    @Test
    public void 라인_읽기_UTF8() {
        Path file = tempDir.resolve("lines.txt");
        EgovFiles.writeString(file, "하나\n둘\n셋");
        assertEquals(List.of("하나", "둘", "셋"), EgovFiles.readLines(file, StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------- 판정형·다중 허용 루트

    @Test
    public void 판정형_tryResolveSecurely는_거부를_빈값으로_돌려준다() {
        Path base = tempDir.resolve("base");

        assertEquals(base.toAbsolutePath().normalize().resolve("ok/file.txt"),
                EgovFiles.tryResolveSecurely(base, "ok/file.txt").orElseThrow());

        assertTrue(EgovFiles.tryResolveSecurely(base, "../escape.txt").isEmpty(), "탈출 시도는 빈값");
        assertTrue(EgovFiles.tryResolveSecurely(base, null).isEmpty(), "null 입력은 빈값(무예외)");
        assertTrue(EgovFiles.tryResolveSecurely(base, "").isEmpty(), "빈 문자열은 빈값(무예외)");
        assertTrue(EgovFiles.tryResolveSecurely(base, "bad\0name").isEmpty(), "널 바이트는 빈값(무예외)");
    }

    @Test
    public void 다중_허용_루트_중_하나의_하위이면_포함으로_판정한다() {
        Path storeRoot = tempDir.resolve("store");
        Path imageRoot = tempDir.resolve("image");

        assertTrue(EgovFiles.isContainedIn(imageRoot.resolve("2026/a.png"), storeRoot, imageRoot),
                "두 번째 허용 루트의 하위");
        assertTrue(EgovFiles.isContainedIn(storeRoot, storeRoot, imageRoot), "루트 자신도 포함");
        assertFalse(EgovFiles.isContainedIn(tempDir.resolve("outside/f.txt"), storeRoot, imageRoot));
        assertFalse(EgovFiles.isContainedIn(storeRoot.resolve("../escape.txt"), storeRoot, imageRoot),
                "정규화 후 루트 밖이면 제외");
    }

    @Test
    public void 유사_접두_디렉터리명은_하위로_오인되지_않는다() {
        Path dataRoot = tempDir.resolve("data");
        assertFalse(EgovFiles.isContainedIn(tempDir.resolve("data-evil/f.txt"), dataRoot),
                "경로 구성요소 단위 비교 — 문자열 접두 비교가 아니다");
    }

    @Test
    public void 허용_루트_0개는_설정_결함으로_즉시_예외다() {
        Path candidate = tempDir.resolve("f.txt");
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.isContainedIn(candidate));
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.isContainedIn(candidate.toString()));
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.requireContainedIn(candidate));
    }

    @Test
    public void 문자열형_판정은_해석_불가_입력을_false로_돌려준다() {
        Path root = tempDir.resolve("root");
        assertFalse(EgovFiles.isContainedIn((String) null, root));
        assertFalse(EgovFiles.isContainedIn("", root));
        assertFalse(EgovFiles.isContainedIn("bad\0path", root));
        assertTrue(EgovFiles.isContainedIn(root.resolve("db-stored/file.pdf").toString(), root));
    }

    @Test
    public void 봉쇄형_requireContainedIn은_루트_밖이면_예외_안이면_정규화_경로를_반환한다() {
        Path root = tempDir.resolve("root");

        assertEquals(root.toAbsolutePath().normalize().resolve("sub/f.txt"),
                EgovFiles.requireContainedIn(root.resolve("sub/f.txt"), root));
        assertEquals(root.toAbsolutePath().normalize().resolve("f.txt"),
                EgovFiles.requireContainedIn(root.resolve("sub/../f.txt"), root), "반환값은 정규화된다");

        assertThrows(IllegalArgumentException.class,
                () -> EgovFiles.requireContainedIn(tempDir.resolve("outside/f.txt"), root));
        assertThrows(IllegalArgumentException.class,
                () -> EgovFiles.requireContainedIn(root.resolve("../escape.txt"), root));
        assertThrows(IllegalArgumentException.class,
                () -> EgovFiles.requireContainedIn("bad\0path", root), "해석 불가 문자열은 cause 를 달아 거부");
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.requireContainedIn((String) null, root));
    }


    // ---------------------------------------------------------------- 플랫폼 의존 동작 고정

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void 윈도우에서는_역슬래시가_구분자라_탈출로_차단된다() {
        Path base = tempDir.resolve("base");
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.resolveSecurely(base, "..\\escape.txt"));
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.resolveSecurely(base, "C:\\evil.txt"));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void POSIX에서는_역슬래시가_일반_문자라_기준_안의_파일명이_된다() {
        Path base = tempDir.resolve("base");
        // 탈출이 아니라 그런 이름의 파일 하나로 해석된다 — 기준 디렉터리 밖으로 나가지 않는다.
        Path resolved = EgovFiles.resolveSecurely(base, "..\\escape.txt");
        assertTrue(resolved.startsWith(base.toAbsolutePath().normalize()),
                "역슬래시 입력도 기준 디렉터리 안에 머문다");
        assertEquals("..\\escape.txt", resolved.getFileName().toString());
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void 심볼릭_링크는_어휘적_정규화로_걸러지지_않는다_계약_고정() throws Exception {
        Path base = Files.createDirectories(tempDir.resolve("base"));
        Path outside = Files.createDirectories(tempDir.resolve("outside"));
        Files.createSymbolicLink(base.resolve("link"), outside);

        // 어휘적 정규화라 링크 경유 경로는 통과한다(javadoc 에 명시된 계약).
        Path viaLink = EgovFiles.resolveSecurely(base, "link/secret.txt");
        assertTrue(EgovFiles.isContainedIn(viaLink, base), "어휘적으로는 기준 안으로 판정된다");

        // 실경로로 확정하면 기준 밖임이 드러난다 — 봉쇄가 필요하면 이 단계를 덧붙여야 한다.
        Files.write(outside.resolve("secret.txt"), "x".getBytes(StandardCharsets.UTF_8));
        assertFalse(viaLink.toRealPath().startsWith(base.toRealPath()),
                "실경로 기준으로는 기준 디렉터리 밖이다");
    }

}
