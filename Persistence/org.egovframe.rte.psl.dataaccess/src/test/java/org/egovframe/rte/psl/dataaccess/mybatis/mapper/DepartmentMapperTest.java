package org.egovframe.rte.psl.dataaccess.mybatis.mapper;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.mapper.DepartmentMapper;
import org.egovframe.rte.psl.dataaccess.vo.DeptVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;

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
public class DepartmentMapperTest extends TestBase {

	@Resource(name = "departmentMapper")
	DepartmentMapper departmentMapper;

	@Before
	public void onSetUp() throws Exception {
		// 외부에 sql file 로부터 DB 초기화 (기존 테이블 삭제/생성 및 초기데이터 구축)
		// Spring 의 JdbcTestUtils 사용,
		// continueOnError 플래그는 true로 설정 - cf.) DDL 이 포함된 경우 rollback 에 유의
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"));
	}

	public DeptVO makeVO() {
		DeptVO vo = new DeptVO();
		vo.setDeptNo(new BigDecimal(10));
		vo.setDeptName("총무부");
		vo.setLoc("본사");
		return vo;
	}

	public void checkResult(DeptVO vo, DeptVO resultVO) {
		assertNotNull(resultVO);
		assertEquals(vo.getDeptNo(), resultVO.getDeptNo());
		assertEquals(vo.getDeptName(), resultVO.getDeptName());
		assertEquals(vo.getLoc(), resultVO.getLoc());
	}

	@Test
	public void testInsert() throws Exception {
		DeptVO vo = makeVO();

		// insert
		departmentMapper.insertDepartment(vo);

		// select
		DeptVO resultVO = departmentMapper.selectDepartment(vo.getDeptNo());

		// check
		checkResult(vo, resultVO);
	}

	@Test
	public void testUpdate() throws Exception {
		DeptVO vo = makeVO();

		// insert
		departmentMapper.insertDepartment(vo);

		// data change
		vo.setDeptName("개발부");
		vo.setLoc("연구소");

		// update
		int effectedRows = departmentMapper.updateDepartment(vo);
		assertEquals(1, effectedRows);

		// select
		DeptVO resultVO = departmentMapper.selectDepartment(vo.getDeptNo());

		// check
		checkResult(vo, resultVO);
	}

	@Test
	public void testDelete() throws Exception {
		DeptVO vo = makeVO();

		// insert
		departmentMapper.insertDepartment(vo);

		// delete
		int effectedRows = departmentMapper.deleteDepartment(vo.getDeptNo());
		assertEquals(1, effectedRows);

		// select
		DeptVO resultVO = departmentMapper.selectDepartment(vo.getDeptNo());

		// null 이어야 함
		assertNull(resultVO);
	}
}
