package egovframework.rte.psl.dataaccess.ibatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import egovframework.rte.psl.dataaccess.TestBase;
import egovframework.rte.psl.dataaccess.dao.DeptDAO;
import egovframework.rte.psl.dataaccess.vo.DeptVO;

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
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-*.xml" })
@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class BasicDataAccessTest extends TestBase {

	@Resource(name = "deptDAO")
	private DeptDAO deptDAO;

	@Before
	public void onSetUp() throws Exception {
		// 외부에 sql file 로부터 DB 초기화 (기존 테이블 삭제/생성 및
		// 초기데이터 구축)
		// Spring 의 JdbcTestUtils 사용,
		// continueOnError 플래그는 true로 설정 - cf.) DDL 이
		// 포함된 경우 rollback 에 유의
		JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"), true);
	}

	public DeptVO makeVO() {
		DeptVO vo = new DeptVO();
		vo.setDeptNo(new BigDecimal(90));
		vo.setDeptName("test 부서");
		vo.setLoc("test 위치");
		return vo;
	}

	public void checkResult(DeptVO vo, DeptVO resultVO) {
		assertNotNull(resultVO);
		assertEquals(vo.getDeptNo(), resultVO.getDeptNo());
		assertEquals(vo.getDeptName(), resultVO.getDeptName());
		assertEquals(vo.getLoc(), resultVO.getLoc());
	}

	@Test
	public void testBasicInsert() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptDAO.insertDept("insertDept", vo);
		// deptDAO.insertDept("Dept.insertDept", vo);
		// // cf. useStatementNamespaces="true" 일 때

		// select
		DeptVO resultVO = deptDAO.selectDept("selectDept", vo);

		// check
		checkResult(vo, resultVO);
	}

	@Test
	public void testBasicUpdate() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptDAO.insertDept("insertDept", vo);

		// data change
		vo.setDeptName("upd Dept");
		vo.setLoc("upd loc");

		// update
		int effectedRows = deptDAO.updateDept("updateDept", vo);
		assertEquals(1, effectedRows);

		// select
		DeptVO resultVO = deptDAO.selectDept("selectDept", vo);

		// check
		checkResult(vo, resultVO);
	}

	@Test
	public void testBasicDelete() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptDAO.insertDept("insertDept", vo);

		// delete
		int effectedRows = deptDAO.deleteDept("deleteDept", vo);
		assertEquals(1, effectedRows);

		// select
		DeptVO resultVO = deptDAO.selectDept("selectDept", vo);

		// null 이어야 함
		assertNull(resultVO);
	}

	@Test
	public void testBasicSelectList() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptDAO.insertDept("insertDept", vo);

		// 검색조건으로 key 설정
		DeptVO searchVO = new DeptVO();
		searchVO.setDeptNo(new BigDecimal(90));

		// selectList
		List<DeptVO> resultList = deptDAO.selectDeptList(isMysql ? "selectDeptListMysql" : "selectDeptList", searchVO);

		// key 조건에 대한 결과는 한건일 것임
		assertNotNull(resultList);
		assertTrue(resultList.size() > 0);
		assertEquals(1, resultList.size());
		// assertTrue(resultList.get(0) instanceof
		// DeptVO);
		checkResult(vo, resultList.get(0));

		// 검색조건으로 name 설정 - '%' || #deptName# || '%'
		DeptVO searchVO2 = new DeptVO();
		searchVO2.setDeptName(""); // '%' || '' || '%'
									// --> '%%'

		// selectList
		List<DeptVO> resultList2 = deptDAO.selectDeptList(isMysql ? "selectDeptListMysql" : "selectDeptList", searchVO2);

		// like 조건에 대한 결과는 한건 이상일 것임
		assertNotNull(resultList2);
		assertTrue(resultList2.size() > 0);

	}

	@Test
	public void testInsertUsingParameterMap() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptDAO.insertDept("insertDeptUsingParameterMap", vo);

		// select
		DeptVO resultVO = deptDAO.selectDept("selectDept", vo);

		// check
		checkResult(vo, resultVO);
	}

	@Test
	public void testInsertAndSelectUsingParameterClass() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptDAO.insertDept("insertDeptUsingParameterClass", vo);

		// select
		DeptVO resultVO = deptDAO.selectDept("selectDept", vo);

		// check
		checkResult(vo, resultVO);
	}

	@Test
	public void testInsertUsingInLineParamWithDBType() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptDAO.insertDept("insertDeptUsingInLineParamWithDBType", vo);

		// select
		DeptVO resultVO = deptDAO.selectDept("selectDept", vo);

		// check
		checkResult(vo, resultVO);
	}

	@Test
	public void testInsertAndSelectUsingResultClass() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptDAO.insertDept("insertDeptUsingParameterClass", vo);

		// select
		// resultClass 를 VO로 직접 명시하는 경우는 VO에 정의된
		// attribute 변수명으로 select 시 column alias 필요
		DeptVO resultVO = deptDAO.selectDept("selectDeptUsingResultClass", vo);

		// check
		checkResult(vo, resultVO);
	}

	@Test
	public void testSelectListWithPaging() throws Exception {
		DeptVO vo = makeVO();

		vo.setDeptNo(new BigDecimal(10));
		vo.setDeptName("부서 10");
		deptDAO.insertDept("insertDept", vo);

		vo.setDeptNo(new BigDecimal(11));
		vo.setDeptName("부서 11");
		deptDAO.insertDept("insertDept", vo);

		vo.setDeptNo(new BigDecimal(12));
		vo.setDeptName("부서 12");
		deptDAO.insertDept("insertDept", vo);

		vo.setDeptNo(new BigDecimal(13));
		vo.setDeptName("부서 13");
		deptDAO.insertDept("insertDept", vo);

		vo.setDeptNo(new BigDecimal(14));
		vo.setDeptName("부서 14");
		deptDAO.insertDept("insertDept", vo);

		// 검색조건으로 name 설정 - '%' || #deptName# || '%'
		DeptVO searchVO = new DeptVO();
		searchVO.setDeptName(""); // '%' || '' || '%' --> '%%'

		// selectList
		List<DeptVO> resultList = deptDAO.selectDeptListWithPaging(isMysql ? "selectDeptListMysql" : "selectDeptList", searchVO, 0, 2);

		// like 조건에 대한 결과는 한건 이상일 것임
		assertNotNull(resultList);
		assertTrue(resultList.size() == 2);
		assertEquals(resultList.get(0).getDeptNo(), new BigDecimal(10));
		assertEquals(resultList.get(1).getDeptNo(), new BigDecimal(11));

		// selectList
		List<DeptVO> resultList2 = deptDAO.selectDeptListWithPaging(isMysql ? "selectDeptListMysql" : "selectDeptList", searchVO, 1, 2);

		// like 조건에 대한 결과는 한건 이상일 것임
		assertNotNull(resultList2);
		assertTrue(resultList2.size() == 2);
		assertEquals(resultList2.get(0).getDeptNo(), new BigDecimal(12));

		// selectList
		List<DeptVO> resultList3 = deptDAO.selectDeptListWithPaging(isMysql ? "selectDeptListMysql" : "selectDeptList", searchVO, 2, 2);

		// like 조건에 대한 결과는 한건 이상일 것임
		assertNotNull(resultList3);
		assertTrue(resultList3.size() == 1);
		assertEquals(resultList3.get(0).getDeptNo(), new BigDecimal(14));

	}

}
