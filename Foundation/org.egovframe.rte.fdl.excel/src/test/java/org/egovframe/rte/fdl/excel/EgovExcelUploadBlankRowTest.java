package org.egovframe.rte.fdl.excel;

import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egovframe.rte.fdl.excel.config.ExcelTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * 빈 행이 섞인 시트를 업로드할 때 데이터 행이 누락되거나 업로드가 중단되지 않는지 검증한다.
 * <p>
 * uploadExcel 은 sheet.getPhysicalNumberOfRows() 로 얻은 "행 개수"를 "행 인덱스" 상한으로 사용했다.
 * 빈 행이 있으면 두 값이 어긋나므로 시작 위치 앞에 빈 행이 있으면 마지막 데이터 행이 누락되고,
 * 데이터 중간에 빈 행이 있으면 sheet.getRow(i) 가 null 을 돌려줘 매핑에서 NullPointerException 이 난다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ExcelTestConfig.class)
public class EgovExcelUploadBlankRowTest {

    private static final String QUERY_ID = "insertEmpUsingBatch";

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "excelService")
    private EgovExcelService excelService;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 시작 위치 앞에 빈 행이 있으면 마지막 데이터 행이 예외 없이 누락된다.
     */
    @Test
    public void testBlankRowBeforeStartKeepsEveryDataRow() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("emp");
            createHeaderRow(sheet, 0);
            // 1행은 만들지 않아 빈 행으로 둔다
            createDataRows(sheet, 2, 5);

            excelService.uploadExcel(QUERY_ID, sheet, 2, 0);

            assertEquals(List.of(1001, 1002, 1003, 1004, 1005), selectEmpNos());
        }
    }

    /**
     * 데이터 중간의 빈 행에서 업로드가 중단되지 않는다.
     */
    @Test
    public void testBlankRowBetweenDataRowsDoesNotAbortUpload() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("emp");
            createDataRows(sheet, 0, 2);
            // 2행은 만들지 않아 빈 행으로 둔다
            createDataRows(sheet, 3, 2, 1003);

            assertDoesNotThrow(() -> excelService.uploadExcel(QUERY_ID, sheet, 0, 0));

            assertEquals(List.of(1001, 1002, 1003, 1004), selectEmpNos());
        }
    }

    /**
     * commitCnt 로 청크 커밋할 때도 빈 행 때문에 청크가 잘리지 않는다.
     */
    @Test
    public void testBlankRowWithCommitCountKeepsEveryDataRow() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("emp");
            createHeaderRow(sheet, 0);
            // 1행은 만들지 않아 빈 행으로 둔다
            createDataRows(sheet, 2, 5);

            excelService.uploadExcel(QUERY_ID, sheet, 2, 2);

            assertEquals(List.of(1001, 1002, 1003, 1004, 1005), selectEmpNos());
        }
    }

    /**
     * 빈 행이 없는 시트의 결과는 그대로다.
     */
    @Test
    public void testSheetWithoutBlankRowIsUnaffected() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("emp");
            createHeaderRow(sheet, 0);
            createDataRows(sheet, 1, 5);

            excelService.uploadExcel(QUERY_ID, sheet, 1, 0);

            assertEquals(List.of(1001, 1002, 1003, 1004, 1005), selectEmpNos());
        }
    }

    private void createHeaderRow(Sheet sheet, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue("EMP_NO");
        row.createCell(1).setCellValue("EMP_NAME");
        row.createCell(2).setCellValue("JOB");
    }

    private void createDataRows(Sheet sheet, int firstRowIndex, int count) {
        createDataRows(sheet, firstRowIndex, count, 1001);
    }

    private void createDataRows(Sheet sheet, int firstRowIndex, int count, int firstEmpNo) {
        for (int i = 0; i < count; i++) {
            Row row = sheet.createRow(firstRowIndex + i);
            row.createCell(0).setCellValue(firstEmpNo + i);
            row.createCell(1).setCellValue("NAME" + (firstEmpNo + i));
            row.createCell(2).setCellValue("CLERK");
        }
    }

    private List<Integer> selectEmpNos() {
        return jdbcTemplate.queryForList("select emp_no from emp order by emp_no", Integer.class);
    }

}
