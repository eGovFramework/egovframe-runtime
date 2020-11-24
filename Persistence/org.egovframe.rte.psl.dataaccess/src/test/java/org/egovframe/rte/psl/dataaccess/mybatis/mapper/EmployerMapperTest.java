package org.egovframe.rte.psl.dataaccess.mybatis.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.mapper.EmployerMapper;
import org.egovframe.rte.psl.dataaccess.vo.EmpVO;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
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
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-*.xml" })
@Transactional
public class EmployerMapperTest extends TestBase {

	@Resource(name = "employerMapper")
	EmployerMapper employerMapper;

	@Before
	public void onSetUp() throws Exception {
		// 외부에 sql file 로부터 DB 초기화 (기존 테이블 삭제/생성 및 초기데이터 구축)
		// Spring 의 JdbcTestUtils 사용,
		// continueOnError 플래그는 true로 설정 - cf.) DDL 이 포함된 경우 rollback 에 유의
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"));
	}

	public EmpVO makeVO() {
		EmpVO vo = new EmpVO();
		vo.setEmpNo(new BigDecimal(100));
		vo.setEmpName("홍길동");
		vo.setJob("대리");
		return vo;
	}

	public void checkResult(EmpVO vo, EmpVO resultVO) {
		assertNotNull(resultVO);
		assertEquals(vo.getEmpNo(), resultVO.getEmpNo());
		assertEquals(vo.getEmpName(), resultVO.getEmpName());
		assertEquals(vo.getJob(), resultVO.getJob());
	}

	@Test
	public void testInsert() throws Exception {
		EmpVO vo = makeVO();

		// insert
		employerMapper.insertEmployer(vo);

		// select
		EmpVO resultVO = employerMapper.selectEmployer(vo.getEmpNo());

		// check
		checkResult(vo, resultVO);
	}

	@Test
	public void testUpdate() throws Exception {
		EmpVO vo = makeVO();

		// insert
		employerMapper.insertEmployer(vo);

		// data change
		vo.setEmpName("홍길서");
		vo.setJob("과장");

		// update
		int effectedRows = employerMapper.updateEmployer(vo);
		assertEquals(1, effectedRows);

		// select
		EmpVO resultVO = employerMapper.selectEmployer(vo.getEmpNo());

		// check
		checkResult(vo, resultVO);
	}

	@Test
	public void testDelete() throws Exception {
		EmpVO vo = makeVO();

		// insert
		employerMapper.insertEmployer(vo);

		// delete
		int effectedRows = employerMapper.deleteEmployer(vo.getEmpNo());
		assertEquals(1, effectedRows);

		// select
		EmpVO resultVO = employerMapper.selectEmployer(vo.getEmpNo());

		// null 이어야 함
		assertNull(resultVO);
	}

	@Test
	public void testSelectList() throws Exception {
		EmpVO vo = makeVO();

		// insert
		employerMapper.insertEmployer(vo);

		// 검색조건으로 key 설정
		EmpVO searchVO = new EmpVO();
		searchVO.setEmpName("홍길");

		// selectList
		List<EmpVO> resultList = employerMapper.selectEmployerList(searchVO);

		// key 조건에 대한 결과는 한건일 것임
		assertNotNull(resultList);
		assertTrue(resultList.size() > 0);
		assertEquals(1, resultList.size());
		// assertTrue(resultList.get(0) instanceof DeptVO);
		checkResult(vo, resultList.get(0));

	}
}
