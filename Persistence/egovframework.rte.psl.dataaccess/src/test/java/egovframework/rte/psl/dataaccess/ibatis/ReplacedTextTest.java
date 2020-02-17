package egovframework.rte.psl.dataaccess.ibatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import egovframework.rte.psl.dataaccess.TestBase;
import egovframework.rte.psl.dataaccess.dao.MapTypeDAO;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.jdbc.JdbcTestUtils;
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
@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
@SuppressWarnings("deprecation")
public class ReplacedTextTest extends TestBase {

	@Resource(name = "mapTypeDAO")
	MapTypeDAO mapTypeDAO;

	@Before
	public void onSetUp() throws Exception {
		JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"), true);

		// init data
		JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource("META-INF/testdata/sample_schema_initdata_" + usingDBMS + ".sql"), true);
	}

	@Test
	public void testReplaceTextOrderBy() throws Exception {
		// order by DEPT_NO
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("orderExpr", "DEPT_NO");

		// select
		List<Map<?, ?>> resultList = mapTypeDAO.selectDeptList("selectUsingReplacedOrderBy", map);

		// check
		assertNotNull(resultList);
		// order by DEPT_NO 에 의한 결과
		assertEquals(4, resultList.size());
		assertEquals(new BigDecimal(10), resultList.get(0).get("deptNo"));
		assertEquals(new BigDecimal(20), resultList.get(1).get("deptNo"));
		assertEquals(new BigDecimal(30), resultList.get(2).get("deptNo"));
		assertEquals(new BigDecimal(40), resultList.get(3).get("deptNo"));

		// order by DEPT_NAME ASC
		map.clear();
		map.put("orderExpr", "DEPT_NAME ASC");

		// select
		resultList = mapTypeDAO.selectDeptList("selectUsingReplacedOrderBy", map);

		// check
		assertNotNull(resultList);
		// order by DEPT_NAME ASC 에 의한 결과
		assertEquals(4, resultList.size());
		assertEquals("ACCOUNTING", resultList.get(0).get("deptName"));
		assertEquals("OPERATIONS", resultList.get(1).get("deptName"));
		assertEquals("RESEARCH", resultList.get(2).get("deptName"));
		assertEquals("SALES", resultList.get(3).get("deptName"));

		map.clear();
		StringBuilder complexExpr = new StringBuilder();
		complexExpr.append(" ( select max(EMP_NO) ");
		complexExpr.append("   from   EMP B ");
		complexExpr.append("   where  DEPT.DEPT_NO = B.DEPT_NO ) ");
		// cf.) hsql 1.8.0.10 의 경우 null 인 값이 먼저 조회되고
		// 있으며, nulls first/last 표현식을 지원하지 않음
		if (isOracle || isTibero) {
			complexExpr.append(" nulls first ");
		}
		// order by complex expression
		// order by 절 내에서 메인 쿼리와 join 에 의한 각 DEPT_NO 별
		// max(EMP_NO) 의 순서
		// DEPT_NO 40 null
		// DEPT_NO 30 max(EMP_NO) 7900
		// DEPT_NO 20 max(EMP_NO) 7902
		// DEPT_NO 10 max(EMP_NO) 7934
		map.put("orderExpr", complexExpr.toString());

		// select
		resultList = mapTypeDAO.selectDeptList("selectUsingReplacedOrderBy", map);

		// check
		assertNotNull(resultList);
		// order by DEPT_NAME ASC 에 의한 결과
		assertEquals(4, resultList.size());
		assertEquals(new BigDecimal(40), resultList.get(0).get("deptNo"));
		assertEquals(new BigDecimal(30), resultList.get(1).get("deptNo"));
		assertEquals(new BigDecimal(20), resultList.get(2).get("deptNo"));
		assertEquals(new BigDecimal(10), resultList.get(3).get("deptNo"));

		// order by complex expression
		// order by 절 내에서 메인 쿼리와 join 에 의한 각 DEPT_NO 별
		// max(EMP_NO) 의 순서
		// DEPT_NO 40 null
		// DEPT_NO 20 min(EMP_NO) 7369
		// DEPT_NO 30 min(EMP_NO) 7499
		// DEPT_NO 10 min(EMP_NO) 7782
		map.put("orderExpr", complexExpr.toString().replaceAll("max", "min"));

		// select
		resultList = mapTypeDAO.selectDeptList("selectUsingReplacedOrderBy", map);

		// check
		assertNotNull(resultList);
		// order by DEPT_NAME ASC 에 의한 결과
		assertEquals(4, resultList.size());
		assertEquals(new BigDecimal(40), resultList.get(0).get("deptNo"));
		assertEquals(new BigDecimal(20), resultList.get(1).get("deptNo"));
		assertEquals(new BigDecimal(30), resultList.get(2).get("deptNo"));
		assertEquals(new BigDecimal(10), resultList.get(3).get("deptNo"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testReplaceTextTable() throws Exception {

		// from DEPT
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("table", "DEPT");

		// select

		List<Map<String, Object>> resultList = mapTypeDAO.getSqlMapClientTemplate().queryForList("selectUsingReplacedTable", map);

		// check
		assertNotNull(resultList);
		// select * from $table$ 에 의한 결과
		assertEquals(4, resultList.size());
		assertTrue(resultList.get(0).containsKey("deptNo"));

		// from DEPT .. order by DEPT_NO
		map.clear();
		map.put("table", "DEPT");
		map.put("orderExpr", "DEPT_NO");

		// select
		resultList = mapTypeDAO.getSqlMapClientTemplate().queryForList("selectUsingReplacedTable", map);

		// check
		assertNotNull(resultList);
		// select * from $table$ order by $orderExpr$ 에
		// 의한 결과
		assertEquals(4, resultList.size());
		assertEquals(new BigDecimal(10), resultList.get(0).get("deptNo"));
		assertEquals(new BigDecimal(20), resultList.get(1).get("deptNo"));
		assertEquals(new BigDecimal(30), resultList.get(2).get("deptNo"));
		assertEquals(new BigDecimal(40), resultList.get(3).get("deptNo"));

		map.clear();
		map.put("table", "EMP");
		map.put("orderExpr", "EMP_NO");
		// order by 1 --> EMP_NO
		// 1 7369
		// 2 7499
		// 3 7521
		// 4 7566
		// 5 7654
		// 6 7698
		// 7 7782
		// 8 7788
		// 9 7839
		// 10 7844
		// 11 7876
		// 12 7900
		// 13 7902
		// 14 7934

		// select
		resultList = mapTypeDAO.getSqlMapClientTemplate().queryForList("selectUsingReplacedTable", map);

		// check
		assertNotNull(resultList);
		// select * from $table$ order by $orderExpr$ 에
		// 의한 결과
		assertEquals(14, resultList.size());
		assertEquals(new BigDecimal(7369), resultList.get(0).get("empNo"));
		assertEquals(new BigDecimal(7499), resultList.get(1).get("empNo"));
		assertEquals(new BigDecimal(7521), resultList.get(2).get("empNo"));
		assertEquals(new BigDecimal(7566), resultList.get(3).get("empNo"));
		assertEquals(new BigDecimal(7654), resultList.get(4).get("empNo"));
		assertEquals(new BigDecimal(7698), resultList.get(5).get("empNo"));
		assertEquals(new BigDecimal(7782), resultList.get(6).get("empNo"));
		assertEquals(new BigDecimal(7788), resultList.get(7).get("empNo"));
		assertEquals(new BigDecimal(7839), resultList.get(8).get("empNo"));
		assertEquals(new BigDecimal(7844), resultList.get(9).get("empNo"));
		assertEquals(new BigDecimal(7876), resultList.get(10).get("empNo"));
		assertEquals(new BigDecimal(7900), resultList.get(11).get("empNo"));
		assertEquals(new BigDecimal(7902), resultList.get(12).get("empNo"));
		assertEquals(new BigDecimal(7934), resultList.get(13).get("empNo"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testReplaceTextAllQuery() throws Exception {

		// selectQuery
		Map<String, Object> map = new HashMap<String, Object>();
		StringBuilder selectQuery = new StringBuilder();
		selectQuery.append("select * from DEPT");

		map.put("selectQuery", selectQuery.toString());

		// select
		List<Map<String, Object>> resultList = mapTypeDAO.getSqlMapClientTemplate().queryForList("selectUsingReplacedAllQuery", map);

		assertNotNull(resultList);
		assertEquals(4, resultList.size());
		assertTrue(resultList.get(0).containsKey("deptNo"));

		map.clear();
		selectQuery = new StringBuilder();
		selectQuery.append("select * from DEPT ");
		selectQuery.append("where DEPT_NAME like '%ES%' ");
		selectQuery.append("order by DEPT_NO DESC ");

		map.put("selectQuery", selectQuery.toString());

		// select
		resultList = mapTypeDAO.getSqlMapClientTemplate().queryForList("selectUsingReplacedAllQuery", map);

		assertNotNull(resultList);
		// 20,'RESEARCH','DALLAS' -- R'ES'EARCH
		// 30,'SALES','CHICAGO' -- SAL'ES'
		assertEquals(2, resultList.size());
		assertTrue(resultList.get(0).containsKey("deptNo"));
	}
}
