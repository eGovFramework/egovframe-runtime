package org.egovframe.rte.psl.dataaccess.mybatis;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.dao.EmpMapper;
import org.egovframe.rte.psl.dataaccess.dao.JobHistMapper;
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
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-*.xml" })
@Transactional
public class DynamicSQLMapperTest extends TestBase {

	@Resource(name = "jobHistMapper")
	JobHistMapper jobHistMapper;

	@Resource(name = "empMapper")
	EmpMapper empMapper;

	@Before
	public void onSetUp() throws Exception {
		// 외부에 sql file 로부터 DB 초기화 (기존 테이블 삭제/생성 및
		// 초기데이터 구축)
		// Spring 의 JdbcTestUtils 사용,
		// continueOnError 플래그는 true로 설정 - cf.) DDL 이
		// 포함된 경우 rollback 에 유의
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"));

		// init data
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_initdata_" + usingDBMS + ".sql"));

		// hsql 인 경우 테스트를 위해 dual 준비
		if (isHsql) {
			ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_hsql_dual.sql"));
		}
	}

	@Rollback(false)
	@Test
	public void testDynamicStatement() throws Exception {
		JobHistVO vo = new JobHistVO();
		// 입력 파라메터 객체의 property 에 따른 Dynamic 테스트
		vo.setEmpNo(new BigDecimal(7788));

		// select
		List<JobHistVO> resultList = jobHistMapper.selectJobHistList("selectJobHistListUsingDynamicElement", vo);

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
		resultList = jobHistMapper.selectJobHistList("selectJobHistListUsingDynamicElement", vo);

		// check
		assertNotNull(resultList);
		// where 이 수행되지 않아 전체 데이터가 조회될 것임
		assertEquals(17, resultList.size());

	}

	@SuppressWarnings("unchecked")
	@Rollback(false)
	@Test
	public void testDynamicUnary() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		// 입력 파라메터 객체의 property 에 따른 Dynamic 테스트
		// isEmpty 테스트 - String
		map.put("testEmptyString", "");
		// isEmpty 테스트 - Collection
		List<Object> list = new ArrayList<Object>();
		map.put("testEmptyCollection", list);
		// isNull 테스트
		map.put("testNull", null);
		// isPropertyAvailable 테스트 - cf.) property 의 값을
		// null 로 설정하더라도 해당 property 는 Available 한것에
		// 유의!
		map.put("testProperty", null);

		// select
		Map<String, Object> resultMap = (Map<String, Object>) jobHistMapper.selectOne("selectDynamicUnary", map);

		// check
		assertNotNull(resultMap);
		assertEquals("empty String", resultMap.get("isEmptyString"));
		assertEquals("empty Collection", resultMap.get("isEmptyCollection"));
		assertEquals("null", resultMap.get("isNull"));
		/* MyBatis에는 isPropertyAvailable이 없어서 삭제함
		assertEquals("testProperty Available", resultMap
		    .get("testPropertyAvailable"));
		*/

		// 입력 파라메터 객체의 property 에 따른 Dynamic 테스트 2
		// isEmpty 테스트 - String - null 인 경우도 isEmpty 는
		// 만족함
		map.put("testEmptyString", null);
		// isEmpty 테스트 - Collection - null 인 경우도
		// isEmpty 는 만족함
		List<Object> nullList = null;
		map.put("testEmptyCollection", nullList);

		// select
		resultMap = (Map<String, Object>) jobHistMapper.selectOne("selectDynamicUnary", map);

		// check
		assertNotNull(resultMap);
		assertEquals("empty String", resultMap.get("isEmptyString"));
		assertEquals("empty Collection", resultMap.get("isEmptyCollection"));

		// 입력 파라메터 객체의 property 에 따른 Dynamic 테스트 3
		map.clear();
		// isEmpty 테스트 - String
		map.put("testEmptyString", "aa");
		// isEmpty 테스트 - Collection
		list.clear();
		list.add("aa");
		map.put("testEmptyCollection", list);
		// isNull 테스트
		map.put("testNull", new BigDecimal(0));
		// isPropertyAvailable 테스트 - key 자체를 담지 않았을 때
		// isNotPropertyAvailable 임
		// map.put("testProperty", null);

		// select
		resultMap = (Map<String, Object>) jobHistMapper.selectOne("selectDynamicUnary", map);

