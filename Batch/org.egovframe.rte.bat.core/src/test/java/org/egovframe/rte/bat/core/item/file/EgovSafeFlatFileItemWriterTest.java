package org.egovframe.rte.bat.core.item.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.WriteFailedException;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovSafeFlatFileItemWriter 의 임시 파일 쓰기와 완료 시 원자적 교체, 실패 시 정리, 최대 크기 가드를 검증한다.
 */
public class EgovSafeFlatFileItemWriterTest {

    @TempDir
    Path tempDir;

    private EgovSafeFlatFileItemWriter<String> writer(File destination, long maxBytes) throws Exception {
        EgovSafeFlatFileItemWriter<String> writer = new EgovSafeFlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(destination));
        writer.setLineAggregator(new PassThroughLineAggregator<>());
        writer.setLineSeparator("\n");
        if (maxBytes > 0) {
            writer.setMaxFileSizeBytes(maxBytes);
        }
        writer.afterPropertiesSet();
        return writer;
    }

    private StepExecution stepExecution(BatchStatus status) {
        StepExecution stepExecution = new StepExecution("safeWriteStep", new JobExecution(1L));
        stepExecution.setStatus(status);
        return stepExecution;
    }

    @Test
    public void testCompletedStepMovesTemporaryFileToFinalName() throws Exception {
        File destination = tempDir.resolve("out.dat").toFile();
        File tmp = tempDir.resolve("out.dat" + EgovSafeFlatFileItemWriter.TMP_SUFFIX).toFile();
        EgovSafeFlatFileItemWriter<String> writer = writer(destination, 0);

        writer.open(new ExecutionContext());
        writer.write(new Chunk<>(List.of("LINE-1", "LINE-2")));
        assertTrue(tmp.exists());
        assertFalse(destination.exists());

        writer.afterStep(stepExecution(BatchStatus.COMPLETED));

        assertTrue(destination.exists());
        assertFalse(tmp.exists());
        assertEquals(List.of("LINE-1", "LINE-2"), Files.readAllLines(destination.toPath(), StandardCharsets.UTF_8));
    }

    @Test
    public void testFailedStepRemovesIncompleteOutput() throws Exception {
        File destination = tempDir.resolve("fail.dat").toFile();
        File tmp = tempDir.resolve("fail.dat" + EgovSafeFlatFileItemWriter.TMP_SUFFIX).toFile();
        EgovSafeFlatFileItemWriter<String> writer = writer(destination, 0);

        writer.open(new ExecutionContext());
        writer.write(new Chunk<>(List.of("PARTIAL")));
        writer.afterStep(stepExecution(BatchStatus.FAILED));

        assertFalse(destination.exists());
        assertFalse(tmp.exists());
    }

    @Test
    public void testCompletedStepReplacesExistingOutput() throws Exception {
        File destination = tempDir.resolve("replace.dat").toFile();
        Files.write(destination.toPath(), List.of("OLD-CONTENT"), StandardCharsets.UTF_8);
        EgovSafeFlatFileItemWriter<String> writer = writer(destination, 0);

        writer.open(new ExecutionContext());
        writer.write(new Chunk<>(List.of("NEW-CONTENT")));
        writer.afterStep(stepExecution(BatchStatus.COMPLETED));

        assertEquals(List.of("NEW-CONTENT"), Files.readAllLines(destination.toPath(), StandardCharsets.UTF_8));
    }

    @Test
    public void testMaxFileSizeGuardRejectsOversizedChunkBeforeWriting() throws Exception {
        File destination = tempDir.resolve("limited.dat").toFile();
        File tmp = tempDir.resolve("limited.dat" + EgovSafeFlatFileItemWriter.TMP_SUFFIX).toFile();
        // "12345678" + "\n" = 9 bytes 허용, 이어지는 "X" + "\n" = 2 bytes 로 한도 10 초과
        EgovSafeFlatFileItemWriter<String> writer = writer(destination, 10);

        writer.open(new ExecutionContext());
        writer.write(new Chunk<>(List.of("12345678")));
        WriteFailedException ex = assertThrows(WriteFailedException.class, () -> writer.write(new Chunk<>(List.of("X"))));
        assertTrue(ex.getMessage().contains("size limit"), ex.getMessage());

        writer.afterStep(stepExecution(BatchStatus.FAILED));
        assertFalse(tmp.exists());
        assertFalse(destination.exists());
    }

    @Test
    public void testOpenWithoutResourceIsRejectedAndStaleTemporaryFileIsReplaced() throws Exception {
        EgovSafeFlatFileItemWriter<String> noResource = new EgovSafeFlatFileItemWriter<>();
        noResource.setLineAggregator(new PassThroughLineAggregator<>());
        assertThrows(ItemStreamException.class, () -> noResource.open(new ExecutionContext()));

        File destination = tempDir.resolve("stale.dat").toFile();
        File tmp = tempDir.resolve("stale.dat" + EgovSafeFlatFileItemWriter.TMP_SUFFIX).toFile();
        Files.write(tmp.toPath(), List.of("LEFTOVER"), StandardCharsets.UTF_8);
        EgovSafeFlatFileItemWriter<String> writer = writer(destination, 0);

        writer.open(new ExecutionContext());
        writer.write(new Chunk<>(List.of("FRESH")));
        writer.afterStep(stepExecution(BatchStatus.COMPLETED));

        assertEquals(List.of("FRESH"), Files.readAllLines(destination.toPath(), StandardCharsets.UTF_8));
    }

}
