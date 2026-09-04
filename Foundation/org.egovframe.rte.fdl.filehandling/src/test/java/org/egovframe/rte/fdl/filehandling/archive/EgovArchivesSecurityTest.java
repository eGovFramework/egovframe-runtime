package org.egovframe.rte.fdl.filehandling.archive;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * EgovArchives 해제의 <b>공격 아카이브</b> 회귀 검증 — Zip Slip(CWE-22)·링크(CWE-59)·
 * 압축 폭탄(CWE-400) 에 실제로 쓰이는 엔트리 변형을 모아 두고, 판정은 하나로 한다:
 * <b>대상 디렉터리 밖에 파일이 생기는가.</b>
 *
 * <p>기능 테스트({@link EgovArchivesTest})가 대표 사례를 다루고, 이 클래스는 변형 배터리를 맡는다.</p>
 */
public class EgovArchivesSecurityTest {

    private static final String BS = String.valueOf((char) 92);

    @TempDir
    Path tempDir;

    private Path outside;
    private Path target;

    private void arrange() throws IOException {
        outside = Files.createDirectories(tempDir.resolve("outside"));
        target = Files.createDirectories(tempDir.resolve("target"));
    }

    // ------------------------------------------------------------ Zip Slip 변형

    @Test
    public void 절대_경로_엔트리는_차단되고_밖에_아무것도_생기지_않는다() throws IOException {
        arrange();
        for (String name : List.of("/abs/pwn.txt", "//srv/share/pwn.txt")) {
            Path zip = zip(tempDir.resolve("abs.zip"), name);
            assertThrows(IllegalArgumentException.class, () -> EgovArchives.unzip(zip, target), name);
            assertEquals(0, filesUnder(outside), "밖에 생기면 안 된다: " + name);
            assertEquals(0, filesUnder(target), "대상에도 생기면 안 된다: " + name);
        }
    }

    @Test
    public void 드라이브_문자_엔트리는_어느_OS에서도_밖에_생기지_않는다() throws IOException {
        // Windows: 드라이브 절대 경로라 차단(IAE). POSIX: 정규화 후 "C:/pwn.txt" 는 "C:" 라는 이름의
        // 하위 디렉터리일 뿐이라 대상 안에 풀린다. 보안 속성은 양쪽 모두 "경계 밖에 없음" 이다.
        arrange();
        Path zip = zip(tempDir.resolve("drive.zip"), "C:" + BS + "pwn.txt");
        try {
            EgovArchives.unzip(zip, target);
            assertFalse(System.getProperty("os.name").toLowerCase().contains("win"),
                    "Windows 에서는 드라이브 절대 경로가 차단돼야 한다");
            assertTrue(Files.exists(target.resolve("C:").resolve("pwn.txt")), "POSIX 에서는 대상 안의 중첩 이름");
        } catch (IllegalArgumentException blocked) {
            assertTrue(System.getProperty("os.name").toLowerCase().contains("win"), "차단은 Windows 의 동작");
        }
        assertEquals(0, filesUnder(outside));
    }

    @Test
    public void 역슬래시_상위_이동_엔트리는_POSIX에서도_차단된다() throws IOException {
        // EgovArchives 는 엔트리 이름의 역슬래시를 / 로 정규화한 뒤 검사한다 — POSIX 에서
        // "..\..\x" 가 이름 하나로 남아 통과하는 일이 없다(EgovFiles 단독 계약보다 엄격)
        arrange();
        Path zip = zip(tempDir.resolve("bs.zip"), ".." + BS + ".." + BS + "pwn.txt");
        assertThrows(IllegalArgumentException.class, () -> EgovArchives.unzip(zip, target));
        assertEquals(0, filesUnder(outside) + filesUnder(target));
    }

    @Test
    public void 중첩_상위_이동은_정규화_후_차단된다() throws IOException {
        arrange();
        for (String name : List.of("sub/../../pwn.txt", "a/b/../../../pwn.txt", "..")) {
            Path zip = zip(tempDir.resolve("nest.zip"), name);
            assertThrows(IllegalArgumentException.class, () -> EgovArchives.unzip(zip, target), name);
            assertEquals(0, filesUnder(outside), name);
        }
    }

    @Test
    public void 기준_자신으로_정규화되는_파일_엔트리는_실패로_닫힌다() throws IOException {
        // "sub/.." 는 정규화하면 대상 디렉터리 자신이라 탈출은 아니지만, 파일 엔트리로 디렉터리에
        // 쓰려는 셈이라 입출력 실패로 닫힌다 — 어느 쪽이든 밖에는 아무것도 생기지 않는다
        arrange();
        Path zip = zip(tempDir.resolve("self.zip"), "sub/..");
        assertThrows(RuntimeException.class, () -> EgovArchives.unzip(zip, target));
        assertEquals(0, filesUnder(outside) + filesUnder(target));
    }

    // ------------------------------------------------------------ tar 링크·장치

