package org.egovframe.rte.fdl.excel;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.excel.config.ExcelTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ExcelTestConfig.class)
@Transactional
public class EgovExcelUploadTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovExcelUploadTest.class);

    @Resource(name = "dataSource")
    protected DataSource dataSource;

    @Resource(name = "excelService")
    private EgovExcelService excelService;

    @Resource(name = "excelBigService")
    private EgovExcelService excelBigService;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    /**
     * 엑셀파일 업로드
     */
    @Rollback(false)
    @Test
    public void testUploadExcelFile() {
        LOGGER.debug("### EgovExcelUploadTest testUploadExcelFile() Start ");
        try {
            FileInputStream fileIn = new FileInputStream(new File("testdata/testBatch.xls"));
            excelService.uploadExcel("insertEmpUsingBatch", fileIn);
        } catch (Exception e) {
            LOGGER.debug("[{}] EgovExcelUploadTest testUploadExcelFile() : {}", e.getMessage());
        } finally {
            LOGGER.debug("### EgovExcelUploadTest testUploadExcelFile() End ");
        }
    }

    /**
     * 대용량 엑셀파일 업로드
     */
    @Rollback(false)
    @Test
    public void testBigUploadExcelFile() {
        LOGGER.debug("### EgovExcelUploadTest testBigUploadExcelFile() Start ");
        try {
            FileInputStream fileIn = new FileInputStream(new File("testdata/zipExcel.xls"));
            excelBigService.uploadExcel("insertZipUsingBatch", fileIn, 2, (long) 5000);
        } catch (Exception e) {
            LOGGER.debug("### EgovExcelUploadTest testBigUploadExcelFile() Exception : {}", e.getMessage());
        } finally {
            LOGGER.debug("### EgovExcelUploadTest testBigUploadExcelFile() End ");
        }
    }

}
