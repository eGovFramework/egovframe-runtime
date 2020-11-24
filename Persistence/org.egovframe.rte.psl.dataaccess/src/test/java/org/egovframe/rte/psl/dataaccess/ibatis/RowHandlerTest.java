package org.egovframe.rte.psl.dataaccess.ibatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.dao.EmpDAO;
import org.egovframe.rte.psl.dataaccess.rowhandler.FileWritingRowHandler;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2014.01.22 권윤정  SimpleJdbcTestUtils -> JdbcTestUtils 변경
 *   2014.01.22 권윤정  SimpleJdbcTemplate -> JdbcTemplate 변경
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-*.xml" })
@Transactional
public class RowHandlerTest extends TestBase {

	@Resource(name = "schemaProperties")
	Properties schemaProperties;

	@Resource(name = "empDAO")
	EmpDAO empDAO;

	// fileWritingRowHandler 는 prototype 으로 선언했음
	@Resource(name = "fileWritingRowHandler")
	FileWritingRowHandler rowHandler;

	boolean isHsql = true;

	@Before
	public void onSetUp() throws Exception {
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"));

		// init data
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_initdata_" + usingDBMS + ".sql"));
	}

	@SuppressWarnings("deprecation")
	@Rollback(false)
	@Test
	public void testRowHandlerForOutFileWriting() throws Exception {

		// select to outFile using rowHandler
		empDAO.getSqlMapClientTemplate().queryWithRowHandler("selectEmpListToOutFileUsingRowHandler", null, rowHandler);

		// check
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		org.springframework.core.io.Resource resource = resourceLoader.getResource("file:./src/test/resources/META-INF/testdata/" + schemaProperties.getProperty("outResultFile"));
		// BufferedOutputStream flush 및 close
		rowHandler.releaseResource();

		assertEquals(38416, rowHandler.getTotalCount());

		File file = resource.getFile();
		assertNotNull(file);
		// 대용량 out file size 체크
		assertTrue(1000000 < file.length());
	}

}