		// check
		assertNotNull(resultMap);
		assertEquals("not empty String", resultMap.get("isEmptyString"));
		assertEquals("not empty Collection", resultMap.get("isEmptyCollection"));
		assertEquals("not null", resultMap.get("isNull"));

		/* MyBatis에는 isPropertyAvailable이 없어서 삭제함
		assertEquals("testProperty Not Available", resultMap
		    .get("testPropertyAvailable"));
		*/

	}

	@SuppressWarnings("unchecked")
	@Rollback(false)
	@Test
	public void testDynamicBinary() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String castTypeScale = "numeric(2)";
		// oracle 인 경우 - numeric 에 대응되는 type 은 number
		if (isOracle) {
			castTypeScale = "number(2)";
		} else if (isMysql) {
			castTypeScale = "decimal(2)";
		}

		// 입력 파라메터 객체의 property 에 따른 Dynamic 테스트
		// isEqual 테스트 - String
		map.put("testString", "test");
		// isEqual 테스트 - BigDecimal
		map.put("testNumeric", new BigDecimal(10));
		// dual 임시 테이블 상에 상수 조회시 numeric(db) -
		// decimal(java) 처리를 위해 cast 처리 추가
		map.put("castTypeScale", castTypeScale);

		// select
		Map<String, Object> resultMap = (Map<String, Object>) jobHistMapper.selectOne("selectDynamicBinary", map);

		// check
		assertNotNull(resultMap);
		assertEquals("test", resultMap.get("testString"));
		assertEquals("test : equals", resultMap.get("isEqual"));
		assertEquals(new BigDecimal(10), resultMap.get("testNumeric"));
		assertEquals("10 : equals", resultMap.get("isEqualNumeric"));
		assertEquals("10 <= 10", resultMap.get("isGreaterEqual"));
		assertTrue(!resultMap.containsKey("isGreaterThan"));
		assertEquals("10 >= 10", resultMap.get("isLessEqual"));
		assertTrue(!resultMap.containsKey("isLessThan"));

		// 입력 파라메터 객체의 property 에 따른 Dynamic 테스트 2
		map.clear();

		// isEqual 테스트 - String
		map.put("testString", "not test");
		// isEqual 테스트 - BigDecimal
		map.put("testNumeric", new BigDecimal(11));
		// dual 임시 테이블 상에 상수 조회시 numeric(db) -
		// decimal(java) 처리를 위해 cast 처리 추가
		map.put("castTypeScale", castTypeScale);

		// select
		resultMap = (Map<String, Object>) jobHistMapper.selectOne("selectDynamicBinary", map);

		// check
		assertNotNull(resultMap);
		assertEquals("not test", resultMap.get("testString"));
		assertEquals("test : not equals", resultMap.get("isEqual"));
		assertEquals(new BigDecimal(11), resultMap.get("testNumeric"));
		assertEquals("10 : not equals", resultMap.get("isEqualNumeric"));
		assertEquals("10 <= 11", resultMap.get("isGreaterEqual"));
		assertEquals("10 < 11", resultMap.get("isGreaterThan"));
		assertTrue(!resultMap.containsKey("isLessEqual"));
		assertTrue(!resultMap.containsKey("isLessThan"));

		// 입력 파라메터 객체의 property 에 따른 Dynamic 테스트 2
		map.clear();

		// isEqual 테스트 - String
		// isEqual 비교 대상 property 에 null 값을 넘기면 에러는
		// 발생하지 않고, isNotEqual 과 매칭됨
		map.put("testString", null);
		// isEqual 테스트 - BigDecimal
		map.put("testNumeric", new BigDecimal(9));
		// dual 임시 테이블 상에 상수 조회시 numeric(db) -
		// decimal(java) 처리를 위해 cast 처리 추가
		map.put("castTypeScale", castTypeScale);

		// select
		resultMap = (Map<String, Object>) jobHistMapper.selectOne("selectDynamicBinary", map);

		// check
		assertNotNull(resultMap);
		// oracle 인 경우 '' 는 null 과 같고 결과 객체에는 null 로
		// 맵핑됨
		assertEquals(!(isOracle || isTibero) ? "" : null, resultMap.get("testString"));
		assertEquals("test : not equals", resultMap.get("isEqual"));
		assertEquals(new BigDecimal(9), resultMap.get("testNumeric"));
		assertEquals("10 : not equals", resultMap.get("isEqualNumeric"));
		assertTrue(!resultMap.containsKey("isGreaterEqual"));
		assertTrue(!resultMap.containsKey("isGreaterThan"));
		assertEquals("10 >= 9", resultMap.get("isLessEqual"));
		assertEquals("10 > 9", resultMap.get("isLessThan"));

		// 입력 파라메터 객체의 property 에 따른 Dynamic 테스트 3
		map.clear();

		map.put("testString", "test");
		// isEqual 테스트 - BigDecimal
		map.put("testOtherString", "test");

		// select
		resultMap = (Map<String, Object>) jobHistMapper.selectOne("selectDynamicBinary", map);

		// check
		assertNotNull(resultMap);

		assertEquals("test : equals", resultMap.get("isEqual"));
		// testNumeric property 를 넘기지 않았을 때 기대 결과

		/* 숫자와 공백을 비교하는 것은 의미없으므로 삭제
		//assertTrue(!resultMap.containsKey("isGreaterEqual"));
		//assertTrue(!resultMap.containsKey("isGreaterThan"));
		//assertTrue(!resultMap.containsKey("isLessEqual"));
		//assertTrue(!resultMap.containsKey("isLessThan"));
		 *
		 */
		// testOtherString 비교
		assertEquals("test", resultMap.get("testOtherString"));
		assertEquals("test : testOtherString not equals testString", resultMap.get("comparePropertyEqual"));

		/* 문자열 비교는 하지않으므로 삭제함
		assertEquals("'test' >= 'test'", resultMap
		    .get("comparePropertyGreaterEqual"));
		assertTrue(!resultMap.containsKey("comparePropertyGreaterThan"));
		assertEquals("'test' <= 'test'", resultMap
		    .get("comparePropertyLessEqual"));
		assertTrue(!resultMap.containsKey("comparePropertyLessThan"));
		*/

		// 입력 파라메터 객체의 property 에 따른 Dynamic 테스트 4
		map.clear();

		map.put("testString", "test");
		// 'test' >= 'sample' 테스트
		map.put("testOtherString", "sample");

		// select
		resultMap = (Map<String, Object>) jobHistMapper.selectOne("selectDynamicBinary", map);

		// check
		assertNotNull(resultMap);

		assertEquals("test : equals", resultMap.get("isEqual"));
		// testNumeric property 를 넘기지 않았을 때 기대 결과
		/* 숫자와 공백을 비교하는 것은 의미없으므로 삭제
		assertTrue(!resultMap.containsKey("isGreaterEqual"));
		assertTrue(!resultMap.containsKey("isGreaterThan"));
		assertTrue(!resultMap.containsKey("isLessEqual"));
		assertTrue(!resultMap.containsKey("isLessThan"));
		*/
		// testOtherString 비교
		assertEquals("sample", resultMap.get("testOtherString"));
		assertEquals("test : testOtherString not equals testString", resultMap.get("comparePropertyEqual"));
		assertTrue(!resultMap.containsKey("comparePropertyGreaterEqual"));
		assertTrue(!resultMap.containsKey("comparePropertyGreaterThan"));
		/* 문자열 비교는 하지않으므로 삭제함
		assertEquals("'sample' <= 'test'", resultMap
		    .get("comparePropertyLessEqual"));
		assertEquals("'sample' < 'test'", resultMap
		    .get("comparePropertyLessThan"));
		*/

		// 입력 파라메터 객체의 property 에 따른 Dynamic 테스트 5
		map.clear();

		map.put("testString", "test");
		// 'test' <= 'testa' 테스트
		map.put("testOtherString", "testa");

		// select
		resultMap = (Map<String, Object>) jobHistMapper.selectOne("selectDynamicBinary", map);

		// check
		assertNotNull(resultMap);

		assertEquals("test : equals", resultMap.get("isEqual"));
		// testNumeric property 를 넘기지 않았을 때 기대 결과
		/* 숫자와 공백을 비교하는 것은 의미없으므로 삭제
		assertTrue(!resultMap.containsKey("isGreaterEqual"));
		assertTrue(!resultMap.containsKey("isGreaterThan"));
		assertTrue(!resultMap.containsKey("isLessEqual"));
		assertTrue(!resultMap.containsKey("isLessThan"));
		*/
		// testOtherString 비교
		assertEquals("testa", resultMap.get("testOtherString"));
		assertEquals("test : testOtherString not equals testString", resultMap.get("comparePropertyEqual"));
		/* 문자열 비교는 하지않으므로 삭제함
		assertEquals("'testa' >= 'test'", resultMap
		    .get("comparePropertyGreaterEqual"));
		assertEquals("'testa' > 'test'", resultMap
		    .get("comparePropertyGreaterThan"));
		*/
		assertTrue(!resultMap.containsKey("comparePropertyLessEqual"));
		assertTrue(!resultMap.containsKey("comparePropertyLessThan"));

	}

	@SuppressWarnings("unchecked")
	@Rollback(false)
	@Test
	public void testDynamicParameterPresent() throws Exception {

		// 입력 파라메터 객체의 전달 여부에 따른 Dynamic 테스트
		// isParameterPresent 테스트
		Map<String, Object> map = new HashMap<String, Object>();

		// select
		Map<String, Object> resultMap = (Map<String, Object>) jobHistMapper.selectOne("selectDynamicParameterPresent", map);

		// check
		assertNotNull(resultMap);
		assertEquals("parameter object exist", resultMap.get("isParameterPresent"));

		map = null;

		// select
		resultMap = (Map<String, Object>) jobHistMapper.selectOne("selectDynamicParameterPresent", map);

		// check
		assertNotNull(resultMap);
		assertEquals("parameter object not exist", resultMap.get("isParameterPresent"));
	}

	@Rollback(false)
	@Test
	public void testDynamicIterate() throws Exception {
		try {

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
			EmpIncludesEmpListVO resultVO = empMapper.selectEmpIncludesEmpList("selectEmpIncludesSameMgrMoreSalaryEmpList", vo);

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
			List<JobHistVO> resultList = jobHistMapper.selectList("selectJobHistListUsingDynamicIterate", resultVO);

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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Rollback(false)
	@Test
	public void testDynamicIterateSimple() throws Exception {
		// Collection 형의 객체 size 만큼
		List<Object> iterateList = new ArrayList<Object>();
		iterateList.add("a");
		iterateList.add("b");
		iterateList.add("c");

		Map<String, Object> map = new HashMap<String, Object>();

		// select
		Map<String, Object> resultMap = (Map<String, Object>) jobHistMapper.selectOne("selectDynamicIterateSimple1", iterateList);

		// check
		assertNotNull(resultMap);
		assertEquals("a", resultMap.get("a"));
		assertEquals("b", resultMap.get("b"));
		assertEquals("c", resultMap.get("c"));
		assertTrue(!resultMap.containsKey("d"));

		// map 안에 collection 이란 property 로 List 를 넣은 경우
		map.clear();
		map.put("collection", iterateList);

		// select
		resultMap = (Map<String, Object>) jobHistMapper.selectOne("selectDynamicIterateSimple2", map);

		// check
		assertNotNull(resultMap);
		assertEquals("a", resultMap.get("a"));
		assertEquals("b", resultMap.get("b"));
		assertEquals("c", resultMap.get("c"));
		assertTrue(!resultMap.containsKey("d"));

		// iterate 를 위한 테스트로 Map, Set, Iterator 를 시도해
		// 보았으나 아래 에러를 냄. (List 나 Array 와 같이 index 로 접근
		// 가능해야 하는듯)
		// The 'xxx'(ex. collection) property of the
		// XXX (ex. java.util.HashMap$EntryIterator)
		// class is not a List or
		// Array.
	}

	@Rollback(false)
	@Test
	public void testDynamicNestedIterate() throws Exception {
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
		columnMap1.put("nested", "false");
		condition.add(columnMap1);

		Map<String, Object> columnMap2 = new HashMap<String, Object>();
		columnMap2.put("columnName", "SAL");
		columnMap2.put("columnOperation", "<");
		columnMap2.put("columnValue", new BigDecimal(3000));
		columnMap2.put("nested", "false");
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
		List<JobHistVO> resultList = jobHistMapper.selectList("selectJobHistListUsingDynamicNestedIterate", map);

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
