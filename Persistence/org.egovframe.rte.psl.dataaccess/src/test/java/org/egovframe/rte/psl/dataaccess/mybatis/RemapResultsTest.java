package org.egovframe.rte.psl.dataaccess.mybatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.MapTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2014.01.22 권윤정  SimpleJdbcTestUtils -> JdbcTestUtils 변경
 * 2014.01.22 권윤정  SimpleJdbcTemplate -> JdbcTemplate 변경
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DataAccessTestConfig.class)
@Transactional
public class RemapResultsTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "mapTypeMapper")
    private MapTypeMapper mapTypeMapper;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    @Rollback(false)
    @Test
    public void testReplaceTextAllQueryExpectedException() {
        try {
            Map<String, Object> map = new HashMap<>();

            StringBuilder selectQuery = new StringBuilder();
            selectQuery.append("select * from DEPT");

            map.put("selectQuery", selectQuery.toString());

            // select
            List<Map<String, Object>> resultList = mapTypeMapper.selectList("org.egovframe.rte.psl.dataaccess.EmpMapper.selectUsingReplacedAllQuery", map);

            assertNotNull(resultList);
            assertEquals(4, resultList.size());
            assertTrue(resultList.get(0).containsKey("deptNo"));

            map.clear();
            selectQuery = new StringBuilder();
            selectQuery.append("select * from DEPT ");
            selectQuery.append("where DEPT_NAME like '%ES%' ");
            selectQuery.append("order by DEPT_NO DESC ");

            map.put("selectQuery", selectQuery.toString());

            resultList = mapTypeMapper.selectList("org.egovframe.rte.psl.dataaccess.EmpMapper.selectUsingReplacedAllQuery", map);

            assertNotNull(resultList);
            assertEquals(2, resultList.size());
            assertTrue(resultList.get(0).containsKey("deptNo"));

            map.clear();
            selectQuery = new StringBuilder();
            selectQuery.append("select * from EMP ");

            map.put("selectQuery", selectQuery.toString());

            resultList = mapTypeMapper.selectList("org.egovframe.rte.psl.dataaccess.EmpMapper.selectUsingReplacedAllQuery", map);

        } catch (BadSqlGrammarException | UncategorizedSQLException e) {
            assertNotNull(e);
            fail("기대한 exception 이 아닙니다.");
        }
    }

    @Rollback(false)
    @Test
    public void testReplaceTextRemapResultsAllQuery() {
        Map<String, Object> map = new HashMap<>();

        StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("select * from DEPT");

        map.put("selectQuery", selectQuery.toString());

        // select
        List<Map<String, Object>> resultList = mapTypeMapper.selectList("org.egovframe.rte.psl.dataaccess.EmpMapper.selectUsingReplacedAllQueryUsingRemapResults", map);

        assertNotNull(resultList);
        assertEquals(4, resultList.size());
        assertTrue(resultList.get(0).containsKey("deptNo"));

        map.clear();
        selectQuery = new StringBuilder();
        selectQuery.append("select * from EMP ");

        map.put("selectQuery", selectQuery.toString());

        resultList = mapTypeMapper.selectList("org.egovframe.rte.psl.dataaccess.EmpMapper.selectUsingReplacedAllQueryUsingRemapResults", map);

        assertNotNull(resultList);
        assertEquals(14, resultList.size());
        assertTrue(resultList.get(0).containsKey("empNo"));
    }

}
