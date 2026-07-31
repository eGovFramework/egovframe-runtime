package org.egovframe.rte.fdl.excel;

import jakarta.annotation.Resource;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 엑셀 파일 생성 후 반환된 Workbook을 계속 사용할 수 있는지 검증한다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ExcelTestConfig.class)
public class EgovExcelCreateWorkbookContractTest {

    private final String fileLocation = "testdata";

    @Resource(name = "excelService")
    private EgovExcelService excelService;

    /**
     * XSSF(.xlsx) : createWorkbook(Workbook, String)이 전달받은 Workbook을 닫지 않고 반환해야 한다.
     */
    @Test
    public void testCreateWorkbookReturnsWritableXssf() throws IOException {
        String path = fileLocation + "/createContract.xlsx";
        if (EgovFileUtil.isExistsFile(path)) {
            EgovFileUtil.delete(new File(path));
        }

        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("data");
        sheet.createRow(0).createCell(0).setCellValue("hello");

        Workbook returned = excelService.createWorkbook(wb, path);
        assertSame(wb, returned);

        returned.createSheet("more");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        returned.write(baos);
        assertTrue(baos.size() > 0, "returned XSSF workbook must be writable");
    }

    /**
     * HSSF(.xls) : createWorkbook(Workbook, String)이 전달받은 Workbook을 닫지 않고 반환해야 한다.
     */
    @Test
    public void testCreateWorkbookReturnsWritableHssf() throws IOException {
        String path = fileLocation + "/createContract.xls";
        if (EgovFileUtil.isExistsFile(path)) {
            EgovFileUtil.delete(new File(path));
        }

        HSSFWorkbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("data");
        sheet.createRow(0).createCell(0).setCellValue("hello");

        Workbook returned = excelService.createWorkbook(wb, path);
        assertSame(wb, returned);

        returned.createSheet("more");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        returned.write(baos);
        assertTrue(baos.size() > 0, "returned HSSF workbook must be writable");
    }

    /**
     * Workbook close()가 실패하더라도 FileOutputStream은 createWorkbook 내부에서 닫혀야 한다.
     */
    @Test
    public void testOutputStreamClosedWhenWorkbookCloseFails() throws IOException {
        String path = fileLocation + "/createContractCloseFails.xls";
        if (EgovFileUtil.isExistsFile(path)) {
            EgovFileUtil.delete(new File(path));
        }

        OutputStream[] captured = new OutputStream[1];
        Workbook wb = closeFailingWorkbook(captured);

        assertSame(wb, assertDoesNotThrow(() -> excelService.createWorkbook(wb, path)));
        assertFalse(((FileOutputStream) captured[0]).getChannel().isOpen());
    }

    private Workbook closeFailingWorkbook(OutputStream[] captured) {
        return (Workbook) Proxy.newProxyInstance(Workbook.class.getClassLoader(), new Class<?>[]{Workbook.class}, (proxy, method, args) -> {
            if ("write".equals(method.getName())) {
                OutputStream stream = (OutputStream) args[0];
                captured[0] = stream;
                stream.write(1);
                return null;
            }
            if ("close".equals(method.getName())) {
                throw new IOException("close failure");
            }
            if ("equals".equals(method.getName())) {
                return proxy == args[0];
            }
            if ("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            }
            if ("toString".equals(method.getName())) {
                return "closeFailingWorkbook";
            }
            return null;
        });
    }
}
