package egovframework.rte.psl.dataaccess.ibatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import egovframework.rte.psl.dataaccess.TestBase;
import egovframework.rte.psl.dataaccess.dao.EmpDAO;
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
public class CacheModelTest extends TestBase {

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

	public EmpVO makeVO() throws Exception {
		EmpVO vo = new EmpVO();
		// '홍길동','CLERK',7902,'2009-02-18',800,NULL,20
		// vo.setEmpNo(new BigDecimal(????));
		vo.setEmpName("홍길동");
		vo.setJob("CLERK");
		vo.setMgr(new BigDecimal(7902));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
		vo.setHireDate(sdf.parse("2009-02-18"));
		vo.setSal(new BigDecimal(800));
		vo.setDeptNo(new BigDecimal(20));

		return vo;
	}

	public void checkResult(EmpVO vo, EmpVO resultVO) throws Exception {
		checkResult(vo, resultVO, "홍길동");
	}

	public void checkResult(EmpVO vo, EmpVO resultVO, String empName) throws Exception {

		assertNotNull(resultVO);
		assertEquals(vo.getEmpNo(), resultVO.getEmpNo());
		assertEquals(empName, resultVO.getEmpName());
		assertEquals("CLERK", resultVO.getJob());
		assertEquals(new BigDecimal(7902), resultVO.getMgr());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
		assertEquals(sdf.parse("2009-02-18"), resultVO.getHireDate());
		assertEquals(new BigDecimal(800), resultVO.getSal());
		// nullValue test - <result property="comm"
		// column="COMM" .. nullValue="0" />
		assertEquals(new BigDecimal(0), resultVO.getComm());
		assertEquals(new BigDecimal(20), resultVO.getDeptNo());
	}

	@Test
	public void testSelectUsingCacheModelLRU() throws Exception {
		EmpVO vo = makeVO();

		// insert
		BigDecimal selectKey = empDAO.insertEmpUsingSelectKey("insertEmpUsingSelectKey", vo);

		// key 를 딴 값을 받아와 비교를 위해 source vo 에 설정
		vo.setEmpNo(selectKey);

		// select - cache model use
		EmpVO resultVO = empDAO.selectEmp("selectEmpUsingCacheModelLRU", vo);

		// check
		checkResult(vo, resultVO);

		// select - 같은 조건으로 동일한 쿼리 재실행
		EmpVO resultVO2 = empDAO.selectEmp("selectEmpUsingCacheModelLRU", vo);

		// check
		checkResult(vo, resultVO2);

		// 결과 객체의 instance 가 같음!
		assertEquals(resultVO, resultVO2);
		assertSame(resultVO, resultVO2);

		// select - cache model 을 사용하지 않는 일반 select
		EmpVO resultVO3 = empDAO.selectEmp("selectEmpUsingResultMap", vo);

		// check
		checkResult(vo, resultVO3);

		// 결과 객체의 instance 가 다름 (cache 를 사용하지 않으면 매번
		// 새로운 결과 객체가 만들어짐)
		assertNotSame(resultVO, resultVO3);

		// EMP 추가 insert -> <flushOnExecute
		// statement="insertEmpUsingSelectKey" /> 테스트
		EmpVO vo2 = makeVO();
		vo2.setEmpName("홍길순");
		// 기타 속성은 같다고 가정

		// insert - vo2
		BigDecimal selectKey2 = empDAO.insertEmpUsingSelectKey("insertEmpUsingSelectKey", vo2);

		// select - '홍길동' 을 cache 사용하여 재조회
		EmpVO resultVO4 = empDAO.selectEmp("selectEmpUsingCacheModelLRU", vo);

		// check
		checkResult(vo, resultVO4);

		// 이미 flush 되어 이전 조회한 cache 된 instance 와는 다름
		assertNotSame(resultVO, resultVO4);

		// '홍길순' 조회 및 cache 적용 확인
		vo2.setEmpNo(selectKey2);

		// select - '홍길순' 을 cache 사용하여 조회
		EmpVO resultVO5 = empDAO.selectEmp("selectEmpUsingCacheModelLRU", vo2);

		// check
		checkResult(vo2, resultVO5, "홍길순");

		// select - '홍길순' 을 cache 사용하여 재조회
		EmpVO resultVO6 = empDAO.selectEmp("selectEmpUsingCacheModelLRU", vo2);

		// check
		checkResult(vo2, resultVO6, "홍길순");

		// 결과 객체의 instance 가 같음!
		assertEquals(resultVO5, resultVO6);
		assertSame(resultVO5, resultVO6);

	}

