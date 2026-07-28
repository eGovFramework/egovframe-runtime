package org.egovframe.rte.fdl.excel;

import jakarta.annotation.Resource;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egovframe.rte.fdl.excel.config.ExcelTestConfig;
import org.egovframe.rte.fdl.filehandling.EgovFileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 엑셀 파일을 로딩한 뒤 그대로 다시 write() 할 수 있는지 검증한다.
 * <p>
 * loadWorkbook/loadExcelTemplate 계열 메서드가 반환 직전에 Workbook(및 HSSF의 경우
 * POIFSFileSystem)을 닫아 버리면, "로드 → 수정 → 저장" 워크플로우에서 write() 시
 * 예외가 발생한다(XSSF: "document seems to have been closed already", HSSF:
 * "directory cannot be null"). 본 테스트는 로딩된 Workbook이 write() 가능한 상태로
 * 반환되는지 확인하여 해당 회귀를 방지한다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ExcelTestConfig.class)
public class EgovExcelLoadThenWriteTest {

    private final String fileLocation = "testdata";

    @Resource(name = "excelService")
    private EgovExcelService excelService;

    /**
     * XSSF(.xlsx) : loadWorkbook(String, XSSFWorkbook)로 로딩한 뒤 write() 가능해야 한다.
     */
    @Test
    public void testLoadWorkbookThenWriteXssf() throws IOException {
        String path = fileLocation + "/loadThenWrite.xlsx";
        if (EgovFileUtil.isExistsFile(path)) {
            EgovFileUtil.delete(new File(path));
        }

        XSSFWorkbook src = new XSSFWorkbook();
        Sheet sheet = src.createSheet("data");
        sheet.createRow(0).createCell(0).setCellValue("hello");
        excelService.createWorkbook(src, path);

        Workbook loaded = excelService.loadWorkbook(path, new XSSFWorkbook());
        Cell cell = loaded.getSheetAt(0).getRow(0).getCell(0);
        assertEquals("hello", cell.getStringCellValue());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        loaded.write(baos);
        assertTrue(baos.size() > 0, "loaded XSSF workbook must be writable");
    }

    /**
     * XSSF(.xlsx) : loadExcelTemplate(String, XSSFWorkbook)로 로딩한 뒤 write() 가능해야 한다.
     */
    @Test
    public void testLoadExcelTemplateThenWriteXssf() throws IOException {
        String path = fileLocation + "/loadTemplateThenWrite.xlsx";
        if (EgovFileUtil.isExistsFile(path)) {
            EgovFileUtil.delete(new File(path));
        }

        XSSFWorkbook src = new XSSFWorkbook();
        src.createSheet("data").createRow(0).createCell(0).setCellValue("tmpl");
        excelService.createWorkbook(src, path);

        XSSFWorkbook loaded = excelService.loadExcelTemplate(path, new XSSFWorkbook());
        assertEquals("tmpl", loaded.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        loaded.write(baos);
        assertTrue(baos.size() > 0, "loaded XSSF template must be writable");
    }

    /**
     * HSSF(.xls) : loadWorkbook(String)으로 로딩한 뒤 write() 가능해야 한다.
     */
    @Test
    public void testLoadWorkbookThenWriteHssf() throws IOException {
        String path = fileLocation + "/loadThenWrite.xls";
        if (EgovFileUtil.isExistsFile(path)) {
            EgovFileUtil.delete(new File(path));
        }

        HSSFWorkbook src = new HSSFWorkbook();
        src.createSheet("data").createRow(0).createCell(0).setCellValue("hssf");
        excelService.createWorkbook(src, path);

        Workbook loaded = excelService.loadWorkbook(path);
        assertEquals("hssf", loaded.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        loaded.write(baos);
        assertTrue(baos.size() > 0, "loaded HSSF workbook must be writable");
    }

    /**
     * HSSF(.xls) : loadExcelTemplate(String)으로 로딩한 뒤 write() 가능해야 한다.
     */
    @Test
    public void testLoadExcelTemplateThenWriteHssf() throws IOException {
        String path = fileLocation + "/loadTemplateThenWrite.xls";
        if (EgovFileUtil.isExistsFile(path)) {
            EgovFileUtil.delete(new File(path));
        }

        HSSFWorkbook src = new HSSFWorkbook();
        src.createSheet("data").createRow(0).createCell(0).setCellValue("hssftmpl");
        excelService.createWorkbook(src, path);

        Workbook loaded = excelService.loadExcelTemplate(path);
        assertEquals("hssftmpl", loaded.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        loaded.write(baos);
        assertTrue(baos.size() > 0, "loaded HSSF template must be writable");
    }
}
