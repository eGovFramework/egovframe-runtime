package org.egovframe.rte.psl.dataaccess.ibatis;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.dao.EmpDAO;
import org.egovframe.rte.psl.dataaccess.dao.JobHistDAO;
import org.egovframe.rte.psl.dataaccess.vo.EmpIncludesEmpListVO;
import org.egovframe.rte.psl.dataaccess.vo.EmpVO;
import org.egovframe.rte.psl.dataaccess.vo.JobHistVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

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
@SuppressWarnings("deprecation")
public class ReuseSQLTest extends TestBase {

	@Resource(name = "jobHistDAO")
	JobHistDAO jobHistDAO;

	@Resource(name = "empDAO")
	EmpDAO empDAO;

	@Before
	public void onSetUp() throws Exception {
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"));

		// init data
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_initdata_" + usingDBMS + ".sql"));
	}

	@Rollback(false)
	@Test
	public void testIncludeWithDynamicStatement() throws Exception {
		JobHistVO vo = new JobHistVO();
		// 입력 파라메터 객체의 property 에 따른 Dynamic 테스트
		vo.setEmpNo(new BigDecimal(7788));

		// select
		List<JobHistVO> resultList = jobHistDAO.selectJobHistList("selectJobHistListUsingIncludeA", vo);

		// check
		assertNotNull(resultList);
		assertEquals(3, resultList.size());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
		assertEquals(sdf.parse("1987-04-19"), resultList.get(0).getStartDate());
		assertEquals(sdf.parse("1988-04-13"), resultList.get(1).getStartDate());
		assertEquals(sdf.parse("1990-05-05"), resultList.get(2).getStartDate());

		// 입력 파라메터 객체의 property 에 따른 Dynamic 테스트
		vo.setEmpNo(null);

		// select
		resultList = jobHistDAO.selectJobHistList("selectJobHistListUsingDynamicElement", vo);

		// check
		assertNotNull(resultList);
		// where 이 수행되지 않아 전체 데이터가 조회될 것임
		assertEquals(17, resultList.size());

	}

	@SuppressWarnings("unchecked")
	@Rollback(false)
	@Test
	public void testIncludeWithDynamicIterate() throws Exception {
		// CompositeKeyTest.testCompositeKeySelect() 참조
		EmpVO vo = new EmpVO();
		// 7521,'WARD','SALESMAN',7698,'1981-02-22',1250,500,30
		// --> mgr 이 7698 인 EMP
		// 7499,'ALLEN','SALESMAN',7698,'1981-02-20',1600
		// --> O
		// 7654,'MARTIN','SALESMAN',7698,'1981-09-28',1250
		// --> O
		// 7844,'TURNER','SALESMAN',7698,'1981-09-08',1500
		// --> O
		// 7900,'JAMES','CLERK',7698,'1981-12-03',950
		// --> X
		vo.setEmpNo(new BigDecimal(7521));

		// select
		EmpIncludesEmpListVO resultVO = empDAO.selectEmpIncludesEmpList("selectEmpIncludesSameMgrMoreSalaryEmpList", vo);

		// check
		assertNotNull(resultVO);
		assertEquals(new BigDecimal(7521), resultVO.getEmpNo());
		assertEquals("WARD", resultVO.getEmpName());
		assertTrue(resultVO.getEmpList() instanceof List);
		assertEquals(3, resultVO.getEmpList().size());
		assertEquals(new BigDecimal(7499), resultVO.getEmpList().get(0).getEmpNo());
		assertEquals(new BigDecimal(1600), resultVO.getEmpList().get(0).getSal());
		assertEquals(new BigDecimal(7844), resultVO.getEmpList().get(1).getEmpNo());
		assertEquals(new BigDecimal(1500), resultVO.getEmpList().get(1).getSal());
		assertEquals(new BigDecimal(7654), resultVO.getEmpList().get(2).getEmpNo());
		assertEquals(new BigDecimal(1250), resultVO.getEmpList().get(2).getSal());

		// select
		List<JobHistVO> resultList = jobHistDAO.getSqlMapClientTemplate().queryForList("selectJobHistListUsingIncludeB", resultVO);

		assertNotNull(resultList);
		// 7499, 7654, 7844 의 jobhist 는 초기데이터에 따라 각 1건
		// 임
		assertEquals(3, resultList.size());

		assertEquals(new BigDecimal(7499), resultList.get(0).getEmpNo());
		assertEquals(new BigDecimal(7654), resultList.get(1).getEmpNo());
		assertEquals(new BigDecimal(7844), resultList.get(2).getEmpNo());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
		assertEquals(sdf.parse("1981-02-20"), resultList.get(0).getStartDate());
		assertEquals(sdf.parse("1981-09-28"), resultList.get(1).getStartDate());
		assertEquals(sdf.parse("1981-09-08"), resultList.get(2).getStartDate());

	}

