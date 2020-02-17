package egovframework.rte.psl.dataaccess.mybatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import egovframework.rte.psl.dataaccess.TestBase;
import egovframework.rte.psl.dataaccess.dao.EmpMapper;
import egovframework.rte.psl.dataaccess.vo.EmpDeptSimpleCompositeVO;
import egovframework.rte.psl.dataaccess.vo.EmpExtendsDeptVO;
import egovframework.rte.psl.dataaccess.vo.EmpVO;

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
		JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"), true);

		// init data
		JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource("META-INF/testdata/sample_schema_initdata_" + usingDBMS + ".sql"), true);
	}

	@Test
	public void testResultMapSelect() throws Exception {
		EmpVO vo = new EmpVO();
		// 7369,'SMITH','CLERK',7902,'1980-12-17',800,NULL,20
		vo.setEmpNo(new BigDecimal(7369));

		// select
		EmpVO resultVO = empMapper.selectEmp("egovframework.rte.psl.dataaccess.EmpMapper.selectEmpUsingResultMap", vo);

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

	@Test
	public void testExtendsResultMapSelect() throws Exception {
		EmpVO vo = new EmpVO();
		// 7369,'SMITH','CLERK',7902,'1980-12-17',800,NULL,20
		vo.setEmpNo(new BigDecimal(7369));

		// select
		EmpExtendsDeptVO resultVO = empMapper.selectEmpExtendsDept("egovframework.rte.psl.dataaccess.EmpMapper.selectEmpExtendsDeptUsingResultMap", vo);

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

	@Test
	public void testSimpleCompositeResultMapSelect() throws Exception {
		EmpVO vo = new EmpVO();
		// 7369,'SMITH','CLERK',7902,'1980-12-17',800,NULL,20
		vo.setEmpNo(new BigDecimal(7369));

		// select
		EmpDeptSimpleCompositeVO resultVO = empMapper.selectEmpDeptSimpleComposite("egovframework.rte.psl.dataaccess.EmpMapper.selectEmpDeptSimpleCompositeUsingResultMap", vo);

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
