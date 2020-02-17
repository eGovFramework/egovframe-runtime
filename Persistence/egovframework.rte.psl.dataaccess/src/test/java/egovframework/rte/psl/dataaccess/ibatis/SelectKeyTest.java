package egovframework.rte.psl.dataaccess.ibatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-*.xml" })
@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class SelectKeyTest extends TestBase {

	@Resource(name = "empDAO")
	EmpDAO empDAO;

	@Before
	public void onSetUp() throws Exception {
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

		assertNotNull(resultVO);
		assertEquals(vo.getEmpNo(), resultVO.getEmpNo());
		assertEquals("홍길동", resultVO.getEmpName());
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

	@SuppressWarnings("deprecation")
	@Test
	public void testInsertUsingSelectKey() throws Exception {
		EmpVO vo = makeVO();

		// insert
		BigDecimal selectKey = empDAO.insertEmpUsingSelectKey("insertEmpUsingSelectKey", vo);

		// key 를 딴 값을 받아와 비교를 위해 source vo 에 설정
		vo.setEmpNo(selectKey);

		// select
		EmpVO resultVO = empDAO.selectEmp("selectEmpUsingResultMap", vo);

		// check
		checkResult(vo, resultVO);

		// delete all
		empDAO.getSqlMapClientTemplate().delete("deleteEmpAll");

		EmpVO vo2 = makeVO();

		// 처음 데이터 입력인 경우는 1000 으로 key 를 설정토록 작성하였음.
		BigDecimal selectKey2 = empDAO.insertEmpUsingSelectKey("insertEmpUsingSelectKey", vo2);

		assertEquals(new BigDecimal(1000), selectKey2);

		vo2.setEmpNo(selectKey2);

		// check
		checkResult(vo, resultVO);
	}
}
