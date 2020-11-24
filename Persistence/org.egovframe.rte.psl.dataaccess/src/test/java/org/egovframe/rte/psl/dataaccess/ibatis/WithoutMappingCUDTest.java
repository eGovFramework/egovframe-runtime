package org.egovframe.rte.psl.dataaccess.ibatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.dao.DeptDAO;
import org.egovframe.rte.psl.dataaccess.vo.DeptVO;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
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
public class WithoutMappingCUDTest extends TestBase {

	@Resource(name = "deptDAO")
	DeptDAO deptDAO;

	@Before
	public void onSetUp() throws Exception {
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"));
	}

	public DeptVO makeVO() {
		DeptVO vo = new DeptVO();
		vo.setDeptNo(new BigDecimal(90));
		vo.setDeptName("test Dept");
		vo.setLoc("test Loc");
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
	public void testSimpleInsert() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptDAO.insertDept("insertDeptSimple", vo);

		// select
		DeptVO resultVO = deptDAO.selectDept("selectDeptSimpleUsingResultClass", vo);

		// check
		checkResult(vo, resultVO);
	}

	@Rollback(false)
	@Test
	public void testSimpleUpdate() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptDAO.insertDept("insertDeptSimple", vo);

		// data change
		vo.setDeptName("upd Dept");
		vo.setLoc("upd loc");

		// update
		int effectedRows = deptDAO.updateDept("updateDeptSimple", vo);
		assertEquals(1, effectedRows);

		// select
		DeptVO resultVO = deptDAO.selectDept("selectDeptSimpleUsingResultClass", vo);

		// check
		checkResult(vo, resultVO);
	}

	@Rollback(false)
	@Test
	public void testSimpleDelete() throws Exception {
		DeptVO vo = makeVO();

		// insert
		deptDAO.insertDept("insertDeptSimple", vo);

		// delete
		int effectedRows = deptDAO.deleteDept("deleteDeptSimple", vo);
		assertEquals(1, effectedRows);

		// select
		DeptVO resultVO = deptDAO.selectDept("selectDeptSimpleUsingResultClass", vo);

		// null 이어야 함
		assertNull(resultVO);
	}
}
