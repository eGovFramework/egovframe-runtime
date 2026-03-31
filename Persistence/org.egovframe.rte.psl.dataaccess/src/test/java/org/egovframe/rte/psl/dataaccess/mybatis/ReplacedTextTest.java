package org.egovframe.rte.psl.dataaccess.mybatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.MapTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
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
public class ReplacedTextTest {

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
    public void testReplaceTextOrderBy() {
        Map<String, Object> map = new HashMap<>();
        map.put("orderExpr", "DEPT_NO");

        List<Map<?, ?>> resultList = mapTypeMapper.selectDeptList("org.egovframe.rte.psl.dataaccess.EmpMapper.selectUsingReplacedOrderBy", map);

        // check
        assertNotNull(resultList);
        assertEquals(4, resultList.size());
        assertEquals(new BigDecimal(10), resultList.get(0).get("deptNo"));
        assertEquals(new BigDecimal(20), resultList.get(1).get("deptNo"));
        assertEquals(new BigDecimal(30), resultList.get(2).get("deptNo"));
        assertEquals(new BigDecimal(40), resultList.get(3).get("deptNo"));

        map.clear();
        map.put("orderExpr", "DEPT_NAME ASC");

        // select
        resultList = mapTypeMapper.selectDeptList("org.egovframe.rte.psl.dataaccess.EmpMapper.selectUsingReplacedOrderBy", map);

        // check
        assertNotNull(resultList);
        assertEquals(4, resultList.size());
        assertEquals("ACCOUNTING", resultList.get(0).get("deptName"));
        assertEquals("OPERATIONS", resultList.get(1).get("deptName"));
        assertEquals("RESEARCH", resultList.get(2).get("deptName"));
        assertEquals("SALES", resultList.get(3).get("deptName"));

        map.clear();

        StringBuilder complexExpr = new StringBuilder();
        complexExpr.append(" ( select max(EMP_NO) ");
        complexExpr.append("   from   EMP B ");
        complexExpr.append("   where  DEPT.DEPT_NO = B.DEPT_NO ) ");

        map.put("orderExpr", complexExpr.toString());

        // select
        resultList = mapTypeMapper.selectDeptList("org.egovframe.rte.psl.dataaccess.EmpMapper.selectUsingReplacedOrderBy", map);

        // check
        assertNotNull(resultList);
        assertEquals(4, resultList.size());
        assertEquals(new BigDecimal(40), resultList.get(0).get("deptNo"));
        assertEquals(new BigDecimal(30), resultList.get(1).get("deptNo"));
        assertEquals(new BigDecimal(20), resultList.get(2).get("deptNo"));
        assertEquals(new BigDecimal(10), resultList.get(3).get("deptNo"));

        map.put("orderExpr", complexExpr.toString().replaceAll("max", "min"));

        // select
        resultList = mapTypeMapper.selectDeptList("org.egovframe.rte.psl.dataaccess.EmpMapper.selectUsingReplacedOrderBy", map);

        // check
        assertNotNull(resultList);
        assertEquals(4, resultList.size());
        assertEquals(new BigDecimal(40), resultList.get(0).get("deptNo"));
        assertEquals(new BigDecimal(20), resultList.get(1).get("deptNo"));
        assertEquals(new BigDecimal(30), resultList.get(2).get("deptNo"));
        assertEquals(new BigDecimal(10), resultList.get(3).get("deptNo"));
    }

    @Rollback(false)
    @Test
    public void testReplaceTextTable() {
        Map<String, Object> map = new HashMap<>();
        map.put("table", "DEPT");

        // select
        List<Map<String, Object>> resultList = mapTypeMapper.selectList("org.egovframe.rte.psl.dataaccess.EmpMapper.selectUsingReplacedTable", map);

        // check
        assertNotNull(resultList);
        assertEquals(4, resultList.size());
        assertTrue(resultList.get(0).containsKey("deptNo"));

        map.clear();
        map.put("table", "DEPT");
        map.put("orderExpr", "DEPT_NO");

        // select
        resultList = mapTypeMapper.selectList("org.egovframe.rte.psl.dataaccess.EmpMapper.selectUsingReplacedTable", map);

        // check
        assertNotNull(resultList);
        assertEquals(4, resultList.size());
        assertEquals(new BigDecimal(10), resultList.get(0).get("deptNo"));
        assertEquals(new BigDecimal(20), resultList.get(1).get("deptNo"));
        assertEquals(new BigDecimal(30), resultList.get(2).get("deptNo"));
        assertEquals(new BigDecimal(40), resultList.get(3).get("deptNo"));

        map.clear();
        map.put("table", "EMP");
        map.put("orderExpr", "EMP_NO");

        // select
        resultList = mapTypeMapper.selectList("org.egovframe.rte.psl.dataaccess.EmpMapper.selectUsingReplacedTable", map);

        // check
        assertNotNull(resultList);
        assertEquals(14, resultList.size());
        assertEquals(new BigDecimal(7369), resultList.get(0).get("empNo"));
        assertEquals(new BigDecimal(7499), resultList.get(1).get("empNo"));
        assertEquals(new BigDecimal(7521), resultList.get(2).get("empNo"));
        assertEquals(new BigDecimal(7566), resultList.get(3).get("empNo"));
        assertEquals(new BigDecimal(7654), resultList.get(4).get("empNo"));
        assertEquals(new BigDecimal(7698), resultList.get(5).get("empNo"));
        assertEquals(new BigDecimal(7782), resultList.get(6).get("empNo"));
        assertEquals(new BigDecimal(7788), resultList.get(7).get("empNo"));
        assertEquals(new BigDecimal(7839), resultList.get(8).get("empNo"));
        assertEquals(new BigDecimal(7844), resultList.get(9).get("empNo"));
        assertEquals(new BigDecimal(7876), resultList.get(10).get("empNo"));
        assertEquals(new BigDecimal(7900), resultList.get(11).get("empNo"));
        assertEquals(new BigDecimal(7902), resultList.get(12).get("empNo"));
        assertEquals(new BigDecimal(7934), resultList.get(13).get("empNo"));
    }

    @Rollback(false)
    @Test
    public void testReplaceTextAllQuery() {
        Map<String, Object> map = new HashMap<String, Object>();

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

        // select
        resultList = mapTypeMapper.selectList("org.egovframe.rte.psl.dataaccess.EmpMapper.selectUsingReplacedAllQuery", map);

        assertNotNull(resultList);
        assertEquals(2, resultList.size());
        assertTrue(resultList.get(0).containsKey("deptNo"));
    }

}
