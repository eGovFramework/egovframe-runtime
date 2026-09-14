package org.egovframe.rte.fdl.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egovframe.rte.fdl.cmmn.exception.BaseRuntimeException;
import org.egovframe.rte.fdl.excel.util.EgovWorkbooks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovWorkbooks 의 형식 자동 감지 로딩과 SXSSF 스트리밍 쓰기를 검증한다.
 */
public class EgovWorkbooksTest {

    @TempDir
    Path tempDir;

    private File save(Workbook wb, String name) throws IOException {
        File target = tempDir.resolve(name).toFile();
        EgovWorkbooks.write(wb, target);
        wb.close();
        return target;
    }

    @Test
    public void testXlsIsDetectedByContentNotExtension() throws IOException {
        HSSFWorkbook src = new HSSFWorkbook();
        src.createSheet("legacy").createRow(0).createCell(0).setCellValue("xls");
        File saved = save(src, "misleading.dat");

        try (Workbook opened = EgovWorkbooks.open(saved)) {
            assertInstanceOf(HSSFWorkbook.class, opened);
            assertEquals("xls", opened.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        }
    }

    @Test
    public void testXlsxIsDetectedWithTheSameApi() throws IOException {
        XSSFWorkbook src = new XSSFWorkbook();
        src.createSheet("modern").createRow(0).createCell(0).setCellValue("xlsx");
        File saved = save(src, "modern.bin");

        try (Workbook opened = EgovWorkbooks.open(saved)) {
            assertInstanceOf(XSSFWorkbook.class, opened);
            assertEquals("xlsx", opened.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        }
    }

    @Test
    public void testStreamInputWithoutMarkSupportIsBuffered() throws IOException {
        XSSFWorkbook src = new XSSFWorkbook();
        src.createSheet("s").createRow(0).createCell(0).setCellValue("stream");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        src.write(bytes);
        src.close();
        InputStream noMark = new InputStream() {
            private final InputStream delegate = new ByteArrayInputStream(bytes.toByteArray());

            @Override
            public int read() throws IOException {
                return delegate.read();
            }

            @Override
            public boolean markSupported() {
                return false;
            }
        };

        try (Workbook opened = EgovWorkbooks.open(noMark)) {
            assertEquals("stream", opened.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        }
    }

    @Test
    public void testStreamingWorkbookWritesFiveThousandRowsAndReadsBack() throws IOException {
        SXSSFWorkbook streaming = EgovWorkbooks.createStreaming();
        Sheet sheet = streaming.createSheet("big");
        for (int i = 0; i < 5_000; i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(i);
            row.createCell(1).setCellValue("행-" + i);
        }
        File saved = save(streaming, "big.xlsx");

        try (Workbook opened = EgovWorkbooks.open(saved)) {
            Sheet readBack = opened.getSheetAt(0);
            assertEquals(4_999, readBack.getLastRowNum());
            assertEquals("행-4999", readBack.getRow(4_999).getCell(1).getStringCellValue());
        }
    }

    @Test
    public void testWriteCreatesMissingParentDirectories() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        wb.createSheet("s");
        File nested = tempDir.resolve("out/deep/wb.xlsx").toFile();

        EgovWorkbooks.write(wb, nested);
        wb.close();

        assertTrue(nested.exists());
        try (Workbook opened = EgovWorkbooks.open(nested)) {
            assertEquals(1, opened.getNumberOfSheets());
        }
    }

    @Test
    public void testNonExcelAndNullInputsAreRejected() throws IOException {
        Path bogus = tempDir.resolve("not-excel.txt");
        Files.writeString(bogus, "plain text", StandardCharsets.UTF_8);

        assertThrows(BaseRuntimeException.class, () -> EgovWorkbooks.open(bogus.toFile()));
        assertThrows(BaseRuntimeException.class, () -> EgovWorkbooks.open(tempDir.resolve("missing.xlsx").toFile()));
        assertThrows(IllegalArgumentException.class, () -> EgovWorkbooks.open((File) null));
        assertThrows(IllegalArgumentException.class, () -> EgovWorkbooks.open((InputStream) null));
        assertThrows(IllegalArgumentException.class, () -> EgovWorkbooks.write(null, tempDir.resolve("x.xlsx").toFile()));
    }
}
