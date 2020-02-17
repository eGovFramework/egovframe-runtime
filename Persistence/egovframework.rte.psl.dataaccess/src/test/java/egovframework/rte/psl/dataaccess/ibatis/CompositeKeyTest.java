package egovframework.rte.psl.dataaccess.ibatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import egovframework.rte.psl.dataaccess.TestBase;
import egovframework.rte.psl.dataaccess.dao.EmpDAO;
import egovframework.rte.psl.dataaccess.vo.EmpIncludesEmpListVO;
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
 *  <p></p>
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2014.01.22 권윤정  SimpleJdbcTestUtils -> JdbcTestUtils 변경
 *   2014.01.22 권윤정  SimpleJdbcTemplate -> JdbcTemplate 변경
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-*.xml" })
@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class CompositeKeyTest extends TestBase {

	@Resource(name = "empDAO")
	EmpDAO empDAO;

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
	public void testCompositeKeySelect() throws Exception {
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
		assertEquals("SALESMAN", resultVO.getJob());
		assertEquals(new BigDecimal(7698), resultVO.getMgr());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
		assertEquals(sdf.parse("1981-02-22"), resultVO.getHireDate());
		assertEquals(new BigDecimal(1250), resultVO.getSal());
		assertEquals(new BigDecimal(500), resultVO.getComm());
		assertEquals(new BigDecimal(30), resultVO.getDeptNo());

		assertTrue(resultVO.getEmpList() instanceof List);
		assertEquals(3, resultVO.getEmpList().size());
		assertEquals(new BigDecimal(7499), resultVO.getEmpList().get(0).getEmpNo());
		assertEquals(new BigDecimal(1600), resultVO.getEmpList().get(0).getSal());
		assertEquals(new BigDecimal(7844), resultVO.getEmpList().get(1).getEmpNo());
		assertEquals(new BigDecimal(1500), resultVO.getEmpList().get(1).getSal());
		assertEquals(new BigDecimal(7654), resultVO.getEmpList().get(2).getEmpNo());
		assertEquals(new BigDecimal(1250), resultVO.getEmpList().get(2).getSal());

	}

}
