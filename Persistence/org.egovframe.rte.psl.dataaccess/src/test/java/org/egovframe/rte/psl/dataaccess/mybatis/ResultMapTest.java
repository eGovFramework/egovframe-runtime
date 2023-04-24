package org.egovframe.rte.psl.dataaccess.mybatis;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.dao.EmpMapper;
import org.egovframe.rte.psl.dataaccess.vo.EmpDeptSimpleCompositeVO;
import org.egovframe.rte.psl.dataaccess.vo.EmpExtendsDeptVO;
import org.egovframe.rte.psl.dataaccess.vo.EmpVO;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
public class ResultMapTest extends TestBase {

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
	}

	@Rollback(false)
	@Test
	public void testResultMapSelect() throws Exception {
		EmpVO vo = new EmpVO();
		// 7369,'SMITH','CLERK',7902,'1980-12-17',800,NULL,20
		vo.setEmpNo(new BigDecimal(7369));

		// select
		EmpVO resultVO = empMapper.selectEmp("org.egovframe.rte.psl.dataaccess.EmpMapper.selectEmpUsingResultMap", vo);

		// check
		assertNotNull(resultVO);
		assertEquals(new BigDecimal(7369), resultVO.getEmpNo());
		assertEquals("SMITH", resultVO.getEmpName());
		assertEquals("CLERK", resultVO.getJob());
		assertEquals(new BigDecimal(7902), resultVO.getMgr());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
		assertEquals(sdf.parse("1980-12-17"), resultVO.getHireDate());
		assertEquals(new BigDecimal(800), resultVO.getSal());

		// nullValue test - <result property="comm"
		// column="COMM" .. nullValue="0" />
		//assertEquals(new BigDecimal(0), resultVO.getComm());
		assertEquals(new BigDecimal(20), resultVO.getDeptNo());
	}

	@Rollback(false)
	@Test
	public void testExtendsResultMapSelect() throws Exception {
		EmpVO vo = new EmpVO();
		// 7369,'SMITH','CLERK',7902,'1980-12-17',800,NULL,20
		vo.setEmpNo(new BigDecimal(7369));

		// select
		EmpExtendsDeptVO resultVO = empMapper.selectEmpExtendsDept("org.egovframe.rte.psl.dataaccess.EmpMapper.selectEmpExtendsDeptUsingResultMap", vo);

		// check
		assertNotNull(resultVO);
		// resultMap extends test (extends empResult)
		assertEquals(new BigDecimal(7369), resultVO.getEmpNo());
		assertEquals("SMITH", resultVO.getEmpName());
		assertEquals("CLERK", resultVO.getJob());
		assertEquals(new BigDecimal(7902), resultVO.getMgr());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
		assertEquals(sdf.parse("1980-12-17"), resultVO.getHireDate());
		assertEquals(new BigDecimal(800), resultVO.getSal());
		// nullValue test - <result property="comm"
		// column="COMM" .. nullValue="0" />
		//assertEquals(new BigDecimal(0), resultVO.getComm());
		assertEquals(new BigDecimal(20), resultVO.getDeptNo());

		assertEquals("RESEARCH", resultVO.getDeptName());
		assertEquals("DALLAS", resultVO.getLoc());

	}

	@Rollback(false)
	@Test
	public void testSimpleCompositeResultMapSelect() throws Exception {
		EmpVO vo = new EmpVO();
		// 7369,'SMITH','CLERK',7902,'1980-12-17',800,NULL,20
		vo.setEmpNo(new BigDecimal(7369));

		// select
		EmpDeptSimpleCompositeVO resultVO = empMapper.selectEmpDeptSimpleComposite("org.egovframe.rte.psl.dataaccess.EmpMapper.selectEmpDeptSimpleCompositeUsingResultMap", vo);

		// check
		assertNotNull(resultVO);
		assertEquals(new BigDecimal(7369), resultVO.getEmpNo());
		assertEquals("SMITH", resultVO.getEmpName());
		assertEquals("CLERK", resultVO.getJob());
		assertEquals(new BigDecimal(7902), resultVO.getMgr());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
		assertEquals(sdf.parse("1980-12-17"), resultVO.getHireDate());
		assertEquals(new BigDecimal(800), resultVO.getSal());
		// nullValue test - <result property="comm"
		// column="COMM" .. nullValue="0" />
		//assertEquals(new BigDecimal(0), resultVO.getComm());
		assertEquals(new BigDecimal(20), resultVO.getDeptNo());
		assertEquals("RESEARCH", resultVO.getDeptName());
		assertEquals("DALLAS", resultVO.getLoc());

	}
}
