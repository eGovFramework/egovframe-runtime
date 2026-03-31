package org.egovframe.rte.psl.dataaccess.ibatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.EmpDAO;
import org.egovframe.rte.psl.dataaccess.rowhandler.FileWritingRowHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2014.01.22 권윤정  SimpleJdbcTestUtils -> JdbcTestUtils 변경
 * 2014.01.22 권윤정  SimpleJdbcTemplate -> JdbcTemplate 변경
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DataAccessTestConfig.class)
@Transactional
public class RowHandlerTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "schemaProperties")
    private Properties schemaProperties;

    @Resource(name = "empDAO")
    private EmpDAO empDAO;

    @Resource(name = "fileWritingRowHandler")
    private FileWritingRowHandler rowHandler;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    @Rollback(false)
    @Test
    public void testRowHandlerForOutFileWriting() throws IOException {

        // select to outFile using rowHandler
        empDAO.getSqlMapClientTemplate().queryWithRowHandler("selectEmpListToOutFileUsingRowHandler", null, rowHandler);

        // check
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        org.springframework.core.io.Resource resource = resourceLoader.getResource("file:./src/test/resources/META-INF/spring/" + schemaProperties.getProperty("outResultFile"));
        rowHandler.releaseResource();

        assertEquals(38416, rowHandler.getTotalCount());

        File file = resource.getFile();
        assertNotNull(file);
        assertTrue(1000000 < file.length());
    }

}
