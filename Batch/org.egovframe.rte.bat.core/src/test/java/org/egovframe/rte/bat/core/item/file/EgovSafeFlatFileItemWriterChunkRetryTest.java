package org.egovframe.rte.bat.core.item.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.WriteFailedException;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * 청크 재시도(트랜잭션 롤백 후 같은 청크로 write() 재호출) 시 쓴 바이트 수를 누적 카운터로 세면 실제 파일 크기와
 * 무관하게 계속 누적되어, 한도 이내인 파일도 부당하게 거부하는 문제를 검증한다.
 */
public class EgovSafeFlatFileItemWriterChunkRetryTest {

    @TempDir
    Path tempDir;

    private EgovSafeFlatFileItemWriter<String> writer(File destination, long maxBytes) throws Exception {
        EgovSafeFlatFileItemWriter<String> writer = new EgovSafeFlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(destination));
        writer.setLineAggregator(new PassThroughLineAggregator<>());
        writer.setLineSeparator("\n");
        writer.setMaxFileSizeBytes(maxBytes);
        writer.afterPropertiesSet();
        return writer;
    }

    @Test
    public void testRetriedChunkAfterRollbackIsNotRejectedWhenActualFileSizeIsWithinLimit() throws Exception {
        File destination = tempDir.resolve("retry.dat").toFile();
        // "LINE-1" + "\n" = 7 bytes. 청크 하나만큼만 허용, 같은 청크가 두 번 누적되면 한도를 넘는다.
        EgovSafeFlatFileItemWriter<String> writer = writer(destination, 7);
        Chunk<String> chunk = new Chunk<>(List.of("LINE-1"));
        TransactionTemplate transactionTemplate = new TransactionTemplate(new ResourcelessTransactionManager());

        writer.open(new ExecutionContext());

        // 첫 시도: 트랜잭션 안에서 write() 후 롤백 — 트랜잭션 버퍼에 있던 이 청크는 임시 파일에 쓰이지 않는다.
        transactionTemplate.execute(status -> {
            try {
                writer.write(chunk);
            } catch (Exception ex) {
                fail("첫 시도는 한도 이내라 실패하면 안 된다: " + ex.getMessage());
            }
            status.setRollbackOnly();
            return null;
        });

        // 재시도: 같은 청크를 다시 write() — 실제 파일에는 여전히 이 청크가 한 번도 반영되지
        // 않았으므로 한도 이내다. 누적 카운터로 셌다면 롤백을 반영하지 못해 이미 7B 를 센 상태가 된다.
        assertDoesNotThrow(() -> transactionTemplate.executeWithoutResult(status -> {
            try {
                writer.write(chunk);
            } catch (WriteFailedException ex) {
                throw new RuntimeException(ex);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }), "재시도된 청크는 실제 파일 크기가 한도 이내이므로 거부되면 안 된다");

        assertEquals(List.of("LINE-1"),
                Files.readAllLines(new File(tempDir.toFile(), "retry.dat" + EgovSafeFlatFileItemWriter.TMP_SUFFIX).toPath()));
    }

}
