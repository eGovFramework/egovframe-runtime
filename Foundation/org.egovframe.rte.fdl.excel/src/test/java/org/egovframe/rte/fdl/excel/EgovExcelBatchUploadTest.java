package org.egovframe.rte.fdl.excel;

import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egovframe.rte.fdl.excel.config.ExcelTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 엑셀 업로드의 xls·xlsx 자동 감지(uploadExcelAuto)와 트랜잭션 밖 JDBC 배치 실행을 검증한다.
 *
 * <p>의도적으로 @Transactional 없이 실행한다. 트랜잭션 밖에서는 BATCH executor 세션이 쓰이고 반환 건수가 실제 저장 행 수와
 * 일치해야 하며, 트랜잭션 안에서는 기존 행별 실행이 둘러싼 트랜잭션에 참여해야 한다.</p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ExcelTestConfig.class)
public class EgovExcelBatchUploadTest {

    private static final String XLSX = "testdata/testBatch.xlsx";
    private static final String XLS = "testdata/testBatch.xls";
    private static final String QUERY_ID = "insertEmpUsingBatch";

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "excelService")
    private EgovExcelService excelService;

    @Resource(name = "txManager")
    private PlatformTransactionManager txManager;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void onSetUp() throws SQLException {
        jdbcTemplate = new JdbcTemplate(dataSource);
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    private int empCount() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from emp", Integer.class);
        return count == null ? 0 : count;
    }

    private static int rowsIn(String path) throws IOException {
        try (Workbook wb = WorkbookFactory.create(new File(path))) {
            return wb.getSheetAt(0).getPhysicalNumberOfRows();
        }
    }

    private Integer uploadAuto(String path, int start, long commitCnt) throws IOException {
        try (InputStream in = new FileInputStream(path)) {
            return excelService.uploadExcelAuto(QUERY_ID, in, start, commitCnt);
        }
    }

    @Test
    public void testAutoDetectUploadOfXlsxReturnsTheStoredRowCount() throws IOException {
        int expected = rowsIn(XLSX);

        Integer affected = uploadAuto(XLSX, 0, 0);

        assertEquals(expected, affected);
        assertEquals(expected, empCount());
    }

    @Test
    public void testAutoDetectUploadOfXlsUsesTheSameApi() throws IOException {
        int expected = rowsIn(XLS);

        Integer affected = uploadAuto(XLS, 0, 0);

        assertEquals(expected, affected);
        assertEquals(expected, empCount());
    }

    @Test
    public void testCommitChunksStoreEveryRowAndReturnTheTotal() throws IOException {
        int expected = rowsIn(XLSX);

        Integer affected = uploadAuto(XLSX, 0, 1000);

        assertEquals(expected, affected);
        assertEquals(expected, empCount());
    }

    @Test
    public void testStartOffsetSkipsLeadingRows() throws IOException {
        int total = rowsIn(XLSX);

        Integer affected = uploadAuto(XLSX, total - 2, 0);

        assertEquals(2, affected);
        assertEquals(2, empCount());
    }

    @Test
    public void testSheetUploadOutsideTransactionUsesTheBatchPath() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("emp");
            createEmpRow(sheet, 0, 9001, "kim", "dev");
            createEmpRow(sheet, 1, 9002, "lee", "ops");
            createEmpRow(sheet, 2, 9003, "park", "qa");

            Integer affected = excelService.uploadExcel(QUERY_ID, (Sheet) sheet, 1, 0);

            assertEquals(2, affected);
            assertEquals(2, empCount());
        }
    }

    @Test
    public void testUploadInsideTransactionJoinsTheEnclosingTransaction() throws IOException {
        int expected = rowsIn(XLSX);
        TransactionTemplate tx = new TransactionTemplate(txManager);

        tx.executeWithoutResult(status -> {
            try {
                uploadAuto(XLSX, 0, 0);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            status.setRollbackOnly();
        });
        assertEquals(0, empCount());

        tx.executeWithoutResult(status -> {
            try {
                uploadAuto(XLSX, 0, 0);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
        assertEquals(expected, empCount());
    }

    @Test
    public void testDuplicateKeysAreReportedAsDataAccessException() throws IOException {
        uploadAuto(XLSX, 0, 0);

        assertThrows(DataAccessException.class, () -> uploadAuto(XLSX, 0, 0));
    }

    @Test
    public void testDefaultUploadExcelAutoIsUnsupportedForOtherImplementations() {
        EgovExcelService other = Mockito.mock(EgovExcelService.class, Mockito.CALLS_REAL_METHODS);

        assertThrows(UnsupportedOperationException.class, () -> other.uploadExcelAuto(QUERY_ID, InputStream.nullInputStream(), 0, 0));
    }

    private static void createEmpRow(XSSFSheet sheet, int rowIdx, int empNo, String name, String job) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(empNo);
        row.createCell(1).setCellValue(name);
        row.createCell(2).setCellValue(job);
    }

}
