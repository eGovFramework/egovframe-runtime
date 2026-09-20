package org.egovframe.rte.fdl.filehandling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * EgovFiles.deleteRecursively 가 깨진(대상 없는) 심볼릭 링크를 "없는 대상"으로 오판해
 * 링크 파일 자체를 지우지 않고 조용히 0 을 반환하던 문제의 회귀 테스트.
 */
public class EgovFilesDeleteRecursivelyTest {

    @TempDir
    Path tempDir;

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void 깨진_심볼릭_링크는_대상이_없어도_링크_자체가_삭제된다() throws Exception {
        Path missingTarget = tempDir.resolve("does-not-exist");
        Path link = tempDir.resolve("dangling-link");
        try {
            Files.createSymbolicLink(link, missingTarget);
        } catch (UnsupportedOperationException e) {
            assumeTrue(false, "심볼릭 링크 미지원 파일시스템: " + e);
        }

        long deleted = EgovFiles.deleteRecursively(link);

        assertEquals(1, deleted, "링크 파일 자체는 존재하므로 삭제 대상 1개로 세어야 한다");
        assertFalse(Files.exists(link, LinkOption.NOFOLLOW_LINKS), "삭제 후 링크 파일이 남아 있으면 안 된다");
    }
}
