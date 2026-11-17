package org.egovframe.rte.fdl.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egovframe.rte.fdl.excel.upload.EgovExcelTestMapping;
import org.egovframe.rte.fdl.excel.util.EgovExcelUtil;
import org.egovframe.rte.fdl.excel.vo.EmpVO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 수식 셀에서 EgovExcelUtil.getValue 가 값을 돌려주는지 검증한다.
 * <p>
 * 캐시 결과 타입과 다른 접근자 호출 시 POI 의 IllegalStateException 누출 방지
 * (IllegalArgumentException catch 로는 잡히지 않음).
 */
public class EgovExcelUtilFormulaCellTest {

    @Test
    public void testNumericFormulaOnHssf() throws IOException {
        assertEquals("3", valueOfFormula(HSSFWorkbook::new, "1+2"));
    }

    @Test
    public void testNumericFormulaOnXssf() throws IOException {
        assertEquals("3", valueOfFormula(XSSFWorkbook::new, "1+2"));
    }

    @Test
    public void testStringFormulaOnHssf() throws IOException {
        assertEquals("ABCD", valueOfFormula(HSSFWorkbook::new, "\"AB\"&\"CD\""));
    }

    @Test
    public void testStringFormulaOnXssf() throws IOException {
        assertEquals("ABCD", valueOfFormula(XSSFWorkbook::new, "CONCATENATE(\"AB\",\"CD\")"));
    }

    /**
     * 캐시 결과가 문자열도 숫자도 아니면 수식 문자열을 돌려주는 폴백 분기를 탄다.
     */
    @Test
    public void testBooleanFormulaFallsBackToFormulaText() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Cell cell = evaluatedFormulaCell(wb, "TRUE()");
            assertEquals(CellType.BOOLEAN, cell.getCachedFormulaResultType());
            assertEquals("TRUE()", EgovExcelUtil.getValue(cell));
        }
    }

    /**
     * 프레임워크가 배포한 매핑 예제가 수식 셀이 든 행을 매핑할 수 있어야 한다.
     */
    @Test
    public void testShippedMappingHandlesFormulaCell() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("emp");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue(1001);
            row.createCell(1).setCellFormula("\"HONG\"&\"GILDONG\"");
            row.createCell(2).setCellFormula("1+2");
            wb.getCreationHelper().createFormulaEvaluator().evaluateAll();

            EmpVO vo = new EgovExcelTestMapping().mappingColumn(row);

            assertEquals("HONGGILDONG", vo.getEmpName());
            assertEquals("3", vo.getJob());
        }
    }

    private String valueOfFormula(Supplier<Workbook> workbookSupplier, String formula) throws IOException {
        try (Workbook wb = workbookSupplier.get()) {
            return EgovExcelUtil.getValue(evaluatedFormulaCell(wb, formula));
        }
    }

    private Cell evaluatedFormulaCell(Workbook wb, String formula) {
        Cell cell = wb.createSheet().createRow(0).createCell(0);
        cell.setCellFormula(formula);
        wb.getCreationHelper().createFormulaEvaluator().evaluateFormulaCell(cell);
        return cell;
    }

}
