package org.egovframe.rte.psl.dataaccess.ibatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.TypeTestDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2014.01.22 권윤정  SimpleJdbcTestUtils -> JdbcTestUtils 변경
 * 2014.01.22 권윤정  SimpleJdbcTemplate -> JdbcTemplate 변경
 * 2014.01.22 권윤정  SLF4J로 로깅방식 변경
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DataAccessTestConfig.class)
@Transactional
public class TypeHandlerTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "typeTestDAO")
    private TypeTestDAO typeTestDAO;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    public Map<String, Object> makeMap() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault());
        String strDate = sdf.format(cal.getTime());

        map.put("calendarType", cal);
        map.put("strDate", strDate);

        LoggerFactory.getLogger(this.getClass()).debug("== input map : {} ==", map);

        return map;
    }

    public void checkResult(Map<String, Object> map, Map<String, Object> resultMap) {
        assertNotNull(resultMap);
        assertEquals(map.get("id"), resultMap.get("id"));
        assertEquals(map.get("calendarType"), resultMap.get("calendarType"));
        assertEquals(((String) map.get("strDate")).substring(0, 8) + "000000", ((String) resultMap.get("strDate")).substring(0, 8) + "000000");
    }

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
