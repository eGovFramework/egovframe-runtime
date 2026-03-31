package org.egovframe.rte.psl.dataaccess.mybatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.EmpMapper;
import org.egovframe.rte.psl.dataaccess.mapper.EmployerMapper;
import org.egovframe.rte.psl.dataaccess.resulthandler.FileWritingResultHandler;
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
 * 2014.01.22 권윤정 최초생성
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DataAccessTestConfig.class)
@Transactional
public class ResultHandlerMapperTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "schemaProperties")
    private Properties schemaProperties;

    @Resource(name = "employerMapper")
    private EmployerMapper employerMapper;

    @Resource(name = "empMapper")
    private EmpMapper empMapper;

    @Resource(name = "fileWritingResultHandler")
    private FileWritingResultHandler resultHandler;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    @Rollback(false)
    @Test
    public void testResultHandlerForOutFileWriting() throws IOException {
        //  1. DAO방식 테스트
        empMapper.selectEmpListToOutFileUsingResultHandler("org.egovframe.rte.psl.dataaccess.mapper.EmployerMapper.selectEmpListToOutFileUsingResultHandler", resultHandler);

        // 2. Mapper방식 테스트
        employerMapper.selectEmpListToOutFileUsingResultHandler(resultHandler);

        // check
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        org.springframework.core.io.Resource resource = resourceLoader.getResource("file:./src/test/resources/META-INF/spring/" + schemaProperties.getProperty("outResultFile"));

        // 각 38,416개씩 두번 실행했으므로 총 76,832개 출력
        assertEquals(76832, resultHandler.getTotalCount());

        File file = resource.getFile();
        assertNotNull(file);

        // 대용량 out file size 체크
        assertTrue(1000000 < file.length());
    }

}