	@Test
	public void testSelectUsingCacheModelMEMORY() throws Exception {
		EmpVO vo = makeVO();

		// insert
		BigDecimal selectKey = empDAO.insertEmpUsingSelectKey("insertEmpUsingSelectKey", vo);

		// key 를 딴 값을 받아와 비교를 위해 source vo 에 설정
		vo.setEmpNo(selectKey);

		// select - cache model use
		EmpVO resultVO = empDAO.selectEmp("selectEmpUsingCacheModelMEMORY", vo);

		// check
		checkResult(vo, resultVO);

		// select - 같은 조건으로 동일한 쿼리 재실행
		EmpVO resultVO2 = empDAO.selectEmp("selectEmpUsingCacheModelMEMORY", vo);

		// check
		checkResult(vo, resultVO2);

		// 결과 객체의 instance 가 같음!
		assertEquals(resultVO, resultVO2);
		assertSame(resultVO, resultVO2);

		// select - cache model 을 사용하지 않는 일반 select
		EmpVO resultVO3 = empDAO.selectEmp("selectEmpUsingResultMap", vo);

		// check
		checkResult(vo, resultVO3);

		// 결과 객체의 instance 가 다름 (cache 를 사용하지 않으면 매번
		// 새로운 결과 객체가 만들어짐)
		assertNotSame(resultVO, resultVO3);

		// EMP 추가 insert -> <flushOnExecute
		// statement="insertEmpUsingSelectKey" /> 테스트
		EmpVO vo2 = makeVO();
		vo2.setEmpName("홍길순");
		// 기타 속성은 같다고 가정

		// insert - vo2
		BigDecimal selectKey2 = empDAO.insertEmpUsingSelectKey("insertEmpUsingSelectKey", vo2);

		// select - '홍길동' 을 cache 사용하여 재조회
		EmpVO resultVO4 = empDAO.selectEmp("selectEmpUsingCacheModelMEMORY", vo);

		// check
		checkResult(vo, resultVO4);

		// 이미 flush 되어 이전 조회한 cache 된 instance 와는 다름
		assertNotSame(resultVO, resultVO4);

		// '홍길순' 조회 및 cache 적용 확인
		vo2.setEmpNo(selectKey2);

		// select - '홍길순' 을 cache 사용하여 조회
		EmpVO resultVO5 = empDAO.selectEmp("selectEmpUsingCacheModelMEMORY", vo2);

		// check
		checkResult(vo2, resultVO5, "홍길순");

		// select - '홍길순' 을 cache 사용하여 재조회
		EmpVO resultVO6 = empDAO.selectEmp("selectEmpUsingCacheModelMEMORY", vo2);

		// check
		checkResult(vo2, resultVO6, "홍길순");

		// 결과 객체의 instance 가 같음!
		assertEquals(resultVO5, resultVO6);
		assertSame(resultVO5, resultVO6);

	}

