package org.egovframe.rte.psl.dataaccess.ibatis;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.dao.EmpDAO;
import org.egovframe.rte.psl.dataaccess.vo.EmpVO;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
public class BatchTest extends TestBase {

	@Resource(name = "empDAO")
	EmpDAO empDAO;

	@Before
	public void onSetUp() throws Exception {

		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_ddl_" + usingDBMS + ".sql"));
	}

	public EmpVO makeVO() throws Exception {
		return makeVO(1000);
	}

	public EmpVO makeVO(int id) throws Exception {
		EmpVO vo = new EmpVO();

		vo.setEmpNo(new BigDecimal(id));
		// vo.setEmpName("name" + (id== 1500 ?
		// id+"exception" : id));
		vo.setEmpName("name" + id);
		vo.setJob("CLERK");

		return vo;
	}

	private List<EmpVO> makeVOList() throws Exception {
		List<EmpVO> list = new ArrayList<EmpVO>();
		// 1000건 배치 입력 테스트 (1000 ~ 1999)
		for (int i = 0; i < 1000; i++) {
			list.add(makeVO(1000 + i));
		}
		return list;
	}

	@Test
	public void testBatchInsert() throws Exception {
		List<EmpVO> list = makeVOList();

		// insert
		// 현재 spring 연계 ibatis 의 batch 형식으로 작성 후 중간에
		// exception 발생시켜도 rollback 이 불가한 문제가 있음.
		// ibatis 의 batch 관련하여서는
		// sqlMapClient.startTransaction() 이하의 코드 등 추가
		// 작업이 필요한지 확인 필요!
		Integer rowsAffected = (Integer) empDAO.batchInsertEmp("insertEmpUsingBatch", list);

		// Oracle 인 경우 executeBatch() 의 건수를 알 수 없음.
		// ref.)
		// http://www.mail-archive.com/dev@ibatis.apache.org/msg01074.html
		assertEquals(isOracle ? 0 : 1000, rowsAffected.intValue());
	}

}
