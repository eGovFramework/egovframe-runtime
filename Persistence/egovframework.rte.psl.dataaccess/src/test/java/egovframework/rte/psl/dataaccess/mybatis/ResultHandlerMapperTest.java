package egovframework.rte.psl.dataaccess.mybatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import egovframework.rte.psl.dataaccess.TestBase;
import egovframework.rte.psl.dataaccess.dao.EmpMapper;
import egovframework.rte.psl.dataaccess.mapper.EmployerMapper;
import egovframework.rte.psl.dataaccess.resulthandler.FileWritingResultHandler;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileWritingResultHandler 테스트
 * 
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2014.01.22 권윤정 최초생성
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-*.xml" })
@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class ResultHandlerMapperTest extends TestBase {

	@Resource(name = "schemaProperties")
	Properties schemaProperties;

	@Resource(name = "employerMapper")
	EmployerMapper employerMapper;

	@Resource(name = "empMapper")
	EmpMapper empMapper;

	// fileWritingRowHandler 는 prototype 으로 선언했음
	@Resource(name = "fileWritingResultHandler")
	FileWritingResultHandler resultHandler;

	boolean isHsql = true;

	@Before
	public void onSetUp() throws Exception {
		JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"), true);

		// init data
		JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource("META-INF/testdata/sample_schema_initdata_" + usingDBMS + ".sql"), true);
	}

	@Test
	public void testResultHandlerForOutFileWriting() throws Exception {

		// select to outFile using resultHandler
		//  1. DAO방식 테스트
		empMapper.selectEmpListToOutFileUsingResultHandler("egovframework.rte.psl.dataaccess.mapper.EmployerMapper.selectEmpListToOutFileUsingResultHandler", resultHandler);
		// 2. Mapper방식 테스트
		employerMapper.selectEmpListToOutFileUsingResultHandler(resultHandler);

		// check
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		org.springframework.core.io.Resource resource = resourceLoader.getResource("file:./src/test/resources/META-INF/testdata/" + schemaProperties.getProperty("outResultFile"));

		// BufferedOutputStream flush 및 close
		// resultHandler.releaseResource();

		// 각 38,416개씩 두번 실행했으므로 총 76,832개 출력
		assertEquals(76832, resultHandler.getTotalCount());

		File file = resource.getFile();
		assertNotNull(file);
		// 대용량 out file size 체크
		assertTrue(1000000 < file.length());
	}
}