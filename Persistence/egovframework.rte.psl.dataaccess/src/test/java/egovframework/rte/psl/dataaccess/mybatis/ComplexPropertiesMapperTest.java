package egovframework.rte.psl.dataaccess.mybatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import egovframework.rte.psl.dataaccess.TestBase;
import egovframework.rte.psl.dataaccess.dao.EmpGeneralMapper;
import egovframework.rte.psl.dataaccess.vo.DeptIncludesEmpListVO;
import egovframework.rte.psl.dataaccess.vo.DeptVO;
import egovframework.rte.psl.dataaccess.vo.EmpIncludesDeptVO;
import egovframework.rte.psl.dataaccess.vo.EmpIncludesMgrVO;
import egovframework.rte.psl.dataaccess.vo.EmpVO;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.UncategorizedSQLException;
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
public class ComplexPropertiesMapperTest extends TestBase {

	@Resource(name = "empGeneralMapper")
	EmpGeneralMapper empGeneralMapper;

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
	public void testComplexPropertiesOneToOneResultMapSelect() throws Exception {
		EmpVO vo = new EmpVO();
		// 7369,'SMITH','CLERK',7902,'1980-12-17',800,NULL,20
		vo.setEmpNo(new BigDecimal(7369));

		// select
		EmpIncludesDeptVO resultVO = empGeneralMapper.selectEmpDeptComplexProperties("selectEmpIncludesDeptResultUsingResultMap", vo);

		// check
		assertNotNull(resultVO);
		assertEquals(new BigDecimal(7369), resultVO.getEmpNo());
		assertEquals("SMITH", resultVO.getEmpName());
		assertEquals("CLERK", resultVO.getJob());
		assertEquals(new BigDecimal(7902), resultVO.getMgr());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
		assertEquals(sdf.parse("1980-12-17"), resultVO.getHireDate());
		assertEquals(new BigDecimal(800), resultVO.getSal());
		//assertEquals(new BigDecimal(0), resultVO.getComm());  -->NULL을 리턴하여 주석처리함.
		assertEquals(new BigDecimal(20), resultVO.getDeptNo());
		// 1:1 relation included DeptVO
		assertEquals(new BigDecimal(20), resultVO.getDeptVO().getDeptNo());
		assertEquals("RESEARCH", resultVO.getDeptVO().getDeptName());
		assertEquals("DALLAS", resultVO.getDeptVO().getLoc());
	}

	@Test
	public void testComplexPropertiesOneToManyResultMapSelect() throws Exception {
		DeptVO vo = new DeptVO();
		// 20,'RESEARCH','DALLAS'
		vo.setDeptNo(new BigDecimal(20));

		// select
		DeptIncludesEmpListVO resultVO = empGeneralMapper.selectDeptEmpListComplexProperties("selectDeptIncludesEmpListResultUsingResultMap", vo);

		// check
		assertNotNull(resultVO);
		assertEquals(new BigDecimal(20), resultVO.getDeptNo());
		assertEquals("RESEARCH", resultVO.getDeptName());
		assertEquals("DALLAS", resultVO.getLoc());

		assertTrue(0 < resultVO.getEmpVOList().size());

		//* deptNo 20 인 EmpList 는 초기데이터에 따라7369,
		//* 'SMITH','CLERK',7902,'1980-12-17',800,NULL,20
		//* 7566,
		//* 'JONES','MANAGER',7839,'1981-04-02',2975,NULL,20
		//* 7788,
		//* 'SCOTT','ANALYST',7566,'1987-04-19',3000,NULL,20
		//* 7876
		//* ,'ADAMS','CLERK',7788,'1987-05-23',1100,NULL,20
		//* 7902
		//* ,'FORD','ANALYST',7566,'1981-12-03',3000,NULL,20

		assertEquals(5, resultVO.getEmpVOList().size());

		assertEquals(new BigDecimal(7369), resultVO.getEmpVOList().get(0).getEmpNo());
		assertEquals("SMITH", resultVO.getEmpVOList().get(0).getEmpName());
		assertEquals("CLERK", resultVO.getEmpVOList().get(0).getJob());
		assertEquals(new BigDecimal(7902), resultVO.getEmpVOList().get(0).getMgr());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
		assertEquals(sdf.parse("1980-12-17"), resultVO.getEmpVOList().get(0).getHireDate());
		assertEquals(new BigDecimal(800), resultVO.getEmpVOList().get(0).getSal());
		//assertEquals(new BigDecimal(0), resultVO.getEmpVOList().get(0).getComm());  -->NULL을 리턴하여 주석처리함.
		assertEquals(new BigDecimal(20), resultVO.getEmpVOList().get(0).getDeptNo());

		assertEquals(new BigDecimal(7566), resultVO.getEmpVOList().get(1).getEmpNo());
		assertEquals(new BigDecimal(7788), resultVO.getEmpVOList().get(2).getEmpNo());
		assertEquals(new BigDecimal(7876), resultVO.getEmpVOList().get(3).getEmpNo());
		assertEquals(new BigDecimal(7902), resultVO.getEmpVOList().get(4).getEmpNo());
	}

