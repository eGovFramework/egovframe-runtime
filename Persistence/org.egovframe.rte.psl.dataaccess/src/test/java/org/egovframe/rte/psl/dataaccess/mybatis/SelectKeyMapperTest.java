package org.egovframe.rte.psl.dataaccess.mybatis;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.dao.EmpMapper;
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
public class SelectKeyMapperTest extends TestBase {

	@Resource(name = "empMapper")
	EmpMapper empMapper;

	@Before
	public void onSetUp() throws Exception {
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"));

		// init data
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_initdata_" + usingDBMS + ".sql"));
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
		// assertEquals(new BigDecimal(0), resultVO.getComm());
		assertEquals(new BigDecimal(20), resultVO.getDeptNo());
	}

	@Rollback(false)
	@Test
	public void testInsertUsingSelectKey() throws Exception {

		EmpVO vo = makeVO();

		// insert
		// empNo를 키값으로 리턴
		// DB 초기화 값으로 empNo 7934 까지 입력된 상태
		empMapper.insertEmpUsingSelectKey("org.egovframe.rte.psl.dataaccess.EmpMapper.insertEmpUsingSelectKey", vo);
		BigDecimal selectKey = vo.getEmpNo();

		assertEquals(new BigDecimal(7935), selectKey);

		// select
		EmpVO resultVO = empMapper.selectEmp("org.egovframe.rte.psl.dataaccess.mapper.EmployerMapper.selectEmployer", vo);
		assertEquals(vo.getEmpNo(), resultVO.getEmpNo());
		checkResult(vo, resultVO);

		// delete all
		empMapper.delete("org.egovframe.rte.psl.dataaccess.EmpMapper.deleteEmpAll", null);

		// 두번째 테스트
		EmpVO vo2 = makeVO();

		empMapper.insertEmpUsingSelectKey("org.egovframe.rte.psl.dataaccess.EmpMapper.insertEmpUsingSelectKey", vo2);
		BigDecimal selectKey2 = vo2.getEmpNo();

		assertEquals(new BigDecimal(1000), selectKey2);

		EmpVO resultVO2 = empMapper.selectEmp("org.egovframe.rte.psl.dataaccess.mapper.EmployerMapper.selectEmployer", vo2);
		assertEquals(vo2.getEmpNo(), resultVO2.getEmpNo());
		checkResult(vo2, resultVO2);

	}

}
