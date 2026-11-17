package org.egovframe.rte.fdl.filehandling.archive;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * EgovArchives 해제 공격 아카이브 회귀 테스트(Zip Slip CWE-22, 링크 CWE-59, 압축 폭탄 CWE-400).
 * 대상 디렉터리 밖에 파일이 생기지 않음을 검증한다.
 */
public class EgovArchivesSecurityTest {

    private static final String BS = String.valueOf((char) 92);

    @Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	Path tempDir;

	@Before
	public void initTempDir() {
		tempDir = temporaryFolder.getRoot().toPath();
	}

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
        for (String name : Arrays.asList("/abs/pwn.txt", "//srv/share/pwn.txt")) {
            Path zip = zip(tempDir.resolve("abs.zip"), name);
            assertThrows(name, IllegalArgumentException.class, () -> EgovArchives.unzip(zip, target));
            assertEquals("밖에 생기면 안 된다: " + name, 0, filesUnder(outside));
            assertEquals("대상에도 생기면 안 된다: " + name, 0, filesUnder(target));
        }
    }

    @Test
    public void 드라이브_문자_엔트리는_어느_OS에서도_밖에_생기지_않는다() throws IOException {
        // Windows는 드라이브 절대 경로로 차단, POSIX는 "C:" 하위 디렉터리로 대상 안에 해제
        arrange();
        Path zip = zip(tempDir.resolve("drive.zip"), "C:" + BS + "pwn.txt");
        try {
            EgovArchives.unzip(zip, target);
            assertFalse("Windows 에서는 드라이브 절대 경로가 차단돼야 한다", System.getProperty("os.name").toLowerCase().contains("win"));
            assertTrue("POSIX 에서는 대상 안의 중첩 이름", Files.exists(target.resolve("C:").resolve("pwn.txt")));
        } catch (IllegalArgumentException blocked) {
            assertTrue("차단은 Windows 의 동작", System.getProperty("os.name").toLowerCase().contains("win"));
        }
        assertEquals(0, filesUnder(outside));
    }

    @Test
    public void 역슬래시_상위_이동_엔트리는_POSIX에서도_차단된다() throws IOException {
        // 엔트리 이름의 역슬래시를 / 로 정규화한 뒤 검사(POSIX에서도 차단)
        arrange();
        Path zip = zip(tempDir.resolve("bs.zip"), ".." + BS + ".." + BS + "pwn.txt");
        assertThrows(IllegalArgumentException.class, () -> EgovArchives.unzip(zip, target));
        assertEquals(0, filesUnder(outside) + filesUnder(target));
    }

    @Test
    public void 중첩_상위_이동은_정규화_후_차단된다() throws IOException {
        arrange();
        for (String name : Arrays.asList("sub/../../pwn.txt", "a/b/../../../pwn.txt", "..")) {
            Path zip = zip(tempDir.resolve("nest.zip"), name);
            assertThrows(name, IllegalArgumentException.class, () -> EgovArchives.unzip(zip, target));
            assertEquals(name, 0, filesUnder(outside));
        }
    }

    @Test
    public void 기준_자신으로_정규화되는_파일_엔트리는_실패로_닫힌다() throws IOException {
        // "sub/.."는 대상 디렉터리 자신이라 탈출은 아니며 입출력 실패로 끝난다
        arrange();
        Path zip = zip(tempDir.resolve("self.zip"), "sub/..");
        assertThrows(RuntimeException.class, () -> EgovArchives.unzip(zip, target));
        assertEquals(0, filesUnder(outside) + filesUnder(target));
    }

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
        assertTrue("한도까지만 풀린다", filesUnder(target) <= 100);
    }

    /** 한도 초과로 거부한 엔트리의 부분 파일은 남지 않는다. */
    @Test
    public void 바이트_한도_초과_시_부분_파일이_남지_않는다() throws IOException {
        arrange();
        Path zip = zipBig(tempDir.resolve("bomb.zip"), "big.bin", 5 * 1024 * 1024);
        assertThrows(IllegalArgumentException.class,
                () -> EgovArchives.unzip(zip, target, StandardCharsets.UTF_8, EgovExtractLimits.of(10, 1024 * 1024)));
        assertFalse("거부된 엔트리의 부분 파일은 남지 않아야 한다", Files.exists(target.resolve("big.bin")));
    }

    // ------------------------------------------------------------ 미리 심긴 링크

    /** 대상 디렉터리에 밖을 가리키는 링크가 미리 있어도 링크를 따라 밖에 쓰지 않는다. */
    @Test
        public void 대상_디렉터리의_기존_심볼릭_링크_아래로는_해제되지_않는다() throws IOException {
		org.junit.Assume.assumeFalse(System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("win"));
        arrange();
        plantLink(target.resolve("dlink"), outside);
        Path zip = zip(tempDir.resolve("via-link.zip"), "dlink/pwn.txt");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> EgovArchives.unzip(zip, target));
        assertTrue(e.getMessage(), e.getMessage().contains("link"));
        assertFalse("링크를 따라 경계 밖에 쓰면 안 된다", Files.exists(outside.resolve("pwn.txt")));
    }

    @Test
        public void 대상_디렉터리의_기존_파일_링크를_엔트리가_덮어쓰지_않는다() throws IOException {
		org.junit.Assume.assumeFalse(System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("win"));
        // 엔트리 자리에 밖의 파일을 가리키는 링크를 미리 심는다
        arrange();
        Path secret = Files.write(outside.resolve("secret.txt"), "TOP-SECRET".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        plantLink(target.resolve("flink.txt"), secret);
        Path zip = zip(tempDir.resolve("via-file-link.zip"), "flink.txt");
        assertThrows(IllegalArgumentException.class, () -> EgovArchives.unzip(zip, target));
        assertEquals("링크 대상(경계 밖)은 그대로다", "TOP-SECRET", new String(Files.readAllBytes(secret), java.nio.charset.StandardCharsets.UTF_8));
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

    private static void plantLink(Path link, Path to) {
        try {
            Files.createSymbolicLink(link, to);
        } catch (UnsupportedOperationException | IOException e) {
            assumeTrue("이 파일시스템에서는 심볼릭 링크를 만들 수 없다: " + e, false);
        }
    }

    private static long filesUnder(Path dir) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) {
            return s.filter(Files::isRegularFile).count();
        }
    }
}