	@Test
	public void testSelectUsingCacheModelFIFO() throws Exception {
		EmpVO vo = makeVO();

		// insert
		BigDecimal selectKey = empDAO.insertEmpUsingSelectKey("insertEmpUsingSelectKey", vo);

		// key 를 딴 값을 받아와 비교를 위해 source vo 에 설정
		vo.setEmpNo(selectKey);

		// select - cache model use
		EmpVO resultVO = empDAO.selectEmp("selectEmpUsingCacheModelFIFO", vo);

		// check
		checkResult(vo, resultVO);

		// select - 같은 조건으로 동일한 쿼리 재실행
		EmpVO resultVO2 = empDAO.selectEmp("selectEmpUsingCacheModelFIFO", vo);

		// check
		checkResult(vo, resultVO2);

		// 결과 객체의 instance 가 같음!
		assertEquals(resultVO, resultVO2);
		assertSame(resultVO, resultVO2);

		// select - cache model 을 사용하지 않는 일반 select
		EmpVO resultVO3 = empDAO.selectEmp("selectEmpUsingResultMap", vo);

		// check
		checkResult(vo, resultVO3);

		// 결과 객체의 instance 가 다름 (cache 를 사용하지 않으면 매번
		// 새로운 결과 객체가 만들어짐)
		assertNotSame(resultVO, resultVO3);

		// EMP 추가 insert -> <flushOnExecute
		// statement="insertEmpUsingSelectKey" /> 테스트
		EmpVO vo2 = makeVO();
		vo2.setEmpName("홍길순");
		// 기타 속성은 같다고 가정

		// insert - vo2
		BigDecimal selectKey2 = empDAO.insertEmpUsingSelectKey("insertEmpUsingSelectKey", vo2);

		// select - '홍길동' 을 cache 사용하여 재조회
		EmpVO resultVO4 = empDAO.selectEmp("selectEmpUsingCacheModelFIFO", vo);

		// check
		checkResult(vo, resultVO4);

		// 이미 flush 되어 이전 조회한 cache 된 instance 와는 다름
		assertNotSame(resultVO, resultVO4);

		// '홍길순' 조회 및 cache 적용 확인
		vo2.setEmpNo(selectKey2);

		// select - '홍길순' 을 cache 사용하여 조회
		EmpVO resultVO5 = empDAO.selectEmp("selectEmpUsingCacheModelFIFO", vo2);

		// check
		checkResult(vo2, resultVO5, "홍길순");

		// select - '홍길순' 을 cache 사용하여 재조회
		EmpVO resultVO6 = empDAO.selectEmp("selectEmpUsingCacheModelFIFO", vo2);

		// check
		checkResult(vo2, resultVO6, "홍길순");

		// 결과 객체의 instance 가 같음!
		assertEquals(resultVO5, resultVO6);
		assertSame(resultVO5, resultVO6);

	}

	@Test
	public void testSelectUsingCacheModelOSCACHE() throws Exception {
		EmpVO vo = makeVO();

		// insert
		BigDecimal selectKey = empDAO.insertEmpUsingSelectKey("insertEmpUsingSelectKey", vo);

		// key 를 딴 값을 받아와 비교를 위해 source vo 에 설정
		vo.setEmpNo(selectKey);

		// select - cache model use
		EmpVO resultVO = empDAO.selectEmp("selectEmpUsingCacheModelOSCACHE", vo);

		// check
		checkResult(vo, resultVO);

		// select - 같은 조건으로 동일한 쿼리 재실행
		EmpVO resultVO2 = empDAO.selectEmp("selectEmpUsingCacheModelOSCACHE", vo);

		// check
		checkResult(vo, resultVO2);

		// 결과 객체의 instance 가 같음!
		assertEquals(resultVO, resultVO2);
		assertSame(resultVO, resultVO2);

		// select - cache model 을 사용하지 않는 일반 select
		EmpVO resultVO3 = empDAO.selectEmp("selectEmpUsingResultMap", vo);

		// check
		checkResult(vo, resultVO3);

		// 결과 객체의 instance 가 다름 (cache 를 사용하지 않으면 매번
		// 새로운 결과 객체가 만들어짐)
		assertNotSame(resultVO, resultVO3);

		// EMP 추가 insert -> <flushOnExecute
		// statement="insertEmpUsingSelectKey" /> 테스트
		EmpVO vo2 = makeVO();
		vo2.setEmpName("홍길순");
		// 기타 속성은 같다고 가정

		// insert - vo2
		BigDecimal selectKey2 = empDAO.insertEmpUsingSelectKey("insertEmpUsingSelectKey", vo2);

		// select - '홍길동' 을 cache 사용하여 재조회
		EmpVO resultVO4 = empDAO.selectEmp("selectEmpUsingCacheModelOSCACHE", vo);

		// check
		checkResult(vo, resultVO4);

		// 이미 flush 되어 이전 조회한 cache 된 instance 와는 다름
		assertNotSame(resultVO, resultVO4);

		// '홍길순' 조회 및 cache 적용 확인
		vo2.setEmpNo(selectKey2);

		// select - '홍길순' 을 cache 사용하여 조회
		EmpVO resultVO5 = empDAO.selectEmp("selectEmpUsingCacheModelOSCACHE", vo2);

		// check
		checkResult(vo2, resultVO5, "홍길순");

		// select - '홍길순' 을 cache 사용하여 재조회
		EmpVO resultVO6 = empDAO.selectEmp("selectEmpUsingCacheModelOSCACHE", vo2);

		// check
		checkResult(vo2, resultVO6, "홍길순");

		// 결과 객체의 instance 가 같음!
		assertEquals(resultVO5, resultVO6);
		assertSame(resultVO5, resultVO6);

	}
}
