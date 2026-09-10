package org.egovframe.rte.bat.core.item.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link EgovIndexFileWriter}가 (+1) 옵션으로 초기 파일명을 만들 때
 * indexResource에 지정된 확장자를 그대로 사용하는지 검증한다.
 *
 * <p>configureWriterIndexResouce()가 검증하는 파일명 규칙은
 * "파일명" + "_NDX" + "(순번)" + ".확장자"이고, (순번) 앞부분은 {@code [a-zA-Z0-9_]+}로
 * 제한되어 점을 포함할 수 없다. 기존 파일이 있을 때 쓰이는 generateNewIndexFilename()은
 * (순번) 뒷부분을 확장자로 보존하므로, 초기 파일명도 같은 자리에서 확장자를 가져와야
 * 디렉토리 상태와 무관하게 같은 확장자가 나온다.</p>
 */
class EgovIndexFileWriterInitialExtensionTest {

    @TempDir
    Path tempDir;

    /** beforeStep() 공개 진입점으로 인덱스 리소스를 구성하고 확정된 파일명을 돌려준다. */
    private String resolveFilename(String indexResource) throws Exception {
        EgovIndexFileWriter<Object> writer = new EgovIndexFileWriter<>();
        writer.setIndexResource(indexResource);
        writer.beforeStep(new StepExecution("indexFileWriterStep", new JobExecution(1L)));

        assertNotNull(writer.getResource(), "resource가 설정되어야 한다");
        return writer.getResource().getFilename();
    }

    @Test
    @DisplayName("기존 파일이 없어도 지정한 확장자(.txt)로 초기 파일명을 만든다")
    void initialFilenameKeepsConfiguredExtension() throws Exception {
        String filename = resolveFilename(tempDir.toString() + "/DATA_NDX(1).txt");

        assertEquals(".txt", filename.substring(filename.lastIndexOf('.')),
                "indexResource에 지정한 확장자를 그대로 사용해야 한다 : " + filename);
    }

    @Test
    @DisplayName("확장자를 생략하면 초기 파일명에도 확장자가 붙지 않는다")
    void initialFilenameKeepsOmittedExtension() throws Exception {
        // 파일명 규칙 4) ".확장자" 생략 가능
        String filename = resolveFilename(tempDir.toString() + "/DATA_NDX(1)");

        assertEquals("DATA_NDX_", filename.substring(0, "DATA_NDX_".length()));
        assertEquals(-1, filename.indexOf('.'),
                "확장자를 지정하지 않았으므로 확장자가 붙지 않아야 한다 : " + filename);
    }

    @Test
    @DisplayName("기존 파일이 있으면 확장자가 보존된다(형제 경로 기준)")
    void nextFilenameKeepsExtension() throws Exception {
        Files.createFile(new File(tempDir.toFile(), "DATA_NDX_20260101000000.txt").toPath());

        assertEquals("DATA_NDX_20260101000001.txt",
                resolveFilename(tempDir.toString() + "/DATA_NDX(1).txt"));
    }
}
