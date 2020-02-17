package egovframework.rte.fdl.logging;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath*:META-INF/spring/context-common.xml",
		"classpath*:META-INF/spring/context-datasource.xml",
		"classpath*:META-INF/spring/context-connectionFactory.xml"})
public class AdvancedLogTargetTest {
	
	@Resource(name = "dataSource")
	DataSource dataSource;

	@Value("#{jdbcProperties['usingDBMS']}") 
	private String usingDBMS;
	
    @Before
    public void onSetUp() throws Exception {
        JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource("META-INF/testdata/dialect/" + usingDBMS + ".sql"), true);
    }

    
	/**
	 * 아래는 JDBCAppender를 테스트하는 메소드 
	 * logger: level=ERROR, appender=JDBC, tableName=db_log
	 * */
	@Test
	public void testJDBCAppender() {
		Logger logger = LogManager.getLogger("dbLogger");

		// 로그 출력
		logger.trace("trace");
		logger.debug("debug");
		logger.info("info");
		logger.warn("warn");
		logger.error("error");
		logger.fatal("fatal");


		// 로그 확인
		
		// db_log 테이블에 저장된 로그 조회
		String sql = "SELECT * FROM db_log";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		// error레벨 이상 로그만 출력되므로 error, fatal 로그만 저장됨
		List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
		assertEquals(2, resultList.size());
		for(Map<String, Object> result : resultList) {
			// db_log 테이블에 저장된 로그를 콘솔에 출력
			LogManager.getLogger("egovframework").debug(result);
		}
		
		// 저장된 error, fatal 로그의 각 컬럼 확인
		assertEquals("ERROR", resultList.get(0).get("level"));
		assertEquals("dbLogger", resultList.get(0).get("logger"));
		assertEquals("error", resultList.get(0).get("message"));
		
		assertEquals("FATAL", resultList.get(1).get("level"));
		assertEquals("dbLogger", resultList.get(1).get("logger"));
		assertEquals("fatal", resultList.get(1).get("message"));
	
	}
}
