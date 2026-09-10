package org.egovframe.rte.fdl.filehandling.archive;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.egovframe.rte.fdl.filehandling.EgovFiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovArchives(디렉터리 아카이브 생성·해제) 검증 — Zip Slip 봉쇄·한도·형식 왕복.
 */
public class EgovArchivesTest {

    @TempDir
    Path tempDir;

    private Path sampleTree() throws IOException {
        Path src = tempDir.resolve("src");
        Files.createDirectories(src.resolve("sub/깊은"));
        Files.createDirectories(src.resolve("빈디렉터리"));
        EgovFiles.writeString(src.resolve("루트.txt"), "가");
        EgovFiles.writeString(src.resolve("sub/깊은/파일.txt"), "나");
        return src;
    }

    private void craftZip(Path zip, int files, int bytesPerFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip), StandardCharsets.UTF_8)) {
            byte[] content = new byte[bytesPerFile];
            for (int i = 0; i < files; i++) {
                zos.putNextEntry(new ZipEntry("f" + i + ".bin"));
                zos.write(content);
                zos.closeEntry();
            }
        }
    }

    // ---------------------------------------------------------------- zip

    @Test
    public void zip_왕복은_한글_파일명과_빈_하위_디렉터리를_보존한다() throws Exception {
        Path src = sampleTree();
        Path zip = tempDir.resolve("out/아카이브.zip");

        long written = EgovArchives.zipDirectory(src, zip);
        assertEquals(5, written); // 루트.txt, sub, sub/깊은, sub/깊은/파일.txt, 빈디렉터리

        Path restored = tempDir.resolve("restored");
        long read = EgovArchives.unzip(zip, restored);

        assertEquals(5, read);
        assertEquals("가", EgovFiles.readString(restored.resolve("루트.txt")));
        assertEquals("나", EgovFiles.readString(restored.resolve("sub/깊은/파일.txt")));
        assertTrue(Files.isDirectory(restored.resolve("빈디렉터리")),
                "빈 하위 디렉터리 보존 — 원본 BackupJob 은 파일만 수집해 소실");
    }

    @Test
    public void 엔트리_이름은_상대_경로다_원본_BackupJob의_절대경로_기록_결함_회귀() throws Exception {
        Path src = sampleTree();
        Path zip = tempDir.resolve("relative.zip");
        EgovArchives.zipDirectory(src, zip);

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            int seen = 0;
            while ((entry = zis.getNextEntry()) != null) {
                seen++;
                assertFalse(entry.getName().startsWith("/"), "절대 경로 금지: " + entry.getName());
                assertFalse(entry.getName().contains(":"), "드라이브 문자 금지: " + entry.getName());
                assertFalse(entry.getName().contains("\\"), "구분자는 / 통일: " + entry.getName());
            }
            assertEquals(5, seen);
        }
    }

    @Test
    public void 빈_원본_디렉터리는_침묵_실패_대신_예외다() throws Exception {
        Path empty = tempDir.resolve("empty");
        Files.createDirectories(empty);
        assertThrows(IllegalArgumentException.class,
                () -> EgovArchives.zipDirectory(empty, tempDir.resolve("e.zip")),
                "원본 BackupJob 은 빈 디렉터리에서 예외 없이 false 를 반환했다");
    }

    @Test
    public void 아카이브_파일이_원본_디렉터리_안이면_거부한다() throws Exception {
        Path src = sampleTree();
        assertThrows(IllegalArgumentException.class,
                () -> EgovArchives.zipDirectory(src, src.resolve("self.zip")));
    }

    @Test
    public void Zip_Slip_탈출_엔트리는_차단되고_파일이_만들어지지_않는다() throws Exception {
        Path evil = tempDir.resolve("evil.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(evil), StandardCharsets.UTF_8)) {
            zos.putNextEntry(new ZipEntry("ok.txt"));
            zos.write("x".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("../escaped.txt"));
            zos.write("y".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        Path target = tempDir.resolve("slip/target");
        assertThrows(IllegalArgumentException.class, () -> EgovArchives.unzip(evil, target));
        assertFalse(Files.exists(tempDir.resolve("slip/escaped.txt")),
                "탈출 파일이 대상 디렉터리 밖에 만들어지면 안 된다");
    }

    @Test
    public void 구형_윈도우_zip은_MS949_문자셋_지정으로_한글_파일명이_보존된다() throws Exception {
        Charset ms949 = Charset.forName("MS949");
        Path legacy = tempDir.resolve("legacy.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(legacy), ms949)) {
            zos.putNextEntry(new ZipEntry("한글문서.txt"));
            zos.write("내용".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        Path out = tempDir.resolve("legacy-out");
        assertEquals(1, EgovArchives.unzip(legacy, out, ms949));
        assertEquals("내용", EgovFiles.readString(out.resolve("한글문서.txt")));
    }

    @Test
    public void 해제는_기존_파일을_대체한다() throws Exception {
        Path src = sampleTree();
        Path zip = tempDir.resolve("replace.zip");
        EgovArchives.zipDirectory(src, zip);

        Path out = tempDir.resolve("replace-out");
        EgovFiles.writeString(out.resolve("루트.txt"), "이전 내용");
        EgovArchives.unzip(zip, out);
        assertEquals("가", EgovFiles.readString(out.resolve("루트.txt")));
    }

    // ---------------------------------------------------------------- tar

    @Test
    public void tar_왕복은_100바이트_초과_긴_이름을_POSIX_확장으로_보존한다() throws Exception {
        // tar 기본 헤더의 name 필드는 100바이트다. 그것을 넘기되 파일시스템 한계 안에 들어야 한다 —
        // ext4 등은 파일명 상한이 255"바이트"라 한글(UTF-8 3바이트)로는 85자를 넘길 수 없다.
        // 36자 = 108바이트: tar 의 100바이트는 넘고 255바이트에는 못 미친다.
        String longName = "긴이름".repeat(12) + ".txt";
        Path src = tempDir.resolve("tarsrc");
        Files.createDirectories(src.resolve("sub"));
        EgovFiles.writeString(src.resolve(longName), "긴 이름 내용");
        EgovFiles.writeString(src.resolve("sub/일반.txt"), "일반");

        Path tar = tempDir.resolve("아카이브.tar");
        long written = EgovArchives.tarDirectory(src, tar);
        assertEquals(3, written); // longName, sub, sub/일반.txt

        Path out = tempDir.resolve("tar-out");
        long read = EgovArchives.untar(tar, out);
        assertEquals(3, read);
        assertEquals("긴 이름 내용", EgovFiles.readString(out.resolve(longName)));
        assertEquals("일반", EgovFiles.readString(out.resolve("sub/일반.txt")));
    }

    @Test
    public void tar_심볼릭_링크_엔트리는_거부한다() throws Exception {
        Path evil = tempDir.resolve("link.tar");
        try (TarArchiveOutputStream tos =
                new TarArchiveOutputStream(Files.newOutputStream(evil), StandardCharsets.UTF_8.name())) {
            TarArchiveEntry link = new TarArchiveEntry("evil-link", TarConstants.LF_SYMLINK);
            link.setLinkName("../../outside");
            tos.putArchiveEntry(link);
            tos.closeArchiveEntry();
        }

        assertThrows(IllegalArgumentException.class,
                () -> EgovArchives.untar(evil, tempDir.resolve("link-out")),
                "링크를 따라 대상 밖에 쓰는 우회 차단");
    }

    @Test
    public void tar_탈출_엔트리도_차단된다() throws Exception {
        Path evil = tempDir.resolve("escape.tar");
        try (TarArchiveOutputStream tos =
                new TarArchiveOutputStream(Files.newOutputStream(evil), StandardCharsets.UTF_8.name())) {
            TarArchiveEntry entry = new TarArchiveEntry("../tar-escaped.txt");
            byte[] content = "y".getBytes(StandardCharsets.UTF_8);
            entry.setSize(content.length);
            tos.putArchiveEntry(entry);
            tos.write(content);
            tos.closeArchiveEntry();
        }

        Path target = tempDir.resolve("tarslip/target");
        assertThrows(IllegalArgumentException.class, () -> EgovArchives.untar(evil, target));
        assertFalse(Files.exists(tempDir.resolve("tarslip/tar-escaped.txt")));
    }

    // ---------------------------------------------------------------- 한도

    @Test
    public void 엔트리_수_한도를_넘으면_즉시_중단한다() throws Exception {
        Path zip = tempDir.resolve("count.zip");
        craftZip(zip, 3, 10);

        assertThrows(IllegalArgumentException.class,
                () -> EgovArchives.unzip(zip, tempDir.resolve("count-out"),
                        StandardCharsets.UTF_8, EgovExtractLimits.of(2, 1024)));
    }

    @Test
    public void 누적_해제_크기_한도는_헤더_선언이_아니라_실제_기록_바이트에_적용된다() throws Exception {
        Path zip = tempDir.resolve("bytes.zip");
        craftZip(zip, 3, 10); // 총 30바이트

        assertThrows(IllegalArgumentException.class,
                () -> EgovArchives.unzip(zip, tempDir.resolve("bytes-out"),
                        StandardCharsets.UTF_8, EgovExtractLimits.of(100, 15)));

        // 한도를 명시적으로 풀면 통과한다
        assertEquals(3, EgovArchives.unzip(zip, tempDir.resolve("bytes-ok"),
                StandardCharsets.UTF_8, EgovExtractLimits.unlimited()));
    }

    @Test
    public void 한도_값은_양수만_허용한다() {
        assertThrows(IllegalArgumentException.class, () -> EgovExtractLimits.of(0, 10));
        assertThrows(IllegalArgumentException.class, () -> EgovExtractLimits.of(10, -1));
        assertEquals(EgovExtractLimits.DEFAULT_MAX_ENTRIES, EgovExtractLimits.defaults().getMaxEntries());
    }

}