	@SuppressWarnings("unchecked")
	@Rollback(false)
	@Test
	public void testIncludeWithDynamicNestedIterate() throws Exception {
		// nested iterate 태그 테스트 - Map 안에 condition 이란
		// key 로 columnName, columnOperation,
		// columnValue 를 Map 형태로 모아 담고
		// columnValue 가 nested iterate 로 풀려야 하는 경우(ex.
		// in 조건절) nested 'true' 로 추가 설정을 하여 호출함.
		Map<String, Object> map = new HashMap<String, Object>();
		List<Object> condition = new ArrayList<Object>();
		Map<String, Object> columnMap1 = new HashMap<String, Object>();
		columnMap1.put("columnName", "DEPT_NO");
		columnMap1.put("columnOperation", "=");
		columnMap1.put("columnValue", new BigDecimal(30));
		condition.add(columnMap1);

		Map<String, Object> columnMap2 = new HashMap<String, Object>();
		columnMap2.put("columnName", "SAL");
		columnMap2.put("columnOperation", "<");
		columnMap2.put("columnValue", new BigDecimal(3000));
		condition.add(columnMap2);

		Map<String, Object> columnMap3 = new HashMap<String, Object>();
		columnMap3.put("columnName", "JOB");
		columnMap3.put("columnOperation", "in");
		List<Object> jobList = new ArrayList<Object>();
		jobList.add("CLERK");
		jobList.add("SALESMAN");
		columnMap3.put("columnValue", jobList);
		// List 를 nested 로 포함하고 있음을 flag 로 알림
		columnMap3.put("nested", "true");
		condition.add(columnMap3);

		map.put("condition", condition);

		// select
		List<JobHistVO> resultList = jobHistDAO.getSqlMapClientTemplate().queryForList("selectJobHistListUsingIncludeC", map);

		// check
		assertNotNull(resultList);

		// 결과 데이터
		// Empno Startdate Enddate Job Sal Comm Deptno
		// 1 7499 81/02/20 SALESMAN 1600 300 30
		// 2 7521 81/02/22 SALESMAN 1250 500 30
		// 3 7654 81/09/28 SALESMAN 1250 1400 30
		// cf.) 7698 81/05/01 MANAGER 2850 30 데이터는 in
		// 조건절에 JOB 이 'MANAGER' 인 것이 없기 때문에 nested 안에서
		// 필터링 됨.
		// 4 7844 81/09/08 SALESMAN 1500 0 30
		// 5 7900 83/01/15 CLERK 950 30
		assertEquals(5, resultList.size());
		assertEquals(new BigDecimal(7499), resultList.get(0).getEmpNo());
		assertEquals(new BigDecimal(7521), resultList.get(1).getEmpNo());
		assertEquals(new BigDecimal(7654), resultList.get(2).getEmpNo());
		assertEquals(new BigDecimal(7844), resultList.get(3).getEmpNo());
		assertEquals(new BigDecimal(7900), resultList.get(4).getEmpNo());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
		assertEquals(sdf.parse("1981-02-20"), resultList.get(0).getStartDate());
		assertEquals(sdf.parse("1981-02-22"), resultList.get(1).getStartDate());
		assertEquals(sdf.parse("1981-09-28"), resultList.get(2).getStartDate());
		assertEquals(sdf.parse("1981-09-08"), resultList.get(3).getStartDate());
		assertEquals(sdf.parse("1983-01-15"), resultList.get(4).getStartDate());

	}
}
