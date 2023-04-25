package org.egovframe.rte.psl.dataaccess.ibatis;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.dao.TypeTestDAO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2014.01.22 권윤정  SimpleJdbcTestUtils -> JdbcTestUtils 변경
 *   2014.01.22 권윤정  SimpleJdbcTemplate -> JdbcTemplate 변경
 *   2014.01.22 권윤정  SLF4J로 로깅방식 변경
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-*.xml" })
@Transactional
public class TypeHandlerTest extends TestBase {

	@Resource(name = "typeTestDAO")
	TypeTestDAO typeTestDAO;

	@Before
	public void onSetUp() throws Exception {

		// 외부에 sql file 로부터 DB 초기화 (TypeTest 기존 테이블
		// 삭제/생성)
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_ddl_typetest_" + usingDBMS + ".sql"));
	}

	public Map<String, Object> makeMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", 1);

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault());
		// ex.) 20090221171025
		String strDate = sdf.format(cal.getTime());

		map.put("calendarType", cal);
		map.put("strDate", strDate);

		LoggerFactory.getLogger(this.getClass()).debug("== input map : {} ==", map);

		return map;
	}

	public void checkResult(Map<String, Object> map, Map<String, Object> resultMap) {
		assertNotNull(resultMap);
		assertEquals(map.get("id"), resultMap.get("id"));

		// mysql 의 경우 timestamp 정밀도가 3자리 낮음
		if (isMysql || isTibero) {

			Calendar cal = (Calendar) map.get("calendarType");
			Calendar resultCal = (Calendar) resultMap.get("calendarType");
			String orgMiliSecs = Long.toString(cal.getTimeInMillis());
			String resultMiliSecs = Long.toString(resultCal.getTimeInMillis());
			assertEquals(orgMiliSecs.substring(0, orgMiliSecs.length() - 3), resultMiliSecs.substring(0, resultMiliSecs.length() - 3));
		} else {
			assertEquals(map.get("calendarType"), resultMap.get("calendarType"));
		}
		assertEquals(isHsql || isMysql ? ((String) map.get("strDate")).substring(0, 8) + "000000" : map.get("strDate"), resultMap.get("strDate"));
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Rollback(false)
	@Test
	public void testTypeHandlerTest() throws Exception {

		Map<String, Object> map = makeMap();

		// insert
		typeTestDAO.getSqlMapClientTemplate().insert("insertTypeHandlerTest", map);

		// select
		Map<String, Object> resultMap = (Map<String, Object>) typeTestDAO.getSqlMapClientTemplate().queryForObject("selectTypeHandlerTest", map);

		// check
		checkResult(map, resultMap);
	}

}
