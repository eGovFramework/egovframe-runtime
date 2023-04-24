package org.egovframe.rte.psl.dataaccess.mybatis;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.dao.DeptMapper;
import org.egovframe.rte.psl.dataaccess.vo.DeptVO;
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
import java.util.List;

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
public class BasicDataMapperTest extends TestBase {

	@Resource(name = "deptMapper")
	DeptMapper deptMapper;

	@Before
	public void onSetUp() throws Exception {
		// 외부에 sql file 로부터 DB 초기화 (기존 테이블 삭제/생성 및 초기데이터 구축)
		// Spring 의 JdbcTestUtils 사용,
		// continueOnError 플래그는 true로 설정 - cf.) DDL 이 포함된 경우 rollback 에 유의
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"));
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

	@Rollback(false)
	@Test
	public void testBasicInsert() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDept", vo);
		// deptDAO.insertDept("Dept.insertDept", vo);
		// // cf. useStatementNamespaces="true" 일 때

		// select
		DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDept", vo);

		// check
		checkResult(vo, resultVO);
	}

	@Rollback(false)
	@Test
	public void testBasicUpdate() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDept", vo);

		// data change
		vo.setDeptName("upd Dept");
		vo.setLoc("upd loc");

		// update
		int effectedRows = deptMapper.updateDept("org.egovframe.rte.psl.dataaccess.DeptMapper.updateDept", vo);
		assertEquals(1, effectedRows);

		// select
		DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDept", vo);

		// check
		checkResult(vo, resultVO);
	}

	@Rollback(false)
	@Test
	public void testBasicDelete() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDept", vo);

		// delete
		int effectedRows = deptMapper.deleteDept("org.egovframe.rte.psl.dataaccess.DeptMapper.deleteDept", vo);
		assertEquals(1, effectedRows);

		// select
		DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDept", vo);

		// null 이어야 함
		assertNull(resultVO);
	}

	@Rollback(false)
	@Test
	public void testBasicSelectList() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDept", vo);

		// 검색조건으로 key 설정
		DeptVO searchVO = new DeptVO();
		searchVO.setDeptNo(new BigDecimal(90));
		// searchVO.setDeptName(new String("부서"));

		// selectList
		List<DeptVO> resultList = deptMapper.selectDeptList(isMysql ? "org.egovframe.rte.psl.dataaccess.DeptMapper.selectDeptListMysql"
				: "org.egovframe.rte.psl.dataaccess.DeptMapper.selectDeptList", searchVO);

		// key 조건에 대한 결과는 한건일 것임
		assertNotNull(resultList);
		assertTrue(resultList.size() > 0);
		assertEquals(1, resultList.size());
		// assertTrue(resultList.get(0) instanceof DeptVO);
		checkResult(vo, resultList.get(0));

		// 검색조건으로 name 설정 - '%' || #deptName# || '%'
		DeptVO searchVO2 = new DeptVO();
		searchVO2.setDeptName(""); // '%' || '' || '%' --> '%%'

		// selectList
		List<DeptVO> resultList2 = deptMapper.selectDeptList(isMysql ? "org.egovframe.rte.psl.dataaccess.DeptMapper.selectDeptListMysql"
				: "org.egovframe.rte.psl.dataaccess.DeptMapper.selectDeptList", searchVO2);

		// like 조건에 대한 결과는 한건 이상일 것임
		assertNotNull(resultList2);
		assertTrue(resultList2.size() > 0);

	}

	@Rollback(false)
	@Test
	public void testInsertUsingParameterMap() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDeptUsingParameterMap", vo);

		// select
		DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDept", vo);

		// check
		checkResult(vo, resultVO);
	}

	@Rollback(false)
	@Test
	public void testInsertAndSelectUsingParameterClass() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDeptUsingParameterClass", vo);

		// select
		DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDept", vo);

		// check
		checkResult(vo, resultVO);
	}

	@Rollback(false)
	@Test
	public void testInsertUsingInLineParamWithDBType() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDeptUsingInLineParamWithDBType", vo);

		// select
		DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDept", vo);

		// check
		checkResult(vo, resultVO);
	}

	@Rollback(false)
	@Test
	public void testInsertAndSelectUsingResultClass() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDeptUsingParameterClass", vo);

		// select
		// resultClass 를 VO로 직접 명시하는 경우는 VO에 정의된
		// attribute 변수명으로 select 시 column alias 필요
		DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDeptUsingResultClass", vo);

		// check
		checkResult(vo, resultVO);
	}

}
