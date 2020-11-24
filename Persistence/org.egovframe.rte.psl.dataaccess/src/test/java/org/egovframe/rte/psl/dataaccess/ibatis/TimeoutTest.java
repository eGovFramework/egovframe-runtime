package org.egovframe.rte.psl.dataaccess.ibatis;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Reader;
import java.sql.SQLException;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.dao.EmpDAO;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

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
public class TimeoutTest extends TestBase {

	@Resource(name = "empDAO")
	EmpDAO empDAO;

	@Before
	public void onSetUp() throws Exception {
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"));

		// init data
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_initdata_" + usingDBMS + ".sql"));
	}

	@SuppressWarnings("deprecation")
	@Rollback(false)
	@Test
	public void testTimeOut() throws Exception {
		// select 시 timeout 테스트 실패!!
		// // 대량 데이터 조회 시 timeout 설정 테스트
		// // 대량 데이터 조회를 위해 조인 조건없이 catesian product 로
		// 14^5 = 537824 건 조회
		// // select a.* from EMP a, EMP b, EMP c, EMP
		// d, EMP e
		//
		// // select
		// List<EmpVO> resultList =
		// empDAO.selectEmpList("selectTestLargeTimeOut",
		// null);
		//
		// assertTrue("== resultList.size() : " +
		// resultList.size() + " ==",
		// resultList.size() < 537824);

		// Oracle 인 경우 insert ~ select
		// hsql 인 경우 timeout 미지원, tibero 인 경우에는
		// org.springframework.transaction.TransactionSystemException
		// 이 발생하였으며
		// catch 할 수 없음.
		if (!isHsql && !isTibero) {
			try {
				// insert ~ select
				empDAO.getSqlMapClientTemplate().insert("insertTestLargeTimeOut");

				fail("timeout 테스트이므로 더 시간이 많이 걸리는 쿼리를 사용해야 합니다.");
			} catch (Exception e) {
				assertNotNull(e);
				assertTrue(e.getCause() instanceof SQLException);
				if (isOracle) {
					assertTrue(e instanceof UncategorizedSQLException);
					assertTrue(e.getCause().getMessage().contains("ORA-01013"));
				} else if (isMysql) {
					assertTrue(e.getMessage().contains("Statement cancelled due to timeout or client request"));
				} else if (isHsql) {
					assertTrue(e.getMessage().contains("Query execution was interrupted"));
				}
			}
		}

	}

	@Rollback(false)
	@Test
	public void testOrgSqlMapInsertTimeOut() throws Exception {

		if (!isHsql) {
			Reader reader = Resources.getResourceAsReader("META-INF/sqlmap/sql-map-config-org.xml");
			SqlMapClient sqlMap = SqlMapClientBuilder.buildSqlMapClient(reader);

			try {
				// insert ~ select
				sqlMap.insert("insertTestLargeTimeOut");

				fail("timeout 테스트이므로 더 시간이 많이 걸리는 쿼리를 사용해야 합니다.");
			} catch (Exception e) {
				assertNotNull(e);
				assertTrue(e instanceof SQLException);
				if (isOracle) {
					assertTrue(e.getMessage().contains("ORA-01013"));
				} else if (isMysql) {
					assertTrue(e.getMessage().contains("Statement cancelled due to timeout or client request"));
				} else if (isHsql) {
					assertTrue(e.getMessage().contains("Query execution was interrupted"));
				}
			}
		}

	}
}