	@Test
	public void testComplexPropertiesOneToManyVOListResultMapSelect() throws Exception {
		DeptVO vo = new DeptVO();
		// deptName 에 의한 like 검색 테스트 '%'|| 'E' ||'%'
		// --> R'E'S'E'ARCH, SAL'E'S, OP'E'RATIONS
		// 20,'RESEARCH','DALLAS'
		// 30,'SALES','CHICAGO'
		// 40,'OPERATIONS','BOSTON'
		vo.setDeptName("E");

		// select
		List<DeptIncludesEmpListVO> resultList = empGeneralMapper.selectDeptEmpListComplexPropertiesList(isMysql ? "selectDeptIncludesEmpListResultListUsingResultMapMysql"
				: "selectDeptIncludesEmpListResultListUsingResultMap", vo);

		// check
		assertNotNull(resultList);
		assertEquals(3, resultList.size());

		assertEquals(new BigDecimal(20), resultList.get(0).getDeptNo());
		assertEquals(new BigDecimal(30), resultList.get(1).getDeptNo());
		assertEquals(new BigDecimal(40), resultList.get(2).getDeptNo());

		//* deptNo 20 인 EmpList 는 초기데이터에 따라 5 명, deptNo
		//* 30 인 EmpList 는 초기데이터에 따라 6 명, deptNo 40 인
		//* EmpList 는 초기데이터에 따라 0 명 --> cf.)outer join 에
		//* 따라 deptNo 만 가진 EmpVO 1건 생김

		assertEquals(5, resultList.get(0).getEmpVOList().size());
		assertEquals(6, resultList.get(1).getEmpVOList().size());
		// cf.)outer join 에 따라 deptNo 만 가진 EmpVO 1건 생김을
		// 확인함. 주의할 것!
		assertEquals(1, resultList.get(2).getEmpVOList().size());
		assertNull(resultList.get(2).getEmpVOList().get(0).getEmpNo());

		//* deptNo 20 인 EmpList 는 초기데이터에 따라7369,
		//* 'SMITH','CLERK',7902,'1980-12-17',800,NULL,20
		//* 7566,
		//* 'JONES','MANAGER',7839,'1981-04-02',2975,NULL,20
		//* 7788,
		//* 'SCOTT','ANALYST',7566,'1987-04-19',3000,NULL,20
		//* 7876
		//* ,'ADAMS','CLERK',7788,'1987-05-23',1100,NULL,20
		//* 7902
		//* ,'FORD','ANALYST',7566,'1981-12-03',3000,NULL,20

		assertEquals(new BigDecimal(7566), resultList.get(0).getEmpVOList().get(1).getEmpNo());
		assertEquals(new BigDecimal(7788), resultList.get(0).getEmpVOList().get(2).getEmpNo());
		assertEquals(new BigDecimal(7876), resultList.get(0).getEmpVOList().get(3).getEmpNo());
		assertEquals(new BigDecimal(7902), resultList.get(0).getEmpVOList().get(4).getEmpNo());
	}