    @Test
    public void tar_하드_링크_엔트리는_거부한다() throws IOException {
        arrange();
        Path tar = tar(tempDir.resolve("hard.tar"), "evil-hard", "../../outside/secret", TarArchiveEntry.LF_LINK);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> EgovArchives.untar(tar, target));
        assertTrue(e.getMessage().contains("link"), e.getMessage());
        assertEquals(0, filesUnder(outside) + filesUnder(target));
    }

    @Test
    public void tar_FIFO_장치_엔트리는_거부한다() throws IOException {
        arrange();
        Path tar = tar(tempDir.resolve("fifo.tar"), "evil-fifo", null, TarArchiveEntry.LF_FIFO);
        assertThrows(IllegalArgumentException.class, () -> EgovArchives.untar(tar, target));
        assertEquals(0, filesUnder(target));
    }

    // ------------------------------------------------------------ 충돌·한도

    @Test
    public void 파일과_같은_이름의_디렉터리_엔트리는_실패로_닫힌다() throws IOException {
        arrange();
        Path zip = zip(tempDir.resolve("clash.zip"), "a.txt", "a.txt/b.txt");
        assertThrows(UncheckedIOException.class, () -> EgovArchives.unzip(zip, target));
        assertEquals(0, filesUnder(outside));
    }

    @Test
    public void 한도_초과로_중단돼도_경계_밖은_그대로다() throws IOException {
        arrange();
        String[] many = new String[150];
        for (int i = 0; i < many.length; i++) {
            many[i] = "f" + i + ".txt";
        }
        Path zip = zip(tempDir.resolve("many.zip"), many);
        assertThrows(IllegalArgumentException.class,
                () -> EgovArchives.unzip(zip, target, StandardCharsets.UTF_8, EgovExtractLimits.of(100, 1_000_000)));
        assertEquals(0, filesUnder(outside));
        assertTrue(filesUnder(target) <= 100, "한도까지만 풀린다");
    }

    /** 한도 초과로 거부한 엔트리의 부분 파일은 남지 않는다. */
    @Test
    public void 바이트_한도_초과_시_부분_파일이_남지_않는다() throws IOException {
        arrange();
        Path zip = zipBig(tempDir.resolve("bomb.zip"), "big.bin", 5 * 1024 * 1024);
        assertThrows(IllegalArgumentException.class,
                () -> EgovArchives.unzip(zip, target, StandardCharsets.UTF_8, EgovExtractLimits.of(10, 1024 * 1024)));
        assertFalse(Files.exists(target.resolve("big.bin")), "거부된 엔트리의 부분 파일은 남지 않아야 한다");
    }

    // ------------------------------------------------------------ 미리 심긴 링크

    /**
     * 대상 디렉터리에 밖을 가리키는 링크가 <b>미리</b> 있어도 링크를 따라 밖에 쓰지 않는다 — 어휘적 봉쇄를
     * 통과한 뒤 쓰기 직전에 실경로를 재확인해 거부한다. 이 API 군으로는 링크를 심을 수 없지만(링크 엔트리 거부),
     * 다른 경로로 심긴 링크와 결합되는 방어 심도를 고정한다.
     */
    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void 대상_디렉터리의_기존_심볼릭_링크_아래로는_해제되지_않는다() throws IOException {
        arrange();
        plantLink(target.resolve("dlink"), outside);
        Path zip = zip(tempDir.resolve("via-link.zip"), "dlink/pwn.txt");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> EgovArchives.unzip(zip, target));
        assertTrue(e.getMessage().contains("link"), e.getMessage());
        assertFalse(Files.exists(outside.resolve("pwn.txt")), "링크를 따라 경계 밖에 쓰면 안 된다");
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void 대상_디렉터리의_기존_파일_링크를_엔트리가_덮어쓰지_않는다() throws IOException {
        // 엔트리 이름과 같은 자리에 밖의 파일을 가리키는 링크가 있으면, 열어서 쓰는 순간 밖의 파일이 덮어써진다
        arrange();
        Path secret = Files.writeString(outside.resolve("secret.txt"), "TOP-SECRET");
        plantLink(target.resolve("flink.txt"), secret);
        Path zip = zip(tempDir.resolve("via-file-link.zip"), "flink.txt");
        assertThrows(IllegalArgumentException.class, () -> EgovArchives.unzip(zip, target));
        assertEquals("TOP-SECRET", Files.readString(secret), "링크 대상(경계 밖)은 그대로다");
    }

    // ------------------------------------------------------------ helpers

    private static Path zip(Path file, String... names) throws IOException {
        Files.deleteIfExists(file);
        try (ZipOutputStream z = new ZipOutputStream(Files.newOutputStream(file), StandardCharsets.UTF_8)) {
            for (String n : names) {
                z.putNextEntry(new ZipEntry(n));
                if (!n.endsWith("/")) {
                    z.write("payload".getBytes(StandardCharsets.UTF_8));
                }
                z.closeEntry();
            }
        }
        return file;
    }

    private static Path zipBig(Path file, String name, int bytes) throws IOException {
        try (ZipOutputStream z = new ZipOutputStream(Files.newOutputStream(file), StandardCharsets.UTF_8)) {
            z.putNextEntry(new ZipEntry(name));
            z.write(new byte[bytes]);
            z.closeEntry();
        }
        return file;
    }

    private static Path tar(Path file, String name, String linkName, byte type) throws IOException {
        try (TarArchiveOutputStream t = new TarArchiveOutputStream(Files.newOutputStream(file), "UTF-8")) {
            t.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            TarArchiveEntry e = new TarArchiveEntry(name, type);
            if (linkName != null) {
                e.setLinkName(linkName);
            }
            e.setSize(0);
            t.putArchiveEntry(e);
            t.closeArchiveEntry();
        }
        return file;
    }

    private static void plantLink(Path link, Path to) {
        try {
            Files.createSymbolicLink(link, to);
        } catch (UnsupportedOperationException | IOException e) {
            assumeTrue(false, "이 파일시스템에서는 심볼릭 링크를 만들 수 없다: " + e);
        }
    }

    private static long filesUnder(Path dir) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) {
            return s.filter(Files::isRegularFile).count();
        }
    }
}