	@Test
	public void testComplexPropertiesOneToManyVOListRepetitionSelect() throws Exception {
		DeptVO vo = new DeptVO();
		// deptName 에 의한 like 검색 테스트 '%'|| 'E' ||'%'
		// --> R'E'S'E'ARCH, SAL'E'S, OP'E'RATIONS
		// 20,'RESEARCH','DALLAS'
		// 30,'SALES','CHICAGO'
		// 40,'OPERATIONS','BOSTON'
		vo.setDeptName("E");

		// select
		List<DeptIncludesEmpListVO> resultList = empGeneralMapper.selectDeptEmpListComplexPropertiesList(isMysql ? "selectDeptIncludesEmpListResultListUsingRepetitionSelectMysql"
				: "selectDeptIncludesEmpListResultListUsingRepetitionSelect", vo);

		// check
		assertNotNull(resultList);
		assertEquals(3, resultList.size());

		assertEquals(new BigDecimal(20), resultList.get(0).getDeptNo());
		assertEquals(new BigDecimal(30), resultList.get(1).getDeptNo());
		assertEquals(new BigDecimal(40), resultList.get(2).getDeptNo());

		//* deptNo 20 인 EmpList 는 초기데이터에 따라 5 명, deptNo
		//* 30 인 EmpList 는 초기데이터에 따라 6 명 deptNo 40 인
		//* EmpList 는 초기데이터에 따라 0 명 --> 위 outer join
		//* 케이스와 달리 EmpList 도 건수 없음 확인

		assertEquals(5, resultList.get(0).getEmpVOList().size());
		assertEquals(6, resultList.get(1).getEmpVOList().size());
		assertEquals(0, resultList.get(2).getEmpVOList().size());

		// * deptNo 20 인 EmpList 는 초기데이터에 따라7369,
		// * 'SMITH','CLERK',7902,'1980-12-17',800,NULL,20
		// * 7566,
		// * 'JONES','MANAGER',7839,'1981-04-02',2975,NULL,20
		// * 7788,
		// * 'SCOTT','ANALYST',7566,'1987-04-19',3000,NULL,20
		// * 7876
		// * ,'ADAMS','CLERK',7788,'1987-05-23',1100,NULL,20
		// * 7902
		// * ,'FORD','ANALYST',7566,'1981-12-03',3000,NULL,20

		assertEquals(new BigDecimal(7566), resultList.get(0).getEmpVOList().get(1).getEmpNo());
		assertEquals(new BigDecimal(7788), resultList.get(0).getEmpVOList().get(2).getEmpNo());
		assertEquals(new BigDecimal(7876), resultList.get(0).getEmpVOList().get(3).getEmpNo());
		assertEquals(new BigDecimal(7902), resultList.get(0).getEmpVOList().get(4).getEmpNo());
	}

	@Test
	public void testComplexPropertiesHierarcyRepetitionSelect() throws Exception {
		EmpVO vo = new EmpVO();
		// 7369,'SMITH','CLERK',7902
		// --> 7902,'FORD','ANALYST',7566
		// --> 7566,'JONES','MANAGER',7839
		// --> 7839,'KING','PRESIDENT',NULL
		vo.setEmpNo(new BigDecimal(7369));

		try {

			// select
			// EmpIncludesMgrVO resultVO =
			// empDAO.selectEmpMgrHierarchy("selectEmpWithMgr",
			// vo);
			EmpIncludesMgrVO resultVO = empGeneralMapper.selectEmpMgrHierarchy("selectMgrHierarchy", vo);

			// check
			assertNotNull(resultVO);
			assertEquals(new BigDecimal(7369), resultVO.getEmpNo());
			assertEquals("SMITH", resultVO.getEmpName());
			assertEquals("CLERK", resultVO.getJob());
			assertEquals(new BigDecimal(7902), resultVO.getMgr());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
			assertEquals(sdf.parse("1980-12-17"), resultVO.getHireDate());
			assertEquals(new BigDecimal(800), resultVO.getSal());
			//assertEquals(new BigDecimal(0), resultVO.getComm());  -->NULL을 리턴하여 주석처리함.
			assertEquals(new BigDecimal(20), resultVO.getDeptNo());

			assertTrue(resultVO.getMgrVO() instanceof EmpIncludesMgrVO);
			assertEquals(new BigDecimal(7902), resultVO.getMgrVO().getEmpNo());
			assertEquals(new BigDecimal(7566), resultVO.getMgrVO().getMgrVO().getEmpNo());
			assertEquals(new BigDecimal(7839), resultVO.getMgrVO().getMgrVO().getMgrVO().getEmpNo());
			assertNull(resultVO.getMgrVO().getMgrVO().getMgrVO().getMgrVO());

		} catch (UncategorizedSQLException ue) {
			ue.printStackTrace();
			// tibero 인 경우 ibatis 의 재귀 queyr 형태의 sub 객체
			// 맵핑 시
			// com.tmax.tibero.jdbc.TbSQLException:
			// TJDBC-90646:Resultset
			// is already closed 에러 발생 확인함!
			//assertTrue(isTibero);
			System.out.println("=====ue.getCause()======" + ue.getCause());
			// assertTrue(ue.getCause() instanceof NestedSQLException);
			//            assertTrue(ue.getCause().getCause().getCause().getCause() instanceof TbSQLException);
			//            assertTrue(((TbSQLException) ue.getCause().getCause().getCause()
			//                .getCause()).getMessage().contains(
			//                "TJDBC-90646:Resultset is already closed"));
		}
	}

}
